package org.cei.planner.mcts.junit;

import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import javaff.JavaFF;
import javaff.data.Plan;

import org.cei.planner.IPlanner;
import org.cei.planner.PDDLPlanner;
import org.cei.planner.mcrw.MCRWPlanner;
import org.cei.planner.mcts.MCTSPlanner;
import org.cei.planner.policy.RandomMCPolicy;
import org.junit.Test;

public class BenchmarkDriverLog {
	private static final String DRIVER_LOG_PATH = "./problems/driverlog/";
	private static final String DEPOTS_PATH = "./problems/depots/";
	private static final String ROVERS_PATH = "./problems/rovers/";

	private static final String DOMAIN_FILE = "domain.pddl";
	
	private static final int ITERATIONS = 20;
	
	@Test
	public void benchmarkMCTS() throws Exception {
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
				if (!problemFile.getName().endsWith(DOMAIN_FILE)) {
					MCTSPlanner.getLog().info("Solving Problem " + problemFile.getName());
					ExecutorService execService = Executors.newCachedThreadPool();
					long totalTime = 0;
					int planLength = 0;
					for (int i = 0; i < ITERATIONS; i++) {
						long startTime = System.nanoTime();
						PDDLPlanner planner = new PDDLPlanner(domainFile, problemFile, mctsPlanner);
						Future<Plan> futurePlan = execService.submit(planner);
						Plan plan = futurePlan.get();
						totalTime += System.nanoTime() - startTime;
						planLength += plan.getActions().size();
					}
					double averageTime_ms = (totalTime * Math.pow(10, -6)) / ITERATIONS;
					double averagePlanLength = planLength / ITERATIONS;
					System.out.println(problemFile.getName() + ", " + averagePlanLength + ", " + averageTime_ms);
				}
			}
		}
	}
	
	@Test
	public void benchmarkMCRW() throws Exception {
		//Divert parser output to file to remove from console
		PrintStream output = new PrintStream(new File("./output/out.txt"));
		JavaFF.parsingOutput = output;
		//Setup Logger
		MCRWPlanner.getLog().setLevel(Level.OFF);
		FileHandler mcrwOutput = new FileHandler("./output/MCRWOutput.txt");
		mcrwOutput.setFormatter(new SimpleFormatter());
		MCRWPlanner.getLog().addHandler(mcrwOutput);
		
		File domainFile = new File(DRIVER_LOG_PATH + DOMAIN_FILE);
		File dir = new File(DRIVER_LOG_PATH);
		File[] directoryListing = dir.listFiles();
		
		IPlanner mcrwPlanner = new MCRWPlanner();
		
		if (directoryListing != null) {
			for (File problemFile : directoryListing) {
				if (!problemFile.getName().endsWith(DOMAIN_FILE)) {
					MCRWPlanner.getLog().info("Solving Problem " + problemFile.getName());
					ExecutorService execService = Executors.newCachedThreadPool();
					long totalTime = 0;
					int planLength = 0;
					for (int i = 0; i < ITERATIONS; i++) {
						long startTime = System.nanoTime();
						PDDLPlanner planner = new PDDLPlanner(domainFile, problemFile, mcrwPlanner);
						Future<Plan> futurePlan = execService.submit(planner);
						Plan plan = futurePlan.get();
						totalTime += System.nanoTime() - startTime;
						planLength += plan.getActions().size();
					}
					double averageTime_ms = (totalTime * Math.pow(10, -6)) / ITERATIONS;
					double averagePlanLength = planLength / ITERATIONS;
					double avgtimeSpenth = (RandomMCPolicy.timeSpenth * Math.pow(10, -6)) / ITERATIONS;
					System.out.println(problemFile.getName() + ", " + averagePlanLength + ", " + averageTime_ms + ", " + avgtimeSpenth);
					RandomMCPolicy.timeSpenth = 0;
				}
			}
		}
	}
	
	
}
