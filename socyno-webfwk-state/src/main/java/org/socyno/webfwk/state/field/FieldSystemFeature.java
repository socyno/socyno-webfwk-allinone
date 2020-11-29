package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.socyno.webfwk.state.module.feature.SystemFeatureService;
import org.socyno.webfwk.state.module.feature.SystemFeatureSimple;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.ConvertUtil;

public class FieldSystemFeature extends FieldTableView {
    
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
        PagedList<SystemFeatureSimple> list;
        if ((list = SystemFeatureService.query(filter.getKeyword(), 1, 1000)) == null || list.getList() == null) {
            return Collections.emptyList();
        }
        List<OptionSystemFeature> options = new ArrayList<>();
        for (SystemFeatureSimple f : list.getList()) {
            if (f == null) {
                continue;
            }
            options.add(new OptionSystemFeature().setId(f.getId()).setCode(f.getCode()).setName(f.getName()));
        }
        return options.stream().sorted(Comparator.comparing(OptionSystemFeature::getOptionDisplay)).collect(Collectors.toList());
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
        List<SystemFeatureSimple> features = null;
        if (values == null || values.length <= 0
                || (ids = ConvertUtil.asNonNullUniqueLongArray((Object[])values)).length <= 0
                || (features = SystemFeatureService.queryByIds(SystemFeatureSimple.class, ids)) == null || features.isEmpty()) {
            return Collections.emptyList();
        }
        List<OptionSystemFeature> options = new ArrayList<>();
        for (SystemFeatureSimple feature : features) {
            options.add(new OptionSystemFeature().setId(feature.getId())
                            .setName(feature.getName()).setCode(feature.getCode()));
        }
        return options.stream().sorted(Comparator.comparing(OptionSystemFeature::getOptionDisplay))
                        .collect(Collectors.toList());
    }
}
