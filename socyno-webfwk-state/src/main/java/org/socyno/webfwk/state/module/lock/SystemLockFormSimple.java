package org.socyno.webfwk.state.module.lock;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import org.socyno.webfwk.state.abs.*;
import org.socyno.webfwk.state.util.StateFormBasicSaved;

@Getter
@Setter
@ToString
@Attributes(title = "系统分布式锁")
public class SystemLockFormSimple extends StateFormBasicSaved implements AbstractStateFormBase {
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return SystemLockService.getInstance().getStates();
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "锁标的类型")
    private String objectType;
    
    @Attributes(title = "锁标的对象")
    private String objectId;
    
    @Attributes(title = "锁状态")
    private String locked;
    
    @Attributes(title = "锁标题")
    private String title;
    
    @Attributes(title = "任务结果")
    private String result;
    
    @Attributes(title = "任务日志")
    private String logfile;
    
    @Attributes(title = "任务启动时间")
    private String runningAt;
    
    @Attributes(title = "任务释放时间")
    private String unlockedAt;
    
    @Attributes(title = "任务释放者编号")
    private String unlockedBy;
    
    @Attributes(title = "任务释放者账户")
    private String unlockedCodeBy;
    
    @Attributes(title = "任务释放者姓名")
    private String unlockedNameBy;
    
    @Attributes(title = "锁超时时间（秒）")
    private String timeoutSeconds;
    
}
