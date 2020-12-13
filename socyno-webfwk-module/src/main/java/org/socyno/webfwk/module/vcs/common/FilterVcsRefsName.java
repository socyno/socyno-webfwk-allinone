package org.socyno.webfwk.module.vcs.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.module.app.form.ApplicationService;
import org.socyno.webfwk.state.field.FilterBasicKeyword;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterVcsRefsName extends FilterBasicKeyword implements FieldOptionsFilter {
    
    public static class FieldOptionsVcsRefsType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                for (VcsRefsType v : VcsRefsType.values()) {
                    if (VcsRefsType.Master.equals(v)) {
                        continue;
                    }
                    add(FieldSimpleOption.create(v.name(), v.getDisplay()));
                }
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
    
    @Attributes(title = "请选择类型", required = true, position = 10, type = FieldOptionsVcsRefsType.class)
    private String vcsRefsType;
    
    public FilterVcsRefsName() {
        this(null, (String)null, null);
    }
    
    public FilterVcsRefsName(Long applicationId) {
        this(applicationId, (String)null, null);
    }
    
    public FilterVcsRefsName(Long applicationId, String vcsRefsType) {
        this(applicationId, vcsRefsType, null);
    }
    
    public FilterVcsRefsName(Long applicationId, VcsRefsType vcsRefsType) {
        this(applicationId, vcsRefsType, null);
    }
    
    public FilterVcsRefsName(Long applicationId, String vcsRefsType, String keyword) {
        this(applicationId, VcsRefsType.forName(vcsRefsType, true), keyword);
    }
    
    public FilterVcsRefsName(Long applicationId, VcsRefsType vcsRefsType, String keyword) {
        super(keyword, ApplicationService.getInstance().getFormName(), applicationId);
        if (vcsRefsType != null) {
            setVcsRefsType(vcsRefsType.name());
        }
        
    }
}
