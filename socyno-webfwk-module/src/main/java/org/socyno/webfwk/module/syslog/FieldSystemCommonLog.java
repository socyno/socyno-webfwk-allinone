package org.socyno.webfwk.module.syslog;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.tool.CommonUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author yanming.zhang.wb
 * @date 2020-09-17 15:40
 */

public class FieldSystemCommonLog {
    
    protected static AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }

    public static List<CommonLogDetail> getLogDetail(Long ...formId) throws Exception{
        
        if(formId == null || formId.length <=0 ){
            return Collections.emptyList();
        }
        
        return getFormBaseDao().queryAsList(CommonLogDetail.class,
                String.format(SQL_QUERY_LOG_DETAIL, SystemCommonLogService.DEFAULT.getFormName(),
                        SystemCommonLogService.DEFAULT.getLogDetailTable()
                        , CommonUtil.join("?", formId.length,",")), formId);
    }

    /**
     * SELECT 
     *   d.*
     * FROM
     *   %s s 
     * JOIN %s d 
     *   ON 
     *   s.`operate_detail_id` = d.`id`
     *  AND 
     *  s.`id` IN (%s)
     *  
     */
    @Multiline
    private static final String SQL_QUERY_LOG_DETAIL="X";
    

    @Getter
    @Setter
    @ToString
    public static class CommonLogDetail{
        
        @Attributes(title = "日志ID")
        private Long id;
        
        @Attributes(title = "操作前")
        private String operateBefore;
        
        @Attributes(title = "操作后")
        private String operateAfter;
        
    }

}
