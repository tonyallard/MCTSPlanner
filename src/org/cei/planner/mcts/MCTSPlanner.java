package org.cei.planner.mcts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javaff.data.GroundProblem;
import javaff.data.Plan;
import javaff.data.TotalOrderPlan;
import javaff.planning.STRIPSState;
import javaff.planning.State;

import org.cei.planner.IPlanner;

public class MCTSPlanner implements IPlanner {

	@Override
	public Plan solve(GroundProblem problem) {
		State rootNode = problem.getSTRIPSInitialState();
		Map<State, Set<State>> stateSpace = new HashMap<>();
		stateSpace.put(rootNode, new HashSet<State>());
		
		//Selection
		MCTSNode selectedNode = getNextInterestingNode(rootNode, stateSpace);
		//Expansion
		//Simulation
		//Backpropogation
		return new TotalOrderPlan();
	}

	private MCTSNode getNextInterestingNode(State rootNode, Map<State, Set<State>> stateSpace) {
		State currentNode = rootNode;
		Set<State> nextStates = currentNode.getNextStates(currentNode.getActions());
		
	}

}
