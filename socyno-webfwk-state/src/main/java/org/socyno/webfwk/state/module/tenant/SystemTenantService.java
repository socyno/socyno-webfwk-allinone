package org.socyno.webfwk.state.module.tenant;

import lombok.Getter;
import lombok.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.abs.*;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.field.OptionSystemFeature;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.exception.NamingFormatInvalidException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class SystemTenantService extends
        AbstractStateFormServiceWithBaseDao<SystemTenantFormDetail, SystemTenantFormDefault, SystemTenantFormSimple> {
    
    @Getter
    private static final SystemTenantService Instance = new SystemTenantService();
    
    private SystemTenantService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        DISABLED ("disabled", "禁用"),
        ENABLED  ("enabled",  "有效")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    
    public class EventCreate extends AbstractStateCreateAction<SystemTenantFormDetail, SystemTenantFormCreation> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantFormDetail form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemTenantFormDetail originForm, SystemTenantFormCreation form, String message) throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            checkSystemTenantChange(form);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                        .put("code",            form.getCode())
                        .put("name",            form.getName())
                        .put("code_namespace",  form.getCodeNamespace())
                        .put("code_lib_group",  form.getCodeLibGroup())
            ), new ResultSetProcessor () {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    r.next();
                    id.set(r.getLong(1));
                }
            });
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventUpdate extends AbstractStateAction<SystemTenantFormDetail, SystemTenantFormEdition, Void> {

        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTenantFormDetail originForm, final SystemTenantFormEdition form, final String message) throws Exception {
            checkSystemTenantChange(form);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                        .put("=id",    form.getId())
                        .put("name",        form.getName())
            ));
            SystemTenantBasicService.setTenantFeatures(form.getId(), form.getFeatures());
            SystemTenantBasicService.setTenantDatabases(form.getId(), form.getDatabases());
//            if (form.getNamespaces() != null) {
//                ClassUtil.getSingltonInstance(FieldDeployNamespace.class).setTenantDeployNamespace(form.getId(),
//                        form.getNamespaces());
//            }
            return null;
        }
    }
    
    public class EventDisable extends AbstractStateAction<SystemTenantFormDetail, StateFormBasicInput, Void> {
        
        public EventDisable() {
            super("禁用", getStateCodesEx(STATES.DISABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantFormDetail form, String sourceState) {
            
        }
    }
    
    public class EventEnable extends AbstractStateAction<SystemTenantFormDetail, StateFormBasicInput, Void> {
        
        public EventEnable() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantFormDetail form, String sourceState) {
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        /**
         * 创建
         */
        Create(EventCreate.class),
        
        /**
         * 更新用户信息
         */
        Update(EventUpdate.class),
        
        /**
         * 禁用
         */
        Disable(EventDisable.class),
        
        /**
         * 恢复
         */
        Enable(EventEnable.class)
        
        ;
        
        private final Class<? extends AbstractStateAction<SystemTenantFormDetail, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SystemTenantFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemTenantFormDefault>("默认查询", 
                SystemTenantFormDefault.class, SystemTenantQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    
    @Override
    public String getFormName() {
        return "system_tenant";
    }
    
    @Override
    public String getFormTable() {
        return "system_tenant";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统租户";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantBasicService.getDao();
    }
    
    private void checkSystemTenantChange(AbstractSystemTenantInput changed) throws Exception {
        if (StringUtils.isBlank(changed.getCode()) || changed.getCode().matches("^\\d+$")
                            || !changed.getCode().matches("^[a-z0-9][a-z0-9\\-\\.]+[a-z0-9]$")) {
            throw new NamingFormatInvalidException("租户代码命名不规范 ：只能包含数字、小写字母或短横线(-), 且不能为纯数字，不能以短横线开头或结尾");
        }
        StringBuffer sql = new StringBuffer(
                String.format("SELECT COUNT(1) FROM %s WHERE code = ?", getFormTable()));
        if (changed.getId() != null) {
            sql.append("AND id != ").append(changed.getId());
        }
        if (getFormBaseDao().queryAsObject(Long.class, sql.toString(),
                new Object[] { changed.getCode() }) > 0) {
            throw new MessageException(String.format("租户代码(%s)已被占用，请重新命名！", changed.getCode()));
        }
    }
    
    /**
     * 检索租户详情。
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractSystemTenant> T get(@NonNull Class<?> clazz, Object idOrCode) throws Exception {
        if (idOrCode == null || StringUtils.isBlank(idOrCode.toString())) {
            return null;
        }
        List<T> result;
        if ((result = (List<T>)queryFormWithStateRevision(clazz,
                        String.format("SELECT * FROM %s WHERE %s=?", getFormTable(),
                                idOrCode.toString().matches("^\\d+$") ? "id" : "code"),
                        new Object[] { idOrCode })) == null) {
            return null;
        }
        if (SystemTenantFormDetail.class.isAssignableFrom(clazz)) {
            return (T)withDetails((SystemTenantFormDetail)result.get(0));
        }
        return result.get(0);
    }
    
    public AbstractSystemTenant getSimple(Object idOrCode) throws Exception {
        return get(SystemTenantFormDefault.class, idOrCode);
    }
    
    public SystemTenantFormDetail getDetails(Object idOrCode) throws Exception {
        return get(SystemTenantFormDetail.class, idOrCode);
    }
    
    private SystemTenantFormDetail withDetails(SystemTenantFormDetail form) throws Exception {
        if (form == null) {
            return null;
        } 
        form.setFeatures(SystemTenantBasicService.getTenantFeatures(OptionSystemFeature.class, form.getCode()));
        form.setDatabases(SystemTenantBasicService.getTenantDatabases(SystemTenantDbInfo.class, form.getCode()));
        return form;
    }
    
    /**
     * 检索租户详情
     */
    public SystemTenantFormDetail getForm(long id) throws Exception {
        return getDetails(id);
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemTenantFormSimple> forms) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
