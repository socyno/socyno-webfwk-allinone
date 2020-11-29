package org.socyno.webfwk.module.department;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.module.app.form.FieldApplicationOfflineIncluded;
import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessors;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.state.field.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain=true)
@Attributes(title="产品线清单查询")
public class DepartmentListDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字", position = 10)
    private String keyword;
    
    @Attributes(title = "包括已禁用", position = 15)
    private boolean disableIncluded;
    
    @Attributes(title = "负责人", position = 20, type = FieldSystemUser.class)
    private Long ownerId;
    
    @Attributes(title = "包含应用", position = 30, type = FieldApplicationOfflineIncluded.class)
    private Long applicationId;
    
    @Attributes(title = "包含业务系统", position = 40, type = FieldSubsystemAccessors.class)
    private Long subsystemId;
    
    protected String requiredAccessEventKey() {
        return DepartmentService.DEFAULT.getFormAccessEventKey();
    }
    
    public DepartmentListDefaultQuery(String keyword, Integer limit, Integer page) {
        super(limit, page);
        setKeyword(keyword);
    }
    
    public DepartmentListDefaultQuery(String keyword) {
        this(keyword, null, null);
    }
    
    public DepartmentListDefaultQuery() {
        super();
    }
    
    /**
     *  SELECT DISTINCT
     *       s.*
     *  FROM %s s
     */
    @Multiline
    public final static String SQL_SELECT_FORM = "X";
    
    public AbstractSqlStatement buildWhereSql() throws Exception {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder builder = new StringBuilder("WHERE 1 = 1");
        
        if (getSubsystemId() != null) {
            sqlArgs.add(getSubsystemId());
            builder.append(" and EXISTS (SELECT ps.productline_id FROM productline_subsystem ps")
                    .append(" WHERE ps.productline_id = s.id AND ps.subsystem_id = ?)");
        }
        
        if (getApplicationId() != null) {
            sqlArgs.add(getApplicationId());
            builder.append(" and EXISTS (SELECT ps.productline_id FROM productline_subsystem ps, application pa")
                    .append(" WHERE pa.id = ? AND ps.productline_id = s.id AND pa.subsystem_id = ps.subsystem_id)");
        }
        
        if (StringUtils.isNotBlank(getKeyword())) {
            builder.append(" and ( s.code LIKE CONCAT('%', ?, '%') OR s.name LIKE CONCAT('%', ?, '%')"
                                + " OR s.description LIKE CONCAT('%', ?, '%'))");
            sqlArgs.add(getKeyword());
            sqlArgs.add(getKeyword());
            sqlArgs.add(getKeyword());
        }
        if (getOwnerId() != null) {
            sqlArgs.add(getOwnerId());
            builder.append(" and s.owner_id = ?");
        }
        if (!isDisableIncluded()) {
            sqlArgs.add(DepartmentService.STATES.DISABLED.getCode());
            builder.append(String.format(" and s.%s != ?", DepartmentService.DEFAULT.getFormStateField()));
        }
        return new BasicSqlStatement().setSql(builder.toString()).setValues(sqlArgs.toArray());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format("SELECT COUNT(1) FROM %s s %s" ,
                        DepartmentService.DEFAULT.getFormTable() , query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                SQL_SELECT_FORM + " %s ORDER BY s.code ASC LIMIT %s, %s",
                DepartmentService.DEFAULT.getFormTable(), query.getSql() ,
                getOffset(), getLimit()));
    }
}
