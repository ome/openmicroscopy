/*
 * org.openmicroscopy.shoola.env.ui.UIFactory
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

package org.openmicroscopy.shoola.env.ui;



//Java imports
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Factory for the various windows and widgets used within the container.
 * Other utility methods (to load images, format tooltips, etc.) are also
 * included.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class UIFactory 
{

	/**
	 * Creates the splash screen that is used at initialization.
	 * 
	 * @return	The splash screen.
	 */
	public static SplashScreen makeSplashScreen()
	{
		return new SplashScreenProxy();
	}
	
	/**
	 * Creates the {@link TopFrame}.
	 * 
	 * @param c	Reference to the singleton {@link Container}.
	 * @return	The {@link TopFrame}.
	 */
	public static TopFrame makeTopFrame(Container c)
	{
		return new TopFrameImpl(c);
	}
	
	/**
	 * Creates the {@link UserNotifier}. Should only be used at initialization 
	 * time.
	 */
	public static UserNotifier makeUserNotifier()
	{
		return new UserNotifierImpl();
	}
	
	/**
	 * Creates the {@link UserNotifier}.
	 * 
	 * @param tf	Reference to the {@link TopFrame}. 
	 * @param reg	reference to the {@link Registry}.
	 * @return	The {@link UserNotifier}.
	 */
	public static UserNotifier makeUserNotifier(TopFrame tf, Registry reg)
	{
		return new UserNotifierImpl((TopFrameImpl) tf, reg);
	}
	
	/** 
	 * Utility factory method to create an icon from a file.
	 *
	 * @param path    The path of the icon file relative to this class.
	 * @return  An instance of {@link javax.swing.Icon Icon} or
	 * 			<code>null</code> if the path was invalid.
	 */
	public static Icon createIcon(String path)
	{
		URL location = UIFactory.class.getResource(path);
		ImageIcon icon = null;
		if (location != null)	icon = new ImageIcon(location);
		return icon;
	}
	
	/** 
	 * Utility factory method to build a tool tip in a fixed font and color.
	 * Pass the tool tip text and get back an <i>HTML</i> string to be
	 * passed, in turn, to the <code>setToolTipText</code> method of a 
	 * {@link javax.swing.JComponent}.
	 *
	 * @param   toolTipText     The textual content of the tool tip.
	 * @return  An <i>HTML</i> fomatted string to be passed to 
	 * 			<code>setToolTipText()</code>.
	 */
	public static String formatToolTipText(String toolTipText) 
	{
		StringBuffer buf = new StringBuffer(90+toolTipText.length());
		buf.append("<html><body bgcolor=#FFFCB7 text=#AD5B00>");
		//TODO: change into platform independent font
		buf.append("<font face=Arial size=2>");  
		buf.append(toolTipText);
		buf.append("</font></body></html>");
		return buf.toString();
	} 
	
}
