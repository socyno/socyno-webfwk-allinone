package com.weimob.webfwk.util.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PagedList<F> implements PagedList0<F> {
    private long page;
    private int  limit;
    private List<F> list ;
}
