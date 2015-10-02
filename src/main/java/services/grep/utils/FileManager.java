package main.java.services.grep.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.java.services.grep.exceptions.UnexpectedFileFormatException;
import main.java.services.grep.processors.ProcessingType;
import main.java.services.grep.processors.TargetServices;

/*
 * 단순한 기능으로는 현재 format이 비슷한 init file들을 각 class에서 읽어서 낭비하고 있는 code를 줄여준다는 의미이며
 * 더 깊이 본다면 각 init file들이 다른 format을 가지게 되어서 code가 길어질 때에도 여기서 유연하게 관리할 수 있다는 점과
 * file 자체를 다루는 것이 사용자의 unexpected behavior에 대해서도 안전할 수 있도록 lock기능까지도 포함하게 하기 위함이다.
 * 
 * method들을 static으로 하긴 했지만 왠만해서는 ref 유지되는 방향으로 가야 될 것 같다.
 * 그래야 lock 기능을 제대로 발휘할 수 있을 것이기 때문이다.
 */
public class FileManager {
	
	// REGEX 편의 및 오차 방지 위해 선언.
	private static final String COMMENTS = "\\*";
	private static final String BLANK = "\\s*";
	private static final String COMMA = ",";
	private static final String DELIMITER = BLANK + COMMA + BLANK;
	private static final String STARTS = "^";
	private static final String ENDS = "$";
	private static final String WORDS = "\\w+";
	private static final String WORDS_WITH_DOT = "(\\w|.)+";
	private static final String BRACKET_OPEN = "(";
	private static final String BRACKET_CLOSE = ")";
	private static final String OR = "|";
	
	public FileManager() {
	}
	
	/*
	 * 나중에 format이 복잡해지면 모르겠지만, 일단 지금은 init의 형태를 대략 통일해놓았다.
	 * 그렇기 때문에 같은 방식으로 parsing할 수 있다.
	 * 그렇지만 각 class에서 하면 될 일을 여기로 가져온 이유는 file을 좀더 정확히 다루기 위해서이다.
	 * 앞으로는 file의 lock 등을 더 구체적으로 만들 것이다.
	 */
	public static List<String[]> parseInit(String file_init, String regex_declare, int arg_limit) throws UnexpectedFileFormatException {
		List<String[]> result = null;
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(file_init));
			
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				
				if(line.startsWith(COMMENTS) || line.isEmpty()) {
					continue;
				}
				
				if(!line.matches(regex_declare)) {
					throw new UnexpectedFileFormatException(file_init, line);
				}
				
				String[] array = line.split(DELIMITER, arg_limit);
				
				if(result == null) {
					result = new ArrayList<String[]>();
				}
				
				result.add(array);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	/*
	 * runnable까지 받아서 하는건 좀 힘들듯 하다.
	 * 워낙 각 class별로 local vars가 많기 때문이다.
	 * 이정도까지만 해도 적당할 듯 하다.
	 */
	public static List<String[]> parseAccountInit() throws UnexpectedFileFormatException {
		final String FILE_INIT = "account-info";
		final String REGEX_DECLARE = STARTS + BLANK
				+ BRACKET_OPEN + TargetServices.INSTAGRAM + OR + TargetServices.FACEBOOK + OR + TargetServices.NAVER + OR + BRACKET_CLOSE + DELIMITER
				+ WORDS + DELIMITER
				+ WORDS + DELIMITER
				+ WORDS + DELIMITER
				+ WORDS_WITH_DOT + DELIMITER
				+ BRACKET_OPEN + ProcessingType.NONE.toString() + OR + ProcessingType.SERIAL.toString() + OR + ProcessingType.PARALLEL.toString() + OR + ProcessingType.BOTH.toString() + BRACKET_CLOSE
				+ BLANK + ENDS;
		final int ARG_LIMIT = 5;
		
		return parseInit(FILE_INIT, REGEX_DECLARE, ARG_LIMIT);
	}
}
