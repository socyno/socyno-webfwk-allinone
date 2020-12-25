package com.weimob.webfwk.module.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.weimob.webfwk.module.release.build.FieldBuildService;
import com.weimob.webfwk.module.subsystem.FieldSubsystemAccessable;
import com.weimob.webfwk.module.subsystem.SubsystemFormSimple;
import com.weimob.webfwk.module.vcs.common.VcsType;
import com.weimob.webfwk.state.abs.AbstractStateFormBase;
import com.weimob.webfwk.state.util.StateFormBasicSaved;
import com.weimob.webfwk.util.state.field.FieldText;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApplicationFormSimple extends StateFormBasicSaved implements AbstractStateFormBase, ApplicationFormAbstract {
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
    
    @Attributes(title = "业务系统", type = FieldSubsystemAccessable.class)
    private SubsystemFormSimple subsystem;
    
    @Attributes(title = "应用类型", type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "源码仓库类型", type = FieldOptionsVcsType.class)
    private String vcsType;
    
    @Attributes(title = "构建版本")
    private String buildVersion;
    
    @Attributes(title = "代码仓库")
    private String vcsPath;
    
    @Attributes(title = "发布分支")
    private String releaseBranch;
    
    @Attributes(title = "是否收藏")
    private Boolean bookmarked;
    
    @Attributes(title = "构建服务", type = FieldBuildService.class)
    private String buildService;
    
    @Attributes(title = "代码分级", type = FieldOptionsCodeLevel.class)
    private String codeLevel;

    @Getter
    public enum ApplicationType {
        
        APPLICATION("application", "服务端"),
        CLIENT("client", "客户端"),
        LIBRARY("library", "组件库"),
        STATIC("static", "静态资源");
        
        private final String value;
        
        private final String display;
        
        ApplicationType(String value, String display) {
            this.value = value;
            this.display = display;
        }
        
        public static ApplicationType get(String value) {
            ApplicationType result = null;
            for (ApplicationType r : ApplicationType.values()) {
                if (r.getValue().equalsIgnoreCase(value) || r.name().equalsIgnoreCase(value)) {
                    result = r;
                    break;
                }
            }
            return result;
        }
        
        public static String getDisplayOrValue(String value) {
            ApplicationType result;
            if ((result = get(value)) == null) {
                return value;
            }
            return result.getDisplay();
        }
    }
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return ApplicationService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    public static class FieldOptionsCodeLevel extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                add(FieldSimpleOption.create("10:red", "红"));
                add(FieldSimpleOption.create("20:yellow", "黄"));
                add(FieldSimpleOption.create("30:blue", "蓝"));
            }
        };
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }
    
    public static class FieldOptionsBuildService extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                add(FieldSimpleOption.create("custom", "custom"));
                add(FieldSimpleOption.create("maven", "maven"));
            }
        };
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    public static class FieldOptionsYesOrNo extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                add(FieldSimpleOption.create("0", "否"));
                add(FieldSimpleOption.create("1", "是"));
            }
        };
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    public static class FieldOptionsApplicationType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                for (ApplicationType applicationType : ApplicationType.values()) {
                    add(FieldSimpleOption.create(applicationType.getValue(), applicationType.getDisplay()));
                }
            }
        };
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    public static class FieldOptionsVcsType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {
            {
                for (VcsType vcsType : VcsType.values()) {
                    add(FieldSimpleOption.create(vcsType.name()));
                }
            }
        };
        
        @Override
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
}
