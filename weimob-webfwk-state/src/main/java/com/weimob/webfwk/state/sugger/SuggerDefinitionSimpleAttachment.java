package com.weimob.webfwk.state.sugger;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.model.CommonAttachementItem;
import com.weimob.webfwk.state.service.AttachmentService;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;

import lombok.Getter;

public class SuggerDefinitionSimpleAttachment extends Definition {
    
    @Getter
    private static final SuggerDefinitionSimpleAttachment instance = new SuggerDefinitionSimpleAttachment();
    
    private final OptionClass<?> optionClass = new OptionClass<CommonAttachementItem>() {
        @Override
        protected Class<CommonAttachementItem> getType() {
            return CommonAttachementItem.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @Override
        protected Collection<CommonAttachementItem> queryOptions(Collection<CommonAttachementItem> values)
                throws Exception {
            if (values == null || values.size() <= 0) {
                return null;
            }
            Set<Long> attachementIds = new HashSet<>();
            for (CommonAttachementItem v : values) {
                attachementIds.add(v.getId());
            }
            return AttachmentService.queryByIds(attachementIds);
        }
        
        @Override
        protected void fillOriginOption(Object form, CommonAttachementItem origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}