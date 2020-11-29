package org.socyno.webfwk.state.basic;

public interface AbstractStateFormTotalFilter<F extends AbstractStateForm> extends AbstractStateFormFilter<F>{
    public int getTotal() throws Exception ;
}
