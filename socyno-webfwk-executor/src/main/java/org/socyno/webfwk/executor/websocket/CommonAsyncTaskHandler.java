package org.socyno.webfwk.executor.websocket;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.socyno.webfwk.executor.service.AsyncTaskService;
import org.socyno.webfwk.util.model.SimpleLock;
import org.socyno.webfwk.util.remote.R;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.FileTailer;
import org.socyno.webfwk.util.tool.StringUtils;
import org.socyno.webfwk.util.websocket.BasicStateTaskStatusView;
import org.socyno.webfwk.util.websocket.WebSocketViewDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@Configuration
@EnableWebSocket
public class CommonAsyncTaskHandler extends WebSocketHanlderWithTokenInterceptor {
    
    @Override
    public String getRequestPath() {
        return "/ws/async/task";
    }
    
    @Override
    public WebSocketViewDefinition getFormViewDefinition(WebSocketRequest request, WebSocketSession session) throws Exception {
        switch (request.getAction()) {
            case "Status":
                return new RemoteWebSocketViewDefinition(BasicStateTaskStatusView.class);
            default:
                return null;
        }
    }
    
    /**
     * 写入异步任务结束标记和结果
     */
    private void sendAsyncTaskStatus(WebSocketSession session, SimpleLock lock, String logsContent) throws IOException {
        if (lock == null) {
            writeResponse(session, R.error("Task Not Found"));
            return;
        }
        BasicStateTaskStatusView status = new BasicStateTaskStatusView();
        status.setId(lock.getId());
        status.setTitle(lock.getTitle());
        status.setStatus(BasicStateTaskStatusView.TaskStatus.valueFromLock(lock));
        status.setResult(BasicStateTaskStatusView.TaskResult.valueFromLock(lock));
        status.setTargetType(lock.getObjectType());
        status.setTargetId(lock.getObjectId());
        status.setCreatedAt(CommonUtil.ifNull(lock.getRunningAt(), lock.getCreatedAt()));
        status.setCreatedBy(lock.getCreatedUserName());
        status.setCompletedAt(lock.getUnlockedAt());
        status.setLogsTextDelta(logsContent);
        writeResponse(session, R.ok().setData(status));
    }
    
    private class TaskLogFileTailer extends FileTailer {
        
        private SimpleLock task;
        
        private WebSocketSession session;
        
        private boolean ensureFinished = false;
        
        public TaskLogFileTailer (SimpleLock task, WebSocketSession session) {
            super(task.getLogfile(), 120000 /* 读等待超时两分钟 */);
            this.task = task;
            this.session = session;
        }
        
        @Override
        protected boolean onChunkEnd() throws IOException {
            /**
             * 通过该变量来控制退出，可以在任务完成后，继续对日志文件
             * 做一次读取，避免实时读取时内容不完整的问题。
             */
            if (ensureFinished) {
                return true;
            }
            try {
                /* 任务丢失，或已经结束, 退出日志读取 */
                if ((task = AsyncTaskService.DEFAULT.getStatus(task.getId())) == null || task.isFinished()) {
                    sendAsyncTaskStatus(session, task, null);
                    ensureFinished = true;
                }
            } catch (Exception e) {
                /* 系统异常, 则退出日志读取 */
                sendAsyncTaskStatus(session, task, null);
                ensureFinished = true;
            }
            return false;
        }
        
        @Override
        protected void onChunkStarted() throws IOException {
        }
        
        @Override
        protected void onLineRead(String line) throws IOException {
            sendAsyncTaskStatus(session, task, line);
        }
    }
    
    /**
     * 获取通用异步任务日志
     * @param session
     * @param parameters
     * @throws Exception 
     */
    public void handlerActionStatus(
            final WebSocketSession session, 
            final Map<String, String> parameters) throws Exception {
        Long taskId;
        String logsPath = null;
        SimpleLock taskLock = null;
        while (true) {
            /* 任务不存在或日志文件不存在， 则只发送状态信息 */
            if ((taskId = CommonUtil.parseLong(parameters.get("taskId"))) == null
                    || (taskLock = AsyncTaskService.DEFAULT.getStatus(taskId)) == null
                    || StringUtils.isBlank(logsPath = taskLock.getLogfile()) 
                    || !new File(logsPath).exists()) {
                sendAsyncTaskStatus(session, taskLock, null);
                /* 视连接及当前任务情况决定是否继续检测 */
                if (taskLock != null && taskLock.isRunning() && session.isOpen()) {
                    Thread.sleep(5000);
                    continue;
                }
                break;
            } 
            
            /* 如果任务存在日志文件，则启动 tail 读取器，完成后退出 */
            new TaskLogFileTailer(taskLock, session).start();
            break;
        }
    }
}
