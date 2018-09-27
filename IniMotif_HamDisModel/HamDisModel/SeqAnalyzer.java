package HamDisModel;

public class SeqAnalyzer{
	private int LEN; // the width of all the sequences	
	private int IndexLen;  // the length of index of the sequences
	
	public SeqAnalyzer(int LEN){
		this.LEN = LEN;
		IndexLen = (1<<(2*LEN));
	}
	
	public int getLength(){
		return LEN;
	}
	
	//	 transfer base to index
	public int Base2Index(char base){
		switch (base){
		case 'a':
		case 'A': return 0;
		case 'c':
		case 'C': return 1;
		case 'g':
		case 'G': return 2;
		case 't':
		case 'T': return 3;
		default: //System.out.println("illegal symbol \'"+base+"\' in the string!");
		         return -1;
		}
	}

	// transfer index to base
	public char Index2Base(int ind){
		switch (ind){
		case 0: return 'A';
		case 1: return 'C';
		case 2: return 'G';
		case 3: return 'T';
		default: System.out.println("error, illegal base");
		         return 'X';
		}
	}
	
	// get the index for a sequence
	// AAAA will be 0; AAAC is 1; TTTT will be (4^4-1)
	public int Seq2Index(String str){
		if(str.length()!=LEN){
			System.out.println("Error! Inconsistent input sequence length.");
			System.out.println("Input String: "+str+" Width: "+LEN);
			return -1;
		}
		
		int index = 0;
		for(int i=0; i<LEN;i++){
			index += Base2Index(str.charAt(i));
			if(i<LEN-1){
				index = index << 2;
			}
		}
		return index;
	}
	
	//reconstruct a sequence from the index
	public String Index2Seq(int index){
		int MASK = 3;
		char[] arr = new char[LEN];
		int tmp = (index & MASK);
		
		arr[LEN-1] = Index2Base(tmp);
		for(int i=1; i<LEN; i++){
			index = (index >> 2);
			tmp = (index & MASK);
			arr[LEN-i-1] = Index2Base(tmp);
		}		
		return new String(arr);
	}
	
    // get the reverse complementary String of s
	public String getRevCom(String s){
		char[] res = s.toCharArray();
		for(int i=0; i<res.length; i++){
			res[res.length-1-i] = getComplement(s.charAt(i));						 
		}
		return new String(res);
	}
	
	// get Complementary char
	public char getComplement(char ch){
		switch(ch){
		case 'a':
		case 'A': return 'T';
		case 'c':
		case 'C': return 'G';
		case 'g':
		case 'G': return 'C';
		case 't':
		case 'T': return 'A';
		default: System.err.println("Error! Illegal char in getComplement! Input char: "+ch);
		}
		return ch;
	}
	
	// get index of reverse complementary String of s
	public int getRevCom(int ind){
		if(ind<0 || ind>=IndexLen){
			System.err.println("Error! Index out of boundary. ind: "+ind);
		}
		
		int MASK = 3;		
		int tmp = (ind & MASK);
		int res = MASK-tmp;
		
		for(int i=1; i<LEN; i++){
			ind = (ind >> 2);
			res = (res << 2);
			tmp = 3 - (ind & MASK);
			res = (res | tmp);						
		}		
		return res;
	}
	
	// transfer a sequence to lower range if it is in upper range of sequence space
	// eg. 'GTTT' will be converted into 'AAAC' because of larger index, while 'AAAC' will stay the same
	public int LowerSeq(int ind){
		int res = getRevCom(ind);
		return (ind<res)?ind:res;		
	}
	
	public String LowerSeq(String s){				
		return Index2Seq(LowerSeq(Seq2Index(s)));
	}
	
    //calculate Hamming Distance
	public int CalHamDis(String s1, String s2){
		if(s1.length() != s2.length()){
			System.out.println("Error! Inconsistant Distance.");
			return -1;
		}		
		int dis = 0;
		for(int i=0; i<s1.length();i++){
			if(s1.charAt(i)!=s2.charAt(i)){
				dis++;
			}
		}
		return dis;		
	}
	
	//calculate Hamming Distance
	// the input parameters are the index of two sequences
	public int CalHamDis(int ind1, int ind2){
		
//		System.out.println(Index2Seq(ind1));
//		System.out.println(Index2Seq(ind2));
		
		int MASK = 3;
		int dis = 0;          //Hamming distance
		
		int tmp1 = (ind1 & MASK);
		int tmp2 = (ind2 & MASK);
		
		if(tmp1!=tmp2) dis++;
		
		for(int i=1; i<LEN; i++){
			ind1 = (ind1 >> 2);
			ind2 = (ind2 >> 2);
			
			tmp1 = (ind1 & MASK);
			tmp2 = (ind2 & MASK);
			
			if(tmp1!=tmp2) dis++;
		}
			
//		System.out.println("Hamdis: "+dis+"\n");
		
		return dis;		
	}
	
	// this method checks the input values 
	public boolean isLegal(int ind){
		if(ind>=0 && ind<IndexLen){
			return true;
		}else{
			return false;
		}
	}
	public boolean isLegal(String seq){
		if(seq.length()!=LEN) return false;
		int tmp;
		for(int i=0; i<LEN; i++){
			tmp = Base2Index(seq.charAt(i));
			if(tmp==-1) return false;
		}
		return true;
	}
	
	// get the length of the sequence
	public int getSeqLen(){
		return LEN;
	}
	
	public static void main(String[] args){
		SeqAnalyzer tst = new SeqAnalyzer(7);
		
		System.out.println(tst.Seq2Index("AAAAAAC"));
		System.out.println(tst.Seq2Index("AAAAGAC"));
		System.out.println(tst.Seq2Index("AAAATCA"));
		System.out.println(tst.Seq2Index("GAAGTAT"));
		System.out.println(tst.Seq2Index("GAATTAT"));
		System.out.println(tst.Seq2Index("CACATAT"));
		System.out.println(tst.Seq2Index("CATATAT"));
		System.out.println(tst.Seq2Index("TAAATAA"));
		System.out.println(tst.Seq2Index("TAAATAA"));
		System.out.println(tst.Seq2Index("TTTTTTT"));
		System.out.println(tst.Seq2Index("AAAAAAA"));
		
		System.out.println("\n\n\n");
		
		System.out.println(tst.getRevCom(tst.Seq2Index("AAAAAAC")));
		System.out.println(tst.getRevCom(tst.Seq2Index("AAAAGAC")));
		System.out.println(tst.getRevCom(tst.Seq2Index("AAAATCA")));
		System.out.println(tst.getRevCom(tst.Seq2Index("GAAGTAT")));
		System.out.println(tst.getRevCom(tst.Seq2Index("GAATTAT")));
		System.out.println(tst.getRevCom(tst.Seq2Index("CACATAT")));
		System.out.println(tst.getRevCom(tst.Seq2Index("CATATAT")));
		System.out.println(tst.getRevCom(tst.Seq2Index("TAAATAA")));
		System.out.println(tst.getRevCom(tst.Seq2Index("TAAATAA")));
		System.out.println(tst.getRevCom(tst.Seq2Index("TTTTTTT")));
		System.out.println(tst.getRevCom(tst.Seq2Index("AAAAAAA")));
	}
}