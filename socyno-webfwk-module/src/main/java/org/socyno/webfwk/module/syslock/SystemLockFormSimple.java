package org.socyno.webfwk.module.syslock;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Data;

import java.util.Date;
import java.util.List;

import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.util.state.field.*;


@Data
@Attributes(title = "系统公共锁")
public class SystemLockFormSimple implements AbstractStateForm {

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

    @Attributes(title = "编号", readonly = true)
    private Long  id;

    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String  state;

    @Attributes(title = "版本", readonly = true)
    private Long  revision;

    @Attributes(title = "创建人编号")
    private Long createdUserId ;

    @Attributes(title = "创建时间", type = FieldDateTime.class)
    private Date createdAt ;

    @Attributes(title = "创建人姓名")
    private String createdUserName;

    @Attributes(title = "锁目标的类型", position = 1001)
    private String objectType ;

    @Attributes(title = "锁目标的主键", position = 1002)
    private String objectId ;

    @Attributes(title = "锁状态", position = 1003)
    private String locked ;

    @Attributes(title = "锁标题", position = 1004)
    private String title ;

    @Attributes(title = "执行结果", position = 1005)
    private String result ;

    @Attributes(title = "日志地址", position = 1006)
    private String logfile ;

    @Attributes(title = "任务启动时间", position = 1007)
    private String runningAt ;

    @Attributes(title = "释放时间", position = 1008)
    private String unlockedAt ;

    @Attributes(title = "释放者编号", position = 1009)
    private String unlockedUserId ;

    @Attributes(title = "释放者姓名", position = 1010)
    private String unlockedUserName ;

    @Attributes(title = "超时时间，单位秒", position = 1011)
    private String timeoutSeconds ;

}
