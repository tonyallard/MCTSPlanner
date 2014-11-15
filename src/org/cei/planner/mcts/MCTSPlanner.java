package org.cei.planner.mcts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javaff.data.GroundProblem;
import javaff.data.Plan;

import org.cei.planner.IPlanner;
import org.cei.planner.data.MCTSNode;
import org.cei.planner.data.StateValuePolicyEnum;
import org.cei.planner.executor.ExecutorFactory;
import org.cei.planner.policy.IPolicy;
import org.cei.planner.policy.RandomMCRolloutPolicy;
import org.cei.planner.policy.SoftmaxTreeSearchPolicy;

public class MCTSPlanner implements IPlanner {

	private static final long DEFAULT_RUNNING_TIME = 10 * (long) Math.pow(10, 9); // seconds
	private static final double DEFAULT_LEARNING_RATE = 0.01;
	private static ExecutorService EXECUTOR = ExecutorFactory.getExecutor();
	private static final Logger LOG = Logger.getLogger(MCTSPlanner.class.getName());

	private long runningTime = DEFAULT_RUNNING_TIME;
	private double learningRate = DEFAULT_LEARNING_RATE;

	public MCTSPlanner() {
	}

	public MCTSPlanner(long runningTime, double learningRate) {
		this();
		this.runningTime = runningTime;
		this.learningRate = learningRate;
	}

	public static final Logger getLog() {
		return LOG;
	}

	@Override
	public Plan solve(GroundProblem problem) throws InterruptedException,
			ExecutionException {
		long startTime = System.nanoTime();
		int iterationsMCTS = 0;
		LOG.config("Iteration running time set to " + (this.runningTime * Math.pow(10, -6)) + " ms.");
		// Initialise root state as current
		MCTSNode currentNode = new MCTSNode(problem.getSTRIPSInitialState(), StateValuePolicyEnum.H_VALUE);
		while (!currentNode.isTerminal()) {
			MCTSNode nextState = runMCTSIteration(currentNode);
			currentNode = nextState;
			LOG.info("There have been " + ++iterationsMCTS + " iterations of MCTS");
		}
		LOG.info("Solution found in " + ((System.nanoTime() - startTime) * Math.pow(10, -6))
				+ " ms.");
		return currentNode.getState().getSolution();
	}

	private MCTSNode runMCTSIteration(MCTSNode initialNode)
			throws InterruptedException, ExecutionException {
		long iterationStartTime = System.nanoTime();
		long runningTime = 0;

		long rollouts = 0;

		while (runningTime <= this.runningTime) {
			MCTSNode newNode = runTreeSearchPolicy(initialNode);
			LOG.fine("Tree Policy Completed. " + "Rolling out from node with value: " + newNode.getValue());
			MCTSNode terminalNode = runRolloutPolicy(newNode);
			LOG.fine("Rollout Completed. Terminal Node Value: "
					+ terminalNode.getValue());
			backup(newNode, terminalNode.getValue());
			LOG.fine("Backup Completed. " + "New Node Value: " + newNode.getValue());
			newNode.getParent().setExplored(newNode);
			runningTime = System.nanoTime() - iterationStartTime;
			rollouts++;
		}
		LOG.info(rollouts + " rollouts completed in "
				+ ((System.nanoTime() - iterationStartTime) * Math.pow(10, -6)) + "ms. " + MCTSNode.getNodeCount() + " nodes explored.");
		return bestSuccessor(initialNode);
	}

	private MCTSNode runTreeSearchPolicy(MCTSNode node) throws InterruptedException, ExecutionException {
		IPolicy searchPolicy = new SoftmaxTreeSearchPolicy(node);
		Future<MCTSNode> newNode = EXECUTOR.submit(searchPolicy);
		return newNode.get();
	}

	private MCTSNode runRolloutPolicy(MCTSNode node) throws InterruptedException,
			ExecutionException {
		IPolicy rolloutPolicy = new RandomMCRolloutPolicy(node);
		Future<MCTSNode> terminalState = EXECUTOR.submit(rolloutPolicy);
		return terminalState.get();
	}

	private void backup(MCTSNode node, double value) {
		while (node.getParent() != null) {
			node.setValue(node.getValue()
					+ (learningRate * (value - node.getValue())));
			node = node.getParent();
		}

	}

	private MCTSNode bestSuccessor(MCTSNode node) {
		List<MCTSNode> successors = new ArrayList<>(node.getSuccessors()
				.keySet());
		Collections.sort(successors);
		return successors.get(0);
	}
}
