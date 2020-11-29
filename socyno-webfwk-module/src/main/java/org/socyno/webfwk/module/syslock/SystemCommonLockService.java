package org.socyno.webfwk.module.syslock;

import lombok.Getter;

import java.util.Collection;

import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialRejecter;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDaoV2;
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

public class SystemCommonLockService extends AbstractStateFormServiceWithBaseDaoV2<SystemCommonLockSimple> {
    
    public SystemCommonLockService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    public static final String FORM_DISPLAY = "系统公共锁";
    
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

        private final Class<? extends AbstractStateAction<SystemCommonLockSimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SystemCommonLockSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemCommonLockDefaultForm>("默认查询",
                SystemCommonLockDefaultForm.class, SystemCommonLockListDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public static final SystemCommonLockService DEFAULT = new SystemCommonLockService();
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemCommonLockSimple> forms) throws Exception {
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
    
    public static class LockChecker implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object form) {
            return form != null
                    && StringUtils.isBlank(((SystemCommonLockSimple) form).getLocked());
        }
        
    }
    
    public class EventUnlock extends AbstractStateAction<SystemCommonLockSimple, BasicStateForm, Void> {
        
        public EventUnlock() {
            super("解锁", getStateCodes(STATES.CREATED,STATES.STARTED), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, rejecter = LockChecker.class)
        public void check(String event, SystemCommonLockSimple originForm, String sourceState) {

        }
        
        @Override
        public Void handle(String event, SystemCommonLockSimple originForm, BasicStateForm form, String sourceState) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
                            .put("locked", null)
            ));
            return null;
        }
    }
    
    @Override
    public SystemCommonLockDetail getForm(long formId) throws Exception {
        return getForm(SystemCommonLockDetail.class, formId);
    }
    
}
