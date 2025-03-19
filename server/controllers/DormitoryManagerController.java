package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.DormitoryManagerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping("api/dormitorymanager")
public class DormitoryManagerController {

    private final DormitoryManagerService dormitoryManagerService;
    public DormitoryManagerController(DormitoryManagerService dormitoryManagerService) {
        this.dormitoryManagerService = dormitoryManagerService;
    }

    @PostMapping("/getDormitoryManagerList")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getDormitoryManagerList(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryManagerService.getDormitoryManagerList(dataRequest);
    }

    @PostMapping("/dormitoryManagerEditSave")
    @PreAuthorize(" hasRole('ADMIN')")
    public DataResponse studentEditSave(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryManagerService.dormitoryManagerEditSave(dataRequest);
    }

    @PostMapping("/getDormitoryManagerInfo")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getStudentInfo(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryManagerService.getDormitoryManagerInfo(dataRequest);
    }

    @PostMapping("/dormitoryManagerDelete")
    public DataResponse dormitoryManagerDelete(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryManagerService.dormitoryManagerDelete(dataRequest);
    }

    @PostMapping("/getDormitoryManagerListExcl")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<StreamingResponseBody> getDormitoryManagerListExcl(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryManagerService.getDormitoryManagerListExcl(dataRequest);
    }
}
