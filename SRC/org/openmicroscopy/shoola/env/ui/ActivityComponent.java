/*
 * org.openmicroscopy.shoola.env.ui.ActivityComponent
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.ui;



//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Activity.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public abstract class ActivityComponent 
	extends JPanel
	implements ActionListener
{

	/** Indicates to create a general component. */
	static final int 	GENERAL = 0;
	
	/** Indicates to create an advanced component. */
	static final int 	ADVANCED = 1;
	
	/** Bound property indicating to remove the entry from the display. */
	public static final String 	REMOVE_ACTIVITY_PROPERTY = "removeActivity";
	
	/** The default dimension of the status. */
	private static final Dimension SIZE = new Dimension(22, 22);
	
	/** ID to remove the entry from the display. */
	private static final int 	REMOVE = 0;
	
	/** ID to download the result. */
	private static final int 	DOWNLOAD = 1;
	
	/** ID to cancel the activity. */
	private static final int 	CANCEL = 2;
	
	/** Indicate the status of the activity. */
	private JXBusyLabel 				status;
	
	/** Button to remove the activity from the display. */
	private JButton						removeButton;
	
	/** Button to cancel the activity. */
	private JButton						cancelButton;

	/** The label hosting the icon. */
	private JLabel						iconLabel;
	
	/** The component displaying the status. */
	private JComponent					 statusPane;
	
	/** One of the constants defined by this class. */
	private int							index;
	
	/** Button to download the activity depending on the type of activity. */
	protected JButton					downloadButton;
	
	/** The label displaying the type of activity. */
	protected JLabel					type;

	/** The label displaying message if any. */
	protected JLabel					messageLabel;
	
	/** Convenience reference for subclasses. */
    protected final Registry			registry;
    
    /** Convenience reference for subclasses. */
    protected final UserNotifier		viewer;
   
    /** The result of the activity. */
    protected Object 					result;
    
    /**
     * Creates a button.
     * 
     * @param text The text of the button.
     * @param actionID The action command id.
     * @return See above.
     */
    private JButton createButton(String text, int actionID)
    {
    	JButton b = new JButton(text);
		b.setEnabled(false);
		b.setActionCommand(""+actionID);
		b.addActionListener(this);
		b.setOpaque(false);
		b.setForeground(UIUtilities.HYPERLINK_COLOR);
		UIUtilities.unifiedButtonLookAndFeel(b);
		return b;
    }
    
	/** 
	 * Initializes the components. 
	 * 
	 * @param text The type of activity.
	 * @param icon The icon to display when done.
	 */
	private void initComponents(String text, Icon icon)
	{
		removeButton = createButton("Remove", REMOVE);
		if (index == ADVANCED)
			downloadButton = createButton("Download", DOWNLOAD);
		status = new JXBusyLabel(SIZE);
		type = UIUtilities.setTextFont(text);
		messageLabel = UIUtilities.setTextFont("", Font.ITALIC, 10);
		iconLabel = new JLabel();
		iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		if (icon != null) iconLabel.setIcon(icon);
		statusPane = status;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel barPane = new JPanel();
		barPane.setOpaque(false);
		barPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		double[][] size = {{TableLayout.FILL}, 
						{TableLayout.PREFERRED, TableLayout.PREFERRED}};
		barPane.setLayout(new TableLayout(size));
		barPane.add(type, "0, 0, LEFT, CENTER");
		barPane.add(messageLabel, "0, 1, CENTER, CENTER");
	
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL, 
			TableLayout.PREFERRED}, {TableLayout.PREFERRED}};
		setLayout(new TableLayout(tl));
		add(statusPane, "0, 0, CENTER, CENTER");
		JPanel p = UIUtilities.buildComponentPanel(barPane);
		p.setOpaque(false);
		p.setBackground(barPane.getBackground());
		add(p, "1, 0");
		add(createToolBar(), "2, 0");
	}
	
	/**
	 * Returns the tool bar.
	 * 
	 * @return See above.
	 */
	private JComponent createToolBar()
	{
		JToolBar bar = new JToolBar();
		bar.setOpaque(false);
		bar.setFloatable(false);
		bar.setBorder(null);
		if (index == ADVANCED) {
			bar.add(downloadButton);
			bar.add(Box.createHorizontalStrut(5));
		}
		bar.add(removeButton);
		JLabel l = new JLabel();
		Font f = l.getFont();
		l.setForeground(UIUtilities.LIGHT_GREY.darker());
		l.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
		String s = UIUtilities.formatShortDateTime(null);
		String[] values = s.split(" ");
		if (values.length > 1) {
			String v = values[1];
			if (values.length >= 2) v +=" "+values[2];
			l.setText(v);
			bar.add(Box.createHorizontalStrut(5));
			bar.add(l);
			bar.add(Box.createHorizontalStrut(5));
		}
		return bar;
	}
	
	/** Resets the controls. */
	private void reset()
	{
		removeButton.setEnabled(true);
		if (index == ADVANCED) downloadButton.setEnabled(true);
		status.setBusy(false);
		status.setVisible(false);
		statusPane = iconLabel;
		remove(statusPane);
		add(statusPane, "0, 0, CENTER, CENTER");
		repaint();
	}
	
	/**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param text		The text of the activity.
     * @param icon		The icon to display then done.
     */
	public ActivityComponent(UserNotifier viewer, Registry registry, String 
			text, Icon icon, int index)
	{
		if (viewer == null) throw new NullPointerException("No viewer.");
    	if (registry == null) throw new NullPointerException("No registry.");
    	this.viewer = viewer;
    	this.registry = registry;
    	this.index = index;
		initComponents(text, icon);
		buildGUI();
	}
	
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param text		The text of the activity.
     * @param icon		The icon to display then done.
     */
	public ActivityComponent(UserNotifier viewer, Registry registry, String 
			text, Icon icon)
	{
		this(viewer, registry, text, icon, GENERAL);
	}
	
    /**
	 * Returns the name to give to the file.
	 * 
	 * @param files		Collection of files in the currently selected directory.
	 * @param fileName	The name of the original file.
	 * @param original	The name of the file. 
	 * @param dirPath	Path to the directory.
	 * @param index		The index of the file.
	 * @param extension The extension to check or <code>null</code>.
	 * @return See above.
	 */
	String getFileName(File[] files, String fileName, String original, 
								String dirPath, int index, String extension)
	{
		String path = dirPath+original;
		boolean exist = false;
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
	        	 if ((files[i].getAbsolutePath()).equals(path)) {
	                 exist = true;
	                 break;
	             }
			}
		}
        if (!exist) return original;
        if (fileName == null || fileName.trim().length() == 0) return original;
    	
    	if (extension != null && extension.trim().length() > 0) {
    		int n = fileName.lastIndexOf(extension);
    		String v = fileName.substring(0, n)+" ("+index+")"+extension;
    		index++;
    		return getFileName(files, fileName, v, dirPath, index, extension);
    	} else {
    		int lastDot = fileName.lastIndexOf(".");
    		if (lastDot != -1) {
        		extension = fileName.substring(lastDot, fileName.length());
        		String v = fileName.substring(0, lastDot)+
        		" ("+index+")"+extension;
        		index++;
        		return getFileName(files, fileName, v, dirPath, index, null);
        	} 
    	}
    	
    	return original;
	}
	
	/** Invokes when the activity starts. */ 
	public void startActivity()
	{
		status.setBusy(true);
	}
	
	/** 
	 * Invokes when the activity end. 
	 * 
	 * @param result The result of the activity.
	 */ 
	public void endActivity(Object result)
	{
		this.result = result;
		reset();
		notifyActivityEnd();
		//Post an event to 
		EventBus bus = registry.getEventBus();
		bus.post(new ActivityFinishedEvent(this));
	}
	
	/**
	 * Returns <code>true</code> if the activity is still on-going,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isOngoingActivity() { return status.isBusy(); }
	
	/**
	 * Notifies that it was not possible to complete the activity.
	 * 
	 * @param text The text to set.
	 */
	public void notifyError(String text)
	{
		reset();
		if (text != null) type.setText(text);
		if (index == ADVANCED) downloadButton.setEnabled(false);
	}
	
	/**
	 * Returns the type of activity.
	 * 
	 * @return See above.
	 */
	public JComponent getActivityType()
	{
		return new JLabel(type.getText());
	}
	
	/** Subclasses should override the method. */
	protected abstract void notifyActivityEnd();
	
	/** Creates a loader. */
	protected abstract UserNotifierLoader createLoader();
	
	/** Subclasses should override the method. */
	protected void notifyDownload() {}
	
	/**
	 * Removes the activity from the display
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case REMOVE:
				firePropertyChange(REMOVE_ACTIVITY_PROPERTY, null, this);
				break;
			case DOWNLOAD:
				notifyDownload();
				break;
			case CANCEL:
				firePropertyChange(REMOVE_ACTIVITY_PROPERTY, null, this);
		}
	}
	
	/**
	 * Overridden to make sure that all the components have the correct 
	 * background.
	 * @see JPanel#setBackground(Color)
	 */
	public void setBackground(Color color)
	{
		super.setBackground(color);
		if (removeButton != null) removeButton.setBackground(color);
	}
	
}
