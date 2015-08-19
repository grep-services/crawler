package main.java.services.grep.exceptions;

public class InstagramLibraryException extends Exception {

	private static final String MSG = "Exception : Instagram library exception. Message : %s";
	
	public InstagramLibraryException(String message) {
		super(String.format(MSG, message));
	}

}