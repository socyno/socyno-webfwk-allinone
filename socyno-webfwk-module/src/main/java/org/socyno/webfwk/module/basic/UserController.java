package org.socyno.webfwk.module.basic;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socyno.webfwk.state.module.menu.SystemMenuItemService;
import org.socyno.webfwk.state.module.token.UserTokenService;
import org.socyno.webfwk.state.module.user.SystemUserFormLogin;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.module.user.SystemUserToken;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.remote.R;
import org.socyno.webfwk.util.tool.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class UserController {
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public R login(@RequestBody SystemUserFormLogin loginInfo, HttpServletResponse resp) throws Exception {
        SystemUserToken userToken;
        if ((userToken = SystemUserService.DEFAULT.login(loginInfo)) == null) {
            throw new MessageException("用户或密码错误");
        }
        for (Cookie cookie : userToken.getCookies()) {
            resp.addCookie(cookie);
        }
        return R.ok().setData(userToken);
    }
    
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public R logout(HttpServletRequest request) throws Exception {
        String token;
        if (StringUtils.isNotBlank(token = request.getHeader(UserTokenService.getTokenHeader()))) {
            UserTokenService.markTokenDiscard(token);
        }
        return R.ok();
    }
    
    @RequestMapping(value = "/menus", method = RequestMethod.GET)
    public R getMenus(HttpServletRequest request) throws Exception {
        if (!SessionContext.hasUserSession()) {
            return R.ok();
        }
        return R.ok().setData(SystemMenuItemService.getInstance().getMyMenuTree());
    }
}
