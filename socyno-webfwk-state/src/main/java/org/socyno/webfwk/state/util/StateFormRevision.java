package org.socyno.webfwk.state.util;

import java.util.Date;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StateFormRevision {
    private Long id;
    private String state;
    private long revision;
    private Date updatedAt;
    private Long updatedBy;
    private String updatedCodeBy;
    private String updatedNameBy;
    private Date createdAt;
    private Long createdBy;
    private String createdCodeBy;
    private String createdNameBy;
}
