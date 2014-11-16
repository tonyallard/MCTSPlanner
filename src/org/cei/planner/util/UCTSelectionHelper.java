package org.cei.planner.util;

import java.util.Map;

import org.cei.planner.data.MCTSNode;

public class UCTSelectionHelper {

	private static final double EXPLORATION_CONSTANT_DEFAULT = 0.1;

	private double explorationConstant = 0.0;

	public UCTSelectionHelper() {
		this.explorationConstant = EXPLORATION_CONSTANT_DEFAULT;
	}

	public UCTSelectionHelper(double explorationConstant) {
		this.explorationConstant = explorationConstant;
	}

	public MCTSNode select(MCTSNode node) {
		Map<MCTSNode, Integer> successors = node.getSuccessors();
		Map<MCTSNode, Double> successor_QValues = node.getSuccessorQValues();
		// determine Q-values
		double bestQValue = -Double.MAX_VALUE;
		MCTSNode bestSuccessor = null;
		for (MCTSNode successor : successors.keySet()) {
			double qValue = successor_QValues.get(successor);
			double successorVisits = successors.get(successor);
			int nodeVisits = node.getVisits();
			qValue = qValue
					+ (explorationConstant * Math.sqrt(Math.log(nodeVisits)
							/ successorVisits));
			successor_QValues.put(successor, Double.valueOf(qValue));
			if ((bestSuccessor == null) || (qValue > bestQValue)) {
				bestSuccessor = successor;
				bestQValue = qValue;
			}
		}
		return bestSuccessor;
	}
}
