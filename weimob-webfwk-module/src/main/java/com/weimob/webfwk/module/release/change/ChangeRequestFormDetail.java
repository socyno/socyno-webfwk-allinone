package com.weimob.webfwk.module.release.change;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChangeRequestFormDetail extends ChangeRequestFormSimple {
    
    @Attributes(title = "变更类型详情")
    private AbsChangeDetail changeDetail;
    
    public String genDeployItemName() {
        return String.format("%s-%s-%s-%d.txt", getReleaseId().getReleaseId(), getChangeType(),
                getCategory().getOptionValue(), getId());
    }
    
}
