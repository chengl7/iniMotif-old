package HamDisModel;

import java.util.regex.*;

/*
 * This class handle the full barcode
 * the first part is the sequencing batch, followed by the barcode, followed by the SELEX cycles
 * The barch could either be 0 or [A-Z]+, the barcode is a word of several letters [ACGT] long, the SELEX cycles are several digits 
 * 
 * Date: 2009.04.15
 * Author: Lu Cheng lu.cheng@cs.helsinki.fi
 */

public class Barcode {
	
	private static Pattern FullBarcodePattern;
	
	private String FullBarcode;
	private String batch;
	private String barcode;
	private String cycle;
	private boolean MatchFlag;
	
	static{
		FullBarcodePattern = Pattern.compile("([0A-Z][A-Z]*)_([ACGT]+)_([0-9]+)");
	};
	
	public Barcode() {
		this.FullBarcode = null;
		this.barcode = null;
		this.batch = null;
		this.cycle = null;
		this.MatchFlag = false;
	}
	
	public Barcode(String fullbarcode) {
		this.FullBarcode = null;
		this.barcode = null;
		this.batch = null;
		this.cycle = null;
		this.MatchFlag = false;

		parseBarcode(fullbarcode);
	}
		
	public boolean parseBarcode(String fullcode) {
		Matcher m = FullBarcodePattern.matcher(fullcode);
		
		MatchFlag = m.find();
		if(MatchFlag){
			FullBarcode = m.group();
			batch = m.group(1);
			barcode = m.group(2);
			cycle = m.group(3);
		}else{
			FullBarcode = null;
			barcode = null;
			batch = null;
			cycle = null;
			MatchFlag = false;
		}
		
		return MatchFlag;
	}
	
	public static boolean containBarcode(String str) {
		Matcher m = FullBarcodePattern.matcher(str);
		return m.find();
	}
	
	public static String extractBarcode(String str) {
		Matcher m = FullBarcodePattern.matcher(str);
		m.find();
		return m.group();
	}
	
	public String getFullBarcode() {
		if(MatchFlag){
			return FullBarcode;
		}
		return null;
	}
	
	public String getBatch() {
		if(MatchFlag){
			return batch;
		}
		return null;
	}
	
	public String getBarcode() {
		if(MatchFlag){
			return barcode;
		}
		return null;
	}
	
	public String getCycle() {
		if(MatchFlag){
			return cycle;
		}
		return null;
	}
	
	public boolean getFlag() {
		return MatchFlag;
	}
	
	public String toString(){
		return FullBarcode;
	}
	
	public static void main(String[] args) {
		Barcode bar = new Barcode();
		bar.parseBarcode("A_ACTGT_3");
		System.out.println(bar.FullBarcode);
		System.out.println(bar.batch);
		System.out.println(bar.barcode);
		System.out.println(bar.cycle);
	}
}
