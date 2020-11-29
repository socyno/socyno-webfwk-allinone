package org.socyno.webfwk.state.basic;

import java.util.List;

public interface AbstractStateFormFilter<F extends AbstractStateForm> {
    
    public List<F> apply(Class<F> clazz) throws Exception;

}
