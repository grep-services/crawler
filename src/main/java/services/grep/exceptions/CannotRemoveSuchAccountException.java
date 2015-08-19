package main.java.services.grep.exceptions;

import main.java.services.grep.processors.TaskStatus;

public class CannotRemoveSuchAccountException extends Exception {

	private static final String MSG = "Exception : cannot remove such account. Account : %s, Status : %s";
	
	public CannotRemoveSuchAccountException(String clientId, TaskStatus taskStatus) {
		super(String.format(MSG, clientId, taskStatus.toString()));
	}

}
