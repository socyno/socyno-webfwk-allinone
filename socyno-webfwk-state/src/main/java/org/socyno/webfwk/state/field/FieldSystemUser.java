package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.module.user.SystemUserFormSimple;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.ConvertUtil;

import lombok.NonNull;

public class FieldSystemUser extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    public <T extends OptionSystemUser> List<T> queryDynamicOptions(@NonNull Class<T> clazz, FilterBasicKeyword filter)
            throws Exception {
        PagedList<SystemUserFormSimple> users;
        if ((users = SystemUserService.DEFAULT.queryByNameLike(filter.getKeyword(), false, 1L, 100)) == null || users.getList() == null) {
            return null;
        }
        List<T> simpleUsers = new ArrayList<>();
        for (SystemUserFormSimple user : users.getList()) {
            T option = (T)clazz.newInstance();
            option.setId(user.getId());
            option.setDisplay(user.getDisplay());
            option.setUsername(user.getUsername());
            option.setState(user.getState());
            option.setMailAddress(user.getMailAddress());
            simpleUsers.add(option);
        }
        return simpleUsers.stream().sorted(Comparator.comparing(T::getOptionDisplay)).collect(Collectors.toList());
    }
    
    public <T extends OptionSystemUser> List<T> queryDynamicValues(@NonNull Class<T> clazz, Object[] values, boolean asCode) throws Exception {
        if (values == null || values.length <= 0) {
            return Collections.emptyList();
        }
        List<SystemUserFormSimple> users = null;
        if (asCode) {
            String[] names;
            if ((names = ConvertUtil.asNonBlankUniqueTrimedStringArray((Object[])values)).length <= 0) {
                return Collections.emptyList();
            }
            users = SystemUserService.DEFAULT.queryByUsernames(SystemUserFormSimple.class, names);
        } else {
            Long[] userIds;
            if ((userIds = ConvertUtil.asNonNullUniqueLongArray((Object[])values)).length <= 0) {
                return Collections.emptyList();
            }
            users = SystemUserService.DEFAULT.queryByUserIds(SystemUserFormSimple.class, userIds);
        }
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> simpleUsers = new ArrayList<>();
        for (SystemUserFormSimple user : users) {
            T option = (T)clazz.newInstance();
            option.setId(user.getId());
            option.setDisplay(user.getDisplay());
            option.setUsername(user.getUsername());
            option.setState(user.getState());
            option.setMailAddress(user.getMailAddress());
            simpleUsers.add(option);
        }
        return simpleUsers.stream().sorted(Comparator.comparing(T::getOptionDisplay))
                        .collect(Collectors.toList());
    }
    
    public OptionSystemUser queryDynamicValue(Long userId) throws Exception {
        List<OptionSystemUser> users;
        if ((users = queryDynamicValues(new Long[] {userId})) == null || users.size() != 1) {
            return null;
        }
        return users.get(0);
    }
    
    /**
     * 覆盖父类的方法，根据关键字检索系统用户
     * @param keyword
     * @return
     * @throws Exception
     */
    public List<OptionSystemUser> queryDynamicOptions(FilterBasicKeyword filter)  throws Exception {
        return queryDynamicOptions(OptionSystemUser.class, filter);
    }
    
    /**
     * 覆盖父类的方法，根据选项值检索系统用户
     * @param values
     * @return
     * @throws Exception
     */
    @Override
    public List<OptionSystemUser> queryDynamicValues(Object[] values) throws Exception {
        return queryDynamicValues(OptionSystemUser.class, values, false);
    }
}
