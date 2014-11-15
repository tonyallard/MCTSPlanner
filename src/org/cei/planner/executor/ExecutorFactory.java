package org.cei.planner.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorFactory {
	
	private static ExecutorService EXECUTOR = null;
	
	private ExecutorFactory(){
		//static class
	}
	
	public static ExecutorService getExecutor() {
		if (EXECUTOR == null) {
			EXECUTOR = Executors.newCachedThreadPool();
		}
		return EXECUTOR;
	}
}
