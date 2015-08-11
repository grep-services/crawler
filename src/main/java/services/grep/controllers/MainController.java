package main.java.services.grep.controllers;

import java.util.List;

import main.java.services.grep.processors.Account;
import main.java.services.grep.processors.AccountProvider;
import main.java.services.grep.processors.DBAccessor;
import main.java.services.grep.processors.TaskManager;
import main.java.services.grep.utils.MultiPrinter;

/**
 * 
 * Account 받아 Task에 줘서 필요한 Query 실행시키고 DB에까지 기록.
 * Input을 받아 Account를 관리하거나, Task를 실행/중지 시키거나, DB의 내용을 확인하는 등의 관리.
 * 세부적인 전략으로는, Crawl은 사용자의 명령에 의해 실행되고, 추가분에 대한 Update는 1회/1일, 삭제분에 대해서는 1회/1주 로 한다.
 * 
 * 당장은 몰라도, 앞으로는 Daemon으로 작동할 수 있도록 만든다.
 * 타 object들도 일단 가능한 대로 singleton보다 단순 instance 방식으로 간다.
 * 
 * @author marine
 * @since 150706
 * 
 */
public class MainController implements AccountProvider.AccountCallback {

	private AccountProvider accountProvider;
	private TaskManager taskManager;
	private DBAccessor dbAccessor;
	private MultiPrinter multiPrinter;
	// 최근값, 범위, 수정사항. 범위도 물론 최근값부터 하게 할 수 있지만, 최근값 모드를 따로 두는것도 사용성에서는 의미가 있다.
	private enum ExecuteMode {fetch, range, update};
	
	public MainController() {
		this(false, ExecuteMode.range);
	}
	
	public MainController(boolean isDaemon, ExecuteMode executeMode) {
		accountProvider = new AccountProvider();
		taskManager = new TaskManager();
		dbAccessor = new DBAccessor();
		multiPrinter = new MultiPrinter();
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
			ExecuteMode executeMode = ExecuteMode.range;
			
			// daemon은 안들어온다고 가정한다.
			for(String arg : args) {
				if(arg.equals("-f")) {
					executeMode = ExecuteMode.fetch;
				} else if(arg.equals("-u")) {
					executeMode = ExecuteMode.update;
				}
			}
			
			new MainController(isDaemon, executeMode);
		} else {
			new MainController();
		}
	}

	@Override
	public void onAccountInit(List<Account> accounts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccountInserted(Account account) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccountRemoved() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccountModified() {
		// TODO Auto-generated method stub
		
	}

}
