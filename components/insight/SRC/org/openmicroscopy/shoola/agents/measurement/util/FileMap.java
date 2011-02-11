/*
 * org.openmicroscopy.shoola.agents.measurement.util.FileMap2 
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
import java.util.List;
import javax.swing.JFileChooser;

//Third-party libraries
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLParserFactory;
import net.n3.nanoxml.XMLWriter;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.io.IOConstants;

/** 
 * Collection of methods to manipulate files.
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
	
	/** The sole instance. */
    private static final FileMap  singleton = new FileMap();
    
    /** Reference to the file chooser. */
    private JFileChooser chooser;
    
    /** Creates a new instance. */
    private FileMap()
    {
    	Registry reg = MeasurementAgent.getRegistry();
		Environment env = (Environment) reg.lookup(LookupNames.ENV);
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(env.getOmeroHome()));
    }
    
	/**
	 * Finds the node in the object which has the server userObject.
	 * 
	 * @param server serverObject to find.
	 * @param parent parent node to find child in.
	 * @return Server TreeNode.
	 */
	private static IXMLElement findServer(String server, IXMLElement parent)
	{
		List<IXMLElement> servers = parent.getChildrenNamed(
				IOConstants.SERVER_TAG);
		for (IXMLElement node : servers)
		{
			if (node.getAttribute(IOConstants.SERVER_NAME_ATTRIBUTE, "").equals(
					server))
				return node;
		}
		return null;
	}
	
	/**
	 * Finds the node in the object which has the user userObject.
	 * 
	 * @param user userObject to find.
	 * @param parent parent node to find child in.
	 * @return User TreeNode.
	 */
	private static IXMLElement findUser(String user, IXMLElement parent)
	{
		List<IXMLElement> users = parent.getChildrenNamed(IOConstants.USER_TAG);
		for (IXMLElement node : users)
		{
			if (node.getAttribute(IOConstants.USER_NAME_ATTRIBUTE, "").equals(
					user))
				return node;
		}
		return null;
	}

	/**
	 * Finds the node in the object which has the user userObject.
	 * 
	 * @param pixelsID userObject to find.
	 * @param parent parent node to find child in.
	 * @return User TreeNode.
	 */
	private static IXMLElement findPixelID(Long pixelsID, IXMLElement parent)
	{
		List<IXMLElement> pixels = parent.getChildrenNamed(
				IOConstants.PIXELSID_TAG);
		for (IXMLElement node : pixels)
		{
			if (node.getAttribute(IOConstants.PIXELSID_ATTRIBUTE, -1) == 
				pixelsID)
				return node;
		}
		return null;
	}
		
	/**
	 * Saves the new mapping to the correct node in the tree, build tree if tree 
	 * empty, or construct parts which don't exist.
	 * 
	 * @param fileMap file map tree.
	 * @param server server of file.
	 * @param user username.
	 * @param pixelsID pixels id of image.
	 * @param fileName fileName to save.
	 */
	private static void saveNode(IXMLElement fileMap, String server, 
								String user, Long pixelsID, String fileName)
	{
		IXMLElement serverNode = findServer(server, fileMap);
		if (serverNode == null)
		{
			serverNode = createServerNode(server);
			fileMap.addChild(serverNode);
		}
		IXMLElement userNode = findUser(user , serverNode);
		if (userNode == null)
		{
			userNode = createUserNode(user);
			serverNode.addChild(userNode);
		}
		IXMLElement pixelsNode = findPixelID(pixelsID , userNode);
		if (pixelsNode == null)
		{
			pixelsNode = createPixelsNode(pixelsID);
			userNode.addChild(pixelsNode);
		}
		if (pixelsNode.getChildrenCount() != 0)
		{
			for (int i = pixelsNode.getChildrenCount()-1 ; i >= 0 ; i--)
				pixelsNode.removeChildAtIndex(i);
		}
		
		IXMLElement fileNode = createFileNode(fileName);
		pixelsNode.addChild(fileNode);
	}

	/**
	 * Creates a new server node with server object.
	 * 
	 * @param server see above.
	 * @return see above.
	 */
	private static IXMLElement createServerNode(String server)
	{
		IXMLElement element = new XMLElement(IOConstants.SERVER_TAG);
		element.setAttribute(IOConstants.SERVER_NAME_ATTRIBUTE, server);
		return element;
	}
		
	/**
	 * Creates a new user node with users object.
	 * 
	 * @param user see above.
	 * @return see above.
	 */
	private static IXMLElement createUserNode(String user)
	{
		IXMLElement element = new XMLElement(IOConstants.USER_TAG);
		element.setAttribute(IOConstants.USER_NAME_ATTRIBUTE, user);
		return element;
	}

	/**
	 * Creates a new pixels node with pixels object.
	 * 
	 * @param pixels see above.
	 * @return see above.
	 */
	private static IXMLElement createPixelsNode(Long pixels)
	{
		if (pixels == null) return null;
		IXMLElement element = new XMLElement(IOConstants.PIXELSID_TAG);
		element.setAttribute(IOConstants.PIXELSID_ATTRIBUTE, pixels.toString());
		return element;
	}
	
	/**
	 * Creates a new file node with fileName object.
	 * 
	 * @param fileName see above.
	 * @return see above.
	 */
	private static IXMLElement createFileNode(String fileName)
	{
		IXMLElement element = new XMLElement(IOConstants.FILE_TAG);
		element.setAttribute(IOConstants.FILENAME_ATTRIBUTE, fileName);
		return element;
	}
		
	/**
	 * Saves the created fileMap to IOConstants.FILEMAP file. 
	 * The filemap needs be to converted from OMETreeNode to XMLElement.
	 * @param fileMap tree.
	 * @throws ParsingException 
	 */
	private static void saveFileMap(IXMLElement fileMap) 
		throws ParsingException
	{
		OutputStream out;
		try
		{
			out = new BufferedOutputStream(
						new FileOutputStream(createFileMap()));
		}
		catch (Exception ex)
		{
			ParsingException e = new ParsingException(ex.getMessage());
			e.initCause(ex);
			throw e;
		}
		
		try
		{
			new XMLWriter(out).write(fileMap);
		}
		catch (Exception e)
		{
			throw new ParsingException(e.getMessage());
		}
	}
	
	/**
	 * Loads the filemap and creates a Map from the file. 
	 * The map, maps the pixel id's to the fileNames. 
	 * 
	 * @return Dee above.
	 * @throws ParsingException any exception in reading file.
	 */
	public static IXMLElement getFileMap() 
		throws ParsingException
	{
		IXMLParser parser;
		InputStream in;
		IXMLElement document;
		try
		{
			parser = XMLParserFactory.createDefaultXMLParser();
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
	            new FileInputStream(createFileMap()));
			IXMLReader reader = new StdXMLReader(in);
			parser.setReader(reader);
			document=(IXMLElement) parser.parse();
			in.close();
		}
		catch (Exception ex)
		{
			ParsingException e=new ParsingException(ex.getMessage());
			e.initCause(ex);
			throw e;
		}
		return document;
	}

	/**
	 * Returns <code>true</code> if the fileMap.xml file exists, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean fileMapExists()
	{
		Registry reg = MeasurementAgent.getRegistry();
		Environment env = (Environment) reg.lookup(LookupNames.ENV);
		String fileName = (String) reg.lookup(LookupNames.ROI_MAIN_FILE);
		String directory = env.getOmeroHome();
		File[] list = singleton.chooser.getCurrentDirectory().listFiles();
		String path = directory+File.separator+fileName;
		if (list == null) return false;
		File f;
		for (int i = 0; i < list.length; i++) {
			f = list[i];
			if (f.getAbsolutePath().equals(path)) return true;
		}
		return false;
	}
	
	/**
	 * Creates the file map.
	 * 
	 * @return See above.
	 */
	private static File createFileMap()
	{
		Registry reg = MeasurementAgent.getRegistry();
		Environment env = (Environment) reg.lookup(LookupNames.ENV);
		String fileName = (String) reg.lookup(LookupNames.ROI_MAIN_FILE);
		return new File(env.getOmeroHome(), fileName);
	}
	
	/** 
	 * Returns the file where the ROI were saved for the pixelsID.
	 * 
	 * @param server The name of the server.
	 * @param user   The username for that server.
	 * @param pixelsID see above.
	 * @return see above.
	 * @throws ParsingException see above.
	 */
	public static String getSavedFile(String server, String user, Long pixelsID)
		throws ParsingException
	{
		IXMLElement node;
		if (!fileMapExists()) return null;
		IXMLElement fileMap = getFileMap();
		node = findServer(server, fileMap);
		if (node == null) return null;
		node = findUser(user, node);
		if (node == null) return null;
		node = findPixelID(pixelsID, node);
		if (node == null) return null;
		if (node.getChildrenCount() == 0) return null;
		node = node.getFirstChildNamed(IOConstants.FILE_TAG);
		
		String fileName = node.getAttribute(IOConstants.FILENAME_ATTRIBUTE, "");
		if (fileName == null) return null;
		if (fileName.equals("")) return null;
		return fileName;
	}

	/**
	 * Sets the savedfile(fileMap.xml) file to include the filename and pixelid
	 * map.
	 * 
	 * @param user see above.
	 * @param server see above.
	 * @param pixelsID see above.
	 * @param fileName see above.
	 * @throws ParsingException see above.
	 */
	public static void setSavedFile(String server, String user, long pixelsID, 
										String fileName) 
		throws ParsingException
	{
		IXMLElement fileMap;
		if (fileMapExists())
			fileMap = getFileMap();
		else
		{
			fileMap = new XMLElement(IOConstants.PIXELSSET_TAG,
				IOConstants.FILEMAP_XML_NAMESPACE);
			fileMap.setAttribute(IOConstants.FILEMAP_XML_VERSION_TAG, 
												IOConstants.FILEMAP_XML_VERSION);
		}
		saveNode(fileMap, server, user, pixelsID, fileName);
		saveFileMap(fileMap);
	}


}
