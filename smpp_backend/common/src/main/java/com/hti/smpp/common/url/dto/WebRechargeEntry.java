package com.hti.smpp.common.url.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "web_recharge_entry")
public class WebRechargeEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master")
    private String master;

    private String password;
    private String targetUser;
    private String operation;
    private String amount;
    private String balanceMode;
    private String remoteAddr;
    private String role;

    // Constructors, getters, setters, and other methods

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

}
