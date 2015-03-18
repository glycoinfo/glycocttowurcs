package org.eurocarbdb.MolecularFramework.io.WURCS;

import java.io.IOException;

import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCT;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.Linucs.SugarImporterLinucs;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.similiarity.SearchEngine.SearchEngineException;
import org.eurocarbdb.MolecularFramework.util.validation.GlycoVisitorValidation;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.jdom.JDOMException;

public class example {
	public static void main(String[] args) throws SugarImporterException, GlycoVisitorException, JDOMException, IOException, ResourcesDbException, SearchEngineException {
		// TODO Auto-generated method stub
		SugarImporter t_objImporter = new SugarImporterLinucs();
		Config t_objConf = new Config();
		//g1 [][D-GLCNAC]{[(4+1)][B-D-GLCPNAC]{[(4+1)][B-D-MANP]{[(3+1)][A-D-MANP]{}[(6+1)][A-D-MANP]{}}}}
		String t_strCode = "[][b-D-GLCNAC]{[(4+1)][B-D-GLCPNAC]{[(4+1)][B-D-MANP]{[(3+1)][A-D-MANP]{}[(6+1)][A-D-MANP]{}}}}";
		MonosaccharideConverter t_objTrans = new MonosaccharideConverter(t_objConf);
		Sugar g1 = t_objImporter.parse(t_strCode);
		GlycoVisitorToGlycoCT t_objTo = new GlycoVisitorToGlycoCT(t_objTrans);
		t_objTo.start(g1);
		g1 = t_objTo.getNormalizedSugar();

		SugarExporterGlycoCT t_exporter = new SugarExporterGlycoCT();
		t_exporter.start(g1);
		try {
			System.out.println(t_exporter.getXMLCode());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SugarExporterGlycoCTCondensed t_exporter2 = new SugarExporterGlycoCTCondensed();
		t_exporter2.start(g1);
		System.out.println(t_exporter2.getHashCode());

		SugarExporterWURCS t_exporter3 = new SugarExporterWURCS();
		try {
			t_exporter3.start(g1);
		} catch ( GlycoVisitorException e ) {
			System.out.println(e.getErrorMessage());
		}
		System.out.println( t_exporter3.getWURCS() );

		System.out.println();

		SugarImporter t_objImporterGlycoCT = new SugarImporterGlycoCTCondensed();

		t_strCode = "RES\n1b:o-lrib-PEN-0:0|1:keto|5:keto\n2b:o-lrib-PEN-0:0|1:keto|5:keto";
		Sugar g2 = t_objImporterGlycoCT.parse(t_strCode);
		try {
			t_exporter3.start(g2);
		} catch ( GlycoVisitorException e ) {
			System.out.println(e.getErrorMessage());
		}
		System.out.println( t_exporter3.getWURCS() );
		System.out.println();

		t_strCode
			= "RES\n"
			+ "1b:b-dglc-HEX-1:5\n"
			+ "2s:methyl\n"
			+ "3s:sulfate\n"
			+ "4b:b-dgro-HEX-1:5|2:d|3:d|4:d|6:a\n"
			+ "5b:a-dglc-HEX-1:5\n"
			+ "6b:o-dman-HEX-0:0|1:aldi\n"
			+ "7b:x-dgro-dgal-NON-2:6|2:keto|1:a|3:d\n"
			+ "8s:n-acetyl\n"
			+ "9b:b-dara-HEX-2:5|2:keto\n"
			+ "10b:o-dgro-TRI-0:0|1:aldi\n"
			+ "11b:b-dxyl-HEX-1:5|1:keto|4:keto|6:d\n"
			+ "12b:b-drib-HEX-1:5|2:d|6:d\n"
			+ "13b:b-dglc-HEX-1:5\n"
			+ "14s:n-acetyl\n"
			+ "15s:phospho-ethanolamine\n"
			+ "16b:b-HEX-x:x\n"
			+ "17b:b-SUG-x:x\n"
			+ "18b:o-SUG-0:0\n"
			+ "19b:a-dery-HEX-1:5|2,3:enx\n"
			+ "20b:a-dman-OCT-x:x|1:a|2:keto|3:d\n"
			+ "LIN\n"
			+ "1:1o(2+1)2n\n"
			+ "2:1o(3+1)3n\n"
			+ "3:1o(4+1)4d\n"
			+ "4:4o(6+1)5d\n"
			+ "5:5o(4+1)6d\n"
			+ "6:6o(6+2)7d\n"
			+ "7:7d(5+1)8n\n"
			+ "8:7o(8+2)9d\n"
			+ "9:9o(4+1)10d\n"
			+ "10:10o(3+1)11d\n"
			+ "11:11o(3+1)12d\n"
			+ "12:12o(4+1)13d\n"
			+ "13:13d(2+1)14n\n"
			+ "14:13o(6+1)15n\n"
			+ "15:15n(1+1)16o\n"
			+ "16:16o(4+1)17d\n"
			+ "17:17o(-1+1)18d\n"
			+ "18:18o(-1+1)19d\n"
			+ "19:19o(4+2)20d\n"
		;
		System.out.println( t_strCode );

		g2 = t_objImporterGlycoCT.parse(t_strCode);
		// GlycoCT must be validated by GlycoVisitorValidation
		GlycoVisitorValidation validation = new GlycoVisitorValidation();
		validation.start(g2);
		if ( validation.getErrors().size() > 0 ) {
			System.out.println("Errors:");
			for ( String err : validation.getErrors() ) {
				System.out.println( err );
			}
			System.out.println("Warnings:");
			for ( String warn : validation.getWarnings() ) {
				System.out.println( warn );
			}
			System.out.println();
		}

		try {
			t_exporter3.start(g2);
		} catch ( GlycoVisitorException e ) {
			System.out.println(e.getErrorMessage());
		}
		System.out.println( t_exporter3.getWURCS() );
		System.out.println( t_exporter3.getWURCSCompress() );

		t_strCode
		= "RES\n"
		+ "1r:r1\n"
		+ "REP\n"
		+ "REP1:2o(4+1)2d=-1--1\n"
		+ "RES\n"
		+ "2b:b-dgal-HEX-1:5|6:a\n"
		+ "UND\n"
		+ "UND1:-1.0:-1.0\n"
		+ "ParentIDs:2\n"
		+ "SubtreeLinkageID1:o(2|3+1)n\n"
		+ "RES\n"
		+ "3s:acetyl\n"
		+ "UND2:-1.0:-1.0\n"
		+ "ParentIDs:2\n"
		+ "SubtreeLinkageID1:o(6+1)n\n"
		+ "RES\n"
		+ "4s:methyl\n"
		;
/*
		t_strCode
		= "RES\n"
		+ "1b:b-dglc-HEX-x:x\n"
		+ "2s:n-acetyl\n"
		+ "3b:b-dglc-HEX-1:5\n"
		+ "4s:n-acetyl\n"
		+ "5b:b-dman-HEX-1:5\n"
		+ "6b:a-dman-HEX-1:5\n"
		+ "7b:a-dman-HEX-1:5\n"
		+ "LIN\n"
		+ "1:1d(2+1)2n\n"
		+ "2:1o(4+1)3d\n"
		+ "3:3d(2+1)4n\n"
		+ "4:3o(4+1)5d\n"
		+ "5:5o(3+1)6d\n"
		+ "6:5o(6+1)7d\n"
		+ "UND\n"
		+ "UND1:100.0:100.0\n"
		+ "ParentIDs:6|7\n"
		+ "SubtreeLinkageID1:o(2+1)d\n"
		+ "RES\n"
		+ "8b:b-dglc-HEX-1:5\n"
		+ "9s:n-acetyl\n"
		+ "10b:b-dgal-HEX-1:5\n"
		+ "11b:a-dgro-dgal-NON-2:6|1:a|2:keto|3:d\n"
		+ "12s:n-acetyl\n"
		+ "LIN\n"
		+ "7:8d(2+1)9n\n"
		+ "8:8o(3|4+1)10d\n"
		+ "9:10o(4+2)11d\n"
		+ "10:11d(5+1)12n\n"
		+ "UND2:100.0:100.0\n"
		+ "ParentIDs:6|7\n"
		+ "SubtreeLinkageID1:o(2+1)d\n"
		+ "RES\n"
		+ "13b:b-dglc-HEX-1:5\n"
		+ "14s:n-acetyl\n"
		+ "15b:b-dgal-HEX-1:5\n"
		+ "16b:a-dgro-dgal-NON-2:6|1:a|2:keto|3:d\n"
		+ "17s:n-acetyl\n"
		+ "LIN\n"
		+ "11:13d(2+1)14n\n"
		+ "12:13o(3|4+1)15d\n"
		+ "13:15o(4+2)16d\n"
		+ "14:16d(5+1)17n\n"
		+ "UND3:100.0:100.0\n"
		+ "ParentIDs:6|7\n"
		+ "SubtreeLinkageID1:o(4|6+1)d\n"
		+ "RES\n"
		+ "18b:b-dglc-HEX-1:5\n"
		+ "19s:n-acetyl\n"
		+ "20b:b-dgal-HEX-1:5\n"
		+ "21b:a-dgro-dgal-NON-2:6|1:a|2:keto|3:d\n"
		+ "22s:n-acetyl\n"
		+ "LIN\n"
		+ "15:18d(2+1)19n\n"
		+ "16:18o(3|4+1)20d\n"
		+ "17:20o(4+2)21d\n"
		+ "18:21d(5+1)22n\n"
		+ "UND4:100.0:100.0\n"
		+ "ParentIDs:1|3|5\n"
		+ "SubtreeLinkageID1:o(-1+2)d\n"
		+ "RES\n"
		+ "23b:a-dgro-dgal-NON-2:6|1:a|2:keto|3:d\n"
		+ "24s:n-acetyl\n"
		+ "LIN\n"
		+ "19:23d(5+1)24n\n"
	;
	*/
	System.out.println( t_strCode );

	try {
		g2 = t_objImporterGlycoCT.parse(t_strCode);
	} catch ( SugarImporterException e ) {
		System.out.println(e.getErrorText());
	}
	// GlycoCT must be validated by GlycoVisitorValidation
	validation.start(g2);
	if ( validation.getErrors().size() > 0 ) {
		System.out.println("Errors:");
		for ( String err : validation.getErrors() ) {
			System.out.println( err );
		}
		System.out.println("Warnings:");
		for ( String warn : validation.getWarnings() ) {
			System.out.println( warn );
		}
		System.out.println();
	}

	try {
		t_exporter3.start(g2);
	} catch ( GlycoVisitorException e ) {
		System.out.println(e.getErrorMessage());
	}
	System.out.println( t_exporter3.getWURCS() );
	System.out.println( t_exporter3.getWURCSCompress() );

	t_strCode
	= "RES\n"
	+ "1b:x-dglc-HEX-1:5\n"
	+ "2s:n-acetyl\n"
	+ "3b:b-dglc-HEX-1:5\n"
	+ "4s:n-acetyl\n"
	+ "5b:b-dman-HEX-1:5\n"
	+ "6b:a-dman-HEX-1:5\n"
	+ "7b:x-HEX-x:x\n"
	+ "8s:n-acetyl\n"
	+ "9b:b-dgal-HEX-1:5\n"
	+ "10b:a-dgro-dgal-NON-2:6|1:a|2:keto|3:d\n"
	+ "11s:n-acetyl\n"
	+ "12b:a-dgro-dgal-NON-2:6|1:a|2:keto|3:d\n"
	+ "13s:n-acetyl\n"
	+ "14b:a-dman-HEX-1:5\n"
	+ "15b:x-HEX-x:x\n"
	+ "16s:n-acetyl\n"
	+ "17b:b-dgal-HEX-1:5\n"
	+ "18b:a-dgro-dgal-NON-2:6|1:a|2:keto|3:d\n"
	+ "19s:n-acetyl\n"
	+ "LIN\n"
	+ "1:1d(2+1)2n\n"
	+ "2:1o(4+1)3d\n"
	+ "3:3d(2+1)4n\n"
	+ "4:3o(4+1)5d\n"
	+ "5:5o(-1+1)6d\n"
	+ "6:6o(-1+1)7d\n"
	+ "7:7d(2+1)8n\n"
	+ "8:7o(4+1)9d\n"
	+ "9:9o(3+2)10d\n"
	+ "10:10d(5+1)11n\n"
	+ "11:10o(8+2)12d\n"
	+ "12:12d(5+1)13n\n"
	+ "13:5o(-1+1)14d\n"
	+ "14:14o(-1+1)15d\n"
	+ "15:15d(2+1)16n\n"
	+ "16:15o(4+1)17d\n"
	+ "17:17o(3+2)18d\n"
	+ "18:18d(5+1)19n\n"
	+ "UND\n"
	+ "UND1:100.0:100.0\n"
	+ "ParentIDs:1|3|5|6|7|9|10|12|14|15|17|18\n"
	+ "SubtreeLinkageID1:o(-1+1)n\n"
	+ "RES\n"
	+ "20s:acetyl\n"
	+ "UND2:100.0:100.0\n"
	+ "ParentIDs:1|3|5|6|7|9|10|12|14|15|17|18\n"
	+ "SubtreeLinkageID1:o(-1+1)n\n"
	+ "RES\n"
	+ "21s:acetyl\n"
	+ "UND3:100.0:100.0\n"
	+ "ParentIDs:1|3|5|6|7|9|10|12|14|15|17|18\n"
	+ "SubtreeLinkageID1:o(-1+1)n\n"
	+ "RES\n"
	+ "22s:acetyl";
	
	System.out.println( t_strCode );

	try {
		g2 = t_objImporterGlycoCT.parse(t_strCode);
	} catch ( SugarImporterException e ) {
		System.out.println(e.getErrorText());
	}
	// GlycoCT must be validated by GlycoVisitorValidation
	validation.start(g2);
	if ( validation.getErrors().size() > 0 ) {
		System.out.println("Errors:");
		for ( String err : validation.getErrors() ) {
			System.out.println( err );
		}
		System.out.println("Warnings:");
		for ( String warn : validation.getWarnings() ) {
			System.out.println( warn );
		}
		System.out.println();
	}

	try {
		t_exporter3.start(g2);
	} catch ( GlycoVisitorException e ) {
		System.out.println(e.getErrorMessage());
	}
	System.out.println( t_exporter3.getWURCS() );
	System.out.println( t_exporter3.getWURCSCompress() );

	}

}
