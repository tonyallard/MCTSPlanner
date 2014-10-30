package chess.ai.montecarlotreesearch;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import chess.ChessGame;
import chess.ChessResult;
import chess.ChessTeam;
import chess.exception.IllegalMoveException;
import chess.move.Move;

public class MCTSNode implements Comparable<MCTSNode> {
	
	private static int NODE_COUNT = 0;

	private ChessGame gameState = null;
	private MCTSNode parent = null;
	private Move move = null;
	private Map<MCTSNode, Boolean> successors = null;
	private double value = 0.0;
	private ChessResult result = null;

	public MCTSNode(ChessGame gameState) { // root node constructor
		this.gameState = gameState.clone();
		this.result = this.gameState.getResult();
		NODE_COUNT++;
	}

	public MCTSNode(ChessGame gameState, MCTSNode parent, Move move) {
		this(gameState);
		this.move = move;
		this.parent = parent;
	}
	
	public static int getNodeCount() {
		return NODE_COUNT;
	}

	public MCTSNode getParent() {
		return parent;
	}

	public Move getMove() {
		return move;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public double getValue(ChessTeam team) {
		if (result.equals(ChessResult.DRAW)) {
			return 0;
		}
		if (gameState.getMovingTeam() == team) {
			return -1;
		}
		return 1;
	}

	public ChessGame getGameState() {
		return gameState;
	}

	public ChessResult getResult() {
		return result;
	}

	public Map<MCTSNode, Boolean> getSuccessors() {
		if (successors == null) {
			generateSuccessors();
		}
		return successors;
	}

	public void setExplored(MCTSNode node) {
		if (successors.containsKey(node)) {
			successors.put(node, Boolean.TRUE);
		}
	}

	public void generateSuccessors() {
		successors = new IdentityHashMap<MCTSNode, Boolean>();
		List<Move> moves = gameState.getPossibleMoves(gameState.getMovingTeam());
		for (Move move : moves) {
			ChessTeam team = gameState.getMovingTeam();
			try {
				gameState.applyMove(move);
			} catch (IllegalMoveException e) {
				continue;
			}
			successors.put(new MCTSNode(gameState, this, move), Boolean.FALSE);
			gameState.unapplyMove(move);
			if (team != gameState.getMovingTeam())
			{
				throw new RuntimeException("Something went wrong");
			}
		}
		
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
}
