package org.socyno.webfwk.state.field;

import java.util.List;

import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.ClassUtil;

public class FieldSystemUsername extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    public OptionSystemUsername queryDynamicValue(String username) throws Exception {
        List<OptionSystemUsername> users;
        if ((users = queryDynamicValues(new Object[] { username })) == null || users.size() != 1) {
            return null;
        }
        return users.get(0);
    }
    
    /**
     * 覆盖父类的方法，根据关键字检索系统用户
     * 
     * @param keyword
     * @return
     * @throws Exception
     */
    public List<OptionSystemUsername> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return ClassUtil.getSingltonInstance(FieldSystemUser.class).queryDynamicOptions(OptionSystemUsername.class,
                filter);
    }
    
    /**
     * 覆盖父类的方法，根据选项值检索系统用户
     * 
     * @param values
     * @return
     * @throws Exception
     */
    @Override
    public List<OptionSystemUsername> queryDynamicValues(Object[] values) throws Exception {
        return ClassUtil.getSingltonInstance(FieldSystemUser.class).queryDynamicValues(OptionSystemUsername.class,
                values, true);
    }
}
