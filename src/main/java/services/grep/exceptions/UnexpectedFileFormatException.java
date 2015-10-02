package main.java.services.grep.exceptions;

import java.util.List;

import org.jinstagram.entity.users.feed.MediaFeedData;

/**
 * 
 * 파일의 format 체크도 잘 되어야 한다.
 * 
 * @author marine1079
 * @since 151002
 *
 */
public class UnexpectedFileFormatException extends Exception {

	private static final String MSG = "Exception : unexpected file format. File : %s, Line : %s";
	
	public UnexpectedFileFormatException(String fileName, String line) {
		super(String.format(MSG, fileName, line));
	}

}
