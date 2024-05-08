package com.cca.ia.rag.prompt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dashboard_stats")
public class DashboardStatsEntity {

    @Id
    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "value", nullable = false)
    private Long value;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
