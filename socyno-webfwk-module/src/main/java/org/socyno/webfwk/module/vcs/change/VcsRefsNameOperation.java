package org.socyno.webfwk.module.vcs.change;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.socyno.webfwk.module.application.ApplicationAbstractForm;
import org.socyno.webfwk.util.tool.StringUtils;

@Getter
@Setter
public class VcsRefsNameOperation {
    
    public static enum RefsOpType {
        Update
        , Create
        , Delete
        , ForceUpdate
        ,Query;
    }
    
    private final String application;
    private final String vcsPath;
    private final String vcsRefsName;
    private RefsOpType refsOpType;
    private ApplicationAbstractForm applicationForm;
    
    public VcsRefsNameOperation(String application, String vcsPath, String vcsRefsName, RefsOpType refsOpType) {
        this(application, vcsPath, vcsRefsName, refsOpType, null);
    }
    
    public VcsRefsNameOperation(String application, String vcsPath, String vcsRefsName, RefsOpType refsOpType,
            ApplicationAbstractForm applicationForm) {
        this.vcsPath = vcsPath;
        this.vcsRefsName = vcsRefsName;
        this.application = application;
        this.refsOpType = refsOpType;
        this.applicationForm = applicationForm;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getApplication()).append(getVcsRefsName()).toHashCode();
    }
    
    @Override
    public boolean equals(Object anotherObject) {
        if (anotherObject == null) {
            return false;
        }
        if (this == anotherObject) {
            return true;
        }
        if (anotherObject.getClass() == this.getClass()) {
            VcsRefsNameOperation anotherThis = (VcsRefsNameOperation) anotherObject;
            if (StringUtils.equals(getApplication(), anotherThis.getApplication())
                    && StringUtils.equals(getVcsRefsName(), anotherThis.getVcsRefsName())) {
                return true;
            }
        }
        return false;
    }
}