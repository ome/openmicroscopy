/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.examples.viewer;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.util.image.geom.Factory;

import omero.api.RenderingEnginePrx;
import omero.romio.PlaneDef;
import omero.romio.RegionDef;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;
import sun.awt.image.IntegerInterleavedRaster;

/** 
 * Displays the image and controls.
 * Thanks to  Galli Vanni vanni.galli@supsi.ch to add controls for turning
 * channels on and off.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class ViewerPane 
	extends JPanel
	implements ChangeListener
{
	
	/** The compression level. */
	private static final float  COMPRESSION = 0.5f;
	
	/** The red mask. */
	private static final int	RED_MASK = 0x00ff0000;
	
	/** The green mask. */
	private static final int	GREEN_MASK = 0x0000ff00;
	
	/** The blue mask. */
	private static final int	BLUE_MASK = 0x000000ff;
	
	/** The RGB masks. */
	private static final int[]	RGB = {RED_MASK, GREEN_MASK, BLUE_MASK};
	
	/** Reference to the rendering engine. */
	private RenderingEnginePrx engine;
	
	/** The slider to select the z-section. */
	private JSlider zSlider;
	
	/** The slider to select the z-section. */
	private JSlider tSlider;
	
	/** Box indicating to render the image as compressed or not. */
	private JCheckBox compressed;
	
	/** The image canvas. */
	private ImageCanvasInterface canvas;
	
	/** The image currently viewed. */
	private ImageData image;
	
	/** JPanel displaying all the available channels **/
	private JPanel channelsPane;
	
	/** Number of channels of the image **/
	private int channelsNumber;
	
	/** Indicates that the channels are selected or not. */
	private JCheckBox[] channels;
	
	private double factor = 0.25;
	
	/**
	 * Creates a buffer image from the specified <code>array</code> of 
	 * integers.
	 * 
	 * @param buf	The array to handle.
	 * @param bits	The number of bits in the pixel values.
	 * @param sizeX	The width (in pixels) of the region of image data described.
	 * @param sizeY	The height (in pixels) of the region of image data 
	 * 				described.
	 * @return See above.
	 */
    private BufferedImage createImage(int[] buf, int bits, int sizeX, 
			int sizeY)
    {
    	if (buf == null) return null;
    	DataBuffer j2DBuf = new DataBufferInt(buf, sizeX*sizeY); 
		SinglePixelPackedSampleModel sampleModel =
					new SinglePixelPackedSampleModel(
						DataBuffer.TYPE_INT, sizeX, sizeY, sizeX, RGB);
		WritableRaster raster = 
		new IntegerInterleavedRaster(sampleModel, j2DBuf, 
					new Point(0, 0));
		
		ColorModel colorModel = new DirectColorModel(bits, RGB[0],   
												RGB[1], RGB[2]);
		BufferedImage image =
			new BufferedImage(colorModel, raster, false, null);
		image.setAccelerationPriority(1f);
		return image;
    }
    
	/** Renders a plane. */
	private void render()
	{
		try {
			int sizeX = image.getDefaultPixels().getSizeX();
            int sizeY = image.getDefaultPixels().getSizeY();
			PlaneDef pDef = new PlaneDef();
			pDef.t = engine.getDefaultT();
			pDef.z = engine.getDefaultZ();
			pDef.slice = omero.romio.XY.value;
			//now render the image. possible to render it compressed or not
			//not compressed
			BufferedImage img = null;
			if (!compressed.isSelected()) {
				int[] buf = engine.renderAsPackedInt(pDef);
				img = createImage(buf, 32, sizeX, sizeY);
			} else {
				byte[] values = engine.renderCompressed(pDef);
				ByteArrayInputStream stream = new ByteArrayInputStream(values);
				img = ImageIO.read(stream);
				img.setAccelerationPriority(1f);
			}
             canvas.setImage(img);
		} catch (Exception e) {
		}
	}
	
	/** 
	 * Renders a region. 
	 * 
	 * @param region The region to render
	 */
	private void renderRegion(Rectangle region)
	{
		try {
			PlaneDef pDef = new PlaneDef();
			pDef.t = engine.getDefaultT();
			pDef.z = engine.getDefaultZ();
			pDef.slice = omero.romio.XY.value;
			int factor = 4;
			pDef.region = new RegionDef(region.x*factor, region.y*factor, 
					region.width*factor, region.height*factor);
			//now render the image. possible to render it compressed or not
			//not compressed
			BufferedImage img = null;
			if (!compressed.isSelected()) {
				int[] buf = engine.renderAsPackedInt(pDef);
				img = createImage(buf, 32, pDef.region.width, pDef.region.height);
			} else {
				byte[] values = engine.renderCompressed(pDef);
				ByteArrayInputStream stream = new ByteArrayInputStream(values);
				img = ImageIO.read(stream);
				img.setAccelerationPriority(1f);
			}
			canvas.setImage(Factory.magnifyImage(5, img)); //for testing purpose only
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setSelection(Point p)
	{
		
	}
	
	/** 
	 * Initializes the components. 
	 * @param viewType The type of viewer to display the image, JAVA, or 
	 * Processing.
	 */
	private void initComponents(ViewType viewType)
	{
		compressed = new JCheckBox("Compressed Image");
		compressed.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				render();
			}
		});
		switch(viewType)
		{
			case JAVA:
				canvas = new ImageCanvas();
		}
		zSlider = new JSlider();
		zSlider.setMinimum(0);
		zSlider.setEnabled(false);
		zSlider.addChangeListener(this);
		tSlider = new JSlider();
		tSlider.setMinimum(0);
		tSlider.setEnabled(false);
		tSlider.addChangeListener(this);
	}
	
	/**
	 * Builds the channel component.
	 * 
	 * @param n The number of channels.
	 */
	private void buildChannelsPane(int n)
	{		
		try
		{
			channelsPane.removeAll();
			remove(channelsPane);
			
			channels = new JCheckBox[n];
			
			for (int i = 0; i < n; i ++)
			{
				channels[i] = new JCheckBox("Channel " + i);
				channels[i].setSelected(true);         
				channels[i].addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						setActiveChannels(e);
					}
				});
				
				channelsPane.add(channels[i]);
			}

			add(channelsPane);
		}
		catch (Exception e)
		{
			
		}
	}
	
	/**
	 * Sets the channel active or not.
	 * 
	 * @param evt The event to handle.
	 */
	private void setActiveChannels(ActionEvent evt)
	{
		try
		{
			for (int i = 0; i < channelsNumber; i ++)
			{
				engine.setActive(i, channels[i].isSelected());
			}
			render();
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
	}
	
	/** Builds and lays out the component. */
	private void buildGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT));
		row.add(new JLabel("Z"));
		row.add(zSlider);
		row.add(Box.createHorizontalStrut(5));
		row.add(new JLabel("T"));
		row.add(tSlider);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(row);
		JPanel content = new JPanel();
		content.setLayout(new FlowLayout(FlowLayout.LEFT));
		content.add(compressed);
		p.add(content);
		
		channelsPane = new JPanel();
		channelsPane.setLayout(new FlowLayout(FlowLayout.LEFT));

		p.add(channelsPane);
		
		JPanel pp = new JPanel();
		pp.setLayout(new FlowLayout(FlowLayout.LEFT));
		pp.add(p);
		add(new JScrollPane(canvas.getCanvas()));
		add(pp);
	}
	
	/** Creates a new instance. 
	 * @param viewType The type of viewer used to display the image, Java or 
	 * processing.
	 */
	ViewerPane(ViewType viewType)
	{
		initComponents(viewType);
		buildGUI();
	}
	
	/**
	 * Sets the rendering engine.
	 * 
	 * @param image The image.
	 * @param engine The engine.
	 */
	void setRenderingControl(ImageData image, RenderingEnginePrx engine)
	{
		this.engine = engine;
		this.image = image;
		try {
			this.engine.setCompressionLevel(COMPRESSION);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		PixelsData pixels = image.getDefaultPixels();
		int sizeX = pixels.getSizeX();
		int sizeY = pixels.getSizeY();
		Dimension d = new Dimension(sizeX, sizeY);
		canvas.setCanvasSize(d);

		zSlider.removeChangeListener(this);
		tSlider.removeChangeListener(this);
		zSlider.setMaximum(pixels.getSizeZ());
		zSlider.setEnabled(pixels.getSizeZ() > 1);
		tSlider.setMaximum(pixels.getSizeT());
		tSlider.setEnabled(pixels.getSizeT() > 1);
		try {
			zSlider.setValue(engine.getDefaultZ()+1);
			tSlider.setValue(engine.getDefaultT()+1);
		} catch (Exception e) {
			// TODO: handle exception
		}
		zSlider.addChangeListener(this);
		tSlider.addChangeListener(this);
		// number of channels in the image (RGB)
		channelsNumber = image.getDefaultPixels().getSizeC();		
		buildChannelsPane(channelsNumber);
		render();
	}

	/** Sets the z-section or time-point. */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		try {
			if (src == zSlider) {
				engine.setDefaultZ(zSlider.getValue()-1);
				render();
			} else if (src == tSlider) {
				engine.setDefaultT(tSlider.getValue()-1);
				render();
			}
		} catch (Exception e2) {
			
		}
	}

}
