package HamDisModel;

public class Element {
	private int index;
	private int value;
	
	public Element(int index, int value) {
		this.index = index;
		this.value = value;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int ind) {
		this.index = ind;
	}
	
	public void setValue(int val) {
		this.value = val;
	}
	
	public int getValue() {
		return value;
	}
	
}
