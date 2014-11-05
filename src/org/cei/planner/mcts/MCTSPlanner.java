package org.cei.planner.mcts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javaff.data.GroundProblem;
import javaff.data.Plan;

import org.cei.planner.IPlanner;
import org.cei.planner.mcts.policy.DefaultMCPolicy;
import org.cei.planner.mcts.policy.IPolicy;

public class MCTSPlanner implements IPlanner {

	private static final long DEFAULT_RUNNING_TIME = 10 * (long) Math.pow(10, 9); // seconds
	private static final double DEFAULT_TEMPERATURE = 0.01;
	private static final double DEFAULT_LEARNING_RATE = 0.01;
	private static final Random RAND = new Random(System.nanoTime());

	private long runningTime = DEFAULT_RUNNING_TIME;
	private double temperature = DEFAULT_TEMPERATURE;
	private double learningRate = DEFAULT_LEARNING_RATE;

	private static final Logger LOG = Logger.getLogger(MCTSPlanner.class.getName());

	public MCTSPlanner() {
	}

	public MCTSPlanner(long runningTime, double temperature, double learningRate) {
		this();
		this.runningTime = runningTime;
		this.temperature = temperature;
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
		MCTSNode currentNode = new MCTSNode(problem.getSTRIPSInitialState());
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
			MCTSNode newNode = treePolicy(initialNode);
			LOG.fine("Tree Policy Completed. " + "Rolling out from node with value: " + newNode.getValue());
			MCTSNode terminalNode = defaultPolicy(newNode);
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

	private MCTSNode treePolicy(MCTSNode node) {
		Map<MCTSNode, Boolean> successors = node.getSuccessors();
		List<MCTSNode> unexplored = getUnexploredSuccessors(successors);
		// If there are unexplored add them to the tree
		if (!unexplored.isEmpty()) {
			MCTSNode selected = unexplored.get(RAND.nextInt(unexplored.size()));
			return selected;
		}
		MCTSNode selectedNode = banditSelection(successors);
		return treePolicy(selectedNode);
	}

	private MCTSNode defaultPolicy(MCTSNode node) throws InterruptedException,
			ExecutionException {
		ExecutorService executor = Executors.newCachedThreadPool();
		IPolicy policy = new DefaultMCPolicy(node);
		Future<MCTSNode> terminalState = executor.submit(policy);
		return terminalState.get();
	}

	private void backup(MCTSNode node, double value) {
		while (node.getParent() != null) {
			node.setValue(node.getValue()
					+ (learningRate * (value - node.getValue())));
			node = node.getParent();
		}

	}

	private MCTSNode banditSelection(Map<MCTSNode, Boolean> successors) {
		double totalValue = getTotalValue(successors);
		List<BanditArm> arms = getArms(successors, totalValue);
		Collections.sort(arms);
		double selection = RAND.nextDouble();
		for (BanditArm arm : arms) {
			selection -= arm.getProbability();
			if (selection <= 0) {
				return arm.getNode();
			}
		}
		return null;
	}

	private List<BanditArm> getArms(Map<MCTSNode, Boolean> successors,
			double totalValue) {
		List<BanditArm> arms = new ArrayList<BanditArm>();
		for (MCTSNode node : successors.keySet()) {
			double normalisedProbability = Math.exp(node.getValue()
					/ temperature)
					/ totalValue;
			BanditArm arm = new BanditArm(normalisedProbability, node);
			arms.add(arm);
		}
		return arms;
	}

	private double getTotalValue(Map<MCTSNode, Boolean> successors) {
		double totalValue = 0.0;
		for (MCTSNode node : successors.keySet()) {
			totalValue += Math.exp(node.getValue() / temperature);
		}
		return totalValue;
	}

	private List<MCTSNode> getUnexploredSuccessors(
			Map<MCTSNode, Boolean> successors) {
		List<MCTSNode> unexplored = new ArrayList<MCTSNode>();
		for (MCTSNode node : successors.keySet()) {
			if (!successors.get(node)) {
				unexplored.add(node);
			}
		}
		return unexplored;
	}

	private MCTSNode bestSuccessor(MCTSNode node) {
		List<MCTSNode> successors = new ArrayList<>(node.getSuccessors()
				.keySet());
		Collections.sort(successors);
		return successors.get(0);
	}
}
