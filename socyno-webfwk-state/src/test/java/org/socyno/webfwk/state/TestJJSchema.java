package org.socyno.webfwk.state;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.socyno.webfwk.state.module.role.SystemRoleDetail;
import org.socyno.webfwk.util.exception.FormValidationException;
import org.socyno.webfwk.util.tool.ClassUtil;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestJJSchema {
    
    @Test
    public void testClassToJsonSchema() {
        log.info("{}", ClassUtil.classToJson(SystemRoleDetail.class));
    }
    
    @Data
    @Accessors(chain = true)
    public static class TestCaseWithArrayFeild {
        @Attributes(required = true, type = TestCaseWithArrayFeildType.class)
        private String[] xxxx;
    }
    
    public static class TestCaseWithArrayFeildType extends FieldType {
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        @Override
        public List<? extends FieldOption> getStaticOptions() throws Exception {
            List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>();
            options.add(FieldSimpleOption.create("a"));
            return options;
        }
    }
    
    @Test
    public void testArrayField() throws Exception {
       log.info("{}", ClassUtil.classToJson(TestCaseWithArrayFeild.class));
       ClassUtil.checkFormRequiredAndOpValue(new TestCaseWithArrayFeild().setXxxx(new String[] {"a"}));
       try {
           ClassUtil.checkFormRequiredAndOpValue(new TestCaseWithArrayFeild().setXxxx(new String[] {"a", "b"}));
       } catch (FormValidationException e) {
           log.info("Multiple options checking OK!");
       }
       ClassUtil.checkFormRequiredAndOpValue(new TestCaseWithArrayFeild().setXxxx(new String[] {null}));
    }
}
