package HamDisModel;

import java.util.*;
import java.util.regex.*;
import java.io.*;

/*
 * this version has solved the problem of palindrome re-counting
 * as well as random pick a bind site from those with the same Hamming distance
 * modified by Lu, 08.10.2008
 * 
 * Add a subfold for motifs
 * modified by Lu, 23.01.2009
 * 
 * The back ground distribution and TF-barcode table files are saved
 * modified by Lu, 02.02.2009
 * 
 * The raw SubString distribution is stored in a new folder
 * modified by Lu, 23.02.2009
 * 
 * A test function has been added for drawing pictures, Lu Cheng, 11.03.2009
 * 
 * For the '-uniform' option, we assume all batches and all cycles share the same barcode-TF table, thus the backdis, barcode-TF files are stored in the main dir
 * modified by Lu, 25.03.2009
 * 
 * The program is modified to process all "batch-barcode-cycle" files in one folder
 * modified by Lu, 03.04.2009
 * 
 * Barcode class has been added, and Table class has been rewritten
 * modified by Lu, 15.04.2009
 * 
 * The hamming distance cutoff has been set to 1/4 of the width of the motif, e.g for width 6,7,8,9,10,11,12, the cutoffs are 3,3,3,3,4,4,4,
 * modified by Lu, 20.04.2009 
 * 
 * A inner class Parameters is added for better handling of the data
 * -cycle, -batch, -allbatches, -uniform mode has been added
 * Add the -skip mode, in which the program ignores a barcode file if its output files already exists
 * modified by Lu, 24.04.2009
 * 
 * All direcoties are assumed to end with '/' in the source code
 */

public class InitialMotif {
	public static String InputDir;
	public static String OutputDir;
	public static String BackDisFile = null;
	public static String BarcodeTfFile = null;
	
	public static int WIDTH;  // the width of the motif
	public static int HamDisCutOff;
	public static int SeqLen;
	
	public static double[] BackDis;
	
	public static Table BarcodeTfNameTable;
	public static BufferedWriter LogWriter = null; 
	
//	public static String dir_bindsite;
	public static String dir_hamdis;
	public static String dir_posdis;
	public static String dir_seqbias;
	public static String dir_motif;
	public static String dir_rawcount;
	
	public static FilenameFilter CycleFilter; 
	public static FilenameFilter BatchFilter;
	public static FilenameFilter BarcodeFilter;
	
	static{
		CycleFilter = new FilenameFilter(){
			public boolean accept(File dir, String name){
				Pattern pat = Pattern.compile("\\d+");
				return pat.matcher(name).matches();
			}
		};
		
		BatchFilter = new FilenameFilter(){
			public boolean accept(File dir, String name){
				Pattern pat = Pattern.compile("[0A-Z][A-Z]*");
				return pat.matcher(name).matches();
			}
		};
		
		BarcodeFilter = new FilenameFilter(){
			public boolean accept(File dir, String name){
				return Barcode.containBarcode(name);
			}
		};
	}
	
	public InitialMotif(){
		
	}
	
	// InputDir,OutputDir,BackDisFile,BacodeFile,TfNameFile are not initialized
	public InitialMotif(int width, int hamiscutoff, int seqlen){
		WIDTH = width;
		HamDisCutOff = hamiscutoff; 
		BackDis = new double[4];
		
//		dir_bindsite = OutputDir+"/bind_site_pool/";
		dir_hamdis = OutputDir+"/SubStrDis-HamDis/";
		dir_posdis = OutputDir+"/BindSitePosDis/";
		dir_seqbias = OutputDir+"/SeqBias/";
		dir_motif = OutputDir+"/motif/";
		dir_rawcount = OutputDir+"/SubStrDis/";
//		new File(dir_bindsite).mkdirs();
		new File(dir_hamdis).mkdirs();
		new File(dir_posdis).mkdirs();
		new File(dir_seqbias).mkdirs();
		new File(dir_motif).mkdirs();
		new File(dir_rawcount).mkdirs();
		
		LogWriter = null;
		
		BarcodeTfNameTable = new Table(BarcodeTfFile);		
	}
	
	
	/*
	 * This method processes all files under in one cycle directory, which shall the same parameter settings
	 */
	public static void BatchProcess(String[] args, boolean SKIP_MODE){
		try{
//			System.out.println("Please input arguments in the following order: ");
//			System.out.println("1: Input Directory\n"+"2: Output Directory\n"
//					+"3: Background Distribution File\n"+"4: Barcode-TfNameFile\n"
//					+"5: MotifWidth\n"+"6: Input sequence length: "+"\n");
			
			if(checkInputParas(args)){
				System.out.println("Input OK. Start processing.");				
			}else{
				System.out.println("Input NOT OK. Try again, please!");
				return;
			}
			
            // create an instance and start processing the data
			new InitialMotif();  // initialize the class
			
            // assign parameters
			InputDir = args[0];
			OutputDir = args[1];
			BackDisFile = args[2];
			BarcodeTfFile = args[3];
			WIDTH = Integer.parseInt(args[4]);
			SeqLen =  Integer.parseInt(args[5]);
			
			HamDisCutOff = (int)Math.round((double)WIDTH/4)+1;  // the Hamming distance is set to 1/4 of the width of the motif
			
			System.out.println("Processing... Be patient, it might take a few minutes!\n");
			
			InitialMotif imot = new InitialMotif(WIDTH,HamDisCutOff,SeqLen);
			
			// construct binding site files and Hamming Distance files
			System.out.println("Constructing binding site files and Hamming Distance files.....");
			LogWriter = new BufferedWriter(new FileWriter(new File(OutputDir+"log.txt")));
			LogWriter.write(Calendar.getInstance().getTime()+"\n\n");

			
            // write parameter seting into the output file
			LogWriter.write("Your input parameters are as follows: \n");
			LogWriter.write("1: Input Directory: "+args[0]+"\n");
			LogWriter.write("2: Output Directory: "+args[1]+"\n");
			LogWriter.write("3: Background Distribution File: "+args[2]+"\n");
			LogWriter.write("4: Barcode-TfName File: "+args[3]+"\n");
			LogWriter.write("5: Motif Width: "+args[4]+"\n");
			LogWriter.write("6: Input sequence length: "+args[5]+"\n\n");
			LogWriter.write("7: Hamming Distance CutOff is set as: "+HamDisCutOff+"\n\n");	
									
			String[] fn = new File(InputDir).list(BarcodeFilter);
			for(int i=0; i<fn.length; i++){
				System.out.println(fn[i]);
				imot.handleBackgroundDis(BackDisFile);
				FileHandler fh = new FileHandler(WIDTH, HamDisCutOff, SeqLen, BarcodeTfNameTable, LogWriter,
						(InputDir+fn[i]), dir_hamdis,dir_posdis, dir_seqbias, dir_motif,dir_rawcount, BackDis);
				fh.Process(SKIP_MODE);
			}
			LogWriter.close();
			System.out.println("Binding site and HamDis done!\n");
			
			// construct logos
			System.out.println("Constructing logos from binding site files......");
						
			String CurDir = new File(new File("t.tmp").getAbsolutePath()).getParentFile().getAbsolutePath();
	        System.out.println("current directory: "+CurDir+"\n");
			
			String logo_dir = new File(OutputDir+"logo/").getAbsolutePath();
			new File(logo_dir).mkdirs();
			
			drawLogo(CurDir+"/CreateLogo",dir_motif,logo_dir);

			System.out.println("\n----------------------------------------------------\n");
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void drawLogo(String CurrentDir, String motif_dir, String logo_dir) {
		try{
//			perl CreateLogo.pl -NORMAL /home/lcheng/test/(DrawLogo) /home/lcheng/test/motif/(input) /home/lcheng/test/barcode/(output)
			CurrentDir = addSuffix(CurrentDir);
			motif_dir = addSuffix(motif_dir);
			logo_dir = addSuffix(logo_dir);
			
			Runtime rt = Runtime.getRuntime();
			
//			System.out.println("perl "+CurrentDir+"CreateLogo.pl -NORMAL "+CurrentDir+" "+motif_dir+" "+logo_dir);
			
			Process proc = rt.exec("perl "+CurrentDir+"CreateLogo.pl -NORMAL "+CurrentDir+" "+motif_dir+" "+logo_dir);
			
			int exitVal = proc.waitFor();
	        if(exitVal==0){
	        	System.out.println("Logos are successfully constructed.");
	        }else{
	        	System.out.println("Failure in constructing Logos.");
	        }
			System.out.println("Process exitValue: " + exitVal);
		}catch(InterruptedException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	// add a suffix '/' to the given string
	public static String addSuffix(String str) {
		str = str.replaceAll("/+", "/");
		if(str.endsWith("/")){
			return str;
		}else{
			return str+"/";
		}
	}
	
	// remove the suffix '/' of the given string
	public static String rmSuffix(String str) {
		str = str.replaceAll("/+", "/");
		int LEN = str.length();
		if(str.endsWith("/")){
			return str.substring(0, LEN-1);
		}else{
			return str;
		}
	}
	
	
	public static boolean checkInputParas(String[] args){
		try{
//			 handle input arguments
			System.out.println("Your input parameters are as follows: ");
			System.out.println("1: Input Directory: "+args[0]);
			System.out.println("2: Output Directory: "+args[1]);
			System.out.println("3: Background Distribution File: "+args[2]);
			System.out.println("4: Barcode-TfName File: "+args[3]);
			System.out.println("5: Motif Width: "+args[4]);	
			System.out.println("6: Input sequence length: "+args[5]);
			
			System.out.println();
			
			// check input directories and create output directories
			if(!new File(args[0]).exists() || !new File(args[0]).isDirectory()){
				System.out.println("Input directory doesn't exists. Please check again!");
				return false;
			}
			
			// The directories of the input should all end in '/'
			if(!args[0].endsWith("/") || !args[0].endsWith("/")){
				System.out.println("Directories should end with '/'! Please check the directotries again!"+"\n");
				return false;
			}
			
			new File(args[1]).mkdirs();
			if(!new File(args[2]).exists() || !new File(args[2]).isFile()){
				System.out.println("Background Distribution File doesn't exists. Please check again!");
				return false;
			}
			if(!new File(args[3]).exists() || !new File(args[3]).isFile()){
				System.out.println("Barcode-TfName File doesn't exists. Please check again!");
				return false;
			}
			
			Integer.parseInt(args[4]);
			Integer.parseInt(args[5]);
			
			return true;
		}catch(NumberFormatException e){
			return false;
		}		
	}
		
	// initialize backgrond distribution using external background Distribution file
	// four lines in the file, represent ratios of A,C,G,T
	public void handleBackgroundDis(String fn){
		try{
			if(fn==null) return;
			
			Scanner sc = new Scanner(new File(fn));
			sc.useLocale(Locale.CHINA);
			BackDis[0] = sc.nextDouble();
//			System.out.println("0: "+BackDis[0]);
			BackDis[1] = sc.nextDouble();
//			System.out.println("1: "+BackDis[1]);
			BackDis[2] = sc.nextDouble();
//			System.out.println("2: "+BackDis[2]);
			BackDis[3] = sc.nextDouble();
//			System.out.println("3: "+BackDis[3]);
			
			// normalize background distribution
			double backsum = 0;
			for(int i=0; i<4; i++){
				backsum += BackDis[i];
			}
			for(int i=0; i<4; i++){
				BackDis[i] /= backsum;
			}					
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}		
	}
	
	/*
	 * this method copies the source file to the target file
	 */
	public static void copy(String source, String target) {
		try{
		    BufferedReader in = new BufferedReader(new FileReader(source));
		    BufferedWriter out = new BufferedWriter(new FileWriter(target));
		    
		    String line = null;
		    while((line=in.readLine())!=null){
		    	out.write(line+"\n");
		    }
		    
//		    int c;
//		    while ((c = in.read()) != -1){
//		    	out.write(c);
//		    }
		
		    in.close();
		    out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/*
	 * this method copys a directory to another direcotry
	 */
//	public static int copy(String source, String target){
//		System.out.println("copy the source files into new directory");
//		try{						
//			Runtime cp = Runtime.getRuntime();
//			Process cpp = cp.exec("cp "+source+" "+target);
//			int res = cpp.waitFor();
//			if(res==0){
//				System.out.println(source + " has been copied!");
//				return 0;
//			}else{
//				System.out.println("Something wrong with the copy.");
//				return -1;
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return -1;
//	}
	
	/*
	 * orig_dir is the input directory for original files in format like "batch-barcode-cycle"
	 * OUPUT_DIR is the output directory for storing the splited data, under the "sourcedata" subfolder
	 * This method assigns the sequence files from orig_dir into subfolders in OUTPUT_DIR according to batches and cycles
	 */
	public static HashSet<String> splitData(String orig_dir, String OUTPUT_DIR) {

		if(!orig_dir.endsWith("/")){
			orig_dir += "/";
		}
		
		if(!OUTPUT_DIR.endsWith("/")){
			OUTPUT_DIR += "/";
		}
		
		String SOURCE_DIR = OUTPUT_DIR + "sourcedata/";
		Barcode FileNameParser = new Barcode();
		
		HashSet<String> batches = new HashSet<String>();
		HashSet<String> cycles = new HashSet<String>();

		// step 1: get all batches
		String[] fn = new File(orig_dir).list(BarcodeFilter);
		for(int i=0; i<fn.length; i++){
			FileNameParser.parseBarcode(fn[i]);
			batches.add(FileNameParser.getBatch());
		}
		
		// step 2: put files into different batch folders
		for(String batch : batches){
			new File(SOURCE_DIR+batch).mkdir();
		}
		
		for(int i=0; i<fn.length; i++){
			FileNameParser.parseBarcode(fn[i]);
			copy(orig_dir+fn[i],SOURCE_DIR+FileNameParser.getBatch()+"/"+ fn[i]);
		}
		
		// step 3: continue put files into subfolders
		for(String CurBatch: batches){
			fn = new File(SOURCE_DIR+CurBatch).list(BarcodeFilter);
			
			//step 3.1: get all cycles in one batch
			cycles.clear();
			for(int i=0; i<fn.length; i++){
				if(FileNameParser.parseBarcode(fn[i])){
					cycles.add(FileNameParser.getCycle());
				}
			}
			
			//step 3.2: create cycles for one batch
			for(String cycle: cycles){
				new File(SOURCE_DIR+CurBatch+"/"+cycle).mkdir();
			}
						
			//step 3.3: put files in different folders
			for(int i=0; i<fn.length; i++){
				if(FileNameParser.parseBarcode(fn[i])){
					new File(SOURCE_DIR+CurBatch,fn[i]).renameTo(new File(SOURCE_DIR+CurBatch+"/"+FileNameParser.getCycle()+"/", fn[i]));
				}
			}
		}
		
		return batches;
	}
	
	
	
	public static void main(String[] args){

		InitialMotif imot = new InitialMotif();
		String CurDir = new File(new File("t.tmp").getAbsolutePath()).getParentFile().getAbsolutePath();
		String SKIP_MODE = "-skip";
		
		if(args==null){
			System.out.println("Parameter Input format: ");
			System.out.println("-manual");
			System.out.println("-uniform");
			System.out.println("-manual: you can set all the parameters in para.txt under input directory");
			System.out.println("\n\n");
		}else if(args[0]!=null && args[0].equals("-cycle")){
			
			Parameters paras = imot.new Parameters(CurDir+"/paras/paras.txt");
			String[] input_para = paras.paras;
			String CYCLE_DIR = paras.getInputDir();
			String OUT_DIR = paras.getOutputDir();
			int min_WIDTH = paras.min_WIDTH;
			int max_WIDTH = paras.max_WIDTH;
			
			for(int i=min_WIDTH; i<=max_WIDTH; i++){
				input_para[0] = CYCLE_DIR;
				input_para[1] = OUT_DIR+"WIDTH"+i+"/";
				input_para[4] = ""+i;
				
				if(args.length>1 && args[1].equals(SKIP_MODE)){
					BatchProcess(input_para, true);
				}else{
					BatchProcess(input_para, false);
				}
			}

		}else if(args[0]!=null && args[0].equals("-batch")){

			Parameters paras = imot.new Parameters(CurDir+"/paras/paras.txt");
			String[] input_para = paras.paras;
			String BATCH_DIR = paras.getInputDir();
			String OUT_DIR = paras.getOutputDir();
			int min_WIDTH = paras.min_WIDTH;
			int max_WIDTH = paras.max_WIDTH;

			String[] CycleNames = new File(BATCH_DIR).list(CycleFilter);
			for(int i=0; i<CycleNames.length; i++){
				for(int j=min_WIDTH; j<=max_WIDTH; j++){
					input_para[0] = BATCH_DIR+CycleNames[i]+"/";
					input_para[1] = OUT_DIR+CycleNames[i]+"/"+"WIDTH"+j+"/";
					input_para[4] = ""+j;
					
					if(args.length>1 && args[1].equals(SKIP_MODE)){
						BatchProcess(input_para, true);
					}else{
						BatchProcess(input_para, false);
					}
					
				}
			}

		}else if(args[0]!=null && args[0].equals("-allbatches")){
			
			Parameters paras = imot.new Parameters(CurDir+"/paras/paras.txt");
			String[] input_para = paras.paras;
			String SOURCE_DIR = paras.getInputDir();
			String RESULT_DIR = paras.getOutputDir();
			int min_WIDTH = paras.min_WIDTH;
			int max_WIDTH = paras.max_WIDTH;
			
			// modify parameters, input dir, and output dir
			String[] CycleNames;
			String[] BatchNames = new File(SOURCE_DIR).list(BatchFilter);
			for(int k=0; k<BatchNames.length; k++){

				CycleNames = new File(SOURCE_DIR+BatchNames[k]).list(CycleFilter);
				for(int i=0; i<CycleNames.length; i++){
				
					for(int j=min_WIDTH; j<=max_WIDTH; j++){
						input_para[0] = SOURCE_DIR+BatchNames[k]+"/"+CycleNames[i]+"/";
						input_para[1] = RESULT_DIR+BatchNames[k]+"/"+CycleNames[i]+"/"+"WIDTH"+j+"/";
						input_para[4] = ""+j;
						
						if(args.length>1 && args[1].equals(SKIP_MODE)){
							BatchProcess(input_para, true);
						}else{
							BatchProcess(input_para, false);
						}
					}
				}
			}
			
			// backup the Back.dis, TF-barcode table
			copy(input_para[2],RESULT_DIR+"BackDis.txt");
			copy(input_para[3],RESULT_DIR+"/Barcode-TfName.txt");
			copy(CurDir+"/Well_Plate_Barcode.txt",RESULT_DIR+"/Well_Plate_Barcode.txt");

		}else if(args[0]!=null && args[0].equals("-uniform")){
			
			Parameters paras = imot.new Parameters(CurDir+"/paras/paras.txt");
			String[] input_para = paras.paras;
			String SOURCE_DIR = paras.getInputDir();
			final String OUTPUT_DIR = paras.getOutputDir();
			int min_WIDTH = paras.min_WIDTH;
			int max_WIDTH = paras.max_WIDTH;
			
			// create the new input and output data
			new File(OUTPUT_DIR+"sourcedata").mkdir();
			new File(OUTPUT_DIR+"IniMotifOutput").mkdir();
			
			// reassigns all files
			HashSet<String> batches = splitData(SOURCE_DIR,OUTPUT_DIR);
			System.out.println("Data has been assigned to different folders!");
			
			// modify parameters, input dir, and output dir
			String[] CycleNames;
			for(String batch: batches){
				CycleNames = new File(OUTPUT_DIR+"sourcedata/"+batch).list(CycleFilter);
				for(int i=0; i<CycleNames.length; i++){
					for(int j=min_WIDTH; j<=max_WIDTH; j++){
						input_para[0] = OUTPUT_DIR+"sourcedata/"+batch+"/"+CycleNames[i]+"/";
						input_para[1] = OUTPUT_DIR+"IniMotifOutput/"+batch+"/"+CycleNames[i]+"/"+"WIDTH"+j+"/";
						input_para[4] = ""+j;
						
						if(args.length>1 && args[1].equals(SKIP_MODE)){
							BatchProcess(input_para, true);
						}else{
							BatchProcess(input_para, false);
						}
					}
				}
			}
			
			// backup the Back.dis, TF-barcode table
			String outdir = OUTPUT_DIR+"IniMotifOutput/";
			copy(input_para[2],outdir+"/BackDis.txt");
			copy(input_para[3],outdir+"/Barcode-TfName.txt");
			copy(CurDir+"/Well_Plate_Barcode.txt",outdir+"/Well_Plate_Barcode.txt");
			
		}else{
			System.out.println("Unknown option!");
		}
			
	}
	
	class Parameters{
		String[] paras;
		int min_WIDTH;
		int max_WIDTH;

//		Parameter Structure
//		0: Description
//		1: Input Directory
//		2: Output Directory
//		3: Background Distribution File
//		4: Barcode-TfNameFile
//		5: Minimum MotifWidth
//		6: Maximum MotifWidth
//		7: Input sequence length
		
		Parameters(String parafile){
			try{
				BufferedReader br = new BufferedReader(new FileReader(parafile));
				
				br.readLine(); //explanation of the input parameters
				
				paras = new String[6];
				paras[0] = addSuffix(br.readLine());
				paras[1] = addSuffix(br.readLine());
				paras[2] = br.readLine();
				paras[3] = br.readLine();
				paras[4] = "";
				min_WIDTH = Integer.parseInt(br.readLine());
				max_WIDTH = Integer.parseInt(br.readLine());
				paras[5] = br.readLine();
				
				br.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		String getInputDir() {
			return paras[0];
		}
		
		String getOutputDir() {
			return paras[1];
		}
	}
}