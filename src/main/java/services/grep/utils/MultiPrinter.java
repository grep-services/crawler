package main.java.services.grep.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.java.services.grep.processors.Account;
import main.java.services.grep.processors.ProcessingType;

/**
 * 
 * 이 class는 log target이 중요시되는 log4j wrapper class이다.
 * 내부적으로는 log4j를 사용하지만 외부적으로는 항상 이 class를 경유하도록 한다.
 * 
 * 단순한 log도 log4j를 wrapping해서 params를 줄이거나 출력 format을 보기좋게 하고 일괄적인 on/off 역시 가능하게 하며
 * db에서 table 확인을 목적으로 할 수도 있고, 현재 processing 상황을 monitoring할 수도 있다.
 * 또한 exception 뿐만 아니라 전체적인 흐름들을 screen 또는 file로 logging할 수도 있다.
 * 
 * singleton으로 ref 유지되면서 쓰이는 쪽이 더 적합하다 판단
 * static methods들 유지하는 것 보다 차라리 낫다.
 * 방식은 single 보장만 되는 가장 간단한 방식으로 간다. 어차피 instance가 바로 지속적으로 사용될 것이므로 별 상관 없다.
 * 
 * @author marine1079
 * @since 150714
 *
 */
public class MultiPrinter {

	private static final MultiPrinter multiPrinter = new MultiPrinter();
	
	private MultiPrinter() {
	}
	
	public static MultiPrinter getInstance() {
		return multiPrinter;
	}
	
	public void showAccounts(List<Account> accounts) {
		if(accounts == null) {
			print("Accounts are not exist.");
		} else {
			for(Account account : accounts) {
				showAccount(account);
			}
		}
	}
	
	public void showAccount(Account account) {
		print(String.format("%s, %s, %s, %s, %s, %s", account.getAccountName(), account.getClientId(), account.getClientSecret(), account.getAccessToken(), account.getProcessingType().toString(), account.getTaskStatus().toString()));
	}
	
	public void showTasks() {
		
	}
	
	public void showDatabases() {
		
	}
	
	public void printException(String msg) {
		print("Exception : " + msg);
	}
	
	public void print(String msg) {
		System.out.println(msg);
	}

}