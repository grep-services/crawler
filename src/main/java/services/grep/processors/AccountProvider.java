package main.java.services.grep.processors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.java.services.grep.exceptions.CannotAccessSuchAccountException;
import main.java.services.grep.exceptions.InstagramLibraryException;
import main.java.services.grep.utils.Constants;

/**
 * 
 * Client ID 등과 함께 Token을 보관하는 것이 기본 역할이다.
 * 특히 Rate limit가 남아있는 Account를 return하고, 부족한 것은 재워두는 것도 중요 임무.
 * Serial, Parallel 등의 flag를 부착해두고, 알맞게 사용될 수 있도록 하는 것도 필요하다.
 * 
 * Token을 생성하는 방식은 아직 확실치 않으므로, 바로 string으로 보유하고 있도록 한다.
 * 
 * @author marine
 * @since 150706
 *
 */
public class AccountProvider {
	
	// interface는 public, method는 public abstract, field는 public static final이 default이다.
	public interface AccountCallback {
		void onAccountInit(List<Account> accounts);
		void onAccountInserted(Account account);
		void onAccountRemoved();// 미정
		void onAccountModified();// 미정
	}
	
	private AccountCallback callback;
	private List<Account> accounts;
	private AccountObserver observer;
	
	public AccountProvider(AccountCallback callback) {// 최소한 callback은 있어야 한다.
		this(callback, false);// 변수 없으면 첫 실행이라고 가정한다.
	}
	public AccountProvider(AccountCallback callback, boolean hasInit) {// 이미 초기화되었는지도 받는다.
		// set callback
		this.callback = callback;
		// init accounts
		if(hasInit) {
			initAccounts();
		}
		// observer 만들고 실행. init하면서 이미 remaining set되었겠지만 겹치게 놔둔다. 빼면 1개단위 추가할 때 바로 적용도 안되고, 차라리 겹치는게 낫다.
		observer = new AccountObserver();
		observer.start();
	}
	
	/*
	 * 초기 account들 설정도 필요하다.
	 * 웬만하면 외부 input file로부터 받는것이 추후 server에서 terminal로 실행시키는 등의 작업을 할 때
	 * 일일히 recompile하지 않아도 되어서 더 편할 것이다.
	 * 그리고, 받는 것으로 끝이 아니라, 받은 후 몇가지 설정도 해야 한다.
	 */
	public void initAccounts() {
		BufferedReader reader = null;
		
		final String FILE_INIT = "account-info";
		final String PREFIX_COMMENTS = "\\*";
		final String REGEX_DECLARE = "^\\s*("
				+ Constants.TARGET_SERVICES[0] + "|" // instagram
				+ Constants.TARGET_SERVICES[1] + "|" // naver
				+ Constants.TARGET_SERVICES[2] + "|" // facebook
				+ ")\\s*,\\s*\\w+\\s*,\\s*\\w+\\s*,\\s*\\w+\\s*,\\s*(\\w|.)+\\s*,\\s*("
				+ ProcessingType.NONE + "|"
				+ ProcessingType.SERIAL + "|"
				+ ProcessingType.PARALLEL + "|"
				+ ProcessingType.BOTH + ")\\s*$";
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
				
				ProcessingType processingType = ProcessingType.BOTH;
				
				if(!array[4].isEmpty()) {
					if(array[4].equals(ProcessingType.NONE.toString())) {
						processingType = ProcessingType.NONE;
					} else if(array[4].equals(ProcessingType.SERIAL.toString())) {
						processingType = ProcessingType.SERIAL;
					} else if(array[4].equals(ProcessingType.PARALLEL.toString())) {
						processingType = ProcessingType.PARALLEL;
					}
				}
				
				insertAccount(array[0], array[1], array[2], array[3], processingType, false);
			}
			
			// 그리고는 callback 날린다.
			callback.onAccountInit(accounts);
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
	
	// 그냥 넘어오는 것은 default로 생각하고 callback 부른다.
	public void insertAccount(String account_name, String client_id, String client_secret, String access_token, ProcessingType processing_type) {
		insertAccount(account_name, client_id, client_secret, access_token, processing_type, true);
	}
	/*
	 * remaining 구하는 이슈가 제일 중요한데, queue를 만들어 실시간 체크도 쉽지는 않다.
	 * 현재는 periodically checking 방식 쓰기로 했다.
	 * 하지만 insert, modify, remove 모두 즉각 scheduling에 반영되는게 좋을 것이므로
	 * 주기적 checking과 관계없이 또한 remaining을 update하고 callback해준다.
	 * 
	 * 그리고 insert같은 작업들이 꼭 file을 통해서 오라는 법도 없고 terminal을 통해서 실시간으로 들어올 수도 있도록 만들 것이다.
	 */
	public void insertAccount(String account_name, String client_id, String client_secret, String access_token, ProcessingType processing_type, boolean shouldCallback) {
		// 일단 account를 만든다. status는 내부에서 처리된다.
		Account account = new Account(account_name, client_id, client_secret, access_token, processing_type);
		// 다 되고나서야 추가를 한다. 그래야 실제 사용 가능할 것이다.
		if(accounts == null) {
			accounts = new ArrayList<Account>();
		} else {
			accounts.add(account);					
		}
		// callback 날린다.
		if(shouldCallback) {
			callback.onAccountInserted(account);
		}
	}
	
	// 사용 간편하게 하려면, stop 등등의 절차 없이, remove에서 알아서 모든걸 해줘야 한다.
	public void removeAccount(String account_name) throws CannotAccessSuchAccountException {
		// iterator 써서 해야 되는 것 같다.
		for(Iterator<Account> iterator = accounts.iterator(); iterator.hasNext();) {
			Account account = iterator.next();
			
			if(account.getAccountName().equals(account_name)) {
				TaskStatus taskStatus = account.getTaskStatus();
				
				if(taskStatus == TaskStatus.FREE) {
					iterator.remove();
					
					callback.onAccountRemoved();
					
					break;
				} else {// 예약된 것이나 실행중인 것을 지우려 했을 때는, 삭제를 안해주는 것으로 끝낼 것이 아니라 알려야 한다.
					throw new CannotAccessSuchAccountException(account_name, taskStatus);
				}
			}
		}
	}
	
	// 위와 같은 맥락에서, sync, scheduling 등의 처리도 manually하게 하지 않고 내부적으로 다 해줘야 한다.
	public void modifyAccount(String client_id, ProcessingType processing_type) throws CannotAccessSuchAccountException {
		for(Iterator<Account> iterator = accounts.iterator(); iterator.hasNext();) {
			Account account = iterator.next();
			
			if(account.getAccountName().equals(client_id)) {
				TaskStatus taskStatus = account.getTaskStatus();
				
				if(taskStatus == TaskStatus.FREE) {
					account.setProcessingType(processing_type);
					
					callback.onAccountModified();
					
					break;
				} else {// 예약된 것이나 실행중인 것을 지우려 했을 때는, 삭제를 안해주는 것으로 끝낼 것이 아니라 알려야 한다.
					throw new CannotAccessSuchAccountException(client_id, taskStatus);
				}
			}
		}
	}
	
	/*
	 * list 등 참조하기 위해 inner class로 정의.
	 * 
	 * remain 모자라서 unavailable인 것만 remaining 갱신해준다.
	 * 그리고 main으로 call-back 해서 task manager에까지 전달되게끔 한다.
	 * 거기도 따로 observer를 만들 수 있었겠지만 많아서 좋을 것 없고, call-back 방식이 더 빠르다.
	 */
	class AccountObserver extends Thread {

		private static final int OBSERVATION_PERIOD = 10 * 60 * 1000;
		
		@Override
		public void run() {
			while(true) {
				try {
					synchronized(accounts) {// accounts 건들고 있을때 다른데서 추가/변경/삭제 못하게 막아둔다.(꼭 필요한 부분만 했다.)
						for(Account account : accounts) {
							if(account.getTaskStatus() != TaskStatus.WORKING) {// reserved까지도 해줘야 scheduling에 도움될 것이다.
								try {
									account.updateTaskStatus();
								} catch (InstagramLibraryException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
					
					Thread.sleep(OBSERVATION_PERIOD);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
