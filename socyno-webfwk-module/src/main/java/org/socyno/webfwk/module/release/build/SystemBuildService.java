package org.socyno.webfwk.module.release.build;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;

import org.socyno.webfwk.module.application.ApplicationService;
import org.socyno.webfwk.module.application.FieldApplication;
import org.socyno.webfwk.module.application.FieldApplication.OptionApplication;
import org.socyno.webfwk.state.abs.*;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;

public class SystemBuildService extends
        AbstractStateFormServiceWithBaseDao<SystemBuildFormDetail, SystemBuildFormDetail, SystemBuildFormSimple> {
    
    @Getter
    private static final SystemBuildService Instance = new SystemBuildService();
    
    private SystemBuildService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    public String getFormName() {
        return "system_build_service";
    }
    
    @Override
    protected String getFormTable() {
        return "system_build_service";
    }
    
    @Override
    public String getFormDisplay() {
        return "应用构建服务";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        DISABLED("disabled", "禁用"), 
        ENABLED("enabled", "有效");

        private final String code;
        private final String name;

        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemBuildFormDetail, SystemBuildFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemBuildFormDetail form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemBuildFormDetail originForm, final SystemBuildFormCreation form,
                final String message) throws Exception {
            String regex = "^[A-Za-z-0-9]+$";// 构建服务的名称。要求:英文、数字或短横行
            if (!form.getCode().matches(regex)) {
                throw new MessageException("构建服务名称不合法，必须是英文、数字或-!");
            }
            final AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareInsertQuery(
                            getFormTable(),new ObjectMap()
                            .put("code", form.getCode())
                            .put("title", form.getTitle())
                            .put("description", form.getDescription())), new ResultSetProcessor() {
                        @Override
                        public void process(ResultSet result, Connection conn) throws Exception {
                            result.next();
                            id.set(result.getLong(1));
                        }
                    });
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemBuildFormDetail, SystemBuildFormEdition, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemBuildFormDetail form, String sourceState) {

        }
        
        @Override
        public Void handle(String event, SystemBuildFormDetail originForm, final SystemBuildFormEdition form,
                final String message) throws Exception {
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet resultSet, Connection connection) throws Exception {
                    getFormBaseDao().executeUpdate(
                            SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                                    new ObjectMap().put("=id", form.getId())
                                                   .put("title", form.getTitle())
                                                   .put("description", form.getDescription())));
                }

            });
            return null;
        }
    }
    
    public class EventDisable extends AbstractStateAction<SystemBuildFormDetail, StateFormBasicInput, Void> {
        
        public EventDisable() {
            super("禁用", getStateCodesEx(STATES.DISABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemBuildFormDetail form, String sourceState) {
            
        }
    }
    
    public class EventEnable extends AbstractStateAction<SystemBuildFormDetail, StateFormBasicInput, Void> {
        
        public EventEnable() {
            super("启用", STATES.ENABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemBuildFormDetail form, String sourceState) {
            
        }
    }
    
    public class EventBuild extends AbstractStateAction<SystemBuildFormDetail, SystemBuildFormBuild, Void> {
        
        public EventBuild() {
            super("构建", STATES.ENABLED.getCode(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemBuildFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemBuildFormDetail originForm, final SystemBuildFormBuild form,
                final String message) throws Exception {
            return null;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        /**
         * 创建构建服务
         */
        Create(EventCreate.class),
        
        /**
         * 修改构建服务
         */
        Edit(EventEdit.class),
        
        /**
         * 禁用服务
         */
        Disable(EventDisable.class),
        
        /**
         * 恢复服务
         */
        Enable(EventEnable.class),
        
        /**
         * 构建应用
         */
        Build(EventBuild.class)
        ;
        
        private final Class<? extends AbstractStateAction<SystemBuildFormDetail, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemBuildFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemBuildFormSimple>(
                "默认查询",
                SystemBuildFormSimple.class,
                SystemBuildQueryDefault.class
            ));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    /**
     * 构建服务下拉选项
     * 
     */
    public List<SystemBuildFormOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        SystemBuildQueryDefault query = new SystemBuildQueryDefault(1, 100).setKeyword(filter.getKeyword());
        if (ApplicationService.getInstance().getFormName().equals((filter.getFormName())) && filter.getFormId() != null) {
            OptionApplication application;
            if ((application = ClassUtil.getSingltonInstance(FieldApplication.class)
                    .queryDynamicValue(filter.getFormId())) != null) {
                query.setType(application.getType());
            }
        }
        return listForm(SystemBuildFormOption.class, query).getList();
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemBuildFormSimple> forms) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
