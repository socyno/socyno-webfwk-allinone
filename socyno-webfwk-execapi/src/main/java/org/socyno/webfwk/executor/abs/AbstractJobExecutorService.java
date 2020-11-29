package org.socyno.webfwk.executor.abs;

import java.net.URISyntaxException;

import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.remote.RestClient;
import org.socyno.webfwk.util.tool.CommonUtil;

public abstract class AbstractJobExecutorService {
    
    public abstract String getRemoteUrl();
    
    public int getTimeoutMS()  {
        return 60000;
    }
    
    public abstract String getTokenHead();
    
    public abstract String getTokenData();
    
    public abstract String getRealtimeStatusUrl();
        
    public final static AbstractJobExecutorService DEFAULT = new AbstractJobExecutorService() {
        @Override
        public String getRemoteUrl() {
            // return "http://localhost:8080/webfwk-executor";
            return CommonUtil.ifBlank(ContextUtil.getConfigTrimed("webfwk.executor.restful.url"), 
                    "http://localhost:8080/webfwk-executor");
        }
        
        @Override
        public int getTimeoutMS() {
            return CommonUtil.parseInteger(ContextUtil.getConfigTrimed("webfwk.executor.restful.timeout"), 60000);
        }
        
        @Override
        public String getTokenHead() {
            return SessionContext.getTokenHead();
        }
        
        @Override
        public String getTokenData() {
            return SessionContext.getToken();
        }
        
        @Override
        public String getRealtimeStatusUrl() {
            return CommonUtil.ifBlank(ContextUtil.getConfigTrimed("webfwk.executor.websocket.url"), 
                            "/webfwk-gateway/ws/webfwk-executor/ws/async/task");
        }
    };
    
    protected RestClient getService() throws URISyntaxException {
        RestClient restfulClient = new RestClient(getRemoteUrl());
        restfulClient.setTimeoutMS(getTimeoutMS());
        return restfulClient;
    }
}
