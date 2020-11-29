package org.socyno.webfwk.state;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestFieldType {
    
    @Getter
    public static class TestCaseFeildTypes {
        private Set<String> strSet;
        private List<String> strList;
        private ArrayList<String> strArrList;
        private Collection<String> strCollection;
    }
    
    @Test
    public void testClassToJsonSchema() {
        for (Field field : TestCaseFeildTypes.class.getDeclaredFields()) {
            Class<?> type = field.getType();
            Type genericType = field.getGenericType();
            Type genericSuperclass = type.getGenericSuperclass();
            log.info("type = {}, genericType = {}, genericSuperclass = {}", 
                    type, genericType, genericSuperclass);
        }
    }
}
