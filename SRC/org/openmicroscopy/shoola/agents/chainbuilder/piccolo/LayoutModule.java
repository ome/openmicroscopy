/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.LayoutModule
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */




package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;


/** 
 * A subclass of {@link ModuleView}, used to contain bounds in terms of an x,y 
 * position and a height. this is never added to a scenegraph or drawn -
 * instead, it is just used to hold some space during layouts of chains.
 * For convenience, it is helpful that its an instance of {@link ModuleView}
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
*/

public class LayoutModule extends ModuleView {

	private static double DUMMY_HEIGHT = 50;	
	
	
	double x=0,y=0;
	double height = 0;
	// Some static constants for convenience.
	
	public LayoutModule() {
		super();
	}
	
	public void setOffset(double  x,double y) {
		this.x =x;
		this.y = y;
	}
	
	public double getX() { 
		return x; 
	}
	
	public double getY() {
		return y+DUMMY_HEIGHT;
	}
	
	
	public double getHeight() {
		return  DUMMY_HEIGHT;
	}
	
	public double getWidth() {
		return 0;
	}

}