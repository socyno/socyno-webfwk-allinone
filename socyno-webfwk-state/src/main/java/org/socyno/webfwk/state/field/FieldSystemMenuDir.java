package org.socyno.webfwk.state.field;

import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.module.menu.SystemMenuItemService;

import com.github.reinert.jjschema.v1.FieldType;

public class FieldSystemMenuDir extends FieldType {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.STATIC;
    }
    
    /**
     *  SELECT
     *      d.id, d.name, p.name as pane
     *  FROM
     *      system_menu_dir d,
     *      system_menu_pane p 
     *  WHERE
     *      d.pane_id = p.id
     *  %s
     *  ORDER BY p.`order` ASC, d.`order` ASC
     * 
     */
    @Multiline
    private static final String SQL_QUERY_MENU_DIR_OPTIONS = "X";
    
    @Override
    public List<OptionSystemMenuDir> getStaticOptions() throws Exception {
        return SystemMenuItemService.getInstance().getFormBaseDao().queryAsList(OptionSystemMenuDir.class,
                String.format(SQL_QUERY_MENU_DIR_OPTIONS, ""));
    }
    
    public static OptionSystemMenuDir getOption(Long dirId) throws Exception {
        List<OptionSystemMenuDir> list;
        if (dirId == null
                || (list = SystemMenuItemService.getInstance().getFormBaseDao().queryAsList(OptionSystemMenuDir.class,
                        String.format(SQL_QUERY_MENU_DIR_OPTIONS, String.format(" AND d.id = %s ", dirId)))) == null
                || list.size() <= 0) {
            return null;
            
        }
        return list.get(0);
    }
}
