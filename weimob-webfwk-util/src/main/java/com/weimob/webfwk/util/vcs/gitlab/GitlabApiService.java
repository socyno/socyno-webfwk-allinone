package com.weimob.webfwk.util.vcs.gitlab;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.tool.Base64Util;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.DataUtil;
import com.weimob.webfwk.util.tool.StringUtils;

@Slf4j
public class GitlabApiService {

    private final String apiUrl;

    private final String apiToken;

    public GitlabApiService(String apiUrl, String apiToken) {
        this.apiUrl = apiUrl;
        this.apiToken = apiToken;
    }

    public enum FileChangeType {
        A("create"), M("update"), D("delete");

        private String type;

        FileChangeType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public enum ProjectAccessLevel {
        GUEST("10"), REPORTER("20"), DEVELOPER("30"), MAINTAINER("40"), OWNER("50");

        private String value;

        ProjectAccessLevel(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum CommonVisibility {
        PRIVATE("private"), INTERNAL("internal"), PUBLIC("public");

        private String value;

        CommonVisibility(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final static Pattern RE_PUSH_PATH_RULE = Pattern
            .compile("^([AMD]):([^\\r\\n]+)$", Pattern.CASE_INSENSITIVE);

    public String getApiUrl() {
        return apiUrl;
    }

    private String getApiToken() {
        return apiToken;
    }

    public String getApiPath(String path) {
        if (StringUtils.startsWith(path, "-/")) {
            path = path.substring(2);
            return HttpUtil.concatUrlPath(getApiUrl().replaceAll("/api/v.*$", ""), path);
        }
        return HttpUtil.concatUrlPath(getApiUrl(), path);
    }
    
    public boolean checkSameApiToken(String token) {
        return StringUtils.equals(token, getApiToken());
    }

    private CloseableHttpResponse request(String path, String method, Map<String, Object> params) throws IOException {
        return request(path, method, params, false);
    }

    private CloseableHttpResponse requestJson(String path, String method, Map<String, Object> params)
            throws IOException {
        return request(path, method, params, true);
    }

    private CloseableHttpResponse requestJsonNoStatusCheck(String path, String method, Map<String, Object> params)
            throws IOException {
        return requestNoStatusCheck(path, method, params, true);
    }

    private CloseableHttpResponse request(String path, String method, Map<String, Object> params, boolean asJsonBody)
            throws IOException {
        CloseableHttpResponse resp = requestNoStatusCheck(path, method, params, asJsonBody);
        checkResponseStatus(resp);
        return resp;
    }

    private void checkResponseStatus(@NonNull CloseableHttpResponse resp) throws IOException {
        log.info("Gitlab api response status={}", resp.getStatusLine());
        if (resp.getStatusLine().getStatusCode() < 200 || resp.getStatusLine().getStatusCode() >= 300) {
            String message = resp.getStatusLine().toString();
            try {
                JsonElement json = HttpUtil.getResponseJson(resp);
                if (json.isJsonObject() && ((JsonObject) json).has("message")) {
                    message = CommonUtil.getJstring((JsonObject) json, "message");
                }
            } finally {
                HttpUtil.close(resp);
            }
            throw new MessageException(message);
        }
    }

    private CloseableHttpResponse requestNoStatusCheck(String path, String method, Map<String, Object> params,
            boolean asJsonBody) throws IOException {
        Map<String, Object> headers = new ObjectMap().put("Private-Token", getApiToken()).asMap();
        byte[] body = null;
        if (asJsonBody) {
            body = StringUtils.trimToEmpty(CommonUtil.toJson(params)).getBytes("UTF-8");
            params = null;
            headers.put("Content-Type", ContentType.APPLICATION_JSON.toString());
        }
        CloseableHttpResponse resp = HttpUtil.request(getApiPath(path), method, params, headers, body, 30000);
        return resp;
    }

    public List<GitlabProject> queryProjects(@NonNull String keyword) throws Exception {
        CloseableHttpResponse resp = null;
        List<GitlabProject> projects = new ArrayList<GitlabProject>();
        try {
            Map<String, Object> params = new ObjectMap().put("search", keyword).asMap();
            resp = request("projects", "GET", params);
            JsonElement content = HttpUtil.getResponseJson(resp);
            if (content == null || !content.isJsonArray()) {
                throw new IllegalResponseException();
            }
            for (JsonElement p : (JsonArray) content) {
                GitlabProject project = new GitlabProject();
                project = CommonUtil.fromJsonByFieldNamingPolicy(p, GitlabProject.class);
                projects.add(project);
            }
        } finally {
            HttpUtil.close(resp);
        }
        return projects;
    }

    public GitlabProject getProject(@NonNull Object idOrPath) throws IOException {
        CloseableHttpResponse resp = null;
        try {
            idOrPath = URLEncoder.encode(idOrPath.toString(), "UTF-8");
            resp = request(String.format("projects/%s", idOrPath), "GET", null);
            JsonElement content = HttpUtil.getResponseJson(resp);
            if (content == null || !content.isJsonObject()) {
                throw new IllegalResponseException();
            }

            return CommonUtil.fromJsonByFieldNamingPolicy(content, GitlabProject.class);
        } finally {
            HttpUtil.close(resp);
        }
    }
    
    /**
     * 获取版本目录的子节点及日志信息
     */
    public List<GitlabLogEntry> listCommitLogEntries(@NonNull String pathWithNamespace, String refOrCommit, String path)
            throws Exception {
        List<GitlabLogEntry> data = new ArrayList<>();
        CloseableHttpResponse resp = null;
        try {
            if (StringUtils.isBlank(refOrCommit)) {
                refOrCommit = "master";
            }
            ObjectMap query = new ObjectMap().put("path", CommonUtil.ifBlank(path, "").replaceAll("^/+", ""));
            resp = request(
                    String.format("/projects/%s/repository/commits/%s/tree",
                            URLEncoder.encode(pathWithNamespace, "UTF-8"),
                            URLEncoder.encode(refOrCommit.replaceAll("^refs/(heads|tags)/", ""), "UTF-8")),
                    "GET", query.asMap());
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonArray()) {
                throw new IllegalResponseException();
            }
            for (JsonElement e : (JsonArray) json) {
                data.add(GitlabLogEntry.fromJson((JsonObject) e));
            }
        } finally {
            HttpUtil.close(resp);
        }
        return data;
    }
    
    /**
     * 获取指定仓库的文件树
     * 
     * @param pathWithNamespase
     * @param refOrCommit
     * @return
     * @throws Exception
     */
    public List<GitlabTreeEntry> getCommitTreeEntries(@NonNull String pathWithNamespase, String refOrCommit,
            boolean recursive) throws Exception {
        return getCommitTreeEntries(pathWithNamespase, refOrCommit, recursive, "");
    }

    /**
     * 获取指定仓库的文件树
     * 
     * @param pathWithNamespase
     * @param refOrCommit
     * @return
     * @throws Exception
     */
    public List<GitlabTreeEntry> getCommitTreeEntries(@NonNull String pathWithNamespase, String refOrCommit,
            boolean recursive, String path) throws Exception {
        List<GitlabTreeEntry> data = new ArrayList<>();
        CloseableHttpResponse resp = null;
        try {
            Map<String, Object> query = new ObjectMap().put("recursive", recursive).put("per_page", 200000).asMap();
            if (StringUtils.isNotBlank(refOrCommit)) {
                query.put("ref", refOrCommit);
            }
            if (StringUtils.isNotBlank(path)) {
                query.put("path", path);
            }
            resp = request(String.format("projects/%s/repository/tree", URLEncoder.encode(pathWithNamespase, "UTF-8")),
                    "GET", query);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonArray()) {
                throw new IllegalResponseException();
            }

            for (JsonElement e : (JsonArray) json) {
                data.add(GitlabTreeEntry.fromJson((JsonObject) e));
            }
        } finally {
            HttpUtil.close(resp);
        }
        return data;
    }

    /**
     * 获取指定的 ref 或 commit 的信息
     * 
     * @param project
     * @param refOrCommit
     * @return
     * @throws Exception
     */
    public GitlabCommit fetchRefOrCommit(String project, String refOrCommit) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            String url = String.format("projects/%s/repository/commits/%s", URLEncoder.encode(project, "UTF-8"),
                    URLEncoder.encode(refOrCommit, "UTF-8"));
            resp = request(url, "GET", null);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabCommit.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 获取变更记录清单
     * 
     * @param project
     * @param refOrCommit
     * @return
     * @throws Exception
     */
    public List<GitlabCommit> fetchCommits(String project, String refOrCommit) throws Exception {
        return fetchCommits(project, refOrCommit, null);
    }

    /**
     * 获取变更记录清单
     * 
     * @param project
     * @param refOrCommit
     * @param path
     * @return
     * @throws Exception
     */
    public List<GitlabCommit> fetchCommits(String project, String refOrCommit, String path) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            String url = String.format("projects/%s/repository/commits", URLEncoder.encode(project, "UTF-8"));
            ObjectMap params = new ObjectMap();
            if (StringUtils.isNotBlank(path)) {
                params.put("path", path);
            }
            if (StringUtils.isNotBlank(refOrCommit)) {
                params.put("ref_name", refOrCommit);
            }
            resp = request(url, "GET", params.asMap());
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonArray()) {
                throw new IllegalResponseException();
            }
            List<GitlabCommit> commits = new ArrayList<>(((JsonArray) json).size());
            for (JsonElement c : (JsonArray) json) {
                commits.add(GitlabCommit.fromJson((JsonObject) c));
            }
            return commits;
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 检索文件信息以及内容
     *
     **/
    public GitlabFile fetchFile(String project, String path, String refOrCommit) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            String url = String.format("projects/%s/repository/files/%s", URLEncoder.encode(project, "UTF-8"),
                    URLEncoder.encode(path, "UTF-8"));
            Map<String, Object> params = null;
            if (!StringUtils.isBlank(refOrCommit)) {
                params = new ObjectMap().put("ref", refOrCommit).asMap();
            }
            resp = request(url, "GET", params);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabFile.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 创建 commit
     * 
     * @param project
     * @param targetBranch
     * @param commitMessage
     * @param files
     * @return
     * @throws Exception
     */
    public GitlabCommit uploadFiles(Object idOrNameWithNamespace, String targetBranch, String commitMessage,
            Map<String, byte[]> files) throws Exception {
        targetBranch = formatBranchWithoutRefsHeads(targetBranch);
        if (idOrNameWithNamespace == null || StringUtils.isBlank(idOrNameWithNamespace.toString()) || files == null
                || files.isEmpty() || StringUtils.isBlank(targetBranch) || StringUtils.isBlank(commitMessage)) {
            throw new IllegalArgumentException();
        }
        CloseableHttpResponse resp = null;
        try {
            Matcher matched = null;
            List<Object> actions = new ArrayList<Object>();
            for (Entry<String, byte[]> f : files.entrySet()) {
                String path = f.getKey();
                if (StringUtils.isBlank(path) || (matched = RE_PUSH_PATH_RULE.matcher(path)) == null || !matched.find()) {
                    throw new IllegalArgumentException("File path is invalid");
                }
                String action = matched.group(1).toLowerCase();
                action = "m".equals(action) ? "update" : ("d".equals(action) ? "delete" : "create");
                Map<String, Object> data = new ObjectMap().put("action", action).put("file_path", matched.group(2))
                        .asMap();
                if ("create".equals(action) || "update".equals(action)) {
                    data.put("encoding", "base64");
                    data.put("content", new String(Base64Util.encode(f.getValue())));
                }
                actions.add(data);
            }
            String url = String.format("projects/%s/repository/commits",
                    URLEncoder.encode(idOrNameWithNamespace.toString(), "UTF-8"));
            Map<String, Object> params = new ObjectMap().put("actions", actions).put("branch", targetBranch)
                    .put("commit_message", commitMessage).asMap();
            resp = requestJson(url, "POST", params);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabCommit.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 创建 commit
     * 
     * @param project
     * @param targetBranch
     * @param commitMessage
     * @param filePath
     * @param action
     * @param content
     * @return
     * @throws Exception
     */
    public GitlabCommit uploadFile(Object idOrNameWithNamespace, String targetBranch, String commitMessage,
            String filePath, FileChangeType action, byte[] content) throws Exception {
        Map<String, byte[]> uploads = new HashMap<String, byte[]>();
        uploads.put(String.format("%s:%s", CommonUtil.ifNull(action, FileChangeType.A).name(), filePath), content);
        return uploadFiles(idOrNameWithNamespace, targetBranch, commitMessage, uploads);
    }

    /**
     * 创建新的仓库
     * 
     * @param name
     * @param path
     * @param desc
     * @param visibility
     * @return
     * @throws IOException
     */
    public void createProject(String name, String path, String desc, CommonVisibility visibility) throws IOException {
        createProject((Long) null, name, path, desc, visibility);
    }

    /**
     * 创建新的仓库
     * 
     * @param namespaceId
     * @param name
     * @param path
     * @param desc
     * @param visibility
     * @return
     * @throws IOException
     */
    public void createProject(Long namespaceId, String name, String path, String desc, CommonVisibility visibility)
            throws IOException {
        CloseableHttpResponse resp = null;
        try {
            Map<String, Object> params = new ObjectMap().put("name", name).put("description", desc)
                    .put("lfs_enabled", true).put("emails_disabled", true).put("initialize_with_readme", true)
                    .put("printing_merge_request_link_enabled", false)
                    .put("visibility", CommonUtil.ifNull(visibility, CommonVisibility.PRIVATE).getValue()).asMap();
            if (namespaceId != null) {
                params.put("namespace_id", namespaceId);
            }
            if (!StringUtils.isBlank(path)) {
                params.put("path", path);
            }
            resp = requestJson("projects", "POST", params);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 创建新的仓库
     * 
     * @param name
     * @param desc
     * @return
     * @throws IOException
     */
    public void createProject(String name, String desc) throws IOException {
        createProject(name, null, desc, null);
    }

    /**
     * 创建新的仓库
     * 
     * @param name
     * @param desc
     * @param visibility
     * @return
     * @throws IOException
     */
    public void createProject(String name, String desc, CommonVisibility visibility) throws IOException {
        createProject(name, null, desc, visibility);
    }

    /**
     * 创建本地帐户
     * 
     * @param username
     * @param display
     * @param mailAddress
     * @return
     */
    public GitlabUser createUser(String username, String display, String mailAddress) throws Exception {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(display) || StringUtils.isBlank(mailAddress)) {
            throw new IllegalArgumentException("用户登录名、显示名或邮件地址不合法!");
        }
        CloseableHttpResponse resp = null;
        try {
            Map<String, Object> params = new ObjectMap().put("username", username).put("name", display)
                    .put("email", mailAddress).put("password", DataUtil.randomGuid()).asMap();
            resp = requestJson("users", "POST", params);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabUser.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }
    
    /**
     * 获取项目拥有权限的成员列表
     * 
     * @param idOrPath
     *            : project id or path with namespace
     * @return
     * @throws Exception
     */
    public List<GitlabUser> listMemebersOfProject(@NonNull Object idOrPath) throws Exception {
        CloseableHttpResponse resp = null;
        List<GitlabUser> users = new ArrayList<GitlabUser>();
        try {
            idOrPath = URLEncoder.encode(idOrPath.toString(), "UTF-8");
            resp = request(String.format("projects/%s/members", idOrPath), "GET", null);
            JsonElement content = HttpUtil.getResponseJson(resp);
            if (content == null || !content.isJsonArray()) {
                throw new IllegalResponseException();
            }
            for (JsonElement p : (JsonArray) content) {
                users.add(GitlabUser.fromJson(p));
            }
        } finally {
            HttpUtil.close(resp);
        }
        return users;
    }

    /**
     * 列举用户的 ssh key 清单
     * 
     * @param usernameOrId
     * @return
     * @throws Exception
     */
    public List<GitlabSshKey> listUserSshKey(@NonNull Object usernameOrId) throws Exception {
        if (StringUtils.isBlank(usernameOrId.toString())) {
            throw new IllegalArgumentException("用户登录名、显示名或邮件地址不合法!");
        }
        CloseableHttpResponse resp = null;
        try {
            resp = request(String.format("users/%s/keys", URLEncoder.encode(usernameOrId.toString(), "UTF-8")), "GET",
                    null);
            JsonElement content = HttpUtil.getResponseJson(resp);
            if (content == null || !content.isJsonArray()) {
                throw new IllegalResponseException();
            }
            List<GitlabSshKey> entities = new ArrayList<>();
            for (JsonElement p : (JsonArray) content) {
                entities.add(GitlabSshKey.fromJson(p));
            }
            return entities;
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 删除项目的权限组
     * 
     * @param projectId
     */
    public void removeGroupOfProject(@NonNull Object projectIdOrPath, long groupId) throws IOException {
        CloseableHttpResponse resp = null;
        try {
            resp = request(String.format("projects/%s/share/%s",
                    URLEncoder.encode(projectIdOrPath.toString(), "UTF-8"), groupId), "DELETE", null);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 删除项目的用于权限的成员
     * 
     * @param projectId
     */
    public void removeMemberOfProject(@NonNull Object projectIdOrPath, long userId) throws IOException {
        CloseableHttpResponse resp = null;
        try {
            resp = request(String.format("projects/%s/members/%s",
                    URLEncoder.encode(projectIdOrPath.toString(), "UTF-8"), userId), "DELETE", null);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 获取分组详情
     * 
     * @param name
     * @return
     * @throws Exception
     */
    public GitlabGroup getGroup(Object groupNameOrId) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            if (groupNameOrId == null || StringUtils.isBlank(groupNameOrId.toString())) {
                throw new IllegalArgumentException("组名称或者编号未提供");
            }
            resp = request(String.format("groups/%s", URLEncoder.encode(groupNameOrId.toString(), "UTF-8")), "GET",
                    new ObjectMap().put("with_projects", "false").put("with_custom_attributes", "false").asMap());
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabGroup.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 获取用户信息
     * 
     */
    public GitlabUser getUser(@NonNull Object usernameOrId) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            if (StringUtils.isBlank(usernameOrId.toString())) {
                throw new IllegalArgumentException("用户登录名或编号缺失");
            }
            JsonElement userInfo = null;
            if (usernameOrId.toString().matches("^\\d+$")) {
                resp = request(String.format("users/%s", usernameOrId.toString()), "GET", null);
                userInfo = HttpUtil.getResponseJson(resp);
                if (userInfo == null || !userInfo.isJsonObject()) {
                    throw new IllegalResponseException();
                }
            } else {
                resp = request(String.format("users?username=%s", URLEncoder.encode(usernameOrId.toString(), "UTF-8")),
                        "GET", null);
                userInfo = HttpUtil.getResponseJson(resp);
                if (userInfo == null || !userInfo.isJsonArray() || ((JsonArray) userInfo).size() != 1) {
                    throw new IllegalResponseException();
                }
                userInfo = ((JsonArray) userInfo).get(0);
            }
            return GitlabUser.fromJson((JsonObject) userInfo);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 创建新的分组
     * 
     * @param name
     * @return
     * @throws Exception
     */
    public GitlabGroup createGroup(String name, String path, CommonVisibility visibility, String description)
            throws Exception {
        return createGroup(name, path, visibility, description, null);
    }

    /**
     * 创建新的分组
     * 
     * @param name
     * @return
     * @throws Exception
     */
    public GitlabGroup createGroup(String name, String path, CommonVisibility visibility, String description,
            Long parentGourpId) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("The group name is required.");
            }
            if (StringUtils.isBlank(path)) {
                path = name.trim().toLowerCase();
            }
            Map<String, Object> params = new ObjectMap().put("name", name).put("path", path)
                    .put("description", description)
                    .put("visibility", CommonUtil.ifNull(visibility, CommonVisibility.PRIVATE).getValue()).asMap();
            if (parentGourpId != null) {
                params.put("parent_id", parentGourpId);
            }
            resp = requestJson("groups", "POST", params);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabGroup.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 创建新的私有(private)分组
     * 
     * @param name
     * @param path
     * @param description
     * @return
     * @throws Exception
     */
    public GitlabGroup createGroup(String name, String path, String description) throws Exception {
        return createGroup(name, path, CommonVisibility.PRIVATE, description);
    }

    /**
     * 创建新的私有(private)分组
     * 
     * @param name
     * @param description
     * @return
     * @throws Exception
     */
    public GitlabGroup createGroup(String name, String description) throws Exception {
        return createGroup(name, null, CommonVisibility.PRIVATE, description);
    }

    /**
     * 获取或创建分组。 如果该分组已存在则返回组信息，否则创建一个私有的分组并返回。
     * 
     * @param name
     * @param description
     * @return
     * @throws Exception
     */
    public GitlabGroup getOrCreateGroup(String name, String description, Long parentId) throws Exception {
        GitlabGroup group = null;
        try {
            String nameWithNS = name;
            if (parentId != null) {
                group = getGroup(parentId.toString());
                nameWithNS = String.format("%s/%s", group.getPath(), name);
            }
            group = getGroup(nameWithNS);
        } catch (Exception e) {
            group = createGroup(name, description, parentId);
        }
        return group;
    }

    /**
     * 获取或创建本地帐户
     * 
     * @param username
     * @param display
     * @param mailAddress
     * @return
     */
    public GitlabUser getOrCreateUser(String username, String display, String mailAddress) throws Exception {
        try {
            return getUser(username);
        } catch (Exception e) {
            return createUser(username, display, mailAddress);
        }

    }

    /**
     * 获取或创建分组。 如果该分组已存在则返回组信息，否则创建一个私有的分组并返回。
     * 
     * @param name
     * @param description
     * @return
     * @throws Exception
     */
    public GitlabGroup getOrCreateGroup(String name, String description) throws Exception {
        return getOrCreateGroup(name, description, null);
    }

    /**
     * 通过组名查询组(模糊查询)
     * 
     * @param groupName
     * @return
     * @throws IOException
     */
    public List<GitlabGroupQuery> queryGroupsByName(String groupName) throws IOException {
        CloseableHttpResponse resp = null;
        List<GitlabGroupQuery> groups = new ArrayList<GitlabGroupQuery>();
        try {
            groupName = URLEncoder.encode(groupName, "UTF-8");
            resp = request(String.format("groups?search=%s", groupName), "GET", null);
            JsonElement content = HttpUtil.getResponseJson(resp);
            if (content == null || !content.isJsonArray()) {
                throw new IllegalResponseException();
            }
            for (JsonElement p : (JsonArray) content) {
                groups.add(CommonUtil.fromJsonByFieldNamingPolicy(p, GitlabGroupQuery.class));
            }
        } finally {
            HttpUtil.close(resp);
        }
        return groups;

    }

    /**
     * 通过组名查询组(精确查询,gitlab组名不会重复)
     * 
     * @param groupName
     * @return
     * @throws IOException
     */
    public GitlabGroupQuery queryGroupByName(String groupName) throws IOException {
        List<GitlabGroupQuery> groups = queryGroupsByName(groupName);
        for (GitlabGroupQuery group : groups) {
            if (group != null && group.getName().equals(groupName)) {
                return group;
            }
        }
        return null;
    }

    /**
     * 获取组内所有成员
     */
    public List<GitlabGroupMember> getMembersOfGroup(Object groupNameOrId) throws IOException {
        if (groupNameOrId == null || StringUtils.isBlank(groupNameOrId.toString())) {
            return null;
        }
        CloseableHttpResponse resp = null;
        List<GitlabGroupMember> members = new ArrayList<GitlabGroupMember>();
        try {
            resp = request(String.format("groups/%s/members", URLEncoder.encode(groupNameOrId.toString(), "UTF-8")),
                    "GET", null);
            JsonElement content = HttpUtil.getResponseJson(resp);
            if (content == null || !content.isJsonArray()) {
                throw new IllegalResponseException();
            }
            for (JsonElement p : (JsonArray) content) {
                members.add(CommonUtil.fromJsonByFieldNamingPolicy(p, GitlabGroupMember.class));
            }
        } finally {
            HttpUtil.close(resp);
        }
        return members;
    }

    /**
     * 增加组成员
     * 
     */
    public void addGroupMember(Object groupNameOrId, long userId, ProjectAccessLevel accessLevel, Date expiresDate)
            throws NumberFormatException, IOException {
        addGroupMember(groupNameOrId, userId, accessLevel, expiresDate, false);
    }

    public void addGroupMember(Object groupNameOrId, long userId, ProjectAccessLevel accessLevel, Date expiresDate,
            boolean skipIfExists) throws NumberFormatException, IOException {
        if (groupNameOrId == null || StringUtils.isBlank(groupNameOrId.toString())) {
            throw new IllegalArgumentException("分组的名称或标识号缺失");
        }
        CloseableHttpResponse resp = null;
        try {
            accessLevel = CommonUtil.ifNull(accessLevel, ProjectAccessLevel.GUEST);

            Map<String, Object> params = new ObjectMap().put("user_id", userId)
                    .put("access_level", accessLevel.getValue()).asMap();
            if (expiresDate != null) {
                params.put("expires_at", DateFormatUtils.format(expiresDate, "yyyy-MM-dd"));
            }
            resp = requestJsonNoStatusCheck(
                    String.format("groups/%s/members", URLEncoder.encode(groupNameOrId.toString(), "UTF-8")), "POST",
                    params);
            if (skipIfExists && 409 == HttpUtil.getStatusCode(resp)
                    && StringUtils.containsIgnoreCase(HttpUtil.getResponseText(resp), "already exists")) {
                return;
            }
            checkResponseStatus(resp);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return;
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 添加用户的 Ssh Key
     */
    public GitlabSshKey addUserSshKey(long userId, String title, String key) throws Exception {
        CloseableHttpResponse resp = null;
        if (StringUtils.isBlank(key) || StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("SshKey 的标题或内容缺失");
        }
        try {
            Map<String, Object> params = new ObjectMap().put("title", title).put("key", key).asMap();
            resp = request(String.format("users/%s/keys", userId), "POST", params);
            return GitlabSshKey.fromJson(HttpUtil.getResponseJson(resp));
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 删除组内成员
     * 
     */
    public void removeGroupMember(@NonNull Object groupNameId, long userId) throws IOException {
        CloseableHttpResponse resp = null;
        try {
            if (StringUtils.isBlank(groupNameId.toString())) {
                throw new IllegalArgumentException("分组和用户必须提供");
            }
            resp = request(
                    String.format("groups/%s/members/%s", URLEncoder.encode(groupNameId.toString(), "UTF-8"), userId),
                    "DELETE", null);
            HttpUtil.getResponseJson(resp);
            return;
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 创建新的私有(private)分组
     * 
     * @param name
     * @param description
     * @return
     * @throws Exception
     */
    public GitlabGroup createGroup(String name, String description, Long parentId) throws Exception {
        return createGroup(name, null, CommonVisibility.PRIVATE, description, parentId);
    }

    /**
     * 分配仓库的组权限
     * 
     * @param projectId
     * @param groupId
     * @param accessLevel
     * @param expiresDate
     * @throws Exception
     */
    public void shareProjectToGroup(long projectId, long groupId, ProjectAccessLevel accessLevel, Date expiresDate,
            boolean skipIfShared) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            if (accessLevel == null) {
                accessLevel = ProjectAccessLevel.GUEST;
            }
            Map<String, Object> params = new ObjectMap().put("id", projectId).put("group_id", groupId)
                    .put("group_access", CommonUtil.ifNull(accessLevel, ProjectAccessLevel.GUEST).getValue()).asMap();
            if (expiresDate != null) {
                params.put("expires_at", DateFormatUtils.format(expiresDate, "yyyy-MM-dd"));
            }
            resp = requestJsonNoStatusCheck(String.format("projects/%s/share", projectId), "POST", params);
            if (skipIfShared && 409 == HttpUtil.getStatusCode(resp)
                    && StringUtils.containsIgnoreCase(HttpUtil.getResponseText(resp), "already shared")) {
                return;
            }
            checkResponseStatus(resp);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return;
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 分配仓库的组权限
     * 
     * @param project
     * @param group
     * @param accessLevel
     * @throws Exception
     */
    public void shareProjectToGroup(long project, long group, ProjectAccessLevel accessLevel, Date expiresDate)
            throws Exception {
        shareProjectToGroup(project, group, accessLevel, expiresDate, false);
    }

    /**
     * 分配仓库的组权限
     * 
     * @param project
     * @param group
     * @param accessLevel
     * @throws Exception
     */
    public void shareProjectToGroup(long project, long group, ProjectAccessLevel accessLevel) throws Exception {
        shareProjectToGroup(project, group, accessLevel, null, false);
    }

    /**
     * 删除用户的 Ssh Key
     */
    public void deleteUserSshKey(long userId, long keyId) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            resp = request(String.format("users/%s/keys/%s", userId, keyId), "DELETE", null);
            int statusCode;
            if ((statusCode = HttpUtil.getStatusCode(resp)) == 200 || statusCode == 204) {
                return;
            }
            if (statusCode == 404 && StringUtils.containsIgnoreCase(HttpUtil.getResponseText(resp), "Key Not Found")) {
                return;
            }
            throw new IllegalResponseException();
        } finally {
            HttpUtil.close(resp);
        }
    }

    public String formatBranchWithoutRefsHeads(String name) {
        if ((name = StringUtils.trimToEmpty(name)).isEmpty()) {
            return name;
        }
        if (name.startsWith("refs/heads/")) {
            return name.substring(11);
        }
        return name;
    }

    public String formatBranchWithRefsHeads(String name) {
        if ((name = StringUtils.trimToEmpty(name)).isEmpty()) {
            return name;
        }
        if (!name.startsWith("refs/heads/")) {
            return "refs/heads/" + name;
        }
        return name;
    }

    /**
     * 获取项目分支
     * 
     * @param project
     * @param name
     * @return
     * @throws Exception
     */
    public List<GitlabBranch> listBranches(@NonNull String pathWithNamespaseOrId, String search, Integer limit, Integer page) throws Exception {
        ObjectMap params = new ObjectMap();
        if (StringUtils.isNotBlank(search)) {
            params = params.put("search", search);
        }
        params.put("page", CommonUtil.parseMaximalInteger(page, 1)).put("per_page",
                CommonUtil.parseMaximalInteger(CommonUtil.ifNull(limit, 20), 1));
        CloseableHttpResponse resp = request(
                String.format("projects/%s/repository/branches", URLEncoder.encode(pathWithNamespaseOrId, "UTF-8")),
                "GET", params.asMap());
        JsonElement content = HttpUtil.getResponseJson(resp);
        if (content == null || !content.isJsonArray()) {
            throw new IllegalResponseException();
        }
        List<GitlabBranch> list = new ArrayList<GitlabBranch>();
        for (JsonElement p : (JsonArray) content) {
            GitlabBranch branch = GitlabBranch.fromJson((JsonObject) p);
            list.add(branch);
        }
        return list;
    }

    /**
     * 获取项目指定的分支信息
     * 
     * @param project
     * @param name
     * @return
     * @throws Exception
     */
    public GitlabBranch getBranch(String project, String name) throws Exception {
        if ((name = formatBranchWithoutRefsHeads(name)).isEmpty()) {
            throw new IllegalArgumentException("The branch name is required.");
        }
        CloseableHttpResponse resp = request(
                String.format("projects/%s/repository/branches/%s", URLEncoder.encode(project, "UTF-8"),
                        URLEncoder.encode(name, "UTF-8")), "GET", null);
        JsonElement json = HttpUtil.getResponseJson(resp);
        if (json == null || !json.isJsonObject()) {
            throw new IllegalResponseException();
        }
        return GitlabBranch.fromJson((JsonObject) json);
    }

    /**
     * 创建分支
     * 
     * @param projectIdOrName
     * @param branch
     * @param refOcommit
     * @return
     * @throws Exception
     */
    public GitlabBranch createBranch(String projectIdOrName, String branch, String refOcommit) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            if (StringUtils.isBlank(projectIdOrName)) {
                throw new IllegalArgumentException("The project id or name is required.");
            }
            if (StringUtils.isBlank(branch)) {
                throw new IllegalArgumentException("The new branch name is required.");
            }
            if (StringUtils.isBlank(refOcommit)) {
                throw new IllegalArgumentException("The reference or commit is required.");
            }

            Map<String, Object> params = new ObjectMap().put("id", projectIdOrName).put("branch", branch)
                    .put("ref", refOcommit).asMap();
            resp = request(
                    String.format("projects/%s/repository/branches", URLEncoder.encode(projectIdOrName, "UTF-8")),
                    "POST", params);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabBranch.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }
    
    /**
     * 删除分支
     * 
     * @param projectIdOrName
     * @param branch
     * @return
     * @throws Exception
     */
    public void deleteBranch(String projectIdOrName, String branch) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            if (StringUtils.isBlank(projectIdOrName)) {
                throw new IllegalArgumentException("The project id or name is required.");
            }
            if (StringUtils.isBlank(branch)) {
                throw new IllegalArgumentException("The new branch name is required.");
            }
            resp = request(String.format("projects/%s/repository/branches/%s",
                    URLEncoder.encode(projectIdOrName, "UTF-8"), URLEncoder.encode(branch, "UTF-8")), "DELETE", null);
        } finally {
            HttpUtil.close(resp);
        }
    }
    
    /**
     * 获取项目tags
     * 
     * @param project
     * @param name
     * @return
     * @throws Exception
     */
    public List<GitlabTag> listTags(@NonNull String pathWithNamespaseOrId, String search, Integer limit, Integer page) throws Exception {
        ObjectMap params = new ObjectMap();
        if (StringUtils.isNotBlank(search)) {
            params = params.put("search", search);
        }
        params.put("page", CommonUtil.parseMaximalInteger(page, 1)).put("per_page",
                CommonUtil.parseMaximalInteger(CommonUtil.ifNull(limit, 20), 1));
        
        CloseableHttpResponse resp = request(
                String.format("projects/%s/repository/tags", URLEncoder.encode(pathWithNamespaseOrId, "UTF-8")), "GET",
                params.asMap());
        JsonElement content = HttpUtil.getResponseJson(resp);
        if (content == null || !content.isJsonArray()) {
            throw new IllegalResponseException();
        }
        List<GitlabTag> list = new ArrayList<GitlabTag>();
        for (JsonElement p : (JsonArray) content) {
            GitlabTag branch = GitlabTag.fromJson((JsonObject) p);
            list.add(branch);
        }
        return list;
    }
    
    /**
     * 获取项目指定的分支信息
     * 
     * @param project
     * @param name
     * @return
     * @throws Exception
     */
    public GitlabTag getTag(String project, String tagName) throws Exception {
        if ((tagName = formatBranchWithoutRefsHeads(tagName)).isEmpty()) {
            throw new IllegalArgumentException("The tag name is required.");
        }
        CloseableHttpResponse resp = request(
                String.format("projects/%s/repository/tags/%s", URLEncoder.encode(project, "UTF-8"),
                        URLEncoder.encode(tagName, "UTF-8")), "GET", null);
        JsonElement json = HttpUtil.getResponseJson(resp);
        if (json == null || !json.isJsonObject()) {
            throw new IllegalResponseException();
        }
        return GitlabTag.fromJson((JsonObject) json);
    }

    /**
     * 创建标签
     * 
     * @param tagName
     *            标签名称
     * @param projectIdOrName
     *            仓库名称或ID
     * @param refOrCommit
     *            ref 名称或者 commit 编号
     * @param message
     *            标签的描述信息
     * @return
     * @throws Exception
     */
    public GitlabTag createTag(String tagName, String projectIdOrName, String refOrCommit, String message)
            throws Exception {
        CloseableHttpResponse resp = null;
        try {
            if (StringUtils.isBlank(tagName)) {
                throw new IllegalArgumentException("The tag name is required.");
            }
            if (StringUtils.isBlank(projectIdOrName)) {
                throw new IllegalArgumentException("The project id or name is required.");
            }
            if (StringUtils.isBlank(refOrCommit)) {
                throw new IllegalArgumentException("The commit or reference is required.");
            }
            Map<String, Object> params = new ObjectMap().put("id", projectIdOrName).put("tag_name", tagName)
                    .put("ref", refOrCommit).asMap();
            if (!StringUtils.isBlank(message)) {
                params.put("message", message);
            }
            resp = requestJson(
                    String.format("projects/%s/repository/tags", URLEncoder.encode(projectIdOrName, "UTF-8")), "POST",
                    params);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabTag.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 删除项目标签
     * 
     * @param tagName
     * @param projectIdOrName
     * @return
     * @throws Exception
     */
    public void deleteTag(String tagName, String projectIdOrName) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            if (StringUtils.isBlank(tagName)) {
                throw new IllegalArgumentException("The tag name is required.");
            }
            if (StringUtils.isBlank(projectIdOrName)) {
                throw new IllegalArgumentException("The project id or name is required.");
            }
            resp = request(String.format("projects/%s/repository/tags/%s", URLEncoder.encode(projectIdOrName, "UTF-8"),
                    URLEncoder.encode(tagName, "UTF-8")), "DELETE", null);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 获取当前的用户信息
     * 
     * @return
     * @throws Exception
     * 
     */
    public GitlabUser getCurrentUser() throws Exception {
        CloseableHttpResponse resp = null;
        try {
            resp = request("user", "GET", null);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabUser.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 检索指定 commit 的变更差量内容
     * 
     * @return
     * @throws Exception
     */
    public List<GitlabFileDiff> getCommitDiffs(String project, String refOrCommit) throws Exception {
        return getCommitDiffs(project, refOrCommit, null);
    }

    /**
     * 检索指定两个 commits 的差量内容
     * 
     * @return
     * @throws Exception
     */
    public List<GitlabFileDiff> getCommitDiffs(String project, String toCommit, String fromCommit) throws Exception {
        int page = 1;
        final int limit = 200;
        boolean dataEnded = false;
        List<GitlabFileDiff> pager = null;
        final List<GitlabFileDiff> all = new ArrayList<>();
        while (!dataEnded) {
            if ((pager = getCommitDiffs(project, toCommit, fromCommit, limit, ++page)) == null || pager.size() < limit) {
                dataEnded = true;
            }
            if (pager != null) {
                all.addAll(pager);
            }
        }
        return all;
    }

    /**
     * 检索指定两个 commits 的差量内容
     * 
     * @return
     * @throws Exception
     */
    public List<GitlabFileDiff> getCommitDiffs(String project, String toCommit, String fromCommit, int limit, int page)
            throws Exception {
        project = CommonUtil.ensureNonBlank(project, "仓库名称或ID为提供");
        toCommit = CommonUtil.ensureNonBlank(toCommit, "分支,标签或SHA值必须提供");
        List<GitlabFileDiff> diffs = new ArrayList<>();
        CloseableHttpResponse resp = null;
        try {
            Map<String, Object> params = null;
            if (!(fromCommit = StringUtils.trimToEmpty(fromCommit)).isEmpty() && !fromCommit.matches("^0+$")
                    && !toCommit.equals(fromCommit)) {
                params = new ObjectMap().put("from", fromCommit).put("page", page).put("per_page", limit).asMap();
            }
            resp = request(String.format("projects/%s/repository/commits/%s/diffx",
                    URLEncoder.encode(project, "UTF-8"), URLEncoder.encode(toCommit, "UTF-8")), "GET", params);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonArray()) {
                throw new IllegalResponseException();
            }
            for (JsonElement e : (JsonArray) json) {
                diffs.add(GitlabFileDiff.fromJson((JsonObject) e));
            }
            return diffs;
        } finally {
            HttpUtil.close(resp);
        }
    }

    /**
     * 获取指定 commit 所在的 references 列表
     * 
     * @param project
     * @param commit
     * @return
     * @throws Exception
     */
    public List<GitlabCommitReference> getCommitReferences(String project, String commit) throws Exception {
        project = CommonUtil.ensureNonBlank(project, "仓库名称或ID为提供");
        commit = CommonUtil.ensureNonBlank(commit, "分支,标签或SHA值必须提供");
        List<GitlabCommitReference> refs = new ArrayList<>();
        CloseableHttpResponse resp = null;
        try {
            resp = request(String.format("projects/%s/repository/commits/%s/refs", URLEncoder.encode(project, "UTF-8"),
                    URLEncoder.encode(commit, "UTF-8")), "GET", null);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonArray()) {
                throw new IllegalResponseException();
            }
            for (JsonElement e : (JsonArray) json) {
                refs.add(GitlabCommitReference.fromJson((JsonObject) e));
            }
            return refs;
        } finally {
            HttpUtil.close(resp);
        }
    }

    public GitlabNamespace getNamespace(String namespace) throws Exception {
        CloseableHttpResponse resp = null;
        try {
            if (StringUtils.isBlank(namespace)) {
                throw new IllegalArgumentException("命名空间名称未提供");
            }
            resp = request(String.format("namespaces/%s", URLEncoder.encode(namespace, "UTF-8")), "GET", null);
            JsonElement json = HttpUtil.getResponseJson(resp);
            if (json == null || !json.isJsonObject()) {
                throw new IllegalResponseException();
            }
            return GitlabNamespace.fromJson((JsonObject) json);
        } finally {
            HttpUtil.close(resp);
        }
    }
}
