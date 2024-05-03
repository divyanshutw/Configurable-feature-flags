package com.split.traffic.service.implementations;

import com.split.traffic.model.ExpNameValueModel;
import com.split.traffic.model.ExpValueModel;
import com.split.traffic.service.IExpIdentifierGetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ExpIdentifierPropertyGetService implements IExpIdentifierGetService {

    @Value("#{'${traffic-split-service.exp-config.keys}'.split(';')}")
    List<String> expConfigKeyList;
    @Value("#{'${traffic-split-service.exp-config.values}'.split(';')}")
    List<String> expConfigValueList;
    @Value("#{'${traffic-split-service.exp-config.traffic-split}'.split(';')}")
    List<String> trafficSplitList;

    //todo: not adding any validations for manual errors in properties
    @Override
    public List<ExpNameValueModel> getAllActiveExperiments() {
        List<ExpNameValueModel> expNameValueModelList = new ArrayList<>();
        try {
            List<List<String>> keyWiseExpConfigValueList = keyWiseSplit(expConfigValueList);
            List<List<String>> keyWiseExpTrafficSplitList = keyWiseSplit(trafficSplitList);
            for(int i=0; i<expConfigKeyList.size(); i++){
                List<ExpValueModel> expValueModelList = new ArrayList<>();
                for(int j=0; j<keyWiseExpConfigValueList.get(i).size(); j++){
                    ExpValueModel expValueModel = new ExpValueModel(keyWiseExpConfigValueList.get(i).get(j),
                            Integer.parseInt(keyWiseExpTrafficSplitList.get(i).get(j)));
                    expValueModelList.add(expValueModel);
                }
                ExpNameValueModel expNameValueModel = new ExpNameValueModel(expConfigKeyList.get(i), expValueModelList);
                expNameValueModelList.add(expNameValueModel);
            }
        }
        catch (Exception e) {
            //There must be some manual error in property
            e.printStackTrace();
        }
        return expNameValueModelList;
    }

    private List<List<String>> keyWiseSplit(List<String> keyWiseList) {
        List<List<String>> finalList = new ArrayList<>();
        for(String str: keyWiseList) {
            finalList.add(Arrays.asList(str.split(",")));
        }
        return finalList;
    }
}
