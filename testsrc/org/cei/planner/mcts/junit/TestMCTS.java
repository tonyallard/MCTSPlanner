package org.cei.planner.mcts.junit;

import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import javaff.JavaFF;
import javaff.data.Plan;

import org.cei.planner.IPlanner;
import org.cei.planner.PDDLPlanner;
import org.cei.planner.mcts.MCTSPlanner;
import org.junit.Test;

public class TestMCTS {
	private static final String DRIVER_LOG_PATH = "./problems/driverlog/";
	private static final String DEPOTS_PATH = "./problems/depots/";
	private static final String ROVERS_PATH = "./problems/rovers/";

	private static final String DOMAIN_FILE = "domain.pddl";
	
	@Test
	public void testParsingDriverLog() throws Exception {
		//Divert parser output to file to remove from console
		PrintStream output = new PrintStream(new File("./output/out.txt"));
		JavaFF.parsingOutput = output;
		//Setup Logger
		MCTSPlanner.getLog().setLevel(Level.ALL);
		FileHandler mctsOutput = new FileHandler("./output/MCTSOutput.txt");
		mctsOutput.setFormatter(new SimpleFormatter());
		MCTSPlanner.getLog().addHandler(mctsOutput);
		
		File domainFile = new File(DRIVER_LOG_PATH + DOMAIN_FILE);
		File dir = new File(DRIVER_LOG_PATH);
		File[] directoryListing = dir.listFiles();
		
		IPlanner mctsPlanner = new MCTSPlanner();
		
		if (directoryListing != null) {
			for (File problemFile : directoryListing) {
				if (problemFile.getName().endsWith("pfile01")) {
					MCTSPlanner.getLog().info("Solving Problem " + problemFile.getName());
					
					PDDLPlanner planner = new PDDLPlanner(domainFile, problemFile, mctsPlanner);
					ExecutorService execService = Executors.newCachedThreadPool();
					Future<Plan> futurePlan = execService.submit(planner);
					Plan plan = futurePlan.get();
					plan.print(System.out);
					break;
				}
			}
		}
	}
}
