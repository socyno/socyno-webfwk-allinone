package org.socyno.webfwk.module.formdisplay;


import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.util.*;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

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
        DEFAULT(new StateFormNamedQuery<StateDisplayFormDefault>("默认查询",
                StateDisplayFormDefault.class, StateDisplayQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends StateDisplayFormSimple> forms) throws Exception {
//        DefaultStateFormSugger.getInstance().apply(forms);
    }

    @Override
    protected String getFormTable() {
        return "system_form_display";
    }

    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }

    @Override
    public String getFormName() {
        return "system_form_display";
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

    public class EventCreate extends AbstractStateSubmitAction<StateDisplayFormSimple, StateDisplayFormCreation> {

        public EventCreate() {
            super("申请", STATES.ENABLED.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, StateDisplayFormSimple form, String message) {

        }

        @Override
        public Long handle(String event, StateDisplayFormSimple originForm, StateDisplayFormCreation form, String message) throws Exception {

            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                            .put("created_by", SessionContext.getUserId())
                            .put("created_at", new Date())
                            .put("created_code_by", SessionContext.getUsername())
                            .put("created_name_by", SessionContext.getDisplay())
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
            return id.get();
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
        public Void handle(String event, StateDisplayFormSimple originForm, BasicStateForm form, String sourceState) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
            ));
            return null;
        }
    }


    @Override
    public StateDisplayFormDetail getForm(long formId) throws Exception {
        return getForm(StateDisplayFormDetail.class, formId);
    }

}
