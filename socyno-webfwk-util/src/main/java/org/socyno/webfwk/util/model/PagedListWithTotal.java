package org.socyno.webfwk.util.model;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PagedListWithTotal<F> implements PagedList0<F>  {
    private long page;
    private int  limit;
    private long total;
    private List<F> list ;
}
