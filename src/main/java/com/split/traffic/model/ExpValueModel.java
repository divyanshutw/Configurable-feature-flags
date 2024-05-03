package com.split.traffic.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

//@Data
public class ExpValueModel implements Serializable {
    @NotNull
    @NotBlank
    private String expValue;
    @NotNull
    private Integer trafficPercent;

    public ExpValueModel(String expValue, Integer trafficPercent) {
        this.expValue = expValue;
        this.trafficPercent = trafficPercent;
    }

    public String getExpValue() {
        return expValue;
    }

    public void setExpValue(String expValue) {
        this.expValue = expValue;
    }

    public Integer getTrafficPercent() {
        return trafficPercent;
    }

    public void setTrafficPercent(Integer trafficPercent) {
        this.trafficPercent = trafficPercent;
    }
}
