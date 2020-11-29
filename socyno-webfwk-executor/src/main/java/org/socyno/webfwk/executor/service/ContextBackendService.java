package org.socyno.webfwk.executor.service;

import java.io.IOException;
import java.net.URISyntaxException;
import lombok.Getter;
import lombok.NonNull;

import org.socyno.webfwk.executor.abs.AbstractBackendService;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.remote.HttpUtil;

public class ContextBackendService {
    
    @Getter
    private static final ContextBackendService instance = new ContextBackendService();
    
    protected AbstractBackendService getBackend() {
        return AbstractBackendService.BACKEND;
    }
    
    public String parseFormClassSchema(@NonNull Class<?> clazz) throws IOException, URISyntaxException {
        return getBackend().getService().get(String.class,
                String.format("/api/form/form/%s/construction", HttpUtil.urlEncode(clazz.getName())), null,
                new ObjectMap().put(SessionContext.getTokenHead(), SessionContext.getToken()).asMap());
    }

    public  <T> T queryListForm(String formName, Object data, Class<T> clazz) throws IOException, URISyntaxException {
        return getBackend().getService().post(clazz,
                String.format("/api/form/list/%s", HttpUtil.urlEncode(formName)), data, null,
                new ObjectMap().put(SessionContext.getTokenHead(), SessionContext.getToken()).asMap());
    }

    public <T> T triggerAction(String formName, String eventName, Object data, Class<T> clazz) throws IOException, URISyntaxException {
        return getBackend().getService().post(clazz,
                String.format("/api/form/trigger/%s/%s", HttpUtil.urlEncode(formName),HttpUtil.urlEncode(eventName)), data, null,
                new ObjectMap().put(SessionContext.getTokenHead(), SessionContext.getToken()).asMap());
    }

    public void triggerAction(String formName, String eventName, Object data) throws IOException, URISyntaxException {
        triggerAction(formName, eventName, data, Object.class);
    }
}
