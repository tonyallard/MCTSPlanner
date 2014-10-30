package org.cei.planner.mcts.util;

import javaff.data.Action;
import javaff.data.Condition;
import javaff.data.GroundProblem;
import javaff.data.metric.NamedFunction;
import javaff.data.strips.Proposition;

public class GroundProblemUtil {

	public static String toString(GroundProblem ground) {
		String output = "";
		String newLine = System.getProperty("line.separator");
		// Types
		output += "Function Values" + newLine;
		output += "----------" + newLine;
		for (NamedFunction func : ground.functionValues.keySet()) {
			output += func.toStringTyped() + " = "
					+ ground.functionValues.get(func).doubleValue();
			output += newLine;
		}
		output += newLine;

		// Constants
		output += "Action" + newLine;
		output += "----------" + newLine;
		for (Action action : ground.actions) {
			output += action.toString();
			output += newLine;
		}
		output += newLine;

		// Initial State
		output += "Initial State" + newLine;
		output += "----------" + newLine;
		for (Proposition prop : ground.initial) {
			output += prop.toStringTyped();
			output += newLine;
		}
		output += newLine;

		// Goal State
		output += "Goal State" + newLine;
		output += "----------" + newLine;
		for (Condition goal : ground.goal.getConditionalPropositions()) {
			output += goal.toStringTyped();
			output += newLine;
		}
		output += newLine;

		// Metric
		output += "Metric" + newLine;
		output += "----------" + newLine;
		if (ground.metric != null) {
			output += ground.metric.toStringTyped();
			output += newLine;
		}

		return output;
	}
}
