package com.weimob.webfwk.util.tmpl;

import com.jfinal.template.expr.ast.Array;
import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.tool.Base64Util;

import java.io.UnsupportedEncodingException;

public class EnjoyStringExpand {
    public String base64Encode(String self) {
        return Base64Util.encode(self.getBytes());
    }

    public String urlEncode(String self) throws UnsupportedEncodingException {
        return HttpUtil.urlEncode(self);
    }

    public String format(String self, Array.ArrayListExt args) {
        return String.format(self,args.toArray());
    }
}
