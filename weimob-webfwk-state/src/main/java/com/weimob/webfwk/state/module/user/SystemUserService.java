package com.weimob.webfwk.state.module.user;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import com.weimob.webfwk.state.abs.AbstractStateAction;
import com.weimob.webfwk.state.abs.AbstractStateCreateAction;
import com.weimob.webfwk.state.abs.AbstractStateFormServiceWithBaseDao;
import com.weimob.webfwk.state.annotation.Authority;
import com.weimob.webfwk.state.authority.AuthorityScopeType;
import com.weimob.webfwk.state.authority.AuthoritySpecialChecker;
import com.weimob.webfwk.state.authority.AuthoritySpecialRejecter;
import com.weimob.webfwk.state.field.FieldSystemUserAuth;
import com.weimob.webfwk.state.field.OptionSystemUserAuth;
import com.weimob.webfwk.state.module.role.SystemRoleFormSimple;
import com.weimob.webfwk.state.module.role.SystemRoleService;
import com.weimob.webfwk.state.module.tenant.SystemTenantBasicService;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.state.module.token.UserTokenService;
import com.weimob.webfwk.state.module.user.WindowsAdService.SystemWindowsAdUser;
import com.weimob.webfwk.state.sugger.DefaultStateFormSugger;
import com.weimob.webfwk.state.util.*;
import com.weimob.webfwk.util.context.ContextUtil;
import com.weimob.webfwk.util.context.LoginTokenUtil;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.context.UserContext;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.exception.NamingFormatInvalidException;
import com.weimob.webfwk.util.exception.TenantMissingException;
import com.weimob.webfwk.util.model.AbstractUser;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.model.PagedList;
import com.weimob.webfwk.util.sql.AbstractDao;
import com.weimob.webfwk.util.sql.SqlQueryUtil;
import com.weimob.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import com.weimob.webfwk.util.tool.ClassUtil;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.ConvertUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SystemUserService extends
        AbstractStateFormServiceWithBaseDao<SystemUserFormDetail, SystemUserFormDefault, SystemUserFormSimple> {
    
    public class CurrentUserIsMeChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object originForm) {
            return originForm != null && SessionContext.getUserId() != null 
                    && SessionContext.getUserId().equals(((AbstractUser)originForm).getId());
        }
    }
    
    public class CreateDomainUserRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !SystemTenantBasicService.inSuperTenantContext()
                    && !WindowsAdService.equalsDefaultDomain(SessionContext.getTenant());
        }
    }
    
    public class ChangePasswordRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return originForm == null ||
                    !localPasswordEnabled(((AbstractUser)originForm).getId());
        }
    }
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
        DISABLED ("disabled", "禁用"),
        ENABLED  (AbstractUser.ENABLED,  "有效")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemUserFormDetail, SystemUserFormCreation> {
        
        public EventCreate() {
            super("添加常规账户", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemUserFormDetail form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemUserFormDetail originForm, SystemUserFormCreation form, String message) throws Exception {
            String nameNoTenant = "";
            String nameSuffix = String.format("@%s", SessionContext.getTenant());
            if ((nameNoTenant = StringUtils.removeEnd(form.getUsername(), nameSuffix)).equals(form.getUsername())) {
                throw new NamingFormatInvalidException(String.format("用户的账户名称不规范，必须以 %s 结尾", nameSuffix));
            }
            AbstractUser.ensuerNameFormatValid(nameNoTenant);
            String password = form.getNewPassword();
            String confirmPassword = form.getConfirmPassword();
            if (StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)
                    || !StringUtils.equals(password, confirmPassword)) {
                    throw new MessageException("两次输入的新密码不一致!");
            }
            if (getFormBaseDao().queryAsMap(
                    String.format("SELECT * FROM %s WHERE username = ?", getFormTable()),
                    new Object[] { form.getUsername() }) != null) {
                throw new MessageException("用户的账户名已被占用!");
            }
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                        .put("username",        form.getUsername())
                        .put("mail_address",    form.getMailAddress())
                        .put("display",         form.getDisplay())
                        .put("password",        passwordEncode(password))
                        .put("title",           StringUtils.trimToEmpty(form.getTitle()))
                        .put("department",      StringUtils.trimToEmpty(form.getDepartment()))
                        .put("mobile",          StringUtils.trimToEmpty(form.getMobile()))
                        .put("telphone",        StringUtils.trimToEmpty(form.getTelphone()))
            ), new ResultSetProcessor () {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    r.next();
                    id.set(r.getLong(1));
                }
            });
            return new StateFormEventResultCreateViewBasic(id.get());
        }
    }
    
    public class EventCreateDomain
            extends AbstractStateCreateAction<SystemUserFormDetail, SystemUserFormCreationDomain> {
        
        public EventCreateDomain() {
            super("添加域(Windows)用户", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, rejecter = CreateDomainUserRejecter.class)
        public void check(String event, SystemUserFormDetail form, String sourceState) {
            
        }
        
        @Override
        public StateFormEventResultCreateViewBasic handle(String event, SystemUserFormDetail originForm,
                SystemUserFormCreationDomain form, String message) throws Exception {
            SystemWindowsAdUser windowsAdUser;
            if ((windowsAdUser = WindowsAdService.getAdUser(form.getUsername())) == null) {
                throw new MessageException(String.format("用户（%s）的未在域中注册", form.getUsername()));
            }
            if (getSimple(String.format("%s@%s", windowsAdUser.getLogin(), SessionContext.getTenant())) != null) {
                throw new MessageException(String.format("用户(%s)已存在", windowsAdUser.getLogin()));
            }
            return new StateFormEventResultCreateViewBasic(
                    ensureAdUserExisted(windowsAdUser, SessionContext.getTenant()).getId());
        }
    }
    
    public class EventUpdate extends AbstractStateAction<SystemUserFormDetail, SystemUserFormEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return false;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = CurrentUserIsMeChecker.class)
        public void check(String event, SystemUserFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemUserFormDetail originForm, final SystemUserFormEdition form, final String message) throws Exception {
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet result, Connection conn) throws Exception {
                    ObjectMap changed = new ObjectMap()
                            .put("=id", form.getId())
                            .put("display", form.getDisplay())
                            .put("mail_address", form.getMailAddress())
                            .put("title", StringUtils.trimToEmpty(form.getTitle()))
                            .put("department", StringUtils.trimToEmpty(form.getDepartment()))
                            .put("mobile", StringUtils.trimToEmpty(form.getMobile()))
                            .put("telphone", StringUtils.trimToEmpty(form.getTelphone()))
                            .put("manager", form.getManager() == null ? null : form.getManager().getId());
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(), changed));
                    /**
                     * 当存在授权信息时，首先清理当前授权条目，再添新的条目
                     */
                    List<OptionSystemUserAuth> auths;
                    if ((auths = form.getAuths()) != null) {
                        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                                "system_user_scope_role",
                                new ObjectMap().put("=user_id", form.getId())));
                        for (OptionSystemUserAuth auth : auths) {
                            if (auth == null || StringUtils.isBlank(auth.getScopeType()) || auth.getRoleId() == null) {
                                log.warn("无效的用户授权记录，无法保存，丢弃处理 : {}", auth);
                                continue;
                            }
                            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                                    "system_user_scope_role", new ObjectMap()
                                        .put("user_id",     form.getId())
                                        .put("scope_type",  auth.getScopeType())
                                        .put("scope_id",    auth.getScopeId())
                                        .put("=role_id",    auth.getRoleId())
                            ));
                        }
                    }
                }
            });
            return null;
        }
        
    }

    public class EventChangePassword extends AbstractStateAction<SystemUserFormDetail, SystemUserFormNewPassword, Void> {
        
        public EventChangePassword() {
            super("修改密码", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return false;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = CurrentUserIsMeChecker.class, rejecter = ChangePasswordRejecter.class)
        public void check(String event, SystemUserFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemUserFormDetail originForm, SystemUserFormNewPassword form, String message)
                            throws Exception {
            /* 确认密码非空，且输入确认正确 */
            String newPassword = ((SystemUserFormNewPassword)form).getNewPassword();
            if (StringUtils.isBlank(newPassword)
                    || !StringUtils.equals(newPassword, ((SystemUserFormNewPassword)form).getConfirmPassword())) {
                throw new MessageException("两次输入的新密码不一致!");
            }
            
            /* 当修改自己的密码时，需要验证原密码 */
            if (form.getId().equals(SessionContext.getUserId())) {
                if (!checkLocalPassword(SessionContext.getUserId(), ((SystemUserFormNewPassword)form).getPassword())) {
                    throw new MessageException("输入密码不正确，或者非本地用户!");
                }
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                        .put("=id", form.getId())
                        .put("password", passwordEncode(newPassword))
            ));
            return null;
        }
        
    }
    
    public class EventMarkDisabled extends AbstractStateAction<SystemUserFormDetail, StateFormBasicInput, Void> {
        
        public EventMarkDisabled() {
            super("禁用", STATES.ENABLED.getCode(), STATES.DISABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemUserFormDetail form, String sourceState) {
            
        }
        
    }
    
    public class EventMarkEnabled extends AbstractStateAction<SystemUserFormDetail, StateFormBasicInput, Void> {
        
        public EventMarkEnabled() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemUserFormDetail form, String sourceState) {
            
        }
        
    }

    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        /**
         * 创建用户
         */
        Submit(EventCreate.class),
        
        /**
         * 创建用户
         */
        SubmitDomain(EventCreateDomain.class),
        
        /**
         * 更新用户信息
         */
        Update(EventUpdate.class),
        
        /**
         * 修改用户密码
         */
        Password(EventChangePassword.class),
        
        /**
         * 禁用指定用户
         */
        Disable(EventMarkDisabled.class),
        
        /**
         * 恢复对用户的禁用
         */
        Enable(EventMarkEnabled.class)
        
        ;
        
        private final Class<? extends AbstractStateAction<SystemUserFormDetail, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemUserFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemUserFormDefault>("默认查询", 
                SystemUserFormDefault.class, SystemUserQueryDefault.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    /**
     * 在初始化对象时，事件(EVENTS)的初始化过程中会使用到状态（STATES）列表，
     * 
     * 因此务必确保先设置状态再注册事件。
     */
    private SystemUserService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    private static final SystemUserService Instance = new SystemUserService();
    
    @Override
    public String getFormName() {
        return "system_user";
    }
    
    @Override
    public String getFormTable() {
        return "system_user";
    }
    
    @Override
    public String getFormDisplay() {
        return "系统用户";
    }
    
    @Override
    public AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    /**
     * 获取用户本地密码
     */
    private String getLocalPassword(long userId) throws Exception {
        return getFormBaseDao().queryAsObject(String.class, String.format(
                "SELECT password FROM %s WHERE %s = ?",
                getFormTable(), getFormIdField()
            ), new Object[] {userId});
    }
    
    /**
     * 是否允许本地登录
     */
    public boolean localPasswordEnabled(long userId) throws Exception {
        return !StringUtils.isBlank(getLocalPassword(userId));
    }
    
    private String passwordEncode(String password, boolean withoutSalt) {
        String encoded = DigestUtils.sha256Hex(password);
        return withoutSalt ? encoded : DigestUtils.sha256Hex(String.format("ucasdf$%s&*w", encoded));
    }
    
    private String passwordEncode(String password) {
        return passwordEncode(password,
                !ContextUtil.getConfigTrimed("system.user.password.encrypted.withsalt").equals("yes"));
    }
    
    /**
     * 验证登录密码
     * @param userId
     * @param password
     * @return
     * @throws Exception
     */
    public boolean checkLocalPassword(long userId, String password) throws Exception {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        String localPassword;
        if (StringUtils.isBlank(localPassword = getLocalPassword(userId))) {
            return false;
        }
        return localPassword.equals(passwordEncode(password, false))
                || localPassword.equals(passwordEncode(password, true));
    }
    
    /**
     * 检索用户清单（支持模糊检索, 单屏蔽密码及手机号等信息）。
     * @param nameLike 检索的关键字
     * @param disableIncluded 是否包括已禁用的用户
     */
    public PagedList<SystemUserFormSimple> queryByNameLike(String nameLike, boolean disableIncluded, long page, int limit)
            throws Exception {
        if (StringUtils.isBlank(nameLike)) {
            return null;
        }
        return listForm(SystemUserFormSimple.class,
                new SystemUserQueryDefault(page, limit).setNameLike(nameLike).setDisableIncluded(disableIncluded));
    }
    
    /**
     SELECT DISTINCT
        f.*
     FROM
        %s f
     WHERE
        f.id IN (%s)
     ORDER BY f.id DESC
     */
    @Multiline
    private static final String SQL_QUERY_IDS_USERS = "X";
    
    /**
     * 检索用户详情。
     */
    public <T extends SystemUserFormSimple> List<T> queryByUserIds(@NonNull Class<T> clazz, final Long... ids)
            throws Exception {
        if (ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemUserQueryDefault(1, ids.length)
                    .setIdsIn(StringUtils.join(ids, ','))
                    .setDisableIncluded(true)).getList();
    }
    
    /**
     SELECT DISTINCT
        f.*
     FROM
        %s f
     WHERE
        f.username IN (%s)
     ORDER BY f.id DESC
     */
    @Multiline
    private static final String SQL_QUERY_USERNAMES_USERS = "X";
    
    /**
     * 检索用户详情。
     */
    public <T extends SystemUserFormSimple> List<T> queryByUsernames(@NonNull Class<T> clazz, final String... usernames)
            throws Exception {
        if (usernames == null || usernames.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemUserQueryDefault(1, usernames.length)
                .setNamesIn(StringUtils.join(usernames, ','))
                .setDisableIncluded(true)).getList();
    }

    public void forceSuToUser(String username) throws Exception {
        forceSuToUser(username, null);
    }
    
    public AbstractUser forceCreateUser(SystemWindowsAdUser winadUser) throws Exception {
        return ensureAdUserExisted(winadUser, SessionContext.getTenant());
    }
    
    /**
     * 切换到指定用户上下文，返回当前的上下文对象。
     */
    public void forceSuToUser(String username, String proxiedUsername) throws Exception {
        String tenant = null;
        String ssoTicket = null;
        AbstractUser sysuser = null;
        try {
            /**
             * 设置租户的上下文，确保在正确的租户数据库中检索用户信息
             */
            tenant = AbstractUser.parseTenantFromUsername(username);
            SessionContext.setUserContext(new UserContext().setTenant(tenant));
            sysuser = getSimple(username);
        } catch (TenantMissingException e) {
            /**
             * 否则，将用户(username)视为 weimob sso ticket，
             * 同时将代理用户(proxiedUsername)视为 sso service
             */
            SystemWindowsAdUser winadUser;
            String ssoService = proxiedUsername;
            proxiedUsername = null;
            if ((winadUser = WeimobSsoService.validateTicket(username, ssoService)) != null) {
                ssoTicket = username;
                username = winadUser.getMail();
                tenant = AbstractUser.parseTenantFromUsername(username);
                SessionContext.setUserContext(new UserContext().setTenant(tenant));
                sysuser = ensureAdUserExisted(winadUser, tenant);
            }
        }
        /**
         * 验证用户是否有效
         */
        if (sysuser == null || sysuser.isDisabled()) {
            log.info("账户（{}）不存在，或者已被禁用", username);
            throw new MessageException(String.format("No such user found or disabled : %s", username));
        }
        /**
         * 验证租户是否被警用
         */
        if (!SystemTenantBasicService.checkTenantEnabled(tenant)) {
            log.info("租户（{}）不存在，或者已被禁用", tenant);
            throw new MessageException("账户或密码信息错误");
        }
        /**
         * 代理人模式校验
         */
        AbstractUser proxiedUser = null;
        boolean isAdmin = isAdmin(sysuser.getId());
        boolean isSuperTeant = SystemTenantBasicService.inSuperTenantContext();
        if (StringUtils.isNotBlank(proxiedUsername)) {
            if (!isAdmin) {
                throw new MessageException("未获得被代理用户的授权");
            }
            String proxiedTenant = AbstractUser.parseTenantFromUsername(proxiedUsername);
            if (!isSuperTeant && !StringUtils.equals(proxiedTenant, sysuser.getTenant())) {
                throw new MessageException("未获得被代理用户的授权");
            }
            /* 系统登录前必须先设置为别代理用户的租户上下文，否则无法访问到对应的租户数据库 */
            SessionContext.setUserContext(new UserContext().setTenant(proxiedTenant));
            /* 确认被代理租户可用 */
            if (!SystemTenantBasicService.checkTenantEnabled(SessionContext.getTenant())) {
                log.info("被代理租户（{}）不存在，或者已被禁用", SessionContext.getTenant());
                throw new MessageException("未获得被代理用户的授权");
            }
            /* 确认被代理用户可用 */
            if ((proxiedUser = getSimple(proxiedUsername)) == null || proxiedUser.isDisabled()) {
                log.info("被代理用户（{}）不存在，或者已被禁用", SessionContext.getTenant());
                throw new MessageException("未获得被代理用户的授权");
            }
        }
        /**
         * 确保拥有基础角色
         */
        SystemRoleFormSimple basicRole;
        if ((basicRole = SystemRoleService.getInstance()
                .getSimple(SystemRoleService.InternalRoles.Basic.getCode())) != null) {
            getFormBaseDao()
                    .executeUpdate(
                            SqlQueryUtil
                                    .prepareInsertQuery("system_user_scope_role",
                                            new ObjectMap()
                                                    .put("user_id",
                                                            proxiedUser == null ? sysuser.getId()
                                                                    : proxiedUser.getId())
                                                    .put("scope_type", "System")
                                                    .put("role_id", basicRole.getId()).put("=scope_id", 0)));
        }
        final SystemUserToken userToken = new SystemUserToken();
        if (proxiedUser != null) {
            userToken.setTokenHeader(UserTokenService.getTokenHeader())
                    .setTokenContent(LoginTokenUtil.generateToken(proxiedUser,
                            isAdmin(proxiedUser.getId()), sysuser,
                            new ObjectMap().put("isSuperTenant",
                                    SystemTenantBasicService.equalsSuperTenant(proxiedUser.getTenant()))));
        } else {
            userToken.setTokenHeader(UserTokenService.getTokenHeader()).setTokenContent(LoginTokenUtil
                    .generateToken(sysuser, isAdmin, null, new ObjectMap().put("isSuperTenant", isSuperTeant)));
        }
        SessionContext.setUserContext(
                new UserContext()
                    .setSysUser(sysuser)
                    .setSsoTicket(ssoTicket)
                    .setTokenHead(userToken.getTokenHeader())
                    .setToken(userToken.getTokenContent())
        );
        return;
    }
    
    /**
     * 用户认证
     */
    public SystemUserToken login(SystemUserFormLogin form) throws Exception {
        UserContext currentContext = SessionContext.getUserContext();
        try {
            boolean success = false;
            /**
             * 携带 weimob sso ticket 进行系统登陆
             */
            if (form != null && StringUtils.isNotBlank(form.getTicket())) {
                forceSuToUser(form.getTicket(), form.getService());
                success = true;
            }
            /**
             * 账户密码方式登陆(LDAP 或本地账密)
             */
            String tenant = null;
            if (!success && StringUtils.isNotBlank(form.getUsername()) && StringUtils.isNotBlank(form.getPassword())) {
                try {
                    tenant = AbstractUser.parseTenantFromUsername(form.getUsername());
                } catch (TenantMissingException e) {
                    tenant = WindowsAdService.getDefaultDomain();
                    form.setUsername(String.format("%s%s", form.getUsername(), WindowsAdService.getDefaultDomainSuffix()));
                }
                forceSuToUser(form.getUsername(), form.getProxied());
                AbstractUser sysuser = SessionContext.getUserContext().getSysUser();
                /* 验证用户的密码 */
                if (checkLocalPassword(sysuser.getId(), form.getPassword())) {
                    success = true;
                }
                SystemWindowsAdUser winadUser = null;
                if (!success && WindowsAdService.equalsDefaultDomain(sysuser.getTenant())
                        && (winadUser = WindowsAdService.verifyAdUser(form.getUsername(), form.getPassword())) != null) {
                    success = true;
                }
                if (!success && SystemTenantBasicService.equalsSuperTenant(sysuser.getTenant())
                        && (winadUser = WindowsAdService.verifyAdUser(
                                form.getUsername().replace("@" + SystemTenantBasicService.getSuperTenant(), ""),
                                form.getPassword())) != null) {
                    success = true;
                }
                if (winadUser != null) {
                    ensureAdUserExisted(winadUser, tenant);
                }
            }
            if (success) {
                return new SystemUserToken()
                            .setTokenHeader(SessionContext.getTokenHead())
                            .setTokenContent(SessionContext.getToken());
            }
        } finally {
            SessionContext.setUserContext(currentContext);
        }
        throw new MessageException("账户或密码信息错误");
    }
    
    /**
     * SELECT
     *     COUNT( 1 ) 
     * FROM
     *     system_user_scope_role s,
     *     system_role r 
     * WHERE
     *     r.id = s.role_id 
     * AND 
     *     s.user_id = ?
     * AND
     *     r.code = ?
     * AND
     *     s.scope_type = 'System'
     */
    @Multiline
    private static final String SQL_QUERY_CHECK_USER_ADMIN = "X";
    
    /**
     * 判断用户是否是管理员
     * 
     */
    public boolean isAdmin(Long userId) throws Exception {
        if (userId == null) {
            return false;
        }
        if (Long.valueOf(1).equals(userId)) {
            return true;
        }
        return getFormBaseDao().queryAsObject(Long.class, SQL_QUERY_CHECK_USER_ADMIN,
                new Object[] { userId, SystemRoleService.InternalRoles.Admin.getCode()}) > 0;
    }
    
    public SystemUserFormSimple getSimple(Object idOrUsername) throws Exception {
        if (idOrUsername == null || StringUtils.isBlank(idOrUsername.toString())) {
            return null;
        }
        List<SystemUserFormSimple> result;
        if (idOrUsername.toString().matches("^\\d+$")) {
            result = queryByUserIds(SystemUserFormSimple.class, CommonUtil.parseLong(idOrUsername));
        } else {
            result = queryByUsernames(SystemUserFormSimple.class, idOrUsername.toString());
        }
        if (result == null || result.size() != 1) {
            return null;
        }
        return result.get(0);
    }
    
    public List<SystemUserFormWithSecurity> getUsersSecurity(Long... userIds) throws Exception {
        if (userIds == null || userIds.length <= 0
                || (userIds = ConvertUtil.asNonNullUniqueLongArray((Object[]) userIds)).length <= 0) {
            return Collections.emptyList();
        }
        return listForm(SystemUserFormWithSecurity.class,
                    new SystemUserQueryDefault(1, userIds.length)
                    .setIdsIn(StringUtils.join(userIds, ','))).getList();
    }

    public List<SystemUserFormWithSecurity> getUsersSecurity(Object[] userIds) throws Exception {
        if (userIds == null || userIds.length <= 0) {
            return Collections.emptyList();
        }
        return getUsersSecurity(ConvertUtil.asNonNullUniqueLongArray(userIds));
    }
    
    public List<SystemUserFormWithSecurity> getUsersSecurity(Collection<?> userIds) throws Exception {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getUsersSecurity(ConvertUtil.asNonNullUniqueLongArray(userIds.toArray()));
    }
    
    protected AbstractUser ensureAdUserExisted(SystemWindowsAdUser windowsAdUser, String tenant) throws Exception {
        String username = String.format(String.format("%s@%s", windowsAdUser.getLogin(), tenant));
        ObjectMap queryData = new ObjectMap().put("username", username)
                                                .put("=display", windowsAdUser.getName())
                                                .put("=password", "");
        AbstractUser manager = null;
        if (windowsAdUser.getManager() != null) {
            manager = ensureAdUserExisted(windowsAdUser.getManager(), tenant);
            queryData.put("=manager", manager.getId());
        }
        String title = StringUtils.trimToEmpty(windowsAdUser.getTitle());
        String mobile = StringUtils.trimToEmpty(windowsAdUser.getMobile());
        String mailAddress = StringUtils.trimToEmpty(windowsAdUser.getMail());
        String telphone = StringUtils.trimToEmpty(windowsAdUser.getTelphone());
        String department = StringUtils.trimToEmpty(windowsAdUser.getDepartment());
        queryData.put(title.isEmpty() ? "title" : "=title", title);
        queryData.put(mobile.isEmpty() ? "mobile" : "=mobile", mobile);
        queryData.put(department.isEmpty() ? "department" : "=department", department);
        queryData.put(telphone.isEmpty() ? "telphone" : "=telphone", telphone);
        queryData.put(mailAddress.isEmpty() ? "mail_address" : "=mail_address", mailAddress);
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(getFormTable(), queryData));
        return getSimple(username);
    }
    
    @Override
    protected Map<String, String> getExtraFieldMapper(Class<?> resultClazz, Map<String, String> queryMapper) {
        Map<String, String> finalMapper = new HashMap<>();
        if (queryMapper != null) {
            finalMapper.putAll(queryMapper);
        }
        finalMapper.put("-password", "****");
        if (!SystemUserWithSecurities.class.isAssignableFrom(resultClazz)) {
            finalMapper.put("-mobile", "****");
            finalMapper.put("-telphone", "****");
        }
        return finalMapper;
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemUserFormSimple> forms) throws Exception {
        
        if (forms == null || forms.size() <= 0) {
            return;
        }
        
        DefaultStateFormSugger.getInstance().apply(forms);
        
        Map<Long, List<SystemUserWithAuths>> mappedWithAuths = new HashMap<>();
        for (SystemUserFormSimple user : forms) {
            if (user == null || user.getId() == null) {
                continue;
            }
            List<SystemUserWithAuths> singleWithAuths;
            if (SystemUserWithAuths.class.isAssignableFrom(user.getClass())) {
                if ((singleWithAuths = mappedWithAuths.get(user.getId())) == null) {
                    mappedWithAuths.put(user.getId(), singleWithAuths = new ArrayList<>());
                }
                singleWithAuths.add((SystemUserWithAuths) user);
            }
        }
        
        if (mappedWithAuths.size() > 0) {
            List<OptionSystemUserAuth> optionAuths = ClassUtil.getSingltonInstance(FieldSystemUserAuth.class)
                    .queryDynamicValues(mappedWithAuths.keySet().toArray());
            Map<Long, List<OptionSystemUserAuth>> allOptionAuths = new HashMap<>();
            List<OptionSystemUserAuth> userOptionAuths;
            for (OptionSystemUserAuth option : optionAuths) {
                if ((userOptionAuths = allOptionAuths.get(option.getUserId())) == null) {
                    allOptionAuths.put(option.getUserId(), userOptionAuths = new ArrayList<>());
                }
                userOptionAuths.add(option);
            }
            for (Map.Entry<Long, List<SystemUserWithAuths>> entry : mappedWithAuths.entrySet()) {
                Long userId = entry.getKey();
                for (SystemUserWithAuths with : entry.getValue()) {
                    with.setAuths(allOptionAuths.get(userId));
                }
            }
        }
    }
}
