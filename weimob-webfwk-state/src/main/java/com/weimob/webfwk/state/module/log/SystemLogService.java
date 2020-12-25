package com.weimob.webfwk.state.module.log;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adrianwalker.multilinestring.Multiline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateAction;
import com.weimob.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import com.weimob.webfwk.state.service.SimpleLogService;
import com.weimob.webfwk.state.util.StateFormEventClassEnum;
import com.weimob.webfwk.state.util.StateFormNamedQuery;
import com.weimob.webfwk.state.util.StateFormQueryBaseEnum;
import com.weimob.webfwk.state.util.StateFormStateBaseEnum;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.tool.CommonUtil;

public class SystemLogService
        extends AbstractStateFormServiceWithBaseDao<SystemLogFormDetail, SystemLogFormDefault, SystemLogFormSimple> {
    
    @Getter
    private static final SystemLogService Instance = new SystemLogService();

    private SystemLogService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemLogFormSimple> forms) throws Exception {
        
        if (forms == null || forms.size() <= 0) {
            return;
        }
        
        List<SystemLogFormWithDetails> sameDetails;
        Map<Long, List<SystemLogFormWithDetails>> withDetails = new HashMap<>();
        
        for (SystemLogFormSimple form : forms) {
            if (form == null || form.getOperateDetailId() == null) {
                continue;
            }
            if (SystemLogFormWithDetails.class.isAssignableFrom(form.getClass())) {
                if ((sameDetails = withDetails.get(form.getOperateDetailId())) == null) {
                    withDetails.put(form.getOperateDetailId(), sameDetails = new ArrayList<>());
                }
                sameDetails.add((SystemLogFormWithDetails) form);
            }
        }
        
        if (withDetails.size() > 0) {
            List<SystemLogDetail> allDetails;
            if ((allDetails = queryLogDetails(withDetails.keySet().toArray(new Long[0]))) != null
                    && allDetails.size() > 0) {
                Map<Long, SystemLogDetail> mappedDetails = new HashMap<>();
                for (SystemLogDetail d : allDetails) {
                    mappedDetails.put(d.getId(), d);
                }
                SystemLogDetail logDetail;
                for (Map.Entry<Long, List<SystemLogFormWithDetails>> e : withDetails.entrySet()) {
                    for (SystemLogFormWithDetails logWithDetail : e.getValue()) {
                        if ((logDetail = mappedDetails.get(logWithDetail.getOperateDetailId())) == null) {
                            continue;
                        }
                        logWithDetail.setOperateAfter(logDetail.getOperateAfter());
                        logWithDetail.setOperateBefore(logDetail.getOperateBefore());
                    }
                }
            }
        }
    }
    
    /**
     * SELECT d.* FROM  %s d WHERE d.id IN (%s)
     */
    @Multiline
    private static final String SQL_QUERY_LOG_DETAIL = "X";
    
    private List<SystemLogDetail> queryLogDetails(Long... detailIds) throws Exception {
        if (detailIds == null || detailIds.length <= 0) {
            return Collections.emptyList();
        }
        return getFormBaseDao().queryAsList(
                SystemLogDetail.class,
                String.format(SQL_QUERY_LOG_DETAIL, getLogDetailTable(), CommonUtil.join("?", detailIds.length, ",")),
                detailIds);
    }
    
    @Getter
    @Setter
    @ToString
    public static class SystemLogDetail {
        
        @Attributes(title = "日志ID")
        private Long id;
        
        @Attributes(title = "操作前")
        private String operateBefore;
        
        @Attributes(title = "操作后")
        private String operateAfter;
        
    }

    @Getter
    public enum STATES implements StateFormStateBaseEnum {
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
        ;
        private final Class<? extends AbstractStateAction<SystemLogFormSimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SystemLogFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        /**
         *  默认查询
         */
        DEFAULT(new StateFormNamedQuery<SystemLogFormDefault>(
                "默认查询",
                SystemLogFormDefault.class, 
                SystemLogQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Override
    protected String getFormTable() {
        return SimpleLogService.getFormTable();
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SimpleLogService.getDao();
    }
    
    @Override
    public String getFormName() {
        return SimpleLogService.getFormTable();
    }
    
    @Override
    public String getFormCreatedAtField() {
        return "operate_time";
    }
    
    @Override
    public String getFormCreatedByField() {
        return "operate_user_id";
    }
    
    @Override
    public String getFormCreatedCodeByField() {
        return "operate_user_name";
    }
    
    @Override
    public String getFormCreatedNameByField() {
        return "operate_user_display";
    }
    
    private String getLogDetailTable() {
        return SimpleLogService.getDetailTable();
    }
    
    @Override
    public String getFormDisplay() {
        return "系统日志";
    }
}