package HamDisModel;

import java.util.*;
import java.io.*;

public class Table {
	/*
	 * This class defines the table from a full barcode to the TF name
	 * The key includes the sequencing batch, underline, and barcode, such as A_ACTGC
	 * Note that the cycle has be trimmed out of the full barcode 
	 */
	private HashMap<String, String> BarcodeTfTable;
	
	public Table(){
		BarcodeTfTable = new HashMap<String, String>(300);
	}
	
	public Table(String FileName){
		BarcodeTfTable = new HashMap<String, String>(300);
		buildBarcodeTfTable(FileName);
	}
	
	public void buildBarcodeTfTable(String FileName) {
		try{
			BufferedReader buf = new BufferedReader(new FileReader(new File(FileName)));
			String line = null, key = null, val = null;
			String[] res;
			Barcode tmpBarcode = new Barcode();
			while( (line=buf.readLine())!=null ){
				res = line.split("\t");
				tmpBarcode.parseBarcode(res[0]);
				key = tmpBarcode.getBatch()+"_"+tmpBarcode.getBarcode();
				val = res[1];
				BarcodeTfTable.put(key, val);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public String getVal(String barcode){
		if(BarcodeTfTable.containsKey(barcode)){
			return BarcodeTfTable.get(barcode);
		}else{
			return "nocode";
		}
	}
	
	public String getTfName(Barcode bar){
		String key = bar.getBatch()+"_"+bar.getBarcode();
		return getVal(key);		
	}
	
	public String getTfName(String fullbarcode){
		Barcode tmpBarcode = new Barcode(fullbarcode);
		String key = tmpBarcode.getBatch()+"_"+tmpBarcode.getBarcode();
		return getVal(key);
	}
	
	public String getTfName(String batch, String barcode){
		return getVal(batch+"_"+barcode);
	}
	
}
