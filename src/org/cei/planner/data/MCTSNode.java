package org.cei.planner.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javaff.data.Action;
import javaff.planning.Filter;
import javaff.planning.NullFilter;
import javaff.planning.State;

public class MCTSNode implements Comparable<MCTSNode> {

	private static int NODE_COUNT = 0;

	private State state = null;
	private StateValuePolicyEnum stateValuePolicy = null;
	private MCTSNode parent = null;
	private Map<MCTSNode, Integer> successors = null;
	private Map<MCTSNode, Double> successor_QValues = null;
	private double value = 0.0;
	private Filter filter = NullFilter.getInstance();
	private int visitCount = 0;
	private int selectedCount = 0;

	private MCTSNode() {
		NODE_COUNT++;
	}

	/**
	 * Root node constructor
	 * @param state
	 * @param stateValuePolicyEnum
	 */
	public MCTSNode(State state, StateValuePolicyEnum stateValuePolicy) {
		this();
		this.state = state;
		this.stateValuePolicy = stateValuePolicy;
	}

	public MCTSNode(State state, MCTSNode parent, StateValuePolicyEnum stateValuePolicyEnum) {
		this(state, stateValuePolicyEnum);
		this.parent = parent;
	}
	
	public void visited() {
		visitCount++;
	}

	public int getVisits() {
		return visitCount;
	}
	
	public void selected() {
		selectedCount++;
	}
	
	@Override
	protected void finalize() {
		NODE_COUNT--;
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

	public double getQValue() {
		double qValue = 0.0;
		int notSelected = visitCount - selectedCount;
		if ((notSelected + selectedCount) == 0) {
			qValue = 0.0;
		} else {
			qValue = -(notSelected) / (notSelected + selectedCount);
		}
		return qValue;
	}
	
	public double getHValue() {
		return state.getHValue().doubleValue();
	}

	public State getState() {
		return state;
	}
	
	public List<Action> getActions() {
		return new ArrayList<Action>(filter.getActions(state));
	}

	public Map<MCTSNode, Integer> getSuccessors() {
		if (successors == null) {
			generateSuccessors();
		}
		return successors;
	}

	public Map<MCTSNode, Double> getSuccessorQValues() {
		if (successor_QValues == null) {
			generateSuccessors();
		}
		return successor_QValues;
	}

	public void visited(MCTSNode node) {
		if (successors.containsKey(node)) {
			Integer visitCount = successors.get(node) + 1;
			successors.put(node, visitCount);
		}
	}

	private void generateSuccessors() {
		Set<State> nextStates = state.getNextStates(filter.getActions(state));
		successors = new HashMap<>();
		successor_QValues = new HashMap<>();
		for (State nextState : nextStates) {
			MCTSNode successor = new MCTSNode(nextState, this, stateValuePolicy);
			successors.put(successor, Integer.valueOf(0));
			successor_QValues.put(successor, Double.valueOf(0.0));
		}
	}
	
	public void clearSuccesors() {
		successors = null;
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

	@Override
	public int compareTo(MCTSNode other) {
		if (value > other.value) {
			return -1;
		} else if (value < other.value) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof MCTSNode) {
			MCTSNode otherNode = (MCTSNode)other;
			return this.state.equals(otherNode.state);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.state.hashCode();
	}
}
