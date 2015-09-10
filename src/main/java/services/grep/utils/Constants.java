package main.java.services.grep.utils;

/**
 * 
 * 기본적인 것은 각자 쓰되, global한 것은 여기서 쓴다.
 * 
 * refresh, 수정, 삭제 등의 mode를 변경하거나 하려면, singleton으로 바꿔야 될지도 모를듯...
 * 
 * @author marine1079
 * @since 150714
 *
 */
public class Constants {

	private Constants() {
	}
	
	/*
	 * 이 정보는 사실 한번 실행되고 나서 바뀌면 안된다.
	 * work-list 등에 schedule이 기록되면서 진행되기 때문이다.
	 * 물론 이 tag들이 바뀐다고 해도 동작은 할 것이지만 tag가 바뀌면 결국 원하는 정보는 존재하지 않을수도 있는 만큼, 무의미한 행동이 될 것이다.
	 * error가 나지는 않겠지만 scheduled range에 새로 넣은 tag를 가진 media가 존재해버리면 결국 table은 mix되어버리고 복잡해진다.
	 */
	public static final String[] TARGET_TAGS = {"먹스타그램", "맛스타그램", "맛집", "먹방"};
	
	public static final String TARGET_TABLE = "RESTAURANT";
	
	public static final int INT_NULL = -1;// remaining이 안나오거나 등등을 위함.
	
	public static final int RATE_LIMIT = 5000;

}
