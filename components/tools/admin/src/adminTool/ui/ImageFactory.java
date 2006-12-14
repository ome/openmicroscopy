/*
 * src.adminTool.ui.ImageFactory 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package src.adminTool.ui;

import javax.swing.ImageIcon;

import src.adminTool.model.ExceptionHandler;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
public class ImageFactory 
{

	public static int	SPLASH_SCREEN = 0;
	public static int 	SERVER_CONNECT_TRYING = 1;
	public static int 	SERVER_CONNECTED = 2;
	public static int 	SERVER_CONNECT_FAILED = 3;
	public static int 	ADD_USER = 4;
	public static int 	REMOVE_USER = 5;
	public static int	LEFT_ARROW = 6;
	public static int   RIGHT_ARROW = 7;
	public static int   DEFAULT_GROUP = 8;
	 
	private static int          MAX_IMAGES = 8;
	    
    private static String[]     path = new String[MAX_IMAGES+1];
	    
    static 
	{
    	path[SPLASH_SCREEN] = "/resources/graphx/AdminSplash.png";
    	path[SERVER_CONNECT_TRYING] = "/resources/graphx/server_trying16.png";
    	path[SERVER_CONNECTED] = "/resources/graphx/server_connect16.png";
    	path[SERVER_CONNECT_FAILED] = "/resources/graphx/server_disconn16.png";
    	path[ADD_USER] = "/resources/graphx/addUser_kusers_nuvola48_mod3.png";
    	path[REMOVE_USER] = "/resources/graphx/RemoveUser_kusers_nuvola48_mod3.png";
    	path[LEFT_ARROW] = "/resources/graphx/1leftarrow_nuvola32.png";
    	path[RIGHT_ARROW] = "/resources/graphx/1leftarrow_nuvola32.png";
    	path[DEFAULT_GROUP] = "/resources/graphx/kgpg_identity_nuvola32.png";
	}
	
	private static ImageFactory ref;

	private ImageFactory()
	{
	
	}

	public static ImageFactory get()
	{
		if (ref == null)
	    	ref = new ImageFactory();		
    	return ref;
	}

	public Object clone()
		throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException(); 
	}
	
	public ImageIcon image(int icon)
	{
		return new ImageIcon(ImageFactory.class.getResource(path[icon]));
	}
	
}


