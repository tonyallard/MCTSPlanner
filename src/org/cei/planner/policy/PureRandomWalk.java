package org.cei.planner.policy;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javaff.data.Action;
import javaff.planning.State;

import org.cei.planner.data.MCTSNode;
import org.cei.planner.data.StateValuePolicyEnum;

public class PureRandomWalk implements IPolicy {

	public static final int DEFAULT_NUM_WALKS = 2000;
	public static final int DEFAULT_LENGTH_WALK = 10;
	public static final double DEFAULT_ALPHA = 0.9;
	public static final double DEFAULT_EXTENDING_RATE = 1.5;

	protected static final Random RAND = new Random(System.nanoTime());

	protected static final Logger LOG = Logger.getLogger(PureRandomWalk.class
			.getName());
	private static final StateValuePolicyEnum STATE_VALUE_POLICY = StateValuePolicyEnum.H_VALUE;

	protected MCTSNode initialNode = null;
	protected int numWalks = DEFAULT_NUM_WALKS;
	private int lengthWalk = DEFAULT_LENGTH_WALK;
	private double alpha = DEFAULT_ALPHA;
	private double extendingRate = DEFAULT_EXTENDING_RATE;

	// record keeping
	protected double hMin = 0.0;
	protected double oldHMin = 0.0;
	private double acceptableProgress = 0.0;
	protected int walkLength = 0;

	public PureRandomWalk(MCTSNode node) {
		this.initialNode = node;
	}

	public PureRandomWalk(MCTSNode node, int numWalks, int lengthWalk,
			double alpha, double extendingRate) {
		this(node);
		this.numWalks = numWalks;
		this.lengthWalk = lengthWalk;
		this.alpha = alpha;
		this.extendingRate = extendingRate;
	}

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
				if (currentNode.isGoal()) {
					return currentNode;
				}
			}
			if (hasMadeAcceptableProgress(currentNode)) {
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
		return hMinNode;
	}

	protected void initialiseTempVars() {
		this.hMin = Double.MAX_VALUE;
		this.oldHMin = hMin;
		this.acceptableProgress = Math.max(0, hMin - initialNode.getHValue());
		this.walkLength = lengthWalk;
		
	}

	protected void performIterativeDeepening() {
		// Iterative Deepening
		if (hMin < oldHMin) {
			oldHMin = hMin;
			walkLength = lengthWalk;
		} else {
			walkLength *= extendingRate;
		}
	}

	protected boolean hasMadeAcceptableProgress(MCTSNode node) {
		// Only need to calculate at end points of walk
		double progress = Math.max(0, hMin - node.getHValue());
		if (progress > acceptableProgress) {
			LOG.fine("Acceptable Progress Made of: " + progress);
			return true;
		}
		acceptableProgress = ((1 - alpha) * acceptableProgress)
				+ (alpha * progress);
		LOG.fine("Acceptable Progress benchmark set to: " + acceptableProgress);
		return false;
	}

	protected MCTSNode getSuccessor(MCTSNode node) {
		List<Action> actions = node.getActions();
		Action nextAction = actions.get(RAND.nextInt(actions.size()));
		State nextState = node.getState().apply(nextAction);
		MCTSNode nextNode = new MCTSNode(nextState, node, STATE_VALUE_POLICY);
		node = nextNode;
		return node;
	}
}
