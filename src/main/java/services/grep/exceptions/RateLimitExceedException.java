package main.java.services.grep.exceptions;

import java.util.List;

import org.jinstagram.entity.users.feed.MediaFeedData;

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
	private List<MediaFeedData> mediaList;
	
	public RateLimitExceedException(String clientId, String maxId) {
		this(clientId, maxId, null);
	}
	
	public RateLimitExceedException(String clientId, String maxId, List<MediaFeedData> mediaList) {
		super(String.format(MSG, clientId, maxId));
		
		this.mediaList = mediaList;
	}

}
