package com.split.traffic.service.implementations;

import com.split.traffic.dao.ExpNameRepository;
import com.split.traffic.dao.ExpValueRepository;
import com.split.traffic.dao.entities.ExpNameEntity;
import com.split.traffic.dao.entities.ExpValueEntity;
import com.split.traffic.model.ExpNameValueModel;
import com.split.traffic.model.ExpValueModel;
import com.split.traffic.service.IExpIdentifierGetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExpIdentifierDBGetService implements IExpIdentifierGetService {

    @Autowired
    private ExpNameRepository expNameRepository;
    @Autowired
    private ExpValueRepository expValueRepository;

    @Override
    public List<ExpNameValueModel> getAllActiveExperiments() {
        //todo: add isActive check and startTimestamp & endTimestamp check in following
        //todo: we can add a layer of cache here
        List<ExpNameEntity> expNameEntities = expNameRepository.findAll();
        List<ExpNameValueModel> expNameValueModelList = new ArrayList<>();
        for(ExpNameEntity expNameEntity : expNameEntities) {
            List<ExpValueEntity> expValueEntityList = expValueRepository.findByExpId(expNameEntity.getExpId());
            ExpNameValueModel expNameValueModel = new ExpNameValueModel(expNameEntity.getExpName(), new ArrayList<>());
            for(ExpValueEntity expValueEntity : expValueEntityList) {
                expNameValueModel.getExpValues().add(new ExpValueModel(expValueEntity.getExpValue(), expValueEntity.getTrafficPercent()));
            }
            expNameValueModelList.add(expNameValueModel);
        }
        return expNameValueModelList;
    }
}
