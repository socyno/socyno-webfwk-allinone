package com.weimob.webfwk.state.module.chart;

import lombok.Getter;
import org.apache.http.NameValuePair;

import com.weimob.webfwk.state.abs.AbstractStateAction;
import com.weimob.webfwk.state.abs.AbstractStateCreateAction;
import com.weimob.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityScopeType;
import com.weimob.webfwk.state.authority.AuthoritySpecialChecker;
import com.weimob.webfwk.state.authority.AuthoritySpecialRejecter;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.state.util.StateFormEventClassEnum;
import com.weimob.webfwk.state.util.StateFormEventResultCreateViewBasic;
import com.weimob.webfwk.state.util.StateFormNamedQuery;
import com.weimob.webfwk.state.util.StateFormQueryBaseEnum;
import com.weimob.webfwk.state.util.StateFormStateBaseEnum;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.sql.SqlQueryUtil;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

public class SystemDataChartService
        extends AbstractStateFormServiceWithBaseDao<SystemDataChartFormDetail, SystemDataChartFormDefault, SystemDataChartFormSimple> {
    
    private SystemDataChartService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final SystemDataChartService Instance = new SystemDataChartService();
    
    public class FormOwnerChecker implements AuthoritySpecialChecker {
        @Override
        public boolean check(Object form) throws Exception {
            return form != null
                    && SessionContext.getDisplay().equals(((SystemDataChartFormSimple) form).getCreatedNameBy()) ;
        }
    }
    
    public class ChartTypeMailTempRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object form) throws Exception {
            return !(form != null && StringUtils.isNotBlank(((SystemDataChartFormDetail) form).getMailTemplate()));
        }
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemDataChartFormSimple> forms) throws Exception {
        
    }

    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        CREATED("created", "已创建"),

        ;
        private final String code;
        private final String name;

        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        Edit(EventEdit.class),
        ChartSendMail(EventNotify.class),
        ;
        private final Class<? extends AbstractStateAction<SystemDataChartFormDetail, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SystemDataChartFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemDataChartFormDetail, SystemDataChartFormCreation> {
        public EventCreate() {
            super("创建图表", STATES.CREATED.getCode());
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemDataChartFormDetail originForm,
                           final SystemDataChartFormCreation form, final String message) throws Exception {
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareInsertQuery(getFormTable(), new ObjectMap()
                            .put("uuid", UUID.randomUUID())
                            .put("title", form.getTitle())
                            .put("query_sql", form.getQuerySql())
                            .put("chart_type", form.getChartType())
                            .put("chart_params", form.getChartParams())
                            .put("data_sum", form.getDataSum())
                            .put("sql_params", form.getSqlParams())
                            .put("mail_template", form.getMailTemplate())
                            .put("data_label_column", form.getDataLabelColumn())
                    ), new AbstractDao.ResultSetProcessor() {
                        @Override
                        public void process(ResultSet result, Connection conn) throws Exception {
                            result.next();
                            form.setId(result.getLong(1));
                        }
                    });
            return new StateFormEventResultCreateViewBasic(form.getId());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemDataChartFormDetail originForm, String sourceState) {

        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemDataChartFormDetail, SystemDataChartFormEdition, Void> {
        public EventEdit() {
            super("编辑", STATES.CREATED.getCode(), "");
        }
        
        @Override
        public Void handle(String event, SystemDataChartFormDetail originForm, final SystemDataChartFormEdition form,
                final String message) throws Exception {
            
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareUpdateQuery(getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("title", form.getTitle())
                            .put("query_sql", form.getQuerySql())
                            .put("chart_type", form.getChartType())
                            .put("chart_params", form.getChartParams())
                            .put("data_sum", form.getDataSum())
                            .put("sql_params", form.getSqlParams())
                            .put("mail_template", form.getMailTemplate())
                            .put("data_label_column", form.getDataLabelColumn())
                    ));
            return null;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = FormOwnerChecker.class)
        public void check(String event, SystemDataChartFormDetail originForm, String sourceState) {
            
        }
    }
    
    public class EventNotify
            extends AbstractStateAction<SystemDataChartFormDetail, SystemDataChartFormMail, Void> {
        public EventNotify() {
            super("邮件发送", STATES.CREATED.getCode(), "");
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() throws Exception {
            return true;
        }
        
        @Override
        public Void handle(String event, SystemDataChartFormDetail originForm, final SystemDataChartFormMail form,
                final String message) throws Exception {
            SystemDataChartUtil.sendMailContent(originForm.getMailTemplate(), originForm.getChartData());
            return null;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, rejecter = ChartTypeMailTempRejecter.class)
        public void check(String event, SystemDataChartFormDetail originForm, String sourceState) {
            
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<>(
                "默认查询",
                SystemDataChartFormDefault.class,
                SystemDataChartQueryDefault.class
            ));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Override
    protected String getFormTable() {
        return "system_data_chart";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public SystemDataChartFormDetail getForm(long formId) throws Exception {
        
        SystemDataChartFormDetail form = this.getForm(SystemDataChartFormDetail.class, formId);
        
        try {
            /**
             * 获取前端查询参数（从前端发起的请求，可通过URL传递参数）
             */
            NameValuePair[] contextQueries;
            Map<String, String> queryParams = new HashMap<>();
            if ((contextQueries = getContextQueries()) != null && contextQueries.length > 0) {
                for (NameValuePair nameValuePair : contextQueries) {
                    queryParams.put(nameValuePair.getName(), nameValuePair.getValue());
                }
            }
            
            /**
             * 解析并拼装SQL参数数据
             */
            String[] sqlargs = new String[0];
            if (StringUtils.isNotBlank(form.getSqlParams())) {
                SystemDataChartUtil.SqlParamsDefinition sqlParamDefinition = CommonUtil.fromJson(form.getSqlParams(),
                                        SystemDataChartUtil.SqlParamsDefinition.class);
                String[] namedPlaceholders = sqlParamDefinition.getNamedPlaceholders();
                Map<String, String> namedValues;
                form.setSqlParamsMap(namedValues = sqlParamDefinition.getNamedValues());
                namedValues.putAll(queryParams);
                sqlargs = SystemDataChartUtil.parseAndGenSqlArgs(namedValues, namedPlaceholders);
            }
            
            /**
             * 查询并组装图表数据
             */
            List<Map<String, Object>> chartData = SystemDataChartUtil.getOriginData(form.getQuerySql(), sqlargs);
            form.setChartData(SystemDataChartUtil.genChartData(form.getChartType(), chartData));
            
            /**
             * 根据邮件模板和原始数据，生成邮件内容
             */
            form.setMailContent(SystemDataChartUtil.genMailContent(form.getMailTemplate(), chartData));
        } catch (Exception e) {
            form.setErrorMessage((e instanceof MessageException) ? e.getMessage() : "构造数据异常，请检查图表配置");
        }
        return form;
    }

    @Override
    public String getFormName() {
        return "system_data_chart";
    }
    
    @Override
    public String getFormDisplay() {
        return "数据图表";
    }
}