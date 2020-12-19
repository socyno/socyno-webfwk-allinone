package org.socyno.webfwk.module.productline;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.module.application.FieldApplicationOfflineIncluded;
import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessable;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
import org.socyno.webfwk.state.field.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "产品线清单查询")
public class ProductlineQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字", position = 10)
    private String keyword;
    
    @Attributes(title = "包括已禁用", position = 15)
    private boolean disableIncluded;
    
    @Attributes(title = "负责人", position = 20, type = FieldSystemUser.class)
    private Long ownerId;
    
    @Attributes(title = "包含应用", position = 30, type = FieldApplicationOfflineIncluded.class)
    private Long applicationId;
    
    @Attributes(title = "包含业务系统", position = 40, type = FieldSubsystemAccessable.class)
    private Long subsystemId;
    
    @Attributes(title = "产品编号列表")
    private String idsIn;
    
    @Attributes(title = "产品代码列表")
    private String codesIn;
    
    protected String requiredAccessEventKey() {
        return ProductlineService.getInstance().getFormAccessEventKey();
    }
    
    public ProductlineQueryDefault(String keyword, Integer limit, Long page) {
        super(limit, page);
        setKeyword(keyword);
    }
    
    public ProductlineQueryDefault(Integer limit, Long page) {
        this("", limit, page);
    }
    
    public ProductlineQueryDefault(Integer limit) {
        this(limit, null);
    }
    
    public ProductlineQueryDefault(String keyword) {
        this(keyword, null, null);
    }
    
    public ProductlineQueryDefault() {
        this((String)null);
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
        StringBuilder sqlwhere = new StringBuilder();
        
        if (getSubsystemId() != null) {
            sqlArgs.add(getSubsystemId());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append("EXISTS (SELECT ps.productline_id FROM productline_subsystem ps")
                    .append(" WHERE ps.productline_id = s.id AND ps.subsystem_id = ?)");
        }
        
        if (getApplicationId() != null) {
            sqlArgs.add(getApplicationId());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append("EXISTS (SELECT ps.productline_id FROM productline_subsystem ps, application pa")
                    .append(" WHERE pa.id = ? AND ps.productline_id = s.id AND pa.subsystem_id = ps.subsystem_id)");
        }
        
        if (StringUtils.isNotBlank(getKeyword())) {
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append("( s.code LIKE CONCAT('%', ?, '%') OR s.name LIKE CONCAT('%', ?, '%')"
                            + " OR s.description LIKE CONCAT('%', ?, '%'))");
            sqlArgs.add(getKeyword());
            sqlArgs.add(getKeyword());
            sqlArgs.add(getKeyword());
        }
        
        if (getOwnerId() != null) {
            sqlArgs.add(getOwnerId());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("s.owner_id = ?");
        }
        
        if (!isDisableIncluded()) {
            sqlArgs.add(ProductlineService.STATES.DISABLED.getCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format("s.%s != ?", ProductlineService.getInstance().getFormStateField()));
        }
        
        if (StringUtils.isNotBlank(getIdsIn())) {
            String[] prodIds = CommonUtil.split(getIdsIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
            if (prodIds != null && prodIds.length > 0) {
                sqlArgs.addAll(Arrays.asList(prodIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("s.id IN ")
                        .append(CommonUtil.join("?", prodIds.length, ",", "(", ")"));
            } else {
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("1 = 0 ");
            }
        }
        
        if (StringUtils.isNotBlank(getCodesIn())) {
            String[] prodCodes = CommonUtil.split(getCodesIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
            if (prodCodes != null && prodCodes.length > 0) {
                sqlArgs.addAll(Arrays.asList(prodCodes));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("s.code IN ")
                        .append(CommonUtil.join("?", prodCodes.length, ",", "(", ")"));
            } else {
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("1 = 0 ");
            }
        }
        
        return new BasicSqlStatement().setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString())
                .setValues(sqlArgs.toArray());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format("SELECT COUNT(1) FROM %s s %s" ,
                        ProductlineService.getInstance().getFormTable() , query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                SQL_SELECT_FORM.concat(" %s ORDER BY s.code ASC LIMIT %s, %s"),
                ProductlineService.getInstance().getFormTable(), query.getSql() ,
                getOffset(), getLimit()));
    }
}
