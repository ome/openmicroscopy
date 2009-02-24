
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
import java.awt.Point;
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
	
	public BufferedImage getPlane(int z, int t) throws ServerError
	{
		int[] data = gateway.getRenderedImage(pixels.getId().getValue(), 
													zSlider.getValue(), 
													tSlider.getValue());
		DataBuffer j2DBuf = new DataBufferInt(data, 
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
			 	                0x0000ff00, // Green
				  	                0x000000ff // Blue
				  	          );
			img = new BufferedImage(colorModel, raster, false, null);
			}
		else
			img.setData(raster);
		
		return img;
				  	
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
		add(panel);
		add(tSlider);
		loadNewPlane = false;
		texture = null;
	}

	public void zoom(int r)
	{
		long a  = System.currentTimeMillis();
		imagePanel.zoom(-Math.signum(r)*0.1f);
		imagePanel.repaint();
		System.err.println("scaling time (ms) : "+(System.currentTimeMillis()-a));
	}

	
	public void stateChanged(ChangeEvent arg0) 
	{
		System.err.println("getUsedMemory : " + getUsedMemory());
		try {
			long a  = System.currentTimeMillis();
			imagePanel.setTexture(getPlane(zSlider.getValue(), tSlider.getValue()));
			imagePanel.repaint();
			System.err.println("retrieval time (ms) : "+(System.currentTimeMillis()-a));
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void mouseWheelMoved(MouseWheelEvent arg0) {
		System.err.println("getUsedMemory : " + getUsedMemory());
		zoom(arg0.getWheelRotation());
	}
	
	
}

