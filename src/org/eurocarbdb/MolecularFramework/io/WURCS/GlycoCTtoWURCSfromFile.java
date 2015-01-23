package org.eurocarbdb.MolecularFramework.io.WURCS;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.validation.GlycoVisitorValidation;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;

public class GlycoCTtoWURCSfromFile {

	private static SugarImporter m_objImporterGlycoCT = new SugarImporterGlycoCTCondensed();
	private static SugarExporterWURCS m_objExporterWURCS = new SugarExporterWURCS();

	public static void main(String[] args) {

		String t_strCode = "";

		LinkedHashMap<String, String> t_hashGlycoCTList = new LinkedHashMap<String, String>();

		String t_strFilePath = "C:\\GlycoCTList\\";

		// mkdir for result
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String t_strDate = sdf.format(new Date());
		String t_strResultPath = t_strFilePath + t_strDate;
		File resultDir = new File(t_strResultPath);
		if ( ! resultDir.exists() ) resultDir.mkdirs();

		// for GlycomeDB
		String t_strFileNameR = t_strFilePath + "GlycomeDB_GlycoCTList_uniqueID_20140613112537.txt";
		String t_strFileNameW = t_strResultPath + "\\" + sdf.format(new Date()) + "result-GlycomeDB_GlycoCTmfWURCS.txt";
		String charSet = "utf-8";
		boolean append = false;
		boolean autoFlush = true;
		try{
			String line = null;
			BufferedReader br = openTextFileR( t_strFileNameR, charSet );
			PrintWriter pw = openTextFileW(t_strFileNameW, charSet, append, autoFlush);

			t_strCode = "";
			String ID ="";
			int count = 0;
			while ( (line = br.readLine())!=null ) {
//				System.out.println(line);
				Matcher mat = Pattern.compile("^(.*)ID:(.*)$").matcher(line);
				if(mat.find()) {
					ID = mat.group(2);
//					System.out.printf("%s\n", ID);
					count++;
					continue;
				}

				if ( line.length() == 0 ) {
					if ( t_strCode == "" ) continue;

					t_hashGlycoCTList.put(ID, t_strCode);
					t_strCode = "";
//					if (count == 100) break;
					continue;
				}
				t_strCode += line + "\n";
			}
			br.close();

			GlycoCTListToWURCS(t_hashGlycoCTList, pw);

			pw.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace(System.err);
		}

		System.out.println();
		t_hashGlycoCTList.clear();

		// for UnicarbDB
		String t_strDirName = t_strFilePath + "Matthew_UniCarbKB_20140425_IY";
		t_strFileNameW = t_strResultPath + "\\" + sdf.format(new Date()) + "result-UniCarbDB_GlycoCTmfWURCS.txt";
		try {
			File dir = new File(t_strDirName);
			File[] files = dir.listFiles();
			ArrayList<String> t_aFileNames = new ArrayList<String>();
			for ( File file : files ) {
				t_aFileNames.add( file.getName() );
			}
			int nFile = files.length;

			PrintWriter pw = openTextFileW(t_strFileNameW, charSet, append, autoFlush);

			String line = null;

			Integer id=0;
			int count = 0;
			while ( count < nFile ) {
				id++;
				if (! t_aFileNames.contains( id.toString() + ".txt" ) )
					continue;
//				System.out.println(id);
				count++;

				t_strCode = "";
				t_strFileNameR = t_strDirName +"\\"+ id.toString() + ".txt";
				BufferedReader br = openTextFileR( t_strFileNameR, charSet );
				while ( (line = br.readLine())!=null ) {
//					System.out.println(line);

					if ( line.length() == 0 ) continue;
					t_strCode += line+"\n";
				}
				br.close();
				t_hashGlycoCTList.put(id.toString(), t_strCode);
			}

			GlycoCTListToWURCS(t_hashGlycoCTList, pw);

			pw.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace(System.err);
		}

	}

	/** Open text file for read only */
	public static BufferedReader  openTextFileR( String fileName, String charSet ) throws Exception {
		return new BufferedReader( new InputStreamReader( skipUTF8BOM( new FileInputStream( new File(fileName) ), charSet), charSet) );
	}

	/** Open text file for write */
	public static PrintWriter openTextFileW( String fileName, String charSet, boolean append, boolean autoFlush ) throws Exception {
		return new PrintWriter( new BufferedWriter( new OutputStreamWriter( new FileOutputStream( new File(fileName), append ), charSet ) ), autoFlush );

	}

	/** Skip BOM of UTF-8 */
	public static InputStream skipUTF8BOM( InputStream is, String charSet ) throws Exception{
		if( !charSet.toUpperCase().equals("UTF-8") ) return is;
		if( !is.markSupported() ){
			// if no mark supported, use BufferedInputStream
			is= new BufferedInputStream(is);
		}
		is.mark(3); // set mark to head
		if( is.available()>=3 ){
			byte b[]={0,0,0};
			is.read(b,0,3);
			if( b[0]!=(byte)0xEF ||
					b[1]!=(byte)0xBB ||
					b[2]!=(byte)0xBF ){
				is.reset();// if not BOM, roll back to head
			}
		}
		return is;
	}

	/** Nomarize and validation sugar */
	public static Sugar normalizeSugar(Sugar t_objSugar) throws GlycoVisitorException {

		GlycoVisitorValidation t_validation = new GlycoVisitorValidation();
		t_validation.start(t_objSugar);
		if ( t_validation.getErrors().size() > 0 ) {
			System.out.println("Errors:");
			for ( String err : t_validation.getErrors() ) {
				System.out.println( err );
			}
			System.out.println("Warnings:");
			for ( String warn : t_validation.getWarnings() ) {
				System.out.println( warn );
			}
			System.out.println();
			throw new GlycoVisitorException("Error in GlycoCT validation.");
		}

		GlycoVisitorToGlycoCT t_objTo
			= new GlycoVisitorToGlycoCT( new MonosaccharideConverter( new Config() ) );
		t_objTo.start(t_objSugar);
		return t_objTo.getNormalizedSugar();
	}

	/** Convert and output WURCS from GlycoCT list */
	public static void GlycoCTListToWURCS(LinkedHashMap<String, String> t_hashGlycoCTList, PrintWriter pw) {
		LinkedHashMap<String, LinkedList<String>> hashCountDuplicate = new LinkedHashMap<String, LinkedList<String>>();
		LinkedList<String> undefALINIDs = new LinkedList<String>();
		HashMap<String, LinkedList<String>> hashErrors = new HashMap<String, LinkedList<String>>();
		int success = 0;
		Integer id = 0;
		int count = 0;
		for ( String ID : t_hashGlycoCTList.keySet() ) {
//		while ( count < t_hashGlycoCTList.size() ) {
//			id++;
//			if (! t_hashGlycoCTList.containsKey(id.toString()) )
//				continue;
//			System.out.println(id);
			count++;

			String t_strCode = t_hashGlycoCTList.get(ID);
//			System.out.println(t_strCode);
			String message = "";
			try {
				Sugar gx = m_objImporterGlycoCT.parse(t_strCode);
				gx = normalizeSugar(gx);
				m_objExporterWURCS.start(gx);
//				System.out.println( ID + "\t" + t_objExporterWURCS.getWURCS() );
				String t_strWURCS = m_objExporterWURCS.getWURCSCompress();
				if( t_strWURCS.contains("WURCS") )
					success++;
				pw.println( ID + "\t" + t_strWURCS );
				if( t_strWURCS.contains("*?*") ) {
					message = "Contain undefined substituent.";
				}
				if ( !hashCountDuplicate.containsKey(t_strWURCS) )
					hashCountDuplicate.put(t_strWURCS, new LinkedList<String>() );
				hashCountDuplicate.get(t_strWURCS).add(ID);

			} catch ( SugarImporterException e ) {
				message = "There is an error in GlycoCT";
				if ( e.getErrorText() != null ) {
					message = e.getErrorText();
				}
			} catch ( GlycoVisitorException e ) {
				message = e.getErrorMessage();
			}
			if ( message != "" ) {
				System.out.println(message);
				pw.println( ID + "\t" + message);
				if ( !hashErrors.containsKey(message) )
					hashErrors.put(message, new LinkedList<String>() );
				hashErrors.get(message).add(ID);
			}

		}

		pw.println( "\nTotal CT: " + count );
		pw.println( "Successful conversion: " + (success-undefALINIDs.size()) );
		if ( !hashErrors.isEmpty() ) {
			pw.println( "Errors: " );
			LinkedList<String> errors = new LinkedList<String>();
			for ( String err : hashErrors.keySet() ) errors.add(err);
			Collections.sort(errors);
			for ( String err : errors ) {
//				pw.println( "-" + err + ": " + hashErrors.get(err).size() );
				String errIDs = "";
				int errCount = 0;
				for ( String errID : hashErrors.get(err) ) {
					errIDs += errID;
					errCount++;
					if ( errCount == hashErrors.get(err).size() ) continue;
					errIDs += ",";
					if ( errCount % 20 == 0 )
						errIDs += "\n";
				}
				pw.println(errIDs+"\t"+err+" : "+errCount);
			}
		}
		LinkedList<String> duplicateWURCS = new LinkedList<String>();
		for ( String strWURCS : hashCountDuplicate.keySet() ) {
			if ( hashCountDuplicate.get(strWURCS).size() == 1 ) continue;
			duplicateWURCS.add(strWURCS);
		}
		pw.println( "Duplicated structures: " + duplicateWURCS.size() );
		for ( String strWURCS : duplicateWURCS ) {
			String dupIDs = "";
			for ( String dupID : hashCountDuplicate.get(strWURCS) ) {
				if ( dupIDs != "" ) dupIDs += ",";
				dupIDs += dupID;
			}
			pw.println(dupIDs+"\t"+strWURCS);
		}
	}
}
