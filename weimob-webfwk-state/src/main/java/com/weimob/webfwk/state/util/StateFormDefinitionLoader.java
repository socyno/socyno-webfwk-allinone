package com.weimob.webfwk.state.util;

import lombok.Getter;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.weimob.webfwk.state.service.StateFormService;

@Getter
public class StateFormDefinitionLoader implements ApplicationListener<ContextRefreshedEvent> {
    
    private final String loadBackend;
    
    public StateFormDefinitionLoader() {
        this(null);
    }
    
    public StateFormDefinitionLoader(String loadBackend) {
        this.loadBackend = loadBackend;
    }
    
    public void onApplicationEvent(ContextRefreshedEvent event)  {
        // 判断SPRING容器是否加载完成
        if (event.getApplicationContext().getParent() == null) {
            try {
                StateFormService.parseStateFormRegister(loadBackend);
            } catch (Exception e) {
                throw new Error("加载或解析通用流程表单数据失败", e);
            }
        }
    }
}
