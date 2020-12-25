package com.weimob.webfwk.executor.api.vcs;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.executor.abs.AbstractJobParameters;

import lombok.Data;

@Data
public class VcsAuthResetParameters implements AbstractJobParameters {
    
    @Attributes(title = "并发线程数", description = "默认为 10， 最大 100")
    private int concurrentThreads = 0;
    
    @Attributes(title = "业务系统", description = "只重置指定的业务系统")
    private Long[] subsystemIds;
    
    @Attributes(title = "涉及应用", description = "只重置指定的应用")
    private Long[] applicationIds;
}
