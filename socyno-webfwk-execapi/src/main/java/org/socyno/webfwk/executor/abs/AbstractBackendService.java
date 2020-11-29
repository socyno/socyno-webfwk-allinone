package org.socyno.webfwk.executor.abs;

import org.apache.http.HttpResponse;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.remote.R;
import org.socyno.webfwk.util.remote.RestClient;
import org.socyno.webfwk.util.tool.CommonUtil;

import java.net.URISyntaxException;
import java.util.Map;

public abstract class AbstractBackendService {
    
    public abstract String getRemoteUrl();
    
    public int getTimeoutMS() {
        return 60000;
    }
    
    public abstract String getTokenHead();
    
    public abstract String getTokenData();
    
    public final static AbstractBackendService BACKEND = new AbstractBackendService() {
        @Override
        public String getRemoteUrl() {
            // return "http://localhost:8080/webfwk-backend";
            return CommonUtil.ifBlank(ContextUtil.getConfigTrimed("system.executor.backend.service.url"),
                    "http://localhost:9092/webfwk-backend");
        }
        
        @Override
        public int getTimeoutMS() {
            return CommonUtil.parseInteger(ContextUtil.getConfigTrimed("system.executor.backend.service.timeout"), 60000);
        }
        
        @Override
        public String getTokenHead() {
            return SessionContext.getTokenHead();
        }
        
        @Override
        public String getTokenData() {
            return SessionContext.getToken();
        }
    };
    
    public RestClient getService() throws URISyntaxException {
        RestClient restfulClient = new RestClient(getRemoteUrl()) {
            @Override
            protected R transform(String responseText, HttpResponse r) {
                final String status = "status";
                try {
                    Map<?, ?> rx = (Map<?, ?>) CommonUtil.fromJson(responseText, Map.class);
                    if (rx.get(status) != null) {
                        return super.transform(responseText, r);
                    }
                    return (new R()).setStatus(CommonUtil.parseInteger(rx.get("code"), -1))
                            .setMessage(CommonUtil.ifNull(rx.get("msg"), "").toString()).setData(rx.get("data"));
                } catch (Exception e) {
                    return null;
                }
            }
        };
        restfulClient.setTimeoutMS(getTimeoutMS());
        return restfulClient;
    }
    
}
