/*
 * org.openmicroscopy.shoola.util.processing.graph.XMLNode
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
package org.openmicroscopy.shoola.util.processing.graph;

//Java imports
import java.awt.Color;

//Third-party libraries
import net.n3.nanoxml.XMLElement;
import processing.core.PApplet;

//Application-internal dependencies

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
public class XMLNode
	implements NodeObject
{
	/** The XML Element that is wrapped by the XMLNode. */
	XMLElement element;
	
	/**
	 * Instantiate the XML node, wrapping the XMLElement.
	 * @param element See above.
	 */
	XMLNode(XMLElement element)
	{
		this.element = element;
	}
	
	/**
	 * Render the XMLNode, at location x, y.
	 * {@see NodeRenderer#render(PApplet, float, float)}
	 */
	public void render(PApplet parent, float x, float y)
	{
		parent.pushStyle();
			if(element.getContent()!=null)
			{
				parent.pushStyle();
				parent.fill(new Color(255,102,102).getRGB());
				parent.text(element.getContent(),x,y);
				parent.popStyle();
				parent.ellipse(x,y,20,20);
			}
			else
			{
				parent.text(element.getName(),x,y);
				parent.fill(new Color(102,255,102).getRGB());
				parent.ellipse(x,y,20,20);
			}
			
		parent.popStyle();
	}

	/**
	 * Comaparator for the XMLNode.
	 * {@see NodeObject#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2)
	{
		if(o1 instanceof XMLElement && o2 instanceof XMLElement)
		{
			XMLElement o1E;
			XMLElement o2E;
			o1E = (XMLElement)o1;
			o2E = (XMLElement)o2;
			if(o1E.equals(o2E))
				return 0;
		}
		return -1;
	}

	/**
	 * The equals comparison from {@see NodeObject#equals(Object)}
	 */
	public boolean equals(Object o1)
	{
		if(o1 instanceof XMLElement)
		{
			XMLElement o1Element = (XMLElement)o1;
			return o1Element.equals(element);
		}
		return false;
	}


	
}
