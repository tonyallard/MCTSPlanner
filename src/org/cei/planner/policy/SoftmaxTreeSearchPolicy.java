package org.cei.planner.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cei.planner.data.MCTSNode;

public class SoftmaxTreeSearchPolicy implements IPolicy {

	private static final double DEFAULT_TEMPERATURE = 0.01;
	private static final Random RAND = new Random(System.nanoTime());
	
	private MCTSNode initialNode = null; 
	private double temperature = DEFAULT_TEMPERATURE;
	
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
		MCTSNode selectedNode = softmaxSelection(successors);
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
	
	private MCTSNode softmaxSelection(Map<MCTSNode, Boolean> successors) {
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
}

class BanditArm implements Comparable<BanditArm>{
	
	private double probability = 0.0;
	private MCTSNode node = null;
	
	public BanditArm(double probability, MCTSNode node) {
		super();
		this.probability = probability;
		this.node = node;
	}

	public double getProbability() {
		return probability;
	}

	public MCTSNode getNode() {
		return node;
	}

	@Override
	public int compareTo(BanditArm other) {	
		if (probability > other.probability) {
			return -1;
		} else if (probability < other.probability) {
			return 1;
		}
		return 0;
	}
}