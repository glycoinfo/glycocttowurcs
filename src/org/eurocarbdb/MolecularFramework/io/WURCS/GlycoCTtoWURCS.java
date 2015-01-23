package org.eurocarbdb.MolecularFramework.io.WURCS;

import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;

public class GlycoCTtoWURCS {

	public static void main(String[] args) {
		SugarImporter t_objImporterGlycoCT = new SugarImporterGlycoCTCondensed();
		SugarExporterWURCS t_objExporterWURCS = new SugarExporterWURCS();

		String t_strCode = "";
		for ( int i = 0; i < args.length; i++ ) {
			String[] arg = args[i].split(" ");
			if ( arg.length > 1 ) {
				for ( String str : arg) {
					t_strCode += str + "\n";
				}
			} else {
				t_strCode += args[i] + "\n";
			}
		}
//		System.out.println(t_strCode );

		try {
			Sugar gx = t_objImporterGlycoCT.parse(t_strCode);
			gx = normalizeSugar(gx);
			t_objExporterWURCS.start(gx);
//			System.out.println( ID + "\t" + t_objExporterWURCS.getWURCS() );
			String t_strWURCS = t_objExporterWURCS.getWURCSCompress();
			System.out.println( t_strWURCS );

		} catch ( SugarImporterException e ) {
			String message = "There is an error in GlycoCT";
			if ( e.getErrorText() != null ) {
				message = e.getErrorText();
			}
			System.out.println(message);
			e.printStackTrace();
		} catch ( GlycoVisitorException e ) {
			System.out.println(e.getErrorMessage());
			e.printStackTrace();
		}

	}

	public static Sugar normalizeSugar(Sugar t_objSugar) throws GlycoVisitorException {
		GlycoVisitorToGlycoCT t_objTo
			= new GlycoVisitorToGlycoCT( new MonosaccharideConverter( new Config() ) );
		t_objTo.start(t_objSugar);
		return t_objTo.getNormalizedSugar();
	}

}
