package LogoFactory;

import java.io.*;
import java.util.*;

public class JarsparHandler extends PwmFileHandler {

	public JarsparHandler(String pwmFile) {
		super(pwmFile);
	}

	public double[][] getPFM() {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(super.FileName)));
			
			double[][] pfm = new double[4][];
			for(int i=0; i<4; i++){
				pfm[i] = handleLine(br.readLine());				
			}
			
			return pfm;
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public double[] handleLine(String line){
		Scanner sc = new Scanner(line);
		
		double[] res = new double[50];
		int ind = 0;
		
		while(sc.hasNextDouble()){
			res[ind] = sc.nextDouble();
			ind++;
		}
		
		double[] ret = new double[ind];
		for(int i=0; i<ind; i++){
			ret[i] = res[i];
		}
		
		return ret;
	}

}
