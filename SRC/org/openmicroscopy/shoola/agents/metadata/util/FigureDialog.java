/*
 * org.openmicroscopy.shoola.agents.metadata.util.FigureDialog 
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
package org.openmicroscopy.shoola.agents.metadata.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingView;

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.ColorListRenderer;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.DrawingComponent;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;
import pojos.ChannelData;

/** 
 * Modal dialog displaying option to create a figure of a collection of 
 * images. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FigureDialog 
	extends JDialog
	implements ActionListener, ChangeListener, DocumentListener, 
	PropertyChangeListener
{

	/** Indicates that the dialog is for a split view. */
	public static final int			SPLIT = 0;
	
	/** Indicates that the dialog is for a split view and ROI. */
	public static final int			SPLIT_ROI = 1;
	
	/** Bound property indicating to create a split view figure. */
	public static final String		SPLIT_FIGURE_PROPERTY = "splitFigure";
	
	/** Bound property indicating to create a split view figure with ROI. */
	public static final String		SPLIT_FIGURE_ROI_PROPERTY = 
		"splitFigureROI";
	
	/** Bound property indicating to close the dialog. */
	public static final String		CLOSE_FIGURE_PROPERTY = "closeFigure";
	
	/** Action id indicating to close the dialog. */
	public static final int 		CLOSE = 0;
	
	/** Action id indicating to create a movie. */
	public static final int 		SAVE = 1;
	
	/** Action id indicating to allow the modification of the scale bar. */
	private static final int 		SCALE_BAR = 2;
	
	/** Default text describing the compression check box.  */
    private static final String		PROJECTION_DESCRIPTION = 
    				"Select the type of projection.";
    
    /** The possible options for row names. */
    private static final String[]	ROW_NAMES;

    /** The possible options for row names. */
    private static final String[]	MAGNIFICATION;
    
    /** Index to <code>100%</code>. */
    private static final int		ZOOM_100 = 0;
    
    /** Index to <code>200%</code>. */
    private static final int		ZOOM_200 = 1;
    
    /** Index to <code>300%</code>. */
    private static final int		ZOOM_300 = 2;
    
    /** Index to <code>400%</code>. */
    private static final int		ZOOM_400 = 3;
    
    /** Index to <code>500%</code>. */
    private static final int		ZOOM_500 = 4;
    
	static {
		ROW_NAMES = new String[3];
		ROW_NAMES[FigureParam.IMAGE_NAME] = "Image's name";
		ROW_NAMES[FigureParam.DATASET_NAME] = "Datasets";
		ROW_NAMES[FigureParam.TAG_NAME] = "Tags";
		MAGNIFICATION = new String[5];
		MAGNIFICATION[ZOOM_100] = "100%";
		MAGNIFICATION[ZOOM_200] = "200%";
		MAGNIFICATION[ZOOM_300] = "300%";
		MAGNIFICATION[ZOOM_400] = "400%";
		MAGNIFICATION[ZOOM_500] = "500%";
	}
	
	/** The name to give to the figure. */
	private JTextField 						nameField;
	
	/** Component to select the z-section interval. */
	private TextualTwoKnobsSlider			zRange;
	
	/** Button to close the dialog. */
	private JButton							closeButton;
	
	/** Button to save the result. */
	private JButton							saveButton;

	/** The supported movie formats. */
	private JComboBox						formats;
	
	/** The type of supported projections. */
    private JComboBox						projectionTypesBox;
    
    /** The type of supported projections. */
    private JRadioButton					splitPanelColor;
    
    /** The type of supported projections. */
    private JRadioButton					splitPanelGrey;
    
	/** The type of projection. */
	private Map<Integer, Integer> 			projectionTypes;

    /** Sets the stepping for the mapping. */
    private JSpinner			   			projectionFrequency;
    
    /** The possible options for naming the rows. */
    private JComboBox						rowName;
      
    /** Components displaying the image. */
    private Map<Integer, FigureComponent> components;
    
    /** List of channel buttons. */
    private List<ChannelComponent>			channelList;
    
	/** Option chosen by the user. */
	private int								option;
	
	/** Reference to the renderer.  */
	private Renderer 						renderer;
	
	/** The default plate object. */
	private PlaneDef 						pDef;

	/** The component displaying the merger image. */
	private FigureCanvas					mergeCanvas;
	
	/** The image with all the active channels. */
	private BufferedImage					mergeImage;
 
	/** The width of a thumbnail. */
	private int 							thumbnailWidth;

	/** The height of a thumbnail. */
	private int 							thumbnailHeight;

	/** The width of the image. */
	private NumericalTextField				widthField;
	
	/** The height of the image. */
	private NumericalTextField				heightField;
	
	/** The supported value of the scale bar. */
	private NumericalTextField				scaleBar;
	
	/** Add a scale bar if selected. */
	private JCheckBox						showScaleBar;
	
	/** The selected color for scale bar. */
	private JComboBox						colorBox;
	
	/** The index of the dialog. One of the constants. */
	private int								index;
	
	/** The number of z-sections. */
	private int								maxZ;
	
	/** The components hosting the channel components. */
	private JXTaskPane						channelsPane;

	/** The component hosting the canvas. */
	private JLayeredPane					pane;
	
	/** Component hosting the ROI. */
	private ROIComponent					roiComponent;

	/** 
	 * The drawing component to create drawing, view and editor and link them.
	 */
	private DrawingComponent 				drawingComponent;
	
	/** The size of the thumbnail. */
	private Dimension						size;
	
	/** The magnification factor. */
	private JComboBox						zoomBox;
	
	/**
	 * Sets the channel selection.
	 * 
	 * @param channel   The selected channel.
	 * @param active	Pass <code>true</code> to set the channel active,
	 * 					<code>false</code> otherwise.
	 */
	private void setChannelSelection(int channel, boolean active)
	{
		renderer.setActive(channel, active);
		mergeImage = getMergedImage();
		mergeCanvas.setImage(mergeImage);
		Iterator<ChannelComponent> i = channelList.iterator();
		ChannelComponent btn;
        List<Integer> actives = renderer.getActiveChannels();
        int index;
        while (i.hasNext()) {
			btn = i.next();
			index = btn.getChannelIndex();
			btn.setSelected(actives.contains(index));
		}
        boolean grey = splitPanelGrey.isSelected();

        FigureComponent comp = components.get(channel);
        if (active) {
        	if (grey) comp.resetImage(grey);
        	else comp.resetImage(!active);
        } else comp.resetImage(!active);
	}
	
	/**
	 * Returns the merged image.
	 * 
	 * @return See above.
	 */
	private BufferedImage getMergedImage()
	{
		return scaleImage(renderer.renderPlane(pDef));
	}
	
	/**
	 * Scales the passed image.
	 * 
	 * @param image The image to scale down.
	 * @return See above.
	 */
	private BufferedImage scaleImage(BufferedImage image)
	{
		return Factory.scaleBufferedImage(image, size.width, 
				size.height);
	}
	
	/**
	 * Returns the image corresponding to the passed index.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	private BufferedImage getChannelImage(int index)
	{
		//merge image is RGB
		if (renderer.isChannelActive(index)) {
			if (renderer.isMappedImageRGB(renderer.getActiveChannels())) {
				//if red
				DataBuffer buf = mergeImage.getRaster().getDataBuffer();
				if (renderer.isColorComponent(Renderer.RED_BAND, index)) {
					return Factory.createBandImage(buf,
							size.width, size.height, 
							Factory.RED_MASK, Factory.BLANK_MASK,
							Factory.BLANK_MASK);
				} else if (renderer.isColorComponent(Renderer.GREEN_BAND, 
						index)) {
					return Factory.createBandImage(buf,
							size.width, size.height,  
							Factory.BLANK_MASK, Factory.GREEN_MASK, 
							Factory.BLANK_MASK);
				} else if (renderer.isColorComponent(Renderer.BLUE_BAND, 
						index)) {
					return Factory.createBandImage(buf,
							size.width,  size.height, 
							Factory.BLANK_MASK, Factory.BLANK_MASK,
							Factory.BLUE_MASK);
				}
			} else { //not rgb 
				return scaleImage(renderer.createSingleChannelImage(true, index, 
						pDef));
			}
		}
		//turn off all other channels, create an image and reset channels
		return scaleImage(renderer.createSingleChannelImage(true, index, 
				pDef));
	}
	
	/** Initializes the components. */
	private void initialize()
	{
		size = Factory.computeThumbnailSize(thumbnailWidth, thumbnailHeight, 
        		renderer.getPixelsDimensionsX(), 
        		renderer.getPixelsDimensionsY());
		pDef = new PlaneDef();
		pDef.t = renderer.getDefaultT();
		pDef.z = renderer.getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		widthField = new NumericalTextField(0, renderer.getPixelsDimensionsX());
		widthField.setColumns(5);
		widthField.setText(""+renderer.getPixelsDimensionsX());
		heightField = new NumericalTextField(0, 
				renderer.getPixelsDimensionsY());
		heightField.setColumns(5);
		heightField.setText(""+renderer.getPixelsDimensionsY());
		
		widthField.getDocument().addDocumentListener(this);
		heightField.getDocument().addDocumentListener(this);
		mergeCanvas = new FigureCanvas();
		mergeImage = getMergedImage();
		mergeCanvas.setPreferredSize(new Dimension(thumbnailWidth, 
				thumbnailHeight));
		mergeCanvas.setImage(mergeImage);
	}
	
	private double getMagnificationFactor()
	{
		int maxY = renderer.getPixelsDimensionsY();
		int maxX = renderer.getPixelsDimensionsX();
		if (maxX > thumbnailWidth || maxY >thumbnailHeight) {
			double ratioX = (double) thumbnailWidth/maxX;
			double ratioY = (double) thumbnailHeight/maxY;
			if (ratioX < ratioY) return ratioX;
			return ratioY;
		}
		return -1;
	}
	
	/** Initializes the ROI channels components. */
	private void initChannelROIComponents()
	{
		initialize();
		//draw the roi.
		//Determine the scaling factor.
		zoomBox = new JComboBox(MAGNIFICATION);
		DrawingView canvasView = drawingComponent.getDrawingView();
		double factor = getMagnificationFactor();
		if (factor != -1)
			canvasView.setScaleFactor(factor);
		Coord3D c = new Coord3D(renderer.getDefaultZ(), renderer.getDefaultT());
		try {
			ShapeList list = roiComponent.getShapeList(c);
			ROIFigure figure;
			Drawing drawing = drawingComponent.getDrawing();
			if (list != null) {
				TreeMap map = list.getList();
				Iterator i = map.values().iterator();
				ROIShape shape;
				while (i.hasNext()) {
					shape = (ROIShape) i.next();
					if (shape != null) {
						figure = shape.getFigure();
						//canvasView.addToSelection(figure);
						drawing.add(figure);
					}
				}
				drawingComponent.getDrawingView().setDrawing(drawing);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		
		
		
		List<ChannelData> data = renderer.getChannelData();
        ChannelData d;
        //ChannelToggleButton item;
        ChannelButton item;
        Iterator<ChannelData> k = data.iterator();
        List<Integer> active = renderer.getActiveChannels();
        int j;


        k = data.iterator();
        channelList = new ArrayList<ChannelComponent>();
        ChannelComponent comp;
        while (k.hasNext()) {
        	d = k.next();
			j = d.getIndex();
			comp = new ChannelComponent(j, renderer.getChannelColor(j), 
					active.contains(j));
			channelList.add(comp);
			comp.addPropertyChangeListener(this);
		}
	}
	
	/** Initializes the channels components. */
	private void initChannelComponents()
	{
		initialize();
		components = new LinkedHashMap<Integer, FigureComponent>();
		//Initializes the channels
		List<ChannelData> data = renderer.getChannelData();
        ChannelData d;
        //ChannelToggleButton item;
        ChannelButton item;
        Iterator<ChannelData> k = data.iterator();
        List<Integer> active = renderer.getActiveChannels();
        FigureComponent split;
        int j;
        while (k.hasNext()) {
			d = k.next();
			j = d.getIndex();
			split = new FigureComponent(this, renderer.getChannelColor(j), 
					d.getChannelLabeling(), j);
			split.setOriginalImage(getChannelImage(j));
			split.setCanvasSize(thumbnailWidth, thumbnailHeight);
			if (!active.contains(j))
				split.resetImage(true);
			components.put(j, split);
		}

        k = data.iterator();
        channelList = new ArrayList<ChannelComponent>();
        ChannelComponent comp;
        while (k.hasNext()) {
        	d = k.next();
			j = d.getIndex();
			comp = new ChannelComponent(j, renderer.getChannelColor(j), 
					active.contains(j));
			channelList.add(comp);
			comp.addPropertyChangeListener(this);
		}
	}
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param name The default name of the file.
	 */
	private void initComponents(String name)
	{	
		pane = new JLayeredPane();
		thumbnailHeight = Factory.THUMB_DEFAULT_HEIGHT;
		thumbnailWidth = Factory.THUMB_DEFAULT_WIDTH;	
		closeButton = new JButton("Cancel");
		closeButton.setToolTipText(UIUtilities.formatToolTipText(
				"Close the window."));
		closeButton.setActionCommand(""+CLOSE);
		closeButton.addActionListener(this);
		saveButton = new JButton("Create");
		saveButton.setEnabled(false);
		saveButton.setToolTipText(UIUtilities.formatToolTipText(
				"Create a figure."));
		saveButton.setActionCommand(""+SAVE);
		saveButton.addActionListener(this);
		nameField = new JTextField();
		String s = UIUtilities.removeFileExtension(name);
		if (s != null) {
			nameField.setText(s);
			saveButton.setEnabled(true);
		}
		nameField.getDocument().addDocumentListener(this);
		Map<Integer, String> map = FigureParam.FORMATS;
		String[] f = new String[map.size()];
		Entry entry;
		Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			f[(Integer) entry.getKey()] = (String) entry.getValue();
		}
		formats = new JComboBox(f);
		zRange = new TextualTwoKnobsSlider(1, maxZ, 1, maxZ);
		zRange.layoutComponents();
		zRange.setEnabled(maxZ > 1);
		String[] names = new String[ProjectionParam.PROJECTIONS.size()];
        int index = 0;
        
        i = ProjectionParam.PROJECTIONS.entrySet().iterator();
        projectionTypes = new HashMap<Integer, Integer>();
        int j;
        while (i.hasNext()) {
        	entry = (Entry) i.next();
			j = (Integer) entry.getKey();
			projectionTypes.put(index, j);
			names[index] = (String) entry.getValue();
			index++;
		}
        rowName = new JComboBox(ROW_NAMES); 
        projectionTypesBox = new JComboBox(names);
        projectionTypesBox.setToolTipText(PROJECTION_DESCRIPTION);
        
		projectionFrequency = new JSpinner(new SpinnerNumberModel(1, 1, maxZ+1, 
				1));
		
		ButtonGroup group = new ButtonGroup();
		splitPanelGrey = new JRadioButton("Grey");
		splitPanelColor = new JRadioButton("Color");
		splitPanelColor.addChangeListener(this);
		splitPanelGrey.addChangeListener(this);
		group.add(splitPanelGrey);
		group.add(splitPanelColor);
		splitPanelColor.setSelected(true);
		
        showScaleBar = new JCheckBox("Scale Bar");
		showScaleBar.setFont(showScaleBar.getFont().deriveFont(Font.BOLD));
		showScaleBar.setActionCommand(""+SCALE_BAR);
		showScaleBar.addActionListener(this);
		scaleBar = new NumericalTextField();
		scaleBar.setText(""+EditorUtil.DEFAULT_SCALE);
		
        colorBox = new JComboBox();
		Map<Color, String> colors = EditorUtil.COLORS_BAR;
		Object[][] cols = new Object[colors.size()][2];
		index = 0;
		i = colors.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			cols[index] = new Object[]{entry.getKey(), entry.getValue()};
			index++;
		}
		
		colorBox.setModel(new DefaultComboBoxModel(cols));	
		colorBox.setSelectedIndex(cols.length-1);
		colorBox.setRenderer(new ColorListRenderer());
        
		showScaleBar.setSelected(false);
		scaleBar.setEnabled(false);
        
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { close(); }
		});
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel("Split View Figure",
				"Create a Split View Figure.", 
				"The figure will be saved to the server.", 
				icons.getIcon(IconManager.SPLIT_VIEW_48));
		Container c = getContentPane();
		c.setLayout(new BorderLayout(5, 5));
		c.add(tp, BorderLayout.NORTH);
		c.add(buildBody(), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** 
	 * Builds and lays out the control.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.add(closeButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(saveButton);
		bar.add(Box.createHorizontalStrut(20));
		JPanel p = UIUtilities.buildComponentPanelRight(bar);
		p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return p;
	}
	
	/**
	 * Builds and lays out the components displaying the dimensions.
	 * 
	 * @return See above.
	 */
	private JPanel buildDimensionComponent()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridy = 0;
		c.gridx = 0;
        c.weightx = 0.0;  
        p.add(new JLabel("Width: "), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        p.add(widthField, c);  
        c.gridx++;
        p.add(new JLabel("pixels"), c); 
        c.gridx = 0;
        c.gridy++;
        p.add(new JLabel("Height: "), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        p.add(heightField, c); 
        c.gridx++;
        p.add(new JLabel("pixels"), c); 
		return UIUtilities.buildComponentPanel(p);
	}

	/**
	 * Builds the projection component.
	 * 
	 * @return See above.
	 */
	private JPanel buildProjectionComponent()
	{
		JPanel p = new JPanel();
		/*
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.PREFERRED, 
			TableLayout.PREFERRED, TableLayout.PREFERRED,
			TableLayout.PREFERRED}, //columns
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}; //rows
		p.setLayout(new TableLayout(tl));
		int i = 0;
        p.add(UIUtilities.setTextFont("Intensity"), "0, "+i+"");
        p.add(projectionTypesBox, "1, "+i);
        p.add(UIUtilities.setTextFont("Every n-th slice"), "2, "+i+"");
        p.add(projectionFrequency, "3, "+i);
        i = i+2;
        p.add(UIUtilities.setTextFont("Z-sections Range"), "0, "+i+"");
        p.add(UIUtilities.buildComponentPanel(zRange), "1, "+i+", 4, "+i);
        */
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		p.add(UIUtilities.setTextFont("Intensity"), c);
		c.gridx++;
		p.add(projectionTypesBox, c);
		c.gridx++;
		p.add(UIUtilities.setTextFont("Every n-th slice"), c);
		c.gridx++;
	    p.add(projectionFrequency, c);
	    c.gridy++;
		c.gridx = 0;
	    p.add(UIUtilities.setTextFont("Z-sections Range"), c);
	    c.gridx++;
        p.add(UIUtilities.buildComponentPanel(zRange), c);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/**
	 * Builds the component offering name and formats options.
	 * 
	 * @return See above.
	 */
	private JPanel buildTypeComponent()
	{
		JPanel p = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.PREFERRED, 
			TableLayout.PREFERRED, TableLayout.PREFERRED,
			TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 
				5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED,
				5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED,
				5, TableLayout.PREFERRED}}; //rows
		p.setLayout(new TableLayout(tl));
		int i = 0;
        p.add(UIUtilities.setTextFont("Name"), "0, "+i+"");
        p.add(nameField, "1, "+i+", 4, "+i);
        i = i+2;
        p.add(UIUtilities.setTextFont("Format"), "0, "+i+"");
        p.add(formats, "1, "+i);
        
        i = i+2;
        p.add(UIUtilities.setTextFont("Image Label"), "0, "+i+"");
        p.add(rowName, "1, "+i);
        i = i+2;
        p.add(showScaleBar, "0, "+i);
        p.add(scaleBar, "1, "+i);
        p.add(new JLabel("microns"), "2, "+i);
        i = i+2;
        p.add(UIUtilities.setTextFont("Overlay color"), "0, "+i);
        p.add(UIUtilities.buildComponentPanel(colorBox), "1, "+i);
		return p;
	}
	
	/** 
	 * Builds and lays out the component displaying the channels
	 * 
	 * @return See above
	 */
	private JPanel buildChannelsROIComponent()
	{
		
		/*
		Iterator<Integer> i = components.keySet().iterator();
		while (i.hasNext()) {
			p.add(components.get(i.next()));
		}
		*/
		//Add the final one
	
		JPanel splitPanel = new JPanel();
		splitPanel.add(UIUtilities.setTextFont("Split Panel"));
		splitPanel.add(splitPanelColor);
		splitPanel.add(splitPanelGrey);
		
		JPanel zoomPanel = new JPanel();
		zoomPanel.add(UIUtilities.setTextFont("Zoom"));
		zoomPanel.add(zoomBox);
		
		JPanel p = new JPanel();
		p.add(buildMergeComponent());
		p.add(Box.createHorizontalStrut(5));
		
		JPanel controls = new JPanel();
		double size[][] = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
			TableLayout.PREFERRED, 5, TableLayout.PREFERRED}};
		controls.setLayout(new TableLayout(size));
		int i = 0;
		controls.add(splitPanel, "0, "+i+", LEFT, CENTER");
		i = i+2;
		controls.add(zoomPanel, "0, "+i+", LEFT, CENTER");
		i = i+2;
		controls.add(p, "0, "+i);
		i = i+2;
		controls.add(buildDimensionComponent(), "0, "+i);
		return controls;
	}
	
	/** 
	 * Builds and lays out the component displaying the channels.
	 * 
	 * @return See above
	 */
	private JPanel buildChannelsComponent()
	{
		JPanel p = new JPanel();
		Iterator<Integer> i = components.keySet().iterator();
		while (i.hasNext()) {
			p.add(components.get(i.next()));
		}
		//Add the final one
	
		JPanel splitPanel = new JPanel();
		splitPanel.add(UIUtilities.setTextFont("Split Panel"));
		splitPanel.add(splitPanelColor);
		splitPanel.add(splitPanelGrey);
		
		//content.add(p);
		p.add(Box.createHorizontalStrut(5));
		p.add(buildMergeComponent());
		
		JPanel controls = new JPanel();
		double size[][] = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
				TableLayout.PREFERRED}};
		controls.setLayout(new TableLayout(size));
		controls.add(splitPanel, "0, 0, LEFT, CENTER");
		controls.add(p, "0, 2");
		controls.add(buildDimensionComponent(), "0, 4");
		return controls;
	}
	
	/**
	 * Builds the component displaying the merge image.
	 * 
	 * @return See above.
	 */
	private JPanel buildMergeComponent()
	{
		JComponent comp = mergeCanvas;
		if (index == SPLIT_ROI) {
			JComponent c = drawingComponent.getDrawingView();
			Dimension d = mergeCanvas.getPreferredSize();
			c.setSize(d);
			c.setPreferredSize(d);
			mergeCanvas.setSize(d);
			pane.setPreferredSize(d);
			pane.setSize(d);
			pane.add(mergeCanvas, new Integer(0));
			pane.add(c, new Integer(1));
			comp = pane;
		}
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		Iterator<ChannelComponent> i = channelList.iterator();
		while (i.hasNext()) {
			buttonPanel.add(i.next());
			buttonPanel.add(Box.createHorizontalStrut(5));
		}
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(0, 0));
		JPanel f = UIUtilities.buildComponentPanelCenter(buttonPanel);
		f.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		p.add(f, BorderLayout.NORTH);
		p.add(UIUtilities.buildComponentPanelCenter(comp), 
				BorderLayout.CENTER);
		return p;
	}
	
	/**
	 * Builds the main component.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		double[][] tl = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED,
			TableLayout.PREFERRED}}; //rows
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		p.setLayout(new TableLayout(tl));
		JXTaskPane pane = EditorUtil.createTaskPane("General");
		pane.setCollapsed(false);
		pane.add(buildTypeComponent());
		int i = 0;
		p.add(pane, "0, "+i);
		if (maxZ > 1) {
			pane = EditorUtil.createTaskPane("Projection");
			pane.add(buildProjectionComponent());
			i++;
			p.add(pane, "0, "+i);
		}
		i++;
		channelsPane = EditorUtil.createTaskPane("Channels");
		channelsPane.setCollapsed(false);
		channelsPane.add(buildDefaultPane());
		p.add(channelsPane, "0, "+i);
		return p;
	}
	
	/**
	 * Builds the default component.
	 * 
	 * @return See above.
	 */
	private JPanel buildDefaultPane()
	{
		JPanel p = new JPanel();
		JXBusyLabel label = new JXBusyLabel();
		label.setBusy(true);
		p.add(label);
		return p;
	}
	
	/** Closes the dialog. */
	private void close()
	{
		option = CLOSE;
		firePropertyChange(CLOSE_FIGURE_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
		setVisible(false);
		dispose();
	}
	
	/**
	 * Collects the parameters.
	 * 
	 * @param p The value to fill.
	 */
	private void collectParam(FigureParam p)
	{
		p.setWidth((Integer) widthField.getValueAsNumber());
		p.setHeight((Integer) heightField.getValueAsNumber());
		p.setSplitGrey(splitPanelGrey.isSelected());
		
		//scale bar
		int scale = -1;
		if (showScaleBar.isSelected()) {
			Number n = scaleBar.getValueAsNumber();
			if (n != null) scale = n.intValue();
		}
		p.setScaleBar(scale);
		index = colorBox.getSelectedIndex();
		
		Map<Color, String> m = EditorUtil.COLORS_BAR;
		Iterator i = m.entrySet().iterator();
		int j = 0;
		Entry entry;
		Color c = null;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			if (j == index) c = (Color) entry.getKey();
			j++;
		}
		p.setColor(c);
		//projection
		p.setZStart(zRange.getStartValue()-1);
		p.setZEnd(zRange.getEndValue()-1);
		p.setStepping((Integer) projectionFrequency.getValue());
		p.setProjectionType(
				projectionTypes.get(projectionTypesBox.getSelectedIndex()));
	}
	
	/** Collects the parameters to create a figure. */
	private void saveSplitFigure()
	{
		Map<Integer, String> split = new LinkedHashMap<Integer, String>();
		FigureComponent comp;
		Entry entry;
		Iterator i = components.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			comp = (FigureComponent) entry.getValue();
			if (comp.isSelected()) {
				split.put((Integer) entry.getKey(), comp.getChannelLabel());
			}
		}
		Map<Integer, Color> merge = new LinkedHashMap<Integer, Color>();
		List<Integer> active = renderer.getActiveChannels();
		i = active.iterator();
		int index;
		while (i.hasNext()) {
			index = (Integer) i.next();
			merge.put(index, renderer.getChannelColor(index));
		}
		
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		int label = rowName.getSelectedIndex();
		FigureParam p = new FigureParam(format, name, split, merge, label);
		
		collectParam(p);
		close();
		firePropertyChange(SPLIT_FIGURE_PROPERTY, null, p);
	}
	
	/** Collects the parameters to create a ROI figure. */
	private void saveROIFigure()
	{
		Map<Integer, String> split = new LinkedHashMap<Integer, String>();	
		Map<Integer, Color> merge = new LinkedHashMap<Integer, Color>();
		List<Integer> active = renderer.getActiveChannels();
		Iterator i = active.iterator();
		int index;
		while (i.hasNext()) {
			index = (Integer) i.next();
			merge.put(index, renderer.getChannelColor(index));
			split.put(index, ""+index);
		}
		
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		int label = rowName.getSelectedIndex();
		FigureParam p = new FigureParam(format, name, split, merge, label);
		p.setIndex(FigureParam.SPLIT_VIEW_ROI);
		collectParam(p);
		double zoom = 1;
		switch (zoomBox.getSelectedIndex()) {
			case ZOOM_200:
				zoom = 2;
				break;
			case ZOOM_300:
				zoom = 3;
				break;
			case ZOOM_400:
				zoom = 4;
				break;
			case ZOOM_500:
				zoom = 5;
		}
		p.setMagnificationFactor(zoom);
		
		close();
		firePropertyChange(SPLIT_FIGURE_ROI_PROPERTY, null, p);
	}
	
	/** Collects the parameters to create a figure. */
	private void save()
	{
		switch (index) {
			case SPLIT:
				saveSplitFigure();
				break;
			case SPLIT_ROI:
				saveROIFigure();
		}
	}
	
	/** 
	 * Sets the enabled flag of the {@link #saveButton} depending on
	 * the value to the name field.
	 */
	private void handleText()
	{
		String text = nameField.getText();
		saveButton.setEnabled(!(text == null || text.trim().length() == 0));
	}
	
	/** 
	 * Handles the changes in the image dimension. 
	 * 
	 * @param field The modified numerical field.
	 */
	private void handleDimensionChange(NumericalTextField field)
	{
		Integer n = (Integer) field.getValueAsNumber();
		if (n == null) return;
		Document doc;
		int v;
		if (field == widthField) {
			v = (int) ((n*renderer.getPixelsDimensionsY())/
					renderer.getPixelsDimensionsX());
			doc = heightField.getDocument();
			doc.removeDocumentListener(this);
			heightField.setText(""+v);
			doc.addDocumentListener(this);
		} else {
			v = (int) ((n*renderer.getPixelsDimensionsX())/
					renderer.getPixelsDimensionsY());
			doc = widthField.getDocument();
			doc.removeDocumentListener(this);
			widthField.setText(""+v);
			doc.addDocumentListener(this);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param name  The default name for the file.
	 * @param maxZ 	The number of z-sections.
	 */
	public FigureDialog(JFrame owner, String name, int maxZ)
	{
		super(owner, true);
		this.maxZ = maxZ;
		initComponents(name);
		buildGUI();
		setSize(500, 700);
	}

	/**
	 * Sets the dialog index.
	 * 
	 * @param index The value to set.
	 */
	public void setIndex(int index) { this.index = index; }
	
	/**
	 * Creates and returns a greyScale image with only the selected channel
	 * turned on.
	 * 
	 * @param channel The index of the channel.
	 * @return See above.
	 */
	BufferedImage createSingleGreyScaleImage(int channel)
	{
		return scaleImage(renderer.createSingleChannelImage(false, channel, 
				pDef));
	}
	
	/**
	 * Sets the renderer.
	 * 
	 * @param renderer 	Reference to the renderer.
	 */
	public void setRenderer(Renderer renderer)
	{
		this.renderer = renderer;
		channelsPane.removeAll();
		switch (index) {
			case SPLIT:
				initChannelComponents();
				channelsPane.add(buildChannelsComponent());
				break;
			case SPLIT_ROI:
				initChannelROIComponents();
				channelsPane.add(buildChannelsROIComponent());
				break;
		}
		saveButton.setEnabled(true);
		pack();
	}
	
	/**
	 * Sets the collection of ROIs related to the primary select.
	 * 
	 * @param rois The value to set.
	 */
	public void setROIs(Collection rois)
	{
		if (rois == null) return;
		drawingComponent = new DrawingComponent();
		drawingComponent.getDrawingView().setScaleFactor(1.0);
		roiComponent = new ROIComponent();
		Iterator r = rois.iterator();
		ROIResult result;
		try {
			while (r.hasNext()) {
				result = (ROIResult) r.next();
				roiComponent.loadROI(result.getROIs(), true);
			}
		} catch (Exception e) {}
	}
	
    /**
     * Centers and shows the dialog. Returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    public int centerDialog()
    {
    	UIUtilities.centerAndShow(this);
    	return option;	
    }
    
	/**
	 * Closes or creates a figure.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLOSE:
				close();
				break;
			case SAVE:
				save();
				break;
			case SCALE_BAR:
				scaleBar.setEnabled(showScaleBar.isSelected());
		}
	}

	/**
	 * Sets the <code>enabled</code> flag of the controls.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{ 
		Document doc = e.getDocument();
		if (doc == nameField.getDocument())
			handleText(); 
		else if (doc == widthField.getDocument()) 
			handleDimensionChange(widthField);
		else handleDimensionChange(heightField);
	}

	/**
	 * Sets the <code>enabled</code> flag of the controls.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{ 
		Document doc = e.getDocument();
		if (doc == nameField.getDocument())
			handleText(); 
		else if (doc == widthField.getDocument()) 
			handleDimensionChange(widthField);
		else handleDimensionChange(heightField);
	}
	
	/**
	 * Listens to channel button selection
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ChannelButton.CHANNEL_SELECTED_PROPERTY.equals(name)) {
			//mergeCanvas.setImage(getMergedImage());
			Map map = (Map) evt.getNewValue();
			if (map == null) return;
			if (map.size() != 1) return;
			Set set = map.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Integer index;
			ChannelButton obj = (ChannelButton) evt.getSource();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				index = (Integer) entry.getKey();
				setChannelSelection(index.intValue(), 
						((Boolean) entry.getValue()));
			}
		} else if (ChannelComponent.CHANNEL_SELECTION_PROPERTY.equals(name)) {
			switch (index) {
				case SPLIT:
					ChannelComponent c = (ChannelComponent) evt.getNewValue();
					setChannelSelection(c.getChannelIndex(), c.isActive());
					break;
				case SPLIT_ROI:
					
			}
			
		}
	}
	
	/**
	 * Reacts to change in the type of split i.e. either grey or color.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		boolean grey = splitPanelGrey.isSelected();
		if (components == null) return;
		Iterator<Integer> i = components.keySet().iterator();
		FigureComponent comp;
		List active = renderer.getActiveChannels();
		Integer index;
		while (i.hasNext()) {
			index = i.next();
			comp = components.get(index);
			if (grey) comp.resetImage(grey);
			else {
				if (active.contains(index)) comp.resetImage(grey);
				else comp.resetImage(!grey);
			}
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}


}
