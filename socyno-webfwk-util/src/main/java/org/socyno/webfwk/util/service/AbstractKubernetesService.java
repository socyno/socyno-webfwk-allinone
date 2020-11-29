package org.socyno.webfwk.util.service;


import java.net.URL;
import java.util.List;

import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.remote.HttpUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.DataUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetList;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;

@Slf4j
public abstract class AbstractKubernetesService {
	
	private static final MediaType JSON_TYPE = MediaType.parse("application/json");
	
    protected abstract Config getClientConfig(String env, String clusterName);
    
	/**
	 * 格式化应用名为标准的k8s命名规范
	 * @param context
	 * @return
	 */
    public static String lowerCaseReplace(String context) {
        return CommonUtil.ifNull(context, "").toLowerCase().replace("_", "-");
    }
	
    /**
     * 获取应用的 Deployment 信息
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return
     * @throws Exception
     */
    public Deployment getDeployment(String context, String cluster, String namespace, String env)
            throws Exception {
        return getK8sAppsResource(Deployment.class, context, cluster, namespace, env);
    }
    
    /**
     * 获取应用的 StatefulSet 信息
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return
     * @throws Exception
     */
    public StatefulSet getStatefulSet(String context, String cluster, String namespace, String env)
            throws Exception {
        return getK8sAppsResource(StatefulSet.class, context, cluster, namespace, env);
    }
    
    /**
     * 获取应用的 DaemonSet 信息
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return
     * @throws Exception
     */
    public DaemonSet getDaemonSet(String context, String cluster, String namespace, String env)
            throws Exception {
        return getK8sAppsResource(DaemonSet.class, context, cluster, namespace, env);
    }
    
    /**
     * 获取应用的 ReplicaSet 信息
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return
     * @throws Exception
     */
    public ReplicaSet getReplicaSet(String context, String cluster, String namespace, String env)
            throws Exception {
        return getK8sAppsResource(ReplicaSet.class, context, cluster, namespace, env);
    }
    
    public <T> T getK8sAppsResource(@NonNull Class<T> resouceClass, String context, String cluster, String namespace, String env)
            throws Exception {
        T resourceObject = null;
        String resouceName = resouceClass.getSimpleName();
        Exception errorThrowable = new MessageException(String.format("查询 K8S 资源( %s)失败", resouceName));
        try {
            resourceObject = new ObjectMapper().readValue(getK8sAppsResourceData(
                    resouceClass, context, cluster, namespace, env).toString(), resouceClass);
        } catch (Exception e) {
            if (!(e instanceof MessageException)) {
                log.error(String.format("查询 K8S 资源( %s)失败 ：环境 = %s, 集群 = %s， 命名空间 = %s, 应用 = %s, 原因 : 响应数据异常", 
                        resouceName, env, cluster, namespace, context), e);
            }
            throw e;
        }
        if (resourceObject == null) {
            log.error(String.format("查询 K8S 资源( %s)失败 ：环境 = %s, 集群 = %s， 命名空间 = %s, 应用 = %s, 原因 : 响应数据为空", 
                    resouceName, env, cluster, namespace, context));
            throw errorThrowable;
        }
        return resourceObject;
    }
    
    private JsonNode getK8sAppsResourceData(@NonNull Class<?> resourceClass, String context, String cluster, String namespace, String env)
            throws Exception {
        if (!resourceClass.equals(Deployment.class) 
                && !resourceClass.equals(StatefulSet.class) 
                && !resourceClass.equals(DaemonSet.class)
                && !resourceClass.equals(ReplicaSet.class)) {
                throw new MessageException("查询的资源类不支持。");
        }
        if (StringUtils.isBlank(context)) {
            throw new MessageException("查询的应用名称必须提供。");
        }
        if (StringUtils.isAnyBlank(namespace, cluster)) {
            throw new MessageException("查询的命名空间和集群必须提供。");
        }
        String resourceName = resourceClass.getSimpleName();
        Exception errorThrowable = new MessageException(String.format("查询 K8S 资源( %s)失败", resourceName));
        log.info("查询 K8S 资源( {}) ：环境 = {}, 集群 = {}， 命名空间 = {}, 应用 = {}", 
                resourceName, env, cluster, namespace, context);
        Config k8sServerConfig = getClientConfig(env, cluster);
        OkHttpClient k8sClient = HttpClientUtils.createHttpClient(k8sServerConfig);
        String path = HttpUtil.concatUrlPath(k8sServerConfig.getMasterUrl(),
                String.format("apis/apps/v1/namespaces/%s/%ss/%s", HttpUtil.urlEncode(namespace), 
                        HttpUtil.urlEncode(resourceName.toLowerCase()), HttpUtil.urlEncode(lowerCaseReplace(context))));
        try(Response response = k8sClient.newCall(new Request.Builder().url(new URL(path)).get().build()).execute()) {
            String respState = String.format("%s %s", response.code(), response.message());
            String respBody = new String(IOUtils.toByteArray(response.body().byteStream()), "UTF-8");
            log.info("K8s api request : path = {}, response = {}, {}", path, respState, respBody);
            if (response.code() != 200) {
                log.error("查询 K8S 资源({})失败 ：环境 = {}, 集群 = {}， 命名空间 = {}, 应用 = {}, 原因 : 响应状态错误", 
                        resourceName, env, cluster, namespace, context);
                throw errorThrowable;
            }
            JsonNode kindNode = null;
            JsonNode resouceObject = new ObjectMapper().readTree(respBody);
            if (!resouceObject.isObject() || (kindNode = resouceObject.get("kind")) == null
                    || !kindNode.isTextual() || !resourceName.equalsIgnoreCase(kindNode.textValue())) {
                log.error(String.format("查询 K8S 资源( %s)失败 ：环境 = %s, 集群 = %s， 命名空间 = %s, 应用 = %s, 原因 : 响应数据不匹配", 
                        resourceName, env, cluster, namespace, context));
                throw errorThrowable;
            }
            return resouceObject;
        } catch (Exception e) {
            if (!(e instanceof MessageException)) {
                log.error(String.format("查询 K8S 资源( %s)失败 ：环境 = %s, 集群 = %s， 命名空间 = %s, 应用 = %s, 原因 : 响应数据异常", 
                        resourceName, env, cluster, namespace, context), e);
            }
            throw e;
        }
    }
    
    private void updateK8sAppsResource(@NonNull Class<?> resouceClass, String context, String resouce, String cluster, String namespace, String env)
            throws Exception {
        if (StringUtils.isAnyBlank(context, resouce)) {
            throw new MessageException("替换的应用名称或内容必须提供。");
        }
        if (StringUtils.isAnyBlank(namespace, cluster)) {
            throw new MessageException("替换的命名空间和集群必须提供。");
        }
        String resouceName = resouceClass.getSimpleName();
        log.info("替换 K8S 资源( {}) ：环境 = {}, 集群 = {}， 命名空间 = {}, 应用 = {}, 内容 = {}", 
                            resouceName, env, cluster, namespace, context, resouce);
        Config k8sServerConfig = getClientConfig(env, cluster);
        OkHttpClient k8sClient = HttpClientUtils.createHttpClient(k8sServerConfig);
        String path = HttpUtil.concatUrlPath(k8sServerConfig.getMasterUrl(),
                String.format("apis/apps/v1/namespaces/%s/%ss/%s", HttpUtil.urlEncode(namespace), 
                        HttpUtil.urlEncode(resouceName.toLowerCase()), HttpUtil.urlEncode(lowerCaseReplace(context))));
        try (Response response = k8sClient
                .newCall(new Request.Builder().url(new URL(path)).put(RequestBody.create(JSON_TYPE, resouce)).build())
                .execute()) {
            String respState = String.format("%s %s", response.code(), response.message());
            String respBody = new String(IOUtils.toByteArray(response.body().byteStream()), "UTF-8");
            log.info("K8s api request : path = {}, response = {}, {}", path, respState, respBody);
            if (response.code() != 200 && response.code() != 201) {
                log.error("替换 K8S 资源({})失败 ：环境 = {}, 集群 = {}， 命名空间 = {}, 应用 = {}, 原因 : 响应状态错误", resouceName, env,
                                cluster, namespace, context);
                throw new MessageException(String.format("替换 K8S 资源( %s)失败", resouceName));
            }
        } catch (Exception e) {
            if (!(e instanceof MessageException)) {
                log.error(String.format("替换 K8S 资源( %s)失败 ：环境 = %s, 集群 = %s， 命名空间 = %s, 应用 = %s, 原因 : 响应数据异常", 
                        resouceName, env, cluster, namespace, context), e);
            }
            throw e;
        }
    }
    
    /**
     * 获取应用的版本信息
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return
     * @throws Exception
     */
	public String getPackageVersion(String context, String cluster, String namespace, String env) throws Exception{
		Deployment deployment = getDeployment(context , cluster , namespace , env);
    	return deployment.getSpec().getTemplate().getMetadata().getLabels().get("appVersion") ;
	}
    
    /**
     * 获取应用的 Pods 清单信息
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return
     * @throws Exception
     */
    public PodList getPods(String context, String cluster, String namespace, String env)
            throws Exception {
        if (StringUtils.isBlank(context)) {
            throw new MessageException("查询的应用名称必须提供。");
        }
        if (StringUtils.isAnyBlank(namespace, cluster)) {
            throw new MessageException("查询的命名空间和集群必须提供。");
        }
        log.info("查询 K8S Pods 信息 ：环境 = {}, 集群 = {}， 命名空间 = {}, 应用 = {}", env, cluster, namespace, context);
        Config k8sServerConfig = getClientConfig(env, cluster);
        OkHttpClient k8sHttpClient = HttpClientUtils.createHttpClient(k8sServerConfig);
        String path = HttpUtil.concatUrlPath(k8sServerConfig.getMasterUrl(),
                String.format("api/v1/namespaces/%s/pods?limit=100&labelSelector=appname%%3d%s",
                        HttpUtil.urlEncode(namespace),
                        HttpUtil.urlEncode(AbstractKubernetesService.lowerCaseReplace(context))));
        PodList pods = null;
        try(Response response = k8sHttpClient.newCall(new Request.Builder().url(new URL(path)).get().build()).execute()) {
            String respState = String.format("%s %s", response.code(), response.message());
            String respBody = new String(IOUtils.toByteArray(response.body().byteStream()), "UTF-8");
            log.info("K8s api request : path = {}, response = {}, {}", path, respState, respBody);
            pods = new ObjectMapper().readValue(respBody, PodList.class);
        }
        if (pods == null || !"PodList".equalsIgnoreCase(pods.getKind())) {
            throw new MessageException(String.format("查询 K8S Pods 信息失败 : 环境 = %s, 集群 = %s， 命名空间 = %s, 应用 = %s", env,
                    cluster, namespace, context));
        }
        return pods;
    }
    
    /**
     * 获取应用的副本集（ReplicatSet）清单
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return
     * @throws Exception
     */
    public ReplicaSetList getReplicatSets(String context, String cluster, String namespace, String env)
            throws Exception {
        if (StringUtils.isBlank(context)) {
            throw new MessageException("查询的应用名称必须提供。");
        }
        if (StringUtils.isAnyBlank(namespace, cluster)) {
            throw new MessageException("查询的命名空间和集群必须提供。");
        }
        log.info("查询 K8S ReplicatSets 信息 ：环境 = {}, 集群 = {}， 命名空间 = {}, 应用 = {}", env, cluster, namespace, context);
        Config k8sServerConfig = getClientConfig(env, cluster);
        OkHttpClient k8sHttpClient = HttpClientUtils.createHttpClient(k8sServerConfig);
        String path = HttpUtil.concatUrlPath(k8sServerConfig.getMasterUrl(),
                String.format("apis/apps/v1/namespaces/%s/replicasets?labelSelector=appname%%3d%s",
                        HttpUtil.urlEncode(namespace),
                        HttpUtil.urlEncode(AbstractKubernetesService.lowerCaseReplace(context))));
        ReplicaSetList list = null;
        try(Response response = k8sHttpClient.newCall(new Request.Builder().url(new URL(path)).get().build()).execute()) {
            String respState = String.format("%s %s", response.code(), response.message());
            String respBody = new String(IOUtils.toByteArray(response.body().byteStream()), "UTF-8");
            log.info("K8s api request : path = {}, response = {}, {}", path, respState, respBody);
            list = new ObjectMapper().readValue(respBody, ReplicaSetList.class);
        }
        if (list == null || !"ReplicaSetList".equalsIgnoreCase(list.getKind())) {
            throw new MessageException(String.format("查询 K8S ReplicatSets 信息失败 : 环境 = %s, 集群 = %s， 命名空间 = %s, 应用 = %s", env,
                    cluster, namespace, context));
        }
        return list;
    }
    
    /**
     * 获取应用的事件清单信息
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return
     * @throws Exception
     */
    public EventList getEvents(String cluster, String namespace, String env, String targetName) throws Exception {
        if (StringUtils.isAnyBlank(namespace, cluster)) {
            throw new MessageException("查询的命名空间和集群必须提供。");
        }
        log.info("查询 K8S Events 信息 ：环境 = {}, 集群 = {}， 命名空间 = {}, Pod = {}", env, cluster, namespace, targetName);
        Config k8sServerConfig = getClientConfig(env, cluster);
        OkHttpClient k8sHttpClient = HttpClientUtils.createHttpClient(k8sServerConfig);
        String path = HttpUtil.concatUrlPath(k8sServerConfig.getMasterUrl(),
                String.format("/api/v1/namespaces/%s/events", HttpUtil.urlEncode(namespace)));
        if (StringUtils.isNotBlank(targetName)) {
            path = String.format("%s?fieldSelector=%s", path,
                    HttpUtil.urlEncode(String.format("involvedObject.name=%s", targetName)));
        }
        EventList events = null;
        Exception errorThrowable = new MessageException("查询 K8S Events 信息失败");
        try (Response response = k8sHttpClient.newCall(new Request.Builder().url(new URL(path)).get().build())
                .execute()) {
            String respState = String.format("%s %s", response.code(), response.message());
            String respBody = new String(IOUtils.toByteArray(response.body().byteStream()), "UTF-8");
            log.info("K8s api request : path = {}, response = {}, {}", path, respState, respBody);
            if (response.code() != 200) {
                log.error("查询 K8S Events 失败 ：环境 = {}, 集群 = {}， 命名空间 = {}, 对象 = {}, 原因 : 响应状态错误", 
                         env, cluster, namespace, targetName);
                throw errorThrowable;
            }
            events = new ObjectMapper().readValue(respBody, EventList.class);
        } catch (Exception e) {
            if (!(e instanceof MessageException)) {
                log.error(String.format("查询 K8S Events 失败 ：环境 = %s, 集群 = %s， 命名空间 = %s, 对象 = %s, 原因 : 响应数据异常", 
                        env, cluster, namespace, targetName), e);
            }
            throw e;
        }
        if (events == null || !EventList.class.getSimpleName().equalsIgnoreCase(events.getKind())) {
            log.error("查询 K8S Events 失败 ：环境 = {}, 集群 = {}， 命名空间 = {}, 对象 = {}, 原因 : 响应数据数据异常", 
                    env, cluster, namespace, targetName);
            throw errorThrowable;
        }
        return events;
    }
    
    /**
     * 重启 Deployment
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return 返回替换资源的 UUID， 通过 Label 标签 kfRestartGuid 可定位
     * @throws Exception
     */
    private String restartAppsResource(@NonNull Class<?> resourceClass, String context, String cluster, String namespace, String env) throws Exception {
        JsonNode tmplNode = null;
        JsonNode deployment = getK8sAppsResourceData(resourceClass, context, cluster, namespace, env);
        if ((tmplNode = deployment.get("spec")) == null || (tmplNode = tmplNode.get("template")) == null
                || (tmplNode = tmplNode.get("metadata")) == null || (tmplNode = tmplNode.get("labels")) == null) {
            throw new Exception();
        }
        String labelGuid = null;
        ((ObjectNode)tmplNode).put("kfRestartGuid", labelGuid = DataUtil.randomGuid());
        updateK8sAppsResource(Deployment.class, context, deployment.toString(), cluster, namespace, env);
        return labelGuid;
    }

    /**
     * 重启 Deployment
     * @param context
     * @param cluster
     * @param namespace
     * @param env
     * @return 返回替换资源的 UUID， 通过 Label 标签 kfRestartGuid 可定位
     * @throws Exception
     */
    public String restartDeployment(String context, String cluster, String namespace, String env) throws Exception {
        return restartAppsResource(Deployment.class, context, cluster, namespace, env);
    }
    
    public String restartDaemonSet(String context, String cluster, String namespace, String env) throws Exception {
        return restartAppsResource(DaemonSet.class, context, cluster, namespace, env);
    }
    
    public String restartStatefulSet(String context, String cluster, String namespace, String env) throws Exception {
        return restartAppsResource(StatefulSet.class, context, cluster, namespace, env);
    }

    public String getStatusDisplay(List<ContainerStatus> containerStatusList){
        for (ContainerStatus container : containerStatusList) {
            ContainerState state = container.getState();
            if (state.getRunning() != null) {
                continue;
            }
            if (state.getTerminated() != null) {
                return CommonUtil.ifBlank(state.getTerminated().getReason(),
                        "Terminated");
            }

            if (state.getWaiting() != null) {
                return CommonUtil.ifBlank(state.getWaiting().getReason(),
                        "Waiting");
            }
        }

        return "Running";
    }
}
