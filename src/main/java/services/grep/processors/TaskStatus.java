package main.java.services.grep.processors;

//추후 monitoring시 .name()으로 쉽게 출력하기 위해 enum을 간단히 사용.
public enum TaskStatus {
	UNAVAILABLE, FREE, RESERVED, WORKING;
}