/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import omero.cmd.CmdCallback;
import omero.cmd.CmdCallbackI;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.events.importer.BrowseContainer;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.ThumbnailLabel;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.env.data.util.Status;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FilesetData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Component hosting the file to import and displaying the status of the 
 * import process.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @version 3.0
 * @since 3.0-Beta4
 */
public class FileImportComponent
	extends JPanel
	implements PropertyChangeListener, FileImportComponentI
{
	/** The default size of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);

	/** Text indicating that the folder does not contain importable files.*/
	private static final String EMPTY_FOLDER = "No data to import";
	
	/** The maximum width used for the component.*/
	private static final int LENGTH = 350;
	
	/** 
	private static final String EMPTY_DIRECTORY = "No data to import";
	
	/** One of the constants defined by this class. */
	private int type;

	/** The component indicating the progress of the import. */
	private JXBusyLabel busyLabel;
	
	/** The component displaying the file name. */
	private JPanel namePane;

	/** The component displaying the result. */
	private JLabel resultLabel;
	
	/** The component displaying the imported image. */
	private ThumbnailLabel imageLabel;
	
	/** Keeps track of the extra images if any. */
	private List<ThumbnailLabel> imageLabels;

	/** The imported image. */
	private Object image;

	/** Indicates the status of the on-going import. */
	private Status status;
	
	/** The component displaying the name of the file. */
	private JLabel fileNameLabel;
	
	/** Keep tracks of the components. */
	private Map<File, FileImportComponent> components;
	
	/** The mouse adapter to view the image. */
	private MouseAdapter adapter;

	/** The data object corresponding to the folder. */
	private DataObject containerFromFolder;
	
	/** Button to cancel the import for that file. */
	private JButton cancelButton;
	
	/** The node where to import the folder. */
	private DataObject data;
	
	/** The dataset if any. */
	private DatasetData dataset;
	
	/** The node of reference if any. */
	private Object refNode;
	
	/** The object where the data have been imported.*/
	private DataObject containerObject;
	
	/** The component used when importing a folder. */
	private JXTaskPane pane;
	
	/** The parent of the node. */
	private FileImportComponentI parent;

	/** 
	 * Flag indicating that the container hosting the imported image
	 * can be browsed or not depending on how the import is launched.
	 */
	private boolean browsable;
	
	/** Set to <code>true</code> if attempt to re-import.*/
	private boolean reimported;
	
	/** Flag indicating the the user is member of one group only.*/
	private boolean singleGroup;

	/** The button displayed the various options post if the import worked.*/
	private JButton actionMenuButton;
	
	/** The popup menu associated with the action button */
	private JPopupMenu menu;
	
	/** The state of the import */
	private ImportStatus resultIndex;
	
	/** The index associated to the main component.*/
	private int index;
	
	/** Reference to the callback.*/
	private CmdCallback callback;
	
	/** The importable object.*/
	private ImportableFile importable;
	
	/** The collection of tags added to the imported images.*/
	private Collection<TagAnnotationData> tags;
	
	/** The label indicating the status of the import.*/
	private JLabel refLabel;
	
	/** The button displaying the available action.*/
	private JButton refButton;
	
	/** Retries to upload the file.*/
	private void retry()
	{
		Object o = status.getImportResult();
		if (o instanceof Exception || image instanceof Exception)
			firePropertyChange(RETRY_PROPERTY, null, this);
	}
	/**
	 * Creates or recycles the menu corresponding to the import status.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createActionMenu()
	{
	    if (menu != null) return menu;
	    menu = new JPopupMenu();
	    JMenuItem item;
	    String logText = "View Import Log";
	    String checksumText = "View Checksum";
	    String exceptionText = "View Exception";
	    String copyExceptionText = "Copy Exception to Clipboard";
	    Object result = status.getImportResult();
	    switch (resultIndex) {
	    case FAILURE_LIBRARY:
	        menu.add(new JMenuItem(new AbstractAction(exceptionText) {
                public void actionPerformed(ActionEvent e) {
                    viewError();
                }
            }));
	        menu.add(new JMenuItem(new AbstractAction(copyExceptionText) {
                public void actionPerformed(ActionEvent e) {
                    copyErrorToClipboard();
                }
            }));
	        break;
	    case FAILURE:
	        menu.add(new JMenuItem(new AbstractAction("Submit") {
	            public void actionPerformed(ActionEvent e) {
	                submitError();
	            }
	        }));
	        menu.add(new JMenuItem(new AbstractAction(exceptionText) {
                public void actionPerformed(ActionEvent e) {
                    viewError();
                }
            }));
	        menu.add(new JMenuItem(new AbstractAction(copyExceptionText) {
                public void actionPerformed(ActionEvent e) {
                    copyErrorToClipboard();
                }
            }));
	        break;
	    case UPLOAD_FAILURE:
	        menu.add(new JMenuItem(new AbstractAction("Retry") {
	            public void actionPerformed(ActionEvent e) {
	                retry();
	            }
	        }));
	        break;
	    case SUCCESS:
	        logText = "Import Log";
	        checksumText = "Checksum";
	        item = new JMenuItem(new AbstractAction("In Full Viewer") {
	            public void actionPerformed(ActionEvent e) {
	                launchFullViewer();
	            }
	        });
	        boolean b = false;
	        if (result instanceof Collection)
	            b = ((Collection) result).size() == 1;
	        item.setEnabled(b && !status.isHCS());
	        menu.add(item);
	        item = new JMenuItem(new AbstractAction("In Data Browser") {
	            public void actionPerformed(ActionEvent e) {
	                browse();
	            }
	        });
	        item.setEnabled(browsable);
	        menu.add(item);
	    }
	    item = new JMenuItem(new AbstractAction(logText) {
	        public void actionPerformed(ActionEvent e) {
	            displayLogFile();
	        }
	    });
	    item.setEnabled(status.getLogFileID() > 0);
	    menu.add(item);

	    item = new JMenuItem(new AbstractAction(checksumText) {
	        public void actionPerformed(ActionEvent e) {
	            showChecksumDetails();
	        }
	    });
	    item.setEnabled(status.hasChecksum());
	    menu.add(item);
	    return menu;
	}

	/** Displays or loads the log file.*/
	private void displayLogFile()
	{
		firePropertyChange(LOAD_LOGFILEPROPERTY, null, this);
	}

	/**
	 * Displays the checksum details dialog for the file(s) in this entry
	 */
	private void showChecksumDetails()
	{
		firePropertyChange(CHECKSUM_DISPLAY_PROPERTY, null, status);
	}

	/**
	 * Formats the tool tip of a successful import.
	 * 
	 * @return See above.
	 */
	private void formatResultTooltip()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body>");
		buf.append("<b>Date Uploaded: </b>");
		buf.append(UIUtilities.formatShortDateTime(null));
		buf.append("<br>");
		if (image instanceof PlateData) {
			PlateData p = (PlateData) image;
			buf.append("<b>Plate ID: </b>");
			buf.append(p.getId());
			buf.append("<br>");
		}
		if (!status.isHCS()) {
			Object o = status.getImportResult();
			if (o instanceof Set) {
				Set<PixelsData> list = (Set<PixelsData>) o;
				int n = list.size();
				if (n == 1) {
					buf.append("<b>Image ID: </b>");
					Iterator<PixelsData> i = list.iterator();
					while (i.hasNext()) {
						buf.append(i.next().getImage().getId());
						buf.append("<br>");
					}
				} else if (n > 1) {
					buf.append("<b>Number of Images: </b>");
					buf.append(n);
					buf.append("<br>");
				}
			}
		}
		buf.append("<b>Size: </b>");
		buf.append(FileUtils.byteCountToDisplaySize(status.getSizeUpload()));
		buf.append("<br>");
		buf.append("<b>Group: </b>");
		buf.append(importable.getGroup().getName());
		buf.append("<br>");
		buf.append("<b>Owner: </b>");
		buf.append(EditorUtil.formatExperimenter(importable.getUser()));
		buf.append("<br>");
		if (containerObject instanceof ProjectData) {
			buf.append("<b>Project: </b>");
			buf.append(((ProjectData) containerObject).getName());
			buf.append("<br>");
		} else if (containerObject instanceof ScreenData) {
			buf.append("<b>Screen: </b>");
			buf.append(((ScreenData) containerObject).getName());
			buf.append("<br>");
		} else if (containerObject instanceof DatasetData) {
			buf.append("<b>Dataset: </b>");
			buf.append(((DatasetData) containerObject).getName());
			buf.append("<br>");
		} else if (dataset != null) {
			buf.append("<b>Dataset: </b>");
			buf.append(dataset.getName());
			buf.append("<br>");
		}
		if (!CollectionUtils.isEmpty(tags)) {
			buf.append("<b>Tags: </b>");
			Iterator<TagAnnotationData> i = tags.iterator();
			while (i.hasNext()) {
				buf.append(i.next().getTagValue());
				buf.append(" ");
			}
		}
		buf.append("</body></html>");
		String tip = buf.toString();
		fileNameLabel.setToolTipText(tip);
		resultLabel.setToolTipText(tip);
	}
	
	/** Indicates that the import was successful or if it failed.*/
	private void formatResult()
	{
		if (callback != null) {
			try {
				((CmdCallbackI) callback).close(true);
			} catch (Exception e) {}
		}
		if (namePane.getPreferredSize().width > LENGTH)
			fileNameLabel.setText(EditorUtil.getPartialName(
					getFile().getName()));
		resultLabel.setVisible(true);
		busyLabel.setVisible(false);
		busyLabel.setBusy(false);
		remove(refLabel);
		remove(refButton);
		refLabel = resultLabel;
		refButton = actionMenuButton;
		addControlsToDisplay();
		IconManager icons = IconManager.getInstance();
		Object result = status.getImportResult();
		if (image instanceof ImportException) result = image;
		if (result instanceof ImportException) {
			ImportException e = (ImportException) result;
			resultLabel.setIcon(icons.getIcon(IconManager.DELETE));
			resultLabel.setToolTipText(
					UIUtilities.formatExceptionForToolTip(e));
			actionMenuButton.setVisible(true);
			actionMenuButton.setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
			actionMenuButton.setText("Failed");
			int status = e.getStatus();
			if (status == ImportException.CHECKSUM_MISMATCH)
				resultIndex = ImportStatus.UPLOAD_FAILURE;
			else if (status == ImportException.MISSING_LIBRARY)
			    resultIndex = ImportStatus.FAILURE_LIBRARY;
			else resultIndex = ImportStatus.FAILURE;
		} else if (result instanceof CmdCallback) {
			callback = (CmdCallback) result;
		} else {
			formatResultTooltip();
			resultLabel.setIcon(icons.getIcon(IconManager.APPLY));
			actionMenuButton.setVisible(true);
			actionMenuButton.setForeground(UIUtilities.HYPERLINK_COLOR);
			actionMenuButton.setText("View");
			resultIndex = ImportStatus.SUCCESS;
		}
	}

	/** Submits the error.*/
	private void submitError()
	{
		Object o = status.getImportResult();
		if (o instanceof Exception)
			firePropertyChange(SUBMIT_ERROR_PROPERTY, null, this);
	}

	/** Views the error.*/
	private void viewError()
	{
	    Object o = status.getImportResult();
	    if (o instanceof ImportException) {
	        String v = UIUtilities.printErrorText((ImportException) o);
	        JFrame f = ImporterAgent.getRegistry().getTaskBar().getFrame();
	        EditorDialog d = new EditorDialog(f, v, EditorDialog.VIEW_TYPE);
	        d.allowEdit(false);
	        UIUtilities.centerAndShow(d);
	    }
	}

	/** Copies the error to the clipboard.*/
    private void copyErrorToClipboard()
    {
        Object o = status.getImportResult();
        if (o instanceof ImportException) {
            String v = UIUtilities.printErrorText((ImportException) o);
            UIUtilities.copyToClipboard(v);
        }
    }

	/** Browses the node or the data object. */
	private void browse()
	{
		EventBus bus = ImporterAgent.getRegistry().getEventBus();
		Object d = dataset;
		if (dataset == null || data instanceof ScreenData) d = data;
		if (d == null) return;
		bus.post(new BrowseContainer(d, null));
	}
	
	/**
	 * Indicates that the file will not be imported. 
	 * 
	 * @param fire	Pass <code>true</code> to fire a property,
	 * 				<code>false</code> otherwise.
	 */
	private void cancel(boolean fire)
	{
		boolean b = status.isCancellable() || getFile().isDirectory();
		if (!isCancelled() && !hasImportFailed() && b &&
		        !status.isMarkedAsDuplicate()) {
			busyLabel.setBusy(false);
			busyLabel.setVisible(false);
			status.markedAsCancel();
			cancelButton.setEnabled(false);
			cancelButton.setVisible(false);
			firePropertyChange(CANCEL_IMPORT_PROPERTY, null, this);
		}
	}

	/**
	 * Launches the full viewer for the selected item.
	 */
	private void launchFullViewer()
	{
		ViewImage evt;
		int plugin = ImporterAgent.runAsPlugin();
		if (image == null) image = status.getImportResult();
		Object ho = image;
		if (image instanceof Collection) {
			Collection l = (Collection) image;
			if (CollectionUtils.isEmpty(l) || l.size() > 1) return;
			Iterator<Object> i = l.iterator();
			while (i.hasNext()) {
				ho = i.next();
			}
		}
		if (ho instanceof ThumbnailData) {
			ThumbnailData data = (ThumbnailData) ho;
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			evt = new ViewImage(
					new SecurityContext(importable.getGroup().getId()),
					new ViewImageObject(data.getImageID()), null);
			evt.setPlugin(plugin);
			bus.post(evt);
		} else if (ho instanceof PixelsData) {
			PixelsData data = (PixelsData) ho;
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			evt = new ViewImage(
					new SecurityContext(importable.getGroup().getId()),
					new ViewImageObject(data.getImage().getId()), null);
			evt.setPlugin(plugin);
			bus.post(evt);
		} else if (image instanceof PlateData) {
			firePropertyChange(BROWSE_PROPERTY, null, image);
		}
	}
	/** Initializes the components. */
	private void initComponents()
	{
		actionMenuButton = new JButton();
		actionMenuButton.setVisible(false);
		actionMenuButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ev) {
				JPopupMenu popup = createActionMenu();
				popup.show(actionMenuButton, 0, actionMenuButton.getHeight());
			}
		});
		
		adapter = new MouseAdapter() {
			
			/**
			 * Views the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{ 
				if (e.getClickCount() == 1) {
					launchFullViewer();
				}
			}
		};
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		busyLabel = new JXBusyLabel(SIZE);
		busyLabel.setVisible(false);
		busyLabel.setBusy(false);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setForeground(UIUtilities.HYPERLINK_COLOR);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				cancelLoading();
			}
		});
		cancelButton.setVisible(true);
		
		namePane = new JPanel();
		namePane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		IconManager icons = IconManager.getInstance();
		Icon icon;
		if (getFile().isFile()) icon = icons.getIcon(IconManager.IMAGE);
		else icon = icons.getIcon(IconManager.DIRECTORY);
		imageLabel = new ThumbnailLabel(icon);
		imageLabel.addPropertyChangeListener(this);
		imageLabels = new ArrayList<ThumbnailLabel>();
		ThumbnailLabel label;
		for (int i = 0; i < MAX_THUMBNAILS; i++) {
			label = new ThumbnailLabel();
			if (i == MAX_THUMBNAILS-1) {
				Font f = label.getFont();
				label.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
			}
			label.setVisible(false);
			label.addPropertyChangeListener(this);
			imageLabels.add(label);
		}
		fileNameLabel = new JLabel(getFile().getName());
		namePane.add(imageLabel);
		Iterator<ThumbnailLabel> j = imageLabels.iterator();
		while (j.hasNext()) {
			namePane.add(j.next());
		}
		namePane.add(Box.createHorizontalStrut(4));
		namePane.add(fileNameLabel);
		namePane.add(Box.createHorizontalStrut(10));
		resultLabel = new JLabel();
		status = new Status(importable.getFile());
		status.addPropertyChangeListener(this);
		image = null;
		refButton = cancelButton;
		refLabel = busyLabel;
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		double[][] design = new double[][]
				{{LENGTH, TableLayout.FILL,
					TableLayout.PREFERRED, TableLayout.PREFERRED,
					TableLayout.PREFERRED, TableLayout.PREFERRED},
					{TableLayout.PREFERRED}};
		setLayout(new TableLayout(design));
		removeAll();
		if (namePane.getPreferredSize().width > LENGTH)
			fileNameLabel.setText(EditorUtil.getPartialName(
					getFile().getName()));
		add(UIUtilities.buildComponentPanel(namePane, false),
				"0, 0, l, c");
		add(new StatusLabel(status), "1, 0, l, c");
		
		/*
		add(busyLabel, "2, 0, l, c");
		add(resultLabel, "3, 0, l, c");
		add(UIUtilities.buildComponentPanel(cancelButton, false),
				"4, 0, l, c");
		add(UIUtilities.buildComponentPanel(actionMenuButton, false),
				"5, 0, l, c");
				*/
		addControlsToDisplay();
	}

	private void addControlsToDisplay()
	{
		add(refLabel, "2, 0, l, c");
		add(UIUtilities.buildComponentPanel(refButton, false), "3, 0, l, c");
	}
	
	/** 
	 * Attaches the listeners to the newly created component.
	 * 
	 * @param c The component to handle.
	 */
	private void attachListeners(FileImportComponent c)
	{
		PropertyChangeListener[] listeners = getPropertyChangeListeners();
		if (listeners != null && listeners.length > 0) {
			for (int j = 0; j < listeners.length; j++) {
				c.addPropertyChangeListener(listeners[j]);
			}
		}
	}
	
	/**
	 * Adds the specified files to the list of import data.
	 * 
	 * @param files The files to import.
	 */
	private void insertFiles(Map<File, Status> files)
	{
		resultIndex = ImportStatus.SUCCESS;
		if (files == null || files.size() == 0) return;
		components = Collections.synchronizedMap(new HashMap<File, FileImportComponent>());
		
		Entry<File, Status> entry;
		Iterator<Entry<File, Status>> i = files.entrySet().iterator();
		FileImportComponent c;
		File f;
		DatasetData d = dataset;
		Object node = refNode;
		if (importable.isFolderAsContainer()) {
			node = null;
			d = new DatasetData();
			d.setName(getFile().getName());
		}
		ImportableFile copy;
		while (i.hasNext()) {
			entry = i.next();
			f = entry.getKey();
			copy = importable.copy();
			copy.setFile(f);
			c = new FileImportComponent(copy, browsable, singleGroup,
					getIndex(), tags);
			if (f.isFile()) {
				c.setLocation(data, d, node);
				c.setParent(this);
			}
			c.setType(getType());
			attachListeners(c);
			c.setStatusLabel(entry.getValue());
			entry.getValue().addPropertyChangeListener(this);
			components.put((File) entry.getKey(), c);
		}
		
		removeAll();
		pane = EditorUtil.createTaskPane(getFile().getName());
		pane.setCollapsed(false);

		IconManager icons = IconManager.getInstance();
		pane.setIcon(icons.getIcon(IconManager.DIRECTORY));
		Font font = pane.getFont();
		pane.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
		layoutEntries(false);
		double[][] size = {{TableLayout.FILL}, {TableLayout.PREFERRED}};
		setLayout(new TableLayout(size));
		add(pane, new TableLayoutConstraints(0, 0));
		validate();
		repaint();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param importable The component hosting information about the file.
	 * @param browsable Flag indicating that the container can be browsed or not.
	 * @param singleGroup Passes <code>true</code> if the user is member of
	 * only one group, <code>false</code> otherwise.
	 * @param index The index of the parent.
	 * @param tags The tags that will be linked to the objects.
	 */
	public FileImportComponent(ImportableFile importable, boolean
			browsable, boolean singleGroup, int index,
			Collection<TagAnnotationData> tags)
	{
		if (importable == null)
			throw new IllegalArgumentException("No file specified.");
		if (importable.getGroup() == null)
			throw new IllegalArgumentException("No group specified.");
		this.index = index;
		this.tags = tags;
		this.importable = importable;
		this.singleGroup = singleGroup;
		this.browsable = browsable;
		resultIndex = ImportStatus.QUEUED;
		initComponents();
		buildGUI();
		setLocation(importable.getParent(), importable.getDataset(),
				importable.getRefNode());
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getFile()
     */
	@Override
    public FileObject getFile() { return importable.getFile(); }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getOriginalFile()
     */
    @Override
    public FileObject getOriginalFile() { return importable.getOriginalFile(); }
    
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#setLocation(omero.gateway.model.DataObject, omero.gateway.model.DatasetData, java.lang.Object)
     */
	@Override
    public void setLocation(DataObject data, DatasetData dataset, 
			Object refNode)
	{
		this.data = data;
		this.dataset = dataset;
		this.refNode = refNode;
		if (refNode != null && refNode instanceof TreeImageDisplay) {
			TreeImageDisplay n = (TreeImageDisplay) refNode;
			Object ho = n.getUserObject();
			if (ho instanceof DatasetData || ho instanceof ProjectData ||
				ho instanceof ScreenData) {
				containerObject = (DataObject) ho;
			}
			return;
		}
		if (dataset != null) {
			containerObject = dataset;
			return;
		}
		if (data != null && data instanceof ScreenData) {
			containerObject = data;
		}
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#setImportLogFile(java.util.Collection, long)
     */
	@Override
    public void setImportLogFile(Collection<FileAnnotationData> data, long id)
	{
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getDataset()
     */
	@Override
    public DatasetData getDataset() { return dataset; }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getDataObject()
     */
	@Override
    public DataObject getDataObject() { return data; }
	
	/**
	 * Replaces the initial status label.
	 * 
	 * @param label The value to replace.
	 */
	void setStatusLabel(Status label)
	{
		status = label;
		status.addPropertyChangeListener(this);
		buildGUI();
		revalidate();
		repaint();
	}

	/** 
	 * Sets the parent of the component.
	 * 
	 * @param parent The value to set.
	 */
	void setParent(FileImportComponentI parent)
	{
		this.parent = parent;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasParent()
     */
	@Override
    public boolean hasParent() { return parent != null; }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getStatus()
     */
	@Override
    public Status getStatus() { return status; }

	/**
	 * Returns the associated file if any.
	 *
	 * @param series See above.
	 * @return See above.
	 */
	private FileObject getAssociatedFile(int series)
	{
	    List<FileObject> l = getFile().getAssociatedFiles();
	    Iterator<FileObject> i = l.iterator();
	    FileObject f;
	    while (i.hasNext()) {
            f = i.next();
            if (f.getIndex() == series) {
                return f;
            }
        }
	    return null;
	}

	/**
	 * Returns <code>true</code> if the file has some associated files,
	 * <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	private boolean hasAssociatedFiles() {
	    List<FileObject> l = getFile().getAssociatedFiles();
	    return CollectionUtils.isNotEmpty(l);
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#setStatus(java.lang.Object)
     */
	@Override
    public void setStatus(Object image)
	{
		busyLabel.setVisible(false);
		busyLabel.setBusy(false);
		cancelButton.setVisible(false);
		this.image = image;
		if (image instanceof PlateData) {
			menu = null;
			imageLabel.setData((PlateData) image);
			fileNameLabel.addMouseListener(adapter);
			formatResultTooltip();
		}
		else if (image instanceof Collection){
		    Collection<?> c = (Collection)image;
		    if(!c.isEmpty()) {
		        Object obj = c.iterator().next();
		        if(obj instanceof ThumbnailData) {
		            List<ThumbnailData> list = new ArrayList<ThumbnailData>((Collection) image);
		            int m = list.size();
		            ThumbnailData data = list.get(0);
		            imageLabel.setData(data);
		            list.remove(0);
		            if (list.size() > 0) {
		                ThumbnailLabel label = imageLabels.get(0);
		                label.setVisible(true);
		                label.setData(list.get(0));
		                list.remove(0);
		                if (list.size() > 0) {
		                    label = imageLabels.get(1);
		                    label.setVisible(true);
		                    label.setData(list.get(0));
		                    list.remove(0);
		                    int n = status.getNumberOfImportedFiles()-m;
		                    if (n > 0) {
		                        label = imageLabels.get(2);
		                        label.setVisible(true);
		                        StringBuffer buf = new StringBuffer("... ");
		                        buf.append(n);
		                        buf.append(" more");
		                        label.setText(buf.toString());
		                    }
		                }
		            }
		        }
		        else if (obj instanceof PixelsData) {
		           //Result from the import itself
		            this.image = null;
		            Iterator i = c.iterator();
		            FileObject f;
		            while (i.hasNext()) {
		                Object object = i.next();
		                if (object instanceof PixelsData) {
		                    PixelsData pix = (PixelsData) object;
		                    if (hasAssociatedFiles()) {
		                        int series = pix.getImage().getSeries();
		                        f = getAssociatedFile(series);
		                        if (f != null) {
		                            f.setImageID(pix.getImage().getId());
		                        }
		                    } else {
		                        f = getOriginalFile();
		                        f.setImageID(pix.getImage().getId());
		                    }
		                }
		            }
		            formatResult();
		        }
		    }
		} else if (image instanceof ImportException) {
			if (getFile().isDirectory()) {
				this.image = null;
			} else formatResult();
		} else if (image instanceof Boolean) {
			busyLabel.setBusy(false);
			busyLabel.setVisible(false);
			cancelButton.setVisible(false);
			if (status.isMarkedAsCancel() ||
					status.isMarkedAsDuplicate()) {
				resultIndex = ImportStatus.IGNORED;
				this.image = null;
			}
		}
		revalidate();
		repaint();
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getImportErrors()
     */
	@Override
    public List<FileImportComponentI> getImportErrors()
	{
		List<FileImportComponentI> l = null;
		if (getFile().isFile()) {
			Object r = status.getImportResult();
			if (r instanceof Exception || image instanceof Exception) {
				l = new ArrayList<FileImportComponentI>();
				l.add(this);
				return l;
			}
		} else {
			if (components != null) {
			    Collection<FileImportComponent> values =  components.values();
                synchronized (components) {
                    Iterator<FileImportComponent> i = values.iterator();
    				FileImportComponentI fc;
    				l = new ArrayList<FileImportComponentI>();
    				List<FileImportComponentI> list;
    				while (i.hasNext()) {
    					fc = i.next();
    					list = fc.getImportErrors();
    					if (!CollectionUtils.isEmpty(list))
    						l.addAll(list);
    				}
                }
			}
		}
		return l;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getGroupID()
     */
	@Override
    public long getGroupID() { return importable.getGroup().getId(); }

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getExperimenterID()
     */
    @Override
    public long getExperimenterID() { return importable.getUser().getId(); }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getImportErrorObject()
     */
	@Override
    public ImportErrorObject getImportErrorObject()
	{
		Object r = status.getImportResult();
		Exception e = null;
		if (r instanceof Exception) e = (Exception) r;
		else if (image instanceof Exception) e = (Exception) image;
		if (e == null) return null;
		ImportErrorObject object = new ImportErrorObject(
		        getFile().getTrueFile(), e, getGroupID());
		object.setImportContainer(status.getImportContainer());
		long id = status.getLogFileID();
		if (id <= 0) {
			FilesetData data = status.getFileset();
			if (data != null) {
				id = data.getId();
				object.setRetrieveFromAnnotation(true);
			}
		}
		object.setLogFileID(id);
		return object;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasImportFailed()
     */
	@Override
    public boolean hasImportFailed()
	{
		return resultIndex == ImportStatus.FAILURE ||
				resultIndex == ImportStatus.UPLOAD_FAILURE;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasUploadFailed()
     */
	@Override
    public boolean hasUploadFailed()
	{
		return resultIndex == ImportStatus.UPLOAD_FAILURE ||
				(resultIndex == ImportStatus.FAILURE &&
				!status.didUploadStart());
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#isCancelled()
     */
	@Override
    public boolean isCancelled()
	{
		boolean b = status.isMarkedAsCancel();
		if (b || getFile().isFile()) return b;
		if (components == null) return false;
		Collection<FileImportComponent> values =  components.values();
        synchronized (components) {
            Iterator<FileImportComponent> i = values.iterator();
    		while (i.hasNext()) {
    			if (i.next().isCancelled())
    				return true;
    		}
        }
		return false;
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasImportToCancel()
     */
    @Override
    public boolean hasImportToCancel()
    {
        boolean b = status.isMarkedAsCancel();
        if (b) return false;
        if (getFile().isFile() && !hasImportStarted()) return true;
        if (components == null) return false;
        Collection<FileImportComponent> values =  components.values();
        synchronized (components) {
            Iterator<FileImportComponent> i = values.iterator();
            FileImportComponentI fc;
            while (i.hasNext()) {
                fc = i.next();
                if (!fc.isCancelled() && !fc.hasImportStarted())
                    return true;
            }
        }
        return false;
    }
    
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasFailuresToReimport()
     */
	@Override
    public boolean hasFailuresToReimport()
	{
		if (getFile().isFile()) return hasUploadFailed() && !reimported;
		if (components == null) return false;
		Collection<FileImportComponent> values =  components.values();
        synchronized (components) {
            Iterator<FileImportComponent> i = values.iterator();
    		while (i.hasNext()) {
    			if (i.next().hasUploadFailed())
    				return true;
    		}
        }
		return false;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasFailuresToReupload()
     */
	@Override
    public boolean hasFailuresToReupload()
	{
		if (getFile().isFile()) return hasUploadFailed() && !reimported;
		if (components == null) return false;
		Collection<FileImportComponent> values =  components.values();
        synchronized (components) {
            Iterator<FileImportComponent> i = values.iterator();
    		while (i.hasNext()) {
    			if (i.next().hasFailuresToReupload())
    				return true;
    		}
        }
		return false;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasImportStarted()
     */
	@Override
    public boolean hasImportStarted()
	{
		if (getFile().isFile()) return resultIndex != ImportStatus.QUEUED;
		if (components == null) return false;
		Collection<FileImportComponent> values =  components.values();
		int count = 0;
        synchronized (components) {
            Iterator<FileImportComponent> i = values.iterator();
    		while (i.hasNext()) {
    			if (i.next().hasImportStarted()) count++;
    		}
        }
		return count == components.size();
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasFailuresToSend()
     */
	@Override
    public boolean hasFailuresToSend()
	{
		if (getFile().isFile()) return resultIndex == ImportStatus.FAILURE;
		if (components == null) return false;
		Collection<FileImportComponent> values =  components.values();
        synchronized (components) {
            Iterator<FileImportComponent> i = values.iterator();
    		while (i.hasNext()) {
    			if (i.next().hasFailuresToSend())
    				return true;
    		}
        }
		return false;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasComponents()
     */
	@Override
    public boolean hasComponents()
	{
		return components != null && components.size() > 0;
	}
	
    public void layoutEntries(boolean failure)
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		if (!hasComponents()) return;
		Entry<File, FileImportComponent> e;
		Set<Entry<File, FileImportComponent>> entries = components.entrySet();
		synchronized (components) {
    		Iterator<Entry<File, FileImportComponent>> i =
    		        entries.iterator();
    		int index = 0;
    		FileImportComponent fc;
    		if (failure) {
    			while (i.hasNext()) {
    				e = i.next();
    				fc = e.getValue();
    				if (fc.hasImportFailed()) {
    					if (index%2 == 0)
    						fc.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
    					else 
    						fc.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
    					p.add(fc);
    					index++;
    				}
    			}
    		} else {
    			while (i.hasNext()) {
    				e = i.next();
    				fc = e.getValue();
    				if (index%2 == 0)
    					fc.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
    				else 
    					fc.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
    				p.add(fc);
    				index++;
    			}
    		}
		}
		
		pane.removeAll();
		pane.add(p);
		pane.revalidate();
		pane.repaint();
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getImportStatus()
     */
	@Override
    public ImportStatus getImportStatus()
	{
		if (getFile().isFile()) {
			if (hasImportFailed()) return ImportStatus.FAILURE;
			return resultIndex;
		}
		if (components == null || components.size() == 0) {
			if (image instanceof Boolean) {
				if (getFile().isDirectory()) {
					return ImportStatus.SUCCESS;
				} else {
					if (!status.isMarkedAsCancel() &&
						!status.isMarkedAsDuplicate())
						return ImportStatus.FAILURE;
				}
			}
			return resultIndex;
		}
			
		Collection<FileImportComponent> values =  components.values();
		int n = components.size();
		int count = 0;
        synchronized (components) {
            Iterator<FileImportComponent> i = values.iterator();
    		while (i.hasNext()) {
    			if (i.next().hasImportFailed())
    				count++;
    		}
        }
		if (count == n) return ImportStatus.FAILURE;
		if (count > 0) return ImportStatus.PARTIAL;
		return ImportStatus.SUCCESS;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasToRefreshTree()
     */
	@Override
    public boolean hasToRefreshTree()
	{
		if (getFile().isFile()) {
			if (hasImportFailed()) return false;
			switch (type) {
				case PROJECT_TYPE:
				case NO_CONTAINER:
					return true;
				default:
					return false;
			}
		}
		if (components == null) return false;
		if (importable.isFolderAsContainer() && type != PROJECT_TYPE) {
		    Collection<FileImportComponent> values =  components.values();
            synchronized (components) {
                Iterator<FileImportComponent> i = values.iterator();
    			while (i.hasNext()) {
    				if (i.next().toRefresh()) 
    					return true;
    			}
            }
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#toRefresh()
     */
	@Override
    public boolean toRefresh()
	{
		/*
		if (file.isFile()) {
			if (deleteButton.isVisible()) return false;
			else if (errorBox.isVisible())
				return !(errorBox.isEnabled() && errorBox.isSelected());
			return true;
		}
		if (components == null) return false;
		Iterator<FileImportComponent> i = components.values().iterator();
		int count = 0;
		while (i.hasNext()) {
			if (i.next().hasFailuresToSend()) 
				count++;
		}
		return components.size() != count;
		*/
		return true;
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#cancelLoading()
     */
	@Override
    public void cancelLoading()
	{
		if (components == null || components.isEmpty()) {
			cancel(getFile().isFile());
			return;
		}
		Collection<FileImportComponent> values =  components.values();
		synchronized (components) {
		    Iterator<FileImportComponent> i = values.iterator();
	        while (i.hasNext()) {
	            i.next().cancelLoading();
	        }
        }
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#setType(int)
     */
	@Override
    public void setType(int type) { this.type = type; }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getType()
     */
	@Override
    public int getType() { return type; }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#isFolderAsContainer()
     */
	@Override
    public boolean isFolderAsContainer()
	{
		return importable.isFolderAsContainer();
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getContainerFromFolder()
     */
	@Override
    public DataObject getContainerFromFolder() { return containerFromFolder; }

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getFilesToReupload()
     */
	@Override
    public List<FileImportComponentI> getFilesToReupload()
	{
		List<FileImportComponentI> l = null;
		if (getFile().isFile()) {
			if (hasFailuresToReupload() && !reimported) {
			    ArrayList<FileImportComponentI> ret = new ArrayList<FileImportComponentI>();
                ret.add(this);
                return ret;
			}
		} else {
			if (components != null) {
			    Collection<FileImportComponent> values =  components.values();
		        synchronized (components) {
		            Iterator<FileImportComponent> i = values.iterator();
    				FileImportComponentI fc;
    				l = new ArrayList<FileImportComponentI>();
    				List<FileImportComponentI> list;
    				while (i.hasNext()) {
    					fc = i.next();
    					list = fc.getFilesToReupload();
    					if (!CollectionUtils.isEmpty(list))
    						l.addAll(list);
    				}
		        }
			}
		}
		return l;
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#setReimported(boolean)
     */
	@Override
    public void setReimported(boolean reimported)
	{ 
		this.reimported = reimported;
		repaint();
	}
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#uploadComplete(java.lang.Object)
     */
	@Override
    public void uploadComplete(Object result)
	{
		if (result instanceof CmdCallback)
			callback = (CmdCallback) result;
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getIndex()
     */
	@Override
    public int getIndex() { return index; }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getImportResult()
     */
	@Override
    public Object getImportResult() { return status.getImportResult(); }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#isHCS()
     */
	@Override
    public boolean isHCS() { return status.isHCS(); }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getImportSize()
     */
	@Override
    public long getImportSize() { return status.getSizeUpload(); }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#hasResult()
     */
	@Override
    public boolean hasResult() { return image != null; }
	
	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#getImportableFile()
     */
	@Override
    public ImportableFile getImportableFile() { return importable; }

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#onResultsSaving(java.lang.String, boolean)
     */
	@Override
    public void onResultsSaving(String message, boolean busy)
	{
	    busyLabel.setVisible(busy);
	    busyLabel.setBusy(busy);
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#setBackground(java.awt.Color)
     */
	@Override
    public void setBackground(Color color)
	{
		if (busyLabel != null) busyLabel.setBackground(color);
		if (namePane != null) {
			namePane.setBackground(color);
			for (int i = 0; i < namePane.getComponentCount(); i++) 
				namePane.getComponent(i).setBackground(color);
		}
		super.setBackground(color);
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#propertyChange(java.beans.PropertyChangeEvent)
     */
	@Override
    public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Status.FILES_SET_PROPERTY.equals(name)) {
			if (isCancelled()) {
				busyLabel.setBusy(false);
				busyLabel.setVisible(false);
				return;
			}
			Map<File, Status> files = (Map<File, Status>)
				evt.getNewValue();
			int n = files.size();
			insertFiles(files);
			firePropertyChange(IMPORT_FILES_NUMBER_PROPERTY, null,n);
		} else if (Status.FILE_IMPORT_STARTED_PROPERTY.equals(name)) {
			resultIndex = ImportStatus.STARTED;
			Status sl = (Status) evt.getNewValue();
			if (sl.equals(status) && busyLabel != null) {
				cancelButton.setEnabled(sl.isCancellable());
				firePropertyChange(Status.FILE_IMPORT_STARTED_PROPERTY,
				        null, this);
			}
		} else if (Status.UPLOAD_DONE_PROPERTY.equals(name)) {
			Status sl = (Status) evt.getNewValue();
			if (sl.equals(status) && hasParent()) {
				if (sl.isMarkedAsCancel()) cancel(true);
				else {
					formatResult();
					firePropertyChange(Status.UPLOAD_DONE_PROPERTY, null,
							this);
				}
			}
		} else if (Status.CANCELLABLE_IMPORT_PROPERTY.equals(name)) {
			Status sl = (Status) evt.getNewValue();
			if (sl.equals(status))
				cancelButton.setVisible(sl.isCancellable());
		} else if (Status.SCANNING_PROPERTY.equals(name)) {
			Status sl = (Status) evt.getNewValue();
			if (sl.equals(status)) {
				if (busyLabel != null && !isCancelled()) {
					busyLabel.setBusy(true);
					busyLabel.setVisible(true);
				}
			}
		} else if (Status.FILE_RESET_PROPERTY.equals(name)) {
			importable.setFile((File) evt.getNewValue());
			fileNameLabel.setText(getFile().getName());
		} else if (ThumbnailLabel.BROWSE_PLATE_PROPERTY.equals(name)) {
			firePropertyChange(BROWSE_PROPERTY, evt.getOldValue(), 
					evt.getNewValue());
		} else if (Status.CONTAINER_FROM_FOLDER_PROPERTY.equals(name)) {
			containerFromFolder = (DataObject) evt.getNewValue();
			if (containerFromFolder instanceof DatasetData) {
				containerObject = containerFromFolder;
			} else if (containerFromFolder instanceof ScreenData) {
				containerObject = containerFromFolder;
			}
		} else if (Status.DEBUG_TEXT_PROPERTY.equals(name)) {
			firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
		} else if (ThumbnailLabel.VIEW_IMAGE_PROPERTY.equals(name)) {
			//use the group
			SecurityContext ctx = new SecurityContext(
					importable.getGroup().getId());
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			Long id = (Long) evt.getNewValue();
			bus.post(new ViewImage(ctx, new ViewImageObject(id), null));
		} else if (Status.IMPORT_DONE_PROPERTY.equals(name) ||
				Status.PROCESSING_ERROR_PROPERTY.equals(name)) {
			Status sl = (Status) evt.getNewValue();
			if (sl.equals(status))
				firePropertyChange(Status.IMPORT_DONE_PROPERTY, null,
						this);
		}
	}

	/* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI#toString()
     */
	@Override
    public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(getFile().getAbsolutePath());
		if (importable.getGroup() != null)
			buf.append("_"+importable.getGroup().getId());
		if (importable.getUser() != null)
			buf.append("_"+importable.getUser().getId());
		return buf.toString();
	}

}
