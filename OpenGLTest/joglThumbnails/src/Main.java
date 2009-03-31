

/*
 * .Main
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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

//Java imports
import java.awt.image.BufferedImage;

import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import com.sun.opengl.util.texture.Texture;

//Third-party libraries
import omero.ServerError;
import omero.client;
import omero.api.GatewayPrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Pixels;
import omerogateway.OmeroGateway;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

//Application-internal dependencies

import browser.ThumbnailBrowser;


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
 * @since 3.0-Beta4
 */
public class Main 
{
	JFrame				frame;
	ThumbnailBrowser 	thumbnailBrowser;
	OmeroGateway		omeroGateway;
	
	public static void main(String[] args) 
	{
		new Main().run(args);
	}

	private void run(String[] args) 
	{
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		frame = new JFrame("Thumbnail Browser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buildUI();
		frame.setSize(800, 600);
		frame.show();
	}
	
	private void buildUI() 
	{
		try
		{
			omeroGateway = createGateway("localhost", "root", "ome");
			thumbnailBrowser = new ThumbnailBrowser(omeroGateway);
			frame.getContentPane().add(thumbnailBrowser.getBrowserView());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
  	
	private OmeroGateway createGateway(String server, String user, String password) 
	throws CannotCreateSessionException, PermissionDeniedException, ServerError
	{
		return new OmeroGateway(server, user, password);
	}
}

