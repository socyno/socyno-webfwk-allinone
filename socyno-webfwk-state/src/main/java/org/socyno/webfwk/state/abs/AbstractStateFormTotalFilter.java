package org.socyno.webfwk.state.abs;

public interface AbstractStateFormTotalFilter<F extends AbstractStateFormBase> extends AbstractStateFormFilter<F>{
    public int getTotal() throws Exception ;
}
