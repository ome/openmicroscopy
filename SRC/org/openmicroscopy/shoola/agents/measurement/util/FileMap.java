/*
 * org.openmicroscopy.shoola.agents.measurement.util.FileMap 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util;


//Java imports
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

//Third-party libraries
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLParserFactory;
import net.n3.nanoxml.XMLWriter;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.io.IOConstants;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FileMap
{	
	/** Exception, there is a missing attribute in the pixel element. */
	
	private final static String PIXELELEMENT_ATTRIBUTE = 
									"Missing is attribute in pixelElement";
	
	/** Exception, there is a missing attribute in the fileElement. */
	private final static String FILEELEMENT_ATTRIBUTE = 
										"Missing is attribute in fileElement";
	
	/** Exception the output file could not be created. */
	private final static String XML_OUTPUT_ERROR = "Cannot create XML output";
	
	/**
	 * This method loads the filemap and creates a HashMap from the file. 
	 * The map, maps the pixel id's to the fileNames. 
	 * @return see above.
	 * @throws ParsingException any exception in reading file.
	 */
	public static HashMap<Long, String> getSavedFileMap() throws ParsingException
	{
		HashMap<Long, String> fileMap = new HashMap<Long, String>();
		IXMLParser parser;
		InputStream in;
		IXMLElement document;
		try
		{
			parser=XMLParserFactory.createDefaultXMLParser();
		}
		catch (Exception ex)
		{
			InternalError e=
					new InternalError("Unable to instantiate NanoXML Parser");
			e.initCause(ex);
			throw e;
		}
		
		try
		{
			in = new BufferedInputStream(
	            new FileInputStream(IOConstants.FILEMAP_XML));
			IXMLReader reader=new StdXMLReader(in);
			parser.setReader(reader);
			document=(IXMLElement) parser.parse();
		}
		catch (Exception ex)
		{
			ParsingException e=new ParsingException(ex.getMessage());
			e.initCause(ex);
			throw e;
		}
		
		ArrayList<IXMLElement> pixelSet = 
							document.getChildrenNamed(IOConstants.PIXELSID_TAG);
		
		for(IXMLElement pixelsElement : pixelSet)
		{
			long id = pixelsElement.getAttribute(IOConstants.PIXELSID_ATTRIBUTE,-1);
			if(id == -1)
				throwParsingException(PIXELELEMENT_ATTRIBUTE);
			ArrayList<IXMLElement> fileElementList=
				pixelsElement.getChildrenNamed(IOConstants.FILE_TAG);
		
			for (IXMLElement fileElement : fileElementList)
			{
				String fileName = fileElement.getAttribute(IOConstants.FILENAME_ATTRIBUTE,"");
				if(fileName.equals(""))
					throwParsingException(FILEELEMENT_ATTRIBUTE);
				fileMap.put(id, fileName);
			}
		}
		return fileMap;
	}
		
	/** 
	 * Get the file where the ROI were saved for the pixelsID.
	 * @param pixelsID see above.
	 * @return see above.
	 * @throws ParsingException see above.
	 */
	public static String getSavedFile(Long pixelsID) throws ParsingException
	{
		HashMap<Long, String> fileMap = getSavedFileMap();
		if(fileMap.containsKey(pixelsID))
			return fileMap.get(pixelsID);
		return null;
	}
	
	/**
	 * Throw a parsing exception with the string str.
	 * @param str see above.
	 * @throws ParsingException see above.
	 */
	private static void throwParsingException(String str) throws ParsingException
	{
		ParsingException e=new ParsingException(str);
		throw e;
	}
		
	/**
	 * Returns true if the fileMap.xml file exists.
	 * @return see above.
	 */
	public static boolean  fileMapExists()
	{
		File xmlFile = new File(IOConstants.FILEMAP_XML);
		return xmlFile.exists();
	}
	
	/**
	 * Set the savedfile(fileMap.xml) file to include the filename and pixelid
	 * map.
	 * @param fileName see above.
	 * @param pixelsID see above.
	 * @throws ParsingException see above.
	 */
	public static void setSavedFile(String fileName, long pixelsID) throws ParsingException
	{
		HashMap<Long, String> fileMap = new HashMap<Long, String>();
		if(fileMapExists())
			fileMap = getSavedFileMap();
		fileMap.put(pixelsID, fileName);
		saveFileMap(fileMap);
	}
	
	/**
	 * Save the read filemap to the filemap file(fileMap.xml);
	 * @param fileMap the hashmap to save.
	 * @throws ParsingException any parsing exceptions.
	 */
	public static void saveFileMap(HashMap<Long, String> fileMap) throws ParsingException
	{
		IXMLElement document;
		OutputStream out;
		try
		{
			out = new BufferedOutputStream(
	            new FileOutputStream(IOConstants.FILEMAP_XML));
		}
		catch (Exception ex)
		{
			ParsingException e=new ParsingException(ex.getMessage());
			e.initCause(ex);
			throw e;
		}
		
		document = new XMLElement(IOConstants.PIXELSSET_TAG,
				IOConstants.FILEMAP_XML_NAMESPACE);
		document.setAttribute(IOConstants.FILEMAP_XML_VERSION_TAG, IOConstants.FILEMAP_XML_VERSION);
	
		Iterator <Long>iterator=fileMap.keySet().iterator();
		try
		{
			while (iterator.hasNext())
			{
				Long pixelsID = iterator.next();
				writePixelsElement(document, pixelsID, fileMap.get(pixelsID));
			}
			new XMLWriter(out).write(document);
		}
		catch (Exception e)
		{
			throwParsingException(XML_OUTPUT_ERROR);
		}
	}
	
	/**
	 * Write a pixel element(pixelsID, fileName) to the document.
	 * @param document the document being created.
	 * @param pixelsID the pixels id to save.
	 * @param fileName the filename mapped to the pixels id.
	 */
	private static void writePixelsElement(IXMLElement document, Long pixelsID,
																String fileName)
	{
		IXMLElement pixelsElement = new XMLElement(IOConstants.PIXELSID_TAG);
		pixelsElement.setAttribute(IOConstants.PIXELSID_ATTRIBUTE, pixelsID.toString());
		IXMLElement fileElement = new XMLElement(IOConstants.FILE_TAG);
		fileElement.setAttribute(IOConstants.FILENAME_ATTRIBUTE, fileName);
		pixelsElement.addChild(fileElement);
		document.addChild(pixelsElement);
	}
}




