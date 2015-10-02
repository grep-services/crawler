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
	
	public static final int INT_NULL = -1;// remaining이 안나오거나 등등을 위함.
	
	public static final int INSTAGRAM_RATE_LIMIT = 5000;

}
