package org.socyno.webfwk.util.model;

import java.util.List;

public interface PagedList0<F> {
    public long getPage ();
    public int getLimit();
    public List<F> getList() ;
}
