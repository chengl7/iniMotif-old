package HamDisModel;

public class MyInteger implements Comparable<MyInteger>{
	private int value;
	
	public MyInteger(int val){
		value = val;
	}
	
	public int getValue(){
		return value;
	}
	
	public void set(int val){
		value = val;
	}
	
	public int compareTo(MyInteger o){
		return (this.value - o.value);
	}
	
	public boolean equals(Object o){
		MyInteger oo = (MyInteger)o;
		return (value == oo.getValue());
	}
}
