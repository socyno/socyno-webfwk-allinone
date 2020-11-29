package org.socyno.webfwk.executor.controller;

import java.io.FileOutputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.socyno.webfwk.executor.api.build.ApplicationBuildParameters;
import org.socyno.webfwk.executor.api.build.ApplicationBuildStatus;
import org.socyno.webfwk.executor.service.AsyncTaskService;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.exception.PageNotFoundException;
import org.socyno.webfwk.util.model.SimpleLock;
import org.socyno.webfwk.util.remote.R;
import org.socyno.webfwk.util.remote.SshClient;
import org.socyno.webfwk.util.remote.SshClient.CmdMonitor;
import org.socyno.webfwk.util.remote.SshScriptConfig;
import org.socyno.webfwk.util.service.AbstractAsyncTaskService.AsyncTaskExecutor;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.SimpleLogger;
import org.socyno.webfwk.util.tool.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.oldratlee.bse.BseUtils;


@RestController
@RequestMapping(value = "/build")
public class ApplicationBuildController {
    
    private final static String BuildPackageUrlKey = "packagePath";
    
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public R start(final @RequestBody ApplicationBuildParameters buildForm) throws Exception {
        
        long taskId = AsyncTaskService.DEFAULT.execute(new AsyncTaskExecutor("应用构建",
                String.format("%s/%s", buildForm.getTenant(), buildForm.getApplication()),
                String.format("应用构建:%s/%s", buildForm.getTenant(), buildForm.getApplication())) {
            
            private SshScriptConfig getAppBuildScriptConfigs() {
                return new SshScriptConfig().setHost(ContextUtil.getConfigTrimed("system.application.build.host.name"))
                        .setUser(ContextUtil.getConfigTrimed("system.application.build.host.user"))
                        .setScript(ContextUtil.getConfigTrimed("system.application.build.host.path"))
                        .setPassword(ContextUtil.getConfigTrimed("system.application.build.host.password"));
            }
            private final StringBuffer packageNameBuffer = new StringBuffer();
            private final Pattern RegexpPackagePrefix = Pattern.compile("\\{package\\:");
            private final Pattern RegexpPackageSuffix = Pattern.compile("\\:package\\}");
            private final String ErrorPackageNotFound = "!!!!!!! No Build Package Found!!!!!!!!!";
            
            @Override
            public boolean execute(FileOutputStream logsOutputStream, String logsDir) throws Exception {
                SshScriptConfig configs = getAppBuildScriptConfigs();
                SimpleLogger.logInfo(logsOutputStream, CommonUtil.toPrettyJson(buildForm));
                int statusCode = SshClient.exec2(configs.getHost(), configs.getUser(),
                        configs.getPassword(),
                        StringUtils.join(BseUtils.escapePlainString(new String[] {
                            "bash",
                            configs.getScript(),
                            String.format("APPLICATOIN_BUILD_TENANT=%s",       StringUtils.trimToEmpty(buildForm.getTenant())),
                            String.format("APPLICATOIN_BUILD_NAMESPACE=%s",    StringUtils.trimToEmpty(buildForm.getNamespace())),
                            String.format("APPLICATOIN_BUILD_APPNAME=%s",      StringUtils.trimToEmpty(buildForm.getApplication())),
                            String.format("APPLICATOIN_BUILD_APPTYPE=%s",      StringUtils.trimToEmpty(buildForm.getAppType())),
                            String.format("APPLICATOIN_BUILD_VERSION=%s",      StringUtils.trimToEmpty(buildForm.getBuildVersion())),
                            String.format("APPLICATOIN_BUILD_GROUPID=%s",      StringUtils.trimToEmpty(buildForm.getArtifectGroupId())),
                            String.format("APPLICATOIN_BUILD_VCSPATH=%s",      StringUtils.trimToEmpty(buildForm.getVcsPath())),
                            String.format("APPLICATOIN_BUILD_VCSREF=%s",       StringUtils.trimToEmpty(buildForm.getVcsRefsName())),
                            String.format("APPLICATOIN_BUILD_VCSREV=%s",       StringUtils.trimToEmpty(buildForm.getVcsRevision())),
                            String.format("APPLICATOIN_BUILD_SERVICE=%s",      StringUtils.trimToEmpty(buildForm.getBuildService()))
                        }), " "), new CmdMonitor() {
                            @Override
                            public void errorMessageFetched(String error) {
                                SimpleLogger.logError(logsOutputStream, error);
                            }
                            
                            @Override
                            public void outputMessageFetched(String output) {
                                if (StringUtils.isNotEmpty(output)) {
                                    Matcher matcher;
                                    String matcherLeft;
                                    if ((matcher = RegexpPackagePrefix.matcher(output)).find()) {
                                        packageNameBuffer.setLength(0);
                                        packageNameBuffer.append(" ");
                                        matcherLeft = output.substring(matcher.end());
                                        if ((matcher = RegexpPackageSuffix.matcher(matcherLeft)).find()) {
                                            packageNameBuffer.append(matcherLeft.substring(0, matcher.start()));
                                        } else {
                                            packageNameBuffer.append(matcherLeft);
                                        }
                                    } else if (packageNameBuffer.length() > 0
                                            && (matcher = RegexpPackageSuffix.matcher(output)).find()) {
                                        packageNameBuffer.append(output.substring(0, matcher.start()));
                                    }
                                }
                                SimpleLogger.logInfo(logsOutputStream, output);
                            }
                        });
                SimpleLogger.logInfo(logsOutputStream, "Exit = %s", statusCode);
                if (statusCode != 0) {
                    return false;
                }
                dataPut(BuildPackageUrlKey, packageNameBuffer.toString().trim().replaceAll("[\\r\\n]+", ""));
                if (!StringUtils.startsWith(dataGet(BuildPackageUrlKey), "http")) {
                    statusCode = -99;
                    dataRemove(BuildPackageUrlKey);
                    dataPut("errorMessage", ErrorPackageNotFound);
                    SimpleLogger.logError(logsOutputStream, ErrorPackageNotFound);
                }
                dataPut("exitCode", "" + statusCode);
                return statusCode == 0;
            }
        });
        
        return R.ok().setData(taskId);
    }
    
    @RequestMapping(value = "/status/{taskId}", method = RequestMethod.GET)
    public R status(@PathVariable Long taskId) throws Exception {
        
        SimpleLock task;
        if (taskId == null || (task = AsyncTaskService.DEFAULT.getStatus(taskId)) == null) {
            throw new PageNotFoundException();
        }
        ApplicationBuildStatus status = AsyncTaskService.toJobBasicStatus(ApplicationBuildStatus.class, task);
        
        Map<String, String> resultData;
        if ((resultData = AsyncTaskService.DEFAULT.getResultData(taskId)) != null) {
            status.setPackageUrl(resultData.get(BuildPackageUrlKey));
        }
        ClassUtil.checkFormRequiredAndOpValue(status);
        return R.ok().setData(status);
    }
}
        