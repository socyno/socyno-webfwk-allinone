package org.socyno.webfwk.module.release.change;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "其他变更", visibleSelector = { "ckother_create" }
                              , editableSelector = { "ckother_create" }
                              , requiredSelector = { "ckother_create" })
public class ChangeRequestForCkOtherCreation extends ChangeRequestSimple {

}
