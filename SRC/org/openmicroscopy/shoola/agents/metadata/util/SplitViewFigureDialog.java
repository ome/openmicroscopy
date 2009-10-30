/*
 * org.openmicroscopy.shoola.agents.metadata.util.SplitViewFigureDialog 
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.SplitViewFigureParam;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.ColorListRenderer;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
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
public class SplitViewFigureDialog 
	extends JDialog
	implements ActionListener, ChangeListener, DocumentListener, 
	PropertyChangeListener
{

	/** Bound property indicating to create a split view figure. */
	public static final String		SPLIT_FIGURE_PROPERTY = "splitFigure";
	
	/** Action id indicating to close the dialog. */
	public static final int 		CLOSE = 0;
	
	/** Action id indicating to create a movie. */
	public static final int 		SAVE = 1;
	
	/** Action id indicating to allow the modification of the scale bar. */
	private static final int 		SCALE_BAR = 2;
	
	/** Default text describing the compression check box.  */
    private static final String		PROJECTION_DESCRIPTION = 
    				"Select the type of projection.";
    
    /** The size of the channel button. */
    private static final Dimension	BUTTON_SIZE = new Dimension(22, 22);
    
    /** The possible options for row names. */
    private static final String[]	ROW_NAMES;

	static {
		ROW_NAMES = new String[3];
		ROW_NAMES[SplitViewFigureParam.IMAGE_NAME] = "Image's name";
		ROW_NAMES[SplitViewFigureParam.DATASET_NAME] = "Datasets";
		ROW_NAMES[SplitViewFigureParam.TAG_NAME] = "Tags";
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
    private Map<Integer, SplitViewComponent> components;
    
    private List<ChannelButton>				channelList;
    
	/** Option chosen by the user. */
	private int								option;
	
	/** Reference to the renderer.  */
	private Renderer 						renderer;
	
	/** The default plate object. */
	private PlaneDef 						pDef;

	/** The component displaying the merger image. */
	private SplitViewCanvas					mergeCanvas;
	
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
		Iterator<ChannelButton> i = channelList.iterator();
        ChannelButton btn;
        List<Integer> actives = renderer.getActiveChannels();
        int index;
        while (i.hasNext()) {
			btn = i.next();
			index = btn.getChannelIndex();
			btn.setSelected(actives.contains(index));
		}
        SplitViewComponent comp = components.get(channel);
        comp.resetImage(!active);
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
		Dimension d = Factory.computeThumbnailSize(
				thumbnailWidth, thumbnailHeight, 
        		renderer.getPixelsSizeX(), renderer.getPixelsSizeY());
		return Factory.scaleBufferedImage(image, d.width, 
				d.height);
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
							thumbnailWidth, thumbnailHeight, 
							Factory.RED_MASK, Factory.BLANK_MASK,
							Factory.BLANK_MASK);
				} else if (renderer.isColorComponent(Renderer.GREEN_BAND, 
						index)) {
					return Factory.createBandImage(buf,
							thumbnailWidth, thumbnailHeight,  
							Factory.BLANK_MASK, Factory.GREEN_MASK, 
							Factory.BLANK_MASK);
				} else if (renderer.isColorComponent(Renderer.BLUE_BAND, 
						index)) {
					return Factory.createBandImage(buf,
							thumbnailWidth, thumbnailHeight, 
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
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param name The default name of the file.
	 */
	private void initComponents(String name)
	{
		widthField = new NumericalTextField(0, renderer.getPixelsDimensionsX());
		widthField.setColumns(5);
		widthField.setText(""+renderer.getPixelsDimensionsX());
		heightField = new NumericalTextField(0, 
				renderer.getPixelsDimensionsY());
		heightField.setColumns(5);
		heightField.setText(""+renderer.getPixelsDimensionsY());
		
		widthField.getDocument().addDocumentListener(this);
		heightField.getDocument().addDocumentListener(this);
		thumbnailHeight = Factory.THUMB_DEFAULT_HEIGHT;
		thumbnailWidth = Factory.THUMB_DEFAULT_WIDTH;
		pDef = new PlaneDef();
		pDef.t = renderer.getDefaultT();
		pDef.z = renderer.getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		
		mergeCanvas = new SplitViewCanvas();
		mergeImage = getMergedImage();
		mergeCanvas.setPreferredSize(new Dimension(thumbnailWidth, 
				thumbnailHeight));
		mergeCanvas.setImage(mergeImage);
		channelList = new ArrayList<ChannelButton>();
		components = new LinkedHashMap<Integer, SplitViewComponent>();
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
		Map<Integer, String> map = SplitViewFigureParam.FORMATS;
		String[] f = new String[map.size()];
		Entry entry;
		Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			f[(Integer) entry.getKey()] = (String) entry.getValue();
		}
		int maxZ = renderer.getPixelsDimensionsZ();
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
		
		//Initializes the channels
		List<ChannelData> data = renderer.getChannelData();
        ChannelData d;
        //ChannelToggleButton item;
        ChannelButton item;
        Iterator<ChannelData> k = data.iterator();
        List<Integer> active = renderer.getActiveChannels();
        SplitViewComponent split;
        while (k.hasNext()) {
			d = k.next();
			j = d.getIndex();
			split = new SplitViewComponent(this, renderer.getChannelColor(j), 
					d.getChannelLabeling(), j);
			split.setOriginalImage(getChannelImage(j));
			split.setCanvasSize(thumbnailWidth, thumbnailHeight);
			if (!active.contains(j))
				split.resetImage(true);
			components.put(j, split);
		}

        k = data.iterator();
        while (k.hasNext()) {
        	d = k.next();
			j = d.getIndex();
			item = new ChannelButton("", renderer.getChannelColor(j), j);
			item.setPreferredSize(BUTTON_SIZE);
			item.setBackground(UIUtilities.BACKGROUND_COLOR);
			channelList.add(item);
			item.setSelected(active.contains(j));
			item.addPropertyChangeListener(this);
		}
        
        showScaleBar = new JCheckBox("Scale Bar");
		showScaleBar.setFont(showScaleBar.getFont().deriveFont(Font.BOLD));
		showScaleBar.setSelected(true);
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
		colorBox.setRenderer(new ColorListRenderer());
        
        
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
				icons.getIcon(IconManager.ATTACHMENT_48));
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
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.PREFERRED, 
			TableLayout.PREFERRED, TableLayout.PREFERRED,
			TableLayout.FILL}, //columns
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
		return p;
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
        p.add(UIUtilities.setTextFont("Row Name"), "0, "+i+"");
        p.add(rowName, "1, "+i);
        i = i+2;
        p.add(showScaleBar, "0, "+i);
        p.add(scaleBar, "1, "+i);
        p.add(new JLabel("microns"), "2, "+i);
        i = i+2;
        p.add(UIUtilities.buildComponentPanel(colorBox), "1, "+i);
		return p;
	}
	
	/** 
	 * Builds and lays out the component displaying the channels
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
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		Iterator<ChannelButton> i = channelList.iterator();
		while (i.hasNext()) {
			buttonPanel.add(i.next());
			buttonPanel.add(Box.createHorizontalStrut(5));
		}
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(0, 0));
		JPanel f = UIUtilities.buildComponentPanelCenter(buttonPanel);
		f.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		p.add(f, BorderLayout.NORTH);
		p.add(UIUtilities.buildComponentPanelCenter(mergeCanvas), 
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
		if (renderer.getPixelsDimensionsZ() > 1) {
			pane = EditorUtil.createTaskPane("Projection");
			pane.add(buildProjectionComponent());
			i++;
			p.add(pane, "0, "+i);
		}
		i++;
		pane = EditorUtil.createTaskPane("Channels");
		pane.setCollapsed(false);
		pane.add(buildChannelsComponent());
		p.add(pane, "0, "+i);
		return p;
	}
	
	/** Closes the dialog. */
	private void close()
	{
		option = CLOSE;
		setVisible(false);
		dispose();
	}
	
	/** Collects the parameters to create a figure. */
	private void save()
	{
		SplitViewFigureParam p;
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		int label = rowName.getSelectedIndex();
		Map<Integer, String> split = new LinkedHashMap<Integer, String>();
		SplitViewComponent comp;
		Entry entry;
		Iterator i = components.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			comp = (SplitViewComponent) entry.getValue();
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
		p = new SplitViewFigureParam(format, name, split, merge, label);
		
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
		i = m.entrySet().iterator();
		int j = 0;
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
		close();
		firePropertyChange(SPLIT_FIGURE_PROPERTY, null, p);
		//close();
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
		Dimension d = Factory.computeThumbnailSize(n, n, 
        		renderer.getPixelsSizeX(), renderer.getPixelsSizeY());
		Document doc;
		if (field == widthField) {
			doc = heightField.getDocument();
			doc.removeDocumentListener(this);
			heightField.setText(""+d.height);
			doc.addDocumentListener(this);
		} else {
			doc = widthField.getDocument();
			doc.removeDocumentListener(this);
			widthField.setText(""+d.width);
			doc.addDocumentListener(this);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner 	The owner of the dialog.
	 * @param name  	The default name for the file.
	 * @param renderer 	Reference to the renderer.
	 */
	public SplitViewFigureDialog(JFrame owner, String name, Renderer renderer)
	{
		super(owner, true);
		this.renderer = renderer;
		initComponents(name);
		buildGUI();
		pack();
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
		}
	}
	
	/**
	 * Reacts to change in the type of split i.e. either grey or color.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		boolean grey = splitPanelGrey.isSelected();
		Iterator<Integer> i = components.keySet().iterator();
		SplitViewComponent comp;
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
