/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.Panel3d
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
import java.awt.BorderLayout;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;


/** 
 * The panel that contains the 3d view 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */




public class Panel3D extends JPanel {
	
	private Spots3DCanvas canvas;
	//private VisADConfig config;
	public Panel3D(SpotsTrajectorySet tSet) {
		super();
		canvas = new Spots3DCanvas(tSet);
		//this.config = config;
		setLayout(new BorderLayout());
		add(canvas);
		
	}
	
	public Spots3DCanvas getCanvas() {
		return canvas;
	}
}