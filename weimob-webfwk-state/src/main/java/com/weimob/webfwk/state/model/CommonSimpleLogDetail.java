package com.weimob.webfwk.state.model;

import lombok.Data;

@Data
public class CommonSimpleLogDetail {
    
    private Long id;
    /**
     * 操作前对象
     */
    private String operateBefore;
    /**
     * 操作后对象
     */
    private String operateAfter;
}
