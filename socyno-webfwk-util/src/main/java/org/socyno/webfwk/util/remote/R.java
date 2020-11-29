package org.socyno.webfwk.util.remote;

public class R extends R0<R> {

    public R() {
        super();
    }
    
    public R(String message) {
        super(message);
    }
    
    public R(int status, String message) {
        super(status, message);
    }
    
    public static R ok() {
        return new R();
    }

    public static R ok(String message) {
        return new R(message);
    }

    public static R error() {
        return error("Internal Server Error");
    }

    public static R error(String message) {
        return new R(1, message);
    }
}
