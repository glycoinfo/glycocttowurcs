package org.eurocarbdb.MolecularFramework.io.WURCS;

import java.util.Comparator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.util.analytical.misc.GlycoVisitorCountNodeType;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorCountBranchingPoints;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorCountLongestBranch;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorCountResidueTerminal;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
 * Class of GlycoNode comparator for WURCS
 * @author MasaakiMatsubara
 *
 */
public class WURCSGlycoNodeComparator implements  Comparator<GlycoNode> {


	public int compare(GlycoNode r1, GlycoNode r2) {

		// First criterion: RESIDUE COUNT
		if (ResidueCount(r1,r2)!=0)
		{
			return (ResidueCount(r1,r2));
		}

		// Second criterion: LONGEST BRANCH
		if (LongestBranch(r1,r2)!=0){
			return (LongestBranch(r1,r2));
		}
		// Third criterion: TERMINAL RESIDUE COUNT
		if (TerminalResidue(r1,r2)!=0){
			return (TerminalResidue(r1,r2));
		}
		// Fourth criterion: BRANCHING POINTS COUNT
		if (BranchingCount(r1,r2)!=0){
			return (BranchingCount(r1,r2));
		}
		//Last criterion: ALPHANUM SORT OF REMAINING GLYCO - CT
		if (AlphaNum(r1,r2)!=0){
			return (AlphaNum(r1,r2));
		}

		// Not able to discrimate subgraphs
		return 0;
	}

	private int BranchingCount(GlycoNode r1, GlycoNode r2) {
		int t_g1Count=0;
		int t_g2Count=0;

		GlycoVisitorCountBranchingPoints t_oCounter = new  GlycoVisitorCountBranchingPoints();
		try {
			t_oCounter.start(r1);
			t_g1Count = t_oCounter.getBranchingPointsCountResidue();
			t_oCounter.clear();
			t_oCounter.start(r2);
			t_g2Count = t_oCounter.getBranchingPointsCountResidue();

			if (t_g1Count < t_g2Count){
				return 1;
			}
			else if (t_g1Count > t_g2Count){
				return -1;
			}
		} catch (GlycoVisitorException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int TerminalResidue(GlycoNode r1, GlycoNode r2) {
		int t_g1Count=0;
		int t_g2Count=0;

		GlycoVisitorCountResidueTerminal t_oCounter = new  GlycoVisitorCountResidueTerminal();
		try {
			t_oCounter.start(r1);
			t_g1Count = t_oCounter.getTerminalCountResidue();

			t_oCounter.start(r2);
			t_g2Count = t_oCounter.getTerminalCountResidue();

			if (t_g1Count < t_g2Count){
				return 1;
			}
			else if (t_g1Count > t_g2Count){
				return -1;
			}
		} catch (GlycoVisitorException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int AlphaNum(GlycoNode r1, GlycoNode r2) {
		String t_g1StringBMU="";
		String t_g2StringBMU="";

//		System.out.println("Compare BMU");
		SugarExporterWURCS t_objWURCSExporter = new SugarExporterWURCS();
		try {
			t_objWURCSExporter.start(r1);
			t_g1StringBMU=t_objWURCSExporter.getRESs();
		} catch (GlycoVisitorException e) {
			e.printStackTrace();
		}
		t_objWURCSExporter.clear();
		try {
			t_objWURCSExporter.start(r2);
			t_g2StringBMU=t_objWURCSExporter.getRESs();
		} catch (GlycoVisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("1:"+t_g1StringBMU);
//		System.out.println("2:"+t_g2StringBMU);

		return (t_g2StringBMU.compareTo(t_g1StringBMU) * -1);
	}

	private int LongestBranch (GlycoNode r1, GlycoNode r2) {
		int t_g1Count=0;
		int t_g2Count=0;

		GlycoVisitorCountLongestBranch t_oCounter = new  GlycoVisitorCountLongestBranch();
		try {
			t_oCounter.start(r1);
			t_g1Count = t_oCounter.getLongestBranchResidue();

			t_oCounter.start(r2);
			t_g2Count = t_oCounter.getLongestBranchResidue();

			if (t_g1Count < t_g2Count){
				return 1;
			}
			else if (t_g1Count > t_g2Count){
				return -1;
			}
		} catch (GlycoVisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}
	private int ResidueCount (GlycoNode r1, GlycoNode r2)
	{
		int t_g1Count=0;
		int t_g2Count=0;

		GlycoVisitorCountNodeType t_oCounter = new  GlycoVisitorCountNodeType();
		try {
			t_oCounter.start(r1);
			t_g1Count =
				t_oCounter.getMonosaccharideCount()+
				t_oCounter.getNonMonosaccharideCount()+
				t_oCounter.getSubstituentCount()+
				t_oCounter.getAlternativeNodeCount();
			t_oCounter.clear();

			t_oCounter.start(r2);
			t_g2Count = t_oCounter.getMonosaccharideCount()+
			t_oCounter.getNonMonosaccharideCount()+
			t_oCounter.getSubstituentCount()+
			t_oCounter.getAlternativeNodeCount();

			if (t_g1Count < t_g2Count){
				return 1;
			}
			else if (t_g1Count > t_g2Count){
				return -1;
			}
		} catch (GlycoVisitorException e) {
			e.printStackTrace();
		}
		return 0;

	}


}
