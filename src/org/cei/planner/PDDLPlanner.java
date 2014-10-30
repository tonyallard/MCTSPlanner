package org.cei.planner;

import java.io.File;
import java.util.concurrent.Callable;

import javaff.data.GroundProblem;
import javaff.data.Plan;
import javaff.data.UngroundProblem;
import javaff.parser.PDDL21parser;

public class PDDLPlanner implements Callable<Plan> {

	private IPlanner planner = null;
	private UngroundProblem ungroundProblem = null; 
	
	public PDDLPlanner (File domain, File problem, IPlanner planner) {
		this.planner = planner;
		this.ungroundProblem = PDDL21parser.parseFiles(domain, problem);
	}

	@Override
	public Plan call() throws Exception {
		GroundProblem groundProblem = ungroundProblem.ground();
		return planner.solve(groundProblem);
	}
}
