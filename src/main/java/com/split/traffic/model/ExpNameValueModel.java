package com.split.traffic.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

//@Data
public class ExpNameValueModel implements Serializable {
    @NotNull
    @NotBlank
    private String expName;

    @NotNull
    @NotEmpty
    private List<ExpValueModel> expValues;

    public String getExpName() {
        return expName;
    }

    public void setExpName(String expName) {
        this.expName = expName;
    }

    public List<ExpValueModel> getExpValues() {
        return expValues;
    }

    public void setExpValues(List<ExpValueModel> expValues) {
        this.expValues = expValues;
    }

    public ExpNameValueModel(String expName, List<ExpValueModel> expValues) {
        this.expName = expName;
        this.expValues = expValues;
    }
}
