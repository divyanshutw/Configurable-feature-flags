package com.split.traffic.filters;

import com.split.traffic.model.ExpNameValueModel;
import com.split.traffic.model.ExpValueModel;
import com.split.traffic.service.ExpIdentifierGetFactory;
import com.split.traffic.service.IExpIdentifierGetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExpIdentifierFilter extends OncePerRequestFilter {

    @Autowired
    private ExpIdentifierGetFactory expIdentifierGetFactory;

    @Value("${traffic-split-service.isExperimentationActive:true}")
    private boolean isExperimentationActive;

    private static final String EXP_ID = "expId";
    private static final int LOWER_LIMIT = 1;
    private static final int UPPER_LIMIT = 100;
    private static final String EXP_HEADER_NAME = "X-Exp";
    public static final int MAX_AGE = 3600 * 24 * 7;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!isExperimentationActive) {
            filterChain.doFilter(request,response);
        }

        long expIdentifierValue = getExpIdentifier(request);
        IExpIdentifierGetService expIdentifierGetService = expIdentifierGetFactory.getExpIdentifierService();
        List<ExpNameValueModel> expNameValueModelList = expIdentifierGetService.getAllActiveExperiments();
        if(expNameValueModelList != null && !expNameValueModelList.isEmpty()) {
            for (ExpNameValueModel expNameValueModel : expNameValueModelList) {
                if(expNameValueModel!=null && expNameValueModel.getExpName()!=null && expNameValueModel.getExpName().length()>0
                && expNameValueModel.getExpName()!=null && expNameValueModel.getExpValues().size()>0) {
                    //todo: write code here to check if the cookie of expNameValueModel.getExpName() is already present or not. If already present, then skip below logic.
                    String expValue = getExpValue(expIdentifierValue, expNameValueModel);
                    request.setAttribute(expNameValueModel.getExpName(), expValue);
                    response.addHeader(expNameValueModel.getExpName(), expValue);
                    response.addHeader("Set-Cookie",expNameValueModel.getExpName()+"="+expValue);
                }
            }
        }
        request.setAttribute(EXP_HEADER_NAME, expIdentifierValue);
        response.addHeader("Set-Cookie",EXP_ID+"="+expIdentifierValue);

        filterChain.doFilter(request, response);

//        response.addCookie(getExpIdentifierCookie(expIdentifierValue));
    }

    private Cookie getExpIdentifierCookie(long expIdentifierValue) {
        Cookie expIdentifierCookie = new Cookie(EXP_ID, Long.toString(expIdentifierValue));
        expIdentifierCookie.setPath("/");
        expIdentifierCookie.setHttpOnly(true);
        expIdentifierCookie.setSecure(true);
        expIdentifierCookie.setDomain(".trafficDivide.com");
        expIdentifierCookie.setMaxAge(MAX_AGE);
        return expIdentifierCookie;
    }

    //PROBABILISTIC DISTRIBUTION LOGIC
    private String getExpValue(long expIdentifierValue, ExpNameValueModel expNameValueModel) {
        boolean isDataValid = true;
        for(ExpValueModel expValueModel : expNameValueModel.getExpValues()) {
            if(expValueModel==null || expValueModel.getExpValue()==null || expValueModel.getExpValue().length()<=0
            || expValueModel.getTrafficPercent()<0 || expValueModel.getTrafficPercent()>100) {
                isDataValid = false;
                break;
            }
        }
        if(isDataValid) {
            int expIdentifier = (int) expIdentifierValue % UPPER_LIMIT + 1;

            List<Integer> fractionsStrList = expNameValueModel.getExpValues().stream().map(e -> e.getTrafficPercent()).collect(Collectors.toList());

            long lowerLimit = LOWER_LIMIT;
            long upperLimit;
            for (int i = 0; i < fractionsStrList.size(); i++) {
                int fraction = fractionsStrList.get(i);
                upperLimit = lowerLimit + fraction - 1;

                if (expIdentifier >= lowerLimit && expIdentifier <= upperLimit) {
                    return expNameValueModel.getExpValues().get(i).getExpValue();
                }

                lowerLimit = upperLimit + 1;
            }
        }
        return expNameValueModel.getExpValues().get(expNameValueModel.getExpValues().size()-1).getExpValue();
    }

    private long getExpIdentifier(HttpServletRequest httpServletRequest) {
        Cookie[] cookieArr = httpServletRequest.getCookies();
        if(cookieArr != null && cookieArr.length > 0) {
            for (Cookie cookie : cookieArr) {
                if (cookie!=null && cookie.getName().equalsIgnoreCase(EXP_ID)) {
                    try {
                        return Long.parseLong(cookie.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        return System.currentTimeMillis();
    }
}
