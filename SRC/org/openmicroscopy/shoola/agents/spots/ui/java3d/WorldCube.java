/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.WorldCube
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.spots.ui.java3d;

//Java imports
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;


//Third-party libraries

//Application-internal dependencies

/** 
 * The 3d cube that delinates the space of interest 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class WorldCube extends Shape3D {
	
	
	public WorldCube(Extents extents) {
		super();
		setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		setCapability(Shape3D.ALLOW_PICKABLE_WRITE);
		setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		resetCube(extents);
	}
	
	public void resetCube(Extents extents) {
		CubeGeometry cube = new CubeGeometry(extents);
		
		setGeometry(cube);
		setPickable(false);
		Appearance appearance = new Appearance();
     	PolygonAttributes polygonAttributes = new PolygonAttributes();
     	polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
        polygonAttributes.setPolygonMode( PolygonAttributes.POLYGON_LINE);
        appearance.setPolygonAttributes( polygonAttributes );
        ColoringAttributes ca = new ColoringAttributes();
        ca.setColor(0.0f,0.0f,0.0f);
        appearance.setColoringAttributes(ca);
        
        setAppearance( appearance );
	}
}