/*
 * pojos.FileAnnotationData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package pojos;



//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import ome.model.annotations.FileAnnotation;
import ome.model.core.OriginalFile;

/** 
 * Annotation to upload files to the server.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FileAnnotationData 
	extends AnnotationData
{
	
	/** Identifies the <code>PDF</code> file formats. */
	public static final String PDF = "pdf";
	
	/** Identifies the <code>Text</code> file formats. */
	public static final String TEXT = "txt";
	
	/** Identifies the <code>CSV</code> file formats. */
	public static final String CSV = "csv";
	
	/** Identifies the <code>XML</code> file formats. */
	public static final String XML = "xml";
	
	/** Identifies the <code>HTML</code> file formats. */
	public static final String HTML = "html";
	
	/** Identifies the <code>HTM</code> file formats. */
	public static final String HTM = "htm";
	
	/** Identifies the <code>Word</code> file formats. */
	public static final String WORD = "doc";
	
	/** Identifies the <code>Excel</code> file formats. */
	public static final String EXCEL = "xls";
	
	/** Identifies the <code>Power point</code> file formats. */
	public static final String POWER_POINT = "ppt";
	
	/** Identifies the <code>RTF</code> file formats. */
	public static final String RTF = "rtf";
	
	/** 
	 * The <code>PDF</code> file format as defined by specification
	 * corresponding to the extension. 
	 */
	private static final String SERVER_PDF = "application/pdf";
	
	/** 
	 * The <code>XML</code> file format as defined by specification
	 * corresponding to the extension. 
	 */
	private static final String SERVER_XML = "text/xml";
	
	/** 
	 * The <code>TEXT</code> file format as defined by specification
	 * corresponding to the extension. 
	 */
	private static final String SERVER_TEXT = "text/plain";
	
	/** 
	 * The <code>CSV</code> file format as defined by specification
	 * corresponding to the extension. 
	 */
	private static final String SERVER_CSV = "text/csv";
	
	/** The file to upload to the server. */
	private File 	attachedFile;
	
	/** The format of the file. */
	private String 	format;
	
	/**
	 * Controls if the file format is supported.
	 * 
	 * @param path The absolute path.
	 */
	private void validateFormat(String path)
	{
		if (path == null)
			throw new IllegalArgumentException("Path cannot be null.");
		if (path.endsWith(PDF)) format = PDF;
		else if (path.endsWith(TEXT)) format = TEXT;
		else if (path.endsWith(CSV)) format = CSV;
		else if (path.endsWith(XML)) format = XML;
		else if (path.endsWith(HTML) || path.endsWith(HTM)) format = HTML;
		else if (path.endsWith(WORD)) format = WORD;
		else if (path.endsWith(EXCEL)) format = EXCEL;
		else if (path.endsWith(POWER_POINT)) format = POWER_POINT;
		else if (path.endsWith(RTF)) format = RTF;
		else
			throw new IllegalArgumentException("Format not supported.");
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param file 	The file to attach.
	 */
	public FileAnnotationData(File file)
	{
		super(FileAnnotation.class);
		if (file == null)
			throw new IllegalArgumentException("File cannot be null.");
		validateFormat(file.getAbsolutePath());
		attachedFile = file;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param annotation The annotation to wrap.
	 */
	public FileAnnotationData(FileAnnotation annotation)
	{
		super(annotation);
		format = null;
	}
	
	/**
	 * Returns the file format as defined by the specification, corresponding 
	 * to the file extension.
	 * 
	 * @return See above.
	 */
	public String getServerFileFormat()
	{
		if (format == null) return SERVER_TEXT;
		if (format.equals(PDF)) return SERVER_PDF;
		if (format.equals(XML) || format.equals(HTML) || 
			format.equals(HTM)) return SERVER_XML;
		if (format.equals(CSV)) return SERVER_CSV;
		if (format.equals(TEXT)) return SERVER_TEXT;
		if (format.equals(WORD)) return SERVER_TEXT;
		if (format.equals(EXCEL)) return SERVER_TEXT;
		if (format.equals(POWER_POINT)) return SERVER_TEXT;
		throw new IllegalArgumentException("Format not supported.");
	}
	
	/** 
	 * Returns the format of the uploaded file.
	 * 
	 * @return See above.
	 */
	public String getFileFormat()
	{
		if (attachedFile != null) return format;
		OriginalFile f = ((FileAnnotation) asAnnotation()).getFile();
		String format = f.getFormat().getValue();
		if (SERVER_PDF.equals(format)) return PDF;
		else if (SERVER_CSV.equals(format)) return CSV;
		else if (SERVER_TEXT.equals(format)) return TEXT;
		else if (SERVER_XML.equals(format)) return XML;
		throw new IllegalArgumentException("Format not supported.");
	}
	
	/**
	 * Returns a user readable description of the file.
	 * 
	 * @return See above.
	 */
	public String getFileKind()
	{
		String format = getFileFormat();
		if (PDF.equals(format))
			return "PDF Document";
		else if (XML.equals(format))
			return "XML Document";
		else if (WORD.equals(format))
			return "Microsoft Word Document";
		else if (EXCEL.equals(format))
			return "Microsoft Excel Document";
		else if (TEXT.equals(format))
			return "Plain Text Document";
		else if (HTML.equals(format) || HTM.equals(format))
			return "HTML Document";
		else if (CSV.equals(format))
			return "Comma Separated Value Document";
		return "";
	}
	
	/**
	 * Returns the file to upload to the server.
	 * 
	 * @return See above.
	 */
	public File getAttachedFile() { return attachedFile; }
	
	/**
	 * Returns the name of the file.
	 * 
	 * @return See above.
	 */
	public String getFileName()
	{
		if (attachedFile != null)
			return attachedFile.getName();
		OriginalFile f = ((FileAnnotation) asAnnotation()).getFile();
		if (f != null) return f.getName();
		return "";
	}
	
	/**
	 * Returns the absolute path to the file.
	 * 
	 * @return See above.
	 */
	public String getFilePath()
	{
		if (attachedFile != null)
			return attachedFile.getAbsolutePath();
		OriginalFile f = ((FileAnnotation) asAnnotation()).getFile();
		if (f != null) return f.getPath();
		return "";
	}
	
	/**
	 * Returns the size of the file.
	 * 
	 * @return See above.
	 */
	public long getFileSize()
	{
		if (getId() < 0) return -1;
		OriginalFile f = ((FileAnnotation) asAnnotation()).getFile();
		if (f == null) return -1;
		return f.getSize();
	}
	
	/**
	 * Returns the original file.
	 * @see AnnotationData#getContent()
	 */
	public Object getContent()
	{
		return ((FileAnnotation) asAnnotation()).getFile();
	}

	/**
	 * Returns the absolute path to the file
	 * @see AnnotationData#getContentAsString()
	 */
	public String getContentAsString() { return getFilePath();  }

	/**
	 * Sets the text annotation.
	 * @see AnnotationData#setContent(Object)
	 */
	public void setContent(Object content)
	{
		if (content == null)
			throw new IllegalArgumentException("Content must be an " +
												"Original file");
		if (content instanceof OriginalFile) {
			((FileAnnotation) asAnnotation()).setFile((OriginalFile) content);
		} else
			throw new IllegalArgumentException("Content must be an " +
											"Original file");
	}
	
}
