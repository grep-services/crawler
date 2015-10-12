package main.java.services.grep.processors;

import java.util.List;

import org.apache.commons.lang3.Range;

/**
 * 
 * range list를 갖고 있는 것으로 끝나는 것이 아니다.
 * 그것의 +, - 즉 net를 구한 뒤 반환하는 기능이 필요.
 * 중요한 점은, 항상 +로 -를 모두 상쇄시킬 수 있어야 한다는 것.
 * 그렇지 않다면, 올바른 schedule format이 아니라는 exception을 발생시킨다.
 * 
 * 1개의 account가 더 들어와서 나눠줘야 할 때
 * 즉 현재 range list의 총 량을 1/n으로 나눠서 다시 schedule로 생산 및 분배할 수 있는 기능도 필요.
 * 
 * 외부에서 string으로 받은 것을 include, exclude 따로 모으고
 * 또 그것들을 순차 정렬하고,
 * 그리고 나서야 schedule을 만드는데 사용하고 한다면 좀 복잡해진다.
 * 
 * 그냥 string array 받으면 자동으로 schedule 만들 수 있도록 한다.
 * 
 * @author marine
 * @since 151012
 *
 */
public class Schedule {
	
	private List<Range<Long>> includeRanges;
	private List<Range<Long>> excludeRanges;
	
	public Schedule() {
		
	}
	
	public Schedule(List<String[]> parsedResult) {
		
	}
	
	// 분사 할 때는 이렇게 하는게 더 나을 것이다.
	public Schedule(List<Range<Long>> includeRanges, List<Range<Long>> excludeRanges) {
		
	}
	
	public void includeRange() {
		
	}
	
	public void excludeRange() {
		
	}
	
	public void sortRangeList(List<Range<Long>> ranges) {
		
	}
	
}
