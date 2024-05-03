package com.split.traffic.service;

import com.split.traffic.dao.ExpNameRepository;
import com.split.traffic.dao.ExpValueRepository;
import com.split.traffic.dao.entities.ExpNameEntity;
import com.split.traffic.dao.entities.ExpValueEntity;
import com.split.traffic.model.ExpIdTrafficModel;
import com.split.traffic.model.ExpNameValueModel;
import com.split.traffic.model.ExpValueModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExpIdentifierService {

    @Autowired
    private ExpNameRepository expNameRepository;
    @Autowired
    private ExpValueRepository expValueRepository;

    public void createNewExp(ExpNameValueModel expNameValueModel) {
        ExpNameEntity expNameEntity = new ExpNameEntity();
        expNameEntity.setExpName(expNameValueModel.getExpName());
        expNameEntity.setIsActive(true);
        expNameRepository.save(expNameEntity);

        for(ExpValueModel expValueModel : expNameValueModel.getExpValues()) {
            ExpValueEntity expValueEntity = new ExpValueEntity();
            expValueEntity.setExpValue(expValueModel.getExpValue());
            expValueEntity.setExpId(expNameEntity.getExpId());
            expValueEntity.setTrafficPercent(expValueModel.getTrafficPercent());
            expValueRepository.save(expValueEntity);
        }
    }

    public List<ExpValueEntity> getExpNameByExpName(String expName) {
        ExpNameEntity expNameEntity = expNameRepository.findByExpName(expName);
        if(expNameEntity!=null){
            List<ExpValueEntity> expValueModels = expValueRepository.findByExpId(expNameEntity.getExpId());
            return expValueModels;
        }
        return new ArrayList<>();
    }

    public void updateExpValue(List<ExpIdTrafficModel> expValueModelList) {
        for(ExpIdTrafficModel expValueModel : expValueModelList) {
            expValueRepository.updateTraffic(expValueModel.getId(), expValueModel.getTrafficPercent());
        }
    }
}
