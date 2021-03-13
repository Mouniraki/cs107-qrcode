package qrcode;

/* Auteur : Mounir RAKI
 * Date : 27.10.2019
 * Partenaire : Guillen STEULET
 */

public class MatrixConstruction {
	
	//On initialise les valeurs ARGB pour le blanc et le noir
	public static int W = 0xFF_FF_FF_FF;
	public static int B = 0xFF_00_00_00;
	
	/*
	 * Constants defining the color in ARGB format
	 * 
	 * W = White integer for ARGB
	 * 
	 * B = Black integer for ARGB
	 * 
	 * both needs to have their alpha component to 255
	 */
	// TODO add constant for White pixel
	// TODO add constant for Black pixel
	

	// ...  MYDEBUGCOLOR = ...;
	// feel free to add your own colors for debugging purposes

	/**
	 * Create the matrix of a QR code with the given data.
	 * 
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @param mask
	 *            The mask used on the data. If not valid (e.g: -1), then no mask is
	 *            used.
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data, int mask) {

		/*
		 * PART 2
		 */
		int[][] matrix = constructMatrix(version, mask);
		/*
		 * PART 3
		 */
		addDataInformation(matrix, data, mask);

		return matrix;
	}

	/*
	 * =======================================================================
	 * 
	 * ****************************** PART 2 *********************************
	 * 
	 * =======================================================================
	 */

	/**
	 * Create a matrix (2D array) ready to accept data for a given version and mask
	 * 
	 * @param version
	 *            the version number of QR code (has to be between 1 and 4 included)
	 * @param mask
	 *            the mask id to use to mask the data modules. Has to be between 0
	 *            and 7 included to have a valid matrix. If the mask id is not
	 *            valid, the modules would not be not masked later on, hence the
	 *            QRcode would not be valid
	 * @return the qrcode with the patterns and format information modules
	 *         initialized. The modules where the data should be remain empty.
	 */
	public static int[][] constructMatrix(int version, int mask) {
		//On initialise une matrice vide de la bonne dimension
		int matrix[][]=initializeMatrix(version);
		addFinderPatterns(matrix);
		addAlignmentPatterns(matrix, version);
		addTimingPatterns(matrix);
		addDarkModule(matrix);
		addFormatInformation(matrix, mask);
		// TODO Implementer
		return matrix;

	}

	/**
	 * Create an empty 2d array of integers of the size needed for a QR code of the
	 * given version
	 * 
	 * @param version
	 *            the version number of the qr code (has to be between 1 and 4
	 *            included
	 * @return an empty matrix
	 */
	public static int[][] initializeMatrix(int version) {
		/* On initialise une variable contenant le nombre de carres par ligne et colonne de notre matrice
		 * selon la version du code QR pour creer notre matrice
		 */
		int size = QRCodeInfos.getMatrixSize(version);
		int[][] matrix = new int[size][size];
		// TODO Implementer
		return matrix;
	}

	/**
	 * Add all finder patterns to the given matrix with a border of White modules.
	 * 
	 * @param matrix
	 *            the 2D array to modify: where to add the patterns
	 */
	public static void addFinderPatterns(int[][] matrix) {
		int sizeModule = 7;
		int[][] patterns = finderPatterns(sizeModule);
		
		//On initialise des booleans pour afficher les separateurs
		boolean leftUpperSeparation = false;
		boolean rightUpperSeparation = false;
		boolean leftLowerSeparation = false;
		
		for(int row = 0 ; row < matrix.length ; ++row) {
			for(int col = 0 ; col < matrix[0].length ; ++col) {
				
				leftUpperSeparation = (col < sizeModule+1 && row < sizeModule+1);
				rightUpperSeparation = (col < sizeModule+1 && (row >= matrix[0].length-sizeModule-1 && row < matrix[0].length));
				leftLowerSeparation = ((col >= matrix.length-sizeModule-1 && col < matrix.length) && row < sizeModule+1);
				
				//Separations
				if(leftUpperSeparation || rightUpperSeparation || leftLowerSeparation) {
					matrix[col][row]=W;
				}
				
				//On affiche le pattern en haut a gauche
				if(col < sizeModule && row < sizeModule) {
					matrix[col][row] = patterns[col][row];
				}
				//On affiche le pattern en bas a gauche
				if(col < sizeModule && (row >= matrix[0].length-sizeModule && row < matrix[0].length)) {
					matrix[col][row] = patterns[col][matrix.length-row-1];
				}
				//On affiche le pattern en haut a droite
				if((col >= matrix.length-sizeModule && col < matrix.length) && row < sizeModule) {
					matrix[col][row]= patterns[matrix.length-col-1][row];
				}
			
			}
		}
		
		// TODO Implementer
	}
	
	public static int[][] finderPatterns(int size){
		int[][] pattern = new int[size][size];
		for(int row = 0; row < pattern.length; ++row) {
			for(int col = 0; col < pattern[0].length; ++col) {
				//On remplit d'office les carres en noir
				pattern[col][row]=B;
				//Si on se trouve dans la zone des carres a dessiner en blanc, on les colorie en blanc
				if((col >= 1 && col <= size-2) && (row>=1 && row <= size-2)) {
					pattern[col][row]=W;
				}
				//On colorie egalement le centre de la forme
				if((col >=2 && col <=size-3) && (row >=2 && row <=size-3)) {
					pattern[col][row]=B;
				}
				
			}
		}
		return pattern;
	}

	/**
	 * Add the alignment pattern if needed, does nothing for version 1
	 * 
	 * @param matrix
	 *            The 2D array to modify
	 * @param version
	 *            the version number of the QR code needs to be between 1 and 4
	 *            included
	 */
	public static void addAlignmentPatterns(int[][] matrix, int version) {
		if(version >= 2) {
			//On initialise une variable qui contient la taille du pattern a afficher
			int sizeModule = 5;
			//On initialise le tableau contenant le motif du alignment pattern
			int[][] pattern = finderPatterns(sizeModule);
			boolean rowCondition = false;
			boolean colCondition = false;
			
			for(int row = 0; row < matrix.length; ++row) {
				for(int col = 0; col < matrix[0].length; ++col) {
					
					/* On teste si on est a 4 cases d'ecart avec le bord de droite et le bord du bas de la matrice
					 * Si c'est le cas, on applique l'Alignment pattern a son emplacement
					 */
					rowCondition = (row >= matrix.length-(2*sizeModule-1) && row <= matrix.length-sizeModule);
					colCondition = (col >= matrix[0].length-(2*sizeModule-1) && col <= matrix[0].length-sizeModule);
					
					if(rowCondition && colCondition) {
						matrix[col][row] = pattern[col-(matrix.length-9)][row-(matrix[0].length-9)];
					}
				}
			}
		}
		// TODO Implementer
	}

	/**
	 * Add the timings patterns
	 * 
	 * @param matrix
	 *            The 2D array to modify
	 */
	public static void addTimingPatterns(int[][] matrix) {
		for(int row = 0; row < matrix.length; ++row) {
			for(int col = 0; col < matrix[0].length; ++col) {
				
				/* On teste si on se trouve sur les cases de la 7e ligne entre les colonnes 9 et matrix.length-9 (notation reelle)
				 * Si c'est le cas, on ajoute des carres en alternance de noir et de blanc   
				 */
				
				if(col == 6 && (row >= 8 && row < matrix.length-8)) {
					matrix[col][row]=B;
					
					if(matrix[col][row-1]==B) {
						matrix[col][row]=W;
					}
					
					if(matrix[col][row-1]==W) {
						matrix[col][row]=B;
					}
				}
				/* On teste si on se trouve sur les cases de la 7e colonne entre les lignes 9 et matrix.length-9 (notation reelle)
				 * Si c'est le cas, on ajoute des carres en alternance de noir et de blanc 
				 */
				
				else if(row == 6 && (col >= 8 && col < matrix[0].length-8)) {
					matrix[col][row]=B;
					
					if(matrix[col-1][row]==B) {
						matrix[col][row]=W;
					}
					
					if(matrix[col-1][row]==W) {
						matrix[col][row]=B;
					}
				}
				
				
			}
		}
		// TODO Implementer
	}

	/**
	 * Add the dark module to the matrix
	 * 
	 * @param matrix
	 *            the 2-dimensional array representing the QR code
	 */
	public static void addDarkModule(int[][] matrix) {
		int matrixSize = matrix.length;
		matrix[8][matrixSize - 8]=B;
		// TODO Implementer
	}

	/**
	 * Add the format information to the matrix
	 * 
	 * @param matrix
	 *            the 2-dimensional array representing the QR code to modify
	 * @param mask
	 *            the mask id
	 */
	public static void addFormatInformation(int[][] matrix, int mask) {
		boolean[] formatInfo = QRCodeInfos.getFormatSequence(mask);
		for(int row = 0; row < matrix.length; ++row) {
			for(int col = 0; col < matrix[0].length; ++col) {
				
				//Premiere moitie sur le bas du premier aligment pattern en haut a gauche
				if(row == 8 && col < 6) {
					if(formatInfo[col]==true) {
						matrix[col][row] = B;
					}
					else {
						matrix[col][row] = W;
					}
				}
				//Deuxieme moitie sur le bas du premier alignment pattern en haut a gauche
				else if(row == 8 && col > 6 && col < 9) {
					if(formatInfo[col-1]==true) {
						matrix[col][row]=B;
					}
					else {
						matrix[col][row]=W;
					}
				}
				
				//Premiere moitie sur le cote droit de l'alignment pattern en haut a gauche
				else if (col == 8 && row < 6) {
					if(formatInfo[(formatInfo.length-1)-row]==true) {
						matrix[col][row]=B;
					}
					else {
						matrix[col][row]=W;
					}
				}
				
				//Dernier petit carre sur le cote droit de l'alignment pattern en haut a gauche
				else if(col == 8 && row == 7) {
					if(formatInfo[row+1]==true) {
						matrix[col][row]=B;
					}
					else {
						matrix[col][row]=W;
					}
				}
				
				//Premiere moitie sur la partie de droite de l'alignment pattern en bas a gauche 
				else if(col == 8 && (row >= matrix.length-7 && row < matrix.length)) {
					if(formatInfo[matrix.length-row-1]==true) {
						matrix[col][row]=B;
					}
					else {
						matrix[col][row]=W;
					}
				}
				//Derniere moitie sur la partie du bas de l'alignment pattern en haut a droite
				else if(row == 8 && (col >= matrix[0].length-8)) {
					if(formatInfo[col-(matrix[0].length-8)+7]==true) {
						matrix[col][row]=B;
					}
					else {
						matrix[col][row]=W;
					}
				}
				
			}
		}
		// TODO Implementer
	}

	/*
	 * =======================================================================
	 * ****************************** PART 3 *********************************
	 * =======================================================================
	 */

	/**
	 * Choose the color to use with the given coordinate using the masking 0
	 * 
	 * @param col
	 *            x-coordinate
	 * @param row
	 *            y-coordinate
	 * @param color
	 *            : initial color without masking
	 * @return the color with the masking
	 */
	public static int maskColor(int col, int row, boolean dataBit, int masking) {
		//On initialise une variable color qui prendra l'inverse de la valeur de la couleur du bit a masquer
		int color = 0; 
		//On traite tous les cas pour les types de masques a appliquer sur les valeurs
		switch(masking) {
		case 0:
			if((col + row)%2 == 0) {
				if(dataBit == true) {
					color = W;
				}
				else {
					color = B;
				}
			}
			else {
				if(dataBit == true) {
					color = B;
				}
				else {
					color = W;
				}
			}
			break;
			
		case 1 :
			if(row%2 == 0) {
				if(dataBit == true) {
					color = W;
				}
				else {
					color = B;
				}
			}
			else {
				if(dataBit == true) {
					color = B;
				}
				else {
					color = W;
				}
			}
			break;
			
		case 2:
			if(col%3 == 0) {
				if(dataBit == true) {
					color = W;
				}
				else {
					color = B;
				}
			}
			else {
				if(dataBit == true) {
					color = B;
				}
				else {
					color = W;
				}
			}
			break;
			
		case 3 :
			if((col + row)%3 == 0) {
				if(dataBit == true) {
					color = W;
				}
				else {
					color = B;
				}
			}
			else {
				if(dataBit == true) {
					color = B;
				}
				else {
					color = W;
				}
			}
			break;
			
		case 4:
			if(((int)(row/2) + (int)(col/3))%2 == 0) {
				if(dataBit == true) {
					color = W;
				}
				else {
					color = B;
				}
			}
			else {
				if(dataBit == true) {
					color = B;
				}
				else {
					color = W;
				}
			}
			break;
			
		case 5 :
			if(((col * row)%2) + ((col * row)%3) == 0) {
				if(dataBit == true) {
					color = W;
				}
				else {
					color = B;
				}
			}
			else {
				if(dataBit == true) {
					color = B;
				}
				else {
					color = W;
				}
			}
			break;
			
		case 6:
			if((((col * row) % 2) + ((col * row)%3)) % 2 == 0) {
				if(dataBit == true) {
					color = W;
				}
				else {
					color = B;
				}
			}
			else {
				if(dataBit == true) {
					color = B;
				}
				else {
					color = W;
				}
			}
			break;
			
		case 7 :
			if((((col + row) % 2) + ((col * row) % 3)) % 2 == 0) {
				if(dataBit == true) {
					color = W;
				}
				else {
					color = B;
				}
			}
			else {
				if(dataBit == true) {
					color = B;
				}
				else {
					color = W;
				}
			}
			break;
			
		default : 
			if(dataBit == true) {
				color = B;
			}
			else {
				color = W;
			}
		}
		return color;
	}

	/**
	 * Add the data bits into the QR code matrix
	 * 
	 * @param matrix
	 *            a 2-dimensionnal array where the bits needs to be added
	 * @param data
	 *            the data to add
	 */
	
	
	/* On initialise une methode dont le role est de remplir la matrice de bas en haut
	 * Se charge egalement d'enregistrer et de sortir le dernier index atteint
	 */
	public static int fillUpwards(int colIndex, int[][] matrix, boolean[] data, int mask, int lastUpperIndex, int lastLowerIndex) {
		
		/* On initialise une variable dataIndex dont le role est de permettre l'acces aux bonnes 
		 * positions de notre tableau data en fonction de notre position sur la matrice et du dernier
		 * index atteint lors du remplissage de haut en bas
		 */
		int dataIndex = 0;
		int rowIndex = matrix.length-1; //limite de la matrice
		
		/* On initialise une variable line qui prendra le relai sur la variable iterative row 
		 * lors du remplissage en longeant le alignment pattern pour les versions 2 a 4
		 */
		int line = matrix.length-5;
		
		/* On initialise une variable beginIndex dont le role est de stocker le nombre de carres 
		 * deja remplis avant la mise en place des donnees dans la matrice
		 */
		int beginIndex = 0;
		
		for(int row = matrix.length-1 ; row >= 0; --row) {
			for(int col = colIndex; col > colIndex-2; --col) {
				
			//POUR LA V1
			if(matrix.length == 21) {
			//PARTIE FINDER PATTERN V1 : on teste si on se trouve sous le finder pattern de droite
				if(colIndex < matrix[0].length && colIndex > matrix[0].length-8) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					
					else {
						dataIndex = (colIndex-col + 2*(rowIndex-row))+beginIndex+lastLowerIndex;
						if(dataIndex < data.length) {
							if(data[dataIndex]==true) {
								matrix[col][row]=B;
							}
							else {
								matrix[col][row]=W;
							}
							matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
							lastUpperIndex = dataIndex+1;
						}
						}
				}
				
				//TIMING PATTERN V1 : on teste si on se trouve vers le timing pattern horizontal
				if(colIndex < matrix[0].length-8 && colIndex >= 9) {
					dataIndex = (colIndex-col + 2*(rowIndex-row))-beginIndex+lastLowerIndex;
					
					if(dataIndex < data.length) {
						if(row > 6) {
							
							if(data[dataIndex]==true) {
								matrix[col][row]=B;
							}
							else {
								matrix[col][row]=W;
							}
							matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
							lastUpperIndex = dataIndex+1;
						}
					
					
						else if(row < 6) {
							if(data[dataIndex-2]==true) {
								matrix[col][row]=B;
							}
							else {
								matrix[col][row]=W;
							}
							matrix[col][row]=maskColor(col, row, data[dataIndex-2], mask);
							lastUpperIndex = dataIndex-1;
						}
						
					}
					
				}
				
				//LEFT FINDER PATTERNS V1 : on teste si on se trouve entre les finder patterns de gauche
				if(colIndex < 9) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					
					else {
						dataIndex = (colIndex-col + 2*(rowIndex-row))-beginIndex+lastLowerIndex;
						if(dataIndex < data.length) {
							if(row > 8 && row < matrix.length-8) {
								if(data[dataIndex]==true) {
									matrix[col][row]=B;
								}
								else {
									matrix[col][row]=W;
								}
								matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
								lastUpperIndex = dataIndex+1;
							}
						}
					}
				}
			}
			
			//POUR V2-V4
			else {
				/* FINDER PATTERN V2-V4 : on teste si on se trouve sous le finder pattern de droite
				 * et juste avant le alignment pattern
				 */
				if(colIndex < matrix[0].length && colIndex > matrix[0].length-4) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					
					else {
						dataIndex = (colIndex-col + 2*(rowIndex-row))+beginIndex+lastLowerIndex;
						if(dataIndex < data.length) {
							if(data[dataIndex]==true) {
								matrix[col][row]=B;
							}
							else {
								matrix[col][row]=W;
							}
							matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
							lastUpperIndex = dataIndex+1;
						}
						}
				}
				
				/* FINDER PATTERN + ALIGNMENT PATTERN V2-V4  : on teste si on se trouve sous le finder pattern de droite
				 * et sous le alignment pattern (on ne prend pas encore compte du fait de longer le alignment pattern)
				 */
				if(colIndex <= matrix[0].length-4 && colIndex > matrix.length-8) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					else {
							dataIndex = (colIndex-col + 2*(rowIndex-row))+lastLowerIndex;
							
							if(dataIndex < data.length) {
								
								if(row > matrix.length-5) {
									if(data[dataIndex]==true) {
										matrix[col][row]=B;
									}
									else {
										matrix[col][row]=W;
									}
									matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
									lastUpperIndex = dataIndex+1;
								}
					
								
								else if(row < matrix.length-9) {
									if(data[dataIndex-beginIndex]==true) {
										matrix[col][row]=B;
									}
									else {
										matrix[col][row]=W;
									}
									matrix[col][row]=maskColor(col, row, data[dataIndex-beginIndex], mask);
									lastUpperIndex = dataIndex-beginIndex+1;
								}
								
							}
						}
				}
				
				/* FINDER PATTERN + ALIGNMENT PATTERN + TIMING PATTERN V2-V4  : on teste si on se trouve sur la derniere colonne
				 * sous le finder pattern de droite et on prend en compte le fait de longer le alignment pattern, puis de sauter
				 * une ligne pour eviter le timing pattern
				 */
				if(colIndex == matrix[0].length-9) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					else {
						//MONTE JUSQU'AU ALIGNMENT PATTERN
							if(row > matrix.length-5) {
									dataIndex = (colIndex-col + 2*(rowIndex-row))+lastLowerIndex;
									if(dataIndex < data.length) {
										if(data[dataIndex]==true) {
											matrix[col][row]=B;
										}
										else {
											matrix[col][row]=W;
										}
										matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
										lastUpperIndex = dataIndex+1;
									}
							}
								
								//LONGE LE ALIGNMENT PATTERN
								if(row <= matrix.length-5 && row >= matrix.length-9) {
									dataIndex = lastUpperIndex++; //On n'incremente que de 1 pour longer le pattern
									if(dataIndex < data.length) {
										if(data[dataIndex]==true) {
											matrix[colIndex-1][line--]=B;
										}
										else {
											matrix[colIndex-1][line--]=W;
										}
									matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
									lastUpperIndex = dataIndex+1;
									}
								}
								
								//CONTINUE JUSQU'AU TIMING PATTERN
								if(row < matrix.length-9 && row > 6) {
									
									dataIndex = (colIndex-col + 2*(rowIndex-row))+lastLowerIndex-5;
									if(dataIndex < data.length) {
										if(data[dataIndex]==true) {
											matrix[col][row]=B;
										}
										else {
											matrix[col][row]=W;
										}
										matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
										lastUpperIndex = dataIndex+1;
									}
								}
								
								//SAUTE LE TIMING PATTERN
								if(row < 6) {
								dataIndex = (colIndex-col + 2*(rowIndex-row))+lastLowerIndex-7; //5-2carres
								if(dataIndex < data.length) {
									if(data[dataIndex]==true) {
										matrix[col][row]=B;
									}
									else {
										matrix[col][row]=W;
									}
									matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
									lastUpperIndex = dataIndex+1;
								}
								}
								
							}
						}
				
				
				//PARTIE TIMING PATTERN : on teste si on se trouve vers le timing pattern horizontal
				if(colIndex < matrix[0].length-8 && colIndex >= 9) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					
					else {
						dataIndex = (colIndex-col + 2*(rowIndex-row))+beginIndex+lastLowerIndex;
						
						if(dataIndex < data.length) {
							if(row > 6) {
								if(data[dataIndex]==true) {
									matrix[col][row]=B;
								}
								else {
									matrix[col][row]=W;
								}
								matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
								lastUpperIndex = dataIndex+1;
								
							}
						
						
							else if(row < 6) {
							
								if(data[dataIndex-4]==true) {
									matrix[col][row]=B;
								}
								else {
									matrix[col][row]=W;
								}
								matrix[col][row]=maskColor(col, row, data[dataIndex-4], mask);
								lastUpperIndex = dataIndex-3;
						}
						
					}
						
					}
				}
				
				//ENTRE LES 2 FINDER PATTERN : on teste si on se trouve entre les 2 finder pattern de gauche
				
				if(colIndex < 9) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					
					else {
						dataIndex = (colIndex-col + 2*(rowIndex-row))-beginIndex+lastLowerIndex;
						if(dataIndex < data.length) {
							if(row > 8 && row < matrix.length-8) {
								if(data[dataIndex]==true) {
									matrix[col][row]=B;
								}
								else {
									matrix[col][row]=W;
								}
								matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
								lastUpperIndex = dataIndex+1;
							}
						}
					}
				}
				
			}
			}
		}
		//On retourne le dernier index atteint lors du remplissage
		return lastUpperIndex;
	}
	
	
	/* On initialise une methode dont le role est de remplir la matrice de haut en bas
	 * Se charge egalement d'enregistrer et de sortir le dernier index atteint
	 */
	public static int fillDownwards (int colIndex, int [][] matrix, boolean[] data, int mask, int lastLowerIndex, int lastUpperIndex) {
		
		/* On initialise une variable dataIndex dont le role est de permettre l'acces aux bonnes 
		 * positions de notre tableau data en fonction de notre position sur la matrice et du dernier
		 * index atteint lors du remplissage de haut en bas
		 */
		int dataIndex = 0;
		
		/* On initialise une variable beginIndex dont le role est de stocker le nombre de carres 
		 * deja remplis avant la mise en place des donnees dans la matrice
		 */
		int beginIndex = 0;
		
		for(int row = 0; row < matrix.length; ++row) {
			for(int col = colIndex; col > colIndex-2; --col) {
				
			if(matrix.length==21) {
				
				//PARTIE FINDER PATTERN V1
				if(colIndex < matrix[0].length && colIndex > matrix.length-8) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					else {
						dataIndex = ((colIndex)-col + 2*row)-beginIndex+lastUpperIndex;
						if(dataIndex < data.length) {
							if(data[dataIndex]==true) {
								matrix[col][row]=B;
							}
							else {
								matrix[col][row]=W;
							}
							matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
							lastLowerIndex = dataIndex+1;
						}
					}
					
				}
				
				//TIMING PATTERN V1
				if(colIndex > 9 && colIndex < matrix.length-9) {
					dataIndex = ((colIndex)-col + 2*row)+beginIndex+lastUpperIndex;
					if(dataIndex < data.length) {
						if(row < 6) {
							if(data[dataIndex]==true) {
								matrix[col][row]=B;
							}
							else {
								matrix[col][row]=W;
							}
							matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
							lastLowerIndex = dataIndex+1;
						}
					
					
						else if(row > 6) {
							
							if(data[dataIndex-2]==true) {
								matrix[col][row]=B;
							}
							else {
								matrix[col][row]=W;
							}
							matrix[col][row]=maskColor(col, row, data[dataIndex-2], mask);
							lastLowerIndex = dataIndex-1;
						}
						
					}
				}
				
				//ENTRE LES 2 FINDER PATTERN V1
				if(colIndex < 9) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					
					else {
						if(row > 8 && row < matrix.length-8) {
							
							dataIndex = ((colIndex)-col + 2*row)-beginIndex+lastUpperIndex;
							
							if(dataIndex < data.length) {
								if(data[dataIndex]==true) {
									matrix[col][row]=B;
								}
								else {
									matrix[col][row]=W;
								}
								matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
								lastLowerIndex = dataIndex+1;
							}
						}
					}
				}
			}
			
			//POUR LA V2-V4
			else {
				//FINDER PATTERN
				if(colIndex < matrix[0].length && colIndex > matrix.length-4) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					else {
						dataIndex = ((colIndex)-col + 2*row)-beginIndex+lastUpperIndex;
						if(dataIndex < data.length) {
							if(data[dataIndex]==true) {
								matrix[col][row]=B;
							}
							else {
								matrix[col][row]=W;
							}
							matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
							lastLowerIndex = dataIndex+1;
						}
					}
					
				}
				
				//PARTIE FINDER PATTERN + ALIGNMENT PATTERN V2-V4
				if(colIndex <= matrix[0].length-4 && colIndex > matrix[0].length-8) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					else {							

							dataIndex = ((colIndex)-col + 2*row)-beginIndex+lastUpperIndex;
								
							if(dataIndex < data.length) {
									
								if(row < matrix.length-9) {
									if(data[dataIndex]==true) {
										matrix[col][row]=B;
									}
									else {
										matrix[col][row]=W;
									}
									matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
									lastLowerIndex = dataIndex+1;
								}
						
									
								else if(row > matrix.length-5) {
									if(data[dataIndex]==true) {
										matrix[col][row]=B;
									}
									else {
										matrix[col][row]=W;
									}
									matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
									lastLowerIndex = dataIndex+1;
								}
									
									
							}
					}
					
				}
				
				
				//PARTIE TIMING PATTERN V2-V4
				if(colIndex < matrix[0].length-9 && colIndex > 9) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					else {
						dataIndex = ((colIndex)-col + 2*row)+beginIndex+lastUpperIndex;
						if(dataIndex < data.length) {
							if(row < 6) {
								if(data[dataIndex]==true) {
									matrix[col][row]=B;
								}
								else {
									matrix[col][row]=W;
								}
								matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
								lastLowerIndex = dataIndex+1;
								
							}
						
						
							else if(row > 6) {
								if(data[dataIndex-4]==true) {
									matrix[col][row]=B;
								}
								else {
									matrix[col][row]=W;
								}
								matrix[col][row]=maskColor(col, row, data[dataIndex-4], mask);
								lastLowerIndex = dataIndex-3;
							}
							
							
						}
					}
					
				}
				
				//ENTRE LES 2 FINDER PATTERN V2-V4
				if(colIndex < 9) {
					if(matrix[col][row]!=0) {
						dataIndex = 0;
						++beginIndex;
					}
					
					else {
						if(row > 8 && row < matrix.length-8) {
							
							dataIndex = ((colIndex)-col + 2*row)-beginIndex+lastUpperIndex;
							
							if(dataIndex < data.length) {
								if(data[dataIndex]==true) {
									matrix[col][row]=B;
								}
								else {
									matrix[col][row]=W;
								}
								matrix[col][row]=maskColor(col, row, data[dataIndex], mask);
								lastLowerIndex = dataIndex+1;
							}
						}
					}
				}
				
				}
			}
		}
		return lastLowerIndex;
	}
		
	
	public static void addDataInformation(int[][] matrix, boolean[] data, int mask) {
		//Se charge d'enregistrer le dernier index atteint par la methode fillUpwards
		int lastUpperIndex = 0;
		
		//Se charge d'enregistrer le dernier index atteint par la methode fillDownwards
		int lastLowerIndex = 0;
		
		//On affiche les donnees dans la matrice
		for(int index = matrix.length-1; index > 0; index-=2) {
			if(index > 6) {
				if(index%4 == 0) {
					lastUpperIndex = fillUpwards(index, matrix, data, mask, lastUpperIndex, lastLowerIndex);
				}
				else {
					lastLowerIndex = fillDownwards(index, matrix, data, mask, lastLowerIndex, lastUpperIndex);
				}
			}
			/* On saute d'une colonne si on se trouve a l'index 6 
			 * correspondant a la position du timing pattern vertical
			 */
			else if(index <= 6){
				if(index%4==0) {
					lastUpperIndex = fillUpwards(index-1, matrix, data, mask, lastUpperIndex, lastLowerIndex);
				}
				else {
					lastLowerIndex = fillDownwards(index-1, matrix, data, mask, lastLowerIndex, lastUpperIndex);
					
				}
			}
		}
		//S'il reste des cases vides, on les remplit en blanc, puis on applique le masque dessus
		for(int row = 0; row < matrix.length; ++row) {
			for(int col = 0; col < matrix[0].length; ++col) {
				if(matrix[col][row]==0) {
						matrix[col][row]=W;
						matrix[col][row]=maskColor(col, row, false, mask);
				}
			}
		}
		
		// TODO Implementer
	}
	/*
	 * =======================================================================
	 * 
	 * ****************************** BONUS **********************************
	 * 
	 * =======================================================================
	 */

	/**
	 * Create the matrix of a QR code with the given data.
	 * 
	 * The mask is computed automatically so that it provides the least penalty
	 * 
	 * @param version
	 *            The version of the QR code
	 * @param data
	 *            The data to be written on the QR code
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data) {
		
		int mask = findBestMasking(version, data);
		int matrix[][] = constructMatrix(version, mask);
		addDataInformation(matrix, data, mask);
		return renderQRCodeMatrix(version, data, mask);
	}

	/**
	 * Find the best mask to apply to a QRcode so that the penalty score is
	 * minimized. Compute the penalty score with evaluate
	 * 
	 * @param data
	 * @return the mask number that minimize the penalty
	 */
	public static int findBestMasking(int version, boolean[] data) {
		// TODO BONUS
		return 0;
	}

	/**
	 * Compute the penalty score of a matrix
	 * 
	 * @param matrix:
	 *            the QR code in matrix form
	 * @return the penalty score obtained by the QR code, lower the better
	 */
	public static int evaluate(int[][] matrix) {
		//TODO BONUS
	
		return 0;
	}

}
