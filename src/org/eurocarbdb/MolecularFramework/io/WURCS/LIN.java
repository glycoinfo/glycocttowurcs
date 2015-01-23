package org.eurocarbdb.MolecularFramework.io.WURCS;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
 * Class for WURCS LIN conversion from GlycoEdes (RepeatingUnit and UnderdeterminedSubtree)
 * @author MasaakiMatsubara
 *
 */
public class LIN {
	public static final int DEFAULT       = 0;
	public static final int REPEAT        = 1;
	public static final int ALTERNATIVE   = 2;
	public static final int UNDERDETERMINED = 3;

	private int m_iType = LIN.DEFAULT;
	private int m_iMinRepeat = -1;
	private int m_iMaxRepeat = -1;
	private double m_iProbabilityUpper = 100.0;
	private double m_iProbabilityLower = 100.0;

	private ArrayList<GlycoNode> m_aParentNodes = new ArrayList<GlycoNode>();
	private ArrayList<GlycoNode> m_aChildNodes = new ArrayList<GlycoNode>();
	private GlycoEdge m_objParentEdge = null;
	private GlycoEdge m_objChildEdge = null;
	private ArrayList<Integer> m_objParentPositions = new ArrayList<Integer>();
	private ArrayList<Integer> m_objChildPositions = new ArrayList<Integer>();

	private int m_iMAPParentPosition;
	private int m_iMAPChildPosition;

	private Substituent m_objSubstituent = null;

	public LIN( Linkage a_objLinkage, GlycoNode a_objParentNode, GlycoNode a_objChildNode ) {
		this.m_aParentNodes.add( a_objParentNode );
		this.m_aChildNodes.add( a_objChildNode );
		this.m_objParentPositions = a_objLinkage.getParentLinkages();
		this.m_objChildPositions = a_objLinkage.getChildLinkages();
	}

	public LIN( Linkage a_objLinkage, ArrayList<GlycoNode> a_aParents, GlycoNode a_objChild, Substituent a_objSubstituent) {
		this.m_objSubstituent = a_objSubstituent;
		this.m_aParentNodes = a_aParents;
		this.m_aChildNodes.add( a_objChild );
		this.m_objParentPositions = a_objLinkage.getParentLinkages();
		this.m_objChildPositions = a_objLinkage.getChildLinkages();
	}

	public LIN( GlycoEdge a_objParentEdge, GlycoEdge a_objChildEdge, GlycoNode a_objParent, GlycoNode a_objChild, Substituent a_objSubstituent ) {
		this.m_objSubstituent = a_objSubstituent;
		this.m_objParentEdge = a_objParentEdge;
		this.m_objChildEdge = a_objChildEdge;
		this.m_aParentNodes.add( a_objParent );
		this.m_aChildNodes.add( a_objChild );
		this.m_objParentPositions = a_objParentEdge.getGlycosidicLinkages().get(0).getParentLinkages();
		this.m_objChildPositions = a_objChildEdge.getGlycosidicLinkages().get(0).getChildLinkages();
	}

	public LIN( GlycoEdge a_objParentEdge, GlycoEdge a_objChildEdge, ArrayList<GlycoNode> a_aParents, GlycoNode a_objChild, Substituent a_objSubstituent ) {
		this.m_objSubstituent = a_objSubstituent;
		this.m_objParentEdge = a_objParentEdge;
		this.m_objChildEdge = a_objChildEdge;
		this.m_aParentNodes = a_aParents;
		this.m_aChildNodes.add( a_objChild );
		this.m_objParentPositions = a_objParentEdge.getGlycosidicLinkages().get(0).getParentLinkages();
		if ( a_objChildEdge != null )
			this.m_objChildPositions = a_objChildEdge.getGlycosidicLinkages().get(0).getChildLinkages();
	}


	public void setType(int a_iType) {
		this.m_iType = a_iType;
	}

	public ArrayList<GlycoNode> getParentNodes() {
		return this.m_aParentNodes;
	}

	public ArrayList<GlycoNode> getChildNodes() {
		return this.m_aChildNodes;
	}

	public ArrayList<Integer> getParentPositions() {
		return this.m_objParentPositions;
	}

	public ArrayList<Integer> getChildPositions() {
		return this.m_objChildPositions;
	}

	public int getMAPParentPosition() {
		return this.m_iMAPParentPosition;
	}

	public int getMAPChildPosition() {
		return this.m_iMAPChildPosition;
	}

	public int getType() {
		return this.m_iType;
	}

    /**
     *
     * @return minima count for this repeat unit ; -1 for unknown
     */
	public int getMinRepeatCount() {
		return this.m_iMinRepeat;
	}

    /**
     *
     * @return maxima count for this repeat unit ; -1 for unknown
     */
	public int getMaxRepeatCount() {
		return this.m_iMaxRepeat;
	}

    /**
     *
     */
    public void setMinRepeatCount(int a_iCount) {
        this.m_iMinRepeat = a_iCount;
    }

    /**
     *
     */
    public void setMaxRepeatCount(int a_iCount) {
        this.m_iMaxRepeat = a_iCount;
    }

	public void setProbabilityUpper(double a_dProbability) {
		this.m_iProbabilityUpper = a_dProbability;
	}

	public void setProbabilityLower(double a_dProbability) {
		this.m_iProbabilityLower = a_dProbability;
	}

    /**
     *
     * @return Upper probability for this undetermined subtree ; 0.0 to 100.0
     */
	public double getProbabilityUpper() {
		return this.m_iProbabilityUpper;
	}

    /**
     *
     * @return Lower probability for this undetermined subtree ; 0.0 to 100.0
     */
	public double getProbabilityLower() {
		return this.m_iProbabilityLower;
	}

	public boolean hasSubstituent() {
		return ( this.m_objSubstituent != null );
	}

	public Substituent getSubstituent() {
		return this.m_objSubstituent;
	}

	public MAP getMAP() {
		if ( this.m_objSubstituent == null ) return null;
		MAP t_objMAP = new MAP(this.m_objSubstituent, this.m_objParentEdge, this.m_objChildEdge);
		if( t_objMAP.getCode() == null )
			System.out.println("There is an error in MAP :" + this.m_objSubstituent.getSubstituentType().getName() );

		return t_objMAP;
	}

	public String getLINCode(HashMap<Monosaccharide, Integer> hashMStoID) throws GlycoVisitorException {
		String t_strNodePos = "";

		// For parent node
		String t_strParent = this.getGLIP( hashMStoID, this.getParentNodes(), this.getParentPositions() );
		String t_strChild  = this.getGLIP( hashMStoID, this.getChildNodes(), this.getChildPositions() );
//		System.out.println(t_strParent);

		// Check IDs and postions of parent and child nodes for swap
		boolean t_bSwap = this.checkSwap(hashMStoID, t_strParent, t_strChild);

		// For probability section
		if ( this.getType() == LIN.UNDERDETERMINED ) {
			double t_dLower = this.getProbabilityLower()/100;
			double t_dUpper = this.getProbabilityUpper()/100;
			String t_strProb = "";
			if (t_dUpper < 0.0 && t_dLower < 0.0) { // probability is unknown
				t_strProb = "%?%";
			} else if (! (t_dUpper == 1.0 && t_dLower == 1.0) ) {
				t_strProb = NumberFormat.getNumberInstance().format(t_dLower).substring(1);
				if ( t_dUpper != t_dLower ) {
					t_strProb += ":"+ NumberFormat.getNumberInstance().format(t_dUpper).substring(1);
				}
				t_strProb = "%"+t_strProb+"%";
			}
			if ( t_strParent.equals("") ) {
				t_strChild += t_strProb;
			} else {
				t_strChild = t_strProb + t_strChild;
			}
		}

		// Swap parent and child
		if ( t_bSwap ) {
			String temp = t_strParent;
			t_strParent = t_strChild;
			t_strChild = temp;
		}

		String t_sMAP = "";
		if ( this.hasSubstituent() ) {
			t_sMAP = this.getMAP().getCode();
		}


		String t_sMLU = "";
		if ( t_strChild == "" && t_strParent == "" ) {
			throw new GlycoVisitorException("MLU must have at least one node.");
		} else if ( t_strChild == "" || t_strParent == "" ) {
//			t_sMLU += t_strChild.replace("-2", "") + t_strParent.replace("-1", "") + t_sMAP.substring(1);
			t_sMLU += t_strChild.replace("2", "") + t_strParent.replace("1", "") + t_sMAP.substring(1);
		} else {
//			t_sMLU += t_strChild +","+ t_strParent + t_sMAP;
			t_sMLU += t_strChild +"-"+ t_strParent + t_sMAP;
		}


		// for type section
		if ( this.getType() == LIN.REPEAT ) {
			String t_strType = "";
			t_strType += "~";
			if ( this.getMinRepeatCount() == -1 ) { // repeat count is unknown
				t_strType += "n";
			} else {
				t_strType += this.getMinRepeatCount();
			}
			if ( this.getMinRepeatCount() != this.getMaxRepeatCount() ) {
				t_strType += ":";
				if ( this.getMaxRepeatCount() == -1 ) { // repeat count is unknown
					t_strType += "n";
				} else {
					t_strType += this.getMaxRepeatCount();
				}
			}
			t_sMLU +=  t_strType;
		}

		return t_sMLU;
	}

	public String getGLIP(HashMap<Monosaccharide, Integer> hashMStoID, ArrayList<GlycoNode> aNodes, ArrayList<Integer> aPositions) {
		ArrayList<Integer> t_aNodeIDs = new ArrayList<Integer>();
		int t_iMinNodeID = hashMStoID.size()+1;
		int t_iMaxNodeID = 0;
		for ( GlycoNode t_objNode : aNodes ) {
			if (! hashMStoID.containsKey( t_objNode ) ) continue;
			int t_iNodeID = hashMStoID.get( t_objNode );
			t_aNodeIDs.add( t_iNodeID );
			if ( t_iNodeID < t_iMinNodeID ) t_iMinNodeID = t_iNodeID;
			if ( t_iNodeID > t_iMaxNodeID ) t_iMaxNodeID = t_iNodeID;
		}
		Collections.sort(t_aNodeIDs);
		String t_strCOLIN = "";
		for ( int t_iNodeID : t_aNodeIDs ) {
			String t_strNodePos = "";
			for ( Integer t_iNodePos : aPositions ) {
//				if ( t_iParentPos == null ) System.out.println(this.m_sMLU);
				String t_strPos = (t_iNodePos == -1)? "?" : t_iNodePos.toString();
//				if ( t_strPos.equals("-1") ) t_strPos = "?";
//				if ( t_strCOLIN != "" ) t_strCOLIN+="\\";
				if ( t_strCOLIN != "" ) t_strCOLIN+="|";
//				t_strNodePos = t_iNodeID +"+"+ t_strPos;
//				t_strNodePos = t_iNodeID +"-"+ t_strPos;
				t_strNodePos = this.convertResidueID(t_iNodeID) + t_strPos;
				if ( this.hasSubstituent() && this.getMAP().isSwappedCarbonPosition() != null ) {

					// TODO: add DirectionOnBackbone
					Monosaccharide MS = null;
					for ( Monosaccharide ms : hashMStoID.keySet() ) {
						if(hashMStoID.get(ms) == t_iNodeID) MS = ms;
					}
					t_strNodePos += this.getDirection(MS, t_iNodePos);
//					t_strNodePos += ( !this.getMAP().isSwappedCarbonPosition() )? "-1" : "-2";
					t_strNodePos += ( !this.getMAP().isSwappedCarbonPosition() )?
									( aNodes.equals( this.getParentNodes() ) )? "2" : "1" :
									( aNodes.equals( this.getChildNodes() ) )? "1" : "2";
				}
				if ( aNodes.size() > 1 || aPositions.size() > 1 ) {
					t_strCOLIN += t_strNodePos;
				} else {
					t_strCOLIN = t_strNodePos;
				}
			}
		}
		return t_strCOLIN;
	}

	/**
	 * Check priority of parent and child
	 * @param t_strParent
	 * @param t_strChild
	 * @return True if child is prior than parent
	 */
	private boolean checkSwap(HashMap<Monosaccharide, Integer> hashMStoID, String t_strParent, String t_strChild ) {
		if ( this.getParentNodes().size() < this.getChildNodes().size() ) return true;
		if ( this.getParentNodes().size() > this.getChildNodes().size() ) return false;

		if ( this.getParentPositions().size() < this.getChildPositions().size() ) return true;
		if ( this.getParentPositions().size() > this.getChildPositions().size() ) return false;
		for (int i=0; i < this.getParentPositions().size(); i++) {
			if ( this.getParentPositions().get(i) < this.getChildPositions().get(i) ) return true;
		}
		int parentID = hashMStoID.get( this.getParentNodes().get(0) );
		int childID = hashMStoID.get( this.getChildNodes().get(0) );
		if ( parentID < childID ) return true;
		if ( parentID > childID ) return false;

		if ( t_strChild.compareTo(t_strParent) > 0 ) return true;
		return false;
	}

	private char getDirection(Monosaccharide a_oMS, int a_iPos) {
		char Direction = 'n';
		RES res = new RES(a_oMS);
		if ( this.m_objSubstituent != null );
			res.addSubstituent(this.m_objSubstituent);
		try {
			String sRESCode = res.getRESCode();
			char CD = (a_iPos == -1)? 'x' : sRESCode.charAt(a_iPos-1);
			if ( Character.isDigit(CD) ) {
				Direction = ((int)CD%2 != 0 )? 'u' : 'd';
			} else if (Character.isAlphabetic(CD)) {
				if ( CD == 'e' || CD == 'E' ) Direction = 'e';
				if ( CD == 'z' || CD == 'Z' ) Direction = 'z';
				if ( CD == 'x' ) Direction = 'x';
			}
		} catch (WURCSException e) {
			return Direction;
		}
		return Direction;
	}

	private String convertResidueID(int a_iResID) {
		//   1 ->  a
		//   2 ->  b
		//  26 ->  z
		//  27 ->  A
		//  28 ->  B
		//  52 ->  Z
		//  53 -> aa
		//  54 -> ab
		// 104 -> aZ
		// 105 -> ba

		String t_strRes = "";

//		System.out.print(a_iResID);
		int t_iRemainder = a_iResID;
		LinkedList<Integer> t_aQuotients = new LinkedList<Integer>();
		while(t_iRemainder > 0) {
			int t_iQuotient = t_iRemainder % 52;
			t_iQuotient = (t_iQuotient == 0)? 52 : t_iQuotient;
			t_aQuotients.addFirst(t_iQuotient);
			t_iRemainder = (int)Math.floor( (t_iRemainder - t_iQuotient) / 52 );
		}

		// int of alphabet
		int t_iLower = (int)'a' - 1;
		int t_iUpper = (int)'A' - 1;
		for (Integer q : t_aQuotients) {
			int alphabet = ( q > 26 )? t_iUpper + q - 26 : t_iLower + q;
			t_strRes += (char)alphabet;
		}
//		System.out.println("->"+t_strRes);

		return t_strRes;
	}
}
