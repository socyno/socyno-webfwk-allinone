package com.weimob.webfwk.util.tool;

import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.weimob.webfwk.util.context.SessionContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleLogger {
    
    public static void logDebug(OutputStream outputStream, String format, Object... args) {
        log(outputStream, "DEBUG", format, args);
    }
    
    public static void logInfo(OutputStream outputStream, String format, Object... args) {
        log(outputStream, "INFO", format, args);
    }
    
    public static void logWarn(OutputStream outputStream, String format, Object... args) {
        log(outputStream, "WARN", format, args);
    }
    
    public static void logError(OutputStream outputStream, String format, Object... args) {
        log(outputStream, "ERROR", format, args);
    }
    
    protected static void log(OutputStream outputStream, String level, String format, Object... args) {
        String result = CommonUtil.ifNull(format, "");
        try {
            result = String.format(format, args);
        } catch (Exception ex) {
        }
        try {
            String prefix = String.format("%s [%s][%s]", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"),
                    SessionContext.getTokenUsername(), level);
            for (String line : result.replaceAll("\r\n", "\n").replaceFirst("[\r\n]$", "").split("[\r\n]")) {
                IOUtils.write(String.format("%s - %s%n", prefix, line), outputStream, "UTF-8");
            }
        } catch (Exception ex) {
            log.warn(ex.toString(), ex);
        }
    }
}
