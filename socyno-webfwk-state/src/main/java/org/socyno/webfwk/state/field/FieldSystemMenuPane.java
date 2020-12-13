package org.socyno.webfwk.state.field;

import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.module.menu.SystemMenuItemService;

import com.github.reinert.jjschema.v1.FieldType;

public class FieldSystemMenuPane extends FieldType {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.STATIC;
    }
    
    /**
     *  SELECT
     *      p.id,
     *      p.name
     *  FROM
     *      system_menu_pane p
     * 
     */
    @Multiline
    private static final String SQL_QUERY_MENU_PANE_OPTIONS = "X";
    
    @Override
    public List<OptionSystemMenuPane> getStaticOptions() throws Exception {
        return SystemMenuItemService.getInstance().getFormBaseDao().queryAsList(OptionSystemMenuPane.class,
                String.format("%s ORDER BY p.`order` ASC", SQL_QUERY_MENU_PANE_OPTIONS));
    }
    
    public static OptionSystemMenuPane getOption(Long paneId) throws Exception {
        List<OptionSystemMenuPane> list;
        if (paneId == null
                || (list = SystemMenuItemService.getInstance().getFormBaseDao().queryAsList(OptionSystemMenuPane.class,
                        String.format("%s WHERE p.id = %s", SQL_QUERY_MENU_PANE_OPTIONS, paneId))) == null
                || list.size() <= 0) {
            return null;
            
        }
        return list.get(0);
    }
}
