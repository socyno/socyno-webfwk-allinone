package com.weimob.webfwk.state.module.display;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.weimob.webfwk.state.abs.*;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityScopeType;
import com.weimob.webfwk.state.authority.AuthoritySpecialChecker;
import com.weimob.webfwk.state.util.*;
import com.weimob.webfwk.util.context.ContextUtil;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.sql.SqlQueryUtil;

public class StateDisplayService extends
        AbstractStateFormServiceWithBaseDao<StateDisplayFormDetail, StateDisplayFormDefault, StateDisplayFormSimple> {
    
    private StateDisplayService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }

    @Getter
    private static final StateDisplayService Instance = new StateDisplayService();

    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        ENABLED("enabled", "启用"),
        DISABLED("disabled", "禁用");

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
         * 创建申请单
         */
        Create(EventCreate.class),
        /**
         * 修改申清单
         */
        Update(EventEdit.class),
        /**
         * 禁用
         */
        Delete(EventDelete.class);

        private final Class<? extends AbstractStateAction<StateDisplayFormSimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<StateDisplayFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }

    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<StateDisplayFormDefault>(
                "默认查询",
                StateDisplayFormDefault.class, 
                StateDisplayQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends StateDisplayFormSimple> forms) throws Exception {
        
    }

    @Override
    protected String getFormTable() {
        return StateFormDisplayScheduled.getFormDisplayTable();
    }

    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }

    @Override
    public String getFormName() {
        return getFormTable();
    }
    
    @Override
    public String getFormDisplay() {
        return "流程显示配置";
    }

    public static class UserChecker implements AuthoritySpecialChecker {

        @Override
        public boolean check(Object form) {
            return form != null
                    && SessionContext.getUserId().equals(((StateDisplayFormSimple) form).getCreatedBy());
        }

    }

    public class EventCreate extends AbstractStateCreateAction<StateDisplayFormSimple, StateDisplayFormCreation> {

        public EventCreate() {
            super("申请", STATES.ENABLED.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, StateDisplayFormSimple form, String message) {

        }

        @Override
        public StateFormEventResultCreateViewBasic handle(String event, StateDisplayFormSimple originForm, StateDisplayFormCreation form, String message) throws Exception {

            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                            .put("name", form.getName())
                            .put("display", form.getDisplay())
                            .put("remark", form.getRemark())
            ), new AbstractDao.ResultSetProcessor() {

                @Override
                public void process(ResultSet resultSet, Connection connection) throws Exception {
                    resultSet.next();
                    id.set(resultSet.getLong(1));
                }
            });
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }

    public class EventEdit extends AbstractStateAction<StateDisplayFormSimple, StateDisplayFormUpdate, Void> {

        public EventEdit() {
            super("编辑", getStateCodes(STATES.ENABLED, STATES.DISABLED), "");
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, StateDisplayFormSimple originForm, String message) {

        }

        @Override
        public Void handle(String event, StateDisplayFormSimple originForm, StateDisplayFormUpdate form, String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("name", form.getName())
                            .put("display", form.getDisplay())
                            .put("remark", form.getRemark())
            ));
            return null;
        }

    }

    public class EventDelete extends AbstractStateDeleteAction<StateDisplayFormSimple> {

        public EventDelete() {
            super("删除", getStateCodes(STATES.ENABLED));
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, StateDisplayFormSimple originForm, String sourceState) {

        }

        @Override
        public Void handle(String event, StateDisplayFormSimple originForm, StateFormBasicInput form, String sourceState) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
            ));
            return null;
        }
    }

}
