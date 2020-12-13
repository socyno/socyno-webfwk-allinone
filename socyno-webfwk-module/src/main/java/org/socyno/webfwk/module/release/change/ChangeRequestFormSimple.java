package org.socyno.webfwk.module.release.change;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Data;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.socyno.webfwk.module.app.form.FieldApplication.OptionApplication;
import org.socyno.webfwk.module.release.change.FieldChangeRequestCategory.OptionChangeCategory;
import org.socyno.webfwk.module.release.change.FieldChangeRequestReleaseId.OptionReleaseId;
import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.model.CommonFormAttachement;
import org.socyno.webfwk.util.state.field.FieldFormAttachements;

@Data
public class ChangeRequestFormSimple implements AbstractStateForm {
    
    @Getter
    public enum ChangeType {
        CheckListOther("ck-other", "其他变更", false);
        private final String code;
        private final String display;
        private final boolean needDbaApproval;
        
        ChangeType(String code, String display, boolean needDbaApproval) {
            this.code = code;
            this.display = display;
            this.needDbaApproval = needDbaApproval;
        }
        
        public static ChangeType get(String changeType) {
            for (ChangeType v : ChangeType.values()) {
                if (v.getCode().equalsIgnoreCase(changeType) || v.name().equalsIgnoreCase(changeType)) {
                    return v;
                }
            }
            return null;
        }
    }
    
    @Getter
    public enum Category {
        Standard("sys-manually", "系统组协助部署", ChangeType.CheckListOther),
        DTSChange("dev-menually", "研发自行人工部署)", ChangeType.CheckListOther);
        
        private final String code;
        private final String display;
        private final ChangeType[] changeTypes;
        
        Category(String code, String display, ChangeType... changeTypes) {
            this.code = code;
            this.display = display;
            this.changeTypes = changeTypes;
        }
        
        public boolean containsChangeType(ChangeType changeType) {
            for (ChangeType type : getChangeTypes()) {
                if (changeType.equals(type)) {
                    return true;
                }
            }
            return false;
        }
        
        public static Category get(String category) {
            for (Category v : Category.values()) {
                if (v.getCode().equalsIgnoreCase(category) || v.name().equalsIgnoreCase(category)) {
                    return v;
                }
            }
            return null;
        }
        
        public static List<Category> get(ChangeType changeType) {
            if (changeType == null) {
                return Collections.emptyList();
            }
            List<Category> result = new LinkedList<>();
            for (Category v : Category.values()) {
                for (ChangeType type : v.getChangeTypes()) {
                    if (changeType.equals(type)) {
                        result.add(v);
                        break;
                    }
                }
            }
            return Collections.unmodifiableList(result);
        }
    }
    
    @Getter
    public enum ScopeType {
        Production("production", "生产环境", false),
        Stage02("stage02", "集成环境", true),
        ProdAndStage02("prod&stage02", "集成和生产", true);

        private final String code;
        private final String display;
        private final boolean needDeployIntegration;
        
        ScopeType(String code, String display, boolean needDeployIntegration) {
            this.code = code;
            this.display = display;
            this.needDeployIntegration = needDeployIntegration;
        }
        
        public static ScopeType get(String scopeType) {
            for (ScopeType v : ScopeType.values()) {
                if (v.getCode().equalsIgnoreCase(scopeType) || v.name().equalsIgnoreCase(scopeType)) {
                    return v;
                }
            }
            return null;
        }
        
        public static boolean stage02Included(String scopeType) {
            ScopeType type;
            if ((type = get(scopeType)) == null) {
                return false;
            }
            return type.equals(Stage02) || type.equals(ProdAndStage02);
        }
        
        public static boolean productionIncluded(String scopeType) {
            ScopeType type;
            if ((type = get(scopeType)) == null) {
                return false;
            }
            return type.equals(Production) || type.equals(ProdAndStage02);
        }
    }
    
    /**
     * 是否可部署集成环境
     */
    public boolean stage02NeedToDeploy() {
        return ScopeType.stage02Included(getScopeType());
    }
    
    /**
     * 是否可部署生产环境
     */
    public boolean productionNeedToDeploy() {
        return ScopeType.productionIncluded(getScopeType());
    }
    
    @Attributes(title = "变更单号")
    private Long id;
    
    @Attributes(title = "上线项目", type = FieldChangeRequestReleaseIdMine.class
                , visibleTags = { "ckother_create", "kafka_create" }
                , editableTags = { "ckother_create", "kafka_create" }
                , requiredTags = { "ckother_create", "kafka_create" })
    private OptionReleaseId releaseId;
    
    @Attributes(title = "变更标题", visibleTags = { "ckother_create" }
                , editableTags = { "ckother_create" }
                , requiredTags = { "ckother_create" })
    private String title;
    
    @Attributes(title = "变更类型", type = FieldOptionsChangeType.class
                , visibleTags = { "ckother_create", "kafka_create" }
                , editableTags = { "" }
                , requiredTags = { "ckother_create", "kafka_create" })
    private String changeType;
    
    @Attributes(title = "适用范围", type = FieldOptionsScopeType.class
                , visibleTags = { "ckother_create", "kafka_create" }
                , editableTags = { "ckother_create", "kafka_create" }
                , requiredTags = { "ckother_create", "kafka_create" })
    private String scopeType;
    
    @Attributes(title = "创建人", type = FieldSystemUser.class)
    private OptionSystemUser createdBy;
    
    @Attributes(title = "创建时间")
    private String createdAt;
    
    @Attributes(title = "变更描述"
            , visibleTags = { "ckother_create", "kafka_create" }
            , editableTags = { "ckother_create", "kafka_create" }
            , requiredTags = { "ckother_create" })
    private String description;
    
    @Attributes(title = "部署项名称")
    private String deployItemName;
    
    @Attributes(title = "变更分类", type = FieldChangeRequestCategory.class
            , visibleTags = { "ckother_create", "kafka_create" }
            , editableTags = { "ckother_create" }
            , requiredTags = { "ckother_create", "kafka_create" })
    private OptionChangeCategory category;
    
    @Attributes(title = "变更应用", type = FieldChangeRequestApplication.class
            , visibleTags = { "ckother_create" }
            , editableTags = { "ckother_create" }
            , requiredTags = { })
    private OptionApplication[] applications;
    
    @Attributes(title = "附件", type = FieldFormAttachements.class
            , visibleTags = { "ckother_create" }
            , editableTags = { "ckother_create" }
            , requiredTags = { })
    private CommonFormAttachement[] attachements;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本")
    private Long revision;
    
    public static class FieldOptionsState extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            return ChangeRequestService.getInstance().getStates();
        }
        
        @Override
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    public static class FieldOptionsChangeType extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            List<FieldOption> options = new LinkedList<>();
            for (ChangeType type : ChangeType.values()) {
                options.add(FieldSimpleOption.create(type.getCode(), type.getDisplay()));
            }
            return Collections.unmodifiableList(options);
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    public static class FieldOptionsScopeType extends FieldType {
        @Override
        public List<? extends FieldOption> getStaticOptions() {
            List<FieldOption> options = new LinkedList<>();
            for (ScopeType type : ScopeType.values()) {
                options.add(FieldSimpleOption.create(type.getCode(), type.getDisplay()));
            }
            return Collections.unmodifiableList(options);
        }
        
        @Override
        public FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
}
