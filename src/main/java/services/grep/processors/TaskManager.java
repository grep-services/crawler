package main.java.services.grep.processors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import main.java.services.grep.exceptions.InstagramLibraryException;
import main.java.services.grep.exceptions.PageNotFoundException;
import main.java.services.grep.exceptions.RateLimitExceedException;
import main.java.services.grep.utils.Constants;
import main.java.services.grep.utils.MultiPrinter;

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
 * 지금은 직접 multi-threading 구조 만들지만
 * 책 사고 공부하고 나서는 좀더 existing structure 이용해서 안전성 높인다.
 * 
 * @author marine
 * @since 150706
 *
 */
public class TaskManager {
	
	// interface는 public, method는 public abstract, field는 public static final이 default이다.
	public interface TaskCallback {
		void onTaskInit();
		void onTaskCreated();
		void onTaskModified();
		void onTaskFinished();
	}
	
	private TaskCallback callback;
	// 초기엔 1개라도, 나중에는 분산될 수 있다. 특히 upper가 -1인 range는 recent부터받으라는 것으로 한다.
	private List<Range<Long>> schedules;
	private List<Task> tasks;

	public TaskManager(TaskCallback callback) {
		this(callback, false);
	}

	// task는 init 안하면 거의 직접 입력하기 쉽지는 않을 것이다.
	public TaskManager(TaskCallback callback, boolean hasInit) {
		this.callback = callback;
		
		if(hasInit) {
			initTasks();
		}
	}
	
	public void initTasks() {
		BufferedReader reader = null;
		
		final String FILE_INIT = "work-list";
		final String PREFIX_COMMENTS = "\\*";
		final String REGEX_DECLARE = "^(INCLUDE|EXCLUDE)\\s*,\\s*(FIRST|LAST|MIN|MAX|\\d+)\\s*,\\s*(FIRST|LAST|MIN|MAX|\\d+)\\s*$";
		final String STR_DELIMITER = "\\s*,\\s*";
		final int ARG_LIMIT = 3;
		
		try {
			reader = new BufferedReader(new FileReader(FILE_INIT));
			
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				
				if(line.startsWith(PREFIX_COMMENTS) || line.isEmpty()) {
					continue;
				}
				
				if(!line.matches(REGEX_DECLARE)) {
					//TODO: throws exception
				}
				
				String[] array = line.split(STR_DELIMITER, ARG_LIMIT);
			}
			
			// 그리고는 callback 날린다.
			callback.onTaskInit();
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
	}

	/*
	 * accounts를 받아서 serial용이면 reserve하고, parallel용이면 task를 생성한다. both면 아래 방식으로 처리한다.
	 * 그 algorithm은 일단 간단히, serial이 최소 1개씩 있을 때에는 parallel 우선인 방식으로 간다.
	 * 즉, 먼저 task들을 살펴보고, serial이 필요한 곳들이 있으면 채워주고, 다 1개씩 있으면 parallel로 넘어가면 된다는 말이다.
	 * 물론 나중에는 parallel의 limit를 정하고, serial로 채우는게 좋겠지만 현실적으로 accounts를 그렇게 많이 만들 것 같지는 않으므로 pass한다.
	 * 다만, serial로만 지정된 account가 있다면 그건 물론 task에 추가해줄 것이다.
	 */
	public void allocAccounts(List<Account> accounts) {
		// weight 순으로 정렬부터 한다. 그래야 task 할당을 먼저 해서 serial, both 등이 적절히 분배될 수 있다.
		accounts.sort(new Comparator<Account>() {
			@Override
			public int compare(Account o1, Account o2) {
				if(o1.getProcessingType().getWeight() > o2.getProcessingType().getWeight()) {
					return 1;
				} else if(o1.getProcessingType().getWeight() < o2.getProcessingType().getWeight()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		
		for(Account account : accounts) {
			allocAccount(account);
		}
	}
	
	// 복수 개만 할 필요는 없다. 추가/수정 정도에서는 단수도 필요할 수 있다.
	public void allocAccount(Account account) {
		if(account.getProcessingType() == ProcessingType.PARALLEL) {
			createTask(account);
		} else if(account.getProcessingType() == ProcessingType.SERIAL) {
			//TODO: 하지만 serial만 1개 있다거나, serial들만 있다거나 하는 때의 처리도 필요할 듯.
			modifyTask(account);
		} else if(account.getProcessingType() == ProcessingType.BOTH) {
			//TODO: 최소 1개씩은 serial 갖고 있는지, 그리고 parallel limit 체크 후 method call.
		}
	}
	
	/*
	 * 복수개의 account를 한꺼번에 task creation, modification 에 적용시키는 것이 좋을 수 있으나 복잡하다.
	 * 어차피 복수개의 account 다룰 일은 init 정도밖에 없고, 나중에 필요하다면 복수개 사용하는 방식으로 upgrade해본다.
	 */
	public void createTask(Account account) {
		callback.onTaskCreated();
	}
	
	public void modifyTask(Account account) {
		callback.onTaskModified();
	}
	
	//TODO: SCHEDULE CALCULATING 하는 METHOD 필요할듯.
	
}

class Task extends Thread {

	private String tag;
	// 초기에 시작한 task라면 모르겠지만, 나중에 추가될 수록 분산된 schedule들을 가지게 될 것이므로 list가 필요하다.
	private List<Range<Long>> schedules;
	// 이 task를 수행하기 위해 할당된 accounts. 물론 다 사용하고 나면 unavailable로 해두고 삭제한다. 그럼 timer에 의해 나중에는 사용가능하게 될 것이다.
	private List<Account> accounts;
	
	public Task(String tag, List<Range<Long>> schedules, List<Account> accounts) {
		this.tag = tag;
		this.schedules = schedules;
		this.accounts = accounts;
	}

	@Override
	public void run() {
		// list iteration 안하고 이렇게 하는 이유는, 삭제까지 하는 구조상 이게 더 적합하기 때문이다.
		while(true) {
			// 웬만하면 limit 높은것으로 1개 뽑고
			Account account = popAccount();
			//TODO: account sync 해야될듯.
			if(account != null) {
				// exception 고려하면서 query 실행
				try {
					List<MediaFeedData> mediaList = account.getTagMediaList(tag);
					// 제대로 받아졌으면, 아무래도 callback 날리든가 해서 db에 기록해야 할듯.
				} catch (PageNotFoundException e) {
					break;//TODO: 이렇게 한다고 작업이 끝내 질것인지.
				} catch (RateLimitExceedException e) {
					// 다 쓴건 status만 정리해두면 된다.
					account.setTaskStatus(TaskStatus.UNAVAILABLE);
				} catch (InstagramLibraryException e) {
					MultiPrinter.print(e.getMessage());
				}
			}
		}
	}
	
	//TODO: unavailable 가리는건 좋은데, max limit 뽑을 수 있도록 노력하기.
	private Account popAccount() {
		Account result = null;
		
		for(Account account : accounts) {
			if(account.getTaskStatus() != TaskStatus.UNAVAILABLE) {
				if(result == null) {
					result = account;
				} else {
					//TODO: limit 구해서 result update.
				}
			}
		}
		
		return result;
	}
	
}