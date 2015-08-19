package main.java.services.grep.processors;

import java.util.List;

import main.java.services.grep.exceptions.PageNotFoundException;
import main.java.services.grep.exceptions.RateLimitExceedException;
import main.java.services.grep.utils.Constants;

import org.apache.commons.lang3.Range;
import org.jinstagram.entity.users.feed.MediaFeedData;

/**
 * 
 * 일단 작업량을 구할 수 있어야 한다. 이것은 DBAccessor와 동조할 필요도 있다.
 * 구해진 작업량에 대해서, 구해올 수 있는 account들을 조사하고, 결론적으로 task들을 생성한다.
 * Rescheduling은 10분마다, 그리고 추가/수정시에 하면 된다.
 * 삭제는 어차피 unavailable, free만 하게 할 것이고, 영향을 안 미친다.
 * task들이 사실상 비슷한 시간 내에 끝나게끔 scheduling되었을 것이므로
 * 어떤 task가 조금 먼저 끝난다고 해도 그냥 넘어간다.
 * 즉, 단순한 multi-threading 방식이면 될 것이다.
 * 
 * @author marine
 * @since 150706
 *
 */
public class TaskManager {
	
	// 초기엔 1개라도, 나중에는 분산될 수 있다. 특히 upper가 -1인 range는 recent부터받으라는 것으로 한다.
	private List<Range<Long>> schedules;
	private List<Task> tasks;

	public TaskManager() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * accounts를 받아서 serial용이면 reserve하고, parallel용이면 task를 생성한다. both면 아래 방식으로 처리한다.
	 * 그 algorithm은 일단 간단히, serial이 최소 1개씩 있을 때에는 parallel 우선인 방식으로 간다.
	 * 즉, 먼저 task들을 살펴보고, serial이 필요한 곳들이 있으면 채워주고, 다 1개씩 있으면 parallel로 넘어가면 된다는 말이다.
	 * 물론 나중에는 parallel의 limit를 정하고, serial로 채우는게 좋겠지만 현실적으로 accounts를 그렇게 많이 만들 것 같지는 않으므로 pass한다.
	 * 다만, serial로만 지정된 account가 있다면 그건 물론 task에 추가해줄 것이다.
	 */
	public void allocAccounts(List<Account> accounts) {
		for(Account account : accounts) {
			allocAccount(account);
		}
	}
	
	// 복수 개만 할 필요는 없다. 추가/수정 정도에서는 단수도 필요할 수 있다.
	public void allocAccount(Account account) {
		
	}
	
	/*
	 * 최소 1개의 account가 필요하다.
	 * 하지만 serial도 최소 1개까지는 parallel보다 먼저 할당해주기로 했으며
	 * 특히 serial로만 설정된 account가 있는 경우에는 이렇게 복수의 account들로 task를 생성하게 될 수도 있다.
	 */
	public void createTask(List<Account> accounts) {
		
	}
	
	//TODO: SCHEDULE CALCULATING 하는 METHOD 필요할듯.
	
}

class Task extends Thread {

	// 초기에 시작한 task라면 모르겠지만, 나중에 추가될 수록 분산된 schedule들을 가지게 될 것이므로 list가 필요하다.
	private List<Range<Long>> schedules;
	// 이 task를 수행하기 위해 할당된 serial/both accounts. 물론 다 사용하고 나면 unavailable로 해두고 삭제한다. 그럼 timer에 의해 나중에는 사용가능하게 될 것이다.
	private List<Account> accounts;
	
	public Task() {
	}
	
	@Override
	public void run() {
		// list iteration 안하고 이렇게 하는 이유는, 삭제까지 하는 구조상 이게 더 적합하기 때문이다.
		while(true) {
			// 웬만하면 limit 높은것으로 1개 뽑고
			Account account = popAccount();
			// exception 고려하면서 query 실행
			try {
				List<MediaFeedData> mediaList = account.getTagMediaList(Constants.TARGET_TAG);
				// 제대로 받아졌으면, 아무래도 callback 날리든가 해서 db에 기록해야 할듯.
			} catch (PageNotFoundException e) {
				break;//TODO: 이렇게 한다고 작업이 끝내 질것인지.
			} catch (RateLimitExceedException e) {
				// 다 쓴건 설정 정리하고 버린다.
				account.setRateRemaining(0);
				account.setTaskStatus(TaskStatus.UNAVAILABLE);
				
				accounts.remove(account);
			}
		}
	}
	
	private Account popAccount() {
		return accounts.get(0);
	}
	
}