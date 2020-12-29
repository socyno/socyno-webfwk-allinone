package com.weimob.webfwk.state.module.notify;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.weimob.webfwk.state.module.notify.SystemNotifyRecordFormSimple.MessageType;
import com.weimob.webfwk.state.module.user.SystemUserService;
import com.weimob.webfwk.util.context.RunableWithSessionContext;
import com.weimob.webfwk.util.context.SessionContext;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.tmpl.EnjoyUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemNotifyService {
    
    public final static int NOEXCEPTION_TMPL_NOTFOUD = 1;
    
    public final static int NOTIFY_DATA_RETURN_ONLY = 2;
    
    @Getter
    private final static SystemNotifyService Instance = new SystemNotifyService();
    
    private static final ThreadPoolExecutor NotifyThreadsPool = new ThreadPoolExecutor(1, 5, 5, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>(200), new ThreadPoolExecutor.DiscardOldestPolicy());
    
    public void sendAsync(String template, Map<String, Object> context, int options) {
        try {
            NotifyThreadsPool.submit(new SystemNotifyThread(template, context, options));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    class SystemNotifyThread extends RunableWithSessionContext {
        private final String template;
        private final Map<String, Object> context;
        private final int options;
        
        SystemNotifyThread(String template, Map<String, Object> context, int options) {
            this.context = context;
            this.template = template;
            this.options = options;
        }
        
        @Override
        public void exec() {
            try {
                sendSync(template, context, options);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 邮件发送
     * 
     * @param template
     *            邮件模板
     * @param context
     *            模板上下文数据
     * @return 
     * @return
     * @throws Exception
     */
    public Map<String, SystemNotifyRecordFormCreation> sendSync(String template, Map<String, Object> context, int options) throws Exception {
        
        /* 获得模板信息 */
        SystemNotifyTemplateFormSimple tmplForm = null;
        if ((tmplForm = SystemNotifyTemplateService.getInstance()
                .getByCode(StringUtils.trimToEmpty(template))) == null) {
            if ((options & NOEXCEPTION_TMPL_NOTFOUD) != 0) {
                return null;
            }
            throw new SystemNotifiyTemplateNotFoundException(template);
        }
        ObjectMap tmplContext = new ObjectMap()
                .put("systemUserService", SystemUserService.getInstance())
                .put("sessionContext", SessionContext.getUserContext());
        if (context != null) {
            tmplContext.putAll(context);
        }
        Map<String, SystemNotifyRecordFormCreation> notifies = new HashMap<>(5);
        if (StringUtils.isNotBlank(tmplForm.getMailContent())) {
            SystemNotifyRecordFormCreation mailNotify= new SystemNotifyRecordFormCreation();
            mailNotify.setType(MessageType.Email.getValue());
            mailNotify.setMessageTo(tmplForm.getMailTo());
            mailNotify.setMessageCc(tmplForm.getMailCc());
            mailNotify.setContent(EnjoyUtil.format(tmplForm.getMailContent(), tmplContext.asMap()));
            notifies.put(MessageType.Email.getValue(), mailNotify);
        }
        if (StringUtils.isNotBlank(tmplForm.getMessageContent())) {
            SystemNotifyRecordFormCreation messageNotify = new SystemNotifyRecordFormCreation();
            messageNotify.setType(MessageType.Message.getValue());
            messageNotify.setMessageTo(tmplForm.getMessageTo());
            messageNotify.setContent(EnjoyUtil.format(tmplForm.getMessageContent(), tmplContext.asMap()));
            notifies.put(MessageType.Message.getValue(), messageNotify);
        }
        if (StringUtils.isNotBlank(tmplForm.getWeixinContent())) {
            SystemNotifyRecordFormCreation weixinNotify = new SystemNotifyRecordFormCreation();
            weixinNotify.setType(MessageType.Weixin.getValue());
            weixinNotify.setMessageTo(tmplForm.getWeixinTo());
            weixinNotify.setContent(EnjoyUtil.format(tmplForm.getWeixinContent(), tmplContext.asMap()));
            notifies.put(MessageType.Weixin.getValue(), weixinNotify);
        }
        if ((options & NOTIFY_DATA_RETURN_ONLY) == 0) {
            for (SystemNotifyRecordFormCreation notify : notifies.values()) {
                SystemNotifyRecordService.getInstance().triggerAction(SystemNotifyRecordService.EVENTS.Create.getName(),
                        notify);
            }
        }
        return notifies;
    }
}
