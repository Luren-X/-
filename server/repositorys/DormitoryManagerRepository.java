package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.DormitoryManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DormitoryManagerRepository extends JpaRepository<DormitoryManager, Integer> {
    Optional<DormitoryManager> findByPersonId(Integer personId);
    Optional<DormitoryManager> findByPersonNum(String num);
    List<DormitoryManager> findByPersonName(String name);

    @Query(value = "from DormitoryManager where ?1 = '' or person.num like %?1% or person.name like %?1% ")
    List<DormitoryManager> findDormitoryManagerListByNumName(String num);

    @Query(value = "from DormitoryManager where ?1 = '' or person.num like %?1% or person.name like %?1% ",
    countQuery = "SELECT count(personId) from DormitoryManager where ?1 = '' or person.num like %?1% or person.name like %?1% ")
    Page<DormitoryManager> findDormitoryManagerPageByNumName(String num, Pageable pageable);
}
