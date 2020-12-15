package org.socyno.webfwk.module.dynamicoption;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.Collections;
import java.util.List;

public class FieldDynamicFieldOptionClassPath extends FieldTableView {
    
    /**
     *  SELECT
     *  DISTINCT
     *      s.class_path
     *  FROM
     *      system_field_option s
     *  WHERE
     *      s.class_path like concat ('%', ? ,'%')
     */
    @Multiline
    private static final String SQL_QUERY_CLASS_PATH = "X";
    
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public List<OptionClassPath> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        if (StringUtils.isBlank(keyword.getKeyword())) {
            return getFormBaseDao().queryAsList(OptionClassPath.class, String
                    .format("SELECT DISTINCT s.class_path FROM %s s", DynamicFieldOptionService.getInstance().getFormName()));
        }
        return getFormBaseDao().queryAsList(OptionClassPath.class, SQL_QUERY_CLASS_PATH,
                new Object[] { keyword.getKeyword() });
    }
    
    @Getter
    @Setter
    @ToString
    public static class OptionClassPath implements FieldOption {
        
        @Attributes(title = "路径/名称")
        private String classPath;
        
        @Override
        public void setOptionValue(String s) {
            this.classPath = s;
        }
        
        @Override
        public String getOptionValue() {
            return this.classPath;
        }
        
        @Override
        public String getOptionDisplay() {
            return this.classPath;
        }
        
        public OptionClassPath() {
            
        }
    }
    
    /**
     *  SELECT
     *     s.id,
     *     s.class_path,
     *     s.category,
     *     s.option_value AS `value`,
     *     s.option_display AS `display`,
     *     s.option_group AS `group`,
     *     s.option_group AS `group`,
     *     s.option_icon AS `icon`,
     *     s.option_style AS `style`,
     *     s.disabled,
     *     s.properties
     *  FROM
     *     %s s
     *  WHERE
     *     s.class_path in (%s)
     *  ORDER BY
     *     s.class_path ASC,
     *     s.option_value ASC
     */
    @Multiline
    private static final String SQL_QUERY_CLASS_PATH_IN = "X";
    
    public List<DynamicFieldOptionEntity> queryByClassPath(String... classPath) throws Exception {
        
        if (classPath == null || classPath.length <= 0) {
            return Collections.emptyList();
        }
        
        return getFormBaseDao().queryAsList(
                DynamicFieldOptionEntity.class, String.format(SQL_QUERY_CLASS_PATH_IN,
                        DynamicFieldOptionService.getInstance().getFormTable(), CommonUtil.join("?", classPath.length, ",")),
                classPath);
    }
}
