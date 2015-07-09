/*
 * org.openmicroscopy.shoola.agents.metadata.util.FigureDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Drawing;


import omero.gateway.model.ROIResult;
//Application-internal dependencies
import omero.model.PlaneInfo;
import omero.romio.PlaneDef;
import ome.model.units.BigResult;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.agents.util.ui.ScriptingDialog;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.ColorListRenderer;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.DrawingComponent;
import org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView;
import org.openmicroscopy.shoola.util.ui.lens.LensComponent;
import org.openmicroscopy.shoola.util.ui.slider.GridSlider;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;

import pojos.ChannelData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.TagAnnotationData;

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
	
	/** Indicates that the dialog is for a movie figure. */
	public static final int			MOVIE = 2;
	
	/** Indicates that the dialog is for a thumbnails figure. */
	public static final int			THUMBNAILS = 3;
	
	/** Indicates that the dialog is ROI movie figure. */
	public static final int			ROI_MOVIE = 4;
	
	/** Bound property indicating to create a split view figure. */
	public static final String		CREATE_FIGURE_PROPERTY = "createFigure";
	
	/** Bound property indicating to close the dialog. */
	public static final String		CLOSE_FIGURE_PROPERTY = "closeFigure";
	
	/** Action id indicating to close the dialog. */
	public static final int 		CLOSE = 0;
	
	/** Action id indicating to create a movie. */
	public static final int 		SAVE = 1;

	/** Action id indicating to allow the modification of the scale bar. */
	private static final int 		SCALE_BAR = 2;
	
	/** Action id indicating to arrange the thumbnails by tags. */
	private static final int 		ARRANGE_BY_TAGS = 3;
	
	/** Action id indicating to turn on or off the projection's controls. */
	private static final int 		PROJECTION = 4;
	
	/** Action id indicating a change in the magnification factor. */
	private static final int 		ZOOM_FACTOR = 5;
	
	/** Action id indicating that the color has changed. */
	private static final int 		COLOR_SELECTION = 7;
	
	/** Indicates to download the script. */
	private static final int 		DOWNLOAD = 8;
	
	/** Indicates to view the script. */
	private static final int 		VIEW = 9;
	
	/** The maximum number of time-points visible at a time. */
	private static final int		MAX_CELLS = 20;
	
	/** The default text for the movie. */
	private static final String		FRAMES_TEXT = "Number of frames: ";
	
	/** The default text for the movie. */
	private static final String		MAGNIFICATION_TEXT = "Magnification x";

	/** Default text describing the compression check box.  */
    private static final String		PROJECTION_DESCRIPTION = 
    				"Select the type of projection.";
    
    /** The default text of thumbnails per row. */
    private static final String		ITEMS_PER_ROW_TEXT = "Images per row";
    
    /** The default number of thumbnails per row. */
    private static final int		ITEMS_PER_ROW = 10;
    
    /** The height of the component displaying the available tags. */
    private static final int		MAX_HEIGHT = 150;
    
    /** The stroke width of the ROI displayed on the image. */
    private static final double		STROKE_WIDTH = 2.5;
    
    /** The possible options for row names. */
    private static final String[]	ROW_NAMES;

    /** The possible options for row names. */
    private static final String[]	MAGNIFICATION;
    
    /** Index to <code>100%</code> magnification. */
    private static final int		ZOOM_100 = 0;
    
    /** Index to <code>200%</code> magnification. */
    private static final int		ZOOM_200 = 1;
    
    /** Index to <code>300%</code> magnification. */
    private static final int		ZOOM_300 = 2;
    
    /** Index to <code>400%</code> magnification. */
    private static final int		ZOOM_400 = 3;
    
    /** Index to <code>500%</code> magnification. */
    private static final int		ZOOM_500 = 4;
    
    /** Index to <code>Auto</code>. */
    private static final int		ZOOM_AUTO = 5;
    
    /** Index corresponding to a <code>24x24</code> thumbnail. */
    private static final int		SIZE_24 = 0;
    
    /** Index corresponding to a <code>32x32</code> thumbnail. */
    private static final int		SIZE_32 = 1;
    
    /** Index corresponding to a <code>48x48</code> thumbnail. */
    private static final int		SIZE_48 = 2;
    
    /** Index corresponding to a <code>64x64</code> thumbnail. */
    private static final int		SIZE_64 = 3;
    
    /** Index corresponding to a <code>96x96</code> thumbnail. */
    private static final int		SIZE_96 = 4;
    
    /** Index corresponding to a <code>128x128</code> thumbnail. */
    private static final int		SIZE_128 = 5;
    
    /** Index corresponding to a <code>160x160</code> thumbnail. */
    private static final int		SIZE_160 = 6;
    
    /** The size available for thumbnails creation. */
    private static final String[]	SIZE_OPTIONS;

	static {
		ROW_NAMES = new String[3];
		ROW_NAMES[FigureParam.IMAGE_NAME] = "Image's name";
		ROW_NAMES[FigureParam.DATASET_NAME] = "Datasets";
		ROW_NAMES[FigureParam.TAG_NAME] = "Tags";
		MAGNIFICATION = new String[6];
		MAGNIFICATION[ZOOM_100] = "100%";
		MAGNIFICATION[ZOOM_200] = "200%";
		MAGNIFICATION[ZOOM_300] = "300%";
		MAGNIFICATION[ZOOM_400] = "400%";
		MAGNIFICATION[ZOOM_500] = "500%";
		MAGNIFICATION[ZOOM_AUTO] = "Zoom To Fit";
		SIZE_OPTIONS = new String[7];
		SIZE_OPTIONS[SIZE_24] = "24x24";
		SIZE_OPTIONS[SIZE_32] = "32x32";
		SIZE_OPTIONS[SIZE_48] = "48x48";
		SIZE_OPTIONS[SIZE_64] = "64x64";
		SIZE_OPTIONS[SIZE_96] = "96x96";
		SIZE_OPTIONS[SIZE_128] = "128x128";
		SIZE_OPTIONS[SIZE_160] = "160x160";
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
    
	/** Option chosen by the user. */
	private int								option;
	
	/** Reference to the renderer.  */
	private Renderer 						renderer;
	
	/** The default plate object. */
	private PlaneDef 						pDef;

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
	private int								dialogType;
	
	/** The pixels set of reference. */
	private PixelsData 						pixels;
	
	/** The components hosting the channel components. */
	private JXTaskPane						channelsPane;

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
	
	/** The size of thumbnails. */
	private JComboBox						sizeBox;
	
	/** The number of items. */
	private NumericalTextField				numberPerRow;
	
	/** Indicates to create a figure with the displayed objects. */
	private JRadioButton					displayedObjects;
	
	/** Indicates to create a figure with the selected objects. */
	private JRadioButton					selectedObjects;
	
	/** The type of objects to handle. */
	private Class							type;
	
	/** Indicates to arrange thumbnails by tags. */
	private JCheckBox						arrangeByTags;
	
	/** Indicates to include images w/o tags. */
	private JCheckBox						includeUntagged;
	
	/**
	 * The component displaying the controls to create the thumbnails figure.
	 */
	private JPanel							thumbnailsPane;
	
	/** The map containing the selected tags. */
	private Map<JCheckBox, TagAnnotationData> tagsSelection;
	
	/** Use to sort data objects.*/
	private ViewerSorter 					sorter;
	
	/** The component displaying the collection of selected tags. */
	private JPanel							selectedTags;
	
	/** The selection of tags. */
	private List<JCheckBox>					selection;
	
	/** Determines the time-points frequency for the movie figure. */
    private JSpinner			   			movieFrequency;
    
    /** The slider displaying the number of time-points. */
    private GridSlider						movieSlider;
    
    /** Indicates to select the last view Z-section. */
    private JRadioButton					planeSelection;
    
    /** Indicates to select project the stack. */
    private JRadioButton					projectionBox;
    
    /** The time of options. */
	private JComboBox						timesBox;
	
	/** Label used to display various info e.g. number of frames for movie. */
	private JLabel							generalLabel;
	
	/** This component is only be used for the Split ROI figure. */
	private LensComponent					lens;
	
	/** The merged image not scaled. */
	private BufferedImage					mergeUnscaled;
	
	/** The ROI box. */
	private Rectangle2D						roiBox;

	/** The default figure. */
	private FigureComponent					mergedComponent;
	
	/** Copy of the rendering definition. */
	private RndProxyDef						rndDef;
	
	/** The ROIs currently displayed on the image. */
	private List<ROI> 						displayedROIs;
	
	/** The original scaling factor for the ROI. */
	private double							scalingFactor;

	/** The menu offering various options to manipulate the script. */
	private JPopupMenu 						optionMenu;
	
	/** Menu offering the ability to download or view the script. */
	private JButton 						menuButton;
	
	/**
	 * Turns off controls if the binary data are not available.
	 */
	private void checkBinaryAvailability()
	{
		if (!MetadataViewerAgent.isBinaryAvailable())
			saveButton.setEnabled(false);
	}
	
	/** 
	 * Creates the option menu.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createOptionMenu()
	{
		if (optionMenu != null) return optionMenu;
		optionMenu = new JPopupMenu();
		optionMenu.add(createMenuItem("Download", DOWNLOAD));
		optionMenu.add(createMenuItem("View", VIEW));
		return optionMenu;
	}
	
	/**
	 * Creates a menu item.
	 * 
	 * @param text The text of the button.
	 * @param actionID The action command id.
	 * @return See above.
	 */
	private JMenuItem createMenuItem(String text, int actionID)
    {
	    JMenuItem b = new JMenuItem(text);
		b.setActionCommand(""+actionID);
		b.addActionListener(this);
		return b;
    }
	
	/**
	 * Returns the selected color or <code>null</code>.
	 * 
	 * @return See above.
	 */
	private String getSelectedColor()
	{
		int index = colorBox.getSelectedIndex();
		Map<Color, String> m = EditorUtil.COLORS_BAR;
		Iterator<Entry<Color, String>> i = m.entrySet().iterator();
		int j = 0;
		Entry<Color, String> entry;
		while (i.hasNext()) {
			entry = i.next();
			if (j == index) return entry.getValue();
			j++;
		}
		return null;
	}
	
	/**
	 * Returns the selected color or <code>null</code>.
	 * 
	 * @return See above.
	 */
	private Color getColor()
	{
		int index = colorBox.getSelectedIndex();
		Map<Color, String> m = EditorUtil.COLORS_BAR;
		Iterator<Entry<Color, String>> i = m.entrySet().iterator();
		int j = 0;
		Entry<Color, String> entry;
		while (i.hasNext()) {
			entry =  i.next();
			if (j == index) return entry.getKey();
			j++;
		}
		return null;
	}
	
	/** Modifies the color of the ROIs. */
	private void modifyROIDisplay()
	{
		if (displayedROIs == null) return;
		Color c = getColor();
		if (c == null) return;
		Entry<Coord3D, ROIShape> entry;
		Iterator<ROI> ro;
		TreeMap<Coord3D, ROIShape> shapes;
		Iterator<Entry<Coord3D, ROIShape>> k;
		ROIShape shape;
		ROI roi;
		ROIFigure fig;
		ro = displayedROIs.iterator();
		while (ro.hasNext()) {
			roi = (ROI) ro.next();
			shapes = roi.getShapes();
			k = shapes.entrySet().iterator();
			while (k.hasNext()) {
				entry = k.next();
				shape = entry.getValue();
				fig = shape.getFigure();
				AttributeKeys.STROKE_WIDTH.set(fig, STROKE_WIDTH);
				AttributeKeys.STROKE_COLOR.set(fig, c);
			}
		}
	}
	
	/** Displays the magnification factor. */
	private void setFactor()
	{
		int v = zoomBox.getSelectedIndex();
		if (v == ZOOM_AUTO) {
			int h = (Integer) heightField.getValueAsNumber();
			float f = (float) h/(float) roiBox.getHeight();
			float ff = (float) (Math.round(f*100)/100.0);
			lens.setZoomFactor(ff);
			generalLabel.setText(MAGNIFICATION_TEXT+ff);
		} else {
			v++;
			generalLabel.setText(MAGNIFICATION_TEXT+v);
			lens.setZoomFactor(v);
		}
	}
	
	/** Modifies the lens factor. */
	private void setLensFactor()
	{
		setFactor();
		//reset 
		Iterator<Entry<Integer, FigureComponent>>
		k = components.entrySet().iterator();
		Entry<Integer, FigureComponent> entry;
		FigureComponent fc;
		int j;
		List<Integer> active = renderer.getActiveChannels();
		int w, h;
		BufferedImage img;
        while (k.hasNext()) {
        	entry =  k.next();
        	j = entry.getKey();
			fc = entry.getValue();
			lens.setPlaneImage(renderer.createSingleChannelImage(true, j, 
					pDef));
			img = lens.getZoomedImage();
			if (img != null) {
				w = img.getWidth()*size.width/pixels.getSizeX();
				h = img.getHeight()*size.height/pixels.getSizeY();
				if (w != 0 && h != 0) {
					fc.setOriginalImage(Factory.scaleBufferedImage(img, w, h));
					fc.setCanvasSize(w, h);
					fc.revalidate();
					if (!active.contains(j))
						fc.resetImage(true);
				}
			}
		}
	}
	
	/** 
	 * Lays out the selected tags. 
	 * 
	 * @param selectedTag Control to select or not the tags.
	 */
	private void layoutSelectedTags(JCheckBox selectedTag)
	{
		selectedTags.removeAll();
		if (selection == null) selection = new ArrayList<JCheckBox>();
		if (selection.contains(selectedTag))
			selection.remove(selectedTag);
		else selection.add(selectedTag);
		
		Iterator<JCheckBox>	i = selection.iterator();
		JCheckBox box;
		int index = 1;
		JLabel label;
		TagAnnotationData tag;
		while (i.hasNext()) {
			box = i.next();
			label = new JLabel();
			tag = tagsSelection.get(box);
			label.setText(index+". "+tag.getTagValue());
			selectedTags.add(label);
			index++;
		}
		selectedTags.revalidate();
		selectedTags.repaint();
	}

	/**
	 * Returns the merged image.
	 * 
	 * @return See above.
	 */
	private BufferedImage getMergedImage()
	{
		mergeUnscaled = renderer.renderPlane(pDef);
		return scaleImage(mergeUnscaled);
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
	 * @param scale Pass <code>true</code> to scale down the image, 
	 * 				<code>false</code> otherwise.
	 * @return See above.
	 */
	private BufferedImage getChannelImage(int index, boolean scale)
	{
		//merge image is RGB
		if (renderer.isChannelActive(index)) {
			if (renderer.isMappedImageRGB(renderer.getActiveChannels())) {
				//if red
				DataBuffer buf = null;
				if (!scale) buf = mergeUnscaled.getRaster().getDataBuffer();
				else {
					BufferedImage image = mergedComponent.getDisplayedImage();
					if (image != null)
						buf = image.getRaster().getDataBuffer();
				}
				if (buf == null) 
					return scaleImage(renderer.createSingleChannelImage(true, 
							index, pDef));
				if (renderer.isColorComponent(Renderer.RED_BAND, index)) {
					if (!scale) 
						return Factory.createBandImage(buf,
								mergeUnscaled.getWidth(),
								mergeUnscaled.getHeight(),
								Factory.RED_MASK, Factory.BLANK_MASK,
								Factory.BLANK_MASK);
					
					return Factory.createBandImage(buf,
							size.width, size.height,
							Factory.RED_MASK, Factory.BLANK_MASK,
							Factory.BLANK_MASK);
				} else if (renderer.isColorComponent(Renderer.GREEN_BAND,
						index)) {
					if (!scale) 
						return Factory.createBandImage(buf,
								mergeUnscaled.getWidth(),
								mergeUnscaled.getHeight(),
								Factory.BLANK_MASK, Factory.GREEN_MASK, 
								Factory.BLANK_MASK);
					return Factory.createBandImage(buf,
							size.width, size.height,
							Factory.BLANK_MASK, Factory.GREEN_MASK,
							Factory.BLANK_MASK);
				} else if (renderer.isColorComponent(Renderer.BLUE_BAND,
						index)) {
					if (!scale) 
						return Factory.createBandImage(buf,
								mergeUnscaled.getWidth(), 
								mergeUnscaled.getHeight(), 
								Factory.BLANK_MASK, Factory.BLANK_MASK,
								Factory.BLUE_MASK);
					return Factory.createBandImage(buf,
							size.width,  size.height, 
							Factory.BLANK_MASK, Factory.BLANK_MASK,
							Factory.BLUE_MASK);
				}
			} else { //not rgb 
				if (!scale)
					renderer.createSingleChannelImage(true, index, pDef);
				return scaleImage(renderer.createSingleChannelImage(true, index,
						pDef));
			}
		}
		//turn off all other channels, create an image and reset channels
		if (!scale)
			return renderer.createSingleChannelImage(true, index, pDef);
		return scaleImage(renderer.createSingleChannelImage(true, index,
				pDef));
	}
	
	/** Initializes the components. */
	private void initialize()
	{
		size = Factory.computeThumbnailSize(thumbnailWidth, thumbnailHeight,
        		pixels.getSizeX(), pixels.getSizeY());
		if (pDef == null)
			initPlane(renderer.getDefaultZ(), renderer.getDefaultT());
	}
	
	/**
	 * Returns the magnification factor.
	 * 
	 * @return See above.
	 */
	private double getMagnificationFactor()
	{
		int maxY = pixels.getSizeY();
		int maxX = pixels.getSizeX();
		if (maxX > thumbnailWidth || maxY >thumbnailHeight) {
			double ratioX = (double) thumbnailWidth/maxX;
			double ratioY = (double) thumbnailHeight/maxY;
			if (ratioX < ratioY) return ratioX;
			return ratioY;
		}
		return -1;
	}
	
	/** Initializes the channels components. */
	private void initChannelComponents()
	{
		initialize();
		components = new LinkedHashMap<Integer, FigureComponent>();
		if (dialogType == SPLIT_ROI) {
			initChannelComponentsForROI();
			return;
		} 
		List<ChannelData> data = renderer.getChannelData();
		List<Integer> active = renderer.getActiveChannels();
		Iterator<ChannelData> k = data.iterator();
        List<ChannelButton> buttons = new ArrayList<ChannelButton>();
        ChannelButton comp;
        int j;
        ChannelData d;
        while (k.hasNext()) {
        	d = k.next();
			j = d.getIndex();
			comp = new ChannelButton("", renderer.getChannelColor(j), j,
					active.contains(j));
			comp.setPreferredSize(FigureComponent.DEFAULT_SIZE);
			buttons.add(comp);
			comp.addPropertyChangeListener(this);
		}
        mergedComponent = new FigureComponent(this, buttons);
        mergedComponent.setCanvasSize(thumbnailWidth, thumbnailHeight);
        mergedComponent.setOriginalImage(getMergedImage());
        
		
		//Initializes the channels
		k = data.iterator();
        FigureComponent split;
        while (k.hasNext()) {
			d = k.next();
			j = d.getIndex();
			split = new FigureComponent(this, renderer.getChannelColor(j), 
					d.getChannelLabeling(), j);
			//split.setSelected(active.contains(j));
			split.setSelected(true);
			split.setOriginalImage(getChannelImage(j, true));
			split.setCanvasSize(thumbnailWidth, thumbnailHeight);
			if (!active.contains(j))
				split.resetImage(true);
			components.put(j, split);
		}
	}
	
	/** Initializes the components for the ROI. */
	private void initChannelComponentsForROI()
	{
		zoomBox = new JComboBox(MAGNIFICATION);
		zoomBox.setActionCommand(""+ZOOM_FACTOR);
		zoomBox.addActionListener(this);
		DrawingCanvasView canvasView = drawingComponent.getDrawingView();
		scalingFactor = getMagnificationFactor();
		if (scalingFactor != -1)
			canvasView.setScaleFactor(scalingFactor);
		try {
			Drawing drawing = drawingComponent.getDrawing();
			Coord3D c;
			TreeMap<Long, ROI> map = roiComponent.getROIMap();
			if (map != null && map.size() > 0) {
				Iterator<ROI> i = map.values().iterator();
				ROI roi;
				TreeMap<Coord3D, ROIShape> shapesMap;
				ROIShape shape;
				Iterator<Entry<Coord3D, ROIShape>> j;
				Entry<Coord3D, ROIShape> entry;
				while (i.hasNext()) {
					roi = i.next();
					shapesMap = roi.getShapes();
					j = shapesMap.entrySet().iterator();
					while (j.hasNext()) {
						entry = j.next();
						c = entry.getKey();
						shape = entry.getValue();
						if (shape != null) {
							if (roiBox == null) {
								roiBox = shape.getBoundingBox();
								drawing.add(shape.getFigure());
								initPlane(c.getZSection(),
										c.getTimePoint());
							}
						}
					}
				}
			}
			int rw = (int) roiBox.getWidth();
			int rh = (int) roiBox.getHeight();
			lens = new LensComponent((JFrame) getOwner(), rw, rh);
			lens.setLensLocation((int) roiBox.getX(), (int) roiBox.getY());
			setFactor();
			canvasView.setDrawing(drawing);
		} catch (Exception e) {
			
		}
		List<Integer> active = renderer.getActiveChannels();
		List<ChannelData> data = renderer.getChannelData();
        ChannelData d;
        Iterator<ChannelData> k = data.iterator();
        List<ChannelButton> buttons = new ArrayList<ChannelButton>();
        ChannelButton comp;
        int j;
        while (k.hasNext()) {
        	d = k.next();
			j = d.getIndex();
			comp = new ChannelButton("", renderer.getChannelColor(j), j,
					active.contains(j));
			comp.setPreferredSize(FigureComponent.DEFAULT_SIZE);
			buttons.add(comp);
			comp.addPropertyChangeListener(this);
		}
        mergedComponent = new FigureComponent(this, buttons);
        mergedComponent.setCanvasSize(thumbnailWidth, thumbnailHeight);
        mergedComponent.setOriginalImage(getMergedImage());
        //Add the view to the canvas.
        mergedComponent.addToView(canvasView);
		//Initializes the channels
		k = data.iterator();
        
        FigureComponent split;

        BufferedImage img;
        int w = (int) roiBox.getWidth()*size.width/pixels.getSizeX();
        int h = (int) roiBox.getHeight()*size.height/pixels.getSizeY();
        while (k.hasNext()) {
			d = k.next();
			j = d.getIndex();
			split = new FigureComponent(this, renderer.getChannelColor(j), 
					d.getChannelLabeling(), j);
			split.setSelected(true);
			lens.setPlaneImage(
					renderer.createSingleChannelImage(true, j, pDef));
			img = lens.getZoomedImage();
			if (img != null) {
				w = img.getWidth()*size.width/pixels.getSizeX();
				h = img.getHeight()*size.height/pixels.getSizeY();
				img = Factory.scaleBufferedImage(img, w, h);
				if (img != null) {
					w = img.getWidth();
					h = img.getHeight();
					split.setOriginalImage(img);
					split.setCanvasSize(w, h);
				}
				if (!active.contains(j))
					split.resetImage(true);
			}
			
			components.put(j, split);
		}
	}
	
	/**
	 * Initializes the plane.
	 * 
	 * @param z The selected z-section.
	 * @param t The selected time-point.
	 */
	private void initPlane(int z, int t)
	{
		pDef = new PlaneDef();
		pDef.t = t;
		pDef.z = z;
		pDef.slice = omero.romio.XY.value;
	}
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param name The default name of the file.
	 */
	private void initComponents(String name)
	{	
		IconManager icons = IconManager.getInstance();
		menuButton = new JButton(icons.getIcon(IconManager.BLACK_ARROW_DOWN));
		menuButton.setText("Script");
		menuButton.setHorizontalTextPosition(JButton.LEFT);
		menuButton.addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				Object src = e.getSource();
				if (src instanceof Component) {
					Point p = e.getPoint();
					createOptionMenu().show((Component) src, p.x, p.y);
				}
			}
		});
		sorter = new ViewerSorter();
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
			checkBinaryAvailability();
		}
		nameField.getDocument().addDocumentListener(this);
		Map<Integer, String> map = FigureParam.FORMATS;
		String[] f = new String[map.size()];
		Entry<Integer, String> entry;
		Iterator<Entry<Integer, String>> i = map.entrySet().iterator();
		int index = 0;
		int v;
		while (i.hasNext()) {
			entry = i.next();
			v = entry.getKey();
			f[v] = entry.getValue();
			if (v == FigureParam.DEFAULT_FORMAT)
				index = v;
		}
		formats = new JComboBox(f);
		formats.setSelectedIndex(index);

		showScaleBar = new JCheckBox("Scale Bar");
		showScaleBar.setFont(showScaleBar.getFont().deriveFont(Font.BOLD));
		showScaleBar.setActionCommand(""+SCALE_BAR);
		showScaleBar.addActionListener(this);
		scaleBar = new NumericalTextField();
		scaleBar.setText(""+EditorUtil.DEFAULT_SCALE);

		colorBox = new JComboBox();
		Map<Color, String> colors = EditorUtil.COLORS_BAR;
		Object[][] cols = new Object[colors.size()][2];
		int k = 0;
		Iterator<Entry<Color, String>> j = colors.entrySet().iterator();
		Entry<Color, String> e;
		while (j.hasNext()) {
			e = j.next();
			cols[k] = new Object[]{e.getKey(), e.getValue()};
			k++;
		}

		colorBox.setModel(new DefaultComboBoxModel(cols));	
		colorBox.setSelectedIndex(cols.length-1);
		colorBox.setRenderer(new ColorListRenderer());
		colorBox.setActionCommand(""+COLOR_SELECTION);
		colorBox.addActionListener(this);
		showScaleBar.setSelected(false);
		scaleBar.setEnabled(false);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { close(); }
		});

		rowName = new JComboBox(ROW_NAMES); 
		
		ButtonGroup optionsGroups = new ButtonGroup();
		displayedObjects = new JRadioButton("Displayed Images");
		selectedObjects = new JRadioButton("Selected Images");
		optionsGroups.add(displayedObjects);
		optionsGroups.add(selectedObjects);
		selectedObjects.setSelected(true);
		
		if (dialogType == THUMBNAILS) {
			includeUntagged = new JCheckBox("Include all thumbnails");
			includeUntagged.setToolTipText("Include all remaining thumbnails " +
					"not selected by Tags.");
			includeUntagged.setHorizontalTextPosition(JCheckBox.LEFT);
			includeUntagged.setFont(
					includeUntagged.getFont().deriveFont(Font.BOLD));
			arrangeByTags = new JCheckBox("Select by Tag");
			arrangeByTags.setToolTipText("Arrange the thumbnails by Tags");
			arrangeByTags.setHorizontalTextPosition(JCheckBox.LEFT);
			arrangeByTags.setFont(
					arrangeByTags.getFont().deriveFont(Font.BOLD));
			arrangeByTags.addActionListener(this);
			arrangeByTags.setActionCommand(""+ARRANGE_BY_TAGS);
			sizeBox = new JComboBox(SIZE_OPTIONS);
			sizeBox.setSelectedIndex(SIZE_96);
			numberPerRow = new NumericalTextField(1, 100);
			numberPerRow.setColumns(3);
			numberPerRow.setText(""+ITEMS_PER_ROW);
			return;
		}
		numberPerRow = new NumericalTextField(1, 100);
		numberPerRow.setColumns(3);
		numberPerRow.setText(""+ITEMS_PER_ROW);
		projectionBox = new JRadioButton("Z-projection");
		projectionBox.addChangeListener(this);
		planeSelection = new JRadioButton("Last-viewed Z-section");
		planeSelection.addChangeListener(this);
		planeSelection.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(projectionBox);
		group.add(planeSelection);
		thumbnailHeight = Factory.THUMB_DEFAULT_HEIGHT;
		thumbnailWidth = Factory.THUMB_DEFAULT_WIDTH;
		
		int maxZ = pixels.getSizeZ();
		zRange = new TextualTwoKnobsSlider(1, maxZ, 1, maxZ);
		zRange.layoutComponents();
		zRange.setEnabled(maxZ > 1);
		String[] names = new String[ProjectionParam.PROJECTIONS.size()];
        k = 0;
        i = ProjectionParam.PROJECTIONS.entrySet().iterator();
        projectionTypes = new HashMap<Integer, Integer>();
        
        while (i.hasNext()) {
        	entry = i.next();
			projectionTypes.put(k, entry.getKey());
			names[k] = (String) entry.getValue();
			k++;
		}
        
        projectionTypesBox = new JComboBox(names);
        projectionTypesBox.setToolTipText(PROJECTION_DESCRIPTION);
        
		projectionFrequency = new JSpinner(new SpinnerNumberModel(1, 1, maxZ+1,
				1));
		
		ButtonGroup g = new ButtonGroup();
		splitPanelGrey = new JRadioButton("Grey");
		splitPanelColor = new JRadioButton("Color");
		splitPanelColor.addChangeListener(this);
		splitPanelGrey.addChangeListener(this);
		g.add(splitPanelGrey);
		g.add(splitPanelColor);
		splitPanelColor.setSelected(true);
		int maxT = pixels.getSizeT();
		movieFrequency = new JSpinner(new SpinnerNumberModel(1, 1, maxT+1, 1));
		movieFrequency.addChangeListener(this);
		widthField = new NumericalTextField(0, pixels.getSizeX());
		widthField.setColumns(5);
		widthField.setText(""+pixels.getSizeX());
		heightField = new NumericalTextField(0, pixels.getSizeY());
		heightField.setColumns(5);
		heightField.setText(""+pixels.getSizeY());
		
		widthField.getDocument().addDocumentListener(this);
		heightField.getDocument().addDocumentListener(this);
		movieSlider = new GridSlider(maxT, 1);
		movieSlider.addPropertyChangeListener(
				GridSlider.COLUMN_SELECTION_PROPERTY, this);
		setProjectionSelected(false);
		map = FigureParam.TIMES;
		f = new String[map.size()];
		i = map.entrySet().iterator();
		while (i.hasNext()) {
			entry = i.next();
			f[entry.getKey()] = (String) entry.getValue();
		}
		timesBox = new JComboBox(f);
		switch (dialogType) {
			case MOVIE:
				generalLabel = new JLabel(FRAMES_TEXT+maxT+"/"+maxT);
				break;
			case SPLIT_ROI:
				generalLabel = new JLabel(MAGNIFICATION_TEXT);
				break;
			default:
				generalLabel = new JLabel();
		}
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		
		TitlePanel tp;
		String text = null;
		switch (dialogType) {
			case THUMBNAILS:
				text = "Create a thumbnail Figure.";
				break;
			case SPLIT:
			case SPLIT_ROI:
				text = "Create a Split View Figure.";
				break;
			case MOVIE:
				text = "Create a Movie Figure.";
		}
		tp = new TitlePanel("Create Figure", text, 
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
		JPanel all = new JPanel();
		all.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		all.setLayout(new BoxLayout(all, BoxLayout.X_AXIS));
		all.add(UIUtilities.buildComponentPanel(menuButton));
		all.add(UIUtilities.buildComponentPanelRight(bar));
		return all;
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
        p.add(UIUtilities.setTextFont("Panel Width: "), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        p.add(widthField, c);  
        c.gridx++;
        p.add(new JLabel("pixels"), c); 
        c.gridx = 0;
        c.gridy++;
        p.add(UIUtilities.setTextFont("Panel Height: "), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        p.add(heightField, c); 
        c.gridx++;
        p.add(new JLabel("pixels"), c); 
		return UIUtilities.buildComponentPanel(p);
	}

	/**
	 * Enables or not the projection controls.
	 * 
	 * @param selected  Pass <code>true</code> to enable the controls,
	 * 					<code>false</code> otherwise.
	 */
	private void setProjectionSelected(boolean selected)
	{
		projectionTypesBox.setEnabled(selected);
		projectionFrequency.setEnabled(selected);
		zRange.setEnabled(selected);
		planeSelection.setEnabled(!selected);
	}
	
	/**
	 * Builds the projection component.
	 * 
	 * @return See above.
	 */
	private JPanel buildProjectionComponent()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		JPanel content = new JPanel();
		content.add(planeSelection);
		content.add(projectionBox);
    	p.add(UIUtilities.buildComponentPanel(content, 0, 0), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		p.add(UIUtilities.setTextFont("Intensity"), c);
		c.gridx++;
		p.add(projectionTypesBox, c);
		c.gridx = 0;
		c.gridy++;
		p.add(UIUtilities.setTextFont("Every n-th slice"), c);
		c.gridx++;
	    p.add(UIUtilities.buildComponentPanel(projectionFrequency), c);
	    c.gridy++;
		c.gridx = 0;
	    p.add(UIUtilities.setTextFont("Z-sections Range"), c);
	    c.gridx++;
        p.add(UIUtilities.buildComponentPanel(zRange), c);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/** Invokes when the projection is selected or not. */
	private void onProjectionSelectionChanged()
	{
		boolean b = projectionBox.isSelected();
		if (projectionTypesBox != null)
			projectionTypesBox.setEnabled(b);
		if (projectionFrequency != null) projectionFrequency.setEnabled(b);
		if (zRange != null) zRange.setEnabled(b);
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
				5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}; //rows
		p.setLayout(new TableLayout(tl));
		int i = 0;
        p.add(UIUtilities.setTextFont("Name"), "0, "+i+"");
        p.add(nameField, "1, "+i+", 4, "+i);
        i = i+2;
        p.add(UIUtilities.setTextFont("Format"), "0, "+i+"");
        p.add(formats, "1, "+i);
        if (dialogType == THUMBNAILS) {
        	i = i+2;
        	p.add(UIUtilities.setTextFont("Thumbnails Size"), "0, "+i+"");
        	p.add(UIUtilities.buildComponentPanel(sizeBox), "1, "+i);
        	i = i+2;
        	p.add(UIUtilities.setTextFont(ITEMS_PER_ROW_TEXT), "0, "+i+"");
        	p.add(UIUtilities.buildComponentPanel(numberPerRow), "1, "+i);
        } else {
        	i = i+2;
        	p.add(UIUtilities.setTextFont("Image Label"), "0, "+i+"");
        	p.add(rowName, "1, "+i);
        	i = i+2;
        	p.add(showScaleBar, "0, "+i);
        	p.add(scaleBar, "1, "+i);
        	p.add(new JLabel("microns"), "2, "+i);
        	i = i+2;
        	p.add(UIUtilities.setTextFont("Overlay"), "0, "+i);
        	p.add(UIUtilities.buildComponentPanel(colorBox), "1, "+i);
        }
        if (ImageData.class.equals(type)) {
        	i = i+2;
        	p.add(UIUtilities.setTextFont("Made of"), "0, "+i+"," +
        	" LEFT, TOP");
        	JPanel controls = new JPanel();
        	controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        	controls.add(displayedObjects);
        	controls.add(selectedObjects);
        	p.add(UIUtilities.buildComponentPanel(controls), "1, "+i);
        }
        switch (dialogType) {
			case MOVIE:
				if (pixels.getSizeT() > 1) {
					i++;
					p.add(new JSeparator(), "0, "+i+", 4, "+i);
					i++;
		        	p.add(buildMovieComponent(), "0, "+i+", 4, "+i);
				}
		}
		return p;
	}
	
	/**
	 * Builds the components hosting the various images.
	 * 
	 * @return See above.
	 */
	private JPanel buildComponents()
	{
		JPanel p = new JPanel();
		double[] columns = new double[components.size()+1];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = TableLayout.PREFERRED;
		}
		double[] rows = {TableLayout.FILL};
 		p.setLayout(new TableLayout(columns, rows));
		Entry<Integer, FigureComponent> entry;
		Iterator<Entry<Integer, FigureComponent>> 
		i = components.entrySet().iterator();
		int index = 0;
		while (i.hasNext()) {
			entry = i.next();
			p.add(entry.getValue(), index+", 0, LEFT, TOP");
			index++;
		}
		p.add(mergedComponent, index+", 0, LEFT, TOP");
		p.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		return p;
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
		JPanel splitPanel = new JPanel();
		splitPanel.add(UIUtilities.setTextFont("Split Panel"));
		splitPanel.add(splitPanelColor);
		splitPanel.add(splitPanelGrey);
		
		JPanel controls = new JPanel();
		double size[][] = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
				TableLayout.PREFERRED}};
		controls.setLayout(new TableLayout(size));
		controls.add(splitPanel, "0, 0, LEFT, CENTER");
		controls.add(buildComponents(), "0, 2, LEFT, CENTER");
		if (dialogType == SPLIT_ROI) {
			JPanel zoomPanel = new JPanel();
			zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.X_AXIS));
			zoomPanel.add(UIUtilities.setTextFont("Zoom"));
			zoomPanel.add(zoomBox);
			JPanel pc = new JPanel();
			pc.setLayout(new BoxLayout(pc, BoxLayout.Y_AXIS));
			pc.add(zoomPanel);
			pc.add(UIUtilities.buildComponentPanel(generalLabel));
			
			JPanel splitControls = new JPanel();
			splitControls.setLayout(new BoxLayout(splitControls,
					BoxLayout.X_AXIS));
			splitControls.add(pc);
			splitControls.add(buildDimensionComponent());
			controls.add(UIUtilities.buildComponentPanel(splitControls),
					"0, 4");
		} else {
			controls.add(buildDimensionComponent(), "0, 4");
		}
		
		return controls;
	}
	
	
	/** 
	 * Builds and lays out the controls for the movie figure.
	 * 
	 * @return See above.
	 */
	private JPanel buildMovieComponent()
	{
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridy = 0;
		c.gridx = 0;
        c.weightx = 0.0;  
        p.add(UIUtilities.setTextFont(ITEMS_PER_ROW_TEXT), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c);
        c.gridx++;
        p.add(UIUtilities.buildComponentPanel(numberPerRow), c);
        c.gridy++;
        c.gridx = 0;
        p.add(UIUtilities.setTextFont("Time-point frequency"), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c);
        c.gridx++;
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(movieFrequency);
        pane.add(Box.createHorizontalStrut(5));
        pane.add(generalLabel);
        p.add(UIUtilities.buildComponentPanel(pane), c);
        c.gridy++;
        c.gridx = 0;
        p.add(UIUtilities.setTextFont("Selected Time-points"), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c);
        c.gridx++;
        if (pixels.getSizeT() <= MAX_CELLS)
        	p.add(movieSlider, c);
        else {
        	JScrollPane sp = new JScrollPane(movieSlider);
        	Dimension ds = movieSlider.getPreferredSize();
        	sp.getViewport().setPreferredSize(new Dimension(
        			MAX_CELLS*GridSlider.CELL_SIZE.width, ds.height));
        	p.add(sp, c);
        }
        c.weightx = 0.0;
        c.gridy++;
        c.gridx = 0;
        p.add(UIUtilities.setTextFont("Time units"), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c);
        c.gridx++;
        p.add(UIUtilities.buildComponentPanel(timesBox), c);
        
		controls.add(UIUtilities.buildComponentPanel(p));
		controls.add(buildDimensionComponent());
		return controls;
	}
	
	/**
	 * Builds the main component.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		if (dialogType == THUMBNAILS) return buildThumbnailsPane();
			
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
		if (pixels.getSizeZ() > 1 && dialogType != SPLIT_ROI) {
			pane = EditorUtil.createTaskPane("Z-section Selection");
			pane.add(buildProjectionComponent());
			i++;
			p.add(pane, "0, "+i);
		}
		if (dialogType != MOVIE) {
			i++;
			channelsPane = EditorUtil.createTaskPane("Channels selection");
			channelsPane.setCollapsed(false);
			channelsPane.add(buildDefaultPane());
			p.add(channelsPane, "0, "+i);
		}
		return p;
	}
	
	/**
	 * Returns the components for the thumbnails script.
	 * 
	 * @return See above.
	 */
	private JPanel buildThumbnailsPane()
	{
		thumbnailsPane = new JPanel();
		thumbnailsPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		double[][] tl = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED}}; //rows
		thumbnailsPane.setLayout(new TableLayout(tl));
		thumbnailsPane.add(buildTypeComponent(), "0, 0");
		return thumbnailsPane;
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
		if (renderer != null) renderer.resetSettings(rndDef, false);
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
		if (mergedComponent != null)
			p.setMergedLabel(mergedComponent.isChannelsName());
		p.setSelectedObjects(selectedObjects.isSelected());
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
		p.setColor(getSelectedColor());
		//projection
		if (projectionBox.isSelected()) {
			p.setZStart((int) zRange.getStartValue()-1);
			p.setZEnd((int) zRange.getEndValue()-1);
			p.setStepping((Integer) projectionFrequency.getValue());
			p.setProjectionType(
					projectionTypes.get(projectionTypesBox.getSelectedIndex()));
		} else {
			p.setZStart(-1);
			p.setZEnd(-1);
			p.setStepping(1);
			p.setProjectionType(ProjectionParam.MAXIMUM_INTENSITY);
		}
	}
	
	/** 
	 * Collects the parameters to create a Split figure. 
	 * 
	 * @return See above.
	 */
	private FigureParam saveSplitFigure()
	{
		Map<Integer, String> split = new LinkedHashMap<Integer, String>();
		FigureComponent comp;
		Entry<Integer, FigureComponent> entry;
		List<Integer> splitActive = new ArrayList<Integer>();
		Iterator<Entry<Integer, FigureComponent>> 
		i = components.entrySet().iterator();
		while (i.hasNext()) {
			entry = i.next();
			comp = entry.getValue();
			split.put(entry.getKey(), comp.getLabel());
			if (comp.isSelected()) {
				splitActive.add((Integer) entry.getKey());
			}
		}
		Map<Integer, Color> merge = new LinkedHashMap<Integer, Color>();
		List<Integer> active = renderer.getActiveChannels();
		Iterator<Integer> j = active.iterator();
		int index;
		while (j.hasNext()) {
			index = j.next();
			merge.put(index, renderer.getChannelColor(index));
		}
		
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		int label = rowName.getSelectedIndex();
		FigureParam p = new FigureParam(format, name, split, merge, label);
		p.setSplitActive(splitActive);
		collectParam(p);
		return p;
	}
	
	/** 
	 * Collects the parameters to create a ROI figure. 
	 * 
	 * @return See above.
	 */
	private FigureParam saveROIFigure()
	{
		Map<Integer, String> split = new LinkedHashMap<Integer, String>();
		List<Integer> splitActive = new ArrayList<Integer>();
		FigureComponent comp;
		Entry<Integer, FigureComponent> entry;
		Iterator<Entry<Integer, FigureComponent>> 
		i = components.entrySet().iterator();
		while (i.hasNext()) {
			entry = i.next();
			comp = entry.getValue();
			split.put(entry.getKey(), comp.getLabel());
			if (comp.isSelected()) {
				splitActive.add((Integer) entry.getKey());
			}
		}
		Map<Integer, Color> merge = new LinkedHashMap<Integer, Color>();
		List<Integer> active = renderer.getActiveChannels();
		Iterator<Integer> j = active.iterator();
		int index;
		while (j.hasNext()) {
			index = j.next();
			merge.put(index, renderer.getChannelColor(index));
		}

		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		int label = rowName.getSelectedIndex();
		FigureParam p = new FigureParam(format, name, split, merge, label);
		p.setSplitActive(splitActive);
		p.setIndex(FigureParam.SPLIT_VIEW_ROI);
		collectParam(p);
		double zoom = 0;
		switch (zoomBox.getSelectedIndex()) {
			case ZOOM_100:
				zoom = 1;
				break;
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
		return p;
	}
	
	/** 
	 * Collects the parameters to create the movie figure.
	 * 
	 * @return See above.
	 */
	private FigureParam saveMovieFigure()
	{
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		int label = rowName.getSelectedIndex();
		FigureParam p = new FigureParam(format, name, label);
		p.setIndex(FigureParam.MOVIE);
		p.setTime(timesBox.getSelectedIndex());
		p.setTimepoints(sorter.sort(movieSlider.getSelectedCells()));
		collectParam(p);
		Number n = numberPerRow.getValueAsNumber();
		if (n != null && n instanceof Integer)
			p.setMaxPerColumn((Integer) n);
		return p;
	}
	
	/** 
	 * Collects the parameters to create the thumbnail figure.
	 * 
	 * @return  See above.
	 */
	private FigureParam saveThumbnailsFigure()
	{
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		FigureParam p = new FigureParam(format, name);
		p.setIndex(FigureParam.THUMBNAILS);
		int width = 96;
		switch (sizeBox.getSelectedIndex()) {
			case SIZE_24:
				width = 24;
				break;
			case SIZE_32:
				width = 32;
				break;
			case SIZE_48:
				width = 48;
				break;
			case SIZE_64:
				width = 64;
				break;
			case SIZE_96:
				width = 96;
				break;
			case SIZE_128:
				width = 128;
				break;
			case SIZE_160:
				width = 160;
		}
		p.setWidth(width);
		
		p.setSelectedObjects(selectedObjects.isSelected());
		if (includeUntagged != null)
			p.setIncludeUntagged(includeUntagged.isSelected());
		//retrieve the id of the selected tags
		if (arrangeByTags.isSelected() && selection != null 
				&& selection.size() > 0) { 
			Iterator<JCheckBox> i = selection.iterator();
			JCheckBox box;
			TagAnnotationData tag;
			List<Long> ids = new ArrayList<Long>();
			while (i.hasNext()) {
				box = i.next();
				tag = tagsSelection.get(box);
				ids.add(tag.getId());
			}
			p.setTags(ids);
		}
		Number n = numberPerRow.getValueAsNumber();
		if (n != null && n instanceof Integer)
			p.setMaxPerColumn((Integer) n);
		return p;
	}
	
	/** Collects the parameters to create a figure. */
	private void save()
	{
		FigureParam p = null;
		switch (dialogType) {
			case SPLIT:
				p = saveSplitFigure();
				break;
			case SPLIT_ROI:
				p = saveROIFigure();
				break;
			case MOVIE:
				p = saveMovieFigure();
				break;
			case THUMBNAILS:
				p = saveThumbnailsFigure();
		}
		close();
		if (p != null)
			firePropertyChange(CREATE_FIGURE_PROPERTY, null, p);
	}
	
	/** 
	 * Sets the enabled flag of the {@link #saveButton} depending on
	 * the value to the name field.
	 */
	private void handleText()
	{
		String text = nameField.getText();
		saveButton.setEnabled(!(text == null || text.trim().length() == 0));
		switch (dialogType) {
			case SPLIT:
			case SPLIT_ROI:
				saveButton.setEnabled(renderer != null);
		}
		checkBinaryAvailability();
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
		int v = (int) ((n*pixels.getSizeX())/pixels.getSizeY());
		if (field == widthField) {
			doc = heightField.getDocument();
			doc.removeDocumentListener(this);
			heightField.setText(""+v);
			doc.addDocumentListener(this);
		} else {
			doc = widthField.getDocument();
			doc.removeDocumentListener(this);
			widthField.setText(""+v);
			doc.addDocumentListener(this);
		}
		setMergedImageForSplitROI(mergeUnscaled);
	}
	
	/**
	 * Resets the image for the merged component.
	 * 
	 * @param image The image to display.
	 */
	private void setMergedImageForSplitROI(BufferedImage image)
	{
		if (dialogType != SPLIT_ROI) return;
		int w = (Integer) widthField.getValueAsNumber();
		if (w <= 0) return;
		int h = (Integer) heightField.getValueAsNumber();
		if (h <= 0) return;
		int x = w*thumbnailWidth/pixels.getSizeX();
		int y = w*thumbnailHeight/pixels.getSizeY();
		if (x == 0 || y == 0) return;
		mergedComponent.setOriginalImage(
				Factory.scaleBufferedImage(image, x, y));
		Dimension d = new Dimension(x, y);
		DrawingCanvasView canvasView = drawingComponent.getDrawingView();
		double r = ((double) w)/pixels.getSizeX();
		double f =  scalingFactor*r;
		if (f != -1) canvasView.setScaleFactor(f, d);
		if (zoomBox.getSelectedIndex() == ZOOM_AUTO) setLensFactor();
		mergedComponent.setCanvasSize(x, y);
	}
	
	/**
	 * Returns the name of the script.
	 * 
	 * @return See above.
	 */
	private String getScriptName()
	{
		switch (dialogType) {
			case SPLIT:
				return FigureParam.SPLIT_VIEW_SCRIPT;
			case SPLIT_ROI:
				return FigureParam.ROI_SCRIPT;
			case MOVIE:
				return FigureParam.MOVIE_SCRIPT;
			case THUMBNAILS:
				return FigureParam.THUMBNAIL_SCRIPT;
		}
		return "";
	}
	
	/**
	 * Returns <code>true</code> if the dialog required a set of pixels
	 * <code>false</code> otherwise.
	 * 
	 * @param index One of the constants identifying the dialog.
	 * @return See above.
	 */
	public static boolean needPixels(int index)
	{
		return THUMBNAILS != index;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param name  The default name for the file.
	 * @param pixels The pixels object of reference.
	 * @param index One of the constants defined by this class.
	 * @param type  The type of objects to handle.
	 */
	public FigureDialog(JFrame owner, String name, PixelsData pixels,
			int index, Class type)
	{
		super(owner, true);
		this.type = type;
		this.pixels = pixels;
		this.dialogType = index;
		initComponents(name);
		buildGUI();
		setSize(500, 700);
	}
	
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
	 * Sets the channel selection.
	 * 
	 * @param channel	The selected channel.
	 * @param active	Pass <code>true</code> to set the channel active,
	 * 					<code>false</code> otherwise.
	 * @param merged	Pass <code>true</code> to indicate that the merged 
	 * 					channels have been modified, <code>false</code>
	 * 					otherwise.
	 */
	void setChannelSelection(int channel, boolean active, boolean merged)
	{
		renderer.setActive(channel, active);
		
		if (dialogType == SPLIT_ROI) 
			setMergedImageForSplitROI(getMergedImage());
		else mergedComponent.setOriginalImage(getMergedImage());

		List<Integer> actives = renderer.getActiveChannels();
        int v;
        Iterator<ChannelButton> i = mergedComponent.getChannels().iterator();
        ChannelButton cb;
        while (i.hasNext()) {
			cb = i.next();
			v = cb.getChannelIndex();
			cb.setSelected(actives.contains(v));
		}
        FigureComponent comp = components.get(channel);
		switch (dialogType) {
			case SPLIT:
			case SPLIT_ROI:
		        boolean grey = splitPanelGrey.isSelected();
		        if (active) {
		        	if (grey) comp.resetImage(grey);
		        	else comp.resetImage(!active);
		        } else comp.resetImage(!active);
		        if (!merged) comp.setSelected(active);
		        break;	
		}
	}

	/**
	 * Returns the type of dialog. One of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getDialogType() { return dialogType; }
	
	/**
	 * Sets the renderer.
	 * 
	 * @param renderer 	Reference to the renderer. Mustn't be <code>null</code>.
	 */
	public void setRenderer(Renderer renderer)
	{
		if (renderer == null)
			throw new IllegalArgumentException("No renderer.");
		this.renderer = renderer;
		rndDef = renderer.getRndSettingsCopy();
		channelsPane.removeAll();
		switch (dialogType) {
			case SPLIT:
			case SPLIT_ROI:
				initChannelComponents();
				channelsPane.add(buildChannelsComponent());
				saveButton.setEnabled(true);
				checkBinaryAvailability();
				pack();
				break;
		}
	}
	
	/**
	 * Sets the collections of tags.
	 * 
	 * @param tags The values to set.
	 */
	public void setTags(Collection tags)
	{
		if (tags == null || tags.size() == 0) return;
		if (thumbnailsPane == null) return;
		JPanel p = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED}}; //rows
		p.setLayout(new TableLayout(tl));
		tagsSelection = new LinkedHashMap<JCheckBox, TagAnnotationData>();
		List<TagAnnotationData> l = sorter.sort(tags);
		Iterator<TagAnnotationData> i = l.iterator();
		TagAnnotationData tag;
		JCheckBox box;
		JPanel tagPane = new JPanel();
		tagPane.setLayout(new BoxLayout(tagPane, BoxLayout.Y_AXIS));
		ActionListener listener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				layoutSelectedTags((JCheckBox) e.getSource());
			}
		};
		while (i.hasNext()) {
			tag = i.next();
			box = new JCheckBox(tag.getTagValue());
			box.setEnabled(false);
			box.addActionListener(listener);
			tagsSelection.put(box, tag);
			tagPane.add(box);
		}
		selectedTags = new JPanel();
		selectedTags.setLayout(new BoxLayout(selectedTags, BoxLayout.Y_AXIS));
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		controls.add(UIUtilities.buildComponentPanel(arrangeByTags));
		controls.add(UIUtilities.buildComponentPanel(includeUntagged));
		p.add(controls, "0, 0, LEFT, TOP");
		p.add(selectedTags, "0, 1, LEFT, TOP");
		JScrollPane pane = new JScrollPane(tagPane);
		Dimension d = pane.getPreferredSize();
		pane.setPreferredSize(new Dimension(d.width, MAX_HEIGHT));
		p.add(pane, "1, 0, 1, 1");
		thumbnailsPane.add(p, "0, 1");
		thumbnailsPane.revalidate();
		thumbnailsPane.repaint();
	}
	
	/**
	 * Sets the collection of ROIs related to the primary select.
	 * 
	 * @param rois The value to set.
	 */
	public boolean setROIs(Collection rois)
	{
		if (rois == null) return false;
		drawingComponent = new DrawingComponent();
		drawingComponent.getDrawingView().setScaleFactor(1.0);
		roiComponent = new ROIComponent();
		Iterator<ROIResult> r = rois.iterator();
		ROIResult result;
		int count = 0;
		try {
			long userID = MetadataViewerAgent.getUserDetails().getId();
			Collection list;
			while (r.hasNext()) {
				result = (ROIResult) r.next();
				list = result.getROIs();
				if (list.size() > 0) count++;
				displayedROIs = roiComponent.loadROI(result.getFileID(), 
						list, userID);
				modifyROIDisplay();
			}
		} catch (Exception e) {}
		return count != 0;
	}
	
	/** 
	 * Sets the parent.
	 * 
	 * @param parentRef The value to handle.
	 */
	public void setParentRef(Object parentRef)
	{
		if (parentRef instanceof DatasetData) 
			nameField.setText(((DatasetData) parentRef).getName());
	}
	
	/**
	 * Sets the planes information to determine the time interval.
	 * 
	 * @param planes The values to set.
	 */
	public void setPlaneInfo(Collection planes)
	{
		if (planes == null) return;
		Map<Integer, String> values = new HashMap<Integer, String>();
		Iterator i = planes.iterator();
		String value = "";
		Map<String, Object> details;
		PlaneInfo pi;
		List<String> notSet;
		while (i.hasNext()) {
			pi = (PlaneInfo) i.next();
			details = EditorUtil.transformPlaneInfo(pi);
			notSet = (List<String>) details.get(EditorUtil.NOT_SET);
			if (!notSet.contains(EditorUtil.DELTA_T)) {
			    if(details.get(EditorUtil.DELTA_T) instanceof BigResult) {
			        MetadataViewerAgent.logBigResultExeption(this, details.get(EditorUtil.DELTA_T), EditorUtil.DELTA_T);
			        value = "N/A";
			    } else {
				value = EditorUtil.formatTimeInSeconds(
						(Double) details.get(EditorUtil.DELTA_T));
			    }
				values.put(pi.getTheT().getValue(), value);
			}
		}
		movieSlider.setCellNames(values);
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
				break;
			case ARRANGE_BY_TAGS:
				boolean b = arrangeByTags.isSelected();
				Iterator<JCheckBox> i = tagsSelection.keySet().iterator();
				while (i.hasNext())
					i.next().setEnabled(b);
				break;
			case PROJECTION:
				setProjectionSelected(projectionBox.isSelected());
				break;
			case ZOOM_FACTOR:
				setLensFactor();
				break;
			case COLOR_SELECTION:
				modifyROIDisplay();
				break;
			case DOWNLOAD:
				firePropertyChange(
						ScriptingDialog.DOWNLOAD_SELECTED_SCRIPT_PROPERTY, null,
						getScriptName());
				break;
			case VIEW:
				firePropertyChange(
						ScriptingDialog.VIEW_SELECTED_SCRIPT_PROPERTY, null,
						getScriptName());
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
			Map<Integer, Boolean> map =
					(Map<Integer, Boolean>) evt.getNewValue();
			if (map == null) return;
			if (map.size() != 1) return;
			Entry<Integer, Boolean> entry;
			Iterator<Entry<Integer, Boolean>> i = map.entrySet().iterator();
			Integer index;
			while (i.hasNext()) {
				entry = i.next();
				index = entry.getKey();
				setChannelSelection(index.intValue(), entry.getValue(), true);
			}
		} else if (ChannelComponent.CHANNEL_SELECTION_PROPERTY.equals(name)) {
			ChannelComponent c = (ChannelComponent) evt.getNewValue();
			setChannelSelection(c.getChannelIndex(), c.isActive(), true);
		} else if (GridSlider.COLUMN_SELECTION_PROPERTY.equals(name)) {
			int maxT = pixels.getSizeT();
			generalLabel.setText(
				FRAMES_TEXT+movieSlider.getNumberOfSelectedCells()+"/"+maxT);
			generalLabel.repaint();
		}
	}
	
	/**
	 * Reacts to change in the type of split i.e. either grey or color.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		if (src == projectionBox || src == planeSelection) {
			onProjectionSelectionChanged();
			return;
		}
		switch (dialogType) {
			case SPLIT:
			case SPLIT_ROI:
				if (src == splitPanelGrey) {
					boolean grey = splitPanelGrey.isSelected();
					if (components == null) return;
					Iterator<Integer> i = components.keySet().iterator();
					FigureComponent comp;
					List<Integer> active = renderer.getActiveChannels();
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
				
			break;
			case MOVIE:
				if (src == movieFrequency) {
					Integer value = (Integer) movieFrequency.getValue();
					movieSlider.selectCells(value);
					
					generalLabel.setText(
							FRAMES_TEXT+movieSlider.getNumberOfSelectedCells()+
							"/"+pixels.getSizeT());
					generalLabel.repaint();
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
