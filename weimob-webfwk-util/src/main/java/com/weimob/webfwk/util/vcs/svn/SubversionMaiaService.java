package com.weimob.webfwk.util.vcs.svn;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.remote.R;
import com.weimob.webfwk.util.remote.RestClient;
import com.weimob.webfwk.util.tool.CommonUtil;

/**
 * 基于 Svn Maia 的接口，多数功能均为二次开发
 *
 */
public class SubversionMaiaService {

    private final String apiUrl;
    
    private final String cookies;
    
    private final RestClient client;
    
    protected static class SubversionMaiaGroupPermissions extends ArrayList<SubversionMaiaGroupPermission> {
        private static final long serialVersionUID = 1L;
        
    }
    
    public SubversionMaiaService(String url, String user, String password) throws Exception {
        CloseableHttpResponse res = null;
        try {
            apiUrl = url;
            StringBuffer cookieContent = new StringBuffer();
            res = HttpUtil.post(HttpUtil.concatUrlPath(url, "user/login.php"),
                    new ObjectMap().put("username", user).put("pswd", password).asMap());
            for (Header sc : res.getHeaders("Set-Cookie")) {
                for (HttpCookie ck : HttpCookie.parse(sc.getValue())) {
                    cookieContent.append(ck.getName()).append("=").append(ck.getValue()).append(";");
                }
            }
            cookies = cookieContent.toString();
            client = new RestClient(url);
            postMaia(R.class, new ObjectMap().put("action", "SvnAdminEnsure"));
        } finally {
            HttpUtil.close(res);
        }
    }
    
    private <T> T postMaia(Class<T> clazz, ObjectMap args) throws IOException {
        RestClient.QueryMapData data = null;
        if (args != null) {
            data = new RestClient.QueryMapData().putAll(args.asMap());
        }
        return client.post(clazz, "weimob-change.php", data, null, new ObjectMap().put("Cookie", cookies).asMap());
    }
    
    public void addGroupAccess(String repo, String path, String groupName, boolean writable) throws IOException {
        postMaia(R.class, new ObjectMap().put("action", "AddGroupAccess").put("repo", repo).put("path", path)
                .put("group", groupName).put("writable", writable));
    }
    
    public void delGroupAccess(String repo, String path, String groupId) throws IOException {
        postMaia(R.class, new ObjectMap().put("action", "DelGroupAccess").put("repo", repo).put("path", path)
                .put("groupId", groupId));
    }
    
    public List<SubversionMaiaGroupPermission> listGroupAccesses(String repo, String path) throws IOException {
        return postMaia(SubversionMaiaGroupPermissions.class,
                new ObjectMap().put("action", "ListGroupAccesses").put("repo", repo).put("path", path));
    }
    
    public void createOrResetGroup(String groupName) throws IOException {
        postMaia(R.class, new ObjectMap().put("action", "CreateOrResetGroupMembers").put("group", groupName));
    }
    
    public void createOrResetGroupWithMembers(String groupName, String... members) throws IOException {
        postMaia(R.class, new ObjectMap().put("action", "CreateOrResetGroupMembers").put("group", groupName)
                .put("members", CommonUtil.toJson(members)));
    }
    
    public void changePassword(String username, String password) throws IOException {
        postMaia(R.class,
                new ObjectMap().put("action", "ChangePassword").put("username", username).put("password", password));
    }
    
    public String executeSvnLook(String command, String... arguments) throws IOException {
        return postMaia(String.class,
                new ObjectMap().put("action", "SvnLookCommand").put("arguments", CommonUtil.toJson(arguments)));
    }
    
    public String executeSvnAdmin(String command, String... arguments) throws IOException {
        return postMaia(String.class,
                new ObjectMap().put("action", "SvnAdminCommand").put("arguments", CommonUtil.toJson(arguments)));
    }
    
    public void forceUpdateAuthz() throws IOException {
        CloseableHttpResponse res = null;
        try {
            res = HttpUtil.post(HttpUtil.concatUrlPath(apiUrl, "priv/gen_access.php"), null,
                    new ObjectMap().put("Cookie", cookies).asMap(), -1);
        } finally {
            HttpUtil.close(res);
        }
    }
}
