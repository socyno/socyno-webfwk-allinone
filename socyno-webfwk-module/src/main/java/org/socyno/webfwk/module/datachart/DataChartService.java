package org.socyno.webfwk.module.datachart;

import lombok.Getter;
import org.apache.http.NameValuePair;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.authority.AuthoritySpecialRejecter;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDaoV2;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.module.notify.SystemNotifyService;
import org.socyno.webfwk.state.module.notify.SystemNotifyTemplateService;
import org.socyno.webfwk.state.module.notify.SystemNotifyTemplateSimple;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tmpl.EnjoyUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DataChartService extends AbstractStateFormServiceWithBaseDaoV2<DataChartSimpleForm> {

    public static final DataChartService DEFAULT = new DataChartService();

    private static final String LINE_CHART = "line";
    private static final String BAR_CHART = "bar";
    private static final String PIE_CHART = "pie";
    private static final String ONLY_DATA = "onlyData";

    public DataChartService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }

    public static class FormOwnerChecker implements AuthoritySpecialChecker {
        @Override
        public boolean check(Object form) throws Exception {
            return form != null
                    && SessionContext.getDisplay().equals(((DataChartSimpleForm) form).getCreatedNameBy()) ;
        }
    }
    
    public static class ChartTypeMailTempRejecter implements AuthoritySpecialRejecter{

        @Override
        public boolean check(Object form) throws Exception {
            return !(form != null
                    && SessionContext.getDisplay().equals(((DataChartSimpleForm) form).getCreatedNameBy())
                    && StringUtils.isNotBlank(((DataChartDetailForm) form).getMailTable()));
        }
    }
    

    @Override
    protected void fillExtraFormFields(Collection<? extends DataChartSimpleForm> forms) throws Exception {

        DefaultStateFormSugger.getInstance().apply(forms);

        DataChartSimpleForm form = (DataChartSimpleForm) forms.toArray()[0];
        //传进来的映射关系
        NameValuePair[] contextQueries = getContextQueries();
        Map<String, String> sqlParamsMap = new HashMap<>();
        if (contextQueries != null && contextQueries.length > 0) {
            for (NameValuePair nameValuePair : contextQueries) {
                sqlParamsMap.put(nameValuePair.getName(), nameValuePair.getValue());
            }
        }
        String[] sqlParams = new String[]{};
        if (DataChartDetailForm.class.isAssignableFrom(form.getClass())) {
            //处理sql参数
            if (StringUtils.isNotBlank(form.getSqlParams())) {
                DataChartUtil.SqlMapParam sqlMapParam = 
                        CommonUtil.fromJson(form.getSqlParams(), DataChartUtil.SqlMapParam.class);
                String[] replaceParams=sqlMapParam.getSqlParams();
                if (sqlParamsMap.size() > 0) {
                    //需要变更默认参数
                    sqlParams = DataChartUtil.replaceParams(sqlParamsMap, replaceParams);
                    ((DataChartDetailForm)form).setSqlParamsMap(sqlParamsMap);
                } else {
                    //无需变更默认参数
                    sqlParams = DataChartUtil.replaceParams(sqlMapParam.getSqlMap(), replaceParams);
                    ((DataChartDetailForm)form).setSqlParamsMap(sqlMapParam.getSqlMap());
                }
            }
            //是否需要从模板生成数据
            if (StringUtils.isNotBlank(form.getMailCode())) {
                SystemNotifyTemplateSimple templateSimple = null;
                try {
                    templateSimple = SystemNotifyTemplateService.getInstance().getByCode(form.getMailCode());
                } catch (Exception e) {
                    throw new MessageException("邮件模板code错误");
                }
                List<Map<String, Object>> originData = DataChartUtil.getOriginData(form.getQuerySql(), sqlParams);
                Map<String, Object> dataMap = new HashMap<>(4);
                dataMap.put("mailTempData", originData);
                ObjectMap tempContext = new ObjectMap()
                        .put("systemUserService", SystemUserService.DEFAULT)
                        .put("sessionContext", SessionContext.getUserContext());
                if (dataMap.size() > 0) {
                    tempContext.putAll(dataMap);
                }
                String mailContent = null;
                try {
                    mailContent = EnjoyUtil.format(templateSimple.getMailContent(), tempContext.asMap());
                } catch (Exception e) {
                    throw new MessageException("模板数据合并异常,请确认模板和数据");
                }
                ((DataChartDetailForm) form).setMailTable(mailContent);
                ((DataChartDetailForm) form).setMailContext(originData);
            }
            if ((LINE_CHART.equals(form.getChartType().getOptionValue()) ||
                    BAR_CHART.equals(form.getChartType().getOptionValue()))) {
                ((DataChartDetailForm) form).setChartData(DataChartUtil.
                        getOriginData(form.getQuerySql(), sqlParams));
            } else if (PIE_CHART.equals(form.getChartType().getOptionValue())) {
                ((DataChartDetailForm) form).setChartData(DataChartUtil.
                        getPieChartData(form.getQuerySql(), sqlParams));
            } else if (ONLY_DATA.equals(form.getChartType().getOptionValue())){
                ((DataChartDetailForm) form).setChartData(DataChartUtil.
                        getOriginData(form.getQuerySql(), sqlParams));
            } else {
                //不确定图表类型时,默认返回查询结果
                ((DataChartDetailForm) form).setChartData(DataChartUtil.
                        getOriginData(form.getQuerySql(), sqlParams));
            }
        }
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
        ChartSendMail(EventChartSendMail.class),
        ;
        private final Class<? extends AbstractStateAction<DataChartSimpleForm, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<DataChartSimpleForm, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }


    public class EventCreate extends AbstractStateSubmitAction<DataChartSimpleForm, DataChartCreateForm> {
        public EventCreate() {
            super("创建图表", STATES.CREATED.getCode());
        }

        @Override
        public Long handle(String event, DataChartSimpleForm originForm,
                           final DataChartCreateForm form, final String message) throws Exception {
            
            final AtomicLong simpleId = new AtomicLong(0);
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareInsertQuery(getFormTable(), new ObjectMap()
                            .put("created_by", SessionContext.getUserId())
                            .put("created_name_by", SessionContext.getDisplay())
                            .put("created_at", new Date())
                            .put("title", form.getTitle())
                            .put("query_sql", form.getQuerySql())
                            .put("chart_type", form.getChartType().getOptionValue())
                            .put("chart_params", StringUtils.isBlank(form.getChartParams()) ? "" : form.getChartParams())
                            .put("need_sum", StringUtils.isBlank(form.getNeedSum()) ? "" : form.getNeedSum())
                            .put("sql_params", StringUtils.isBlank(form.getSqlParams()) ? "" : form.getSqlParams())
                            .put("uuid", UUID.randomUUID())
                            .put("mail_code", StringUtils.isBlank(form.getMailCode())? "" : form.getMailCode())
                            .put("first_column", StringUtils.isBlank(form.getFirstColumn())? "" : form.getFirstColumn())
                    ), new AbstractDao.ResultSetProcessor() {
                        @Override
                        public void process(ResultSet result, Connection conn) throws Exception {
                            result.next();
                            simpleId.set(result.getLong(1));
                        }
                    });

            return simpleId.get();
        }

        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, DataChartSimpleForm originForm, String sourceState) {

        }
    }

    public class EventEdit extends AbstractStateAction<DataChartSimpleForm, DataChartEditForm, Void> {
        public EventEdit() {
            super("编辑", STATES.CREATED.getCode(), "");
        }

        @Override
        public Void handle(String event, DataChartSimpleForm originForm,
                        final DataChartEditForm form, final String message) throws Exception {
            
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareUpdateQuery(getFormTable(), new ObjectMap()
                            .put("=id", form.getId())
                            .put("title", form.getTitle())
                            .put("query_sql", form.getQuerySql())
                            .put("chart_type", form.getChartType().getOptionValue())
                            .put("chart_params", StringUtils.isBlank(form.getChartParams()) ? "" : form.getChartParams())
                            .put("need_sum", StringUtils.isBlank(form.getNeedSum()) ? "" : form.getNeedSum())
                            .put("sql_params", StringUtils.isBlank(form.getSqlParams()) ? "" : form.getSqlParams())
                            .put("mail_code", StringUtils.isBlank(form.getMailCode())? "" : form.getMailCode())
                            .put("first_column", StringUtils.isBlank(form.getFirstColumn())? "" : form.getFirstColumn())
                    ));
            return null;
        }

        @Override
        @Authority(value = AuthorityScopeType.System, checker = FormOwnerChecker.class)
        public void check(String event, DataChartSimpleForm originForm, String sourceState) {

        }
    }

    public class EventChartSendMail extends AbstractStateAction<DataChartSimpleForm, DataChartMailForm, Void> {
        public EventChartSendMail() {
            super("邮件发送", STATES.CREATED.getCode(), "");
        }


        @Override
        public boolean getStateRevisionChangeIgnored() throws Exception {
            return true;
        }

        @Override
        public Void handle(String event, DataChartSimpleForm originForm,
                           final DataChartMailForm form, final String message) throws Exception {

            Map<String, Object> dataMap = new HashMap<>();
            if (form.getMailContext() == null) {
                dataMap.put("mailTempData", ((DataChartDetailForm)originForm).getMailContext());
            }else{
                dataMap.put("mailTempData", form.getMailContext());
            }
            SystemNotifyService.sendSync(originForm.getMailCode(), dataMap, 0);
            
            return null;
        }

        @Override
        @Authority(value = AuthorityScopeType.System, rejecter = ChartTypeMailTempRejecter.class)
        public void check(String event, DataChartSimpleForm originForm, String sourceState) {

        }
    }


    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<>("default",
                DataChartListForm.class, DataChartDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    @Override
    protected String getFormTable() {
        return "apply_charts_simple";
    }

    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public DataChartDetailForm getForm(long formId) throws Exception {
        return this.getForm(DataChartDetailForm.class, formId);
    }

    @Override
    public String getFormName() {
        return "apply_charts_simple";
    }
    
}