package org.cei.planner.mcrw;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javaff.data.GroundProblem;
import javaff.data.Plan;

import org.cei.planner.IPlanner;
import org.cei.planner.data.MCTSNode;
import org.cei.planner.executor.ExecutorFactory;
import org.cei.planner.policy.IPolicy;
import org.cei.planner.policy.PureRandomWalk;

public class MCRWPlanner implements IPlanner{

	public static final int DEFAULT_MAX_ITERATIONS = 7;
	
	private static ExecutorService EXECUTOR = ExecutorFactory.getExecutor();
	private static final Logger LOG = Logger.getLogger(MCRWPlanner.class.getName());
	
	private int maxIterations = DEFAULT_MAX_ITERATIONS;
	private Class<? extends PureRandomWalk> walkPolicy = null;
	
	public MCRWPlanner(Class<? extends PureRandomWalk> walkPolicy) {
		this.walkPolicy = walkPolicy;
	}
	
	public MCRWPlanner(Class<? extends PureRandomWalk> walkPolicy, int maxIterations) {
		this(walkPolicy);
		this.maxIterations = maxIterations;
	}

	public static Logger getLog() {
		return LOG;
	}
		
	@Override
	public Plan solve(GroundProblem problem) throws Exception {
		long startTime = System.nanoTime();
		
		LOG.config("Max iterations set to " + maxIterations);
		// Initial state
		MCTSNode initialNode = new MCTSNode(problem.getMetricInitialState(), walkPolicy.getConstructor(MCTSNode.class).newInstance(new MCTSNode(null, null)).getStateValuePolicy());
		
		MCTSNode currentNode = initialNode;
		
		int iterations = 0;
		double hMin = currentNode.getHValue();
		while (!currentNode.isGoal()) {
			if (iterations > maxIterations) {
				LOG.info("Reached max iterations of " + maxIterations);
				currentNode = initialNode;
				iterations = 0;			
			} else if (currentNode.isTerminal()) { 
				LOG.info("Reached dead end after " + iterations + " iterations");
				currentNode = initialNode;
				iterations = 0;
			}
			IPolicy walkPolicy = this.walkPolicy.getConstructor(MCTSNode.class).newInstance(currentNode);
			Future<MCTSNode> promise = EXECUTOR.submit(walkPolicy);
			currentNode = promise.get();
			if (currentNode.getHValue() < hMin) {
				LOG.info("There have been " + ++iterations + " iterations of MCRW before finding a better h-value of " + currentNode.getValue());
				hMin = currentNode.getHValue();
				iterations = 0;
			} else {
				iterations++;
			}
		}

		LOG.info("Solution found in " + ((System.nanoTime() - startTime) * Math.pow(10, -6))
				+ " ms.");
		return currentNode.getState().getSolution();		
	}
}
