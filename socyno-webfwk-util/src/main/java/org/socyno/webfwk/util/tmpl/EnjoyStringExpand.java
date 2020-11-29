package org.socyno.webfwk.util.tmpl;

import com.jfinal.template.expr.ast.Array;

import java.io.UnsupportedEncodingException;

import org.socyno.webfwk.util.remote.HttpUtil;
import org.socyno.webfwk.util.tool.Base64Util;

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
