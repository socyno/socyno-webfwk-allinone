package org.socyno.webfwk.module.systenant;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import lombok.Getter;
import lombok.NonNull;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.module.deploy.cluster.FieldDeployNamespace;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.field.OptionSystemFeature;
import org.socyno.webfwk.state.module.tenant.SystemTenantBasicService;
import org.socyno.webfwk.state.module.tenant.SystemTenantDbInfo;
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.exception.NamingFormatInvalidException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class SystemTenantService extends AbstractStateFormServiceWithBaseDao<SystemTenantDetail> {
    
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
        
        public static String[] stringify(STATES... states) {
            if (states == null || states.length <=0 ) {
                return new String[0];
            }
            String[] result = new String[states.length];
            for (int i = 0; i < states.length; i++) {
                result[i] = states[i].getCode();
            }
            return result;
        }
        
        public static String[] stringifyEx(STATES... states) {
            if (states == null) {
                states = new STATES[0];
            }
            List<String> result = new ArrayList<>(states.length);
            for (STATES s : STATES.values()) {
                if (!ArrayUtils.contains(states, s)) {
                    result.add(s.getCode());
                }
            }
            return result.toArray(new String[0]);
        }

        public static List<? extends FieldOption> getStatesAsOption() {
            List<FieldOption> options = new ArrayList<>();
            for (STATES s : STATES.values()) {
                options.add(new FieldSimpleOption(s.getCode(), s.getName()));
            }
            return options;
        }
    }
    
    public static enum EVENTS implements StateFormEventBaseEnum {
        /**
         * 创建
         */
        Submit(new AbstractStateSubmitAction<SystemTenantDetail, SystemTenantForCreation>("创建", STATES.ENABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemTenantDetail form, String sourceState) {
                
            }
            
            @Override
            public Long handle(String event, SystemTenantDetail originForm, SystemTenantForCreation form, String message) throws Exception {
                final AtomicLong id = new AtomicLong(-1);
                checkSystemTenantChange(form);
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                        DEFAULT.getFormTable(), new ObjectMap()
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
                return id.get();
            }
        }),
        
        /**
         * 更新用户信息
         */
        Update(new AbstractStateAction<SystemTenantDetail, SystemTenantForEdition, Void>("编辑", STATES.stringifyEx(), "") {
            @Override
            public Boolean messageRequired() {
                return true;
            }
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemTenantDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, SystemTenantDetail originForm, final SystemTenantForEdition form, final String message) throws Exception {
                checkSystemTenantChange(form);
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                        DEFAULT.getFormTable(), new ObjectMap()
                            .put("=id",    form.getId())
                            .put("name",        form.getName())
                ));
                SystemTenantBasicService.setTenantFeatures(form.getId(), form.getFeatures());
                SystemTenantBasicService.setTenantDatabases(form.getId(), form.getDatabases());
                if (form.getNamespaces() != null) {
                    ClassUtil.getSingltonInstance(FieldDeployNamespace.class).setTenantDeployNamespace(form.getId(), form.getNamespaces());
                }
                return null;
            }
        }),
        
        /**
         * 禁用
         */
        Disable(new AbstractStateAction<SystemTenantDetail, BasicStateForm, Void>("禁用", STATES.stringifyEx(STATES.DISABLED), STATES.DISABLED.getCode()) {
           @Override
            public Boolean messageRequired() {
                return true;
            }
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemTenantDetail form, String sourceState) {
                
            }
        }),
        
        /**
         * 恢复
         */
        Enable(new AbstractStateAction<SystemTenantDetail, BasicStateForm, Void>("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode()) {
            @Override
            public Boolean messageRequired() {
                return true;
            }
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemTenantDetail form, String sourceState) {
            }
        })
        
        ;
        
        private final AbstractStateAction<SystemTenantDetail, ?, ?> action;
        EVENTS(AbstractStateAction<SystemTenantDetail, ?, ?> action) {
            this.action = action;
        }
        
        public AbstractStateAction<SystemTenantDetail, ?, ?> getAction() {
            return action;
        }
        
        public String getName() {
            return name().replaceAll("([^A-Z])([A-Z])", "$1_$2").toLowerCase();
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemTenantListDefaultFrom>("default", 
                SystemTenantListDefaultFrom.class, SystemTenantListDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
        public static List<StateFormNamedQuery<?>> getQueries() {
            List<StateFormNamedQuery<?>> queries = new ArrayList<>();
            for (QUERIES q : QUERIES.values()) {
                queries.add(q.getNamedQuery());
            }
            return queries;
        }
    }
    
    public static final SystemTenantService DEFAULT = new SystemTenantService();
    
    @Override
    public String getFormName() {
        return getName();
    }
    
    @Override
    public String getFormTable() {
        return getTable();
    }
    
    protected static String getTable() {
        return "system_tenant";
    }
    
    protected static String getName() {
        return "system_tenant";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return getDao();
    }
    
    @Override
    protected Map<String, AbstractStateAction<SystemTenantDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<SystemTenantDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    private static AbstractDao getDao() {
        return SystemTenantBasicService.getDao();
    }
    
    private static void checkSystemTenantChange(AbstractSystemTenant changed) throws Exception {
        if (StringUtils.isBlank(changed.getCode()) || changed.getCode().matches("^\\d+$")
                            || !changed.getCode().matches("^[a-z0-9][a-z0-9\\-\\.]+[a-z0-9]$")) {
            throw new NamingFormatInvalidException("租户代码命名不规范 ：只能包含数字、小写字母或短横线(-), 且不能为纯数字，不能以短横线开头或结尾");
        }
        StringBuffer sql = new StringBuffer(
                String.format("SELECT COUNT(1) FROM %s WHERE code = ?", DEFAULT.getFormTable()));
        if (changed.getId() != null) {
            sql.append("AND id != ").append(changed.getId());
        }
        if (DEFAULT.getFormBaseDao().queryAsObject(Long.class, sql.toString(),
                new Object[] { changed.getCode() }) > 0) {
            throw new MessageException(String.format("租户代码(%s)已被占用，请重新命名！", changed.getCode()));
        }
    }
    
    /**
     * 检索租户详情。
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractSystemTenant> T get(@NonNull Class<?> clazz, Object idOrCode) throws Exception {
        if (idOrCode == null || StringUtils.isBlank(idOrCode.toString())) {
            return null;
        }
        List<T> result;
        if ((result = (List<T>) DEFAULT
                .queryFormWithStateRevision(clazz,
                        String.format("SELECT * FROM %s WHERE %s=?", DEFAULT.getFormTable(),
                                idOrCode.toString().matches("^\\d+$") ? "id" : "code"),
                        new Object[] { idOrCode })) == null) {
            return null;
        }
        if (SystemTenantDetail.class.isAssignableFrom(clazz)) {
            return (T)withDetails((SystemTenantDetail)result.get(0));
        }
        return result.get(0);
    }
    
    public static AbstractSystemTenant getSimple(Object idOrCode) throws Exception {
        return get(SystemTenantListDefaultFrom.class, idOrCode);
    }
    
    public static SystemTenantDetail getDetails(Object idOrCode) throws Exception {
        return get(SystemTenantDetail.class, idOrCode);
    }
    
    private static SystemTenantDetail withDetails(SystemTenantDetail form) throws Exception {
        if (form == null) {
            return null;
        } 
        form.setFeatures(SystemTenantBasicService.getTenantFeatures(OptionSystemFeature.class, form.getCode()));
        form.setDatabases(SystemTenantBasicService.getTenantDatabases(SystemTenantDbInfo.class, form.getCode()));
        form.setNamespaces(ClassUtil.getSingltonInstance(FieldDeployNamespace.class).queryByTenantId(form.getId()));
        return form;
    }
    
    /**
     * 检索租户详情
     */
    public SystemTenantDetail getForm(long id) throws Exception {
        return getDetails(id);
    }
    
    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
    }
    
    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }
}
