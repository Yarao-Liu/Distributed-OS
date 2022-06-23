package com.github.mrliu.storage.entity.po;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

/**
 * @author Mr.Liu
 */
@Data
public class BaseEntity {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "CRT_TIME")
    private Date crtTime;

    @Column(name = "CRT_HOST")
    private String crtHost;

    @Column(name = "UPD_TIME")
    private Date updTime;

    @Column(name = "UPD_HOST")
    private String updHost;

    @Column(name = "ATTR1")
    private String attr1;

    @Column(name = "ATTR2")
    private String attr2;

    @Column(name = "ATTR3")
    private String attr3;

    @Column(name = "ATTR4")
    private String attr4;
}