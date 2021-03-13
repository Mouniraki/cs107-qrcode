package qrcode;

public class Main {

	public static final String INPUT =  "HelloWorld";

	/*
	 * Parameters
	 */
	
		
	public static final int VERSION = 2;
	public static final int MASK = 0;
	public static final int SCALING = 20;

	public static void main(String[] args) {

		/*
		 * Encoding
		 */
		
		
		/*BONUS? : CHOIX DE LA MEILLEURE VERSION DE QR POUR REPRESENTER LA DONNEE*/
		int stringLength = INPUT.length();
		int preferredVersion = 0;
		
			if(stringLength <= 17) {
				preferredVersion = 1;
			}
			
			else if(stringLength <= 32) {
				preferredVersion = 2;
			}
			
			else if(stringLength <= 53) {
				preferredVersion = 3;
			}
			
			else {
				preferredVersion = 4;
			}
			
		boolean[] encodedData = DataEncoding.byteModeEncoding(INPUT, preferredVersion);
		
		/*
		 * image
		 */
		int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(preferredVersion, encodedData,MASK);
		
		/*
		 * Visualization
		 */
		Helpers.show(qrCode, SCALING);
	}

}
