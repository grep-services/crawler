package main.java.services.grep.exceptions;

/**
 * 
 * 실험결과, tag로 받는 마지막 페이지에서는 page를 찾지 못한다.
 * 다만, 그 이유 말고도 page를 찾지 못하는 경우가 생길 수도 있다.
 * 하지만 아직 그 부분은 발견된 적이 없으므로, 일단 마지막 페이지에서 이 exception을 받는다고 생각하도록 한다.
 * 
 * @author marine1079
 * @since 150721
 *
 */
public class PageNotFoundException extends Exception {

	private static final String MSG = "Exception : page not found. Account : %s, Feed : %s";
	
	public PageNotFoundException(String clientId, String maxId) {
		super(String.format(MSG, clientId, maxId));
	}

}
