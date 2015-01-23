package org.eurocarbdb.MolecularFramework.io.WURCS;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class GlycoCTtoWURCSfromFilebyID extends GlycoCTtoWURCSfromFile {

	public static void main(String[] args) {
		SugarImporter t_objImporterGlycoCT = new SugarImporterGlycoCTCondensed();
		SugarExporterWURCS t_objExporterWURCS = new SugarExporterWURCS();

		String t_strCode = "";

		String t_strFilePath = "C:\\GlycoCTList\\";

		// for GlycomeDB
		String t_strFileNameR = t_strFilePath + "GlycomeDB_GlycoCTList_uniqueID_20140613112537.txt";
		String charSet = "utf-8";
		String ID ="";
		try{
			String line = null;
			BufferedReader br = openTextFileR( t_strFileNameR, charSet );

			t_strCode = "";
			while ( (line = br.readLine())!=null ) {
//				System.out.println(line);
				Matcher mat = Pattern.compile("^GlycomeDB_ID:(.*)$").matcher(line);
				if(mat.find()) {
					ID = mat.group(1);
//					System.out.printf("%s\n", ID);
					continue;
				}

				if ( line.length() == 0 ) {
					if ( t_strCode == "" ) continue;

					if ( ID.equals("15606") ) break;
					t_strCode = "";
					continue;
				}
				t_strCode += line + "\n";
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace(System.err);
		}

		try {
			Sugar gx = t_objImporterGlycoCT.parse(t_strCode);
			gx = normalizeSugar(gx);
			t_objExporterWURCS.start(gx);
//			System.out.println( ID + "\t" + t_objExporterWURCS.getWURCS() );
			String t_strWURCS = t_objExporterWURCS.getWURCS();
			String t_strWURCSCompress = t_objExporterWURCS.getWURCSCompress();
			System.out.println( ID + "\t" + t_strWURCS );
			System.out.println( "Length: " + t_strWURCS.length() );
			System.out.println( ID + "\t" + t_strWURCSCompress );
			System.out.println( "Compressed: " + t_strWURCSCompress.length() );

			String t_strWURCSURL = URLEncoder.encode(t_strWURCS, "UTF-8");
			System.out.println( ID + "\t" + t_strWURCSURL );
			System.out.println( "URLLength: " + t_strWURCSURL.length() );
			String t_strWURCSCompressURL = URLEncoder.encode(t_strWURCSCompress, "UTF-8");
			System.out.println( ID + "\t" + t_strWURCSCompressURL );
			System.out.println( "Compressed: " + t_strWURCSCompressURL.length() );

		} catch ( SugarImporterException e ) {
			String message = "There is an error in GlycoCT";
			if ( e.getErrorText() != null ) {
				message = e.getErrorText();
			}
			System.out.println(message);
			System.out.println( ID + "\t" + message);
		} catch ( GlycoVisitorException e ) {
			System.out.println(e.getErrorMessage());
			System.out.println( ID + "\t" + e.getErrorMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

}
