package com.split.traffic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExpIdentifierGetFactory {

    @Autowired
    private IExpIdentifierGetService expIdentifierDBGetService;
    @Autowired
    private IExpIdentifierGetService expIdentifierPropertyGetService;

    @Value("${traffic-split-service.exp-config.type}")
    private String expConfigType;

    private static final String PROPERTY_BASED_STR = "PROPERTY_BASED";
    private static final String DB_BASED_STR = "DB_BASED";

    public IExpIdentifierGetService getExpIdentifierService() {
        if(PROPERTY_BASED_STR.equalsIgnoreCase(expConfigType)){
            return expIdentifierPropertyGetService;
        }
        return expIdentifierDBGetService;
    }
}
