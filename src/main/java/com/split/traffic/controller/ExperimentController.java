package com.split.traffic.controller;

import com.split.traffic.model.ExpIdTrafficModel;
import com.split.traffic.model.ExpNameValueModel;
import com.split.traffic.model.ExpValueModel;
import com.split.traffic.service.ExpIdentifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
public class ExperimentController {
//todo: not applying all types of validation....we are assuming that data entered by team is correct

    @Autowired
    private ExpIdentifierService expIdentifierService;

    private static final String EXP_HEADER_NAME = "X-Exp";

    /**
     * This is a sample API which shows how to use configured feature flags
     * API curl:
     curl --location --request GET 'localhost:8081/checkExpType?expName=exp3'
    */
    @GetMapping("/checkExpType")
    public ResponseEntity checkExpType(@RequestParam String expName) {
        Object expIdenitifier = RequestContextHolder.getRequestAttributes().getAttribute(EXP_HEADER_NAME, 0);
        Object expValue = RequestContextHolder.getRequestAttributes().getAttribute(expName, 0);
        return new ResponseEntity("I am using "+expName+" : "+expValue+" and X-exp value is "+expIdenitifier, HttpStatus.OK);
    }

    /**
     * This API is to create new experiments
     * API curl:
     curl --location --request POST 'localhost:8081/create' m\
     --header 'X-exp: gg' \
     --header 'Content-Type: application/json' \
     --data-raw '{
     "expName": "exp3",
     "expValues": [
     {
     "expValue": "tata",
     "trafficPercent": 20
     },
     {
     "expValue": "bata",
     "trafficPercent": 20
     },
     {
     "expValue": "sata",
     "trafficPercent": 60
     }
     ]
     }'
     * */
    @PostMapping("/create")
    public ResponseEntity createNewExperiment(@NotNull @Valid @RequestBody ExpNameValueModel expNameValueModel){
        if(!validateTrafficPercent(expNameValueModel.getExpValues())){
            return new ResponseEntity("Sum of traffic percent is not 100", HttpStatus.BAD_REQUEST);
        }
        expIdentifierService.createNewExp(expNameValueModel);
        return new ResponseEntity("Created", HttpStatus.valueOf(200));
    }

    /**
     * This API is to get configuration of an experiment
     * API curl:
     curl --location --request GET 'localhost:8081/getExp?expName=exp3'
     * */
    @GetMapping("/getExp")
    public ResponseEntity getExpByExpName(@NotNull @NotEmpty @RequestParam String expName){
        return new ResponseEntity(expIdentifierService.getExpNameByExpName(expName), HttpStatus.valueOf(200));
    }

    /**
     * This is an API to update anything(traffic, exp values, etc.) in exisiting  experiments
     * API curl:
     curl --location --request POST 'localhost:8081/updateExp' \
     --header 'Content-Type: application/json' \
     --data-raw '[
     {
     "id": 1,
     "trafficPercent": 40
     },
     {
     "id": 2,
     "trafficPercent": 60
     }
     ,
     {
     "id": 3,
     "trafficPercent": 0
     }
     ]'
     * */
    @PostMapping("/updateExp")
    public ResponseEntity updateExperiment(@NotNull @NotEmpty @Valid @RequestBody List<ExpIdTrafficModel> expValueModelList){
        expIdentifierService.updateExpValue(expValueModelList);
        //problem: The problem here is that even if update the traffic values, the new traffic division will be done for new users only.
        //For existing users, we want the session to be sticky, and so we cannot expect the new traffic division to be applied to old users.
        //Due to this, everytime we update the traffic split values, the results will reflect in a few days depending on daily website users.
        //We can solve this problem through multiple approaches but all approaches were heavy in computation(time-taking).
        return new ResponseEntity("Updated", HttpStatus.valueOf(200));
    }

    private boolean validateTrafficPercent(List<ExpValueModel> expValues) {
        int sum = 0;
        for (ExpValueModel expValueModel : expValues) {
            sum += expValueModel.getTrafficPercent();
        }
        return sum==100;
    }
}
