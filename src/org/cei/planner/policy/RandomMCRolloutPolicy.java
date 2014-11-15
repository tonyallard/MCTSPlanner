package org.cei.planner.policy;

import java.util.List;
import java.util.Random;

import javaff.data.Action;
import javaff.planning.State;

import org.cei.planner.data.MCTSNode;
import org.cei.planner.data.StateValuePolicyEnum;

public class RandomMCRolloutPolicy implements IPolicy {

	private static final Random RAND = new Random(System.nanoTime());
	private static final StateValuePolicyEnum STATE_VALUE_POLICY = StateValuePolicyEnum.WIN_LOSS_STATE;
	
	private MCTSNode initialNode = null; 
	private Long maxIterations = null;
	
	public RandomMCRolloutPolicy(MCTSNode node) {
		this.initialNode = node;
	}
	
	public RandomMCRolloutPolicy(MCTSNode node, Long maxIterations) {
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
			MCTSNode nextNode = new MCTSNode(nextState, currentNode, STATE_VALUE_POLICY);
			currentNode = nextNode;
			iterations++;
		}
		currentNode.setValue(currentNode.isGoal() ? 1.0 : 0.0);
		return currentNode;
	}
}
