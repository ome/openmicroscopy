/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util.ui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.util.ui.ColorListRenderer;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;

import omero.gateway.model.ChannelData;

/** 
 * Modal dialog displaying option to export the rendered image. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class MovieExportDialog 
	extends JDialog
	implements ActionListener, DocumentListener
{

	/** Bound property indicating to create a movie. */
	public static final String 		CREATE_MOVIE_PROPERTY = "createMovie";
	
	/** Action id indicating to close the dialog. */
	public static final int 		CLOSE = 0;
	
	/** Action id indicating to create a movie. */
	public static final int 		SAVE = 1;
	
	/** Action id indicating to allow the modification of the scale bar. */
	private static final int 		SCALE_BAR = 2;
	
	/** Action id indicating to allow the modification of the scale bar. */
	private static final int 		Z_INTERVAL = 3;
	
	/** Action id indicating to allow the modification of the scale bar. */
	private static final int 		T_INTERVAL = 4;

	/** Action id indicating to view the script used. */
    public static final int VIEW = 5;
    
	/** The title of the dialog. */
	private static final String TITLE = "Movie Creation";
	
	/** The default value for the scale bar. */
	private static final int DEFAULT_SCALE = 5;
	
	/** Button to close the dialog. */
	private JButton					closeButton;
	
	/** Button to close the dialog. */
	private JButton					saveButton;
	
	/** The name of the file. */
	private JTextField				nameField;
	
	/** The supported movie formats. */
	private JComboBox				formats;
	
	/** The supported value of the scale bar. */
	private NumericalTextField		scaleBar;
	
	/** Add a scale bar if selected. */
	private JCheckBox				showScaleBar;
	
	/** Creates a movie across time. */
	private JCheckBox				timeInterval;
	
	/** Creates a movie across z-section. */
	private JCheckBox				zInterval;
	
	/** Displays or not the real time. */
	private JCheckBox				labelVisible;
	
	/** The selected color for scale bar. */
	private JComboBox				colorBox;
	
	/** To specify the movie play-back rate in frames per second. */
	private JSpinner            	fps;
	
	/** Component to select the time interval. */
	private TextualTwoKnobsSlider	timeRange;
	
	/** Component to select the z-section interval. */
	private TextualTwoKnobsSlider	zRange;
	
	/** Option chosen by the user. */
	private int						option;
	
	/** The parameters to set. */
	private MovieExportParam 		param;
	
	/** Component used to set the default z-section. */
	private JSpinner            	zSpinner;
	
	/** Component used to set the default z-section. */
	private JSpinner            	tSpinner;
	
	/** The number of z-sections. */
	private int						maxZ;
	
	/** The number of time-points. */
	private int						maxT;
	
	/** The collection of channels.*/
	private Map<Object, JComponent>	buttons;

	/** Button to view the script.*/
	private JButton viewScript;

	/** 
	 * Creates the components composing the display. 
	 * 
	 * @param name The default name of the file.
	 * @param defaultZ The default z-section.
	 * @param defaultT The default time-point.
	 * @param channels The collection of channels.
	 */
	private void initComponents(String name, int defaultZ, int defaultT, Object
			channels)
	{
		buttons = new LinkedHashMap<Object, JComponent>();
		
		if (channels instanceof Map) {
			Entry entry;
			Iterator i = ((Map) channels).entrySet().iterator();
			ChannelData data;
			ChannelButton button;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				data = (ChannelData) entry.getKey();
				button = new ChannelButton(data.getChannelLabeling(), 
						(Color) entry.getValue(), data.getIndex());
				button.setSelected(true);
				button.addPropertyChangeListener(new PropertyChangeListener() {
					
					public void propertyChange(PropertyChangeEvent evt) {
						String n = evt.getPropertyName();
						if (ChannelButton.CHANNEL_SELECTED_PROPERTY.equals(n)) {
							Map m = (Map) evt.getNewValue();
							ChannelButton button = 
								(ChannelButton) evt.getSource();
							Boolean b = (Boolean) m.get(
									button.getChannelIndex());
							if (b != null) 
								button.setSelected(b.booleanValue());
						}
					}
				});
				buttons.put(data, button);
			}
		} else {
			JCheckBox box;
			int maxC = (Integer) channels;
			for (int i = 0; i < maxC; i++) {
				box = new JCheckBox(""+i);
				box.setSelected(true);
				buttons.put(i, box);
			}
		}
		viewScript = new JButton("View script");
		viewScript.setActionCommand(""+VIEW);
		viewScript.addActionListener(this);
		closeButton = new JButton("Cancel");
		closeButton.setToolTipText(UIUtilities.formatToolTipText(
				"Close the window."));
		closeButton.setActionCommand(""+CLOSE);
		closeButton.addActionListener(this);
		saveButton = new JButton("Create");
		saveButton.setEnabled(false);
		saveButton.setToolTipText(UIUtilities.formatToolTipText(
				"Create a movie."));
		saveButton.setActionCommand(""+SAVE);
		saveButton.addActionListener(this);
		nameField = new JTextField();
		String s = UIUtilities.removeFileExtension(name);
		if (s != null) {
			nameField.setText(s);
			saveButton.setEnabled(true);
		}
		nameField.getDocument().addDocumentListener(this);
		Map<Integer, String> map = MovieExportParam.FORMATS;
		String[] f = new String[map.size()];
		Entry entry;
		Iterator i = map.entrySet().iterator();
		int index = 0;
		int key;
		String value;
		boolean isMac = UIUtilities.isMacOS();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (Integer) entry.getKey();
			f[key] = (String) entry.getValue();
			if (isMac) {
				if (key == MovieExportParam.QT) index = key;
			}
		}
		formats = new JComboBox(f);
		formats.setSelectedIndex(index);
		timeRange = new TextualTwoKnobsSlider(1, maxT, 1, maxT);
		timeRange.getSlider().setOverlap(true);
		timeRange.layoutComponents();
		timeRange.setEnabled(maxT > 1);
		
		zRange = new TextualTwoKnobsSlider(1, maxZ, 1, maxZ);
		zRange.getSlider().setOverlap(true);
		zRange.layoutComponents();
		zRange.setEnabled(false);
		
		timeInterval = new JCheckBox("Time Interval");
		timeInterval.setFont(timeInterval.getFont().deriveFont(Font.BOLD));
		
		zInterval = new JCheckBox("Z-section Interval");
		zInterval.setFont(zInterval.getFont().deriveFont(Font.BOLD));
	
		if (maxT > 1) timeInterval.setSelected(true);
		else timeInterval.setEnabled(false);
		if (maxZ > 1 && maxT <= 1) {
			zInterval.setEnabled(false);
			zInterval.setSelected(true);
			zRange.setEnabled(true);
		}
		SpinnerModel sp = new SpinnerNumberModel(defaultZ, 1, maxZ, 1);
		zSpinner = new JSpinner(sp);
		sp = new SpinnerNumberModel(defaultT, 1, maxT, 1);
		tSpinner = new JSpinner(sp);
		if (maxT <= 1) tSpinner.setEnabled(false);
		if (maxZ <= 1 || zInterval.isSelected()) zSpinner.setEnabled(false);
		zInterval.addActionListener(this);
		zInterval.setActionCommand(""+Z_INTERVAL);
		timeInterval.addActionListener(this);
		timeInterval.setActionCommand(""+T_INTERVAL);
		
		showScaleBar = new JCheckBox("Scale Bar");
		showScaleBar.setFont(showScaleBar.getFont().deriveFont(Font.BOLD));
		showScaleBar.setSelected(true);
		showScaleBar.setActionCommand(""+SCALE_BAR);
		showScaleBar.addActionListener(this);
		scaleBar = new NumericalTextField();
		scaleBar.setText(""+DEFAULT_SCALE);
		
		fps = new JSpinner();
		fps.setValue(MovieExportParam.DEFAULT_FPS);
		labelVisible = new JCheckBox("Show Labels");
		getRootPane().setDefaultButton(saveButton);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { close(); }
		});
		
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
        colorBox.setSelectedIndex(cols.length-1);
	}
	
	/** Enables or not the controls. */
	private void enabledControls()
	{
		saveButton.setEnabled(true);
		if (zInterval.isSelected()) {
			zRange.setEnabled(maxZ > 1);
			zSpinner.setEnabled(false);
		} else {
			zRange.setEnabled(false);
			zSpinner.setEnabled(maxZ > 1);
		}
		if (timeInterval.isSelected()) {
			timeRange.setEnabled(maxT > 1);
			tSpinner.setEnabled(false);
		} else {
			timeRange.setEnabled(false);
			tSpinner.setEnabled(maxT > 1);
		}
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
	    JPanel p = new JPanel();
	    p.add(viewScript);
	    JPanel all = new JPanel();
	    all.setLayout(new BoxLayout(all, BoxLayout.X_AXIS));
	    all.add(UIUtilities.buildComponentPanel(p));
	    all.add(UIUtilities.buildComponentPanelRight(bar));
	    all.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
	    return all;
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, "Create a movie.", 
				"The movie will be saved to the server and " +
				"attached to the image.", icons.getIcon(IconManager.MOVIE_48));
		Container c = getContentPane();
		c.setLayout(new BorderLayout(5, 5));
		c.add(tp, BorderLayout.NORTH);
		c.add(buildBody(), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Builds the main component.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(UIUtilities.setTextFont("Name"));
        p.add(nameField);
        content.add(p);
        p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(UIUtilities.setTextFont("Format"));
        p.add(formats);
        p.add(UIUtilities.setTextFont("Frame Rate"));
        p.add(fps);
        content.add(p);
        p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(UIUtilities.setTextFont("Channels selection"));
        Iterator<JComponent> k = buttons.values().iterator();
        while (k.hasNext()) {
            p.add(k.next());
        }
        content.add(p);
        p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(timeInterval);
        p.add(UIUtilities.buildComponentPanel(timeRange));
        content.add(p);
        p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(zInterval);
        p.add(zRange);
        content.add(p);
        p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(showScaleBar);
        p.add(scaleBar);
        p.add(new JLabel("microns"));
        p.add(colorBox);
        content.add(p);
        content.add(UIUtilities.buildComponentPanel(labelVisible));
        content.add(Box.createVerticalStrut(5));
        return content;
	}
	
	/** Closes the dialog. */
	private void close()
	{
		option = CLOSE;
		setVisible(false);
		dispose();
	}
	
	/** Collects the parameters to create a movie. */
	private void save()
	{
		String name = nameField.getText();
		int format = formats.getSelectedIndex();
		int scale = -1;
		int f = (Integer) fps.getValue();
		if (showScaleBar.isSelected()) {
			Number n = scaleBar.getValueAsNumber();
			if (n != null) scale = n.intValue();
		}
		int type = MovieExportParam.ZT_MOVIE;;
		if (!timeInterval.isSelected() && zInterval.isSelected()) 
			type = MovieExportParam.Z_MOVIE;
		else if (timeInterval.isSelected() && !zInterval.isSelected()) 
			type = MovieExportParam.T_MOVIE;
		param = new MovieExportParam(name, f, format, scale, type);
		if (type == MovieExportParam.T_MOVIE || 
			type == MovieExportParam.ZT_MOVIE)
			param.setTimeInterval((int) timeRange.getStartValue()-1, 
					(int) timeRange.getEndValue()-1);
		if (type == MovieExportParam.Z_MOVIE || 
				type == MovieExportParam.ZT_MOVIE)
			param.setZsectionInterval((int) zRange.getStartValue()-1, 
					(int) zRange.getEndValue()-1);
		param.setLabelVisible(labelVisible.isSelected());
		
		if (!timeInterval.isSelected()) {
			int t = (Integer) tSpinner.getValue()-1;
			param.setTimeInterval(t, t);
		}
		if (!zInterval.isSelected()) {
			int z = (Integer) zSpinner.getValue()-1;
			param.setZsectionInterval(z, z);
		}
		
		int index = colorBox.getSelectedIndex();
		Entry entry;
		Map<Color, String> m = EditorUtil.COLORS_BAR;
		Iterator i = m.entrySet().iterator();
		int j = 0;
		String c = null;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			if (j == index) c = (String) entry.getValue();
			j++;
		}
		param.setColor(c);
		
		//Channels
		List<Integer> channels = new ArrayList<Integer>();
		i = buttons.entrySet().iterator();
		JComponent comp;
		ChannelButton cb;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			comp = (JComponent) entry.getValue();
			if (comp instanceof JCheckBox) {
				if (((JCheckBox) comp).isSelected()) {
					channels.add((Integer) entry.getKey());
				}
			} else if (comp instanceof ChannelButton) {
				cb = (ChannelButton) comp;
				if (cb.isSelected()) {
					channels.add(cb.getChannelIndex());
				}
			}
		}
		param.setChannels(channels);
		option = SAVE;
		close();
		firePropertyChange(CREATE_MOVIE_PROPERTY, null, param);
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
	 * Returns <code>true</code> if the colors are the same, <code>false</code>
	 * otherwise.
	 * 
	 * @param c1 The color to handle.
	 * @param c2 The color to handle.
	 * @return See above.
	 */
	private boolean isSameColor(Color c1, Color c2)
	{
		if (c1.getRed() != c2.getRed()) return false;
		if (c1.getGreen() != c2.getGreen()) return false;
		if (c1.getBlue() != c2.getBlue()) return false;
		return true;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the frame.
	 * @param name  The name of the movie.
	 * @param maxT  The maximum number of time points.
	 * @param maxZ  The maximum number of z-sections.
	 * @param defaultZ The default z-section.
	 * @param defaultT The default timepoint.
	 * @param channels The channels information or the number of channels.
	 */
	public MovieExportDialog(JFrame owner, String name, int maxT, int maxZ, 
			int defaultZ, int defaultT, Object channels)
	{
		super(owner);
		setModal(true);
		param = null;
		this.maxT = maxT;
		this.maxZ = maxZ;
		initComponents(name, defaultZ, defaultT, channels);
		buildGUI();
		pack();
	}

	/**
	 * Turns off controls if the binary data are not available.
	 * 
	 * @param available Pass <code>false</code> to turn off the control.
	 */
	public void setBinaryAvailable(boolean available)
	{
		saveButton.setEnabled(available);
	}
	
	/**
	 * Sets the default value of the scale bar.
	 * 
	 * @param value The numerical value.
	 * @param color	The selected color.
	 */
	public void setScaleBarDefault(int value, Color color)
	{
		if (color != null) {
			Map<Color, String> m = EditorUtil.COLORS_BAR;
			int index = 0;
			Entry entry;
			Iterator i = m.entrySet().iterator();
			Color c;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				c = (Color) entry.getKey();
				if (isSameColor(c, color))
					break;
				index++;
			}
			colorBox.setSelectedIndex(index);
		}
		if (value > 0)
			scaleBar.setText(""+value);
	}

    /**
     * Shows the dialog and returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    public int showDialog()
    {
    	setLocation(getParent().getLocation());
    	setVisible(true);
    	return option;	
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
     * Returns the parameters used to create the movie or <code>null</code>
     * if none set.
     * 
     * @return See above.
     */
    public MovieExportParam getParameters() { return param; }
    
	/**
	 * Closes or creates a movie.
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
	        case T_INTERVAL:
	        case Z_INTERVAL:
	            enabledControls();
	            break;
	        case VIEW:
	            firePropertyChange(
                        ScriptingDialog.VIEW_SELECTED_SCRIPT_PROPERTY, null,
                        MovieExportParam.MOVIE_SCRIPT);
	    }
	}

	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { handleText(); }

	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { handleText(); }
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}


}
