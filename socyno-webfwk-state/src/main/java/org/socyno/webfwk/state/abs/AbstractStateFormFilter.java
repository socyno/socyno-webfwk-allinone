package org.socyno.webfwk.state.abs;

import java.util.List;

public interface AbstractStateFormFilter<F extends AbstractStateFormBase> {
    
    public List<F> apply(Class<F> clazz) throws Exception;

}
