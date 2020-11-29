package org.socyno.webfwk.module.app.form;

import com.github.reinert.jjschema.v1.FieldOption;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.ConvertUtil;

import java.util.Collections;
import java.util.List;

public class FieldApplication extends FieldTableView {
    
    public static class OptionApplication extends ApplicationFormSimple implements FieldOption {
        @Override
        public String getOptionValue() {
            return "" + getId();
        }
        
        @Override
        public String getOptionDisplay() {
            return getName();
        }
        
        @Override
        public String getOptionGroup() {
            return getSubsystemName();
        }
        
        @Override
        public void setOptionValue(String value) {
            setId(new Long(value));
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && OptionApplication.class.equals(obj.getClass()) &&
                    this.getOptionValue().equals(((OptionApplication) obj).getOptionValue());
        }

        @Override
        public int hashCode() {
            return getOptionValue().hashCode();
        }
    }
    
    public static <T extends OptionApplication> List<T> queryWithAllOptions(@NonNull Class<T> clazz,
            final FilterBasicKeyword filter, final boolean offlineIncluded) throws Exception {
        return ApplicationService.DEFAULT.list(clazz, new ApplicationListAllQuery(50, 1L)
                .setNamelike(filter.getKeyword()).setSortByNameAsc(true).setOfflineIncluded(offlineIncluded)).getList();
    }
    
    public static <T extends OptionApplication> List<T> queryOnlyVisibleOptions(@NonNull Class<T> clazz,
            final FilterBasicKeyword filter, final boolean offlineIncluded) throws Exception {
        return ApplicationService.DEFAULT.list(clazz, new ApplicationListDefaultQuery(50, 1L)
                .setNamelike(filter.getKeyword()).setSortByNameAsc(true).setOfflineIncluded(offlineIncluded)).getList();
    }
    
    public static <T extends OptionApplication> List<T> queryDynamicValues(@NonNull Class<T> clazz, Object[] values,
            boolean asName) throws Exception {
        final Object[] uniquedValues = asName ? ConvertUtil.asNonBlankUniqueTrimedStringArray(values)
                : ConvertUtil.asNonNullUniqueLongArray((Object[]) values);
        if (uniquedValues == null || uniquedValues.length <= 0) {
            return null;
        }
        if (asName) {
            return ApplicationService.DEFAULT
                    .list(clazz,
                            new ApplicationListAllQuery(uniquedValues.length, 1L)
                                    .setAppNamesIn(StringUtils.join(uniquedValues, ',')).setSortByNameAsc(true))
                    .getList();
        }
        return ApplicationService.DEFAULT.list(clazz, new ApplicationListAllQuery(uniquedValues.length, 1L)
                .setAppIdsIn(StringUtils.join(uniquedValues, ',')).setSortByNameAsc(true)).getList();
    }
    
    public static <T extends OptionApplication> List<T> queryValuesBySubsystemIds(@NonNull Class<T> clazz,
            boolean offlineIncluded, Long... subsystemIds) throws Exception {
        final long[] subsystemPrimIds;
        if ((subsystemPrimIds = ConvertUtil.asNonNullUniquePrimitiveLongArray((Object[]) subsystemIds)) == null
                || subsystemPrimIds.length <= 0) {
            return Collections.emptyList();
        }
        
        return ApplicationService.DEFAULT
                .list(clazz,
                        new ApplicationListAllQuery(50000, 1L).setOfflineIncluded(offlineIncluded)
                                .setSubsystemIdsIn(StringUtils.join(subsystemPrimIds, ',')).setSortByNameAsc(true))
                .getList();
    }
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    public OptionApplication queryDynamicValue(Long id) throws Exception {
        if (id == null) {
            return null;
        }
        List<OptionApplication> list;
        if ((list = queryDynamicValues(OptionApplication.class, new Object[] { id }, false)) == null
                || list.size() != 1) {
            return null;
        }
        return list.get(0);
    }
    
    public List<? extends OptionApplication> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return queryOnlyVisibleOptions(OptionApplication.class, filter, false);
    }
    
    @Override
    public List<? extends OptionApplication> queryDynamicValues(Object[] values) throws Exception {
        return queryDynamicValues(OptionApplication.class, values, false);
    }
}
