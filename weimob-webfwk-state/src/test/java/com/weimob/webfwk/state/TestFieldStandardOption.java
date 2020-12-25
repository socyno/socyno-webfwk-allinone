package com.weimob.webfwk.state;

import org.junit.Test;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.AbstractFieldDynamicStandard;
import com.weimob.webfwk.state.field.OptionDynamicStandard;
import com.weimob.webfwk.state.util.StateFormBasicInput;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestFieldStandardOption extends AbstractFieldDynamicStandard {
    
    @Getter
    @ToString
    public static class TestForm extends StateFormBasicInput {
        
        @Attributes(title="选项002", type = TestFieldStandardOption.class)
        private OptionDynamicStandard option2;

        @Attributes(title="选项004", type = TestFieldStandardOption.class)
        private OptionDynamicStandard option4;
        
        public TestForm(String option2, String option4) {
            this.option2 = new OptionDynamicStandard(option2);
            this.option4 = new OptionDynamicStandard(option4);
        }
    }
    
    @Getter
    @ToString
    public static class TestForm2 extends TestForm {
        
        @Attributes(title="选项002", type = TestFieldStandardOption.class)
        private OptionDynamicStandard option2;

        @Attributes(title="选项004", type = TestFieldStandardOption.class)
        private OptionDynamicStandard option3;
        
        public TestForm2(String option2, String option3) {
            super(option3, null);
            this.option2 = new OptionDynamicStandard(option2);
            this.option3 = new OptionDynamicStandard(option3);
        }
    }
    
    @Test
    public void testClassToJsonSchema() throws Exception {
        TestForm form1 = new TestForm("option001", "option003");
        TestForm form2 = new TestForm("option002", "option004");
        TestForm form3 = new TestForm2("option002", "option004");
//        replaceOptionStandardFeild(form1, form2, form3);
        log.info("{}, {}", form1, form2, form3);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
