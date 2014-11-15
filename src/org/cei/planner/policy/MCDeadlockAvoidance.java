package org.cei.planner.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cei.planner.data.MCTSNode;
import org.cei.planner.data.StateValuePolicyEnum;
import org.cei.planner.util.SoftmaxSelectionHelper;

public class MCDeadlockAvoidance extends PureRandomWalk {

	private static final StateValuePolicyEnum STATE_VALUE_POLICY = StateValuePolicyEnum.WIN_LOSS_ACTION;
		
	private SoftmaxSelectionHelper softmaxSelection = new SoftmaxSelectionHelper();
	private Map<MCTSNode, MCTSNode> visited = null;
	
	public MCDeadlockAvoidance(MCTSNode node) {
		super(node);
		this.visited = new HashMap<>();
	}

	public MCDeadlockAvoidance(MCTSNode node, int numWalks, int lengthWalk, double alpha, double extendingRate) {
		super(node, numWalks, lengthWalk, alpha, extendingRate);
	}
	
	@Override
	public StateValuePolicyEnum getStateValuePolicy() {
		return STATE_VALUE_POLICY;
	}
	
	@Override
	public MCTSNode call() throws Exception {
		initialiseTempVars();
		MCTSNode hMinNode = null;
		for (int i = 0; i < numWalks; i++) {
			MCTSNode currentNode = initialNode;
			for (int j = 0; j < walkLength; j++) {
				currentNode = getSuccessor(currentNode);
				currentNode.visited();
				if (currentNode.isGoal()) {
					return currentNode;
				}
			}
			if (hasMadeAcceptableProgress(currentNode)) {
				currentNode.selected();
				return currentNode;
			}

			if (currentNode.getHValue() < hMin) {
				hMin = currentNode.getHValue();
				hMinNode = currentNode;
				LOG.fine("Random Walk found better state after " + i
						+ " walks.");
			}
			performIterativeDeepening();
		}
		if (hMinNode == null) {
			return initialNode;
		}
		hMinNode.selected();
		return hMinNode;
	}
	
	@Override
	protected void initialiseTempVars() {
		super.initialiseTempVars();
		this.visited = new HashMap<>();
		visited.put(initialNode, initialNode);
	}

	@Override
	protected MCTSNode getSuccessor(MCTSNode node) {
		List<MCTSNode> successors = new ArrayList<>(node.getSuccessors().keySet());
		successors = removeDuplicates(successors);
		MCTSNode successor = softmaxSelection.softmaxQValueSelection(successors);
		return successor;
	}

	protected List<MCTSNode> removeDuplicates(List<MCTSNode> successors) {
		List<MCTSNode> uniqueSuccessors = new ArrayList<>();
		for (MCTSNode successor : successors) {
			MCTSNode visitedNode = visited.get(successor);
			if (visitedNode != null) {
				uniqueSuccessors.add(visitedNode);
			} else {
				uniqueSuccessors.add(successor);
				visited.put(successor, successor);
			}
		}
		return uniqueSuccessors;
	}

}
