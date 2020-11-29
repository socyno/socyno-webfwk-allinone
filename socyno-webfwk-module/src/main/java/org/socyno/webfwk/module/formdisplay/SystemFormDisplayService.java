package org.socyno.webfwk.module.formdisplay;


import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.util.*;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

public class SystemFormDisplayService extends AbstractStateFormServiceWithBaseDaoV2<SystemFormDisplaySimple> {

    public SystemFormDisplayService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }

    public static final String FORM_DISPLAY = "表单显示配置";

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

        private final Class<? extends AbstractStateAction<SystemFormDisplaySimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SystemFormDisplaySimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }

    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemFormDisplayDefaultForm>("default",
                SystemFormDisplayDefaultForm.class, SystemFormDisplayListDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    public static final SystemFormDisplayService DEFAULT = new SystemFormDisplayService();

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemFormDisplaySimple> forms) throws Exception {
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

    public static class UserChecker implements AuthoritySpecialChecker {

        @Override
        public boolean check(Object form) {
            return form != null
                    && SessionContext.getUserId().equals(((SystemFormDisplaySimple) form).getCreatedBy());
        }

    }

    public class EventCreate extends AbstractStateSubmitAction<SystemFormDisplaySimple, SystemFormDisplayCreation> {

        public EventCreate() {
            super("申请", STATES.ENABLED.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemFormDisplaySimple form, String message) {

        }

        @Override
        public Long handle(String event, SystemFormDisplaySimple originForm, SystemFormDisplayCreation form, String message) throws Exception {

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

    public class EventEdit extends AbstractStateAction<SystemFormDisplaySimple, SystemFormDisplayForUpdate, Void> {

        public EventEdit() {
            super("编辑", getStateCodes(STATES.ENABLED, STATES.DISABLED), "");
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, SystemFormDisplaySimple originForm, String message) {

        }

        @Override
        public Void handle(String event, SystemFormDisplaySimple originForm, SystemFormDisplayForUpdate form, String message) throws Exception {
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

    public class EventDelete extends AbstractStateDeleteAction<SystemFormDisplaySimple> {

        public EventDelete() {
            super("删除", getStateCodes(STATES.ENABLED));
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, SystemFormDisplaySimple originForm, String sourceState) {

        }

        @Override
        public Void handle(String event, SystemFormDisplaySimple originForm, BasicStateForm form, String sourceState) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
            ));
            return null;
        }
    }


    @Override
    public SystemFormDisplayDetail getForm(long formId) throws Exception {
        return getForm(SystemFormDisplayDetail.class, formId);
    }

}
