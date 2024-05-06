//package com.split.traffic.filters;
//
//import com.split.traffic.model.ExpNameValueModel;
//import com.split.traffic.model.ExpValueModel;
//import com.split.traffic.service.ExpIdentifierGetFactory;
//import com.split.traffic.service.IExpIdentifierGetService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
//@Component
//public class ExpIdentifierFilter1 extends OncePerRequestFilter {
//
//    @Autowired
//    private ExpIdentifierGetFactory expIdentifierGetFactory;
//
//    @Value("${traffic-split-service.isExperimentationActive:true}")
//    private boolean isExperimentationActive;
//
//    private static final String EXP_ID = "expId";
//    private static final int LOWER_LIMIT = 1;
//    private static final int UPPER_LIMIT = 100;
//    private static final String EXP_HEADER_NAME = "X-Exp";
//    public static final int MAX_AGE = 3600 * 24 * 7;
//    private static Map<String, AtomicInteger> CIRCULAR_QUEUE_INDEX = new HashMap<>();
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        if(!isExperimentationActive) {
//            filterChain.doFilter(request,response);
//        }
//
//        IExpIdentifierGetService expIdentifierGetService = expIdentifierGetFactory.getExpIdentifierService();
//        List<ExpNameValueModel> expNameValueModelList = expIdentifierGetService.getAllActiveExperiments();
//        if(expNameValueModelList != null && !expNameValueModelList.isEmpty()) {
//            expNameValueModelList = filterAlreadyActiveExperiments(request, response, expNameValueModelList);
//            if(expNameValueModelList != null && !expNameValueModelList.isEmpty()) {
//                for (ExpNameValueModel expNameValueModel : expNameValueModelList) {
//                    if (expNameValueModel != null && expNameValueModel.getExpName() != null && expNameValueModel.getExpName().length() > 0
//                            && expNameValueModel.getExpName() != null && expNameValueModel.getExpValues().size() > 0) {
//                        //todo: write code here to check if the cookie of expNameValueModel.getExpName() is already present or not. If already present, then skip below logic.
//                        int expIdentifierValue = getExpIdentifier(expNameValueModel.getExpName());
//                        System.out.println(expIdentifierValue);
//                        String expValue = getExpValue(expIdentifierValue+1, expNameValueModel);
//                        request.setAttribute(expNameValueModel.getExpName(), expValue);
//                        response.addHeader(expNameValueModel.getExpName(), expValue);
//                        response.addHeader("Set-Cookie", expNameValueModel.getExpName() + "=" + expValue);
//                    }
//                }
//            }
//        }
//
//        filterChain.doFilter(request, response);
//
////        response.addCookie(getExpIdentifierCookie(expIdentifierValue));
//    }
//
//    private List<ExpNameValueModel> filterAlreadyActiveExperiments(HttpServletRequest request, HttpServletResponse response, List<ExpNameValueModel> expNameValueModelList) {
//        List<ExpNameValueModel> filteredExpNameValueModelList = new ArrayList<>();
//        Cookie[] cookieArr = request.getCookies();
//        for(ExpNameValueModel expNameValueModel : expNameValueModelList) {
//            if(expNameValueModel!=null && expNameValueModel.getExpName() != null && !expNameValueModel.getExpName().isEmpty()) {
//                boolean flag = false;
//                if(cookieArr != null && cookieArr.length > 0) {
//                    for (Cookie cookie : cookieArr) {
//                        if (cookie != null && cookie.getName() != null && !cookie.getName().isEmpty() && cookie.getName().equals(expNameValueModel.getExpName())) {
//                            String expValue = cookie.getValue();
//                            request.setAttribute(expNameValueModel.getExpName(), expValue);
//                            response.addHeader(expNameValueModel.getExpName(), expValue);
//                            flag = true;
//                            break;
//                        }
//                    }
//                }
//                if (!flag) {
//                    filteredExpNameValueModelList.add(expNameValueModel);
//                }
//            }
//        }
//        return filteredExpNameValueModelList;
//    }
//
//    private Cookie getExpIdentifierCookie(long expIdentifierValue) {
//        Cookie expIdentifierCookie = new Cookie(EXP_ID, Long.toString(expIdentifierValue));
//        expIdentifierCookie.setPath("/");
//        expIdentifierCookie.setHttpOnly(true);
//        expIdentifierCookie.setSecure(true);
//        expIdentifierCookie.setDomain(".trafficDivide.com");
//        expIdentifierCookie.setMaxAge(MAX_AGE);
//        return expIdentifierCookie;
//    }
//
//    //PROBABILISTIC DISTRIBUTION LOGIC
//    private String getExpValue(int expIdentifierValue, ExpNameValueModel expNameValueModel) {
//        boolean isDataValid = true;
//        for(ExpValueModel expValueModel : expNameValueModel.getExpValues()) {
//            if(expValueModel==null || expValueModel.getExpValue()==null || expValueModel.getExpValue().length()<=0
//            || expValueModel.getTrafficPercent()<0 || expValueModel.getTrafficPercent()>100) {
//                isDataValid = false;
//                break;
//            }
//        }
//        if(isDataValid) {
//            List<Integer> fractionsStrList = expNameValueModel.getExpValues().stream().map(e -> e.getTrafficPercent()).collect(Collectors.toList());
//
//            int lowerLimit = LOWER_LIMIT;
//            int upperLimit;
//            for (int i = 0; i < fractionsStrList.size(); i++) {
//                int fraction = fractionsStrList.get(i);
//                upperLimit = lowerLimit + fraction - 1;
//
//                if (expIdentifierValue >= lowerLimit && expIdentifierValue <= upperLimit) {
//                    return expNameValueModel.getExpValues().get(i).getExpValue();
//                }
//
//                lowerLimit = upperLimit + 1;
//            }
//        }
//        return expNameValueModel.getExpValues().get(expNameValueModel.getExpValues().size()-1).getExpValue();
//    }
//
//    private int getExpIdentifier(String expName) {
//        if(CIRCULAR_QUEUE_INDEX.containsKey(expName)){
//            CIRCULAR_QUEUE_INDEX.get(expName).set((CIRCULAR_QUEUE_INDEX.get(expName).get()+1)%UPPER_LIMIT);
//        }
//        else{
//            CIRCULAR_QUEUE_INDEX.put(expName, new AtomicInteger(0));
//        }
//        return CIRCULAR_QUEUE_INDEX.get(expName).get();
//    }
//}
