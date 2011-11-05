package main.java.de.WegFetZ.AudioClient.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Ein simpler <code>FileFilter</code>, der mehrere erlaubte Extensions aufnehmen
 * kann und automatisch eine Description erzeugt.
 *
 * @author Janni Kovacs
 * @version 0.8
 */
public class SimpleFileFilter extends FileFilter {

	/** Die Extension Liste */
	private String ext;

	/** Die Description */
	private String desc;

	/** Ob Verzeichnisse akzeptiert werden sollen */
	private boolean acceptDirs = true;

	/**
	 * Erstellt einen neues <code>SimpleFileFilter</code> mit <code>ext</code>
	 * als Extension. ext kann entweder nur eine Extension sein, z.B. "xml" oder
	 * mehrere mit Kommas getrennt: "xml,xsl". Als Description wird automatisch
	 * <code>ext+"-Files (*.ext1, *.ext2)"</code> gesetzt, wobei ext1 und ext2
	 * für Element 1 und Element 2 der Extensionliste stehen. Es können natürlich
	 * so viele Extensions wie gewünscht gesetzt werden.
	 *
	 * @param ext Die Liste mit den Extensions.
	 */
	public SimpleFileFilter(String ext) {
		this.ext = ext;
		this.desc = ext+"-Files ("+generateExtensionString(ext)+")";
	}

	/**
	 * Erstellt einen neues <code>SimpleFileFilter</code> mit <code>ext</code>
	 * als Extension. ext kann entweder nur eine Extension sein, z.B. "xml" oder
	 * mehrere mit Kommas getrennt: "xml,xsl". Als Description wird automatisch
	 * <code>desc+" (*.ext1, *.ext2)"</code> gesetzt, wobei ext1 und ext2
	 * für Element 1 und Element 2 der Extensionliste stehen. Es können natürlich
	 * so viele Extensions wie gewünscht gesetzt werden.
	 *
	 * @param ext Die Liste mit den Extensions.
	 * @param desc Die Description
	 */
	public SimpleFileFilter(String ext, String desc){
    	this.ext = ext;
    	this.desc = desc + " ("+generateExtensionString(ext)+")";
	}

	/**
	 * Liefert die Description zurück
	 *
	 * @return Die Description
	 */
	public String getDescription() {
		return desc;
	}

	/**
	 * Prüft ob dieser <code>FileFilter</code> dieses <code>File</code> akzeptiert.
	 * Dabei wird die Dateiendung mit jeder gesetzten Extension verglichen.
	 *
	 * @param f Das <code>File</code>
	 * @return true wenn der FileFilter dieses File akzeptiert, false andererweise
	 */
	public boolean accept(File f) {
		if(f.isDirectory() && acceptDirs) {
			return true;
		}
		if(!isMultiExtensionString(ext)) {
			return f.getName().endsWith(ext);
		}
		String[] exts = ext.split(",");
		for(int i=0; i<exts.length; i++) {
			if(f.getName().endsWith(exts[i]))
				return true;
		}
		return false;
	}

	/**
	 * Prüft ob der übergebene Extensionstring mehrere Extensions beinhaltet.
	 * Ein "Multi-Extensino-String" ist z.B. sowas: "xml,xsl,txt"
	 *
	 * @param ext Der String
	 * @return true wenn der String mehrere Extensions beinhaltet
	 */
	protected boolean isMultiExtensionString(String ext) {
		return ext.indexOf(',') >= 0;
	}

	/**
	 * Liefert einen String zurück, der als Description verwendet wird.
	 * Der String wird aus einer Extensionliste generiert. Für den String
	 * "xml,xsl,txt" würde der zurückgeliferte String dann so aussehen:
	 * "*.xml, *.xsl, *.txt"
	 *
	 * @param ext Die Extensionliste
	 * @return ein String der die Extensions darstellt.
	 */
	protected String generateExtensionString(String ext) {
		String[] exts = ext.split(",");
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<exts.length; i++) {
			sb.append("*.");
        	sb.append(exts[i]);
        	if(i != exts.length-1)
        		sb.append(",");
		}
		return sb.toString();
	}

}