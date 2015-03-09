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
import java.util.TreeMap;
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

	public static void main(String[] args) {

		String t_strFilePath = "C:\\GlycoCTList\\";

		// mkdir for result
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String t_strDate = sdf.format(new Date());
		String t_strResultPath = t_strFilePath + t_strDate;
		File resultDir = new File(t_strResultPath);
		if ( ! resultDir.exists() ) resultDir.mkdirs();

		String sep = File.separator;

		String t_strFileNameR;
		String t_strFileNameW;
		String t_strFileNameLog;

		// for Glycoepitope
		t_strFileNameR   = t_strFilePath + "Glycoepitope_GlycoCTList_20150224.txt";
		t_strFileNameW   = t_strResultPath + sep + t_strDate + "result-Glycoepitope_GlycoCTmfWURCS.txt";
		t_strFileNameLog = t_strResultPath + sep + t_strDate + "result-Glycoepitope_GlycoCTmfWURCS.log";
		GlycoCTtoWURCSFromFile(t_strFileNameR, t_strFileNameW, t_strFileNameLog);

		// for GlyTouCan motif
		t_strFileNameR   = t_strFilePath + "GlyTouCan_GlycoCTListMotif_20150213.txt";
		t_strFileNameW   = t_strResultPath + sep + t_strDate + "result-GlyTouCanMotif_GlycoCTmfWURCS.txt";
		t_strFileNameLog = t_strResultPath + sep + t_strDate + "result-GlyTouCanMotif_GlycoCTmfWURCS.log";
		GlycoCTtoWURCSFromFile(t_strFileNameR, t_strFileNameW, t_strFileNameLog);

		// for GlyTouCan
		t_strFileNameR   = t_strFilePath + "glytoucanAccGlycoCTcsvToText_20150220.txt";
		t_strFileNameW   = t_strResultPath + sep + t_strDate + "result-GlyTouCan_GlycoCTmfWURCS.txt";
		t_strFileNameLog = t_strResultPath + sep + t_strDate + "result-GlyTouCan_GlycoCTmfWURCS.log";
		GlycoCTtoWURCSFromFile(t_strFileNameR, t_strFileNameW, t_strFileNameLog);

		// for GlycomeDB
		t_strFileNameR   = t_strFilePath + "GlycomeDB_GlycoCTList_uniqueID_20140613112537.txt";
		t_strFileNameW   = t_strResultPath + sep + t_strDate + "result-GlycomeDB_GlycoCTmfWURCS.txt";
		t_strFileNameLog = t_strResultPath + sep + t_strDate + "result-GlycomeDB_GlycoCTmfWURCS.log";
		GlycoCTtoWURCSFromFile(t_strFileNameR, t_strFileNameW, t_strFileNameLog);

		// for UnicarbDB
		String t_strDirName = t_strFilePath + "Matthew_UniCarbKB_20140425_IY";
		t_strFileNameW   = t_strResultPath + sep + t_strDate + "result-UniCarbDB_GlycoCTmfWURCS.txt";
		t_strFileNameLog = t_strResultPath + sep + t_strDate + "result-UniCarbDB_GlycoCTmfWURCS.log";
		GlycoCTtoWURCSFromDir(t_strDirName, t_strFileNameW, t_strFileNameLog);

	}

	/** Open text file for read only */
	public static BufferedReader  openTextFileR( String fileName ) throws Exception {
		String charSet = "utf-8";
		return new BufferedReader( new InputStreamReader( skipUTF8BOM( new FileInputStream( new File(fileName) ), charSet), charSet) );
	}

	/** Open text file for write */
	public static PrintWriter openTextFileW( String fileName ) throws Exception {
		String charSet = "utf-8";
		boolean append = false;
		boolean autoFlush = true;
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

	/** Nomarize and validate sugar */
	public static Sugar normalizeSugar(Sugar a_objSugar, StringBuffer a_strLog) throws GlycoVisitorException {

		// Validation sugar
		GlycoVisitorValidation t_validation = new GlycoVisitorValidation();
		t_validation.start(a_objSugar);
		ArrayList<String> t_aErrorStrings   = t_validation.getErrors();
		ArrayList<String> t_aWarningStrings = t_validation.getWarnings();
		GlycoVisitorValidationForWURCS t_validationWURCS = new GlycoVisitorValidationForWURCS();
		t_validationWURCS.start(a_objSugar);
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

		GlycoVisitorToGlycoCT t_objTo
			= new GlycoVisitorToGlycoCT( new MonosaccharideConverter( new Config() ) );
		t_objTo.start(a_objSugar);
		return t_objTo.getNormalizedSugar();
	}

	/** Convert and output WURCS from GlycoCT list */
	public static void GlycoCTListToWURCS(TreeMap<String, String> a_mapGlycoCTList, PrintWriter pw, PrintWriter log) {
		LinkedHashMap<String, LinkedList<String>> hashCountDuplicate = new LinkedHashMap<String, LinkedList<String>>();
		HashMap<String, LinkedList<String>>       hashErrors         = new HashMap<String, LinkedList<String>>();
		LinkedList<String> undefALINIDs = new LinkedList<String>();
		int success = 0;
		Integer id = 0;
		int count = 0;
		for ( String ID : a_mapGlycoCTList.keySet() ) {
//		while ( count < t_hashGlycoCTList.size() ) {
//			id++;
//			if (! t_hashGlycoCTList.containsKey(id.toString()) )
//				continue;
//			System.out.println(id);
			count++;

			String t_strCode = a_mapGlycoCTList.get(ID);
//			System.out.println(t_strCode);
			String t_strMessage = "";
			StringBuffer t_strValidateGlycoCT = new StringBuffer("");
			try {
				SugarImporter m_objImporterGlycoCT = new SugarImporterGlycoCTCondensed();
				SugarExporterWURCS m_objExporterWURCS = new SugarExporterWURCS();

				Sugar gx = m_objImporterGlycoCT.parse(t_strCode);
				gx = normalizeSugar(gx, t_strValidateGlycoCT);
				m_objExporterWURCS.start(gx);
//				System.out.println( ID + "\t" + t_objExporterWURCS.getWURCS() );
				String t_strWURCS = m_objExporterWURCS.getWURCSCompress();
				if( t_strWURCS.contains("WURCS") )
					success++;
//				System.out.println(ID);
				pw.println( ID + "\t" + t_strWURCS );
				if( t_strWURCS.contains("*?*") ) {
					t_strMessage = "Contain undefined substituent.";
				}
				if ( !hashCountDuplicate.containsKey(t_strWURCS) )
					hashCountDuplicate.put(t_strWURCS, new LinkedList<String>() );
				hashCountDuplicate.get(t_strWURCS).add(ID);

			} catch ( SugarImporterException e ) {
				t_strMessage = "There is an error in GlycoCT";
				if ( e.getErrorText() != null ) {
					t_strMessage = e.getErrorText();
				}
			} catch ( GlycoVisitorException e ) {
				t_strMessage = e.getErrorMessage();
			}
			if ( !t_strMessage.equals("") ) {
				log.println( ID + "\t" + t_strMessage);
				if ( !t_strValidateGlycoCT.toString().equals("") )
					log.println(t_strValidateGlycoCT.toString());

				System.out.println(t_strMessage);
				pw.println( ID + "\t" + t_strMessage);

				if ( !hashErrors.containsKey(t_strMessage) )
					hashErrors.put(t_strMessage, new LinkedList<String>() );
				hashErrors.get(t_strMessage).add(ID);
			} else if ( !t_strValidateGlycoCT.toString().equals("") ) {
				log.println( ID + "\tWarning in GlycoCT.");
				log.println(t_strValidateGlycoCT.toString());
			}

		}

		log.println( "\nTotal CT: " + count );
		log.println( "Successful conversion: " + (success-undefALINIDs.size()) );
		if ( !hashErrors.isEmpty() ) {
			log.println( "Errors: " );
			LinkedList<String> errors = new LinkedList<String>();
			for ( String err : hashErrors.keySet() ) errors.add(err);
			Collections.sort(errors);
			for ( String t_strErr : errors ) {
//				pw.println( "-" + err + ": " + hashErrors.get(err).size() );
				String t_strErrIDs = "";
				int errCount = 0;
				LinkedList<String> errIDs = hashErrors.get(t_strErr);
				Collections.sort(errIDs);
				for ( String errID : errIDs ) {
					t_strErrIDs += errID;
					errCount++;
					if ( errCount == hashErrors.get(t_strErr).size() ) continue;
					t_strErrIDs += ",";
					if ( errCount % 20 == 0 )
						t_strErrIDs += "\n";
				}
				log.println(t_strErrIDs+"\t"+t_strErr+" : "+errCount);
			}
		}
		LinkedList<String> duplicateWURCS = new LinkedList<String>();
		for ( String strWURCS : hashCountDuplicate.keySet() ) {
			if ( hashCountDuplicate.get(strWURCS).size() == 1 ) continue;
			duplicateWURCS.add(strWURCS);
		}
		log.println( "Duplicated structures: " + duplicateWURCS.size() );
		for ( String strWURCS : duplicateWURCS ) {
			String dupIDs = "";
			for ( String dupID : hashCountDuplicate.get(strWURCS) ) {
				if ( dupIDs != "" ) dupIDs += ",";
				dupIDs += dupID;
			}
			log.println(dupIDs+"\t"+strWURCS);
		}
	}

	private static void GlycoCTtoWURCSFromFile(String a_strFileNameR, String a_strFileNameW, String a_strFileNameLog) {
		TreeMap<String, String> t_mapGlycoCTList = new TreeMap<String, String>();

		try{
			String line = null;
			BufferedReader br  = openTextFileR( a_strFileNameR );
			PrintWriter    pw  = openTextFileW( a_strFileNameW );
			PrintWriter    log = openTextFileW( a_strFileNameLog );

			String t_strCode = "";
			String ID ="";
			int count = 0;
			while ( (line = br.readLine())!=null ) {
//				System.out.println(line);
				Matcher mat = Pattern.compile("^(.*)ID:(.*)$").matcher(line);
				if(mat.find()) {
					ID = mat.group(2);
					// Zero fill if ID is integer
					if ( ID.matches("^\\d+$") ) {
						int id = Integer.parseInt( ID );
						ID = String.format("%1$05d", id);
					}
//					System.out.printf("%s\n", ID);
					count++;
					continue;
				}

				if ( line.length() == 0 ) {
					if ( t_strCode == "" ) continue;

					t_mapGlycoCTList.put(ID, t_strCode);
					t_strCode = "";
//					if (count == 100) break;
					continue;
				}
				t_strCode += line + "\n";
			}
			br.close();

			GlycoCTListToWURCS(t_mapGlycoCTList, pw, log);

			pw.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace(System.err);
		}

	}

	private static void GlycoCTtoWURCSFromDir(String a_strDirName, String a_strFileNameW, String a_strFileNameLog) {
		TreeMap<String, String> t_mapGlycoCTList = new TreeMap<String, String>();
		try {
			File dir = new File(a_strDirName);
			File[] files = dir.listFiles();
			ArrayList<String> t_aFileNames = new ArrayList<String>();
			for ( File file : files ) {
				t_aFileNames.add( file.getName() );
			}
			int nFile = files.length;

			PrintWriter pw  = openTextFileW(a_strFileNameW);
			PrintWriter log = openTextFileW(a_strFileNameLog);

			String line = null;

			Integer id=0;
			int count = 0;
			while ( count < nFile ) {
				id++;
				if (! t_aFileNames.contains( id.toString() + ".txt" ) )
					continue;
//				System.out.println(id);
				count++;

				String t_strCode = "";
				String t_strFileNameR = a_strDirName +"\\"+ id.toString() + ".txt";
				BufferedReader br = openTextFileR( t_strFileNameR );
				while ( (line = br.readLine())!=null ) {
//					System.out.println(line);

					if ( line.length() == 0 ) continue;
					t_strCode += line+"\n";
				}
				br.close();
				t_mapGlycoCTList.put(String.format("%1$05d", id), t_strCode);
			}

			GlycoCTListToWURCS(t_mapGlycoCTList, pw, log);

			pw.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
