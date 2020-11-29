package org.socyno.webfwk.module.syslog;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemCommonLogDetail extends SystemCommonLogSimple {

    @Attributes(title = "操作前")
    private String operateBefore;

    @Attributes(title = "操作后")
    private String operateAfter;

}
