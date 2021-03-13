package qrcode;

/* Auteur : Mounir RAKI
 * Date : 27.10.2019
 * Partenaire : Guillen STEULET
 */

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import reedsolomon.ErrorCorrectionEncoding;

public final class DataEncoding {

	/**
	 * @param input
	 * @param version
	 * @return
	 */
	
	public static boolean[] byteModeEncoding(String input, int version) {
		int maxCharactersToEncode = QRCodeInfos.getMaxInputLength(version);
		int charLengthWithoutECC = QRCodeInfos.getCodeWordsLength(version);
		int eccCharLength = QRCodeInfos.getECCLength(version);
		
		int[] encodedString = encodeString(input, maxCharactersToEncode);
		int[] addedInformation = addInformations(encodedString);
		int[] filledSequence = fillSequence(addedInformation, charLengthWithoutECC);
		int[] sequenceWithECC = addErrorCorrection(filledSequence, eccCharLength);
		boolean[] binaryArray = bytesToBinaryArray(sequenceWithECC);
		// TODO Implementer
		return binaryArray;
	}

	/**
	 * @param input
	 *            The string to convert to ISO-8859-1
	 * @param maxLength
	 *          The maximal number of bytes to encode (will depend on the version of the QR code) 
	 * @return A array that represents the input in ISO-8859-1. The output is
	 *         truncated to fit the version capacity
	 */
	public static int[] encodeString(String input, int maxLength) {
		//On convertit notre chaine de caracteres en une sequence de bytes
		byte[] tabByte = input.getBytes(StandardCharsets.ISO_8859_1);
		//Initialisation du tableau de valeurs en int finales apres encodage
		int[] inputBytes;
		
		if(maxLength > input.length()) {
			inputBytes = new int[input.length()];
		}
		else {
			inputBytes = new int [maxLength];
		}
		 
		for(int i=0; i<inputBytes.length; ++i){
					inputBytes[i] = tabByte[i] & 0xFF;
		}
		// TODO Implementer
		//On retourne le tableau fraichement rempli de la sequence completee de bytes
		return inputBytes;
	}

	/**
	 * Add the 12 bits information data and concatenate the bytes to it
	 * 
	 * @param inputBytes
	 *            the data byte sequence
	 * @return The input bytes with an header giving the type and size of the data
	 */
	public static int[] addInformations(int[] inputBytes) {
		//Initialisation de l'indicateur du mode d'encodage (ici : byte mode)
		int byteModeIndicator = 0b0100;
		int messageLength = inputBytes.length;
		int[] encodedData = new int[messageLength+2];
		for(int i=0;i<encodedData.length;++i){
			
			if(i == 0){
				/* La premiere entree du tableau comporte le prefixe d'encodage ainsi que
				* les 4 premiers bits de la longueur du tableau inputBytes
				* */
				encodedData[i] = (byteModeIndicator << 4) | (messageLength >> 4);
			}
			else if(i == 1){
				/*La seconde entree comporte les 4 bits de poids faible de la partie codant pour la longueur
				* ainsi que les 4 premiers bits de la premiere valeur du tableau inputBytes
				* */
				encodedData[i]=((messageLength & 0xF) << 4);
				if(inputBytes.length !=0) {
					encodedData[i]=encodedData[i] | (inputBytes[i-1] >> 4);
				}
			}
			
			else if(i == encodedData.length-1){
				/*La derniere entree comporte les 4 bits de poids faible du dernier element de inputBytes
				 * et les 4 derniers bits de terminaison
				 * */
				encodedData[i] = ((inputBytes[messageLength-1]) & 0xF) << 4;
			}
			else{
				/*Sinon, chaque entree contient les 4 derniers bits de l'element a la place i-2 ainsi que les 4 premiers bits
				 * de l'element a la place i-1
				 * */
				encodedData[i] = ((inputBytes[i-2] & 0xF) << 4 | (inputBytes[i-1] >> 4));
			}
		}
		// TODO Implementer
		return encodedData;
	}

	/**
	 * Add padding bytes to the data until the size of the given array matches the
	 * finalLength
	 * 
	 * @param encodedData
	 *            the initial sequence of bytes
	 * @param finalLength
	 *            the minimum length of the returned array
	 * @return an array of length max(finalLength,encodedData.length) padded with
	 *         bytes 236,17
	 */
	public static int[] fillSequence(int[] encodedData, int finalLength) {
		/*Si notre tableau encodedData a une dimension plus petite que la longueur donnee en parametre
		* On cree un nouveau tableau de taille finalLength dans lequel vont etre inserees les valeurs de
		* encodedData, suivies des valeurs 236 et 17 alternativement jusqu'au remplissage total du nouveau tableau
		*/
		if(finalLength > encodedData.length){
			
			int[] filledSequence = new int[finalLength];
			int firstFiller = 236;
			int secondFiller = 17;

			for(int i=0;i<finalLength;++i){
				if(i < encodedData.length){ 
					filledSequence[i]=encodedData[i];
				}
				/* Si on arrive a la fin du tableau de donnees encodees, on commence par remplir notre tableau
				 * successivement avec les valeurs 236 et 17 (dans cet ordre)
				 */
				else {
					if(i == encodedData.length) {
						filledSequence[i]=firstFiller;
					}
					else if(filledSequence[i-1] == firstFiller) {
						filledSequence[i] = secondFiller;
					}
					else if(filledSequence[i-1] == secondFiller){
						filledSequence[i] = firstFiller;
					}
				}
			}
			return filledSequence;
		}
		// TODO Implementer
		/* Si le tableau encodedData a bien une dimension plus grande ou egale finalLength,
		 * on retourne le tableau encodedData sans modifications
		 */
		else {
			return encodedData;
		}
	}

	/**
	 * Add the error correction to the encodedData
	 * 
	 * @param encodedData
	 *            The byte array representing the data encoded
	 * @param eccLength
	 *            the version of the QR code
	 * @return the original data concatenated with the error correction
	 */
	public static int[] addErrorCorrection(int[] encodedData, int eccLength) {
		/* On initialise un tableau contenant le resultat de la production des bits de correction d'erreur
		 * que l'on va ensuite rajouter a la suite des octets enregistres dans le tableau encodedData
		 */
		int[] eccCode = reedsolomon.ErrorCorrectionEncoding.encode(encodedData,eccLength);
		int[] completedSequence = new int[encodedData.length + eccLength];

		for(int i = 0; i < completedSequence.length; ++i){
			if(i < encodedData.length){
				completedSequence[i] = encodedData[i];
			}
			else{
				completedSequence[i] = eccCode[i-encodedData.length];
			}
		}
		// TODO Implementer
		return completedSequence;
	}

	/**
	 * Encode the byte array into a binary array represented with boolean using the
	 * most significant bit first.
	 * 
	 * @param data
	 *            an array of bytes
	 * @return a boolean array representing the data in binary
	 */
	public static boolean[] bytesToBinaryArray(int[] data) {
		
		/* On initialise un tableau de boolean qui contiendra pour chaque entree une valeur correspondant a 1 bit
         * de chaque entree du tableau data passe en parametres (ex : 1001_0110 sera enregistre dans le tableau
         * boolean comme 1 0 0 1 0 1 1 0, ou chaque 1 correspond a la valeur "true" et chaque 0 a la valeur "false")
         */
        boolean[] binaryArray = new boolean[data.length * 8];
        
        /* On initialise une variable bitPosition dont le role sera de retirer une partie de notre nombre de depart
		pour pouvoir trouver plus facilement les valeurs binaires en 0 et en 1 de chaque entree de notre tableau data
		(ex : si le bit trouve a la position 3 est egal a 1, on soustrait 2^3 au nombre de depart pour trouver
		le bit a la position 2 de maniere plus aisee, car cela a la consequence de "remplacer" le 1 defini a la position
		3 par un 0.).
		 */
        int bitPosition = 0;
        
        /* On initialise une variable qui contiendra successivement le reste de la soustraction de notre chiffre 
           par une puissance de 2
        */
        int remain = 0;
        
        int index = 0;
        int bit = 0;
        
        //Pour parcourir tous les elements du tableau dans lequel on stockera les chiffres binaires
        for(int dI = 0; dI < data.length; ++dI){
        	
            //On initialise le reste a la valeur contenue a l'index i du tableau "valeur"
            remain = data[dI];
            
            //Pour parcourir notre tableau de valeurs int
            for(int bI = 7; bI >= 0; --bI){
            	
                /* On initialise un index qui permettra de parcourir les cases de notre tableau binaryArray
                 * dans l'ordre croissant en fonction de la valeur que prend l'iterateur dI
                 * (pour eviter de faire une boucle sur qu'une seule portion de notre tableau binaryArray)
                 */
                index = 8*dI+7;
                
                //On enleve a chaque fois une puissance de 2 car les valeurs sont comprises entre 0 et 255
                bitPosition = (int) Math.pow(2, bI+1);

                /* Pour trouver la valeur du bit a la premiere position en partant de la gauche, on ne soustrait rien
				 * a notre chiffre de depart et on se contente de decaler tous nos bits de 7 vers la droite
                 */
                if(bI == 7) {
                	bit = remain >> bI;
                }
                
                /* Sinon, on enleve des puissances de 2 a notre chiffre puis on decale sa valeur binaire successivement
                 * de bI cases, puis on recupere la valeur du bit isole dans la variable bit 
                 */
                
                else{
                	
                	if(bit == 0) {
                		bitPosition = 0;
                	}
                	
                    remain -= bitPosition;
                    bit = remain >> bI;
                }
                
                //On remplit le tableau binaryArray
                if(bit == 0) {
                	binaryArray[index-bI] = false;
                }
                
                else {
                	binaryArray[index-bI] = true;
                }

            }
        }
		// TODO Implementer
		return binaryArray;
	}

}
