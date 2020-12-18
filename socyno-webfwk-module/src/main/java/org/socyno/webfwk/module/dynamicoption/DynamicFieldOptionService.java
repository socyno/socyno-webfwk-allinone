package org.socyno.webfwk.module.dynamicoption;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.field.OptionDynamicStandard;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
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
    
    @Override
    protected void fillExtraFormFields(Collection<? extends DynamicFieldOptionFormSimple> forms) throws Exception {
        if (forms == null || forms.size() <= 0) {
            return;
        }
        List<DynamicFieldOptionFormSimple> listedByClassPath;
        Map<String, List<DynamicFieldOptionFormSimple>> mappedByClassPath = new HashMap<>();
        
        for (DynamicFieldOptionFormSimple form : forms) {
            if (form == null || StringUtils.isBlank(form.getClassPath())) {
                continue;
            }
            if ((listedByClassPath = mappedByClassPath.get(form.getClassPath())) == null) {
                mappedByClassPath.put(form.getClassPath(), listedByClassPath = new ArrayList<>());
            }
            listedByClassPath.add(form);
        }
        
        if (mappedByClassPath.size() > 0) {
            List<DynamicFieldOptionEntity> flattedAllOptions = ClassUtil.getSingltonInstance(FieldDynamicFieldOptionClassPath.class)
                    .queryByClassPath(mappedByClassPath.keySet().toArray(new String[0]));
            if (flattedAllOptions != null && flattedAllOptions.size() > 0) {
                List<DynamicFieldOptionEntity> listOptionsByClassPath;
                Map<String, List<DynamicFieldOptionEntity>> mappedOptionsByClassPath = new HashMap<>();
                for (DynamicFieldOptionEntity o : flattedAllOptions) {
                    if ((listOptionsByClassPath = mappedOptionsByClassPath.get(o.getClassPath())) == null) {
                        mappedOptionsByClassPath.put(o.getClassPath(), listOptionsByClassPath = new ArrayList<>());
                    }
                    listOptionsByClassPath.add(o);
                }
                for (Map.Entry<String, List<DynamicFieldOptionFormSimple>> e : mappedByClassPath.entrySet()) {
                    for (DynamicFieldOptionFormSimple form : e.getValue()) {
                        form.setOptions(mappedOptionsByClassPath.get(e.getKey()));
                    }
                }
            }
        }
    }
    
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
    
    public class EventCreate extends AbstractStateSubmitAction<DynamicFieldOptionFormSimple, DynamicFieldOptionFormCreation> {
        public EventCreate() {
            super("新增", STATES.ENABLED.getCode());
        }
        
        @Override
        public Long handle(String event, DynamicFieldOptionFormSimple originForm,
                           final DynamicFieldOptionFormCreation form, final String message) throws Exception {
            if (form.getOptions() == null || form.getOptions().size() <= 0) {
                throw new MessageException("请至少添加一条选项清单记录");
            }
            final AtomicLong simpleId = new AtomicLong(0);
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet result, Connection conn) throws Exception {
                    for (DynamicFieldOptionEntity option : form.getOptions()) {
                        if (OptionDynamicStandard.parseProperties(option.getProperties(), null) == null) {
                            throw new MessageException(String.format("选项(值 = %s)的属性配置内容解析错误", option.getValue()));
                        }
                        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                                getFormTable(),
                                new ObjectMap()
                                    .put("class_path", form.getClassPath())
                                    .put("option_value", StringUtils.trimToEmpty(option.getValue()))
                                    .put("option_group", StringUtils.trimToEmpty(option.getGroup()))
                                    .put("category", StringUtils.trimToEmpty(option.getCategory()))
                                    .put("option_display", StringUtils.trimToEmpty(option.getDisplay()))
                                    .put("option_icon", StringUtils.trimToEmpty(option.getIcon()))
                                    .put("option_style", StringUtils.trimToEmpty(option.getStyle()))
                                    .put("properties", StringUtils.trimToEmpty(option.getProperties()))
                                    .put("disabled", DynamicFieldOptionEntity.OptionDisabled.TRUE.getCode()
                                                            .equals(option.getDisabled()) ? 1 : 0)
                        ), new ResultSetProcessor() {
                            @Override
                            public void process(ResultSet result, Connection conn) throws Exception {
                                result.next();
                                simpleId.set(result.getLong(1));
                            }
                        });
                    }
                }
            });
            return simpleId.get();
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
            /**
             * 当 classpath 被修改时，必须确认新的名称未被占用
             */
            if (!StringUtils.equals(originForm.getClassPath(), form.getClassPath())) {
                List<DynamicFieldOptionEntity> existed;
                if ((existed = ClassUtil.getSingltonInstance(FieldDynamicFieldOptionClassPath.class)
                        .queryByClassPath(form.getClassPath())) != null && existed.size() > 0) {
                    throw new MessageException(String.format("动态选项(%s)名称已被占用，请使用其它名称", form.getClassPath()));
                }
            }
            
            /**
             * 首先，将当前的所有选项置为禁用状态（原则上选项只可新增或修改，禁止删除，因此删除的将以禁用处理）
             */
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(),
                    new ObjectMap().put("=class_path", originForm.getClassPath())
                                   .put("class_path", form.getClassPath())
                                   .put("disabled", 1)
                                   
            ));
            if (form.getOptions() == null || form.getOptions().size() <= 0) {
                return null;
            }
            
            /**
             * 接下来，添加或修改选项清单
             */
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet result, Connection conn) throws Exception {
                    for (DynamicFieldOptionEntity option : form.getOptions()) {
                        if (OptionDynamicStandard.parseProperties(option.getProperties(), null) == null) {
                            throw new MessageException(String.format("选项(值 = %s)的属性配置内容解析错误", option.getValue()));
                        }
                        if (option.getId() != null) {
                            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                                    getFormTable(),
                                    new ObjectMap()
                                        .put("=id", option.getId())
                                        .put("=class_path", form.getClassPath())
                                        .put("category", StringUtils.trimToEmpty(option.getCategory()))
                                        .put("option_value", StringUtils.trimToEmpty(option.getValue()))
                                        .put("option_group", StringUtils.trimToEmpty(option.getGroup()))
                                        .put("option_display", StringUtils.trimToEmpty(option.getDisplay()))
                                        .put("option_icon", StringUtils.trimToEmpty(option.getIcon()))
                                        .put("option_style", StringUtils.trimToEmpty(option.getStyle()))
                                        .put("properties", StringUtils.trimToEmpty(option.getProperties()))
                                        .put("disabled", DynamicFieldOptionEntity.OptionDisabled.TRUE.getCode()
                                                                .equals(option.getDisabled()) ? 1 : 0)
                            ));
                        } else {
                            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                                    getFormTable(),
                                    new ObjectMap()
                                        .put("class_path", form.getClassPath())
                                        .put("=category", StringUtils.trimToEmpty(option.getCategory()))
                                        .put("=option_value", StringUtils.trimToEmpty(option.getValue()))
                                        .put("=option_group", StringUtils.trimToEmpty(option.getGroup()))
                                        .put("=option_display", StringUtils.trimToEmpty(option.getDisplay()))
                                        .put("=option_icon", StringUtils.trimToEmpty(option.getIcon()))
                                        .put("=option_style", StringUtils.trimToEmpty(option.getStyle()))
                                        .put("=properties", StringUtils.trimToEmpty(option.getProperties()))
                                        .put("=disabled", DynamicFieldOptionEntity.OptionDisabled.TRUE.getCode()
                                                                .equals(option.getDisabled()) ? 1 : 0)
                            ));
                        }
                    }
                }
            });
            return null;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, DynamicFieldOptionFormSimple originForm, String sourceState) {
            
        }
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
        return "system_field_option";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "system_field_option";
    }
    
    @Override
    public String getFormDisplay() {
        return "流程动态选项";
    }
}