package org.socyno.webfwk.module.release.build;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;

import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail;
import org.socyno.webfwk.module.app.form.ApplicationService;
import org.socyno.webfwk.module.app.form.FieldApplication;
import org.socyno.webfwk.module.app.form.FieldApplication.OptionApplication;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeIdParser;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
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

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;

public class SysBuildService extends AbstractStateFormServiceWithBaseDao<SysBuildFormDetail> {
    
    public static final SysBuildService DEFAULT = new SysBuildService();
    
    @Override
    public String getFormName() {
        return "system_build_service";
    }
    
    @Override
    protected String getFormTable() {
        return "system_build_service";
    }
    
    @Override
    public String getFormIdField() {
        return super.getFormIdField();
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
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

        public static String[] stringify(STATES... states) {
            if (states == null || states.length <= 0) {
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
         * 创建构建服务
         */
        Create(new AbstractStateSubmitAction<SysBuildFormDetail, SysBuildFormForCreation>("添加",
                STATES.ENABLED.getCode()) {

            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SysBuildFormDetail form, String sourceState) {

            }
            
            @Override
            public Long handle(String event, SysBuildFormDetail originForm, final SysBuildFormForCreation form,
                    final String message) throws Exception {
                String regex = "^[A-Za-z-0-9]+$";// 构建服务的名称。要求:英文、数字或短横行
                if (!form.getCode().matches(regex)) {
                    throw new MessageException("构建服务名称不合法，必须是英文、数字或-!");
                }
                final AtomicLong id = new AtomicLong(0);
                DEFAULT.getFormBaseDao().executeUpdate(
                        SqlQueryUtil.prepareInsertQuery(
                                DEFAULT.getFormTable(),new ObjectMap()
                                .put("code", form.getCode())
                                .put("title", form.getTitle())
                                .put("description", form.getDescription())), new ResultSetProcessor() {
                            @Override
                            public void process(ResultSet result, Connection conn) throws Exception {
                                result.next();
                                id.set(result.getLong(1));
                            }
                        });
                return id.get();
            }
        }),
        
        /**
         * 修改构建服务
         */
        Edit(new AbstractStateAction<SysBuildFormDetail, SysBuildFormForEdit, Void>("编辑", STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SysBuildFormDetail form, String sourceState) {

            }
            
            @Override
            public Void handle(String event, SysBuildFormDetail originForm, final SysBuildFormForEdit form,
                    final String message) throws Exception {
                DEFAULT.getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet resultSet, Connection connection) throws Exception {
                        DEFAULT.getFormBaseDao().executeUpdate(
                                SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(),
                                        new ObjectMap().put("=id", form.getId())
                                                       .put("title", form.getTitle())
                                                       .put("description", form.getDescription())));
                    }

                });
                return null;
            }
        }),
        
        /**
         * 禁用服务
         */
        Disable(new AbstractStateAction<SysBuildFormDetail, BasicStateForm, Void>("禁用",
                STATES.stringifyEx(STATES.DISABLED), STATES.DISABLED.getCode()) {
            @Override
            public Boolean messageRequired() {
                return true;
            }
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SysBuildFormDetail form, String sourceState) {

            }
        }),
        
        /**
         * 恢复服务
         */
        Enable(new AbstractStateAction<SysBuildFormDetail, BasicStateForm, Void>("启用", STATES.DISABLED.getCode(),
                STATES.ENABLED.getCode()) {
            @Override
            public Boolean messageRequired() {
                return true;
            }
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SysBuildFormDetail form, String sourceState) {
            }
        }),
        
        /**
         * 构建应用
         */
        Build(new AbstractStateAction<SysBuildFormDetail, SysBuildFormForBuild, Void>("构建", STATES.ENABLED.getCode(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SysBuildFormDetail form, String sourceState) {
                
            }

            
            @Override
            public Void handle(String event, SysBuildFormDetail originForm, final SysBuildFormForBuild form,
                    final String message) throws Exception {
            
                return null;
            }
        })
        ;
        
        private final AbstractStateAction<SysBuildFormDetail, ?, ?> action;

        EVENTS(AbstractStateAction<SysBuildFormDetail, ?, ?> action) {
            this.action = action;
        }
        
        public AbstractStateAction<SysBuildFormDetail, ?, ?> getAction() {
            return action;
        }
    }

    public static class SysBuildSubsystemParser implements AuthorityScopeIdParser {
        @Override
        public Long getAuthorityScopeId(Object originForm) {
            ApplicationFormDetail manageForm = (ApplicationFormDetail) originForm;
            if (manageForm == null || manageForm.getId() == null) {
                return null;
            }
            return manageForm.getSubsystemId();
        }
    }
    
    @Override
    protected Map<String, AbstractStateAction<SysBuildFormDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<SysBuildFormDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SysBuildListDefaultForm>("default", SysBuildListDefaultForm.class,
                SysBuildListDefaultQuery.class));
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
    
    /**
     * 构建服务下拉选项
     * 
     */
    public static List<OptionSysBuildService> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        SysBuildListDefaultQuery query = new SysBuildListDefaultQuery(1, 100).setKeyword(filter.getKeyword());
        if (ApplicationService.DEFAULT.getFormName().equals((filter.getFormName())) && filter.getFormId() != null) {
            OptionApplication application;
            if ((application = ClassUtil.getSingltonInstance(FieldApplication.class)
                    .queryDynamicValue(filter.getFormId())) != null) {
                query.setType(application.getType());
            }
        }
        return SysBuildService.DEFAULT.listFormX(OptionSysBuildService.class, query).getList();
    }
}
