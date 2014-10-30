package chess.ai.montecarlotreesearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import chess.ChessGame;
import chess.ChessResult;
import chess.ChessTeam;
import chess.ai.AdversarialSearchAlgorithm;
import chess.move.Move;

public class MCTS implements AdversarialSearchAlgorithm {

	
	private static final long DEFAULT_RUNNING_TIME = 10000000000L;
	private static final double DEFAULT_TEMPERATURE = 0.01;
	private static final double DEFAULT_LEARNING_RATE = 0.01;
	private static final Random RAND = new Random(System.nanoTime());
	
	private long runningTime = DEFAULT_RUNNING_TIME;
	private double temperature = DEFAULT_TEMPERATURE;
	private double learningRate = DEFAULT_LEARNING_RATE;
	
	public MCTS() {
	}

	public MCTS(long runningTime, double temperature) {
		this.runningTime = runningTime;
		this.temperature = temperature;
	}

	@Override
	public Move getMove(ChessGame aGame) {
		long startTime = System.nanoTime();
		long runningTime = 0;
		ChessGame game = aGame.clone();
		MCTSNode initialState = new MCTSNode(game);
		ChessTeam team = game.getMovingTeam();
		long rollouts = 0;
		while (runningTime <= this.runningTime) {
			MCTSNode newNode = treePolicy(initialState);
			System.out.println(rollouts + ": Tree Policy Completed");
			double value = defaultPolicy(newNode, team);
			System.out.println(rollouts + ": Rollout Completed. Value: " + value);
			backup(newNode, value);
			System.out.println(rollouts + ": Backup Completed");
			newNode.getParent().setExplored(newNode);
			runningTime = System.nanoTime() - startTime;
			rollouts++;
		}
		System.out.println(rollouts + " rollouts in " + (System.nanoTime() - startTime) + "ns");
		System.out.println(MCTSNode.getNodeCount() + " nodes explored.");
		return bestSuccessor(initialState);
	}

	private Move bestSuccessor(MCTSNode node) {
		List<MCTSNode> successors = getSuccessorList(node.getSuccessors());
		Collections.sort(successors);
		return successors.get(0).getMove();
	}

	private MCTSNode treePolicy(MCTSNode state) {
		Map<MCTSNode, Boolean> successors = state.getSuccessors();
		List<MCTSNode> unexplored = getUnexploredSuccessors(successors);
		// If there are unexplored add them to the tree
		if (!unexplored.isEmpty()) {
			MCTSNode selected = unexplored.get(RAND.nextInt(unexplored.size()));
			return selected;
		}
		MCTSNode selectedNode = banditSelection(successors);		
		return treePolicy(selectedNode);
	}

	private double defaultPolicy(MCTSNode startNode, ChessTeam team) {
		MCTSNode currentNode = startNode;
		while (currentNode.getResult().equals(ChessResult.IN_PROGRESS)) {
			System.out.println(currentNode.getGameState().getMovingTeam().getTeamColour() + " to move.");
			System.out.println(currentNode.getGameState().getBoard());
			int successor = 0;
			try {
				successor = RAND.nextInt(currentNode.getSuccessors().size());
			} catch (IllegalArgumentException e) {
				System.out.println("Err-----");
				System.out.println(currentNode.getGameState().getMovingTeam().getTeamColour() + " to move.");
				System.out.println(currentNode.getGameState().getBoard());
			}
			List<MCTSNode> successors = getSuccessorList(currentNode.getSuccessors());
			System.out.println(successors.size());
			currentNode = successors.get(successor);
		}	
		System.out.println(currentNode.getGameState().getMovingTeam().getTeamColour() + " to move.");
		System.out.println(currentNode.getGameState().getBoard());
		return currentNode.getValue(team);
	}
	
	private void backup(MCTSNode node, double value) {
		while (node.getParent() != null) {
			node.setValue(node.getValue() + (learningRate * (value - node.getValue())));
			node = node.getParent();
		}
		
	}

	private List<MCTSNode> getSuccessorList(Map<MCTSNode, Boolean> successors) {
		List<MCTSNode> successorList = new ArrayList<MCTSNode>();
		for (MCTSNode node : successors.keySet()) {
			successorList.add(node);
		}
		return successorList;
	}

	private MCTSNode banditSelection(Map<MCTSNode, Boolean> successors) {
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
			double normalisedProbability = Math.exp(node.getValue() / temperature) / totalValue;
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
