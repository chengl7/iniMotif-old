package HamDisModel;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

/*
 * the function of this class is to process the file
 * including header process and content process
 * Input: a file
 * Output: data structures in this class
 * 
 * 
 * All dir are assumed to end with "/" in this file
 */


public class FileHandler {
	
	public int WIDTH;              // width of the motif 
	public int HamDisCutOff;       // cutoff of Hamming distance to the consensus sequence
	public int SeqLen;             // sequence length in the file
	public int SeqNum;	           // sequence number in the file
	
	public double NorConstant = 1;  //constant for normalize the count matrix
	public static int MAX_SEQ_NUM = 2000000; // maximum line of input
	public static int MIN_SEQ_NUM = 10;      // minimum line of sequences in the file
    public int ExpSubstrCount = 3;          // The expected number of a w-width substring count 
	
	public double[] BackDis;       // distribution of the four bases, inclued revcom
	public int[][] BindSitePosDis;         // distribution of the position for the binding sites
                                           // the first array is for binding sites on original sequences, 
                                           //while the second is for binding sites on the reverse complementary sequences  
	
	public int[]   SubStrDis;     // distribution of the substring of width WIDTH, both seq and its revcom (except palindrome)are counted
	public int[]   OrigSubStrDis; // the original w-width substring distribution
	public int[][] BindSiteCount;	
	/*
	 * The first line BindSiteCount[0][] indicates whether the substring is RevCom, 1 means yes, 0 mean not
	 * The second line BindSiteCount[1][] means the hamming distance to the consensus sequence
	 * The third line BindSiteCount[2][] is the total number of binding site of that substring
	 * The forth line BindSiteCount[3][0] is the index of the substring
	 */ 

//	public int[] DataStore;  //indice of all the substrings, 1 sequence -- (SeqLen-WIDTH+1) substring
	public ArrayList<Integer> DataStore;        //indice of all the substrings, 1 sequence -- (SeqLen-WIDTH+1) substring    
	public int DataPointer;  //pointer for DataStore
	public int mask;		 //see method HandleLine()
	
	public double[][] motif;          // 4*WIDTH, information content-based weight matrix	
	public double[][] motif_pfm;      // 4*WIDTH, position frequence matrix
	public SeqAnalyzer SubStrTool;   // Tool for WIDTH-mer substring calculation
	public Table BarcodeTfNameTable;  // Tool for barcode cosultation
	public BufferedWriter logWriter = null; //output while processing
	public SeqPool pool;           //the pool for storing sorted sequences
	
	public String fileName;
    public Barcode FullBarcode;
	public String dir_hamdis;
	public String dir_posdis;
	public String dir_seqbias;
	public String dir_motif;
	public String dir_rawcount;

	public BufferedWriter out_hamdis;
	public BufferedWriter out_posdis;
	public BufferedWriter out_seqbias;
	public BufferedWriter out_motif;
	public BufferedWriter out_rawcount;
	
	public FileHandler(int width, int hamcut, int seqlen, Table bbt, BufferedWriter OutputWriter,
			String FileName, String dir_hamdis, String dir_posdis, String dir_seqbias, String dir_motif, String dir_rawcount, double[] backdis){
		WIDTH = width;
		HamDisCutOff = hamcut;
		SeqLen = seqlen;
		SeqNum = 0;
				
		BackDis = backdis;
		BindSitePosDis = new int[2][SeqLen-WIDTH+1];
		
		SubStrDis = new int[1<<(2*WIDTH)];
		OrigSubStrDis = new int[1<<(2*WIDTH)];
		BindSiteCount = new int[4][1<<(2*WIDTH)];
		
		DataStore = new ArrayList<Integer>(MAX_SEQ_NUM);
		DataPointer = 0;
		mask = (1<<(WIDTH*2)) - 1;
				
		motif = new double[4][WIDTH];
		motif_pfm = new double[4][WIDTH];
		SubStrTool = new SeqAnalyzer(WIDTH);
		BarcodeTfNameTable = bbt;
		logWriter = OutputWriter;
		pool = new SeqPool(SeqLen,FileName);
		
		this.fileName = FileName;
		this.FullBarcode = new Barcode(new File(FileName).getName());
		this.dir_hamdis = dir_hamdis;
		this.dir_posdis = dir_posdis;
		this.dir_seqbias = dir_seqbias;
		this.dir_motif = dir_motif;
		this.dir_rawcount = dir_rawcount;
	}
	
	/*
	 * this method is in charge of the whole processing of the file
	 * SKIP_MODE = true, if all data files have already been generated, we will skip to handle that barcode
	 * SKIP_MODE = false, generate all data files as if they do not exist at all
	 */
	public void Process(boolean SKIP_MODE){
		try{
			// check the parameters
			if(!CheckParas()){
				System.out.println("Input parameters are not correct, can not proceed!");
				return;
			}
			
			BufferedReader SeqFileReader = new BufferedReader(new FileReader(new File(fileName)));
			
			String fullbarcode = FullBarcode.getFullBarcode();
			if(fullbarcode == null){
				return;
			}
			
			// if the fullbarcode data files already exists, then we stop processing 
			if(SKIP_MODE){
				if(checkDataFiles(fullbarcode)){
					SeqFileReader.close();
					return;
				}
			}

			out_hamdis = new BufferedWriter(new FileWriter(new File(
					dir_hamdis+fullbarcode+".dat")));
			out_posdis = new BufferedWriter(new FileWriter(new File(
					dir_posdis+fullbarcode+".dis")));
		    out_seqbias = new BufferedWriter(new FileWriter(new File(
					dir_seqbias+fullbarcode+".bias")));
		    out_motif  = new BufferedWriter(new FileWriter(new File(
					dir_motif+fullbarcode+".pwm")));
		    out_rawcount = new BufferedWriter(new FileWriter(new File(
		    		dir_rawcount+fullbarcode+".cnt")));
		    
			logWriter.write("\n"+fullbarcode+"-"+BarcodeTfNameTable.getTfName(fullbarcode)+"\n");
			
			//read in the data and count the substring and so on
			String s = null;
			SeqNum = 0;
			SeqFileReader.readLine();  // read the first line of the sequence file
			while((s=SeqFileReader.readLine())!=null) {
//				System.out.println(s);
				if(HandleLine(s)==0){
					SeqNum ++;
				}				
			}
			SeqFileReader.close();
			
			if(SeqNum<MIN_SEQ_NUM){
				logWriter.write("only "+SeqNum+" sequences are available, so no computation.\n\n\n");
				closeDataBuffers();
				deleteDataFiles(fullbarcode);
				return;
			}
			logWriter.write(SeqNum+" binding site are extracted.\n");
			
			// calculate the expected number of a w-width substring
			this.ExpSubstrCount = calExpSubStrCount();
			
			//output the sequence with the highest count in the pool
			pool.Process();
			Element HighestSeq = pool.getHighest();
			if(HighestSeq.getIndex()==-1){
//				System.out.println("All sequences are less than or equal to "+this.SeqCountCutOff);
				out_posdis.write("\n");
				out_posdis.write(HighestSeq.getValue()+" "+"\n");
				out_posdis.write(pool.getUniqueSeqPortion()+"\n");
			}else{
				out_posdis.write(pool.tool.Index2Seq(HighestSeq.getIndex())+"\n");
				out_posdis.write(HighestSeq.getValue()+" "+"\n");
				out_posdis.write(pool.getUniqueSeqPortion()+"\n");
//				System.out.println("The sequence is "+pool.tool.Index2Seq(HighestSeq.index));
//				System.out.println("The count for this sequence is "+HighestSeq.value);
//				System.out.println("Total sequence count is "+SeqNum+"\n");
			}
			
			//sort the count of different substring
			SeqSort ms = new SeqSort(SubStrDis);
			
			// calculate the Hamming Distance for all candidate substring, pick out the consensus sequence
			int con_ind = SubStrTool.LowerSeq(ms.getConSeq());  // index of consensus sequence
			int[] HamDis = new int[SubStrDis.length];
			for(int i=0; i<SubStrDis.length; i++){
				HamDis[i] = SubStrTool.CalHamDis(con_ind, i);
			}
			
			// output the name of the motif, and the consensus sequence
			this.fileName = new File(fileName).getName();
			String motif_title = FullBarcode.getFullBarcode()+"-"+BarcodeTfNameTable.getTfName(FullBarcode);
			OutPFM(motif_title+"\n");
			OutPFM(WIDTH+"-width   consensus sequence: "+SubStrTool.Index2Seq(con_ind)+"\n");
			OutPFM(WIDTH+"-width reverse complemetary: "+SubStrTool.Index2Seq(SubStrTool.getRevCom(con_ind))+"\n\n");
			
			// output the averaged difference between sequence and its reverse complementary
			OutSeqBias(motif_title,OrigSubStrDis);
			
			//Calculate and output the motif; also the binding site
			CalMotif(HamDis);

			OutSubStrDis(motif_title, SubStrDis);
			
			// output SubStrCount-HamDis data
			OutHamDis(motif_title);
			
			// output bind site position distribution
			OutBindSitePosDis();
			
			// output background distribution
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.CHINA);
			nf.setMaximumFractionDigits(3);
			
			OutPFM("Background Distribution: \n");
			for(int i=0; i<4; i++){
				OutPFM(nf.format(BackDis[i])+" ");
			}
			OutPFM("\n");
			
			closeDataBuffers();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/*
	 * this method break the input sequence into w-width substrings
	 * and count the occurrence of each substring
	 * meanwhile it also count to the background distribution
	 */
	public int HandleLine(String in) throws IOException{
		if(in.length()!=SeqLen){
			System.out.println("Error! The length of the input sequence is not correct.");
			return -1;
		}
		for(int i=0; i<SeqLen; i++){
			if(SubStrTool.Base2Index(in.charAt(i))==-1){
//				System.out.println("This sequence is ignored: "+in);
				logWriter.write("This sequence is ignored: "+in+"\n");
				return -1;
			}
		}
				
		// store the index of each substring
		int[] indexes = new int[SeqLen-WIDTH+1];
		int index = SubStrTool.Seq2Index(in.substring(0, WIDTH));
		StoreSubStr(index);
		indexes[0] = index;  
		for(int i=WIDTH; i<SeqLen; i++){
			index = ((index<<2) & mask) + SubStrTool.Base2Index(in.charAt(i));
//			System.out.print(index+" ");
			indexes[i-WIDTH+1] = index;
			StoreSubStr(index);
		}
		
		// count each substring
		Arrays.sort(indexes);
		index = -1;
		for(int i=0; i<indexes.length; i++){
			if(index != indexes[i]){     //here we exclude identical substrings
				CountSubStr(indexes[i]);
				index = indexes[i];
			}
		}
//		System.out.println();
		return 0;
	}
	
	/*
	 * Count one substring
	 */
	public void CountSubStr(int index){
		OrigSubStrDis[index]++;
		
		SubStrDis[index]++;
		int RevComInd = SubStrTool.getRevCom(index); 
		
		// the palindrome needs to be considered separately
		if(RevComInd != index){
			SubStrDis[RevComInd]++;
		}
		
	}
	
	// store one substring
	private void StoreSubStr(int index) {
		DataStore.add(index);
		DataPointer++;
	}
	
	/*
	 *  find the binding site of each sequence and construct the initial motif
	 *  @parameters
	 *  HamDis: the hamming distances of all different substring to concensus sequence
	 */
	public void CalMotif(int[] HamDis){
		// calculate the count matrix
		for(int i=0; i<DataPointer; ){
			//find the candidate binding site
//			int BindInd = DataStore[i];
			int BindInd = DataStore.get(i);
			int BindSitePos = 0;
			
			boolean IsRevCom=false;
			double variation;
			for(int j=0; j<SeqLen-WIDTH+1; j++,i++){
				variation = (Math.random()-0.5)*0.1; //randomly pick a bind site with same Hamming distance  
				
				if(HamDis[BindInd] > HamDis[DataStore.get(i)]+variation){
					BindInd = DataStore.get(i);
					IsRevCom=false;
					BindSitePos = j;
				}
				if(HamDis[BindInd] > HamDis[SubStrTool.getRevCom(DataStore.get(i))]+variation){
					BindInd = SubStrTool.getRevCom(DataStore.get(i));
					IsRevCom = true;
					BindSitePos = j;
				}				
			}
			
//			System.out.println(SubStrTool.Index2Seq(BindInd)+" "+BindInd+" "+HamDis[BindInd]+" "+HamDis[SubStrTool.getRevCom(BindInd)]);
			if(HamDis[BindInd]<HamDisCutOff){
				
//				System.out.println("Bindsite: "+SubStrTool.Index2Seq(BindInd));
				AddBindSite(BindInd); // add this sequence to the motif
				
				// write the binding site
//				OutBindSite(SubStrTool.Index2Seq(BindInd));  // output the binding site

				// record the BindSitePos to the BindSitePosDis
				// record the BindSitePos to the BindSitePosDis
				if(IsRevCom){
					BindSitePosDis[1][BindSitePos]++;
				}else{
					BindSitePosDis[0][BindSitePos]++;
				}
			}
			
			// write the Hamming Distance information into SubStrDis-HamDis files
			if(IsRevCom){
				int ind = SubStrTool.getRevCom(BindInd);
				CountBindSite(1,HamDis[BindInd],ind);
			}else{
				int ind = BindInd;
				CountBindSite(0,HamDis[ind],ind);
			}
			
		}
		
		
//		DisplayMotif();
		this.OutputMotif("Alignment Matrix");
		
		//calculate the frequency matrix and background distribution
		NorMotifCount(BackDis);
		
//		DisplayMotif();
		this.OutputMotif("Frequency Matrix");
		//store the frequency matrix
		for(int i=0; i<4; i++){
			for(int j=0; j<WIDTH; j++){
				motif_pfm[i][j] = motif[i][j];  
			}
		}

		//calculate the information content based weight matrix
		for(int i=0; i<4; i++){
			for(int j=0; j<WIDTH; j++){
				motif[i][j] = Math.log(motif[i][j]/BackDis[i])/Math.log(2); // note that BackDis has already been 
				                                                            // calculated in NorMotifCount();
			}
		}
		this.OutputMotif("Information content-based weight matrix");
		
		//calcluate the the final motif for logo
		for(int j=0; j<WIDTH; j++){
			double sum = 0;
			for(int i=0; i<4; i++){
				motif[i][j] = motif[i][j]*motif_pfm[i][j];
				sum += motif[i][j];
			}
			for(int k=0; k<4; k++){
				motif[k][j] = motif_pfm[k][j] * sum;
			}
		}
		this.OutputMotif("Final Motif for logo");
		
	}
	
//	public void tempFun(int[] HamDis, int[] substrdis) throws IOException{
//
//		for(int i=0; i<DataPointer; ){
//			//find the candidate binding site
//			int BindInd = DataStore.get(i);
//			
//			boolean IsRevCom=false;
//			double variation;
//			for(int j=0; j<SeqLen-WIDTH+1; j++,i++){
//				variation = (Math.random()-0.5)*0.1; //randomly pick a bind site with same Hamming distance  
//				
//				if(HamDis[BindInd] > HamDis[DataStore.get(i)]+variation){
//					BindInd = DataStore.get(i);
//					IsRevCom=false;
//				}
//
//				if(HamDis[BindInd] > HamDis[SubStrTool.getRevCom(DataStore.get(i))]+variation){
//					BindInd = SubStrTool.getRevCom(DataStore.get(i));
//					IsRevCom = true;
//				}				
//			}
//			
//			if(HamDis[BindInd]<HamDisCutOff){
//				out_rawcount.write(substrdis[BindInd]+"\t"+calProbability(motif_pfm,BindInd)+"\t"+1+"\t"+BindInd+"\n");
//			}else{
//				out_rawcount.write(substrdis[BindInd]+"\t"+calProbability(motif_pfm,BindInd)+"\t"+0+"\t"+BindInd+"\n");
//			}
//			
//			
//		}
//		out_rawcount.close();
//	}
	
	/*
	 * add the index of the sequence into motif
	 */
	public void AddBindSite(int index){
		int MASK = 3;
		
//		System.out.println("binding site: "+SubStrTool.Index2Seq(index));
		
		int tmp = (index & MASK);
			
		motif[tmp][WIDTH-1] += 1;
		for(int i=1; i<WIDTH; i++){
			index = (index >> 2);
			tmp = (index & MASK);
			motif[tmp][WIDTH-1-i] += 1;
		}		
	}
	
	/*
	 *  this method record a binding site
	 *  IsRevCom indicate reverse complement, 1 means yes, 0 mean not
	 *  HamDis is the Hamming distance for the bind site to consensus sequence
	 *  index is the index of the substring
	 */
	public void CountBindSite(int IsRevCom, int HamDis, int index){
		BindSiteCount[0][index] = IsRevCom;
		BindSiteCount[1][index] = HamDis;
		BindSiteCount[2][index] ++;		
		BindSiteCount[3][index] = index;
	}
	
	/*
	 * Normalize Motif count Matrix
	 */ 
	public void NorMotifCount(double[] BackgroundDis){
		// normalize background distribution
		BackDis = BackgroundDis;
		
		// normalize motif count matrix
		double[] sum = new double[WIDTH];
		for(int i=0; i<WIDTH; i++){
			for(int j=0; j<4; j++){
				sum[i] += motif[j][i];
			}
			sum[i] = sum[i]+NorConstant;
		}
		for(int i=0; i<WIDTH; i++){
			for(int j=0; j<4; j++){
				motif[j][i] = (motif[j][i]+BackDis[j]*NorConstant) / sum[i];
			}
		}
	}
	
	/*
	 * Calculate the probability of a w-width sequence in log scale
	 */
	public double calProbability(double[][] pfm, int seqInd) {

		int MASK = 3;
		double probability = 0;

		int tmp = (seqInd & MASK);
		probability += Math.log(pfm[tmp][WIDTH-1]);
		
		for(int i=1; i<WIDTH; i++){
			seqInd = (seqInd>>2);
			tmp = (seqInd & MASK);
			probability += Math.log(pfm[tmp][WIDTH-i-1]);
		}
		
		return probability;
	}


	/*
	 * Calculate the expected number of a w-width sequence
	 */
	public int calExpSubStrCount() {
		int totalCount = (1<<(WIDTH*2));
		return SeqNum*(SeqLen-WIDTH+1)/10/totalCount;
	}
	

//	/*
//	 * calculate the average difference between original sequence and its revcom
//	 * the input array is the substring count distribution
//	 */
//	public void calDiffRevcom(int[] dis) {
//		int RevComInd = 0;
//		for(int i=0; i<dis.length; i++){
//			RevComInd = SubStrTool.getRevCom(i);
//			SeqBias[0][i] = Math.abs(dis[i]-dis[RevComInd]);
//			SeqBias[1][i] = dis[i] + dis[RevComInd];
//		}
//	}
	
	
	// the first line is the title
	public void OutSeqBias(String title, int[] substrdis) {
		try{
			//header
			if(title.charAt(title.length()-1)=='\n'){
				out_seqbias.write(title);
			}else{
				out_seqbias.write(title+"\n");
			}
			
			//body
			int cutoff = (ExpSubstrCount>5)?ExpSubstrCount:5;  //substr lower than cutoff will not be considered
			for(int i=0,RevComInd=0; i<substrdis.length; i++){
				RevComInd = SubStrTool.getRevCom(i);
				if(substrdis[i]>cutoff || substrdis[RevComInd]>cutoff){
					out_seqbias.write(substrdis[i]+" "+substrdis[RevComInd]+"\n");
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
//	public void OutBindSite(String seq){
//		try{
//			out_bindsite.write(seq+"\n");
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//	}
	
	/*
	 * output data for SubStrCount-HamDis figure
	 * the first column indicate reverse complement, 1 means yes, 0 mean not
	 * the second column is Hamming Distance to consensus sequence
	 * the third column is the substring count	
	 * the fourth column is the sequence index
	 */
	public void OutHamDis(String title){		
		try{
			// header
			if(title.charAt(title.length()-1)=='\n'){
				out_hamdis.write(title);
			}else{
				out_hamdis.write(title+"\n");
			}
			
			// data
			for(int i=0; i<BindSiteCount[2].length; i++){
				if(BindSiteCount[2][i]>0){
					out_hamdis.write(BindSiteCount[0][i]+" "+BindSiteCount[1][i]+" "+BindSiteCount[2][i]+" "+BindSiteCount[3][i]+"\n");
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Output the raw w-width substring distribution to the files
	 */
	public void OutSubStrDis(String title, int[] substrdis) {
		try{
			// header
			if(title.charAt(title.length()-1)=='\n'){
				out_rawcount.write(title);
			}else{
				out_rawcount.write(title+"\n");
			}
			
			// data , note that the one seq and its reverse complementary seq should be the same count
			int cutoff = (ExpSubstrCount>3)?ExpSubstrCount:3;
			int RevComInd;
			for(int i=0; i<substrdis.length; i++){
				RevComInd = SubStrTool.getRevCom(i);
				if(substrdis[i]>cutoff || substrdis[RevComInd]>cutoff){
					out_rawcount.write(i+"\t"+substrdis[i]+"\n");
				}
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	public void OutBindSitePosDis() {
		try{
			out_posdis.write(SeqNum+"\n");
			for(int[] posdis : BindSitePosDis){
				for(int i=0; i<posdis.length; i++){
					out_posdis.write(""+posdis[i]);
					if(i!=posdis.length-1){
						out_posdis.write(" ");
					}else{
						out_posdis.write("\n");
					}
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void OutPFM(String str) {
		try{
			out_motif.write(str);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * write the motif into file
	 */
	public void OutputMotif(String title){
		try{
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.CHINA);
			nf.setMaximumFractionDigits(3);
			
			out_motif.write(title+"\n");
			
			for(int i=0; i<4; i++){
				for(int j=0; j<WIDTH; j++){
					out_motif.write(nf.format(motif[i][j])+"\t");
				}
				out_motif.write("\n");
			}
			out_motif.write("\n");

		}catch(IOException e){
			e.printStackTrace();
		}		
	}
	
	/*
	 * close all buffers
	 */
	public void closeDataBuffers() {
		try{
//			out_bindsite.close();
			out_hamdis.close();
			out_posdis.close();
			out_seqbias.close();
			out_motif.close();
			out_rawcount.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * check the existences of all data files
	 */
	public boolean checkDataFiles(String fullbarcode) {
		boolean res = true;
		res = res && new File(dir_hamdis+fullbarcode+".dat").exists();
		res = res && new File(dir_posdis+fullbarcode+".dis").exists();
		res = res && new File(dir_seqbias+fullbarcode+".bias").exists();
		res = res && new File(dir_motif+fullbarcode+".pwm").exists();
		res = res && new File(dir_rawcount+fullbarcode+".cnt").exists();
		return res;
	}
	
	/*
	 * delete all generated files
	 */
	public void deleteDataFiles(String fullbarcode) {
		new File(dir_hamdis+fullbarcode+".dat").delete();
		new File(dir_posdis+fullbarcode+".dis").delete();
		new File(dir_seqbias+fullbarcode+".bias").delete();
		new File(dir_motif+fullbarcode+".pwm").delete();
		new File(dir_rawcount+fullbarcode+".cnt").delete();
	}
	
	
	/*
	 * display the motif matrix
	 */
	public void DisplayMotif(){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		
		for(int i=0; i<4; i++){
			for(int j=0; j<WIDTH; j++){
				System.out.print(nf.format(motif[i][j])+" ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	/*
	 * check the parameters so that the program work properly
	 */
	public boolean CheckParas(){
		if(this.WIDTH<5 || WIDTH>11){
			System.out.println("Width is "+WIDTH+", out of range 5-11!");
			return false;
		}
		if(HamDisCutOff<0 || HamDisCutOff > SeqLen){
			System.out.println("HamDisCutOff is "+HamDisCutOff+". Please check it again.");
			return false;
		}
		
		if(SeqLen<0){
			System.out.println("SeqLen is "+SeqLen+". Please check it again.");
			return false;
		}
		if(BarcodeTfNameTable==null){
			System.out.println("FullBarcode-TF table is null, please check it again!");
			return false;
		}
		if(logWriter==null){
			System.out.println("OutputWriter is null, please check it again!");
			return false;
		}
		if(!new File(fileName).exists()){
			System.out.println("Input file "+fileName+" does not exist!");
			return false;
		}
//		if(!new File(dir_bindsite).exists()){
//			System.out.println("Input Directory "+dir_bindsite+" does not exist!");
//			return false;
//		}
		if(!new File(dir_hamdis).exists()){
			System.out.println("Input Directory "+dir_hamdis+" does not exist!");
			return false;
		}
		if(BackDis==null){
			System.out.println("Background distribution is null!");
			return false;
		}else if(BackDis.length!=4){
			System.out.println("Length of background distribution: "+BackDis.length+". Not 4!");
			return false;
		}else{
			double sum = 0;
			for(int i=0; i<BackDis.length; i++){
				sum += BackDis[i];
			}
			if(sum!=1){
				System.out.println("Background probability does not sum to 1.");
				return false;
			}
		}
		
//		System.out.println("Input parameters OK! Start Processing.....");
		return true;
	}
}
