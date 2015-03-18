package org.eurocarbdb.MolecularFramework.io.WURCS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
 * Class for export WURCS String from Sugar
 * @author MasaakiMatsubara
 *
 */
public class SugarExporterWURCS implements GlycoVisitor
{
	private String m_strVersion = "2.0";
	private String m_strWURCSCode = "";
	private String m_strWURCSCodeCompress = "";
	private String m_strWURCSCodeCompressWithRepeat = "";

	private String m_sRES = "";
	private String m_sLIN = "";
	private ArrayList<String> m_aUniqueRESs = new ArrayList<String>();
	private String m_sRESCompress = "";
	private String m_sRESCompressWithRepeat = "";

	private ArrayList<RES> m_aRESs = new ArrayList<RES>();
	private ArrayList<LIN> m_aLINs = new ArrayList<LIN>();

	private Integer m_iBMUCounter;
	private HashMap<Monosaccharide,Integer> m_hashNodeIDforRES = new HashMap<Monosaccharide,Integer>();
	private HashMap<GlycoNode,RES> m_hashNodeToBMU = new HashMap<GlycoNode,RES>();

	private ArrayList<SugarUnitRepeat> m_aRepeats = new ArrayList<SugarUnitRepeat>();
	private ArrayList<NonMonosaccharide> m_aNonMS = new ArrayList<NonMonosaccharide>();
	private ArrayList<UnderdeterminedSubTree> m_aUnderdeterminedTrees = new ArrayList<UnderdeterminedSubTree>();
	private ArrayList <SugarUnitAlternative> m_aAlternativeUnits = new ArrayList <SugarUnitAlternative> ();
	private HashMap<GlycoNode,Integer> m_hashNodeID = new HashMap<GlycoNode,Integer>();
	private HashMap<GlycoEdge,Integer> m_hashEdgeID = new HashMap<GlycoEdge,Integer>();
	private HashMap<Linkage,Integer> m_hashLinkageID = new HashMap<Linkage,Integer>();
	private String m_sUniqueRES;


	public void visit(Monosaccharide a_objMonosaccharide) throws GlycoVisitorException {
		/// make and add BMU
		this.m_iBMUCounter++;
		this.m_hashNodeIDforRES.put(a_objMonosaccharide,this.m_iBMUCounter);
		this.m_hashNodeToBMU.put( a_objMonosaccharide, new RES(a_objMonosaccharide) );
		this.m_aRESs.add( this.m_hashNodeToBMU.get(a_objMonosaccharide) );

		GlycoEdge t_objEdge = a_objMonosaccharide.getParentEdge();
		if ( t_objEdge != null )
		{
			this.makeLIN(t_objEdge);
		}
	}

	public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
		this.m_aNonMS.add(a_objResidue);
	}

	public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException {
		this.m_aRepeats.add(a_objRepeat);

		for (UnderdeterminedSubTree t_oSubtree : a_objRepeat.getUndeterminedSubTrees())
		{
			this.m_aUnderdeterminedTrees.add(t_oSubtree);
		}

		// traverse repeating unit
		GlycoTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(a_objRepeat);

		// Count end of repeat
		this.m_aRESs.get(this.m_aRESs.size()-1).countEndOfRepeat();

		GlycoEdge t_objEdge = a_objRepeat.getParentEdge();
		if ( t_objEdge != null )
		{
			this.makeLIN(t_objEdge);
		}

		// LIN for repeating edge
		this.makeLIN( a_objRepeat.getRepeatLinkage(), a_objRepeat );

		try {
			for ( GlycoNode t_objRoot : a_objRepeat.getRootNodes() ) {
				RES t_objBMU = this.m_hashNodeToBMU.get(t_objRoot);
				// Set root ob subgraph
				if ( t_objBMU != null ) t_objBMU.setRootOfSubgraph();
			}
		} catch (GlycoconjugateException e) {
			throw new GlycoVisitorException( e.getMessage() );
		}
	}

	public void visit(Substituent a_objSubstituent) throws GlycoVisitorException
	{
		// ALIN for BMU
		GlycoNode t_objParent = a_objSubstituent.getParentNode();
		if ( t_objParent == null) return;
		if (! this.m_hashNodeIDforRES.containsKey(t_objParent) ) return;
		if (! a_objSubstituent.getChildEdges().isEmpty() ) return;
		for ( SugarUnitRepeat t_oRep : this.m_aRepeats) {
			if ( t_oRep.containsNode( a_objSubstituent ) &&
				( t_oRep.getRepeatLinkage().getParent().equals(a_objSubstituent) ||
				  t_oRep.getRepeatLinkage().getChild().equals(a_objSubstituent) ) ) {
				return;
			}
		}

		// If this substituent is only link to parent monosaccharide,
		// add this to parent BMU
		RES t_objBMU = this.m_hashNodeToBMU.get(t_objParent);
		if ( t_objBMU != null ) t_objBMU.addSubstituent(a_objSubstituent);
	}

	public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
		GlycoEdge t_objEdge = a_objCyclic.getParentEdge();
		if ( t_objEdge != null )
		{
			this.makeLIN(t_objEdge);
		}
	}

	public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
		GlycoEdge t_objEdge = a_objAlternative.getParentEdge();
		if ( t_objEdge != null )
		{
			// TODO: Make LIN for SugarUnitAlternative
//			this.makeLIN(t_objEdge);
		}
	}

	public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException
	{
		throw new GlycoVisitorException("UnvalidatedGlycoNode is NOT handled in WURCS.");
	}

	public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException
	{
		//stays empty
	}

	public void start(GlycoNode a_objNode) throws GlycoVisitorException
	{
		this.clear();
		GlycoTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverse(a_objNode);

	}

	public void start(Sugar a_objSugar) throws GlycoVisitorException
	{
		this.clear();
		try {
			Sugar cloneSugar = a_objSugar.copy();
//			if ( cloneSugar.getRootNodes().size() > 1)
//				throw new GlycoVisitorException( "Cannot encode sugar with multiple root nodes." );
			// Concurrent addition-information
			if (cloneSugar.getUndeterminedSubTrees().size()>0)
			{
				for (UnderdeterminedSubTree t_oSubtree : cloneSugar.getUndeterminedSubTrees())
				{
					this.m_aUnderdeterminedTrees.add(t_oSubtree);
				}
			}

			GlycoTraverser t_objTraverser = this.getTraverser(this);
			t_objTraverser.traverseGraph(cloneSugar);
			makeWURCS(t_objTraverser);
		} catch (GlycoconjugateException e) {
			throw new GlycoVisitorException( e.getMessage() );
		}

	}

	public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
	{
		return new WURCSTraverser(a_objVisitor);
	}

	public void clear() {
		this.m_aRepeats.clear();
		this.m_aUnderdeterminedTrees.clear();
		this.m_hashEdgeID.clear();
		this.m_hashNodeID.clear();
		this.m_aNonMS.clear();
		this.m_aAlternativeUnits.clear();
		this.m_hashLinkageID.clear();

		this.m_iBMUCounter = 0;
		this.m_aRESs.clear();
		this.m_aUniqueRESs.clear();
		this.m_aLINs.clear();
		this.m_hashNodeIDforRES.clear();
		this.m_sRES = "";
		this.m_sUniqueRES = "";
		this.m_sRESCompress = "";
		this.m_sLIN = "";
		this.m_strWURCSCode = "";
		this.m_strWURCSCodeCompress = "";
	}

	public String getRESs() throws GlycoVisitorException {
		for ( RES t_objRES : this.m_aRESs ) {
//			System.out.println( t_objBMU.getMonosaccharide().getGlycoCTName() );
//			ArrayList<String> subs = new ArrayList<String>();
//			for (Substituent t_objSub : t_objBMU.getSubstituents() ) {
//				subs.add( t_objSub.getSubstituentType().getName() );
//			}
//			System.out.println( subs.toString() );
//			System.out.println( t_objBMU.getSkeltonCode() );
			try {
//				this.m_sBMU = "[" + t_objBMU.getSkeltonCode() + "]" + this.m_sBMU;
				String sRESCode = t_objRES.getRESCode();
				this.m_sRES += "["+ sRESCode +"]";
				if ( !this.m_aUniqueRESs.contains(sRESCode) ) {
					this.m_sUniqueRES += "["+ sRESCode +"]";
					this.m_aUniqueRESs.add(sRESCode);
				}
//				if ( !this.m_sBMUCompress.equals("") ) this.m_sBMUCompress += "|";
				if ( !this.m_sRESCompress.equals("") ) {
					this.m_sRESCompress += "-";
					this.m_sRESCompressWithRepeat += "-";
				}

				for ( int i=0; i<t_objRES.getStartOfRepeatCount(); i++ )
					this.m_sRESCompressWithRepeat += "<";

				this.m_sRESCompress           += this.m_aUniqueRESs.indexOf(sRESCode)+1;
				this.m_sRESCompressWithRepeat += this.m_aUniqueRESs.indexOf(sRESCode)+1;

				for ( int i=0; i<t_objRES.getEndOfRepeatCount(); i++ )
					this.m_sRESCompressWithRepeat += ">";


				if ( !t_objRES.isInverted() ) continue;
				this.invertLinkagePosition(t_objRES.getMonosaccharide());
			} catch (WURCSException e) {
				throw new GlycoVisitorException(e.getErrorMessage());
			} catch (GlycoconjugateException e) {
				throw new GlycoVisitorException(e.getMessage());
			}
		}
		return this.m_sRES;
	}

	private void invertLinkagePosition(Monosaccharide a_objMonosaccharide) throws GlycoconjugateException {
		int numC = a_objMonosaccharide.getSuperclass().getCAtomCount();
		// For repeating units
		for ( SugarUnitRepeat rep : this.m_aRepeats ) {
			GlycoEdge repLinkage = rep.getRepeatLinkage();
			if ( !repLinkage.getParent().equals(a_objMonosaccharide) && !repLinkage.getChild().equals(a_objMonosaccharide) )
				continue;
			for ( Linkage link : repLinkage.getGlycosidicLinkages() ) {
				ArrayList<Integer> Linkages
					= ( repLinkage.getParent().equals(a_objMonosaccharide) )?
						link.getParentLinkages() : link.getChildLinkages() ;
				ArrayList<Integer> newLinkages = new ArrayList<Integer>();

				for ( Integer pos : Linkages )
					newLinkages.add( (pos == -1)? -1 : numC-pos+1 );

				if ( repLinkage.getParent().equals(a_objMonosaccharide) ) {
					link.setParentLinkages(newLinkages);
				} else {
					link.setChildLinkages(newLinkages);
				}
			}
		}
		for ( UnderdeterminedSubTree t_oUndetSubtree : this.m_aUnderdeterminedTrees ) {
			for ( GlycoNode t_oParent : t_oUndetSubtree.getParents() ) {
				if ( !t_oParent.equals(a_objMonosaccharide) ) continue;

				for ( Linkage link : t_oUndetSubtree.getConnection().getGlycosidicLinkages() ) {
					ArrayList<Integer> newLinkages = new ArrayList<Integer>();
					for ( Integer pos :link.getParentLinkages() )
						newLinkages.add( (pos == -1)? -1 : numC-pos+1 );
					link.setParentLinkages(newLinkages);
				}
			}
		}
	}

	public String getLINs() throws GlycoVisitorException {

		// Sort LINs
		final HashMap<Monosaccharide, Integer> hashMStoID = this.m_hashNodeIDforRES;
		Collections.sort(this.m_aLINs, new Comparator<LIN>() {

			public int compare(LIN oLIN1, LIN oLIN2) {
//				// LIN type: DEFAULT > REPEAT > UNDERDETERMIND > has probability
//				if ( LIN1.getType() == LIN.DEFAULT && LIN2.getType() != LIN.DEFAULT ) return -1;
//				if ( LIN1.getType() != LIN.DEFAULT && LIN2.getType() == LIN.DEFAULT ) return 1;
//				if ( LIN1.getType() == LIN.REPEAT && LIN2.getType() != LIN.REPEAT ) return -1;
//				if ( LIN1.getType() != LIN.REPEAT && LIN2.getType() == LIN.REPEAT ) return 1;
//				if ( LIN1.getType() == LIN.UNDERDETERMINED && LIN2.getType() != LIN.UNDERDETERMINED ) return -1;
//				if ( LIN1.getType() != LIN.UNDERDETERMINED && LIN2.getType() == LIN.UNDERDETERMINED ) return 1;
				// LIN type: DEFAULT > REPEAT
				if ( oLIN1.getType() != LIN.REPEAT && oLIN2.getType() == LIN.REPEAT ) return -1;
				if ( oLIN1.getType() == LIN.REPEAT && oLIN2.getType() != LIN.REPEAT ) return 1;

				// Repeat count
				if ( oLIN1.getMinRepeatCount() != -1 && oLIN2.getMinRepeatCount() == -1 ) return -1;
				if ( oLIN1.getMinRepeatCount() == -1 && oLIN2.getMinRepeatCount() != -1 ) return 1;

				try {
					String LINCode1 = oLIN1.getLINCode(hashMStoID);
					String LINCode2 = oLIN2.getLINCode(hashMStoID);
//					System.out.println(LINCode1 +":" +LINCode2);
//					System.out.println(LINCode1.indexOf('\\'));

					// Unknown nodeID or position
					if ( LINCode1.indexOf('?') == -1 && LINCode2.indexOf('?') != -1 ) return -1;
					if ( LINCode1.indexOf('?') != -1 && LINCode2.indexOf('?') == -1 ) return 1;
//					if ( LINCode1.indexOf('\\') == -1 && LINCode2.indexOf('\\') != -1 ) return -1;
//					if ( LINCode1.indexOf('\\') != -1 && LINCode2.indexOf('\\') == -1 ) return 1;

					// COLINs count
					int count1 = 0, count2 = 0;
					for ( int i=0; i<LINCode1.length(); i++ )
//						if ( LINCode1.charAt(i) == '\\' ) count1++;
						if ( LINCode1.charAt(i) == '|' ) count1++;
					for ( int i=0; i<LINCode2.length(); i++ )
//						if ( LINCode2.charAt(i) == '\\' ) count2++;
						if ( LINCode2.charAt(i) == '|' ) count2++;
					if ( count1 < count2 ) return -1;
					if ( count1 > count2 ) return 1;

					// Probability
					if ( LINCode1.indexOf('%') == -1 && LINCode2.indexOf('%') != -1 ) return -1;
					if ( LINCode1.indexOf('%') != -1 && LINCode2.indexOf('%') == -1 ) return 1;

					// For substituent node
					// Node ID
					if (  oLIN1.getParentNodes().isEmpty() && !oLIN2.getParentNodes().isEmpty() ) return -1;
					if ( !oLIN1.getParentNodes().isEmpty() &&  oLIN2.getParentNodes().isEmpty() ) return 1;
					if (  oLIN1.getChildNodes().isEmpty() && !oLIN2.getChildNodes().isEmpty() ) return -1;
					if ( !oLIN1.getChildNodes().isEmpty() &&  oLIN2.getChildNodes().isEmpty() ) return 1;

					if (  hashMStoID.containsKey( oLIN1.getParentNodes().get(0) ) && !hashMStoID.containsKey( oLIN2.getParentNodes().get(0) ) ) return -1;
					if ( !hashMStoID.containsKey( oLIN1.getParentNodes().get(0) ) &&  hashMStoID.containsKey( oLIN2.getParentNodes().get(0) ) ) return 1;
					if (  hashMStoID.containsKey( oLIN1.getChildNodes().get(0) ) && !hashMStoID.containsKey( oLIN2.getChildNodes().get(0) ) ) return -1;
					if ( !hashMStoID.containsKey( oLIN1.getChildNodes().get(0) ) &&  hashMStoID.containsKey( oLIN2.getChildNodes().get(0) ) ) return 1;

					int parentID1 = hashMStoID.get( oLIN1.getParentNodes().get(0) );
					int parentID2 = hashMStoID.get( oLIN2.getParentNodes().get(0) );
					int childID1 = hashMStoID.get( oLIN1.getChildNodes().get(0) );
					int childID2 = hashMStoID.get( oLIN2.getChildNodes().get(0) );
					if ( Math.min(parentID1,childID1) < Math.min(parentID2,childID2) ) return -1;
					if ( Math.min(parentID1,childID1) > Math.min(parentID2,childID2) ) return 1;
//					if ( Math.max(parentID1,childID1) < Math.max(parentID2,childID2) ) return -1;
//					if ( Math.max(parentID1,childID1) > Math.max(parentID2,childID2) ) return 1;

					// Position
					Integer pos1 = (parentID1 < childID1)? oLIN1.getParentPositions().get(0) : oLIN1.getChildPositions().get(0);
					Integer pos2 = (parentID2 < childID2)? oLIN2.getParentPositions().get(0) : oLIN2.getChildPositions().get(0);
					if ( pos1 < pos2 ) return -1;
					if ( pos1 > pos2 ) return 1;

					// Compare string
					return LINCode1.compareTo(LINCode2);
				} catch (GlycoVisitorException e) {
					e.printStackTrace();
					return 0;
				}
			}

		});
		for ( LIN t_objLIN : this.m_aLINs ) {
			if ( this.m_sLIN != "" ) this.m_sLIN += "_";
			this.m_sLIN += t_objLIN.getLINCode(hashMStoID);
		}
		return this.m_sLIN;
	}

	public String getWURCS() {
		return this.m_strWURCSCode;
	}

	public String getWURCSCompress() {
		return this.m_strWURCSCodeCompress;
	}

	public String getWURCSCompressWithRepeat() {
		return this.m_strWURCSCodeCompressWithRepeat;
	}

	private void makeLIN(GlycoEdge a_objEdge) throws GlycoVisitorException
	{
		this.makeLIN(a_objEdge, null);
	}

	private void makeLIN(GlycoEdge a_objEdge, GlycoNode a_objParentGraph) throws GlycoVisitorException
	{
		Substituent t_objSubstituent = null;
		GlycoEdge t_objParentEdge = a_objEdge;
		GlycoEdge t_objChildEdge = a_objEdge;

		GlycoNode t_objParent = a_objEdge.getParent();
		GlycoNode t_objChild = a_objEdge.getChild();

		// for connecting substituent
		if ( t_objParent.getClass() == Substituent.class && t_objChild.getClass() == Substituent.class ) {
			throw new GlycoVisitorException( "Substituent cannot connect to substituent." );
		}

		/****** LIN for repeating unit *********************************
		 *                        2,4 == Monosaccharide
		 *                        Repeat = 4(4+1)2     ->  2+4,4+1
		 *     3  4  4  4     4   1(4+1)Rep -> 1(4+1)2 ->  1+4,2+1
		 *   6--[--4--3--2--]--1  Rep(3+1)6 -> 4(3+1)6 ->  4+3,6+1
		 *           6|             2(4+1)3, 3(4+1)4, 3(6+1)5
		 *            5           ->2+4,3+1 | 3+4,4+1 | 3+6,5+1
		 *
		 *                        2 == Substituent
		 *                        Repeat = 4(4+1)3     ->  3+1,4+4*2*
		 *                        1(4+1)Rep -> 1(4+1)3 ->  1+4,3+1*2*
		 *                        Rep(3+1)6 -> 4(3+1)6 ->  4+3,6+1
		 *                          3(4+1)4, 3(6+1)5
		 *                        ->3+4,4+1 | 3+6,5+1
		 *
		 *                        4 == Substituent
		 *                        Repeat = 3(4+1)2     ->  2+1,3+4*4*
		 *                        1(4+1)Rep -> 1(4+1)2 ->  1+4,2+1
		 *                        Rep(3+1)6 -> 3(4+1)6 ->  4+3,6+1*4*
		 *                          2(4+1)3, 3(6+1)5
		 *                        ->2+4,3+1 | 3+6,5+1
		 ***************************************************************/

		// search monosaccharide for parent node
		GlycoNode t_objParentParent = t_objParent;
		while ( t_objParent.getClass() != Monosaccharide.class ) {
			// For substituent
			if ( t_objParent.getClass() == Substituent.class ) {
				t_objSubstituent = (Substituent)t_objParent;
				t_objParentParent = t_objParent;
				t_objParentEdge = t_objParent.getParentEdge();
				if ( t_objParentEdge == null ) {
					for ( SugarUnitRepeat t_oRep : this.m_aRepeats ) {
						if ( t_oRep.containsNode(a_objEdge.getParent()) ) {
							return;
						}
					}
					throw new GlycoVisitorException( "Substituent cannot be root node." );
				}
				t_objParent = t_objParentEdge.getParent();
			}
			// For repeating unit
			if ( t_objParent.getClass() == SugarUnitRepeat.class ) {
				SugarUnitRepeat t_oRep = (SugarUnitRepeat)t_objParent;
				t_objParentEdge = t_oRep.getRepeatLinkage();
				t_objParent = t_objParentEdge.getParent();
			}
			// Check connecting two substituent
			if ( t_objParentParent.getClass() == Substituent.class
				&& t_objParent.getClass() == Substituent.class ) {
				throw new GlycoVisitorException( "Substituent cannot connect to substituent." );
			}
		}

		// Search monosaccharide for child node
		GlycoNode t_objChildChild = t_objChild;
		while (t_objChild.getClass() != Monosaccharide.class) {
			// For cyclic
			if ( t_objChild.getClass() == SugarUnitCyclic.class ) {
				SugarUnitCyclic t_oCyclic = (SugarUnitCyclic)t_objChild;
				t_objChild = t_oCyclic.getCyclicStart();
			}
			// For substituent
			if ( t_objChild.getClass() == Substituent.class ) {
				t_objSubstituent = (Substituent)a_objEdge.getChild();
				t_objChildChild = t_objChild;
				t_objChildEdge = t_objChild.getChildEdges().get(0);
				t_objChild = t_objChildEdge.getChild();
			}
			// For repeating unit
			if ( t_objChild.getClass() == SugarUnitRepeat.class ) {
				SugarUnitRepeat t_oRep = (SugarUnitRepeat)t_objChild;
				t_objChildEdge = t_oRep.getRepeatLinkage();
				t_objChild = t_objChildEdge.getChild();
			}
			// Check connecting two substituent
			if ( t_objChildChild.getClass() == Substituent.class
				&& t_objChild.getClass() == Substituent.class ) {
				throw new GlycoVisitorException( "Substituent cannot connect to substituent." );
			}
		}

		// For connecting two substituent
		if ( t_objParent.getClass() == Substituent.class && t_objChild.getClass() == Substituent.class ) {
			throw new GlycoVisitorException( "Substituent cannot connect to substituent." );
		}

		// If there are two edges between parent node and child node
		if ( !t_objParentEdge.equals(t_objChildEdge) ) {
			LIN t_objLIN = new LIN( t_objParentEdge, t_objChildEdge, t_objParent, t_objChild, t_objSubstituent );
			if ( a_objParentGraph != null && a_objParentGraph.getClass() == SugarUnitRepeat.class ) {
				SugarUnitRepeat t_oRep = (SugarUnitRepeat)a_objParentGraph;
				t_objLIN.setType(LIN.REPEAT);
				t_objLIN.setMinRepeatCount( t_oRep.getMinRepeatCount() );
				t_objLIN.setMaxRepeatCount( t_oRep.getMaxRepeatCount() );

				this.m_hashNodeToBMU.get(t_objChild).countStartOfRepeat();
			}
			this.m_aLINs.add( t_objLIN );
			return;
		}

		for (Linkage t_oLink : a_objEdge.getGlycosidicLinkages() ) {
			LIN t_objLIN = new LIN( t_oLink, t_objParent, t_objChild );
			if ( a_objParentGraph != null && a_objParentGraph.getClass() == SugarUnitRepeat.class ) {
				SugarUnitRepeat t_oRep = (SugarUnitRepeat)a_objParentGraph;
				t_objLIN.setType(LIN.REPEAT);
				t_objLIN.setMinRepeatCount( t_oRep.getMinRepeatCount() );
				t_objLIN.setMaxRepeatCount( t_oRep.getMaxRepeatCount() );

				this.m_hashNodeToBMU.get(t_objChild).countStartOfRepeat();
}
			this.m_aLINs.add( t_objLIN );
		}

	}

	private void makeWURCS( GlycoTraverser t_objTraverser ) throws GlycoVisitorException {
		// traverse Underdetermined subtrees, array already prefilled
		WURCSUnderdeterminedSubtreeComparator t_oSubTreeComp = new WURCSUnderdeterminedSubtreeComparator();
		Collections.sort(this.m_aUnderdeterminedTrees,t_oSubTreeComp);

		for ( UnderdeterminedSubTree t_oUndetSubtree : this.m_aUnderdeterminedTrees ) {

			t_objTraverser = this.getTraverser(this);
			t_objTraverser.traverseGraph(t_oUndetSubtree);

			// make LINs for connection of underdetermined subtree
			ArrayList<GlycoNode> t_oParents = t_oUndetSubtree.getParents();
			GlycoEdge t_objParentEdge = t_oUndetSubtree.getConnection();
			GlycoNode t_objRootNode = null;
			try {
				t_objRootNode = t_oUndetSubtree.getRootNodes().get(0);
				// set flag for root of subtree
				RES t_objBMU = this.m_hashNodeToBMU.get(t_objRootNode);
				if ( t_objBMU != null ) t_objBMU.countStartOfRepeat();
			} catch (GlycoconjugateException e) {
				throw new GlycoVisitorException( e.getMessage() );
			}

			// Check parent nodes
			for ( GlycoNode t_oParent : t_oParents ) {
				if ( t_oParent.getClass() != Substituent.class ) continue;
				// Throw exception if there are substituents in parents of subgraph
				// Root node of subgraph is substituent
				if ( t_objRootNode.getClass() == Substituent.class )
					throw new GlycoVisitorException("Substituent cannot connect to substituent.");
				// Root node of subgraph is not substituent
				throw new GlycoVisitorException("Substituent cannot be parent of underdetermined subtree.");
			}

			// Root node of subgraph is not substituent
			if ( t_objRootNode.getClass() != Substituent.class ) {
				for ( Linkage t_oLink : t_objParentEdge.getGlycosidicLinkages() ) {
					LIN t_objLIN = new LIN( t_oLink, t_oParents, t_objRootNode, null );
					t_objLIN.setType(LIN.UNDERDETERMINED);
					t_objLIN.setProbabilityLower( t_oUndetSubtree.getProbabilityLower() );
					t_objLIN.setProbabilityUpper( t_oUndetSubtree.getProbabilityUpper() );
					this.m_aLINs.add( t_objLIN );
				}
				continue;
			}

			// Root node of subgraph is substituent
			GlycoEdge t_objChildEdge = null;
			GlycoNode t_objChildNode = null;
			if ( !t_objRootNode.getChildEdges().isEmpty() ) {
				// Substituent has child -> LIN
				t_objChildEdge = t_objRootNode.getChildEdges().get(0);
				t_objChildNode = t_objChildEdge.getChild();
			} else if ( t_oParents.size() == 1) {
				// Substituent has no child and one parent -> MAP for RES
				RES t_objRES = this.m_hashNodeToBMU.get( t_oUndetSubtree.getParents().get(0) );
				if ( t_objRES != null )
					t_objRES.addSubstituent( (Substituent)t_objRootNode, t_oUndetSubtree );
				continue;
			}
			// Root substituent has two or more parents -> MAP for LIN
			LIN t_objLIN = new LIN( t_objParentEdge, t_objChildEdge, t_oParents , t_objChildNode, (Substituent)t_objRootNode );
			// Set informations of underdetermined subtree to LIN
			t_objLIN.setType(LIN.UNDERDETERMINED);
			t_objLIN.setProbabilityLower( t_oUndetSubtree.getProbabilityLower() );
			t_objLIN.setProbabilityUpper( t_oUndetSubtree.getProbabilityUpper() );
			this.m_aLINs.add( t_objLIN );
		}

		this.m_strWURCSCode
			= "WURCS="+this.m_strVersion+"/"+ this.m_aRESs.size()+","+this.m_aLINs.size() +"/"+ getRESs()+getLINs();
		this.m_strWURCSCodeCompress
			= "WURCS="+this.m_strVersion+"/"+ this.m_aUniqueRESs.size()+","+this.m_aRESs.size()+","+this.m_aLINs.size()+"/"
			+ this.m_sUniqueRES +"/"+ this.m_sRESCompress + "/" + this.m_sLIN;
		this.m_strWURCSCodeCompressWithRepeat
			= "WURCS="+this.m_strVersion+"/"+ this.m_aUniqueRESs.size()+","+this.m_aRESs.size()+","+this.m_aLINs.size()+"/"
			+ this.m_sUniqueRES +"/"+ this.m_sRESCompressWithRepeat + "/" + this.m_sLIN;

	}

}