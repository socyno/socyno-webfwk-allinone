package org.socyno.webfwk.state.module.option;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

import org.socyno.webfwk.state.abs.AbstractStateAction;
import org.socyno.webfwk.state.abs.AbstractStateCreateAction;
import org.socyno.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.field.AbstractFieldDynamicStandard;
import org.socyno.webfwk.state.field.OptionDynamicStandard;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class DynamicFieldOptionService extends
        AbstractStateFormServiceWithBaseDao<DynamicFieldOptionFormSimple, DynamicFieldOptionFormSimple, DynamicFieldOptionFormSimple> {
    
    private DynamicFieldOptionService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final DynamicFieldOptionService Instance = new DynamicFieldOptionService();
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        ENABLED("enabled", "已启用"),
        DISABLED("disabled", "已禁用"),
        
        ;
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        
        /**
         * 新增
         */
        Create(EventCreate.class),
        
        /**
         * 编辑
         */
        Edit(EventEdit.class),;
        private final Class<? extends AbstractStateAction<DynamicFieldOptionFormSimple, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<DynamicFieldOptionFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<DynamicFieldOptionFormSimple, DynamicFieldOptionFormCreation> {
        public EventCreate() {
            super("新增", STATES.ENABLED.getCode());
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, DynamicFieldOptionFormSimple originForm,
                           final DynamicFieldOptionFormCreation form, final String message) throws Exception {
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet result, Connection conn) throws Exception {
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                            getFormTable(),
                            new ObjectMap()
                                .put("class_path", form.getClassPath())
                                .put("description", form.getDescription())
                    ), new ResultSetProcessor() {
                        @Override
                        public void process(ResultSet result, Connection conn) throws Exception {
                            result.next();
                            form.setId(result.getLong(1));
                        }
                    });
                    saveOptionValues(form.getId(), form.getValues());
                }
            });
            return new StateFormEventResultCreateViewBasic(form.getId());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, DynamicFieldOptionFormSimple originForm, String sourceState) {
            
        }
    }
    
    public class EventEdit extends AbstractStateAction<DynamicFieldOptionFormSimple, DynamicFieldOptionFormCreation, Void> {
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        public Void handle(String event, DynamicFieldOptionFormSimple originForm, final DynamicFieldOptionFormCreation form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(),
                    new ObjectMap().put("=id", originForm.getId())
                                   .put("class_path", form.getClassPath())
                                   .put("description", form.getDescription())
                                   
            ));
            saveOptionValues(originForm.getId(), form.getValues());
            return null;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, DynamicFieldOptionFormSimple originForm, String sourceState) {
            
        }
    }
    
    private boolean saveOptionValues(long formId, Collection<DynamicFieldOptionEntity> options) throws Exception {
        if (options == null) {
            return false;
        }
        getFormBaseDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet result, Connection conn) throws Exception {
                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                        AbstractFieldDynamicStandard.getValuesTableName(),
                        new ObjectMap().put("=class_path", formId)
                    ));
                for (DynamicFieldOptionEntity option : options) {
                    if (option == null) {
                        continue;
                    }
                    if (OptionDynamicStandard.parseProperties(option.getProperties(), null) == null) {
                        throw new MessageException(String.format("选项(值 = %s)的属性配置内容解析错误", option.getValue()));
                    }
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                            AbstractFieldDynamicStandard.getValuesTableName(),
                            new ObjectMap()
                                .put("class_path", formId)
                                .put("=category", StringUtils.trimToEmpty(option.getCategory()))
                                .put("=value", StringUtils.trimToEmpty(option.getValue()))
                                .put("=group", StringUtils.trimToEmpty(option.getGroup()))
                                .put("=display", StringUtils.trimToEmpty(option.getDisplay()))
                                .put("=properties", StringUtils.trimToEmpty(option.getProperties()))
                                .put("=disabled", option.isDisabled())
                    ));
                }
            }
        });
        return true;
    }
    
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<DynamicFieldOptionFormSimple>(
            "默认查询",
            DynamicFieldOptionFormSimple.class,
            DynamicFieldOptionQueryDefault.class)
        );
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Override
    protected String getFormTable() {
        return AbstractFieldDynamicStandard.getFormTableName();
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return getFormTable();
    }
    
    @Override
    public String getFormDisplay() {
        return "流程动态选项";
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends DynamicFieldOptionFormSimple> forms) throws Exception {
        if (forms == null || forms.size() <= 0) {
            return;
        }
        List<DynamicFieldOptionFormSimple> sameForms;
        Map<Long, List<DynamicFieldOptionFormSimple>> mappedForms = new HashMap<>();
        for (DynamicFieldOptionFormSimple form : forms) {
            if (form == null || form.getId() == null) {
                continue;
            }
            if ((sameForms = mappedForms.get(form.getId())) == null) {
                mappedForms.put(form.getId(), sameForms = new ArrayList<>());
            }
            sameForms.add(form);
        }
        
        if (mappedForms.size() > 0) {
            List<DynamicFieldOptionEntity> flattedAllOptions = getFormBaseDao().queryAsList(
                    DynamicFieldOptionEntity.class, String.format(
                        "SELECT * FROM %s WHERE class_path IN (%s)", 
                        AbstractFieldDynamicStandard.getValuesTableName(),
                        CommonUtil.join("?", mappedForms.size(), ",")
                    ), mappedForms.keySet().toArray());
            if (flattedAllOptions != null && flattedAllOptions.size() > 0) {
                List<DynamicFieldOptionEntity> sameFormOptions;
                Map<Long, List<DynamicFieldOptionEntity>> mappedFormOptions = new HashMap<>();
                for (DynamicFieldOptionEntity o : flattedAllOptions) {
                    if ((sameFormOptions = mappedFormOptions.get(o.getClassPath())) == null) {
                        mappedFormOptions.put(o.getClassPath(), sameFormOptions = new ArrayList<>());
                    }
                    sameFormOptions.add(o);
                }
                for (Map.Entry<Long, List<DynamicFieldOptionFormSimple>> e : mappedForms.entrySet()) {
                    for (DynamicFieldOptionFormSimple form : e.getValue()) {
                        form.setValues(mappedFormOptions.get(e.getKey()));
                    }
                }
            }
        }
    }
}