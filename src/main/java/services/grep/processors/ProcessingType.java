package main.java.services.grep.processors;

//추후 monitoring시 .name()으로 쉽게 출력하기 위해 enum을 간단히 사용.
public enum ProcessingType {
	// task 할당시 pararell부터 해야 안정적이고, 그다음은 both, 그다음은 serial, 이렇기 때문에 numeric value 할당했다.
	NONE(0), SERIAL(1), PARARELL(3), BOTH(2);
	
	private int weight;
	
	ProcessingType(int weight) {
		this.weight = weight;
	}
	
	public int getWeight() {
		return weight;
	}
}
