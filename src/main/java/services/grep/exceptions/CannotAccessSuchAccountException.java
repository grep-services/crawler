package main.java.services.grep.exceptions;

import main.java.services.grep.processors.TaskStatus;

public class CannotAccessSuchAccountException extends Exception {

	private static final String MSG = "Exception : cannot access such account. Account : %s, Status : %s";
	
	public CannotAccessSuchAccountException(String clientId, TaskStatus taskStatus) {
		super(String.format(MSG, clientId, taskStatus.toString()));
	}

}
