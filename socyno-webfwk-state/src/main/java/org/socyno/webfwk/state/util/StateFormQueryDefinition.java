package org.socyno.webfwk.state.util;

import org.socyno.webfwk.util.tool.ClassUtil;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class StateFormQueryDefinition {
    
    private StateFormQueryDefinition() {
        
    }
    
    private String name;
    
    private String display;

    private String formName;
    
    private String formClass;
    
    private String resultClass;
    
    public static StateFormQueryDefinition fromStateQuery(String formName, String name, StateFormNamedQuery<?> query)
        throws Exception {
        return  new StateFormQueryDefinition()
                .setName(name)
                .setDisplay(query.getName())
                .setFormName(formName)
                .setFormClass(ClassUtil.classToJson(query.getQueryClass()).toString())
                .setResultClass(ClassUtil.classToJson(query.getResultClass()).toString());
    }
}
