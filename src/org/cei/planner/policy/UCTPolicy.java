package org.cei.planner.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cei.planner.data.MCTSNode;
import org.cei.planner.util.UCTSelectionHelper;

public class UCTPolicy implements IPolicy {

	private static final Random RAND = new Random(System.nanoTime());
	 
	private UCTSelectionHelper uct = new UCTSelectionHelper();
	private MCTSNode initialNode = null; 
	
	public UCTPolicy(MCTSNode node) {
		this.initialNode = node;
	}
	
	@Override
	public MCTSNode call() throws Exception {
		return search(initialNode);
	}
	
	private MCTSNode search(MCTSNode node) {
		//increment node visit count n(s)
		node.visited();
		Map<MCTSNode, Integer> successors = node.getSuccessors();
		List<MCTSNode> unexplored = getUnexploredSuccessors(successors);
		// If there are unexplored add them to the tree
		if (!unexplored.isEmpty()) {
			MCTSNode selectedNode = unexplored.get(RAND.nextInt(unexplored.size()));
			//Increment action visit count n(s,a)
			node.visited(selectedNode);
			return selectedNode;
		}
		
		MCTSNode selectedNode = uct.select(node);
		//Increment action visit count n(s,a)
		node.visited(selectedNode);
		return search(selectedNode);
	}

	private List<MCTSNode> getUnexploredSuccessors(
			Map<MCTSNode, Integer> successors) {
		List<MCTSNode> unexplored = new ArrayList<MCTSNode>();
		for (MCTSNode node : successors.keySet()) {
			if (successors.get(node) == 0) {
				unexplored.add(node);
			}
		}
		return unexplored;
	}
}
