package LogoFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UniprobeHandler extends PwmFileHandler {

	public UniprobeHandler(String pwmFile) {
		super(pwmFile);
	}
	
	@Override
	public double[][] getPFM() {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(super.FileName)));
			br.readLine();
			
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
		Scanner sc = new Scanner(line).useDelimiter("\\s+");
		
		double[] res = new double[50];
		int ind = 0;
		
		sc.next();
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
