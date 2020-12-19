package org.socyno.webfwk.state.module.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.abs.AbstractStateAction;
import org.socyno.webfwk.state.abs.AbstractStateCreateAction;
import org.socyno.webfwk.state.abs.AbstractStateDeleteAction;
import org.socyno.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.field.FieldSystemAuths;
import org.socyno.webfwk.state.field.OptionSystemAuth;
import org.socyno.webfwk.state.module.feature.SystemFeatureService;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.state.util.StateFormBasicInput;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormEventResultCreateViewBasic;
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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
public class SystemMenuItemService extends
    AbstractStateFormServiceWithBaseDao<SystemMenuItemFormDetail, SystemMenuItemFormDefault, SystemMenuItemFormSimple> {
    
    private SystemMenuItemService () {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final SystemMenuItemService Instance = new SystemMenuItemService();
    
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
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemMenuItemFormDefault>(
                "默认查询", 
                SystemMenuItemFormDefault.class, SystemMenuItemQueryDefault.class
            ))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemMenuItemFormSimple, SystemMenuItemFormCreation> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuItemFormSimple form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemMenuItemFormSimple originForm,
                SystemMenuItemFormCreation form, String message) throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    getFormBaseDao().executeUpdate(
                            SqlQueryUtil.prepareInsertQuery(getFormTable(),
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
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventUpdate extends AbstractStateAction<SystemMenuItemFormSimple, SystemMenuItemFormEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuItemFormSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuItemFormSimple originForm, final SystemMenuItemFormEdition form,
                final String message) throws Exception {
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet result, Connection conn) throws Exception {
                    List<OptionSystemAuth> auths;
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                        getFormTable(),  new ObjectMap()
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
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemMenuItemFormSimple> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuItemFormSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuItemFormSimple originForm, final StateFormBasicInput form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    getFormTable(),
                    new ObjectMap().put("=id", originForm.getId())));
            setAuths(originForm.getId(), Collections.emptyList());
            return null;
        }
    }
    
    @Getter
    public static enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        Update(EventUpdate.class),
        Delete(EventDelete.class);
        
        private final Class<? extends AbstractStateAction<SystemMenuItemFormSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemMenuItemFormSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Override
    public String getFormName() {
        return "system_menu";
    }
    
    @Override
    protected String getFormTable() {
        return "system_menu";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统菜单";
    }
    
    @Override
    public AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    /**
     * 存储功能的授权数据
     * @param formId
     * @param auths
     * @throws Exception
     */
    private void setAuths(long formId, List<OptionSystemAuth> auths) throws Exception {
        if (auths == null) {
            return;
        }
        getFormBaseDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet arg0, Connection arg1) throws Exception {
                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                        "system_menu_auth",
                        new ObjectMap().put("=menu_id", formId)));
                for (OptionSystemAuth auth : auths) {
                    if (auth == null || StringUtils.isBlank(auth.getOptionValue())) {
                        continue;
                    }
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                            "system_menu_auth",
                            new ObjectMap().put("menu_id", formId).put("=auth_key", auth.getOptionValue())));
                }
            }
        });
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
    public static class SystemMenuAuthForm extends SystemMenuItemFormDefault {
        
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
    
    public SystemMenuTree getMyMenuTree() throws Exception {
        if (!SessionContext.hasUserSession()) {
            return null;
        }
        Map<Long, SystemMenuTree> dirMap = new HashMap<>();
        Map<Long, SystemMenuTree> menuMap = new HashMap<>();
        Map<Long, SystemMenuTree> paneMap = new HashMap<>();
        SystemMenuTree menuTree = new SystemMenuTree().setChildren(new ArrayList<>());
        List<SystemMenuAuthForm> result = getFormBaseDao().queryAsList(
                SystemMenuAuthForm.class, SQL_QUERY_MENU_TREE);
        List<String> myAuths = SessionContext.isAdmin()
                ? SystemFeatureService.getInstance().getTenantAllAuths(SessionContext.getTenant())
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
    
    /**
     * SELECT DISTINCT
     *     a.menu_id,
     *     a.auth_key
     * FROM
     *     system_menu_auth a
     * WHERE
     *     a.menu_id IN (%s)
     */
    @Multiline
    private static final String SQL_QUERY_MENU_AUTHS = "X";
    
    @Override
    protected String loadFormSqlTmpl() {
        return SystemMenuItemQueryDefault.SQL_QUERY_ALL.concat(" AND m.#(formIdField)=#(formIdValue)");
    }
    
    @Getter
    @Setter
    @ToString
    public static class MenuAuthKey {
        
        private long menuId;
        
        private String authKey;
        
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemMenuItemFormSimple> forms) throws Exception {
        if (forms == null || forms.size() <= 0) {
            return;
        }
        List<SystemMenuItemFormWithAuths> sameAuthForms;
        Map<Long, List<SystemMenuItemFormWithAuths>> mappedAuthForms = new HashMap<>();
        for (SystemMenuItemFormSimple form : forms) {
            if (form != null && form.getId() != null
                    && SystemMenuItemFormWithAuths.class.isAssignableFrom(form.getClass())) {
                if ((sameAuthForms = mappedAuthForms.get(form.getId())) == null) {
                    mappedAuthForms.put(form.getId(), sameAuthForms = new ArrayList<>());
                }
                sameAuthForms.add((SystemMenuItemFormWithAuths) form);
            }
        }
        if (mappedAuthForms.size() > 0) {
            List<MenuAuthKey> authKeys = getFormBaseDao().queryAsList(MenuAuthKey.class,
                    String.format(SQL_QUERY_MENU_AUTHS, CommonUtil.join("?", mappedAuthForms.size(), ",")),
                    mappedAuthForms.keySet().toArray());
            Set<String> oneMenuKeys;
            Set<String> allKeys = new HashSet<>();
            Map<Long, Set<String>> allMenuKeys = new HashMap<>();
            for (MenuAuthKey a : authKeys) {
                allKeys.add(a.getAuthKey());
                if ((oneMenuKeys = allMenuKeys.get(a.getMenuId())) == null) {
                    allMenuKeys.put(a.getMenuId(), oneMenuKeys = new HashSet<>());
                }
                oneMenuKeys.add(a.getAuthKey());
            }
            Map<String, OptionSystemAuth> mappedSystemAuths = new HashMap<>();
            List<OptionSystemAuth> allSystemAuths = ClassUtil.getSingltonInstance(FieldSystemAuths.class)
                    .queryDynamicValues(allKeys.toArray(new String[0]));
            for (OptionSystemAuth a : allSystemAuths) {
                mappedSystemAuths.put(a.getOptionValue(), a);
            }
            List<OptionSystemAuth> oneSystemAuths;
            Map<Long, List<OptionSystemAuth>> menuSystemAuths = new HashMap<>();
            for (Map.Entry<Long, Set<String>> e : allMenuKeys.entrySet()) {
                oneSystemAuths = new ArrayList<>();
                for (String authKey : e.getValue()) {
                    oneSystemAuths.add(mappedSystemAuths.get(authKey));
                }
                menuSystemAuths.put(e.getKey(), oneSystemAuths);
            }
            for (Map.Entry<Long, List<SystemMenuItemFormWithAuths>> e : mappedAuthForms.entrySet()) {
                for (SystemMenuItemFormWithAuths form : e.getValue()) {
                    form.setAuths(menuSystemAuths.get(e.getKey()));
                }
            }
        }
    }
}
