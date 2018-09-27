package HamDisModel;

import java.util.*;
import java.io.*;

/*
 * this class use a Hashtable to store a pool of DNA sequences
 * the sequences needs to be sorted first
 */

public class SeqPool {
	private HashMap<String, MyInt> pool;
	public SeqAnalyzer tool;
	private String FileName; // name for the Sequence Pool

	private int SeqNum;
	
	public SeqPool(int SeqLen, String filename) {
		pool = new HashMap<String, MyInt>(20000);
		tool = new SeqAnalyzer(SeqLen);
		FileName = filename;
		
		SeqNum = 0;
	}

	/*
	 * read all the sequences into the list
	 */
	public void Process() {
		try {
			// read each sequence into the pool list
			BufferedReader br = new BufferedReader(new FileReader(new File(
					FileName)));
			String s = br.readLine();
			while ((s = br.readLine()) != null) {
				putString(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * insert one string to the pool
	 */
	private boolean putString(String seq) {
		int ind = tool.Seq2Index(seq);
		if (ind == -1){
			return false;
		}			

		if(pool.containsKey(seq)){
			pool.get(seq).val++;
		}else{
			pool.put(seq, new MyInt(1));
		}
		
		SeqNum++;
		return true;
	}

	/*
	 * get the sequence with the highest count The second of the result is the
	 * count of the high sequence
	 */
	public Element getHighest() {
		Element res = new Element(-1, -1);
		if (pool.size() == 0) {
			return res;
		} else {
			Set<String> keys = pool.keySet();
			String key;
			int value;
			Iterator<String> ite = keys.iterator();
			while (ite.hasNext()) {
				key = ite.next();
				value = pool.get(key).val;
				if (value > res.getValue()) {
//					System.out.println(key+" "+value);
					res.setIndex(tool.Seq2Index(key));
					res.setValue(value);
				}
			}
			return res;
		}
	}
	
	public double getUniqueSeqPortion() {
		double res = pool.size();
		return res/SeqNum;
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println("start time: " + start);

		SeqPool tst = new SeqPool(
				14,
				"/home/lcheng/data/09.08.08/ready_barcodepools/EMBL_seq_7.8.08/part1/AAAAT_1.txt-86078-reads.txt");

		tst.Process();

		Element res = tst.getHighest();
		System.out.println(tst.tool.Index2Seq(res.getIndex()));
		System.out.println(res.getIndex());
		System.out.println(res.getValue());

		long end = System.currentTimeMillis();
		System.out.println("end time: " + end);
		System.out.println("cost time: "+((end-start)));

	}

	class MyInt {
		int val;

		MyInt(int val) {
			this.val = val;
		}
	}
}
