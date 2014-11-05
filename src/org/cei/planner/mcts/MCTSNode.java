package org.cei.planner.mcts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javaff.planning.Filter;
import javaff.planning.NullFilter;
import javaff.planning.State;

public class MCTSNode implements Comparable<MCTSNode> {

	private static int NODE_COUNT = 0;
	private static StateValuePolicyEnum stateValuePolicy = StateValuePolicyEnum.WIN_LOSS;

	private State state = null;
	private MCTSNode parent = null;
	private Map<MCTSNode, Boolean> successors = null;
	private double value = 0.0;
	private Filter filter = NullFilter.getInstance();

	private MCTSNode() {
		NODE_COUNT++;
	}

	public MCTSNode(State state) { // root node constructor
		this();
		this.state = state;
	}

	public MCTSNode(State state, MCTSNode parent) {
		this(state);
		this.parent = parent;
	}
	
	@Override
	protected void finalize() {
		NODE_COUNT--;
	}
	
	public static void setStateValuePolicy(StateValuePolicyEnum stateValuePolicy) {
		MCTSNode.stateValuePolicy = stateValuePolicy;
	}

	public static int getNodeCount() {
		return NODE_COUNT;
	}

	public MCTSNode getParent() {
		return parent;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public State getState() {
		return state;
	}

	public Map<MCTSNode, Boolean> getSuccessors() {
		if (successors == null) {
			generateSuccessors();
		}
		return successors;
	}

	public void setExplored(MCTSNode node) {
		if (successors.containsKey(node)) {
			successors.put(node, Boolean.TRUE);
		}
	}

	private void generateSuccessors() {
		Set<State> nextStates = state.getNextStates(filter.getActions(state));
		successors = new HashMap<>();
		for (State nextState : nextStates) {
			MCTSNode successor = new MCTSNode(nextState, this);
			successors.put(successor, Boolean.FALSE);
		}
	}
	
	public void clearSuccesors() {
		successors = null;
	}

	@Override
	public int compareTo(MCTSNode other) {
		if (value > other.value) {
			return -1;
		} else if (value < other.value) {
			return 1;
		}
		return 0;
	}

	public boolean isTerminal() {
		if (state.getActions().isEmpty() || isGoal()) {
			return true;
		}
		return false;
	}

	public boolean isGoal() {
		return state.goalReached();
	}

	public void calculateValue() {
		if (stateValuePolicy == StateValuePolicyEnum.WIN_LOSS) {
			value = isGoal() ? 1.0 : 0.0;
		}
		
	}
}
