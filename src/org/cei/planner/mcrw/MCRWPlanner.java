package org.cei.planner.mcrw;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javaff.data.GroundProblem;
import javaff.data.Plan;

import org.cei.planner.IPlanner;
import org.cei.planner.data.MCTSNode;
import org.cei.planner.data.StateValuePolicyEnum;
import org.cei.planner.mcts.MCTSPlanner;
import org.cei.planner.policy.IPolicy;
import org.cei.planner.policy.RandomMCPolicy;

public class MCRWPlanner implements IPlanner{

	private static final int DEFAULT_MAX_ITERATIONS = 100;
	private static final int DEFAULT_NUM_WALKS = 10;
	private static final int DEFAULT_LENGTH_WALK = 7;
	
	private static ExecutorService EXECUTOR = Executors.newCachedThreadPool();
	private static final Logger LOG = Logger.getLogger(MCTSPlanner.class.getName());
	
	private int maxIterations = DEFAULT_MAX_ITERATIONS;
	private int numWalks = DEFAULT_NUM_WALKS;
	private int lengthWalk = DEFAULT_LENGTH_WALK;
	
	public MCRWPlanner() {
		
	}
	
	public MCRWPlanner(int maxIterations, int numWalks, int lengthWalk) {
		this.maxIterations = maxIterations;
		this.numWalks = numWalks;
		this.lengthWalk = lengthWalk;
	}

	public static Logger getLog() {
		return LOG;
	}
		
	@Override
	public Plan solve(GroundProblem problem) throws Exception {
		MCTSNode.setStateValuePolicy(StateValuePolicyEnum.H_VALUE);
		long startTime = System.nanoTime();
		
		LOG.config("Max iterations set to " + maxIterations);
		// Initial state
		MCTSNode initialNode = new MCTSNode(problem.getMetricInitialState());
		
		MCTSNode currentNode = initialNode;
		
		int iterations = 0;
		double hMin = currentNode.calculateValue();
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
			currentNode = MCRW(currentNode);
			if (currentNode.getValue() < hMin) {
				LOG.info("There have been " + iterations + " iterations of MCRW before finding a better h-value of " + currentNode.getValue());
				hMin = currentNode.getValue();
				iterations = 0;
			} else {
				iterations++;
			}
		}

		LOG.info("Solution found in " + ((System.nanoTime() - startTime) * Math.pow(10, -6))
				+ " ms.");
		return currentNode.getState().getSolution();		
	}

	private MCTSNode MCRW(MCTSNode node) throws InterruptedException, ExecutionException {
		double hMin = Double.MAX_VALUE;
		MCTSNode hMinNode = null;
		for (int i = 0; i < numWalks; i++) {
			MCTSNode currentNode = node;
			for (int j = 0; j < lengthWalk; j++) {
				IPolicy randomMCPolicy = new RandomMCPolicy(currentNode, 1L);
				Future<MCTSNode> future = EXECUTOR.submit(randomMCPolicy);
				MCTSNode nextState = future.get();
				if (nextState.isGoal()) {
					return nextState;
				}
				currentNode = nextState;
			}
			if (currentNode.getValue() < hMin) {
				hMin = currentNode.getValue();
				hMinNode = currentNode;
				LOG.fine("Random Walk found better state after " + i + " walks.");
			}
		}
		if (hMinNode == null) {
			return node;
		}
		return hMinNode;
	}
}
