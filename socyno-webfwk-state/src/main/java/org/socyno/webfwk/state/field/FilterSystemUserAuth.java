package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.state.authority.AuthorityScopeType;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FilterSystemUserAuth implements FieldOptionsFilter {
    
    public static class FieldOptionsScopeType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                add(FieldSimpleOption.create(AuthorityScopeType.Subsystem.name(),
                        AuthorityScopeType.Subsystem.getDisplay()));
                add(FieldSimpleOption.create(AuthorityScopeType.System.name(), AuthorityScopeType.System.getDisplay()));
            }
        };
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }
    
    @Attributes(title = "授权范围", position = 10, required = true, type = FieldOptionsScopeType.class)
    private String scopeType;
    
    @Attributes(title = "授权角色", position = 20, required = true, type = FieldSystemRole.class)
    private Long roleId;
    
    @Attributes(title = "标的关键字", position = 30)
    private String scopeTargetKeyword;
}
