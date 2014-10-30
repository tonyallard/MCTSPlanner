package org.cei.planner.mcts.util;

import javaff.data.Condition;
import javaff.data.UngroundProblem;
import javaff.data.metric.FunctionSymbol;
import javaff.data.strips.Operator;
import javaff.data.strips.PredicateSymbol;
import javaff.data.strips.Proposition;
import javaff.data.strips.SimpleType;
import javaff.data.strips.PDDLObject;

public class UngroundProblemUtil {

	public static String toString(UngroundProblem unground) {
		String output = "";
		String newLine = System.getProperty("line.separator");
		// Problem Title
		output += unground.DomainName + ": " + unground.ProblemName + newLine;
		output += "----------------------------------" + newLine;
		output += newLine;
		// Types
		output += "Types" + newLine;
		output += "----------" + newLine;
		for (SimpleType type : unground.types) {
			output += type.toString();
			output += newLine;
		}
		output += newLine;

		// Constants
		output += "Constants" + newLine;
		output += "----------" + newLine;
		for (PDDLObject aConst : unground.constants) {
			output += aConst.getName() + " - " + aConst.getType();
			output += newLine;
		}
		output += newLine;

		// Predicates
		output += "Predicates" + newLine;
		output += "----------" + newLine;
		for (PredicateSymbol pred : unground.predSymbols) {
			output += pred.toStringTyped();
			output += newLine;
		}
		output += newLine;

		// Functions
		output += "Functions" + newLine;
		output += "----------" + newLine;
		for (FunctionSymbol func : unground.funcSymbols) {
			output += func.toStringTyped();
			output += newLine;
		}
		output += newLine;

		// Actions
		output += "Operators" + newLine;
		output += "----------" + newLine;
		for (Operator op : unground.actions) {
			output += op.name + ": " + op.params;
			output += newLine;
		}
		output += newLine;

		// Objects
		output += "Objects" + newLine;
		output += "----------" + newLine;
		for (PDDLObject obj : unground.objects) {
			output += obj.toStringTyped();
			output += newLine;
		}
		output += newLine;

		// Initial State
		output += "Initial State" + newLine;
		output += "----------" + newLine;
		for (Proposition prop : unground.initial) {
			output += prop.toStringTyped();
			output += newLine;
		}
		output += newLine;

		// Goal State
		output += "Goal State" + newLine;
		output += "----------" + newLine;
		for (Condition goal : unground.goal.getConditionalPropositions()) {
			output += goal.toStringTyped();
			output += newLine;
		}
		output += newLine;

		// Metric
		output += "Metric" + newLine;
		output += "----------" + newLine;
		if (unground.metric != null) {
			output += unground.metric.toStringTyped();
			output += newLine;
		}

		return output;

	}
}
