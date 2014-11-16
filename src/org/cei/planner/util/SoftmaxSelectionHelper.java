package org.cei.planner.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.cei.planner.data.MCTSNode;

public class SoftmaxSelectionHelper {

	private static final double DEFAULT_TEMPERATURE = 0.01;
	private static final Random RAND = new Random(System.nanoTime());

	private double temperature = 0.0;

	public SoftmaxSelectionHelper () {
		this.temperature = DEFAULT_TEMPERATURE;
	}
	
	public SoftmaxSelectionHelper(double temperature) {
		this.temperature = temperature;
	}

	public MCTSNode softmaxValueSelection(Collection<MCTSNode> nodes) {
		double totalValue = getTotalValue(nodes);
		List<BanditArm> arms = getArms(nodes, totalValue, false);
		return select(arms);
	}
	
	public MCTSNode softmaxQValueSelection(Collection<MCTSNode> nodes) {
		double totalQValue = getTotalQValue(nodes);
		List<BanditArm> arms = getArms(nodes, totalQValue, true);
		return select(arms);
	}

	private double getTotalQValue(Collection<MCTSNode> nodes) {
		double totalQ = 0.0;
		for (MCTSNode node : nodes) {
			totalQ += Math.exp(node.getQValue() / temperature);
		}
		return totalQ;
	}

	private double getTotalValue(Collection<MCTSNode> nodes) {
		double totalValue = 0.0;
		for (MCTSNode node : nodes) {
			totalValue += Math.exp(node.getValue() / temperature);
		}
		return totalValue;
	}
	
	private MCTSNode select(List<BanditArm> arms) {
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

	private List<BanditArm> getArms(Collection<MCTSNode> nodes,
			double totalValue, boolean useQValue) {
		List<BanditArm> arms = new ArrayList<BanditArm>();
		for (MCTSNode node : nodes) {
			//Determine which value to use
			double value = node.getValue();
			if (useQValue) {
				value = node.getQValue();
			}
			//normalise value
			double normalisedProbability = Math.exp(value
					/ temperature)
					/ totalValue;
			//Create arm
			BanditArm arm = new BanditArm(normalisedProbability, node);
			arms.add(arm);
		}
		return arms;
	}
}

class BanditArm implements Comparable<BanditArm> {

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
