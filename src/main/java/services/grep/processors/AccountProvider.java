package main.java.services.grep.processors;

import java.util.List;

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
	
	/*
	 * constants class 따로 두는건 완전히 global한 것들이면 private constructor 해두고 쓸 수 있겠지만
	 * 일반적으로는 구분이 어렵고 양이 많아지면 복잡한 이유로, final이어도 변경될 수 있다는 점이 있지만 이렇게 local에 두는걸 추천한다 한다.
	 */
	private static final int RATE_LIMIT = 5000;
	
	private AccountCallback callback;
	private List<Account> accounts;
	private AccountObserver observer;
	
	/*
	 * inserted된 모든 account들은 하나의 thread가 돌면서 다 remaining을 새로 갱신한다.
	 * 기존 algorithm이 복잡하면, periodically하게 일정 시간마다 돌면서 free account들에 대해서만 query 1개씩 날려서 remaining 갱신할 수도 있다.
	 * (그냥 이 방법이 차라리 더 나을 수도 있다. queue에 timestamp 다 저장하고, 일일히 비교하고 하는 것보다...)
	 */
	
	public AccountProvider() {
		// 나중에는 init을 하냐 안하냐를 받을 수 있을 것 같다.
	}
	
	public void setAccountCallback(AccountCallback callback) {
		this.callback = callback;
	}
	
	/*
	 * 초기 account들 설정도 필요하다.
	 * 웬만하면 외부 input file로부터 받는것이 추후 server에서 terminal로 실행시키는 등의 작업을 할 때
	 * 일일히 recompile하지 않아도 되어서 더 편할 것이다.
	 * 그리고, 받는 것으로 끝이 아니라, 받은 후 몇가지 설정도 해야 한다.
	 */
	public void initAccount() {
		// input 받고 parsing.
		// remains, task-status까지 설정해야 한다.
		// 그리고는 callback 날린다.
	}
	
	/*
	 * 단순히 account instantiation하는게 아니다.
	 * 특히 remaining을 초기 설정 하기 위해, 최소 1개의 query를 날려서 remaining값을 받아온다.
	 * 하지만 이전에 query를 몇 개 날렸는지에 대한 정보는 없기 때문에, 예측을 할 수밖에 없다.
	 * 하지만 최대 1시간 이전에 어느 부분에서 query를 날렸는지는 절대 알 수 없다.
	 * 따라서, 최악의 경우에 맞춰서 예측을 하는 것이 낫다.
	 * 즉, remaining이 1000개라면, 나머지 4000개의 query는 지금으로부터 1시간 전에 모두 소진된 것으로 queue에 기록한다.
	 * 그래도 어쩔 수 없이 그 시각의 오차가 있을 수 밖에 없다.
	 * 따라서, query를 날릴 때 마다 항상 remaining은 받아서 입력하고, query도 계속 queue에 쌓는다. 그러다보면 queue는 정화된다.
	 * 
	 * 아니면, 그냥 queue가 빈 대로 놔두는 것이다.
	 * 다만 이렇게 하면 최대 1시간을 기다려서 remaining을 limit로 만들어두고 사용하는수밖에 없는데 이렇게 되면 너무 test와 독립적이 되어버려서 쓰기 힘들다.
	 */
	public void insertAccount() {
		
	}
	
	// 사용 간편하게 하려면, stop 등등의 절차 없이, remove에서 알아서 모든걸 해줘야 한다.
	public void removeAccount() {
		
	}
	
	// 위와 같은 맥락에서, sync, scheduling 등의 처리도 manually하게 하지 않고 내부적으로 다 해줘야 한다.
	public void modifyAccount() {
		
	}
	
	/*
	 * list 등 참조하기 위해 inner class로 정의.
	 * 
	 * remain 모자라서 unavailable이거나, 일반 free인 account들 remaining 갱신해준다.
	 * 그리고 main으로 call-back 해서 task manager에까지 전달되게끔 한다.
	 * 거기도 따로 observer를 만들 수 있었겠지만 많아서 좋을 것 없고, call-back 방식이 더 빠르다.
	 */
	class AccountObserver extends Thread {

		private static final int OBSERVATION_PERIOD = 10 * 60 * 1000;
		
		@Override
		public void run() {
			try {
				Thread.sleep(OBSERVATION_PERIOD);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
