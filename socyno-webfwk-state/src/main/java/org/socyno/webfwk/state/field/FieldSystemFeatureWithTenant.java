package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.socyno.webfwk.state.module.feature.SystemFeatureService;
import org.socyno.webfwk.state.module.feature.SystemFeatureFormSimple;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.ConvertUtil;

public class FieldSystemFeatureWithTenant extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     * 覆盖父类的方法，根据关键字检索系统功能
     * @param keyword
     * @return
     * @throws Exception
     */
    public List<OptionSystemFeature> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        PagedList<SystemFeatureFormSimple> list;
        if ((list = SystemFeatureService.getInstance().queryWithTenant(SessionContext.getTenant(), filter.getKeyword(),
                1, 1000)) == null || list.getList() == null) {
            return Collections.emptyList();
        }
        List<OptionSystemFeature> options = new ArrayList<>();
        for (SystemFeatureFormSimple l : list.getList()) {
            if (l == null) {
                continue;
            }
            options.add(new OptionSystemFeature().setId(l.getId()).setCode(l.getCode()).setName(l.getName()));
        }
        return options.stream().sorted(Comparator.comparing(OptionSystemFeature::getOptionDisplay))
                .collect(Collectors.toList());
    }
    
    /**
     * 覆盖父类的方法，根据选项值检索系统功能
     * @param values
     * @return
     * @throws Exception
     */
    @Override
    public List<OptionSystemFeature> queryDynamicValues(Object[] values) throws Exception {
        Long[] ids;
        List<SystemFeatureFormSimple> features = null;
        if (values == null || values.length <= 0
                || (ids = ConvertUtil.asNonNullUniqueLongArray((Object[]) values)).length <= 0
                || (features = SystemFeatureService.getInstance().queryByIdsWithTenant(SystemFeatureFormSimple.class,
                        SessionContext.getTenant(), ids)) == null
                || features.isEmpty()) {
            return Collections.emptyList();
        }
        List<OptionSystemFeature> options = new ArrayList<>();
        for (SystemFeatureFormSimple feature : features) {
            options.add(new OptionSystemFeature().setId(feature.getId()).setName(feature.getName())
                    .setCode(feature.getCode()));
        }
        return options.stream().sorted(Comparator.comparing(OptionSystemFeature::getOptionDisplay))
                .collect(Collectors.toList());
    }
}
