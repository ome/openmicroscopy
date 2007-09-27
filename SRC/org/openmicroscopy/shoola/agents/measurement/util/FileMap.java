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

//Third-party libraries

//Application-internal dependencies
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
import java.util.Vector;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLParserFactory;
import net.n3.nanoxml.XMLWriter;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.io.IOConstants;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;

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
	/** Exception, there is a missing or toomany pixelset elements. */
	private final static String PIXELSET_MISSING = 
									"Too many or not enough pixel sets in file.";
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
	public static OMETreeNode getFileMap() throws ParsingException
	{
		OMETreeNode node = new OMETreeNode("Root");
		OMETreeNode root = node;
		
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
	
		ArrayList<IXMLElement> serverList = document.getChildrenNamed(IOConstants.SERVER_TAG);
		for(IXMLElement server : serverList)
		{
			OMETreeNode serverNode = new OMETreeNode(server);
			ArrayList<IXMLElement> userList = server.getChildrenNamed(IOConstants.USER_TAG);
			for(IXMLElement user : userList)
			{
				OMETreeNode userNode = new OMETreeNode(user);
				ArrayList<IXMLElement> pixelList = user.getChildrenNamed(IOConstants.PIXELSID_TAG);
				for(IXMLElement pixel : pixelList)
				{
					OMETreeNode pixelNode = new OMETreeNode(pixel);
					userNode.add(pixelNode);
					XMLElement fileElement = (XMLElement) pixel.getFirstChildNamed(IOConstants.FILE_TAG);
					OMETreeNode fileNode = new OMETreeNode(fileElement);
					pixelNode.add(fileNode);
				}
				serverNode.add(userNode);
			}
			root.add(serverNode);
		}
		return root;
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
	 * Get the file where the ROI were saved for the pixelsID.
	 * @param pixelsID see above.
	 * @return see above.
	 * @throws ParsingException see above.
	 */
	public static String getSavedFile(String server, String user, Long pixelsID) throws ParsingException
	{
		if(!fileMapExists())
			return null;
		OMETreeNode fileMap = getFileMap();
		OMETreeNode node;
		node = findServer(server, fileMap);
		if(node == null)
			return null;
		node = findUser(user, node);
		if(node == null)
			return null;
		node = findPixelID(pixelsID, node);
		if(node == null)
			return null;
		if(node.getChildCount()==0)
			return null;
		node = (OMETreeNode)node.getChildAt(0);
		IXMLElement fileElement = (XMLElement)node.getUserObject();
		
		String fileName = fileElement.getAttribute(IOConstants.FILENAME_ATTRIBUTE, "");
		if(fileName.equals(""))
			return null;
		else
			return fileName;
	}

	/**
	 * Find the node in the object which has the server userObject.
	 * @param server serverObject to find.
	 * @param parent parent node to find child in.
	 * @return Server TreeNode.
	 */
	private static OMETreeNode findServer(String server, OMETreeNode parent)
	{
		Vector<MutableTreeTableNode> servers = parent.getChildList();
		if(servers==null)
			return null;
		for(MutableTreeTableNode node : servers)
		{
			IXMLElement element = (IXMLElement)((OMETreeNode) node).getUserObject();
			if(element.getAttribute(IOConstants.SERVER_NAME_ATTRIBUTE, "").equals(server))
				return (OMETreeNode)node;
		}
		return null;
	}
	
	/**
	 * Find the node in the object which has the user userObject.
	 * @param user userObject to find.
	 * @param parent parent node to find child in.
	 * @return User TreeNode.
	 */
	private static OMETreeNode findUser(String user, OMETreeNode parent)
	{
		Vector<MutableTreeTableNode> users = parent.getChildList();
		if(users==null)
			return null;

		for(MutableTreeTableNode node : users)
		{
			IXMLElement element = (IXMLElement)((OMETreeNode)node).getUserObject();
			if(element.getAttribute(IOConstants.USER_NAME_ATTRIBUTE, "").equals(user))
				return (OMETreeNode)node;
		}
		return null;
	}
	

	/**
	 * Find the node in the object which has the user userObject.
	 * @param pixelsID userObject to find.
	 * @param parent parent node to find child in.
	 * @return User TreeNode.
	 */
	private static OMETreeNode findPixelID(Long pixelsID, OMETreeNode parent)
	{
		Vector<MutableTreeTableNode> pixels = parent.getChildList();
		if(pixels==null)
			return null;
		for(MutableTreeTableNode node : pixels)
		{
			IXMLElement element = (IXMLElement)((OMETreeNode)node).getUserObject();
			if(element.getAttribute(IOConstants.PIXELSID_ATTRIBUTE, -1)==pixelsID)
				return (OMETreeNode)node;
		}
		return null;
	}
		
	/**
	 * Set the savedfile(fileMap.xml) file to include the filename and pixelid
	 * map.
	 * @param user see above.
	 * @param server see above.
	 * @param pixelsID see above.
	 * @param fileName see above.
	 * @throws ParsingException see above.
	 */
	public static void setSavedFile(String server, String user, long pixelsID, 
										String fileName) throws ParsingException
	{
		OMETreeNode fileMap;
		if(fileMapExists())
			fileMap = getFileMap();
		else
			fileMap = new OMETreeNode("root");
		saveNode(fileMap, server, user, pixelsID, fileName);
		saveFileMap(fileMap);
	}
	
	/**
	 * Save the created fileMap to IOConstants.FILEMAP file. 
	 * The filemap needs be to converted from OMETreeNode to XMLElement.
	 * @param fileMap tree.
	 * @throws ParsingException 
	 */
	private static void saveFileMap(OMETreeNode fileMap) throws ParsingException
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
		document.setAttribute(IOConstants.FILEMAP_XML_VERSION_TAG, 
											IOConstants.FILEMAP_XML_VERSION);
	
		Vector<MutableTreeTableNode> serverList = fileMap.getChildList();
		try
		{
			for(MutableTreeTableNode server : serverList)
				writeServerElement(document, (OMETreeNode)server);
			new XMLWriter(out).write(document);
		}
		catch (Exception e)
		{
			throwParsingException(e.getMessage());
		}
	}
	
	/**
	 * Write the serverElement to the document.
	 * @param document see above.
	 * @param server see above.
	 */
	private static void writeServerElement(IXMLElement document, 
															OMETreeNode server)
	{
		IXMLElement serverElement = (XMLElement)server.getUserObject();
		Vector<MutableTreeTableNode> userList = server.getChildList();
		for(MutableTreeTableNode node : userList)
			writeUserElement(serverElement, (OMETreeNode) node);
		document.addChild(serverElement);
	}
	
	/**
	 * Write the user element to the serverElement node.
	 * @param serverElement see above.
	 * @param user see above.
	 */
	private static void writeUserElement(IXMLElement serverElement, 
															OMETreeNode user)
	{
		IXMLElement userElement = (XMLElement)user.getUserObject();
		Vector<MutableTreeTableNode> pixelList = user.getChildList();
		for(MutableTreeTableNode node : pixelList)
			writePixelElement(userElement,(OMETreeNode) node);
		serverElement.addChild(userElement);
	}

	/**
	 * Write the userElement to the pixel element node.
	 * @param userElement see above.
	 * @param pixel see above.
	 */
	private static void writePixelElement(IXMLElement userElement, 
															OMETreeNode pixel)
	{
		IXMLElement pixelElement = (XMLElement)pixel.getUserObject();
		Vector<MutableTreeTableNode> fileList = pixel.getChildList();
		for(MutableTreeTableNode node : fileList)
			writeFileElement(pixelElement, (OMETreeNode)node);
		userElement.addChild(pixelElement);
	}

	/**
	 * Write the fileelement to the pixel element node.
	 * @param pixelElement see above.
	 * @param fileName see above.
	 */
	private static void writeFileElement(IXMLElement pixelElement, 
													OMETreeNode fileName)
	{
		IXMLElement fileElement = (XMLElement)fileName.getUserObject();
		pixelElement.addChild(fileElement);
	}
	/**
	 * Save the new mapping to the correct node in the tree, build tree if tree 
	 * empty, or construct parts which don't exist.
	 * @param fileMap file map tree.
	 * @param server server of file.
	 * @param user username.
	 * @param pixelsID pixels id of image.
	 * @param fileName fileName to save.
	 */
	private static void saveNode(OMETreeNode fileMap, String server, 
								String user, Long pixelsID, String fileName)
	{
		OMETreeNode serverNode = findServer(server, fileMap);
		if(serverNode==null)
		{
			serverNode = createServerNode(server);
			fileMap.add(serverNode);
		}
		OMETreeNode userNode = findUser(server , serverNode);
		if(userNode==null)
		{
			userNode = createUserNode(user);
			serverNode.add(userNode);
		}
		OMETreeNode pixelsNode = findPixelID(pixelsID , userNode);
		if(pixelsNode==null)
		{
			pixelsNode = createPixelsNode(pixelsID);
			userNode.add(pixelsNode);
		}
		if(pixelsNode.getChildCount()!=0)
			for(MutableTreeTableNode node : pixelsNode.getChildList())
				pixelsNode.remove(node);
		OMETreeNode fileNode = createFileNode(fileName);
		pixelsNode.add(fileNode);
	}
	

	/**
	 * Create a new file node with fileName object.
	 * @param fileName see above.
	 * @return see above.
	 */
	private static OMETreeNode createFileNode(String fileName)
	{
		IXMLElement element = new XMLElement(IOConstants.FILE_TAG);
		element.setAttribute(IOConstants.FILENAME_ATTRIBUTE, fileName);
		OMETreeNode node = new OMETreeNode(element);
		return node;
	}
	
	/**
	 * Create a new pixels node with pixels object.
	 * @param pixels see above.
	 * @return see above.
	 */
	private static OMETreeNode createPixelsNode(Long pixels)
	{
		IXMLElement element = new XMLElement(IOConstants.PIXELSID_TAG);
		element.setAttribute(IOConstants.PIXELSID_ATTRIBUTE, pixels.toString());
		OMETreeNode node = new OMETreeNode(element);
		return node;
	}
	
	/**
	 * Create a new users node with users object.
	 * @param user see above.
	 * @return see above.
	 */
	private static OMETreeNode createUserNode(String user)
	{
		IXMLElement element = new XMLElement(IOConstants.USER_TAG);
		element.setAttribute(IOConstants.USER_NAME_ATTRIBUTE, user);
		OMETreeNode node = new OMETreeNode(element);
		return node;
	}
	
	/**
	 * Create a new server node with server object.
	 * @param server see above.
	 * @return see above.
	 */
	private static OMETreeNode createServerNode(String server)
	{
		IXMLElement element = new XMLElement(IOConstants.SERVER_TAG);
		element.setAttribute(IOConstants.SERVER_NAME_ATTRIBUTE, server);
		OMETreeNode node = new OMETreeNode(element);
		return node;
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
		
	
}






