package main.java.services.grep.controllers;

import java.util.List;

import main.java.services.grep.exceptions.UnexpectedFileFormatException;
import main.java.services.grep.processors.Account;
import main.java.services.grep.processors.AccountProvider;
import main.java.services.grep.processors.AccountProvider.AccountCallback;
import main.java.services.grep.processors.DBAccessor;
import main.java.services.grep.processors.TaskManager;
import main.java.services.grep.processors.TaskManager.TaskCallback;
import main.java.services.grep.utils.FileManager;
import main.java.services.grep.utils.MultiPrinter;

/**
 * 
 * Account 받아 Task에 줘서 필요한 Query 실행시키고 DB에까지 기록.
 * Input을 받아 Account를 관리하거나, Task를 실행/중지 시키거나, DB의 내용을 확인하는 등의 관리.
 * refresh 1일단위, 삭제확인 7일단위, 수정확인 1달단위로 간다.
 * 각 module은 이 package에 담아두고 이 class에서 가져와 사용한다.
 * 
 * 당장은 몰라도, 앞으로는 Daemon으로 작동할 수 있도록 만든다.
 * 타 object들도 일단 가능한 대로 singleton보다 단순 instance 방식으로 간다.
 * 
 * 하지만, task class에서 db class 요청하는 등
 * 사실상 main에서의 central control이 힘들긴 하다.
 * 
 * 문제 없으므로, 필요한 것들은 다 singleton으로 간다.
 * 
 * @author marine
 * @since 150706
 * 
 */
public class MainController implements AccountCallback, TaskCallback {

	private AccountProvider accountProvider;
	private TaskManager taskManager;
	
	public MainController() {
		this(false, false, false, false);
	}
	
	public MainController(boolean isDaemon, boolean hasInit, boolean hasAccounts, boolean hasTasks) {
		if(hasInit) {
			init();
		}
		
		accountProvider = new AccountProvider(this, hasAccounts);
		taskManager = new TaskManager(this, hasTasks);
	}
	
	public void init() {
		try {
			FileManager.getInstance().getInitParams();
		} catch (UnexpectedFileFormatException e) {
			MultiPrinter.getInstance().printException(e.getMessage());
		}
	}
	
	/*
	 * Daemon으로 실행할지의 여부와, 신규/지정/변경사항 관련 옵션 받는다. 없으면 default로 간다.
	 * -d는 Daemon, 안달면 그냥이고, -f는 fetch, -r은 range, -u는 update이다.
	 * format checking은 너무 자세하게 하지는 않는다.
	 * 일단 - 합치는 것은 없다. 무조건 따로 간다. 다만 -를 포함하는 것만 valid하다.
	 * 
	 * 어차피 daemon으로 가게 되면 이런 구조가 아니라 thread 구조로 바뀌게 되고
	 * 특히 linux 환경으로 넘어가므로 새로 구조를 만들어야 한다.
	 */
	public static void main(String[] args) {
		
		if(args.length > 0) {
			boolean isDaemon = false;
			// 초기화값이 있는지의 여부.
			boolean hasInit = false;
			boolean hasAccountInit = false;
			boolean hasTaskInit = false;
			
			// daemon은 안들어온다고 가정한다.
			for(String arg : args) {
				if(arg.equals("-i")) {
					hasInit = true;
				}
				
				if(arg.equals("-a")) {
					hasAccountInit = true;
				}
				
				if(arg.equals("-t")) {
					hasTaskInit = true;
				}
			}
			
			new MainController(isDaemon, hasInit, hasAccountInit, hasTaskInit);
		} else {
			new MainController();
		}
	}
/*
	@Override
	public void onAccountInit(List<Account> accounts) {
		// 일단 msg 출력해주는게 좋을 것 같다.
		multiPrinter.showAccounts(accounts);
		// null에서 처음 init된 것이라면 start될 것이다.
		accountProvider.startObserving();
		// task manager에 넘긴다.
	}
*/
	@Override
	public void onAccountInserted(Account account) {
		// 일단 msg 출력해주는게 좋을 것 같다.
		MultiPrinter.getInstance().showAccount(account);
		// null에서 처음 init된 것이라면 start될 것이다.
		accountProvider.startObserving();
		// task manager에 넘긴다.
	}

	@Override
	// 아마 당장은 msg로서의 역할 말고는 없을 것 같다.(어차피 unavailable, free가 지워진 것이므로)
	public void onAccountRemoved() {
		// msg 출력
	}

	@Override
	public void onAccountModified() {
	}

	@Override
	public void onTaskInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskCreated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskModified() {
		// TODO Auto-generated method stub
		
	}

}
