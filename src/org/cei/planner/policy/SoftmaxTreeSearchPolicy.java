package org.cei.planner.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cei.planner.data.MCTSNode;
import org.cei.planner.util.SoftmaxSelectionHelper;

public class SoftmaxTreeSearchPolicy implements IPolicy {

	private static final Random RAND = new Random(System.nanoTime());
	 
	private SoftmaxSelectionHelper softmaxSelection = new SoftmaxSelectionHelper();
	private MCTSNode initialNode = null; 
	
	public SoftmaxTreeSearchPolicy(MCTSNode node) {
		this.initialNode = node;
	}
	
	@Override
	public MCTSNode call() throws Exception {
		return search(initialNode);
	}
	
	private MCTSNode search(MCTSNode node) {
		Map<MCTSNode, Boolean> successors = node.getSuccessors();
		List<MCTSNode> unexplored = getUnexploredSuccessors(successors);
		// If there are unexplored add them to the tree
		if (!unexplored.isEmpty()) {
			MCTSNode selected = unexplored.get(RAND.nextInt(unexplored.size()));
			return selected;
		}
		MCTSNode selectedNode = softmaxSelection.softmaxValueSelection(successors.keySet());
		return search(selectedNode);
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
}