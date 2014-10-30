package org.cei.planner;

import javaff.data.GroundProblem;
import javaff.data.Plan;

public interface IPlanner {
	public Plan solve(GroundProblem problem);
}
