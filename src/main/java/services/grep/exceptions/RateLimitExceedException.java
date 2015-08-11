package main.java.services.grep.exceptions;

/**
 * 
 * 간략히 이렇게 하고, 나중에는 MultiPrinter로 출력한다.
 * 
 * @author marine1079
 * @since 150721
 *
 */
public class RateLimitExceedException extends Exception {

	private static final String MSG = "Exception : rate limit exceed. Account : %s, Position : %s";
	
	public RateLimitExceedException(String clientId, String maxId) {
		super(String.format(MSG, clientId, maxId));
	}

}
