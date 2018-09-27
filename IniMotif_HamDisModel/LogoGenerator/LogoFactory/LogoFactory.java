package LogoFactory;

import java.io.*;

public class LogoFactory {
	private String CurrDir;
	private PwmFileHandler handler;
	
	public LogoFactory(PwmFileHandler _handler){
		CurrDir = new File(new File("t.tmp").getAbsolutePath()).getParentFile().getAbsolutePath();
		this.handler = _handler;
	}
	
	private void drawLogo(String mode, String title, String matrix_ref, String out_file){
		try{
			Runtime rt = Runtime.getRuntime();
//			matrix_ref = matrix_ref.subSequence(0, matrix_ref.length()-1).toString();
			String[] cmd = new String[6];
			cmd[0] = "perl";
			cmd[1] = CurrDir+"/DrawLogo.pl";
			cmd[2] = mode;
			cmd[3] = title;
			cmd[4] = matrix_ref;
			cmd[5] = out_file;
//			String cmd = "perl "+CurrDir+"/DrawLogo.pl "+mode+" "+title+" '"+matrix_ref+"' "+out_file;
//			System.out.println(matrix_ref);
			
			Process proc = rt.exec(cmd);
			int outVal = proc.waitFor();
			if(outVal!=0){
				System.out.println("Failed! Exit Value: "+outVal);
			}
			
		}catch(InterruptedException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void process(String outfile) {
		drawLogo("-NORMAL","", handler.getLogoMatrix(), outfile);
	}
	
	public static void main(String[] args) {
		LogoFactory factory;
		
		if(args.length == 0 || args[0].equals("-help")){
			System.out.println("Program manual:");
			System.out.println("-[file|dir] -[jarspar|uniprobe|transfac] input output");
		}else if(args[0].equals("-file")){
			String src = args[1];
			String input = args[2];
			String output;
			
			if(args.length==3){
				output = input+".png";
			}else{
				output = args[3];
			}
			
			if(src.equals("-jarspar")){
				factory = new LogoFactory(new JarsparHandler(input));
				factory.process(output);
			}else if(src.equals("-uniprobe")){
				factory = new LogoFactory(new UniprobeHandler(input));
				factory.process(output);
			}else if(src.equals("-transfac")){
				factory = new LogoFactory(new TransfacHandler(input));
				factory.process(output);
			}else{
				System.out.println("Unknown option! "+ src);
			}
		}else if(args[0].equals("-dir")){
			String src = args[1];
			String input_dir = args[2];
			String output_dir = args[3];
			
			if(!input_dir.endsWith("/")){
				input_dir += "/";
			}
			
			if(!output_dir.endsWith("/")){
				output_dir += "/";
			}
			
			String[] files = new File(input_dir).list();
			
			if(src.equals("-jarspar")){
				for(int i=0; i<files.length; i++){
					factory = new LogoFactory(new JarsparHandler(input_dir+files[i]));
					factory.process(output_dir+files[i]+".png");
				}
			}else if(src.equals("-uniprobe")){
				for(int i=0; i<files.length; i++){
					factory = new LogoFactory(new UniprobeHandler(input_dir+files[i]));
					factory.process(output_dir+files[i]+".png");
				}
			}else if(src.equals("-transfac")){
				for(int i=0; i<files.length; i++){
					factory = new LogoFactory(new TransfacHandler(input_dir+files[i]));
					factory.process(output_dir+files[i]+".png");
				}
			}else{
				System.out.println("Unknown option! "+ src);
			}
		}
	}
}
