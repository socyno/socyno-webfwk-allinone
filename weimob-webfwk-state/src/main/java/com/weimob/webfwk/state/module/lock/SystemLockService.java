package com.weimob.webfwk.state.module.lock;

import lombok.Getter;

import java.util.Collection;

import com.weimob.webfwk.state.abs.AbstractStateAction;
import com.weimob.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityScopeType;
import com.weimob.webfwk.state.authority.AuthoritySpecialRejecter;
import com.weimob.webfwk.state.service.SimpleLockService;
import com.weimob.webfwk.state.sugger.DefaultStateFormSugger;
import com.weimob.webfwk.state.util.StateFormBasicInput;
import com.weimob.webfwk.state.util.StateFormEventClassEnum;
import com.weimob.webfwk.state.util.StateFormNamedQuery;
import com.weimob.webfwk.state.util.StateFormQueryBaseEnum;
import com.weimob.webfwk.state.util.StateFormStateBaseEnum;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.sql.SqlQueryUtil;

public class SystemLockService extends
        AbstractStateFormServiceWithBaseDao<SystemLockFormDetail, SystemLockFormDefault, SystemLockFormSimple> {
    
    @Getter
    private static final SystemLockService Instance = new SystemLockService();
    
    private SystemLockService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        CREATED("created",    "等待中"),
        STARTED("started",    "运行中"),
        RELEASED("released",  "已结束");
        
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
        DEFAULT(new StateFormNamedQuery<SystemLockFormDefault>(
                "默认查询", 
                SystemLockFormDefault.class,
                SystemLockQueryDefault.class));
        
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
        return SimpleLockService.getInstance().getFormTableName();
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SimpleLockService.getInstance().getDao();
    }
    
    @Override
    public String getFormName() {
        return getFormTable();
    }
    
    @Override
    public String getFormDisplay() {
        return "分布式锁";
    }
    
    @Override
    public String getFormStateField() {
        return "state";
    }
    
    @Override
    public String getFormCreatedAtField() {
        return "created_at";
    }
    
    @Override
    public String getFormCreatedByField() {
        return "created_by";
    }
    
    @Override
    public String getFormCreatedCodeByField() {
        return "created_code_by";
    }
    
    @Override
    public String getFormCreatedNameByField() {
        return "created_name_by";
    }
    
    @Override
    public String getFormUpdatedAtField() {
        return "unlocked_at";
    }
    
    @Override
    public String getFormUpdatedByField() {
        return "unlocked_by";
    }
    
    @Override
    public String getFormUpdatedCodeByField() {
        return "unlocked_code_by";
    }
    
    @Override
    public String getFormUpdatedNameByField() {
        return "unlocked_name_by";
    }
    
    public class LockChecker implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object form) {
            return form != null && ((SystemLockFormSimple) form).getLocked() == null;
        }
        
    }
    
    public class EventUnlock extends AbstractStateAction<SystemLockFormSimple, StateFormBasicInput, Void> {
        
        public EventUnlock() {
            super("解锁", getStateCodesEx(), STATES.RELEASED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, rejecter = LockChecker.class)
        public void check(String event, SystemLockFormSimple originForm, String sourceState) {
            
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() throws Exception {
            return true;
        }
        
        @Override
        public Void handle(String event, SystemLockFormSimple originForm, StateFormBasicInput form, String sourceState) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
                            .put("locked", null)
            ));
            return null;
        }
    }
}
