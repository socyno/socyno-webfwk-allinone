package org.socyno.webfwk.module.sysconfig;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateDeleteAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDaoV2;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

public class SystemConfigsService extends AbstractStateFormServiceWithBaseDaoV2<SystemConfigsSimple> {

    public SystemConfigsService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }

    public static final String FORM_DISPLAY = "系统参数配置";

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

        private final Class<? extends AbstractStateAction<SystemConfigsSimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SystemConfigsSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }

    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemConfigsDefaultForm>("默认查询",
                SystemConfigsDefaultForm.class, SystemConfigsListDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    public static final SystemConfigsService DEFAULT = new SystemConfigsService();

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemConfigsSimple> forms) throws Exception {
        DefaultStateFormSugger.getInstance().apply(forms);
    }

    @Override
    protected String getFormTable() {
        return "system_configs";
    }

    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }

    @Override
    public String getFormName() {
        return "system_configs";
    }

    public static class UserChecker implements AuthoritySpecialChecker {

        @Override
        public boolean check(Object form) {
            return form != null
                    && SessionContext.getUserId().equals(((SystemConfigsSimple) form).getCreatedBy());
        }

    }

    public class EventCreate extends AbstractStateSubmitAction<SystemConfigsSimple, SystemConfigsCreation> {

        public EventCreate() {
            super("申请", STATES.ENABLED.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemConfigsSimple form, String message) {

        }

        @Override
        public Long handle(String event, SystemConfigsSimple originForm, SystemConfigsCreation form, String message) throws Exception {

            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                            .put("created_by", SessionContext.getUserId())
                            .put("created_at", new Date())
                            .put("created_code_by", SessionContext.getUsername())
                            .put("created_name_by", SessionContext.getDisplay())
                            .put("name", form.getName())
                            .put("value", form.getValue())
                            .put("comment", form.getComment())
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

    public class EventEdit extends AbstractStateAction<SystemConfigsSimple, SystemConfigsForUpdate, Void> {

        public EventEdit() {
            super("编辑", getStateCodes(STATES.ENABLED, STATES.DISABLED), "");
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, SystemConfigsSimple originForm, String message) {

        }

        @Override
        public Void handle(String event, SystemConfigsSimple originForm, SystemConfigsForUpdate form, String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("name", form.getName())
                            .put("value", form.getValue())
                            .put("comment", form.getComment())
            ));
            return null;
        }

    }

    public class EventDelete extends AbstractStateDeleteAction<SystemConfigsSimple> {

        public EventDelete() {
            super("删除", getStateCodes(STATES.ENABLED));
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, SystemConfigsSimple originForm, String sourceState) {

        }

        @Override
        public Void handle(String event, SystemConfigsSimple originForm, BasicStateForm form, String sourceState) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
            ));
            return null;
        }
    }


    @Override
    public SystemConfigsDetail getForm(long formId) throws Exception {
        return getForm(SystemConfigsDetail.class, formId);
    }

}
