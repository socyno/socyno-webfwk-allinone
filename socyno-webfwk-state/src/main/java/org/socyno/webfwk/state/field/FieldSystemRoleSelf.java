package org.socyno.webfwk.state.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.util.state.field.FieldTableView;

public class FieldSystemRoleSelf extends FieldTableView {

    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }

    /**
     * 覆盖父类的方法，根据当前账户检索系统角色
     * @param keyword
     * @return
     * @throws Exception
     */
    public static List<OptionSystemRole> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        Map<Long, OptionSystemRole> currentUserRoles = PermissionService.getMySystemRoleEntities();
        return new ArrayList<>(currentUserRoles.values());
    }
}
