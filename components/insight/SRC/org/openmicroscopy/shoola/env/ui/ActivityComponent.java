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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import omero.model.OriginalFile;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.ProcessException;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadAndLaunchActivityParam;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.filter.file.CSVFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import omero.gateway.model.FileAnnotationData;

/**
 * Top class that each action should extend.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public abstract class ActivityComponent 
	extends JPanel
	implements ActionListener
{

	/** Bound property indicating to remove the entry from the display. */
	static final String 	REMOVE_ACTIVITY_PROPERTY = "removeActivity";
	
	/** Bound property indicating to unregister the activity. */
	static final String 	UNREGISTER_ACTIVITY_PROPERTY = "unregisterActivity";

	/** The default dimension of the status. */
	private static final Dimension SIZE = new Dimension(22, 22);
	
	/** ID to remove the entry from the display. */
	private static final int 	REMOVE = 0;
	
	/** ID to cancel the activity. */
	private static final int 	CANCEL = 1;
	
	/** ID to display the standard error. */
	private static final int	EXCEPTION = 2;

	/** ID to display all the output. */
	private static final int	ALL_RESULT = 3;
	
	/** ID to display the error. */
	private static final int	ERROR = 4;
	
	/** ID to display the info. */
	private static final int	INFO = 5;
	
	/** The key to look for to display the output message. */
	private static final String MESSAGE = "Message";
	
	/** The key to look for to display the error message if any. */
	static final String STD_ERR = "stderr";
	
	/** The key to look for to display the output message if any. */
	static final String STD_OUT = "stdout";
	
	/** Indicate the status of the activity. */
	private JXBusyLabel 				status;
	
	/** Button to remove the activity from the display. */
	private JButton						removeButton;
	
	/** Button to cancel the activity. */
	private JButton						cancelButton;

	/** The label hosting the icon. */
	protected JLabel					iconLabel;
	
	/** The component displaying the status. */
	private JComponent					 statusPane;
	
	/** The index of the {@link #cancelButton} or {@link #removeButton}. */
	private int							buttonIndex;
	
	/** The tool bar displaying controls. *. */
	private JToolBar					toolBar;
	
	/** Button to shows the exception. */
	private JButton						exceptionButton;
	
	/** Menu displaying the option to view the standard error. */
	private ActivityResultPopupMenu		errorMenu;
	
	/** Menu displaying the option to view the standard output. */
	private ActivityResultPopupMenu		infoMenu;
	
	/** The exception thrown while running the script. */
	private Throwable					exception;
	
	/** The label displaying the type of activity. */
	protected JLabel					type;

	/** The label displaying message if any. */
	protected JLabel					messageLabel;
	
	/** Convenience reference for subclasses. */
    protected final Registry			registry;
    
    /** Convenience reference for subclasses. */
    protected final SecurityContext ctx;
    
    /** Convenience reference for subclasses. */
    protected final UserNotifier		viewer;
   
    /** The result of the activity. */
    protected Object 					result;
    
    /** Loader associated to the activity. */
    protected UserNotifierLoader 		loader;
    
    /** The object hosting the error.*/
    protected Object					errorObject;
    
    /** The object hosting the info.*/
    protected Object					infoObject;
    
    /** The component displaying the results.*/
    private JComponent					resultPane;
    
	/** Button to show the general result. */
	protected List<ActivityResultRow>	resultButtons;
	
	private PropertyChangeListener		listener;
	
	/** The index where the output goes.*/
	private String paneIndex;
	
    /**
	 * Opens the passed object. Downloads it first.
	 * 
	 * @param object The object to open.
	 * @param parameters Either Analysis parameters or Application data.
	 * @param source The source triggering the operation.
	 */
	private void open(Object object, Object parameters, JComponent source)
	{
		if (!(object instanceof FileAnnotationData || 
				object instanceof OriginalFile)) return;
		Environment env = (Environment) registry.lookup(LookupNames.ENV);
		int index = -1;
		long id = -1;
		String name = "";
		OriginalFile of = null;
		if (object instanceof FileAnnotationData) {
			FileAnnotationData data = (FileAnnotationData) object;
			if (data.isLoaded()) {
				of = (OriginalFile) data.getContent();
				name = data.getFileName();
			} else {
				id = data.getId();
				index = DownloadActivityParam.FILE_ANNOTATION;
				name = "Annotation_"+id;
			}
		} else {
			of = (OriginalFile) object;
			id = of.getId().getValue();
			if (!of.isLoaded()) {
				index = DownloadActivityParam.ORIGINAL_FILE;
				name = "File_"+id;
			} else {
				if (of.getName() != null)
					name = of.getName().getValue();
				else name = "File_"+id;
			}
		}
		String path = env.getOmeroFilesHome();
		File f;
		if (index != -1) {
			path += File.separator+name;
			f = new File(path);
			//Delete the file if it already exists
			if (f.exists()) {
				f.delete();
				f = new File(path);
			}
			f.deleteOnExit();
		} else {
			String v = path + File.separator+name;
			File ff = new File(v);
			if (ff.exists()) 
				ff.delete();
			f = new File(path);
		}
		
		DownloadAndLaunchActivityParam activity;
		
		if (index != -1) 
			activity = new DownloadAndLaunchActivityParam(id, index, f, null);
		else 
			activity = new DownloadAndLaunchActivityParam(of, f, null);
		
		if (parameters instanceof ApplicationData) {
			activity.setApplicationData((ApplicationData) parameters);
		}
		activity.setSource(source);
		viewer.notifyActivity(ctx, activity);
	}
	
	/** 
	 * Initializes the components. 
	 * 
	 * @param text The type of activity.
	 * @param icon The icon to display when done.
	 */
	private void initComponents(String text, Icon icon)
	{
		exceptionButton = createButton("Failure", EXCEPTION, this);
		exceptionButton.setVisible(false);
		removeButton = createButton("Remove", REMOVE, this);
		cancelButton = createButton("Cancel", CANCEL, this);
		//if (index == ADVANCED)
		resultButtons = new ArrayList<ActivityResultRow>();
		status = new JXBusyLabel(SIZE);
		type = UIUtilities.setTextFont(text);
		iconLabel = new JLabel();
		messageLabel = UIUtilities.setTextFont("", 
				iconLabel.getFont().getStyle(), 10);
		iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		if (icon != null) iconLabel.setIcon(icon);
		statusPane = status;
		resultPane = new JPanel();
		listener = new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				//do something
				
			}
		};
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
	
		//icon, message, content, toolbar
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL,
			TableLayout.PREFERRED, TableLayout.PREFERRED},
			{TableLayout.PREFERRED}};
		setLayout(new TableLayout(tl));
		add(statusPane, "0, 0");
		JPanel p = UIUtilities.buildComponentPanel(barPane);
		p.setOpaque(false);
		p.setBackground(barPane.getBackground());
		add(p, "1, 0");
		paneIndex = "2, 0";
		add(resultPane, paneIndex);
		add(createToolBar(), "3, 0");
	}
	
	/**
	 * Returns the tool bar.
	 * 
	 * @return See above.
	 */
	private JComponent createToolBar()
	{
		toolBar = new JToolBar();
		toolBar.setOpaque(false);
		toolBar.setFloatable(false);
		toolBar.setBorder(null);
		buttonIndex = 0;
		toolBar.add(exceptionButton);
		toolBar.add(Box.createHorizontalStrut(5));
		buttonIndex = 2;
		toolBar.add(cancelButton);
		JLabel l = new JLabel();
		Font f = l.getFont();
		l.setForeground(UIUtilities.LIGHT_GREY.darker());
		l.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
		String s = UIUtilities.formatDefaultDate(null);
		String[] values = s.split(" ");
		if (values.length > 1) {
			String v = values[1];
			if (values.length > 2) v +=" "+values[2];
			l.setText(v);
			toolBar.add(Box.createHorizontalStrut(5));
			toolBar.add(l);
			toolBar.add(Box.createHorizontalStrut(5));
		}
		return toolBar;
	}
	
	/** Resets the controls. */
	private void reset()
	{
		toolBar.remove(buttonIndex);
		toolBar.add(removeButton, buttonIndex);
		removeButton.setEnabled(true);
		exceptionButton.setVisible(false);
		status.setBusy(false);
		status.setVisible(false);
		statusPane = iconLabel;
		remove(statusPane);
		add(statusPane, "0, 0, CENTER, CENTER");
		repaint();
	}
	
	/**
	 * Converts the passed mapped.
	 * 
	 * @param m The map to handle.
	 * @return See above.
	 */
	private Map<String, Object> convertResult(Map<String, Object> m)
	{
		Map<String, Object> objects = new HashMap<String, Object>();
		if (m == null) return objects;
		messageLabel.setText("");
		Object v = m.get(MESSAGE);
		if (v != null) {
			if (v instanceof String)
				messageLabel.setText((String) v);
		}
		m.remove(MESSAGE);
		if (m.containsKey(STD_ERR)) {
			errorObject = m.get(STD_ERR);
			m.remove(STD_ERR);
		}
		if (m.containsKey(STD_OUT)) {
			infoObject = m.get(STD_OUT);
			m.remove(STD_OUT);
		}
		return m;
	}
	
	/** Shows the exception. */
	private void showException()
	{
		if (exception == null) return;
		viewer.notifyError(type.getText(), messageLabel.getText(), exception);
	}
	
    /**
     * Returns the identifier of the plugin to run.
     * 
     * @return See above.
     */
    private int runAsPlugin()
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (env == null) return -1;
    	return env.runAsPlugin();
    }
    
	/**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param registry Convenience reference for subclasses.
     * @param ctx The security context.
     */
	ActivityComponent(UserNotifier viewer, Registry registry,
			SecurityContext ctx)
	{
		if (viewer == null) throw new NullPointerException("No viewer.");
    	if (registry == null) throw new NullPointerException("No registry.");
    	this.viewer = viewer;
    	this.registry = registry;
    	this.ctx = ctx;
	}
	
	/**
	 * Initializes the components.
	 * 
	 * @param text		The text of the activity.
     * @param icon		The icon to display then done.
	 */
	void initialize(String text, Icon icon)
	{
		initComponents(text, icon);
		buildGUI();
	}

    /**
     * Creates a button.
     * 
     * @param text The text of the button.
     * @param actionID The action command id.
     * @param l The action listener.
     * @return See above.
     */
    JButton createButton(String text, int actionID, ActionListener l)
    {
    	JButton b = UIUtilities.createHyperLinkButton(text);
		b.setActionCommand(""+actionID);
		b.addActionListener(l);
		return b;
    }

    /**
	 * Returns the name to give to the file.
	 * 
	 * @param files Collection of files in the currently selected directory.
	 * @param fileName The name of the original file.
	 * @param original The name of the file. 
	 * @param dirPath Path to the directory.
	 * @param index The index of the file.
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
        if (CommonsLangUtils.isEmpty(fileName)) return original;
    	
    	if (CommonsLangUtils.isNotEmpty(extension)) {
    		int n = fileName.lastIndexOf(extension);
    		String v = fileName.substring(0, n)+"_("+index+")"+extension;
    		index++;
    		return getFileName(files, fileName, v, dirPath, index, extension);
    	} else {
    		int lastDot = fileName.lastIndexOf(".");
    		if (lastDot != -1) {
        		extension = fileName.substring(lastDot, fileName.length());
        		String v = fileName.substring(0, lastDot)+"_("+index+")"+
        		extension;
        		index++;
        		return getFileName(files, fileName, v, dirPath, index, null);
        	} 
    	}
    	
    	return original;
	}
	
	/** Invokes when the activity has been cancelled. */
	public void onActivityCancelled()
	{
		reset();
		firePropertyChange(UNREGISTER_ACTIVITY_PROPERTY, null, this);
		notifyActivityCancelled();
		EventBus bus = registry.getEventBus();
		bus.post(new ActivityProcessEvent(this, false));
	}
	
	/** Invokes when the call-back has been set. */
	public void onCallBackSet()
	{
		cancelButton.setEnabled(true);
	}
	
	/** Invokes when the activity starts. */ 
	public void startActivity()
	{
		status.setBusy(true);
	}
	
	/**
	 * Returns <code>true</code> if the result can be displayed, 
	 * <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	boolean canPlotResult(Object object)
	{
		if (object instanceof FileAnnotationData) {
			FileAnnotationData fa = (FileAnnotationData) object;
			if (fa.isLoaded()) {
				return fa.getFileName().endsWith("."+CSVFilter.CSV);
			}
		}
		return false;
	}
	
	/** 
	 * Downloads the passed object is supported.
	 * 
	 * @param text   The text used if the object is not loaded.
	 * @param object The object to handle.
	 * @param folder Indicates where to download the file or <code>null</code>.
	 * @param deleteWhenFinished If set file is deleted after download finished
	 */
	void download(String text, Object object, File folder, final boolean deleteWhenFinished)
	{
		if (!(object instanceof FileAnnotationData || 
				object instanceof OriginalFile)) return;
		int index = -1;
		if (text == null) text = "";
		String name = "";
		String description = "";
		long dataID = -1;
		OriginalFile of = null;
		FileAnnotationData fa = null;
		if (object instanceof FileAnnotationData) {
			fa = (FileAnnotationData) object;
			if (fa.isLoaded()) {
				name = fa.getFileName();
				description = fa.getDescription();
				of = (OriginalFile) fa.getContent();
			} else {
				of = null;
				dataID = fa.getId();
				index = DownloadActivityParam.FILE_ANNOTATION;
				if (text.length() == 0) text = "Annotation";
				name = text+"_"+dataID;
			}
		} else {
			of = (OriginalFile) object;
			if (!of.isLoaded()) {
				dataID = of.getId().getValue();
				index = DownloadActivityParam.ORIGINAL_FILE;
				if (text.length() == 0) text = "File";
				name = text+"_"+dataID;
				of = null;
			}
		}
		final OriginalFile original = of;
		final int type = index;
		final String desc = description;
		final long id = dataID;
		if (folder != null) {
			if (original == null && type == -1) return;
			DownloadActivityParam activity;
			IconManager icons = IconManager.getInstance(registry);
			if (original != null) {
				activity = new DownloadActivityParam(original,
						folder, icons.getIcon(IconManager.DOWNLOAD_22));
				
			} else {
				activity = new DownloadActivityParam(id, type,
						folder, icons.getIcon(IconManager.DOWNLOAD_22));
			}
			activity.setLegend(desc);
			activity.setUIRegister(true);
			if (fa != null && deleteWhenFinished)
				activity.setToDelete(fa);
			activity.setOverwrite(true);
			viewer.notifyActivity(ctx, activity);
			return;
		}
		JFrame f = registry.getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Download", "Select where to download the file.", null, 
				true, true);
		IconManager icons = IconManager.getInstance(registry);
		chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
		chooser.setSelectedFileFull(name);
		chooser.setApproveButtonText("Download");
		final FileAnnotationData anno = fa;
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					File[] files = (File[]) evt.getNewValue();
					File folder = files[0];
					if (original == null && type == -1) return;
					IconManager icons = IconManager.getInstance(registry);
					DownloadActivityParam activity;
					if (original != null) {
						activity = new DownloadActivityParam(original,
								folder, icons.getIcon(IconManager.DOWNLOAD_22));
						
					} else {
						activity = new DownloadActivityParam(id, type,
								folder, icons.getIcon(IconManager.DOWNLOAD_22));
					}
					activity.setLegend(desc);
					if (anno != null && deleteWhenFinished)
						activity.setToDelete(anno);
					viewer.notifyActivity(ctx, activity);
				}
			}
		});
		chooser.centerDialog();
	}
	
	/** 
	 * Downloads the passed object is supported.
	 * 
	 * @param text   The text used if the object is not loaded.
	 * @param object The object to handle.
	 */
	void download(String text, Object object)
	{
		download(text, object, null, false);
	}
	
	/**
	 * Views the passed object if supported.
	 * 
	 * @param object The object to view.
	 * @param source The UI component invoking the method.
	 */
	void view(Object object, JComponent source)
	{
		if (object instanceof FileAnnotationData || 
				object instanceof OriginalFile) {
			open(object, null, source);
		} else if (object instanceof File) {
			viewer.openApplication(null, ((File) object).getAbsolutePath());
			if (source != null) source.setEnabled(true);
		} else {
			EventBus bus = registry.getEventBus();
			//Check if running as plug-in
			ViewObjectEvent evt = new ViewObjectEvent(ctx, object, source);
			evt.setPlugin(runAsPlugin());
			bus.post(evt);
		}
	}
	
	/**
	 * Browses the node.
	 * 
	 * @param object The object to view.
	 * @param source The UI component invoking the method.
	 */
	void browse(Object object, JComponent source)
	{
		EventBus bus = registry.getEventBus();
		ViewObjectEvent evt = new ViewObjectEvent(ctx, object, source);
		evt.setBrowseObject(true);
		bus.post(evt);
	}
	
	/**
	 * Returns <code>true</code> if information returned by script,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasInfo() { return infoObject != null; }
	
	/**
	 * Returns <code>true</code> if information returned by script,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasError() { return errorObject != null; }
	
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
	 * @param message The reason of the error.
	 * @param ex The exception to handle.
	 */
	public void notifyError(String text, String message, Throwable ex)
	{
		reset();
		
		int status = -1;
		if (ex != null) {
			Throwable cause = ex.getCause();
			if (cause instanceof ProcessException) {
				status = ((ProcessException) cause).getStatus();
				if (status == ProcessException.NO_PROCESSOR)
					messageLabel.setText("No processor available. " +
							"Please try later.");
			}
		}
		
		if (text != null) {
			type.setText(text);
			if (message != null)
				type.setToolTipText(message);
		}
		//if (message != null) messageLabel.setText(message);
		exception = ex;
		if (exception != null && status != ProcessException.NO_PROCESSOR) {
			exceptionButton.setVisible(true);
			exceptionButton.setToolTipText(
					UIUtilities.formatExceptionForToolTip(ex));
		}
		firePropertyChange(UNREGISTER_ACTIVITY_PROPERTY, null, this);
		notifyActivityError();
		EventBus bus = registry.getEventBus();
		bus.post(new ActivityProcessEvent(this, true));
	}
	
	/**
	 * Shows the menu corresponding to the passed index.
	 * 
	 * @param src The source of the click
	 * @param index The index indicating which menu to show.
	 * @param x The x-coordinate of the mouse clicked.
	 * @param y The y-coordinate of the mouse clicked.
	 */
	private void showMenu(JComponent src, int index, int x, int y)
	{
		switch (index) {
			case ERROR:
				if (errorMenu == null)
					errorMenu = new ActivityResultPopupMenu(errorObject, this);
				errorMenu.show(src, x, y);
				break;
			case INFO:
				if (infoMenu == null)
					infoMenu = new ActivityResultPopupMenu(infoObject, this);
				infoMenu.show(src, x, y);
		}
	}
	
	/** 
	 * Invokes when the activity end. 
	 * 
	 * @param result The result of the activity.
	 */ 
	public void endActivity(Object result)
	{
		this.result = result;
		boolean busy = status.isBusy();
		reset();
		if (result instanceof Map) {
			Map<String, Object> m = convertResult((Map<String, Object>) result);
			int size = m.size();
			this.result = m;
			remove(resultPane);
			Color c = getBackground();
			if (size == 0) {
				JToolBar row = new JToolBar();
				row.setOpaque(false);
				row.setFloatable(false);
				row.setBorder(null);
				row.setBackground(c);
				JButton button;
				if (errorObject != null) {
					button = createButton(ActivityResultRow.ERROR_TEXT, 
							ERROR, this);
					button.addMouseListener(new MouseAdapter() {
						
						public void mouseReleased(MouseEvent e) {
							showMenu((JComponent) e.getSource(), ERROR,
									e.getX(), e.getY()); 
						}
					});
					row.add(button);
				}
				if (infoObject != null) {
					button = createButton(ActivityResultRow.INFO_TEXT, 
							INFO, this);
					button.addMouseListener(new MouseAdapter() {
						
						public void mouseReleased(MouseEvent e) {
							showMenu((JComponent) e.getSource(), INFO,
									e.getX(), e.getY()); 
						}
					});
					row.add(button);
				}
				add(row, paneIndex);
			} else {
				Entry<String, Object> entry;
				Iterator<Entry<String, Object>> i = m.entrySet().iterator();
				ActivityResultRow row = null;
				JPanel content = new JPanel();
				content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
				
				content.setBackground(c);
				int index = 0;
				int max = 2;
				JButton moreButton = null;
				while (i.hasNext()) {
					entry = (Entry<String, Object>) i.next();
					this.result = entry.getValue();
					row = new ActivityResultRow((String) entry.getKey(), 
							entry.getValue(), this);
					row.setBackground(c);
					row.addPropertyChangeListener(listener);
					resultButtons.add(row);
					if (index < max)
						content.add(row);
					else {
						if (moreButton == null) {
							moreButton = createButton(""+(m.size()-max)+" more",
									ALL_RESULT, this);
							content.add(moreButton);
						}
					}
					index++;
				}
				
				if (m.size() == 1) add(row, paneIndex);
				else add(content, paneIndex);
				resultPane = content;
			}
			
			repaint();
		}
		
		firePropertyChange(UNREGISTER_ACTIVITY_PROPERTY, null, this);
		notifyActivityEnd();
		//Post an event to 
		//if (busy) {
		EventBus bus = registry.getEventBus();
		bus.post(new ActivityProcessEvent(this, busy));
		//}
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
	protected abstract void notifyActivityCancelled();
	
	/** Subclasses should override the method. */
	protected abstract void notifyActivityEnd();
	
	/** Subclasses should override the method. */
	protected abstract void notifyActivityError();
	
	/** Creates a loader. */
	protected abstract UserNotifierLoader createLoader();
	
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
			case CANCEL:
				onActivityCancelled();
				if (cancelButton != null) cancelButton.setEnabled(false);
				if (loader != null) loader.cancel();
				break;
			case EXCEPTION:
				showException();
				break;
			case ALL_RESULT:
				Iterator<ActivityResultRow> i = resultButtons.iterator();
				JPanel content = new JPanel();
				content.setBackground(getBackground());
				content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
				while (i.hasNext()) {
					content.add(i.next());
				}
				remove(resultPane);
				add(content, paneIndex);
				resultPane = content;
				validate();
				repaint();
				break;
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
		if (resultPane != null) resultPane.setBackground(color);
		if (removeButton != null) removeButton.setBackground(color);
		if (cancelButton != null) cancelButton.setBackground(color);
		if (exceptionButton != null) exceptionButton.setBackground(color);
	}
	
}
