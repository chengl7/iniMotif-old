package LogoFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

public class TransfacHandler extends PwmFileHandler {

	public TransfacHandler(String pwmFile) {
		super(pwmFile);
	}

	@Override
	public double[][] getPFM() {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(super.FileName)));
			
			Pattern pat = Pattern.compile("^[0-9]{2}\\s+.+");
			double[][] matrix = new double[100][4];
			int row_ind = 0;
			
			
			String str = null;
			while((str=br.readLine())!=null){
				Matcher mat = pat.matcher(str);
				if(mat.matches()){
					matrix[row_ind] = handleLine(str);
					row_ind ++;
				}
			}
			
			double[][] pfm = new double[4][row_ind];
			for(int i=0; i<4; i++){
				for(int j=0; j<pfm[0].length; j++){
					pfm[i][j] = matrix[j][i];
				}
			}
			return pfm;
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}
	
	public double[] handleLine(String line){
		Scanner sc = new Scanner(line).useDelimiter("\\s+");
		
		double[] res = new double[50];
		int ind = 0;
		
		sc.nextDouble();
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
