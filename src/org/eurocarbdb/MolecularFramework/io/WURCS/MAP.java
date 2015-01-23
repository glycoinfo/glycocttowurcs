package org.eurocarbdb.MolecularFramework.io.WURCS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;

/**
 * Class for WURCS MAP conversion from Substituent and GlycoEdges
 * @author MasaakiMatsubara
 *
 */
public class MAP {
	private GlycoEdge m_objParentEdge = null;
	private GlycoEdge m_objChildEdge = null;

	private SubstituentTypeToMAP m_objToMAP = null;

	public MAP( Substituent a_objSubstituent ) {
		this.m_objParentEdge = a_objSubstituent.getParentEdge();
//		this.m_objChildEdge = a_objSubstituent.getChildEdges().get(0);
		this.m_objToMAP = SubstituentTypeToMAP.forName( a_objSubstituent.getSubstituentType().getName() );
	}

	public MAP( Substituent a_objSubstituent, GlycoEdge a_objParentEdge, GlycoEdge a_objChildEdge) {
		this.m_objParentEdge = a_objParentEdge;
		this.m_objChildEdge = a_objChildEdge;
		this.m_objToMAP = SubstituentTypeToMAP.forName( a_objSubstituent.getSubstituentType().getName() );
	}

	public Boolean isSwappedCarbonPosition() {
		return this.m_objToMAP.isSwapCarbonPositions();
	}

	public String getCode() {
		/// make MAP code

		String t_strMAP = "";
		String t_strParentPos = "";
		String t_strChildPos = "";
		String t_strParentTypes = "";
		String t_strChildTypes = "";

		if ( this.m_objChildEdge == null ) {
			t_strMAP = this.m_objToMAP.getMAPforBMU();
		} else {
			t_strMAP = this.m_objToMAP.getMAPforMLU();
		}
		if ( t_strMAP == null) return t_strMAP;

		ArrayList<Linkage> t_aParentLink = this.m_objParentEdge.getGlycosidicLinkages();
		String subName = this.m_objToMAP.getName();
		if ( subName == "anhydro" || subName == "epoxy" || subName == "lactone" ) {
			int parentPos = t_aParentLink.get(0).getParentLinkages().get(0);
			int childPos = t_aParentLink.get(1).getParentLinkages().get(0);
			// Swap position numbers if parent position > child position
			if ( parentPos > childPos ) {
				int tmp = parentPos;
				parentPos = childPos;
				childPos = tmp;
			}
			t_strMAP += parentPos + "-" + childPos;
			return t_strMAP;
		}

		for (Linkage t_objLink : t_aParentLink) {
			t_strParentTypes += t_objLink.getParentLinkageType().getType();
			String t_strLinks = "";
			for (Integer pos : t_objLink.getParentLinkages() ) {
//				if ( t_strLinks != "" ) t_strLinks += "\\";
				if ( t_strLinks != "" ) t_strLinks += "|";
				t_strLinks += (pos==-1)? "?" : pos;
			}
			// For position in MAP
			Boolean isSwap = this.m_objToMAP.isSwapCarbonPositions();
			if ( isSwap != null ) {
				if(t_strParentPos != "") t_strParentPos += "u";
				if(t_strParentTypes.length() > 1) t_strLinks += "d";
			}
			if(t_strParentPos != "") t_strParentPos += ",";
			t_strParentPos += t_strLinks;
		}

		if ( t_strMAP.equals("?*") ) {
			return t_strParentPos + "*" + t_strMAP;
		}
		// If parent linkage type is 'o', inclement position number in MAP and add "O" to head
		if ( t_strParentTypes.length() > 1 ) {
			if ( subName == "phosphate" ) {
				t_strMAP = SubstituentTypeToMAP.forName(subName).getMAPforMLU();
			}
			if (t_strParentTypes.charAt(1) == 'o' ) {
				t_strMAP = this.addOxygenToTail(t_strMAP);
			}
		}
//		System.out.println(t_strMAP);
		if (t_strParentTypes.charAt(0) == 'o' ) {
			t_strMAP = this.addOxygenToHead(t_strMAP);
		}
		// for BMU
		if ( this.m_objChildEdge == null ) {
			return t_strParentPos + "*" + t_strMAP;
		}

		ArrayList<Linkage> t_aChildLink = this.m_objChildEdge.getGlycosidicLinkages();
		for (Linkage t_objLink : t_aChildLink) {
			t_strChildTypes += t_objLink.getChildLinkageType().getType();
			String t_strLinks = "";
			for (Integer pos : t_objLink.getChildLinkages() ) {
//				if ( t_strLinks != "" ) t_strLinks += "\\";
				if ( t_strLinks != "" ) t_strLinks += "|";
				t_strLinks += pos;
			}
			if(t_strChildPos != "") t_strChildPos += "-";
			t_strChildPos += t_strLinks;
		}


		// if child linkage type is 'o', inclement position number in MAP and add "O" to tail
		if (t_strChildTypes.charAt(0) == 'o') {
			t_strMAP = this.addOxygenToTail(t_strMAP);
		}

		return "*" + t_strMAP;
	}

	private String addOxygenToHead(String a_strMAP) {
		// Collect position numbers
		ArrayList<Integer> nums = new ArrayList<Integer>();
		String strnum = "";
		for (int i=0; i < a_strMAP.length(); i++) {
			char ch = a_strMAP.charAt(i);
			if ( Character.isDigit(ch) ) {
				strnum += ch;
				continue;
			}
			if ( strnum.equals("") ) continue;
			if ( nums.contains( Integer.parseInt(strnum) )) continue;
			nums.add( Integer.parseInt(strnum) );
			strnum = "";
		}
		Collections.sort(nums);
		Collections.reverse(nums);
//		System.out.println(nums);

		// Inclement position numbers
		String newMAP = a_strMAP;
		for(Iterator<Integer> it = nums.iterator(); it.hasNext();) {
			Integer num1 = it.next();
			Integer num2 = num1+1;
			newMAP = newMAP.replaceAll(num1.toString(), num2.toString());
		}
		return "O"+newMAP;
	}

	private String addOxygenToTail(String a_strMAP) {
		// Insert "O" to MAP code before last "*"
		StringBuilder sb = new StringBuilder(a_strMAP);
		int t_iInsertPos = a_strMAP.lastIndexOf("*");
		sb.insert(t_iInsertPos, 'O');
		a_strMAP = sb.toString();

		// Count added "O" position
		int t_iPosO = 1;
		for ( int i=0; i < t_iInsertPos; i++) {
			char ch = a_strMAP.charAt(i);
			if ( ch == '^' || ch == '/') {
				i++;
				continue;
			} else if ( ch == '=' || ch == '#' ) {
				continue;
			} else if ( ch == '*' ) {
				break;
			}
			t_iPosO++;
		}
//		System.out.println(t_iPosO);

		// Collect position numbers
		ArrayList<Integer> nums = new ArrayList<Integer>();
		String strnum = "";
		for (int i=0; i < a_strMAP.length(); i++) {
			char ch = a_strMAP.charAt(i);
			if ( Character.isDigit(ch) ) {
				strnum += ch;
				continue;
			}
			if ( strnum.equals("") ) continue;
			if ( nums.contains( Integer.parseInt(strnum) )) continue;
			nums.add( Integer.parseInt(strnum) );
			strnum = "";
		}
		Collections.sort(nums);
		Collections.reverse(nums);
//		System.out.println(nums);

		// inclement position numbers which are greater than added "O" position number
		String newMAP = a_strMAP;
		for(Iterator<Integer> it = nums.iterator(); it.hasNext();) {
			Integer num1 = it.next();
			if (num1 <= t_iPosO) continue;
			Integer num2 = num1+1;
			newMAP = newMAP.replaceAll(num1.toString(), num2.toString());
		}
		return newMAP;
	}

}
