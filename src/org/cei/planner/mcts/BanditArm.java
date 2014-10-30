package chess.ai.montecarlotreesearch;

public class BanditArm implements Comparable<BanditArm>{
	
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
