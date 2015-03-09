package org.eurocarbdb.MolecularFramework.io.WURCS;

import java.util.ArrayList;

import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.validation.GlycoVisitorValidation;
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

		StringBuffer t_strLog = new StringBuffer("");
		try {
			Sugar gx = t_objImporterGlycoCT.parse(t_strCode);
			gx = normalizeSugar(gx, t_strLog);
			t_objExporterWURCS.start(gx);
//			System.out.println( ID + "\t" + t_objExporterWURCS.getWURCS() );
//			String t_strWURCS = t_objExporterWURCS.getWURCSCompressWithRepeat();
			String t_strWURCS = t_objExporterWURCS.getWURCSCompress();
			System.out.println( t_strWURCS );

		} catch ( SugarImporterException e ) {
			String message = "There is an error in importer of GlycoCT.\n";
			if ( e.getErrorText() != null ) {
				message += e.getErrorText();
			}
			System.out.println(message);
			e.printStackTrace();
		} catch ( GlycoVisitorException e ) {
			System.out.println(e.getErrorMessage());
			if ( !t_strLog.toString().equals("") )
				System.out.println( t_strLog.toString() );
			e.printStackTrace();
		}
	}

	/** Nomarize and validate sugar */
	public static Sugar normalizeSugar(Sugar a_objSugar, StringBuffer a_strLog) throws GlycoVisitorException {

		// Validate sugar
		GlycoVisitorValidation t_validation = new GlycoVisitorValidation();
		t_validation.start(a_objSugar);
		ArrayList<String> t_aErrorStrings   = t_validation.getErrors();
		ArrayList<String> t_aWarningStrings = t_validation.getWarnings();

		// Validate for WURCS
		GlycoVisitorValidationForWURCS t_validationWURCS = new GlycoVisitorValidationForWURCS();
		t_validationWURCS.start(a_objSugar);

		// Marge errors and warnings
		t_aErrorStrings.addAll( t_validationWURCS.getErrors() );
		t_aWarningStrings.addAll( t_validationWURCS.getWarnings() );
		if ( !t_aErrorStrings.isEmpty() )
			a_strLog.append("Errors:\n");
		for ( String err : t_aErrorStrings )
			a_strLog.append(err+"\n");

		if ( !t_aWarningStrings.isEmpty() )
			a_strLog.append("Warnings:\n");
		for ( String warn : t_aWarningStrings ) {
			a_strLog.append(warn+"\n");
		}
		if ( !t_aErrorStrings.isEmpty() ) {
			throw new GlycoVisitorException("Error in GlycoCT validation.");
		}

		// Normalize sugar
		GlycoVisitorToGlycoCT t_objTo
			= new GlycoVisitorToGlycoCT( new MonosaccharideConverter( new Config() ) );
		t_objTo.start(a_objSugar);
		return t_objTo.getNormalizedSugar();
	}
}
