
/*
 * .ImageViewerGL
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.nio.Buffer;
import java.nio.IntBuffer;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

import omero.ServerError;
import omero.api.GatewayPrx;
import omero.model.Pixels;
import sun.awt.image.IntegerInterleavedRaster;

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
 * @since 3.0-Beta4
 */
public class ImageViewerGL
	extends JPanel implements ChangeListener, MouseWheelListener
{
	private Pixels	pixels;
	private GatewayPrx gateway;
	private JSlider zSlider;
	private JSlider tSlider;
	private Texture texture;
	private boolean loadNewPlane;
	private ImagePanel imagePanel;
	private BufferedImage img;
	
	private long	meanRT = 0;
	private long 	numRT = 0;
	/**
	 * Get the free memory available in the system.
	 * @return see above.
	 */
	public long getFreeMemory()
	{
		Runtime r = Runtime.getRuntime();
    	return r.freeMemory();
	}
	
	/**
	 * Get the total memory available to the JVM.
	 * @return see above.
	 */
	public  long getTotalMemory()
	{
		Runtime r = Runtime.getRuntime();
		return r.totalMemory();
	}
	
	/**
	 * Return the amount of memory used in JVM.
	 * @return see above.
	 */
	public  long getUsedMemory()
	{
		return getTotalMemory()-getFreeMemory();
	}
	
	
	public ImageViewerGL(GatewayPrx gateway)
	{
		this.gateway = gateway;
		buildUI();
	}
	
	public void setImage(long pixelsId) throws ServerError
	{
		pixels = gateway.getPixels(pixelsId);
		zSlider.setValue(0);
		zSlider.setMaximum(pixels.getSizeZ().getValue()-1);
		tSlider.setValue(0);
		tSlider.setMaximum(pixels.getSizeT().getValue()-1);
	}
	
	public BufferedImage getPlaneAsBufferedImage(int z, int t) throws ServerError
	{
		long a  = System.currentTimeMillis();
	/*	int[] data = gateway.renderAsPackedIntAsRGBA(pixels.getId().getValue(), 
		
													zSlider.getValue(), 
													tSlider.getValue());*/
		int[] dataARGB = gateway.getRenderedImage(pixels.getId().getValue(), 
				zSlider.getValue(), 
				tSlider.getValue());
		long rt = (System.currentTimeMillis()-a);
			meanRT = meanRT + rt;
		numRT = numRT+1;
		
		System.err.println("retrieval time (ms) : "+ rt);
		System.err.println("MEAN retrieval time (ms) : "+ (double)meanRT/(double)numRT);
		/*int[] newData = new int[512*512];
		for(int x = 0 ; x < 512*512; x++)
			{
			Color c = new Color(data[x]);
			int r = (int)(data[x]>>24)&0xff;
			int g = (int)(data[x]>>16)&0xff;
			int b = (int)(data[x]>>8)&0xff;
			//System.err.println(r);
			//System.err.println(g);
			//System.err.println(b);
			Color newC = new Color( r, g,b );
			newData[x] = newC.getRGB();
			}*/
/*		int diff = 0;
		for( int x =0 ; x < 512*512; x++)
			diff= diff + (dataARGB[x]-newData[x]);
		System.err.println(diff);*/
		
		a  = System.currentTimeMillis();
		DataBuffer j2DBuf = new DataBufferInt(dataARGB, 
										(int)(pixels.getSizeX().getValue() 
												* pixels.getSizeY().getValue()), 0);
		SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(
					                DataBuffer.TYPE_INT, pixels.getSizeX().getValue()
					                , pixels.getSizeY().getValue(), pixels.getSizeX().getValue(), 
					                new int[] {
					                        0x00ff0000, // Red
					                        0x0000ff00, // Green
					                        0x000000ff, // Blue
					                });
		
		WritableRaster raster = new IntegerInterleavedRaster(sampleModel,
					                j2DBuf, new Point(0, 0));
	
		
		if(img==null)
			{
			ColorModel colorModel = new DirectColorModel(24, 0x00ff0000, // Red
			 	                0x00ff00, // Green
				  	                0x0000ff);
			img = new BufferedImage(colorModel, raster, false, null);
			}
		else
			img.setData(raster);
	//	System.err.println(data.length);
		System.err.println(pixels.getSizeX().getValue());
		System.err.println(pixels.getSizeY().getValue());
	//	IntBuffer b = IntBuffer.wrap(data);
	//	IntBuffer b2 = IntBuffer.wrap(dataARGB);
	
/*		TextureData textureData = new TextureData(GL.GL_RGBA, pixels.getSizeX().getValue(), 
			pixels.getSizeY().getValue(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_INT_8_8_8_8, false,
				false, false, b, null );*/
		System.err.println("Texture time (ms) : "+(System.currentTimeMillis()-a));
				
//		TextureData textureData = new TextureData(
		//Texture texture = TextureIO.newTexture(textureData);
//		return textureData;
		return img;
				  	
	}

	public TextureData getPlane(int z, int t) throws ServerError
	{
		long a  = System.currentTimeMillis();
		/*int[] data = gateway.renderAsPackedIntAsRGBA(pixels.getId().getValue(), 
		
													zSlider.getValue(), 
													tSlider.getValue());*/
		int[] dataARGB = gateway.getRenderedImage(pixels.getId().getValue(), 
				zSlider.getValue(), 
				tSlider.getValue());
		long rt = (System.currentTimeMillis()-a);
		meanRT = meanRT + rt;
		numRT = numRT+1;
		
		System.err.println("retrieval time (ms) : "+ rt);
		System.err.println("MEAN retrieval time (ms) : "+ (double)meanRT/(double)numRT);
		/*int[] newData = new int[512*512];
		for(int x = 0 ; x < 512*512; x++)
			{
			Color c = new Color(data[x]);
			int r = (int)(data[x]>>24)&0xff;
			int g = (int)(data[x]>>16)&0xff;
			int b = (int)(data[x]>>8)&0xff;
			//System.err.println(r);
			//System.err.println(g);
			//System.err.println(b);
			Color newC = new Color( r, g,b );
			newData[x] = newC.getRGB();
			}*/
/*		int diff = 0;
		for( int x =0 ; x < 512*512; x++)
			diff= diff + (dataARGB[x]-newData[x]);
		System.err.println(diff);*/
		
		/*DataBuffer j2DBuf = new DataBufferInt(dataARGB, 
										(int)(pixels.getSizeX().getValue() 
												* pixels.getSizeY().getValue()), 0);
		SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(
					                DataBuffer.TYPE_INT, pixels.getSizeX().getValue()
					                , pixels.getSizeY().getValue(), pixels.getSizeX().getValue(), 
					                new int[] {
					                        0x00ff0000, // Red
					                        0x0000ff00, // Green
					                        0x000000ff, // Blue
					                });
		
		WritableRaster raster = new IntegerInterleavedRaster(sampleModel,
					                j2DBuf, new Point(0, 0));
	
		
		if(img==null)
			{
			ColorModel colorModel = new DirectColorModel(24, 0x00ff0000, // Red
			 	                0x00ff00, // Green
				  	                0x0000ff);
			img = new BufferedImage(colorModel, raster, false, null);
			}
		else
			img.setData(raster);*/
	//	System.err.println(data.length);
		System.err.println(pixels.getSizeX().getValue());
		System.err.println(pixels.getSizeY().getValue());
		//IntBuffer b = IntBuffer.wrap(data);
		IntBuffer b2 = IntBuffer.wrap(dataARGB);
	
		a = System.currentTimeMillis();
		TextureData textureData = new TextureData(GL.GL_RGBA, pixels.getSizeX().getValue(), 
			pixels.getSizeY().getValue(), 0, GL.GL_BGRA, GL.GL_UNSIGNED_INT_8_8_8_8_REV, false,
				false, false, b2, null );
		System.err.println("Texture time (ms) : "+(System.currentTimeMillis()-a));
				
//		TextureData textureData = new TextureData(
		//Texture texture = TextureIO.newTexture(textureData);
//		return textureData;
		return textureData;
				  	
	}

	
	public void buildUI()
	{
		zSlider = new JSlider(JSlider.VERTICAL);
		zSlider.setMaximum(0);
		tSlider = new JSlider(JSlider.HORIZONTAL);
		tSlider.setMaximum(0);
		tSlider.addChangeListener(this);
		zSlider.addChangeListener(this);
		
		imagePanel = new ImagePanel();
		imagePanel.addMouseWheelListener(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(zSlider);
		panel.add(imagePanel);
		imagePanel.setPreferredSize(new Dimension(512,512));
		imagePanel.setMinimumSize(new Dimension(512,512));
		imagePanel.setMaximumSize(new Dimension(512,512));
		add(panel);
		add(tSlider);
		loadNewPlane = false;
		texture = null;
	}

	public void zoom(double r)
	{
		long a  = System.currentTimeMillis();
		imagePanel.zoom(r);
		imagePanel.repaint();
		System.err.println("scaling time (ms) : "+(System.currentTimeMillis()-a));
	}

	
	public void stateChanged(ChangeEvent arg0) 
	{
		System.err.println("getUsedMemory : " + getUsedMemory());
		try {
			//getPlaneAsBufferedImage(zSlider.getValue(), tSlider.getValue());
			//getPlane(zSlider.getValue(), tSlider.getValue());
			//imagePanel.setTextureAsBufferedImage(getPlaneAsBufferedImage(zSlider.getValue(), tSlider.getValue()));
			imagePanel.setTexture(getPlane(zSlider.getValue(), tSlider.getValue()));
			imagePanel.repaint();
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void mouseWheelMoved(MouseWheelEvent arg0) {
		System.err.println("getUsedMemory : " + getUsedMemory());
		System.err.println("arg0 : " + arg0);
		double v = Math.exp((double)arg0.getWheelRotation());
		System.err.println("val : " + v);
		zoom(v);
	}
	
	
}

