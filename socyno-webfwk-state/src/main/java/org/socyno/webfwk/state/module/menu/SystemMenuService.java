package org.socyno.webfwk.state.module.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.basic.AbstractStateAction;
import org.socyno.webfwk.state.basic.AbstractStateDeleteAction;
import org.socyno.webfwk.state.basic.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.basic.AbstractStateSubmitAction;
import org.socyno.webfwk.state.basic.BasicStateForm;
import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.state.field.OptionSystemAuth;
import org.socyno.webfwk.state.module.feature.SystemFeatureService;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;

import lombok.Getter;
import lombok.Setter;
public class SystemMenuService extends AbstractStateFormServiceWithBaseDao<SystemMenuDetail> {
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
          ENABLED  ("enabled", "有效")
        , DISABLED ("disabled", "禁用")
        ;
        
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
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemMenuListDefaultForm>("default", 
                SystemMenuListDefaultForm.class, SystemMenuListDefaultQuery.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
        public static List<StateFormNamedQuery<?>> getQueries() {
            List<StateFormNamedQuery<?>> queries = new ArrayList<>();
            for (QUERIES item : QUERIES.values()) {
                queries.add(item.getNamedQuery());
            }
            return queries;
        }
    }
    
    public static enum EVENTS implements StateFormEventBaseEnum {
        Create(new AbstractStateSubmitAction<SystemMenuDetail, SystemMenuForCreation>("创建", STATES.ENABLED.getCode()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemMenuDetail form, String sourceState) {
                
            }
            
            @Override
            public Long handle(String event, SystemMenuDetail originForm, SystemMenuForCreation form, String message)
                    throws Exception {
                final AtomicLong id = new AtomicLong(-1);
                DEFAULT.getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet r, Connection c) throws Exception {
                        DEFAULT.getFormBaseDao().executeUpdate(
                                SqlQueryUtil.prepareInsertQuery(DEFAULT.getFormTable(),
                                        new ObjectMap()
                                                .put("path", form.getPath())
                                                .put("name", form.getName())
                                                .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                                .put("order",form.getOrder())
                                                .put("dir_id", form.getDirId())
                            ), new ResultSetProcessor() {
                                @Override
                                public void process(ResultSet r, Connection c) throws Exception {
                                    r.next();
                                    id.set(r.getLong(1));
                                    List<OptionSystemAuth> auths;
                                    if ((auths = form.getAuths()) != null) {
                                        setAuths(id.get(), auths);
                                    }
                                }
                            });
                    }
                });
                return id.get();
            }
        }),
        Update(new AbstractStateAction<SystemMenuDetail, SystemMenuForEdition, Void>("编辑", STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemMenuDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, final SystemMenuDetail originForm, final SystemMenuForEdition form,
                    final String message) throws Exception {
                getDao().executeTransaction(new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet result, Connection conn) throws Exception {
                        List<OptionSystemAuth> auths;
                        DEFAULT.getFormBaseDao()
                                .executeUpdate(SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(),
                                        new ObjectMap()
                                                .put("=id", form.getId())
                                                .put("name", form.getName())
                                                .put("path", form.getPath())
                                                .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                                .put("order", form.getOrder())
                                                .put("dir_id", form.getDirId())
                                            ));
                        
                        if ((auths = form.getAuths()) != null) {
                            setAuths(form.getId(), auths);
                        }
                    }
                });
                return null;
            }
        }),
        Delete(new AbstractStateDeleteAction<SystemMenuDetail>("删除", STATES.stringifyEx()) {
            @Override
            @Authority(value = AuthorityScopeType.System)
            public void check(String event, SystemMenuDetail form, String sourceState) {
                
            }
            
            @Override
            public Void handle(String event, final SystemMenuDetail originForm, final BasicStateForm form,
                    final String message) throws Exception {
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(DEFAULT.getFormTable(),
                        new ObjectMap().put("=id", originForm.getId())));
                setAuths(originForm.getId(), Collections.emptyList());
                return null;
            }
        });
        
        private final AbstractStateAction<SystemMenuDetail, ?, ?> action;
        
        EVENTS(AbstractStateAction<SystemMenuDetail, ?, ?> action) {
            this.action = action;
        }
        
        public AbstractStateAction<SystemMenuDetail, ?, ?> getAction() {
            return action;
        }
    }
    
    public static final SystemMenuService DEFAULT = new SystemMenuService();
    
    @Override
    public String getFormName() {
        return getName();
    }
    
    @Override
    protected String getFormTable() {
        return getTable();
    }
    
    protected static String getTable() {
        return "system_menu";
    }
    
    protected static String getName() {
        return "system_menu";
    }
    
    @Override
    public AbstractDao getFormBaseDao() {
        return getDao();
    }
    
    @Override
    protected Map<String, AbstractStateAction<SystemMenuDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<SystemMenuDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    private static AbstractDao getDao() {
        return ContextUtil.getBaseDataSource();
    }

    
    /**
     * SELECT DISTINCT
     *     a.auth_key
     * FROM
     *     system_menu m,
     *     system_menu_auth a
     * WHERE
     *     m.id = a.menu_id
     * AND
     *     m.id = ?
     */
    @Multiline
    private static final String SQL_QUERY_MENU_AUTHS = "X";
    
    /**
     * 重写获取表单详情的方法, 载如关联的授权清单
     * 
     */
    @Override
    public SystemMenuDetail getForm(long formId) throws Exception {
        List<SystemMenuDetail> list;
        if ((list = queryFormWithStateRevision(
                String.format("%s AND m.id = %s", SystemMenuListDefaultQuery.SQL_QUERY_ALL, formId))) == null
                || list.size() <= 0) {
            return null;
        }
        SystemMenuDetail menu;
        if ((menu = list.get(0)) != null) {
            List<String> auths = getFormBaseDao().queryAsList(String.class, SQL_QUERY_MENU_AUTHS,
                    new Object[] { menu.getId() });
            menu.setAuths(ClassUtil.getSingltonInstance(FieldSystemAuths.class).queryDynamicValues(auths.toArray(new String[0])));
        }
        return menu;
    }
    
    /**
     * 存储功能的授权数据
     * @param formId
     * @param auths
     * @throws Exception
     */
    private static void setAuths(long formId, List<OptionSystemAuth> auths) throws Exception {
        if (auths == null) {
            return;
        }
        DEFAULT.getFormBaseDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet arg0, Connection arg1) throws Exception {
                DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                        "system_menu_auth",
                        new ObjectMap().put("=menu_id", formId)));
                for (OptionSystemAuth auth : auths) {
                    if (auth == null || StringUtils.isBlank(auth.getOptionValue())) {
                        continue;
                    }
                    DEFAULT.getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                            "system_menu_auth",
                            new ObjectMap().put("menu_id", formId).put("=auth_key", auth.getOptionValue())));
                }
            }
        });
    }
    
    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
    }
    
    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }
    /**
     * SELECT DISTINCT
     *     m.*,
     *     p.`id`    AS `pane_id`,
     *     p.`icon`  AS `pane_icon`,
     *     p.`path`  AS `pane_path`,
     *     p.`name`  AS `pane_name`,
     *     p.`order` AS `pane_order`,
     *     d.`icon`  AS `dir_icon`,
     *     d.`path`  AS `dir_path`,
     *     d.`name`  AS `dir_name`,
     *     d.`order` AS `dir_order`,
     *     a.`auth_key`
     * FROM
     *     system_menu m,
     *     system_menu_dir d,
     *     system_menu_pane p,
     *     system_menu_auth a
     * WHERE
     *     m.dir_id = d.id
     * AND 
     *     d.pane_id = p.id
     * AND 
     *     a.menu_id = m.id
     * AND 
     *     m.state_form_status = 'enabled'
     * AND 
     *     d.state_form_status = 'enabled'
     * AND 
     *     p.state_form_status = 'enabled'
     * ORDER BY
     *     p.`order` ASC,
     *     d.`order` ASC,
     *     m.`order` ASC
     */
    @Multiline
    private static final String SQL_QUERY_MENU_TREE = "X";
    
    @Getter
    @Setter
    public static class SystemMenuAuthForm extends SystemMenuListDefaultForm {
        
        @Attributes(title = "目录编号")
        private Long dirId;
        
        @Attributes(title = "目录图标")
        private String dirIcon;
        
        @Attributes(title = "目录路径")
        private String dirPath;
        
        @Attributes(title = "面板排序")
        private Integer dirOrder;
        
        @Attributes(title = "面板编号")
        private Long paneId;
        
        @Attributes(title = "面板图标")
        private String paneIcon;
        
        @Attributes(title = "面板路径")
        private String panePath;
        
        @Attributes(title = "面板排序")
        private Integer paneOrder;
        
        @Attributes(title = "操作接口")
        private String authKey;
    }
    
    private final static Pattern MENU_OPEN_TYPE_PARSER =  Pattern.compile("(^.+)#\\((.+)\\)$");
    private static void setOpenType(SystemMenuTree tree) {
        List<SystemMenuTree> children;
        if ((children = tree.getChildren()) == null || children.size() <= 0) {
            String path;
            Matcher matched;
            if (StringUtils.isBlank(path = tree.getPath()) || (matched = MENU_OPEN_TYPE_PARSER.matcher(path)) == null
                    || !matched.find()) {
                return;
            }
            tree.setPath(matched.group(1).trim());
            tree.setOpenType(matched.group(2).trim());
            return;
        }
        for (SystemMenuTree child : children) {
            setOpenType(child);
        }
    }
    
    private static void sortChildren(SystemMenuTree tree) {
        List<SystemMenuTree> children;
        if ((children = tree.getChildren()) == null || children.size() <= 0) {
            return;
        }
        tree.getChildren().sort(new Comparator<SystemMenuTree>() {
            
            @Override
            public int compare(SystemMenuTree left, SystemMenuTree right) {
                return CommonUtil.ifNull(left.getOrder(), 0) - CommonUtil.ifNull(right.getOrder(), 0);
            }
            
        });
    }
    
    public static SystemMenuTree getMyMenuTree() throws Exception {
        if (!SessionContext.hasUserSession()) {
            return null;
        }
        Map<Long, SystemMenuTree> dirMap = new HashMap<>();
        Map<Long, SystemMenuTree> menuMap = new HashMap<>();
        Map<Long, SystemMenuTree> paneMap = new HashMap<>();
        SystemMenuTree menuTree = new SystemMenuTree().setChildren(new ArrayList<>());
        List<SystemMenuAuthForm> result = DEFAULT.getFormBaseDao().queryAsList(
                SystemMenuAuthForm.class, SQL_QUERY_MENU_TREE);
        List<String> myAuths = SessionContext.isAdmin()
                ? SystemFeatureService.getTenantAllAuths(SessionContext.getTenant())
                : PermissionService.getMyAuths();
        /* 无授权，即无菜单 */
        if (myAuths == null || myAuths.isEmpty()) {
            return null;
        }
        for (SystemMenuAuthForm item : result) {
            long menuId = item.getId();
            long dirId = item.getDirId();
            long paneId = item.getPaneId();
            SystemMenuTree paneTree;
            if ((paneTree = paneMap.get(paneId)) == null) {
                paneMap.put(paneId,
                        paneTree = new SystemMenuTree().setName(item.getPaneName()).setPath(item.getPanePath())
                                .setId(dirId).setIcon(item.getPaneIcon()).setOrder(item.getPaneOrder())
                                .setChildren(new ArrayList<>()));
                menuTree.getChildren().add(paneTree);
            }
            SystemMenuTree dirTree;
            if ((dirTree = dirMap.get(dirId)) == null) {
                dirMap.put(dirId,
                        dirTree = new SystemMenuTree().setName(item.getDirName()).setId(paneId).setParentId(paneId)
                                .setPath(item.getDirPath()).setIcon(item.getDirIcon()).setOrder(item.getDirOrder())
                                .setChildren(new ArrayList<>()));
                paneTree.getChildren().add(dirTree);
            }
            SystemMenuTree menuItem;
            if ((menuItem = menuMap.get(menuId)) == null) {
                menuMap.put(menuId,
                        menuItem = new SystemMenuTree().setName(item.getName()).setPath(item.getPath()).setId(menuId)
                                .setParentId(dirId).setIcon(item.getIcon()).setOrder(item.getOrder())
                                .setAuthKeys(new HashSet<>()));
                dirTree.getChildren().add(menuItem);
            }
            if (StringUtils.isNotBlank(item.getAuthKey())) {
                menuItem.getAuthKeys().add(item.getAuthKey());
            }
        }
        Set<String> menuAuths;
        Long[] menuIds = menuMap.keySet().toArray(new Long[0]);
        for (int m = 0; m < menuIds.length; m++) {
            if ((menuAuths = menuMap.get(menuIds[m]).getAuthKeys()) == null || menuAuths.size() <= 0
                    || !myAuths.containsAll(menuAuths)) {
                menuMap.remove(menuIds[m]);
            }
        }
        int maxMenuChildren = menuTree.getChildren().size() - 1;
        for (int i = maxMenuChildren; i >= 0; i--) {
            SystemMenuTree paneTree = menuTree.getChildren().get(i);
            int maxPaneChildren = paneTree.getChildren().size() - 1;
            for (int j = maxPaneChildren; j >= 0; j--) {
                SystemMenuTree dirTree = paneTree.getChildren().get(j);
                int maxDirChildren = dirTree.getChildren().size() - 1;
                for (int k = maxDirChildren; k >= 0; k--) {
                    SystemMenuTree menuItem = dirTree.getChildren().get(k);
                    if (!menuMap.containsKey(menuItem.getId())) {
                        dirTree.getChildren().remove(k);
                    }
                }
                if (dirTree.getChildren().size() < 1) {
                    paneTree.getChildren().remove(j);
                }
                /* 当菜单目录上配置了地址时，移出其下的所有子菜单,即意味着为两级结构，任意子菜单的授权将被视为该目录的访问授权 */
                if (StringUtils.isNotBlank(dirTree.getPath()) && dirTree.getPath().trim().length() > 3) {
                    dirTree.getChildren().clear();
                }
                setOpenType(dirTree);
                sortChildren(dirTree);
            }
            if (paneTree.getChildren().size() < 1) {
                menuTree.getChildren().remove(i);
            }
            sortChildren(paneTree);
        }
        sortChildren(menuTree);
        return menuTree;
    }
}
