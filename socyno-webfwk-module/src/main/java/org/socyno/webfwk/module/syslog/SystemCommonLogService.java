package org.socyno.webfwk.module.syslog;

import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDaoV2;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.sql.AbstractDao;

public class SystemCommonLogService extends AbstractStateFormServiceWithBaseDaoV2<SystemCommonLogSimple> {

    public static final SystemCommonLogService DEFAULT = new SystemCommonLogService();

    public SystemCommonLogService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemCommonLogSimple> forms) throws Exception {

        if (forms == null || forms.size() <= 0) {
            return;
        }
        Map<Long,SystemCommonLogDetail> detailMap=new HashMap<>(16);

        for (SystemCommonLogSimple simple : forms) {
            if(SystemCommonLogDetail.class.isAssignableFrom(simple.getClass())){
                detailMap.put(simple.getId(),(SystemCommonLogDetail) simple);
            }
        }
        if(detailMap.size() > 0){
            List<FieldSystemCommonLog.CommonLogDetail> logDetailList = 
                    FieldSystemCommonLog.getLogDetail(detailMap.keySet().toArray(new Long[0]));
            if(logDetailList == null || logDetailList.size()<=0){
                return;
            }
            for (Map.Entry<Long, SystemCommonLogDetail> logDetailEntry : detailMap.entrySet()) {
                for (FieldSystemCommonLog.CommonLogDetail logDetail : logDetailList) {
                    if(logDetailEntry.getValue().getOperateDetailId().equals(logDetail.getId())){
                        logDetailEntry.getValue().setOperateBefore(logDetail.getOperateBefore());
                        logDetailEntry.getValue().setOperateAfter(logDetail.getOperateAfter());
                    }
                }
            }
        }
        
         
        
        
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
        private final Class<? extends AbstractStateAction<SystemCommonLogSimple, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<SystemCommonLogSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        /**
         *  默认查询
         */
        DEFAULT(new StateFormNamedQuery<SystemCommonLogDefault>("default",
                SystemCommonLogDefault.class, SystemCommonLogDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    @Override
    protected String getFormTable() {
        return "system_common_log";
    }

    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }

    @Override
    public String getFormName() {
        return "system_common_log";
    }
    
    public String getLogDetailTable() {
        return "system_common_log_detail";
    }

    @Override
    public SystemCommonLogDetail getForm(long formId) throws Exception {
        return this.getForm(SystemCommonLogDetail.class, formId);
    }
}