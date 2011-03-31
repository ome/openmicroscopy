/*
 * org.openmicroscopy.shoola.examples.graph.XMLTest
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
 *----------------------------------------------------------------------------*/
package org.openmicroscopy.shoola.examples.graph;

//Java imports
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


//Third-party libraries

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLParserFactory;

import processing.core.PApplet;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.processing.graph.GraphNode;
import org.openmicroscopy.shoola.util.processing.graph.NetworkGraph;
import org.openmicroscopy.shoola.util.processing.graph.XMLMapper;

/** 
 * 
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
public class XMLTest
extends PApplet
{
	/** 
	 * The Network graph object that manages the link between nodes and particles
	 * lays out the nodes.
	 */
	NetworkGraph networkGraph;

	/**
	 * The mapper from xml to graphNodes.
	 */
	XMLMapper mapper;

	/** The root of the graph. */
	GraphNode root;

	/** The zoom level. */
	float zoom=1;

	/**
	 * Instantiate the main class, XMLTest.
	 * @param args
	 */
	public static void main(String[] args)
	{
		PApplet.main(new String[] {"org.openmicroscopy.shoola.examples.graph.XMLTest"});
	}

	/**
	 * Called when the mouse wheel is moved; used to change the zooming.
	 * @param i The amount the wheel moved.
	 */
	public void mouseWheel(int i)
	{
		zoom = zoom+((float)i)/10.0f;
		if(zoom<0.001)
			zoom = 0.001f;

	}

	/**
	 * Setup the processing sketch.
	 */
	public void setup()
	{
		addMouseWheelListener(new java.awt.event.MouseWheelListener() 
		{ 
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) 
			{ 
				mouseWheel(evt.getWheelRotation());
			}
		}); 
		size( 800, 800 );
		smooth();
		strokeWeight( 2 );
		ellipseMode( CENTER );       
		File file = new File("spim-ome.xml");
		InputStream input = null;
		try
		{
			input = new FileInputStream(file);
		} catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		IXMLParser parser;
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
		IXMLElement document = null;
		try
		{
			IXMLReader reader=new StdXMLReader(input);
			parser.setReader(reader);
			document=(IXMLElement) parser.parse();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();;
		}
		mapper = new XMLMapper((XMLElement)document);
		mapper.parseDocument();
		root = mapper.getRoot();
		networkGraph = new NetworkGraph(this, root);

	}

	/**
	 * Draw the sketch, graph.
	 */
	public void draw()
	{
		background( 255 );
		fill( 0 );
		translate( width/2 , height/2 );
		scale(zoom);
		networkGraph.tick();
		networkGraph.drawNetwork();
	}


	/** 
	 * The mouse has been released.
	 */
	public void mouseReleased()
	{
		networkGraph.mouseReleased();
	}

	/**
	 * The mouse has been pressed.
	 */
	public void mousePressed()
	{

		networkGraph.mousePressed(mouseX, mouseY);
	}

	/**
	 * The mouse has been dragged.
	 */
	public void mouseDragged()
	{
		networkGraph.mouseDragged(mouseX, mouseY, dmouseX, dmouseY);
	}
}
