package main.java.services.grep.processors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.Range;

/**
 * 
 * 형태적으로, range의 list이다.
 * 그리고 1개의 task는 1개의 account를 가지고(대기 account는 있을 수 있어도) 1개의 schedule을 가진다.
 * 당사자인 account는 schedule에서 range를 꺼내서 계속 작업한다.
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
	
	private List<Range<Long>> netRanges;
	private List<Range<Long>> includeRanges;
	private List<Range<Long>> excludeRanges;
	
	public Schedule() {
	}
	
	// string format은 이미 check한 것으로 마무리한다고 생각한다.
	public Schedule(List<String[]> parsedResult) {
		for(String[] array : parsedResult) {
			if(array[0].equals("INCLUDE")) {
				if(includeRanges == null) {
					includeRanges = new ArrayList<Range<Long>>();
				}
				
				includeRanges.add(Range.between(toLong(array[1]), toLong(array[2])));
			} else if(array[0].equals("EXCLUDE")) {
				if(excludeRanges == null) {
					excludeRanges = new ArrayList<Range<Long>>();
				}
				
				excludeRanges.add(Range.between(toLong(array[1]), toLong(array[2])));
			}
		}
		
		calculateRange();
	}
	
	// 분사 할 때는 이렇게 하는게 더 나을 것이다.
	public Schedule(List<Range<Long>> includeRanges, List<Range<Long>> excludeRanges) {
		this.includeRanges = includeRanges;
		this.excludeRanges = excludeRanges;
		
		calculateRange();
	}
	
	private Long toLong(String string) {
		Long result;
		
		if(string.equals("First")) {
			result = getFirst();
		} else if(string.equals("Last")) {
			result = getLast();
		} else if(string.equals("Max")) {
			result = getMax();
		} else if(string.equals("Min")) {
			result = getMin();
		} else {
			result = Long.valueOf(string);
		}
		
		return result;
	}
	
	public void sortRangeList(List<Range<Long>> ranges) {
		ranges.sort(new Comparator<Range<Long>>() {
			@Override
			public int compare(Range<Long> o1, Range<Long> o2) {
				if(o1.isAfterRange(o2)) {
					return 1;
				} else if(o1.isBeforeRange(o2)) {
					return -1;
				} else {// 이곳에 올 일이 사실 없어야 한다.(겹치는 경우)
					return 0;
				}
			}
		});
	}
	
	/*
	 * net = include - exclude 하는 부분.
	 * 이 과정에서 정렬 역시 되도록 한다.
	 * 
	 * exclude 1개를 뽑아서 기준으로 include를 돌면서, 겹치는 것 있으면 include를 그만큼 줄여준다.
	 * exclude not null인 결과는 나올 수 없다.
	 * 즉, include가 여러개라서 한번에 exclude를 상쇄 못시킨다 하더라도, 결국은 다 없어질 것이다.
	 * 그러려면, exclude도 같이 상쇄시켜줘야 한다.
	 * 즉, 복잡할 것 없이, 단순 n*n 한다.
	 */
	public void calculateRange() {
		for(Range<Long> exclude : excludeRanges) {
			for(Range<Long> include : includeRanges) {
				if(exclude.isOverlappedBy(include)) {// common만 있으면 되는 것. 누가 누구를 포함하는지가 아니다.
					
				}
			}
		}
	}
	
	/*
	 * account에게 붙여줄 range의 size를 미리 예상하기는 힘들다.
	 * 대략 예상한다면 효율적일수는 있으나, contents간의 간격(밀도)을 예상하는 것은 힘들기 때문에 pass한다.
	 * account는 여기로부터 range를 받아서 사용한 뒤, 한 만큼은 exclude, 안 한 부분은 다시 include로 넘긴다.
	 * 
	 * serial account에게 넘기려는 도중, 새로운 parallel account로 인해 task가 하나 만들어지게 되어 range 차출이 필요한 등등
	 * 이 부분에서는 항상 range의 sync에 신경써야 한다. 어디서 sync를 신경써야 할지는 좀더 생각해본다.
	 */
	public Range<Long> popRange() {
		Range<Long> range = null;
		
		return range;
	}
	
	/*
	 * size만큼 공간을 비우고, 그 size만큼에(대략) 해당하는 range를 만들어서 return한다.
	 * parallel을 위한 것이며 중요하다.
	 * 하지만 size가 얼마가 될지 같은 것은 외부에서 결정해서 해줄 수 있는 문제이므로 그렇게 가본다.
	 */
	public Range<Long> allocRange(Long size) {
		Range<Long> range = null;
		
		return range;
	}
	
	private long getFirst() {
		return 0;
	}
	
	private long getLast() {
		return Long.MAX_VALUE;// insta current max의 9배 정도 됨...
	}
	
	private long getMin() {
		return 0;
	}
	
	private long getMax() {
		return 100;
	}
	
	public long getSize() {
		long size = 0;
		
		for(Range<Long> range : includeRanges) {
			size += (range.getMaximum() - range.getMinimum());
		}
		
		return size;
	}
	
}
