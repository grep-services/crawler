package main.java.services.grep.controllers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import main.java.services.grep.processors.Account;
import main.java.services.grep.processors.AccountProvider;
import main.java.services.grep.processors.AccountProvider.AccountCallback;
import main.java.services.grep.processors.DBAccessor;
import main.java.services.grep.processors.ProcessingType;
import main.java.services.grep.processors.TaskManager;
import main.java.services.grep.processors.TaskManager.TaskCallback;
import main.java.services.grep.utils.Constants;
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
 * @author marine
 * @since 150706
 * 
 */
public class MainController implements AccountCallback, TaskCallback {

	private AccountProvider accountProvider;
	private TaskManager taskManager;
	private DBAccessor dbAccessor;
	private MultiPrinter multiPrinter;
	// 최근값, 범위, 수정사항. 범위도 물론 최근값부터 하게 할 수 있지만, 최근값 모드를 따로 두는것도 사용성에서는 의미가 있다.
	private enum ExecuteMode {fetch, range, update};
	private String table = Constants.TARGET_TABLE;
	private String[] tags = Constants.TARGET_TAGS;
	
	public MainController() {
		this(false, false, false, false);
	}
	
	public MainController(boolean isDaemon, boolean hasInit, boolean hasAccountInit, boolean hasTaskInit) {
		if(hasInit) {
			init();
		}
		
		accountProvider = new AccountProvider(this, hasAccountInit);
		taskManager = new TaskManager(this, hasTaskInit);
		dbAccessor = new DBAccessor();
		multiPrinter = new MultiPrinter();
	}
	
	public void init() {
		BufferedReader reader = null;
		
		final String FILE_INIT = "total-plan";
		final String PREFIX_COMMENTS = "\\*";
		final String REGEX_DECLARE = "^(NEW|MOD|DEL)\\s*,\\s*("
				+ Constants.TARGET_SERVICES[0] + "|" // instagram
				+ Constants.TARGET_SERVICES[1] + "|" // naver
				+ Constants.TARGET_SERVICES[2] + "|" // facebook
				+ ")\\s*,\\s*#?\\w+\\s*,\\s*d+\\s*,\\s*(RESERVED|RUNNING|STOPPED|PASSED|DONE)\\s*$";
		final String STR_DELIMITER = "\\s*,\\s*";
		final int ARG_LIMIT = 5;
		
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

	@Override
	public void onAccountInit(List<Account> accounts) {
		// 일단 msg 출력해주는게 좋을 것 같다.
		//multiPrinter.showAccounts(accounts);
		MultiPrinter.showAccounts(accounts);
		// task manager에 넘긴다.
	}

	@Override
	public void onAccountInserted(Account account) {
		// 일단 msg 출력해주는게 좋을 것 같다.
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

}
