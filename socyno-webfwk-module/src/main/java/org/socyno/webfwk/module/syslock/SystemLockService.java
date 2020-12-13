package org.socyno.webfwk.module.syslock;

import lombok.Getter;

import java.util.Collection;

import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialRejecter;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class SystemLockService extends
        AbstractStateFormServiceWithBaseDao<SystemLockFormDetail, SystemLockFormDefault, SystemLockFormSimple> {
    
    @Getter
    private static final SystemLockService Instance = new SystemLockService();
    
    public SystemLockService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        CREATED("created", "等待中"),
        STARTED("started", "运行中"),
        RELEASED("released", "已结束");
        
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
         * 解锁
         */
        Unlock(EventUnlock.class);

        private final Class<? extends AbstractStateAction<SystemLockFormSimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SystemLockFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemLockFormDefault>("默认查询",
                SystemLockFormDefault.class, SystemLockQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemLockFormSimple> forms) throws Exception {
          DefaultStateFormSugger.getInstance().apply(forms);
    }
    
    @Override
    protected String getFormTable() {
        return "system_common_lock";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "system_common_lock";
    }
    
    @Override
    public String getFormDisplay() {
        return "分布式锁";
    }
    
    public class LockChecker implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object form) {
            return form != null
                    && StringUtils.isBlank(((SystemLockFormSimple) form).getLocked());
        }
        
    }
    
    public class EventUnlock extends AbstractStateAction<SystemLockFormSimple, BasicStateForm, Void> {
        
        public EventUnlock() {
            super("解锁", getStateCodes(STATES.CREATED,STATES.STARTED), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, rejecter = LockChecker.class)
        public void check(String event, SystemLockFormSimple originForm, String sourceState) {

        }
        
        @Override
        public Void handle(String event, SystemLockFormSimple originForm, BasicStateForm form, String sourceState) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
                            .put("locked", null)
            ));
            return null;
        }
    }
}
