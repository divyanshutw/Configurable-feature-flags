package com.split.traffic.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

//@Data
public class ExpIdTrafficModel implements Serializable {
    @NotNull
    @NotBlank
    private Long id;
    @NotNull
    private Integer trafficPercent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTrafficPercent() {
        return trafficPercent;
    }

    public void setTrafficPercent(Integer trafficPercent) {
        this.trafficPercent = trafficPercent;
    }
}
