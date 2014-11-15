package org.cei.planner.data;

public enum StateValuePolicyEnum {
	WIN_LOSS_STATE, // Is the state a win or a loss
	METRIC, //Not used at present. reserved for  
	H_VALUE, // Uses the H value for the relaxed plan
	WIN_LOSS_ACTION // Is the action part of a win or loss
}
