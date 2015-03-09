package org.eurocarbdb.MolecularFramework.io.WURCS;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;

/**
 * Class for WURCS RES conversion from monosaccharide (and Substituent)
 * @author MasaakiMatsubara
 *
 */
public class RES {

	private String m_strSkeltonCode = null;
	private Monosaccharide m_objMonosaccharide;
	private ArrayList<Substituent> m_aSubstituents = new ArrayList<Substituent>();
	private HashMap<Substituent, UnderdeterminedSubTree> m_hashProbabilities = new HashMap<Substituent, UnderdeterminedSubTree>();
	private boolean m_bRootOfSubgraph = false;
	private int m_nStartOfRepeat = 0;
	private int m_nEndOfRepeat = 0;
	private boolean isInverted = false;

	public RES(Monosaccharide a_objMonosaccharide) {
		this.m_objMonosaccharide = a_objMonosaccharide;
	}

	public void addSubstituent(Substituent a_objSubstituent) {
		this.addSubstituent(a_objSubstituent, null);
	}

	public void addSubstituent(Substituent a_objSubstituent, UnderdeterminedSubTree a_strUndetSubtree) {
		if ( this.m_aSubstituents.contains(a_objSubstituent) ) return;
		this.m_aSubstituents.add(a_objSubstituent);
		this.m_hashProbabilities.put(a_objSubstituent, a_strUndetSubtree);
	}

	public Monosaccharide getMonosaccharide() {
		return this.m_objMonosaccharide;
	}

	public ArrayList<Substituent> getSubstituents() {
		return this.m_aSubstituents;
	}

	public void setRootOfSubgraph() {
		this.m_bRootOfSubgraph = true;
	}

	public void countStartOfRepeat() {
		this.m_nStartOfRepeat++;
	}

	public void countEndOfRepeat() {
		this.m_nEndOfRepeat++;
	}

	public int getStartOfRepeatCount() {
		return this.m_nStartOfRepeat;
	}

	public int getEndOfRepeatCount() {
		return this.m_nEndOfRepeat;
	}

	private boolean hasParent() {
		if ( this.m_bRootOfSubgraph ) return true;

		if ( this.m_objMonosaccharide.getParentNode() != null ) return true;

		for ( GlycoEdge t_objChildEdge : this.m_objMonosaccharide.getChildEdges() ) {
			for ( Linkage t_objLink : t_objChildEdge.getGlycosidicLinkages() ) {
				for ( Integer t_iLinkPos : t_objLink.getParentLinkages() ) {
					int t_iRingStart = this.m_objMonosaccharide.getRingStart();
					if ( t_iRingStart != -1 && t_iLinkPos == t_iRingStart ) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isInverted() {
		return this.isInverted;
	}

	public String getRESCode() throws WURCSException {
		if ( this.m_strSkeltonCode != null )
			return this.m_strSkeltonCode;

		/// Make BMU
//		System.out.println(this.m_objMonosaccharide.getGlycoCTName());
		String skeleton = "";

		boolean isUnknownRingSize = (this.m_objMonosaccharide.getRingStart() == Monosaccharide.UNKNOWN_RING);

		// Check anomeric position
		LinkedList<Integer> anomPositions = new LinkedList<Integer>(); // If unknown empty

		// Get number of carbons and build base skeletoncode
		boolean isAldose = true;
		int numC = this.m_objMonosaccharide.getSuperclass().getCAtomCount();
		if ( numC == 0 ) { // Number of carbons is unknown (For basetype "SUG")
			skeleton = "<0>";
			isAldose = false;
		} else { // Set all charactor to "*" and tail charactor to "h" (e.g. "*****h" for HEX)
			skeleton = "h";
			for ( int i=1; i < numC-1; i++ ) {
				skeleton += "*";
			}
			skeleton += "h";
		}
//		System.out.println(skeleton);

		// Get stereocode
		String stereo = "";
		LinkedList<String> dlList = new LinkedList<String>();
		for( BaseType bs : this.m_objMonosaccharide.getBaseType() ) {
			String code = bs.getStereoCode();
			// For relative configuration
			if ( bs.absoluteConfigurationUnknown() ) {
				// "1" and "2" are converted to "3" and "4"
				code = BaseTypeForRelativeConfiguration.forName(bs.getName()).getStereoCode();
			}
			if ( code.endsWith("1") ) dlList.add("1"); // "L" configuration
			if ( code.endsWith("2") ) dlList.add("2"); // "D" configuration
			stereo = code + stereo;
		}
//		stereo = stereo.replace("*", "X");
//		System.out.println(stereo);
		String dl = "x";
		if ( dlList.size() > 0 ) dl = dlList.getLast();

		// Convert anomeric information
		char anom = this.m_objMonosaccharide.getAnomer().getSymbol().charAt(0);
		if ( this.m_objMonosaccharide.getRingStart() == Monosaccharide.OPEN_CHAIN ) anom = 'o';
		String anomCode = "x";
		if (! dl.equals("x") ) { // For absolute configuration
			if (anom == 'a') anomCode = (dl.equals("1"))? "1" : "2";
			if (anom == 'b') anomCode = (dl.equals("1"))? "2" : "1";
			if (anom == 'o') anomCode = "o";
		} else {                 // For relative configuration
			if (anom == 'a') anomCode = "4";
			if (anom == 'b') anomCode = "3";
			if (anom == 'o') anomCode = "o";
		}
//		System.out.println(anomSkelton);

		// Modify base skeletoncode by core modifications
		// if terminal carbon is modified, replace terminal skeletoncode
		// if non-terminal carbon is modified, insert modification code into skeletoncode
		int nonTermModCount = 0;
		ArrayList<Modification> enMods = new ArrayList<Modification>();
		StringBuilder sb = new StringBuilder(skeleton);
		for(Modification modif : this.m_objMonosaccharide.getModification() ) {
			String modtype = modif.getName();

			if(modif.hasPositionTwo()) { // Modification has two likage position
				int pos1 = modif.getPositionOne();
				int pos2 = modif.getPositionTwo();
//				System.out.println( modif.getPositionOne() +","+ modif.getPositionTwo() +":"+ modif.getName());
				if ( modtype.equals("en") || modtype.equals("enx") ) { // double bond carbons
					// After processing modification with single position,
					// to process double bond carbons for skeletoncharacter code
					enMods.add(modif);
					if ( pos1 != 1 && pos1 != numC ) nonTermModCount++;
					if ( pos2 != 1 && pos2 != numC ) nonTermModCount++;
				}
				continue;
			}

			// Modification with single position
//			System.out.println( modif.getPositionOne() +":"+  modif.getName());
			int pos = modif.getPositionOne();
			boolean atTerminal = (pos == 1 || pos == numC);

			// For alditol
			if ( modtype.equals("aldi") ){
				if ( pos != 1 )
					throw new WURCSException("Modification \"aldi\" is must set to first carbon. :" + this.m_objMonosaccharide.getGlycoCTName());
				isAldose = false;
				continue;
			}

			// For carbonyl acid
			if ( modtype.equals("a") ) {
				// At non-terminal
				if ( !atTerminal ) {
//					sb.replace(pos-1, pos, "a");
//					modCount++;
					throw new WURCSException("Can not do carboxylation to non-terminal carbon. :" + this.m_objMonosaccharide.getGlycoCTName());
				}
				// At terminal
				if ( pos == 1 ) isAldose = false;
				sb.replace(pos-1, pos, "a");
				continue;
			}

			// Count non-terminal modification
			if ( !atTerminal ) nonTermModCount++;

			// For deoxy
			if ( modtype.equals("d") ) {
				if ( pos == 1 ) isAldose = false;
				// At non-terminal
				sb.replace(pos-1, pos, "d");
				// At terminal
				if ( atTerminal ) sb.replace(pos-1, pos, "m");
				continue;
			}

			// For ketose modification
			if ( modtype.equals("keto") ) {
				isAldose = false;
				anomPositions.add(pos);

				// Ver 1.1
				// At non-terminal
//				sb.replace(pos-1, pos, "k");
				// At terminal
//				if ( atTerminal ) sb.replace(pos-1, pos, "o");
				// Ver 2.0
				sb.replace(pos-1, pos, "o");

				// For anomeric position
//				if ( pos == this.m_objMonosaccharide.getRingStart())
//					sb.replace(pos-1, pos, anomCode);
				continue;
			}

		}

		// If aldose, set anomeric position to head of skeletoncode
		if ( isAldose ) {
			sb.replace(0, 1, "o");
//			if ( !isUnknownRingSize ) sb.replace(0, 1, anomCode);
			anomPositions.add(1);
		}

		// If no anomeric position, it must be open chain structure
		if ( anomPositions.size() == 0 ) {
			isUnknownRingSize = false;
			if ( numC != 0 ) anom = 'o';
			else if( anom == 'x' ) anom = 'o';
		} else {
			// For ketose
			if ( anomPositions.get(0) > 1 )
			anomCode = ( anomCode.equals("1") )? "5" :
					   ( anomCode.equals("2") )? "6" :
					   ( anomCode.equals("3") )? "7" :
					   ( anomCode.equals("4") )? "8" :
					   ( anomCode.equals("x") )? "X" : anomCode;

			// For unknown ring size at reducing end
			if ( isUnknownRingSize && !this.hasParent() )
				anomCode = ( anomCode == "x" )? "u" :
						   ( anomCode == "X" )? "U" : anomCode;

			// Replace anomeric position
			for ( Integer pos : anomPositions )
				sb.replace(pos-1, pos, anomCode);
		}

/*
		if ( isUnknownRingSize ) {
			// Set unknown anomeric symbol
			for ( Integer pos : anomPositions ) {
//				if ( this.hasParent() ) System.out.println(pos);
				if ( anomCode == "x" && !this.hasParent() )
					anomCode = (pos==1)? "u" : "U";
				sb.replace(pos-1, pos, anomCode);
			}
		}
*/
/*
		else if ( anom != 'o' && !anomPositions.contains( this.m_objMonosaccharide.getRingStart() ) ) {
			// Error for ring start position
			throw new WURCSException("Ring start is not anomeric position.");
		}
*/

		// For "en" or "enx" modifications
		for (Modification enmod : enMods) {

			if ( !this.replaceCarbonDescriptorByEnMod(enmod, sb) )
				throw new WURCSException("There is an error in the modification \"en\" or \"enx\". :" + this.m_objMonosaccharide.getGlycoCTName());

		}

//		System.out.println(sb.toString());

		// If total stereo and modification count is less than length of carbon chain
		if ( stereo != "" ) {
			if (stereo.length() + nonTermModCount + 2 < numC)
				throw new WURCSException("The stereo information is lacking. :" + this.m_objMonosaccharide.getGlycoCTName());
//			System.out.println("There is an lack of stereo information :" + this.m_objMonosaccharide.getGlycoCTName());
			int j=0;
			for ( int i=1; i < sb.length()-1 ; i++ ) {
				if ( sb.charAt(i) == '*' ) {
					sb.replace( i, i+1, String.valueOf( stereo.charAt(j) ) );
					j++;
				}
				if ( j == stereo.length() ) break;
			}
		}

		// Replace undefined configuration to 'x' or 'u'
		int j=0;
		for ( int i=0; i < numC-1 ; i++ ) {
			if ( sb.charAt(i) == '*' ) j++;
		}
		for ( int i=0; i < numC-1 ; i++ ) {
			char cReplace = ( isUnknownRingSize && anomPositions.contains(i+1) )? 'u' : 'x';
			if ( sb.charAt(i) == '*' ) sb.replace(i, i+1, String.valueOf(cReplace) );
		}

		// Check skeleton code symmetry
		String dlFirst = ( dlList.size() > 0 )? dlList.getFirst() : "x" ;
		if ( numC > 0 && sb.charAt(0) == sb.charAt(numC-1) && dlFirst.equals("1") && anom != 'o' ) {
			// TODO: symmetric skeleton code
//			this.checkSkeletonCodeSymmetry(sb);
		}
		boolean isSymmetry = true;
		for ( int i=0; i < numC/2; i++ ) {
//			if ( i == numC-1-i && sb.charAt(i) != '1' && sb.charAt(i) != '2' ) continue;
			if ( sb.charAt(i) == sb.charAt(numC-1-i) ) continue;
			isSymmetry = false;
		}
		// Invert skeleton code if symmetry and "L" configuration
		if ( isSymmetry && dlFirst.equals("1")) {
			System.out.println("Inverted symmetric skeleton code :" + this.m_objMonosaccharide.getGlycoCTName());
			StringBuilder sbrev = new StringBuilder();
			for ( int i=0; i < numC; i++) {
				char rev = sb.charAt(i);
				rev = (rev == '1')? '2' : (rev == '2')? '1' : rev;
				sbrev.append( String.valueOf(rev) );
			}
			sb = sbrev;
			// Set flag for not direct connected linkages (e.g. in repeating unit)
			this.isInverted = true;

			// Invert linkage position
			try {
				this.invertLinkagePosition();
			} catch (GlycoconjugateException e) {
				throw new WURCSException( "For inverted skeleton code : " + e.getMessage() );
			}
		}

		// Add MAP code
		ArrayList<String> aMAPs = new ArrayList<String>();	// MAP strings
		for ( Substituent sub : this.m_aSubstituents ) {
			String t_strMAP = this.getMAPCode(sub, sb);
			if ( t_strMAP == null ) continue;

			String t_strExtraMAP = this.modifySkeletonCodeBySubstituent(sub, t_strMAP, sb);
			t_strMAP = this.insertProbability(sub, t_strMAP);

			if ( t_strExtraMAP.equals("") ) {
				aMAPs.add( t_strMAP );
				continue;
			}

			if ( t_strMAP.length() < t_strExtraMAP.length() ) {
				aMAPs.add( t_strMAP );
				aMAPs.add( t_strExtraMAP );
			} else {
				aMAPs.add( t_strExtraMAP );
				aMAPs.add( t_strMAP );
			}
		}
		Collections.sort(aMAPs);

		// Add anomeric information to SkeletonCode
		String strAnomPos = "";
		if ( anom != 'o' && !( sb.toString().contains("u") || sb.toString().contains("U") )) {
//			String pos = "+";
			if ( this.m_objMonosaccharide.getRingStart() > 0) {
				strAnomPos += ""+this.m_objMonosaccharide.getRingStart();
			} else if ( numC != 0 ) {
				strAnomPos += anomPositions.get(0);
			} else {
				strAnomPos += "?";
			}
			sb.append("-"+strAnomPos+anom);
		}

		// Add ring information
		String strRingPos = "";
		if (this.m_objMonosaccharide.getRingStart() > 0) {
//			strRingPos += "|" + this.m_objMonosaccharide.getRingStart() + "," + this.m_objMonosaccharide.getRingEnd();
			strRingPos += "_" + this.m_objMonosaccharide.getRingStart() + "-" + this.m_objMonosaccharide.getRingEnd();
		} else if ( isUnknownRingSize && !( sb.toString().contains("u") || sb.toString().contains("U") ) ) {
//			strRingPos += "|?,?";
			strRingPos += "_"+strAnomPos+"-?";
		}
		sb.append(strRingPos);
//		System.out.println(sb.toString());

//		System.out.println(MAPs);
		for ( String MAP : aMAPs) {
			sb.append( "_" + MAP );
		}
		this.m_strSkeltonCode = sb.toString();

		return this.m_strSkeltonCode;
	}

	/**
	 * Replace CarbonDescriptor by "en" modification
	 * @param enmod Modification of "en" or "enx"
	 * @param sb StringBuilder with SkeletonCode
	 * @return True if no error in the replacement
	 */
	private boolean replaceCarbonDescriptorByEnMod(Modification enmod, StringBuilder sb) {
		int numC = sb.length();

		// pos1 < pos2
		int pos1 = enmod.getPositionOne();
		int pos2 = enmod.getPositionTwo();

		char strPos1 = sb.charAt(pos1-1);
		char strPos2 = sb.charAt(pos2-1);
		String strReplace1 = "";
		String strReplace2 = "";
		// For chain form or cyclic form with unknown ring size
		if ( m_objMonosaccharide.getRingStart() < 1 ) {
			/******* Double bond contain terminal carbon **********************************
			 *                  # stereo of double bond between C1 and C2 is unknown
			 *                  # X and Y can be exchange
			 *     ?     Y      X == H      && Y == H      -> C1 == 'n'
			 *     |     |      X == O      && Y == O      -> C1 == 'N'
			 *     ??    C1--X  X == !(H,O) && Y == !(H,O) -> C1 == 'N'
			 *    /  \  X       X == !H     && Y == H      -> C1 == 'F' (e/z unknown)
			 *   ?    C2
			 *        |         Z == H -> C2 == 'f' , Z == !H -> C2 == 'F' (e/z unknown)
			 *        Z         Z == H -> C2 == 'n' , Z == !H -> C2 == 'N' (no chirality)
			 ******************************************************************************/
			if ( pos1 == 1 || pos2 == numC) { // contain terminal carbon
				if ( pos2 == numC ) { // swap terminal carbons
					int tmp = pos1;
					pos1 = pos2;
					pos2 = tmp;
					strPos1 = sb.charAt(pos1-1);
					strPos2 = sb.charAt(pos2-1);
				}

				if ( strPos1 != 'm' && strPos1 != 'h' )
					return false;
//					throw new WURCSException("There is an error in the modification \"en\" or \"enx\". :" + this.m_objMonosaccharide.getGlycoCTName());

				// Terminal carbon is "methyl"
				if ( strPos1 == 'm' ) {
					strReplace1 = "n";
					strReplace2 = "n";
				}
				// Terminal carbon is "hydroxyl"
				if ( strPos1 == 'h' ) {
					strReplace1 = "F";
					strReplace2 = "f";
				}
				// Non-terminal carbon is not "deoxy"
				if ( strPos2 != 'd' )
					strReplace2 = strReplace2.toUpperCase();
				sb.replace(pos1-1, pos1, strReplace1);
				sb.replace(pos2-1, pos2, strReplace2);
				return true;
			}

			/******* Double bond not contain terminal carbon ******************************
			 *                 # stereo of double bond between C1 and C2 is unknown
			 *
			 *      Y     ?    X ==  H  ->  C1 == 'f'
			 *      |     |    X == !H  ->  C1 == 'F'
			 *      C2    ??
			 *  \  /  X  /  \  Y ==  H  ->  C2 == 'f'
			 *   ??    C1      Y == !H  ->  C2 == 'F'
			 *   |     |
			 *   ?     X
			 ******************************************************************************/
			strReplace1 = (strPos1 == 'd')? "f" : "F";
			strReplace2 = (strPos2 == 'd')? "f" : "F";
			sb.replace(pos1-1, pos1, strReplace1);
			sb.replace(pos2-1, pos2, strReplace2);
			return true;
		}

		// For cyclic form
		/**
		 * Can not form double bond on keton group
		 */
		if (   sb.charAt(pos1-1) == 'o' || sb.charAt(pos2-1) == 'o'       // Contain carbonyl group
			|| (pos1 == m_objMonosaccharide.getRingStart() && pos1 != 1 ) // and not terminal anomeric carbon
			||  pos2 == m_objMonosaccharide.getRingStart() ) {
			return false;

		}

		/******* Double bond contain terminal anomeric carbon in cyclic form ******************
		 *                      # C1 is anomeric carbon
		 *
		 *  ?--C6               X ==  H && Y ==  H -> C1 == 'e' && C2 == 'z'
		 *       \              X ==  H && Y == !H -> C1 == 'e' && C2 == 'Z'
		 *        C5--O5
		 *       /      \       X == !H && Y ==  H -> C1 == 'N' && C2 == 'n'
		 *  ?--C4        C1--X  X == !H && Y == !H -> C1 == 'N' && C2 == 'N'
		 *       \     //
		 *        C3--C2
		 *       /      \
		 *      ?        Y
		 ******************************************************************************/
		if ( pos1 == m_objMonosaccharide.getRingStart() && pos1 == 1) {
			if ( strPos1 == 'd' ) {
				strReplace1 = "z";
				strReplace2 = (strPos2 == 'd')? "z" : "Z";
			} else {
				strReplace1 = "N";
				strReplace2 = "N";
			}
			sb.replace(pos1-1, pos1, strReplace1);
			sb.replace(pos2-1, pos2, strReplace2);
			return true;
		}

		/******* Double bond contain ring end carbon in cyclic form **************************
		 *                      # C5 is ring end carbon
		 *                      # stereo of double bond between C5 and C6 is unknown
		 *   X
		 *    \                 # If X is not carbon (C6 is terminal)
		 *     C6               X ==  H      && Y ==  H     -> C5 == 'F' && C6 == 'n'
		 *    /  X              X ==  H      && Y == !H     -> C5 == 'F' && C6 == 'F'
		 *   Y    C5--O5        X ==  O      && Y ==  O     -> C5 == 'F' && C6 == 'N'
		 *       /      \       X ==  !(H,O) && Y == !(H,O) -> C5 == 'F' && C6 == 'N'
		 *  ?--C4        C1--?  # If X is carbon (C6 is not terminal)
		 *       \      /       Y ==  H -> C5 == 'F' && C6 == 'f'
		 *        C3--C2        Y == !H -> C5 == 'F' && C6 == 'F'
		 *       /      \
		 *      ?        ?
		 ******************************************************************************/
		if ( pos1 == m_objMonosaccharide.getRingEnd() ) {     // out of ring and contain ring end
			strReplace1 = "F";
			strReplace2 = (strPos2 == 'd')? "n" : "F";
			// For terminal
			if ( pos2 == numC ) {
				strReplace2 = (strPos2 == 'm')? "f" : "F";
			}
			sb.replace(pos1-1, pos1, strReplace1);
			sb.replace(pos2-1, pos2, strReplace2);
			return true;
		}

		/******* Double bond contain ring end carbon in cyclic form **************************
		 *                      # C5 is ring end carbon
		 *                      # stereo of double bond between C4 and C5 is E (entgegen)
		 *
		 *                      # If Y is not carbon (C5 is terminal)
		 *      Y               X ==  H && Y ==  H -> C4 == 'z' && C5 == 'z'
		 *       \              X == !H && Y ==  H -> C4 == 'Z' && C5 == 'z'
		 *        C5--O5        X == !H && Y == !H -> C4 == 'Z' && C5 == 'Z'
		 *      //      \
		 *  X--C4        C1--?  # If Y is carbon (C5 is not terminal)
		 *       \      /       X ==  H -> C4 == 'e' && C5 == 'E'
		 *        C3--C2        X == !H -> C4 == 'E' && C5 == 'E'
		 *       /      \
		 *      ?        ?
		 ******************************************************************************/
		if ( pos2 == m_objMonosaccharide.getRingEnd() ) {     // on ring and and contain ring end
			strReplace1 = (pos2 == numC)? "z" : "e";
			strReplace2 = (pos2 == numC)? "z" : "E";
			if ( strPos1 != 'd' )
				strReplace1 = strReplace1.toUpperCase();
			sb.replace(pos1-1, pos1, strReplace1);
			sb.replace(pos2-1, pos2, strReplace2);
			return true;
		}

		/******* Double bond in cyclic form *******************************************
		 *      ?               # stereo of double bond between C2 and C3 is Z (zusammen)
		 *       \
		 *        C5--O5
		 *       /      \       X ==  H  -> C2 == 'z'
		 *  ?--C4        C1--?  X == !H  -> C2 == 'Z'
		 *       \      /
		 *        C3==C2        Y ==  H  -> C3 == 'z'
		 *       /      \       Y == !H  -> C3 == 'Z'
		 *      Y        X
		 ******************************************************************************/
		strReplace1 = (strPos1 == 'd')? "z" : "Z";
		strReplace2 = (strPos2 == 'd')? "z" : "Z";
		sb.replace(pos1-1, pos1, strReplace1);
		sb.replace(pos2-1, pos2, strReplace2);
		return true;
	}

	/**
	 * Check symmetry of skeleton code and invert
	 * @param sb StringBuilder of SkeletonCode
	 * @return SkeletonCode is inverted
	 */
	private boolean checkSkeletonCodeSymmetry(StringBuilder sb) {
		int numC = sb.length();
		char dlrev = '*';
		int dlrevpos = -1;
		StringBuilder sbrev = new StringBuilder();
		for ( int i=numC; i > 0; i++) {
			char rev = sb.charAt(i-1);
			rev = (rev == '1')? '2' : (rev == '2')? '1' : rev;
			if ( dlrev == '*' && (rev == '1' || rev == '2' ) ) {
				dlrev = rev;
				dlrevpos = i-1;
			}
			sbrev.append( String.valueOf(rev) );
		}
		// No change when inverted code is also "L" or same string
		if ( dlrev != '1' ) return false;
		if ( sb.toString().equals(sbrev.toString()) ) return false;

		sb.reverse();

		return false;
	}

	/**
	 * Convert substituent to MAP code
	 * @param sub Substituent object
	 * @param skeleton String of skeleton code
	 * @return MAP code
	 */
	private String getMAPCode(Substituent sub, StringBuilder sb) {
		MAP t_objMAP = new MAP(sub);
		if ( this.m_hashProbabilities.get(sub) != null ) {
			UnderdeterminedSubTree t_oUndet = this.m_hashProbabilities.get(sub);
			t_objMAP = new MAP(sub, t_oUndet.getConnection(), null);
		}
		String t_strMAP = t_objMAP.getCode();
		if ( t_strMAP == null )
			System.out.println("There is an error in MAP :" + this.m_objMonosaccharide.getGlycoCTName() + "|" + sub.getSubstituentType().getName() );

		if ( t_strMAP.charAt(0) != '?' && ! Character.isDigit( t_strMAP.charAt(0) )  ){
			System.out.println(t_strMAP);
			return null;
		}
		return t_strMAP;
	}

	/**
	 * Modify SkeletonCode by Substituent
	 * @param sub Substituent
	 * @param a_strMAP MAPCode of Substituent
	 * @param sb StringBuilder of SkeletonCode
	 * @return String of modified SkeletonCode
	 */
	private String modifySkeletonCodeBySubstituent(Substituent sub, String a_strMAP, StringBuilder sb) {

		if ( sb.toString().contains("<0>") ) return "";
		if ( !Character.isDigit( a_strMAP.charAt(0) ) || !a_strMAP.contains("*") ) return "";

		String t_extraMAP = "";
		Integer pos = Integer.valueOf( a_strMAP.substring(0, 1) );
		char head = ( a_strMAP.contains("*") )? a_strMAP.charAt( a_strMAP.indexOf("*")+1 ) : 'O';
//		System.out.println( pos );
		// For cases that hydroxyl or calbonyl group cannot be omitted and/or skeleton code has change
		if ( sb.charAt(pos-1) == 'a' ) {
			// For carbonyl acid modification
			if ( head != 'O' ) {
				// if substituted hydroxy group on carboxyl group
				sb.replace(pos-1, pos, "A");
			}
			t_extraMAP = pos + "*=O";
		} else if ( sb.charAt(pos-1) == 'h' ) {
			// for terminal hydroxyl group
			if ( head != 'O' ) {
				// if substituted hydroxyl group
				sb.replace(pos-1, pos, "H");
			}
		}
		// For substitution of H on carbon
		if ( sub.getParentEdge() != null &&
				sub.getParentEdge().getGlycosidicLinkages().get(0).getParentLinkageType() == LinkageType.H_LOSE ) {
			System.out.println("LinkageType is H_LOSE \"h\".");

			t_extraMAP = pos + "*O";
			boolean isSwap = false;
			String t_strSymbol = ""+a_strMAP.charAt(2);
			if ( a_strMAP.length()>3 && Character.isLowerCase( a_strMAP.charAt(3) ) )
				t_strSymbol += a_strMAP.charAt(3);
			int iSymbol = AtomicPropaties.forSymbol(t_strSymbol).getAtomicNumber();
			if ( iSymbol > 16 ) {
				isSwap = true;
			} else if ( a_strMAP.length() < t_extraMAP.length() ) {
				isSwap = true;
			}
			char c = sb.charAt(pos-1);
			if ( isSwap ) {
				c = (c == '1')? '2' :
					(c == '2')? '1' :
					(c == '3')? '4' :
					(c == '4')? '3' : c;
			}
			// Swap chirality
			String replace = (c == '1')? "5" :
							 (c == '2')? "6" :
							 (c == '3')? "7" :
							 (c == '4')? "8" :
							 (c == 'x')? "X" : ""+c;
			sb.replace(pos-1, pos, replace);
		}
		return t_extraMAP;
	}

	private String insertProbability(Substituent sub, String a_strMAP) {
		if ( this.m_hashProbabilities.get(sub) == null ) return a_strMAP;
		UnderdeterminedSubTree t_objUnd = this.m_hashProbabilities.get(sub);
		double t_dLower = t_objUnd.getProbabilityLower()/100;
		double t_dUpper = t_objUnd.getProbabilityUpper()/100;
		if (t_dUpper == 1.0 && t_dLower == 1.0 ) return a_strMAP;

		String t_strProb = NumberFormat.getNumberInstance().format(t_dLower).substring(1);
		if (t_dUpper < 0.0 && t_dLower < 0.0) { // probability is unknown
			t_strProb = "?";
		}
		if ( t_dUpper != t_dLower ) {
			t_strProb += ":"+ NumberFormat.getNumberInstance().format(t_dUpper).substring(1);
		}
		t_strProb = "%"+t_strProb+"%";

		int pos = (a_strMAP.contains("*"))? a_strMAP.indexOf("*") : a_strMAP.length();
		StringBuffer t_bufMAP = new StringBuffer(a_strMAP);
		t_bufMAP.insert(pos, t_strProb);
		return t_bufMAP.toString();
	}

	private void invertLinkagePosition() throws GlycoconjugateException {
		Integer length = this.m_objMonosaccharide.getSuperclass().getCAtomCount();
		// For child edges
		for ( GlycoEdge childEdge : this.m_objMonosaccharide.getChildEdges() ) {
			for ( Linkage link : childEdge.getGlycosidicLinkages() ) {
				ArrayList<Integer> newLinkages = new ArrayList<Integer>();
				for ( Integer pos : link.getParentLinkages() )
					newLinkages.add( (pos == -1)? -1 : length-pos+1 );
				link.setParentLinkages(newLinkages);
			}
		}

		if ( this.m_objMonosaccharide.getParentEdge() == null ) return;
		// For parent edges
		GlycoEdge parentEdge = this.m_objMonosaccharide.getParentEdge();
		for ( Linkage link : parentEdge.getGlycosidicLinkages() ) {
			ArrayList<Integer> newLinkages = new ArrayList<Integer>();
			for ( Integer pos : link.getChildLinkages() )
				newLinkages.add( (pos == -1)? -1 : length-pos+1 );
			link.setChildLinkages(newLinkages);
		}

	}
}
