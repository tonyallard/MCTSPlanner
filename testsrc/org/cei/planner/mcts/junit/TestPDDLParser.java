package org.cei.planner.mcts.junit;

import java.io.File;

import javaff.data.GroundProblem;
import javaff.data.UngroundProblem;
import javaff.parser.PDDL21parser;

import org.cei.planner.mcts.util.GroundProblemUtil;
import org.cei.planner.mcts.util.UngroundProblemUtil;
import org.junit.Test;

public class TestPDDLParser {
	private static final String DRIVER_LOG_PATH = "./problems/driverlog/";
	private static final String DEPOTS_PATH = "./problems/depots/";
	private static final String ROVERS_PATH = "./problems/rovers/";

	private static final String DOMAIN_FILE = "domain.pddl";
	
	@Test
	public void testParsingDriverLog() {
		File domainFile = new File(DRIVER_LOG_PATH + DOMAIN_FILE);
		File dir = new File(DRIVER_LOG_PATH);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File problemFile : directoryListing) {
				if (!problemFile.getName().endsWith(DOMAIN_FILE)) {
					UngroundProblem unground = PDDL21parser.parseFiles(domainFile, problemFile);
					System.out.println("UNGROUND PROBLEM");
					System.out.println(UngroundProblemUtil.toString(unground));
					GroundProblem ground = unground.ground();
					System.out.println("GROUND PROBLEM");
					System.out.println(GroundProblemUtil.toString(ground));
					break;
				}
			}
		}
	}
	
	
}
