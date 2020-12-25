package com.weimob.webfwk.module.release.mobstore;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.util.StateFormBasicInput;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "移动应用市场设置")
public class ReleaseMobileStoreFormCreate extends StateFormBasicInput  {

    @Attributes(title = "商店名", required = true, position = 1001)
    private String storeName ;

    @Attributes(title = "渠道包名", position = 1002)
    private String channelName ;
}
