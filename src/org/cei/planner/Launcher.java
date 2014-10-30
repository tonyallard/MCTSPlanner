package org.cei.planner;

public class Launcher {

	public static void main(String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"Usage MCTSPlanner domainFile problemFile");
		}
		
	}
}
