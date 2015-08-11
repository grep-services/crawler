package main.java.services.grep.utils;

/**
 * 
 * 이 class는 log target이 중요시되는 log4j wrapper class이다.
 * 내부적으로는 log4j를 사용하지만 외부적으로는 항상 이 class를 경유하도록 한다.
 * 
 * 단순한 log도 log4j를 wrapping해서 params를 줄이거나 출력 format을 보기좋게 하고 일괄적인 on/off 역시 가능하게 하며
 * db에서 table 확인을 목적으로 할 수도 있고, 현재 processing 상황을 monitoring할 수도 있다.
 * 또한 exception 뿐만 아니라 전체적인 흐름들을 screen 또는 file로 logging할 수도 있다.
 * 
 * @author marine1079
 * @since 150714
 *
 */
public class MultiPrinter {

	public MultiPrinter() {
		// TODO Auto-generated constructor stub
	}

}