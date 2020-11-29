package org.socyno.webfwk.module.release.mobstore;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDaoV2;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;

public class ReleaseMobileStoreService extends AbstractStateFormServiceWithBaseDaoV2<ReleaseMobileStoreSimple> {

    public ReleaseMobileStoreService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }

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
         * 启用
         */
        Enabled(EventEnabled.class),
        /**
         * 禁用
         */
        Disabled(EventDisabled.class);

        private final Class<? extends AbstractStateAction<ReleaseMobileStoreSimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<ReleaseMobileStoreSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }

    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<ReleaseMobileStoreListForm>("默认查询",
                ReleaseMobileStoreListForm.class, ReleaseMobileStoreDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    public static final ReleaseMobileStoreService DEFAULT = new ReleaseMobileStoreService();

    @Override
    protected void fillExtraFormFields(Collection<? extends ReleaseMobileStoreSimple> forms) throws Exception {
        
    }

    @Override
    protected String getFormTable() {
        return "release_app_store";
    }

    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }

    @Override
    public String getFormName() {
        return "release_app_store_configs";
    }

    public static class UserChecker implements AuthoritySpecialChecker {

        @Override
        public boolean check(Object form) {
            return form != null
                    && SessionContext.getUserId().equals(((ReleaseMobileStoreSimple) form).getCreatedBy());
        }

    }

    public class EventCreate extends AbstractStateSubmitAction<ReleaseMobileStoreSimple, ReleaseMobileStoreCreation> {

        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, ReleaseMobileStoreSimple form, String message) {

        }

        @Override
        public Long handle(String event, ReleaseMobileStoreSimple originForm, ReleaseMobileStoreCreation form, String message) throws Exception {

            AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                            .put("created_by", SessionContext.getUserId())
                            .put("created_at", new Date())
                            .put("created_code_by", SessionContext.getUsername())
                            .put("created_name_by", SessionContext.getDisplay())
                            .put("store_name", form.getStoreName())
                            .put("channel_name", form.getChannelName())
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

    public class EventEdit extends AbstractStateAction<ReleaseMobileStoreSimple, ReleaseMobileStoreForUpdate, Void> {

        public EventEdit() {
            super("编辑", getStateCodes(STATES.ENABLED, STATES.DISABLED), "");
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileStoreSimple originForm, String message) {

        }

        @Override
        public Void handle(String event, ReleaseMobileStoreSimple originForm, ReleaseMobileStoreForUpdate form, String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("store_name", form.getStoreName())
                            .put("channel_name", form.getChannelName())
            ));
            return null;
        }

    }

    public class EventEnabled extends AbstractStateAction<ReleaseMobileStoreSimple, BasicStateForm, Void> {

        public EventEnabled() {
            super("启用", getStateCodes(STATES.DISABLED), STATES.ENABLED.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileStoreSimple originForm, String sourceState) {

        }

        @Override
        public Void handle(String event, ReleaseMobileStoreSimple originForm, BasicStateForm form, String sourceState) throws Exception {
            return null;
        }
    }

    public class EventDisabled extends AbstractStateAction<ReleaseMobileStoreSimple, BasicStateForm, Void> {

        public EventDisabled() {
            super("禁用", getStateCodes(STATES.ENABLED), STATES.DISABLED.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = UserChecker.class)
        public void check(String event, ReleaseMobileStoreSimple originForm, String sourceState) {

        }

        @Override
        public Void handle(String event, ReleaseMobileStoreSimple originForm, BasicStateForm form, String sourceState) throws Exception {
            return null;
        }
    }


    @Override
    public ReleaseMobileStoreDetail getForm(long formId) throws Exception {
        return getForm(ReleaseMobileStoreDetail.class, formId);
    }

}
