package HamDisModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
 * Sort substrings according to their counts
 * 
 * Lu Cheng 24.04.2009
 */

public class SeqSort{
	private ArrayList<Element> list;
	private boolean IsSorted;
		
	public SeqSort(int[] arr){
		list = new ArrayList<Element>(arr.length);
		IsSorted = false;
		
		// add all elements into the list and order them
		for(int i=0; i<arr.length; i++){
			list.add(i, new Element(i,arr[i]));
		}
		
		this.sort();
	}	
	
	// sort the list in ascending order of count(value) 
	private void sort(){
		Comparator<Element> com = new Comparator<Element>(){
			public int compare(Element o1, Element o2){
				return (o1.getValue() - o2.getValue());
			}
		};
		Collections.sort(list, com);
		IsSorted = true;
	}
	
	//get the index() of Consensus sequence
	public int getConSeq(){
		if(!IsSorted){
			System.err.println("Error, the array is not sorted yet!");
			return -1;
		}
						
		return list.get(list.size()-1).getIndex();
	}
	
	// the index with the highest value
	public int getHighestIndex(){
		return list.get(list.size()-1).getIndex();
	}
}
