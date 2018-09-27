package LogoFactory;

import java.text.NumberFormat;
import java.util.Locale;

public abstract class PwmFileHandler {
	public String FileName;
	
	private double[] BackDis = {0.25, 0.25, 0.25, 0.25};
	private double[][] pfm;
	private double[][] motif;
	
	public PwmFileHandler(String _FileName) {
		this.FileName = _FileName;
	}
	
	private String mat2str(double[][] mat) {
		int rows = 4;
		int cols = mat[0].length;
		
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.CHINA);
		nf.setMaximumFractionDigits(3);
		
		String str = "";
		for(int i=0; i<rows; i++){
			for(int j=0; j<cols; j++){
				if(j==cols-1){
					str += nf.format(mat[i][j]) + "\n";
				}else{
					str += nf.format(mat[i][j]) + "\t";
				}
			}
		}
		
		return str;
	}
	
	public String getLogoMatrix(){
		pfm = getPFM();
		
		// ensure the position frequency matrix is normalized
		pfm = normalize(pfm);
		
		
		// calculate information content based matrix
		int WIDTH = pfm[0].length;
		motif = new double[4][WIDTH];
		for(int i=0; i<4; i++){
			for(int j=0; j<WIDTH; j++){
				motif[i][j] = Math.log(pfm[i][j]/BackDis[i])/Math.log(2) * pfm[i][j] ; 
			}
		}
		
		for(int j=0; j<WIDTH; j++){
			double sum = 0;
			for(int i=0; i<4; i++){
				sum += motif[i][j];
			}
			for(int k=0; k<4; k++){
				motif[k][j] = pfm[k][j] * sum;
			}
		}
		
		return mat2str(motif);
	}
	
	private double[][] normalize(double[][] mat) {
		int WIDTH = mat[0].length;
		
		double[] sum = new double[WIDTH];
		for(int i=0; i<WIDTH; i++){
			for(int j=0; j<4; j++){
				sum[i] += mat[j][i];
			}
		}
		
		for(int i=0; i<WIDTH; i++){
			for(int j=0; j<4; j++){
				mat[j][i] = mat[j][i]/sum[i] + Double.MIN_VALUE;
			}
		}
		
		return mat;
	}
	
	// the elements are supposed to be separated by tab
	public abstract double[][] getPFM();
	
}
