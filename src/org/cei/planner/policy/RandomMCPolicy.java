package org.cei.planner.policy;

import java.util.List;
import java.util.Random;

import javaff.data.Action;
import javaff.planning.State;

import org.cei.planner.data.MCTSNode;

public class RandomMCPolicy implements IPolicy {

	private static final Random RAND = new Random(System.nanoTime());
	
	private MCTSNode initialNode = null; 
	private Long maxIterations = null;
	public static long timeSpenth = 0;
	
	public RandomMCPolicy(MCTSNode node) {
		this.initialNode = node;
	}
	
	public RandomMCPolicy(MCTSNode node, Long maxIterations) {
		this(node);
		this.maxIterations = maxIterations;
	}
	
	@Override
	public MCTSNode call() throws Exception {
		MCTSNode currentNode = initialNode;
		long iterations = 0;
		while (!currentNode.isTerminal()) {
			if ((maxIterations != null) && (maxIterations.compareTo(iterations) <= 0)) {
				break;
			}			
			List<Action> actions = currentNode.getActions();
			Action nextAction = actions.get(RAND.nextInt(actions.size()));
			State nextState = currentNode.getState().apply(nextAction);
			MCTSNode nextNode = new MCTSNode(nextState, currentNode);
			currentNode = nextNode;
			iterations++;
		}
		long start = System.nanoTime();
		currentNode.calculateValue();
		timeSpenth += System.nanoTime() - start;
		return currentNode;
	}
}
