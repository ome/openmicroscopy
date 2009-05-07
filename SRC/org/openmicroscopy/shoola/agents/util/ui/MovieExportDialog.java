/*
 * org.openmicroscopy.shoola.agents.util.ui.MovieExportDialog 
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
package org.openmicroscopy.shoola.agents.util.ui;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.util.ui.ColorListRenderer;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;

/** 
 * Modal dialog displaying option to export the rendered image. 
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
	
	/** The title of the dialog. */
	private static final String		TITLE = "Movie Creation";
	
	/** The default value for the scale bar. */
	private static final int		DEFAULT_SCALE = 5;
	
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
	private JCheckBox				timeVisible;
	
	/** The selected color for scale bar. */
	private JComboBox				colorBox;
	
	/** To specify the movie playback rate in frames per second. */
	private JSpinner            	fps;
	
	/** Component to select the time interval. */
	private TextualTwoKnobsSlider	timeRange;
	
	/** Component to select the z-section interval. */
	private TextualTwoKnobsSlider	zRange;
	
	/** Option chosen by the user. */
	private int						option;
	
	/** The parameters to set. */
	private MovieExportParam 	param;
	
	/** 
	 * Creates the components composing the display. 
	 * 
	 * @param name The default name of the file.
	 * @param maxT The maximum number of timepoints.
	 * @param maxZ The maximum number of z-sections.
	 */
	private void initComponents(String name, int maxT, int maxZ)
	{
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
		String s = EditorUtil.removeFileExtension(name);
		if (s != null) {
			nameField.setText(s);
			saveButton.setEnabled(true);
		}
		nameField.getDocument().addDocumentListener(this);
		Map<Integer, String> map = MovieExportParam.FORMATS;
		String[] f = new String[map.size()];
		Entry entry;
		Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			f[(Integer) entry.getKey()] = (String) entry.getValue();
		}
		formats = new JComboBox(f);
		timeRange = new TextualTwoKnobsSlider(1, maxT, 1, maxT);
		timeRange.layoutComponents();
		timeRange.setEnabled(maxT > 1);
		
		zRange = new TextualTwoKnobsSlider(1, maxZ, 1, maxZ);
		zRange.layoutComponents();
		zRange.setEnabled(maxZ > 1);
		
		timeInterval = new JCheckBox("Time Interval");
		timeInterval.setFont(timeInterval.getFont().deriveFont(Font.BOLD));
		
		zInterval = new JCheckBox("Z-section Interval");
		zInterval.setFont(zInterval.getFont().deriveFont(Font.BOLD));
		if (maxT > 1) timeInterval.setSelected(true);
		
		if (maxZ > 1 && !timeInterval.isSelected())
			zInterval.setSelected(true);
		showScaleBar = new JCheckBox("Scale Bar");
		showScaleBar.setFont(showScaleBar.getFont().deriveFont(Font.BOLD));
		showScaleBar.setSelected(true);
		showScaleBar.addActionListener(this);
		scaleBar = new NumericalTextField();
		scaleBar.setText(""+DEFAULT_SCALE);
		
		fps = new JSpinner();
		fps.setValue(MovieExportParam.DEFAULT_FPS);
		
		timeVisible = new JCheckBox("Show time value");
		getRootPane().setDefaultButton(saveButton);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { close(); }
		});
		
		colorBox = new JComboBox();
		Map<Color, String> colors = EditorUtil.COLORS_BAR;
		Object[][] cols = new Object[colors.size()][2];
		
		int index = 0;
		i = colors.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			cols[index] = new Object[]{entry.getKey(), entry.getValue()};
			index++;
		}
		colorBox.setModel(new DefaultComboBoxModel(cols));	
		colorBox.setRenderer(new ColorListRenderer());
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
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, "Create a movie and save it " +
				"to the server.", "The movie will be attached to the image.",
				icons.getIcon(IconManager.CREATE_48));
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
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.PREFERRED, 
        			TableLayout.FILL}, //columns
        				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, 
        				TableLayout.PREFERRED, 5, TableLayout.PREFERRED,
        				5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
        				TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10}}; //rows
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
       
        int i = 0;
        content.add(UIUtilities.setTextFont("Name"), "0, "+i+"");
        content.add(nameField, "1, "+i+", 2, "+i);
        i = i+2;
        content.add(UIUtilities.setTextFont("Format"), "0, "+i+"");
        content.add(formats, "1, "+i);
        i = i+2;
        content.add(UIUtilities.setTextFont("Frame Rate"), "0, "+i+"");
        content.add(fps, "1, "+i);
        content.add(new JLabel("fps"), "2, "+i);
        i = i+2;
        content.add(timeInterval, "0, "+i+", l, c");
        content.add(UIUtilities.buildComponentPanel(timeRange), 
        		"1, "+i+", 2, "+i);
        i = i+2;
        content.add(zInterval, "0, "+i+", l, c");
        content.add(UIUtilities.buildComponentPanel(zRange), 
        		"1, "+i+", 2, "+i);
        i = i+2;
        content.add(showScaleBar, "0, "+i);
        content.add(scaleBar, "1, "+i);
        content.add(new JLabel("microns"), "2, "+i);
        i = i+2;
        content.add(UIUtilities.buildComponentPanel(colorBox), "1, "+i);
        i = i+2;
        content.add(timeVisible, "0, "+i);
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
		param.setTimeInterval(timeRange.getStartValue()-1, 
				timeRange.getEndValue()-1);
		param.setZsectionInterval(zRange.getStartValue()-1, 
				zRange.getEndValue()-1);
		param.setTimeVisible(timeVisible.isSelected());
		
		int index = colorBox.getSelectedIndex();
		Entry entry;
		Map<Color, String> m = EditorUtil.COLORS_BAR;
		Iterator i = m.entrySet().iterator();
		int j = 0;
		Color c = null;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			if (j == index) c = (Color) entry.getKey();
			j++;
		}
		param.setColor(c);
		option = SAVE;
		firePropertyChange(CREATE_MOVIE_PROPERTY, null, param);
		close();
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
	 */
	public MovieExportDialog(JFrame owner, String name, int maxT, int maxZ)
	{
		super(owner, true);
		param = null;
		initComponents(name, maxT, maxZ);
		buildGUI();
		pack();
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
