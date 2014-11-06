package org.cei.planner.policy;

import java.util.concurrent.Callable;

import org.cei.planner.data.MCTSNode;

public interface IPolicy extends Callable<MCTSNode> {

}
