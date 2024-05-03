package com.split.traffic.service;

import com.split.traffic.model.ExpNameValueModel;

import java.util.List;

public interface IExpIdentifierGetService {
    List<ExpNameValueModel> getAllActiveExperiments();
}
