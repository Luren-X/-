package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.DormitoryManagerRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.repositorys.UserTypeRepository;
import cn.edu.sdu.java.server.util.ComDataUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.*;

@Service
public class DormitoryManagerService {
    private final DormitoryManagerRepository dormitoryManagerRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder encoder;
    private final UserTypeRepository userTypeRepository;
    private final UserRepository userRepository;
    private final SystemService systemService;

    public DormitoryManagerService(DormitoryManagerRepository dormitoryManagerRepository, PersonRepository personRepository, PasswordEncoder encoder, UserTypeRepository userTypeRepository, UserRepository userRepository, SystemService systemService) {
        this.dormitoryManagerRepository = dormitoryManagerRepository;
        this.personRepository = personRepository;
        this.encoder = encoder;
        this.userTypeRepository = userTypeRepository;
        this.userRepository = userRepository;
        this.systemService = systemService;
    }

    public List<Map<String,Object>> getDormitoryMangerMapList(String numName) {
        List<Map<String,Object>> dataList = new ArrayList<>();
        List<DormitoryManager> sList = dormitoryManagerRepository.findDormitoryManagerListByNumName(numName);  //数据库查询操作
        if (sList == null || sList.isEmpty())
            return dataList;
        for (DormitoryManager dormitoryManager : sList) {
            dataList.add(getMapFromDormitoryManager(dormitoryManager));
        }
        return dataList;
    }

    public Map<String,Object> getMapFromDormitoryManager(DormitoryManager d) {
        Map<String,Object> m = new HashMap<>();
        Person p;
        if(d == null)
            return m;
        m.put("manageArea",d.getManageArea());
        m.put("studentNum",d.getStudentNum());
        m.put("enterTime",d.getEnterTime());
        p = d.getPerson();
        if(p == null)
            return m;
        m.put("personId", d.getPersonId());
        m.put("num",p.getNum());
        m.put("name",p.getName());
        m.put("card",p.getCard());
        String gender = p.getGender();
        m.put("gender",gender);
        m.put("genderName", ComDataUtil.getInstance().getDictionaryLabelByValue("XBM", gender)); //性别类型的值转换成数据类型名
        m.put("birthday", p.getBirthday());  //时间格式转换字符串
        m.put("email",p.getEmail());
        m.put("phone",p.getPhone());
        m.put("address",p.getAddress());
        m.put("introduce",p.getIntroduce());
        return m;
    }

    public DataResponse getDormitoryManagerList(DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        List<Map<String,Object>> dataList = getDormitoryMangerMapList(numName);
        return CommonMethod.getReturnData(dataList);  //按照测试框架规范会送Map的list
    }



    public List<Map<String,Object>> getDormitoryManagerMapList(String numName) {
        List<Map<String,Object>> dataList = new ArrayList<>();
        List<DormitoryManager> sList = dormitoryManagerRepository.findDormitoryManagerListByNumName(numName);  //数据库查询操作
        if (sList == null || sList.isEmpty())
            return dataList;
        for (DormitoryManager dormitoryManager : sList) {
            dataList.add(getMapFromDormitoryManager(dormitoryManager));
        }
        return dataList;
    }

    public DataResponse dormitoryManagerEditSave(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Map<String,Object> form = dataRequest.getMap("form"); //参数获取Map对象
        String num = CommonMethod.getString(form, "num");  //Map 获取属性的值
        DormitoryManager d = null;
        Person p;
        User u;
        Optional<DormitoryManager> op;
        boolean isNew = false;
        if (personId != null) {
            op = dormitoryManagerRepository.findById(personId);  //查询对应数据库中主键为id的值的实体对象
            if (op.isPresent()) {
                d = op.get();
            }
        }
        Optional<Person> nOp = personRepository.findByNum(num); //查询是否存在num的人员
        if (nOp.isPresent()) {
            if (d == null || !d.getPerson().getNum().equals(num)) {
                return CommonMethod.getReturnMessageError("新工号已经存在，不能添加或修改！");
            }
        }
        if (d == null) {
            p = new Person();
            p.setNum(num);
            p.setType("3");
            personRepository.saveAndFlush(p);  //插入新的Person记录
            personId = p.getPersonId();
            String password = encoder.encode("123456");
            u = new User();
            u.setPersonId(personId);
            u.setUserName(num);
            u.setPassword(password);
            u.setUserType(userTypeRepository.findByName(EUserType.ROLE_DORMITORYMANAGER));
            u.setCreateTime(DateTimeTool.parseDateTime(new Date()));
            u.setCreatorId(CommonMethod.getPersonId());
            userRepository.saveAndFlush(u); //插入新的User记录
            d = new DormitoryManager();   // 创建实体对象
            d.setPersonId(personId);
            dormitoryManagerRepository.saveAndFlush(d);  //插入新的Student记录
            isNew = true;
        } else {
            p = d.getPerson();
        }
        personId = p.getPersonId();
        if (!num.equals(p.getNum())) {   //如果人员编号变化，修改人员编号和登录账号
            Optional<User> uOp = userRepository.findByPersonPersonId(personId);
            if (uOp.isPresent()) {
                u = uOp.get();
                u.setUserName(num);
                userRepository.saveAndFlush(u);
            }
            p.setNum(num);  //设置属性
        }
        p.setName(CommonMethod.getString(form, "name"));
        p.setCard(CommonMethod.getString(form, "card"));
        p.setGender(CommonMethod.getString(form, "gender"));
        p.setBirthday(CommonMethod.getString(form, "birthday"));
        p.setEmail(CommonMethod.getString(form, "email"));
        p.setPhone(CommonMethod.getString(form, "phone"));
        p.setAddress(CommonMethod.getString(form, "address"));
        personRepository.save(p);  // 修改保存人员信息
        d.setManageArea(CommonMethod.getString(form, "manageArea"));
        d.setStudentNum(CommonMethod.getInteger(form, "studentNum"));
        d.setEnterTime(CommonMethod.getString(form, "enterTime"));
        dormitoryManagerRepository.save(d);  //修改保存宿管信息
        systemService.modifyLog(d,isNew);
        return CommonMethod.getReturnData(d.getPersonId());  // 将personId返回前端
    }

    public DataResponse getDormitoryManagerInfo(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        DormitoryManager d = null;
        Optional<DormitoryManager> op;
        if (personId != null) {
            op = dormitoryManagerRepository.findById(personId); //根据宿管主键从数据库查询学生的信息
            if (op.isPresent()) {
                d = op.get();
            }
        }
        return CommonMethod.getReturnData(getMapFromDormitoryManager(d)); //这里回传包含宿管信息的Map对象
    }

    public DataResponse dormitoryManagerDelete(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        DormitoryManager d = null;
        Optional<DormitoryManager> op;
        if (personId != null && personId > 0) {
            op = dormitoryManagerRepository.findById(personId);   //查询获得实体对象
            if(op.isPresent()) {
                d = op.get();
                Optional<User> uOp = userRepository.findById(personId); //查询对应该宿管的账户
                //删除对应该学生的账户
                uOp.ifPresent(userRepository::delete);
                Person p = d.getPerson();
                dormitoryManagerRepository.delete(d);    //首先数据库永久删除宿管信息
                personRepository.delete(p);   // 然后数据库永久删除宿管信息
            }
        }
        return CommonMethod.getReturnMessageOK();  //通知前端操作正常
    }

    public ResponseEntity<StreamingResponseBody> getDormitoryManagerListExcl(DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        List<Map<String,Object>> list = getDormitoryManagerMapList(numName);
        Integer[] widths = {8, 20, 10, 15, 15, 15, 25, 10, 15, 30, 20, 30};
        int i, j, k;
        String[] titles = {"序号", "工号", "姓名", "学院", "管理区域", "管理人数", "证件号码", "性别", "出生日期", "邮箱", "电话", "地址"};
        String outPutSheetName = "dormitorymanager.xlsx";
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFCellStyle styleTitle = CommonMethod.createCellStyle(wb, 20);
        XSSFSheet sheet = wb.createSheet(outPutSheetName);
        for (j = 0; j < widths.length; j++) {
            sheet.setColumnWidth(j, widths[j] * 256);
        }
        //合并第一行
        XSSFCellStyle style = CommonMethod.createCellStyle(wb, 11);
        XSSFRow row = null;
        XSSFCell[] cell = new XSSFCell[widths.length];
        row = sheet.createRow((int) 0);
        for (j = 0; j < widths.length; j++) {
            cell[j] = row.createCell(j);
            cell[j].setCellStyle(style);
            cell[j].setCellValue(titles[j]);
            cell[j].getCellStyle();
        }
        Map<String,Object> m;
        if (list != null && !list.isEmpty()) {
            for (i = 0; i < list.size(); i++) {
                row = sheet.createRow(i + 1);
                for (j = 0; j < widths.length; j++) {
                    cell[j] = row.createCell(j);
                    cell[j].setCellStyle(style);
                }
                m = list.get(i);
                cell[0].setCellValue((i + 1) + "");
                cell[1].setCellValue(CommonMethod.getString(m, "num"));
                cell[2].setCellValue(CommonMethod.getString(m, "name"));
                cell[3].setCellValue(CommonMethod.getString(m, "dept"));
                cell[4].setCellValue(CommonMethod.getString(m, "manageArea"));
                cell[5].setCellValue(CommonMethod.getString(m, "studentNum"));
                cell[6].setCellValue(CommonMethod.getString(m, "card"));
                cell[7].setCellValue(CommonMethod.getString(m, "genderName"));
                cell[8].setCellValue(CommonMethod.getString(m, "birthday"));
                cell[9].setCellValue(CommonMethod.getString(m, "email"));
                cell[10].setCellValue(CommonMethod.getString(m, "phone"));
                cell[11].setCellValue(CommonMethod.getString(m, "address"));
            }
        }
        try {
            StreamingResponseBody stream = wb::write;
            return ResponseEntity.ok()
                    .contentType(CommonMethod.exelType)
                    .body(stream);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
}
