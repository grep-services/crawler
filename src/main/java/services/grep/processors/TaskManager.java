package main.java.services.grep.processors;

/**
 * 
 * 일단 작업량을 구할 수 있어야 한다. 이것은 DBAccessor와 동조할 필요도 있다.
 * 구해진 작업량에 대해서, 구해올 수 있는 account들을 조사하고, 결론적으로 task들을 생성한다.
 * Rescheduling은 아직은 account를 추가할 때만 한다.
 * account를 삭제할 경우는 어차피 자동으로 관여되게 되어있고
 * task들이 사실상 비슷한 시간 내에 끝나게끔 scheduling되었을 것이므로
 * 어떤 task가 조금 먼저 끝난다고 해도 그냥 넘어간다.
 * 즉, 단순한 multi-threading 방식이면 될 것이다.
 * 
 * @author marine
 * @since 150706
 *
 */
public class TaskManager {

	public TaskManager() {
		// TODO Auto-generated constructor stub
	}

}

class Task extends Thread {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
	}
	
	private void asdf() {
		
	}
	
}