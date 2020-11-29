package org.socyno.webfwk.util.vcs.gitlab;

import java.util.Date;

import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import com.google.gson.JsonElement;

import lombok.Data;

@Data
public class GitlabLogEntry {
    
    private String type;
    
    private String fileName;
    
    private String commitPath;
    
    private EntryCommit commit;
    
    @Data
    public static class EntryCommit {
        
        private String id;
        
        private String message;
        
        private Date authoredDate;
        
        private String authorName;
        
        private String authorEmail;
        
        private Date committedDate;
        
        private String committerName;
        
        private String committerEmail;
    }
    
    public static GitlabLogEntry fromJson(JsonElement json) throws IllegalJsonDataException {
        GitlabLogEntry entry;
        if ((entry = CommonUtil.fromJsonByFieldNamingPolicy(json, GitlabLogEntry.class)) == null
                || StringUtils.isBlank(entry.getType()) || StringUtils.isBlank(entry.getFileName())
                || entry.getCommit() == null || StringUtils.isBlank(entry.getCommit().getMessage())
                || StringUtils.isBlank(entry.getCommit().getId()) || entry.getCommit().getCommittedDate() == null) {
            throw new IllegalJsonDataException();
        }
        return entry;
    }
}
