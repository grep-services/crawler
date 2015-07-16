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
	
	public MainController() {
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
