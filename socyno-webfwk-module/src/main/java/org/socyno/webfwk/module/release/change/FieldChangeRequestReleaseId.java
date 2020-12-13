package org.socyno.webfwk.module.release.change;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class FieldChangeRequestReleaseId extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Getter
    @Setter
    @ToString
    public static class OptionReleaseId implements FieldOption {
        
        private String title;
        
        private String category;
        
        private String releaseId;
        
        private String releaseDate;
        
        private String releaseCycle;
        
        @Override
        public String getOptionDisplay() {
            return releaseId;
        }
        
        @Override
        public String getOptionValue() {
            return releaseId;
        }
        
        @Override
        public String getOptionGroup() {
            return releaseCycle;
        }
        
        @Override
        public void setOptionValue(String value) {
            setReleaseId(value);
        }
        
        @Override
        public int hashCode() {
            return getOptionValue() == null ? -1 : getOptionValue().hashCode();
        }
        
        @Override
        public boolean equals(Object anthor) {
            if (anthor instanceof OptionReleaseId) {
                return StringUtils.equals(getOptionValue(), ((OptionReleaseId) anthor).getOptionValue());
            }
            return false;
        }
        
    }
    
    /**
     * SELECT
     *    r.title,
     *    r.category,
     *    r.release_id,
     *    r.release_date,
     *    r.release_cycle
     * FROM
     *    release_change_requirement r
     * %s
     * ORDER BY r.release_date DESC
     * LIMIT 0, 20
     */
    @Multiline
    private final static String SQL_QUERY_OPTION_RELEASES = "X";
    
    public List<? extends OptionReleaseId> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return queryDynamicOptions(filter, false);
        
    }
    
    public List<? extends OptionReleaseId> queryDynamicOptions(FilterBasicKeyword filter, boolean mineOnly)
            throws Exception {
        List<Object> whereArgs = new LinkedList<>();
        StringBuilder whereSql = new StringBuilder();
        if (filter != null && StringUtils.isNotBlank(filter.getKeyword())) {
            whereArgs.add(filter.getKeyword());
            StringUtils.appendIfNotEmpty(whereSql, " AND ")
                    .append(filter.getKeyword().matches("^\\d+$") ? "r.release_id LIKE CONCAT('%', ?)"
                            : "r.release_id LIKE CONCAT(?, '%')");
        }
        if (mineOnly) {
            whereArgs.add(SessionContext.getUsername());
            StringUtils.appendIfNotEmpty(whereSql, " AND ").append(
                    "EXISTS (SELECT u.release_id FROM release_change_requirement_user u WHERE u.username = ? AND u.release_id = r.release_id)");
        }
        
        return ChangeRequestService.getInstance().getFormBaseDao().queryAsList(OptionReleaseId.class,
                String.format(SQL_QUERY_OPTION_RELEASES, StringUtils.prependIfNotEmpty(whereSql, "WHERE ")),
                whereArgs.toArray());
    }
    
    /**
     * SELECT
     *    r.title,
     *    r.category,
     *    r.release_id,
     *    r.release_date,
     *    r.release_cycle
     * FROM
     *    release_change_requirement r
     * WHERE
     *    r.release_id IN (%s)
     *    %s
     */
    @Multiline
    private final static String SQL_QUERY_VALUE_RELEASES = "X";
    
    public static List<? extends OptionReleaseId> queryDynamicValues(String[] values) throws Exception {
        return queryDynamicValues(values, false);
    }
    
    public static List<? extends OptionReleaseId> queryDynamicValues(String[] values, boolean mineOnly)
            throws Exception {
        if (values == null || (values = ConvertUtil.asNonBlankUniqueTrimedStringArray((Object[]) values)).length <= 0) {
            return Collections.emptyList();
        }
        List<Object> whereArgs = new LinkedList<>();
        StringBuilder whereSql = new StringBuilder();
        if (mineOnly) {
            whereArgs.add(SessionContext.getUsername());
            whereSql.append(
                    " AND EXISTS (SELECT u.release_id FROM release_change_requirement_user u WHERE u.username = ? AND u.release_id = r.release_id)");
        }
        return ChangeRequestService.getInstance().getFormBaseDao().queryAsList(OptionReleaseId.class,
                String.format(SQL_QUERY_VALUE_RELEASES, CommonUtil.join("?", values.length, ","), whereSql.toString()),
                ArrayUtils.addAll(values, whereArgs.toArray()));
    }
    
}
