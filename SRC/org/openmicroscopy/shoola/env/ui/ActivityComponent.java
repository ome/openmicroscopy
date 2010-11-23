/*
 * org.openmicroscopy.shoola.env.ui.ActivityComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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



//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnalysisResultsHandlingParam;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.filter.file.CSVFilter;
import org.openmicroscopy.shoola.util.filter.file.GIFFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import omero.model.OriginalFile;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.ProjectData;

/**
 * Top class that each action should extend.
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

	/** Bound property indicating to remove the entry from the display. */
	static final String 	REMOVE_ACTIVITY_PROPERTY = "removeActivity";
	
	/** Bound property indicating to unregister the activity. */
	static final String 	UNREGISTER_ACTIVITY_PROPERTY = "unregisterActivity";

	/** Text indicating to view the object. */
	static final String   VIEW_TEXT = "View";
	
	/** Text indicating to browse the object. */
	private static final String   BROWSE_TEXT = "Browse";
	
	/** The default dimension of the status. */
	private static final Dimension SIZE = new Dimension(22, 22);
	
	/** ID to remove the entry from the display. */
	private static final int 	REMOVE = 0;
	
	/** ID to download the result. */
	private static final int 	DOWNLOAD = 1;
	
	/** ID to cancel the activity. */
	private static final int 	CANCEL = 2;
	
	/** ID to view the object e.g. the image. */
	private static final int	VIEW = 3;
	
	/** ID to display the standard output. */
	private static final int	INFO = 4;
	
	/** ID to show the result. */
	private static final int	RESULT = 5;
	
	/** ID to display the standard error. */
	private static final int	ERROR = 6;
	
	/** ID to display the standard error. */
	private static final int	EXCEPTION = 7;
	
	/** ID to plot the result. */
	private static final int 	PLOT = 8;
	
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
	
	/** One of the constants defined by this class. */
	private int							index;
	
	/** The index of the {@link #cancelButton} or {@link #removeButton}. */
	private int							buttonIndex;
	
	/** The tool bar displaying controls. *. */
	private JToolBar					toolBar;
	
	/** Button to shows the exception. */
	private JButton						exceptionButton;
	
	/** Menu displaying the option to view the standard error. */
	private ActivityResultMenu			errorMenu;
	
	/** Menu displaying the option to view the standard output. */
	private ActivityResultMenu			infoMenu;
	
	/** The exception thrown while running the script. */
	private Throwable					exception;
	
	/** Button to download the result depending on the type of activity. */
	protected JButton					downloadButton;
	
	/** Button to view the result depending on the type of activity. */
	protected JButton					viewButton;
	
	/** Button to show the standard output menu. */
	protected JButton					infoButton;
	
	/** Button to show the general result. */
	protected JButton					resultButton;
	
	/** Button to show the error. */
	protected JButton					errorButton;
	
	/** Button to cancel the activity. */
	private JButton						plotButton;
	
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
    
    /** Loader associated to the activity. */
    protected UserNotifierLoader 		loader;
    
    /**
	 * Opens the passed object. Downloads it first.
	 * 
	 * @param object The object to open.
	 * @param parameters Either Analysis parameters or Application data.
	 */
	private void open(Object object, Object parameters)
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
		DownloadActivityParam activity;
		if (index != -1) 
			activity = new DownloadActivityParam(id, index, f, null);
		else 
			activity = new DownloadActivityParam(of, f, null);
		if (parameters instanceof ApplicationData) {
			activity.setApplicationData((ApplicationData) parameters);
		} else if (parameters instanceof AnalysisResultsHandlingParam) {
			activity.setResults((AnalysisResultsHandlingParam) parameters);
		}
		viewer.notifyActivity(activity);
	}
	
	/** 
	 * Initializes the components. 
	 * 
	 * @param text The type of activity.
	 * @param icon The icon to display when done.
	 */
	private void initComponents(String text, Icon icon)
	{
		exceptionButton = createButton("Error", EXCEPTION, this);
		exceptionButton.setVisible(false);
		removeButton = createButton("Remove", REMOVE, this);
		cancelButton = createButton("Cancel", CANCEL, this);
		//if (index == ADVANCED)
		downloadButton = createButton("Download", DOWNLOAD, this);
		downloadButton.setVisible(false);
		plotButton = createButton("Plot", PLOT, this);
		plotButton.setVisible(false);
		viewButton = createButton(VIEW_TEXT, VIEW, this);
		viewButton.setVisible(false);
		infoButton = createButton("Info", INFO, this);
		infoButton.setVisible(false);
		resultButton = createButton("Show result", RESULT, this);
		resultButton.setVisible(false);
		errorButton = createButton("Error", ERROR, this);
		errorButton.setVisible(false);
		errorButton.addMouseListener(new MouseAdapter() {
			
			/** 
			 * Displays the menu.
			 * @see MouseAdapter#mouseReleased(MouseEvent) 
			 */
			public void mouseReleased(MouseEvent e) {
				Point p = e.getPoint();
				errorMenu.show((Component) e.getSource(), p.x, p.y);
			}
			
		});
		infoButton.addMouseListener(new MouseAdapter() {
			
			/** 
			 * Displays the menu.
			 * @see MouseAdapter#mouseReleased(MouseEvent) 
			 */
			public void mouseReleased(MouseEvent e) {
				Point p = e.getPoint();
				infoMenu.show((Component) e.getSource(), p.x, p.y);
			}
			
		});
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
		toolBar = new JToolBar();
		toolBar.setOpaque(false);
		toolBar.setFloatable(false);
		toolBar.setBorder(null);
		buttonIndex = 0;
		//if (index == ADVANCED) {
		toolBar.add(exceptionButton);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(downloadButton);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(viewButton);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(resultButton);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(errorButton);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(infoButton);
		toolBar.add(Box.createHorizontalStrut(5));
		//toolBar.add(infoButton);
		//toolBar.add(Box.createHorizontalStrut(5));
		buttonIndex = 12;
		//}
		toolBar.add(cancelButton);
		JLabel l = new JLabel();
		Font f = l.getFont();
		l.setForeground(UIUtilities.LIGHT_GREY.darker());
		l.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
		String s = UIUtilities.formatShortDateTime(null);
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
		downloadButton.setVisible(false);
		viewButton.setVisible(false);
		infoButton.setVisible(false);
		resultButton.setVisible(false);
		errorButton.setVisible(false);
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
			errorButton.setVisible(true);
			errorMenu = new ActivityResultMenu(m.get(STD_ERR), this);
			m.remove(STD_ERR);
		}
		if (m.containsKey(STD_OUT)) {
			infoButton.setVisible(true);
			infoMenu = new ActivityResultMenu(m.get(STD_OUT), this);
			m.remove(STD_OUT);
		}
		return m;
	}
	
	/** Displays the result. */
	private void showResult()
	{
		JFrame f = registry.getTaskBar().getFrame();
		ActivityResultDialog d = new ActivityResultDialog(f, this, result);
		UIUtilities.centerAndShow(d);
	}
	
	/** Shows the exception. */
	private void showException()
	{
		if (exception == null) return;
		viewer.notifyError(type.getText(), messageLabel.getText(), exception);
		
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
	ActivityComponent(UserNotifier viewer, Registry registry, String 
			text, Icon icon)
	{
		if (viewer == null) throw new NullPointerException("No viewer.");
    	if (registry == null) throw new NullPointerException("No registry.");
    	this.viewer = viewer;
    	this.registry = registry;
		initComponents(text, icon);
		buildGUI();
	}

	/**
	 * Sets the index associated to this activity.
	 * 
	 * @param index The value to set.
	 */
	void setIndex(int index) { this.index = index; }
	
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
    	JButton b = new JButton(text);
    	Font f = b.getFont();
    	b.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
		b.setActionCommand(""+actionID);
		b.addActionListener(l);
		b.setOpaque(false);
		b.setForeground(UIUtilities.HYPERLINK_COLOR);
		UIUtilities.unifiedButtonLookAndFeel(b);
		return b;
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
		notifyActivityCancelled();
		firePropertyChange(UNREGISTER_ACTIVITY_PROPERTY, null, this);
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
	 * Returns the text of the viewable object.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	String getViewText(Object object)
	{
		if (!isViewable(object)) return VIEW_TEXT;
		if (object instanceof DatasetData || object instanceof ProjectData) 
			return BROWSE_TEXT;
		return VIEW_TEXT;
	}
	
	/**
	 * Returns <code>true</code> if the object can be viewed, 
	 * <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	boolean isViewable(Object object)
	{
		String mimetype = null;
		if (object instanceof ImageData) return true;
		if (object instanceof DatasetData) return true;
		if (object instanceof ProjectData) return true;
		if (object instanceof FileAnnotationData) {
			FileAnnotationData fa = (FileAnnotationData) object;
			if (fa.isLoaded()) {
				OriginalFile of = (OriginalFile) fa.getContent();
				if (of.isLoaded() && of.getMimetype() != null)
					mimetype = of.getMimetype().getValue();
			}
		} else if (object instanceof OriginalFile) {
			OriginalFile of = (OriginalFile) object;
			if (of.isLoaded() && of.getMimetype() != null)
				mimetype = of.getMimetype().getValue();
		}
		if (mimetype != null) {
			return (JPEGFilter.MIMETYPE.equals(mimetype) || 
					PNGFilter.MIMETYPE.equals(mimetype) ||
					TIFFFilter.MIMETYPE.equals(mimetype) ||
					GIFFilter.MIMETYPE.equals(mimetype));
		}
		return false;
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
	 * Returns <code>true</code> if the object can be downloaded, 
	 * <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	boolean isDownloadable(Object object)
	{
		return (object instanceof FileAnnotationData || 
				object instanceof OriginalFile);
	}
	
	/** 
	 * Downloads the passed object is supported.
	 * 
	 * @param text   The text used if the object is not loaded.
	 * @param object The object to handle.
	 * 
	 */
	void download(String text, Object object)
	{
		if (!(object instanceof FileAnnotationData || 
				object instanceof OriginalFile)) return;
		int index = -1;
		if (text == null) text = "";
		String name = "";
		String description = "";
		long dataID = -1;
		OriginalFile of = null;
		if (object instanceof FileAnnotationData) {
			FileAnnotationData data = (FileAnnotationData) object;
			if (data.isLoaded()) {
				name = data.getFileName();
				description = data.getDescription();
				of = (OriginalFile) data.getContent();
			} else {
				of = null;
				dataID = data.getId();
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
		JFrame f = registry.getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Download", "Select where to download the file.", null, 
				true);
		IconManager icons = IconManager.getInstance(registry);
		chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
		chooser.setSelectedFileFull(name);
		chooser.setApproveButtonText("Download");
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					File folder = (File) evt.getNewValue();
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
					/*
					activity.setLegendExtension(
							DownloadActivity.LEGEND_TEXT_CSV);
							*/
					viewer.notifyActivity(activity);
				}
			}
		});
		chooser.centerDialog();
	}
	
	/**
	 * Views the passed object if supported.
	 * 
	 * @param object The object to view.
	 */
	void view(Object object)
	{
		if (object instanceof FileAnnotationData || 
				object instanceof OriginalFile) {
			open(object, new ApplicationData(""));
		} else if (object instanceof File) {
			viewer.openApplication(null, ((File) object).getAbsolutePath());
		} else {
			EventBus bus = registry.getEventBus();
			bus.post(new ViewObjectEvent(object));
		}
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
	 * @param message The reason of the error.
	 * @param ex The exception to handle.
	 */
	public void notifyError(String text, String message, Throwable ex)
	{
		reset();
		if (text != null) type.setText(text);
		if (message != null) messageLabel.setText(message);
		exception = ex;
		if (exception != null) {
			exceptionButton.setVisible(true);
		}
		notifyActivityError();
		firePropertyChange(UNREGISTER_ACTIVITY_PROPERTY, null, this);
		EventBus bus = registry.getEventBus();
		bus.post(new ActivityProcessEvent(this, false));
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
			if (size == 1) {
				Entry entry;
				Iterator i = m.entrySet().iterator();
				while (i.hasNext()) {
					entry = (Entry) i.next();
					this.result = entry.getValue();
				}
			} else this.result = m;
		}
		downloadButton.setVisible(isDownloadable(this.result));
		if (isViewable(this.result)) {
			viewButton.setText(getViewText(this.result));
			viewButton.setVisible(true);
		} else viewButton.setVisible(false);
		
		if (!viewButton.isVisible() && !downloadButton.isVisible()) {
			if (this.result instanceof Collection) {
				Collection l = (Collection) this.result;
				if (this instanceof DeleteActivity)
					resultButton.setText("Show error");
				resultButton.setVisible(l.size() > 0);
			} else if (this.result instanceof Map) {
				Map l = (Map) this.result;
				resultButton.setVisible(l.size() > 0);
			}
		}
		notifyActivityEnd();
		firePropertyChange(UNREGISTER_ACTIVITY_PROPERTY, null, this);
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
			case DOWNLOAD:
				download("", result);
				break;
			case CANCEL:
				if (loader != null) loader.cancel();
				break;
			case VIEW:
				view(result);
				break;
			case RESULT:
				showResult();
				break;
			case EXCEPTION:
				showException();
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
		if (cancelButton != null) cancelButton.setBackground(color);
		if (downloadButton != null) downloadButton.setBackground(color);
		if (viewButton != null) viewButton.setBackground(color);
		if (infoButton != null) infoButton.setBackground(color);
		if (errorButton != null) errorButton.setBackground(color);
		if (exceptionButton != null) exceptionButton.setBackground(color);
	}
	
}
