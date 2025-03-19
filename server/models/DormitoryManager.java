package cn.edu.sdu.java.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
@Table( name = "dormitorymanager",
        uniqueConstraints = {
        })
public class DormitoryManager {

    @Id
    private Integer personId;

    @OneToOne
    @JoinColumn(name="personId")
    @JsonIgnore
    private Person person;

    @Size(max = 50)
    private String manageArea;

    @Size(max = 20)
    private String enterTime;

    private Integer studentNum;
}
