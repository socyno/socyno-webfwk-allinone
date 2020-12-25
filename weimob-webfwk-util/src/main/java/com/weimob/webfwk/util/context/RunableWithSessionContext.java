package com.weimob.webfwk.util.context;

public abstract class RunableWithSessionContext implements Runnable {
	
	private final UserContext userContext;

	public RunableWithSessionContext() {
		userContext = SessionContext.getUserContext();
	}
    
	public abstract void exec();
	
    @Override
    public final void run() {
        SessionContext.setUserContext(userContext);
        exec();
    }
    
}