package org.socyno.webfwk.module.productline;

import java.util.List;

import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessors;
import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductlineFormDetail extends ProductlineFormDefault implements ProductlineWithSubsystems {
    
    @Attributes(title = "业务系统清单", type = FieldSubsystemAccessors.class)
    private List<SubsystemFormSimple> subsystems;
    
}
