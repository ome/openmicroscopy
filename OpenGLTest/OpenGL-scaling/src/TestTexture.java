/*
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */


import java.awt.Point;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import omero.ServerError;
import omero.client;
import omero.api.GatewayPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.SessionPrx;
import sun.awt.image.IntegerInterleavedRaster;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.texture.*;

/** Demonstrates simple use of the TextureIO texture loader. */

public class TestTexture  
{
  
	private JSlider zSlider;
	private JSlider tSlider;
	private Texture texture;
	private GLU glu = new GLU();
	private JFrame frame;
	private ImageViewerGL imagePanel; 
	private client client;
	private GatewayPrx gateway;
	private ServiceFactoryPrx session;
	
	private BufferedImage img;
	private Pixels 	  pixels;
	
	
	public static void main(String[] args) 
	{
		new TestTexture().run(args);
	}

	private void run(String[] args) 
	{
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		frame = new JFrame("ImageViewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 
		try
		{
			createGateway();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		buildUI();
		
		frame.setSize(800, 600);
		frame.show();
		try {
			imagePanel.setImage(11136);
			//imagePanel.getPlane(0,0);
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void createGateway() throws CannotCreateSessionException, 
										PermissionDeniedException, 
										ServerError
	{
		client = new client("warlock.openmicroscopy.org.uk");
		session = client.createSession("root", "ome");
		gateway = session.createGateway();
	}
	
	private void buildUI()
	{
		imagePanel = new ImageViewerGL(gateway);
		frame.getContentPane().add(imagePanel);
	}
  	
}
