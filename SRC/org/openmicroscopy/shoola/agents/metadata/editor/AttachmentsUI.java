/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AttachmentsUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.PDFFilter;
import org.openmicroscopy.shoola.util.filter.file.TEXTFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import pojos.AnnotationData;
import pojos.FileAnnotationData;

/** 
 * The UI component displaying the documents linked to the data object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class AttachmentsUI
	extends AnnotationUI 
	implements ActionListener, PropertyChangeListener
{

	/** The title associated to this component. */
	private static final String TITLE = "Related documents ";
	
	/** Action id indicating to add new file. */
	private static final String	ADD_ACTION = "add";
	
	/** Button to add a new file. */
	private JButton							addButton;
	
	/** Collection of annotation files to remove. */
	private List<AnnotationData> 			removedFiles;
	
	/** Collection of supported file formats. */
	private List<FileFilter>				filters;
	
	/** Collection of files to attach to the object. */
	private List<File>						addedFiles;
	
	/** Map used to handle the files to add. */
	
	private Map<Integer, File>				rows;
	
	/** Map hosting the label displaying the files that can be downloaded. */
	private Map<JLabel, FileAnnotationData> toDownload;
	
	/** The label corresponding to the selected file. */
	private JLabel							selectedFile;
	
	/** The pop up menu. */
	private AttachmentPopupMenu				menu;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		toDownload = new HashMap<JLabel, FileAnnotationData>();
		rows = new HashMap<Integer, File>();
		addedFiles = new ArrayList<File>();
		filters = new ArrayList<FileFilter>();
		filters.add(new PDFFilter());
		filters.add(new TEXTFilter());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addButton = new JButton("Attach...");
		addButton.setToolTipText("Attach a document.");
		addButton.addActionListener(this);
		addButton.setActionCommand(ADD_ACTION);
		removedFiles = new ArrayList<AnnotationData>();
	}
	
	/** Launches a file chooser to select the file to attach. */
	private void browseFile()
	{
		JFrame owner = 
			MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(owner, FileChooser.SAVE, 
								"Browse File", "Attach a file of the " +
										"selected element", filters);
		chooser.addPropertyChangeListener(
				FileChooser.APPROVE_SELECTION_PROPERTY, this);
		UIUtilities.centerAndShow(chooser);
	}
	
	/**
	 * Brings up the widget asking the user what to do with the file.
	 * 
	 * @param source The source of the click.
	 */
	private void viewFile(JLabel source)
	{
		FileAnnotationData data = toDownload.get(source);
		if (data == null) return;
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		un.notifyDownload(data);
	}
	
	/**
	 * Builds an element composing the display of the uploaded files
	 * 
	 * @param f	The annotation to display.
	 * @return See above.
	 */
	private JPanel buildExistingFileRow(FileAnnotationData f)
	{
		IconManager icons = IconManager.getInstance();
		Icon icon = null;
		
		String format = f.getFileFormat();
		if (FileAnnotationData.PDF.equals(format))
			icon = icons.getIcon(IconManager.PDF_DOC);
		else if (FileAnnotationData.TEXT.equals(format))
			icon = icons.getIcon(IconManager.TEXT_DOC);
		else if (FileAnnotationData.XML.equals(format) ||
				FileAnnotationData.HTML.equals(format) ||
				FileAnnotationData.HTM.equals(format))
			icon = icons.getIcon(IconManager.XML_DOC);
		
		JPanel p = new JPanel();
		String name = f.getFileName();
		
		int width = p.getFontMetrics(p.getFont()).stringWidth(name);
		if (width < icon.getIconWidth())
			width = icon.getIconWidth();
		double[][] tl = {{width}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED} }; //rows
		p.setLayout(new TableLayout(tl));
		
		JLabel label = new JLabel(icon);
		toDownload.put(label, f);
		label.addMouseListener(new MouseAdapter() {
		
			/** 
			 * Brings up a dialog box indicating to download 
			 * the file associated to the component.
			 */
			public void mouseReleased(MouseEvent e) {
				Object src = e.getSource();
				if (e.getClickCount() == 2)
					viewFile((JLabel) src);
				else if (e.isPopupTrigger())
					showMenu(e.getPoint(), e.getComponent());
			}
			
			/** 
			 * Sets the selected label and shows the menu.
			 */
			public void mousePressed(MouseEvent e)
			{
				Object src = e.getSource();
				selectedFile = (JLabel) src;
				if (e.isPopupTrigger())
					showMenu(e.getPoint(), e.getComponent());
			}
		
			/**
			 * Modifies the cursor when entered.
			 */
			public void mouseEntered(MouseEvent e) {
				Object src = e.getSource();
				if (src instanceof JLabel) {
					((JLabel) src).setCursor(
							Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				
			}
		
		});
		//Add listener
		p.add(label, "0, 0");
		p.add(new JLabel(name), "0, 1");
		return p;
	}
	
	/**
	 * Shows the tag menu.
	 * 
	 * @param location	The location of the mouse click.
	 * @param invoker	The last selected component.
	 */
	void showMenu(Point location, Component invoker)
	{
		if (menu == null)
			menu = new AttachmentPopupMenu(this);
		menu.show(invoker, location.x, location.y);
	}
	
	/**
	 * Lays out the components used to add new <code>file</code>s.
	 * 
	 * @return See above.
	 */
	private JPanel layoutAttachments()
	{
		JPanel content = new JPanel();
		Collection c = model.getAttachments();
		if (c == null) return content;
		List<FileAnnotationData> toLayout = new ArrayList<FileAnnotationData>();
		Iterator i = c.iterator();
		FileAnnotationData f;
		while (i.hasNext()) {
			f = (FileAnnotationData) i.next();
			if (!removedFiles.contains(f))
				toLayout.add(f);
		}
		
		double[] columns = {TableLayout.FILL, 10, TableLayout.FILL, 
							10, TableLayout.FILL}; //rows
		TableLayout layout = new TableLayout();
		layout.setColumn(columns);
		content.setLayout(layout);
		for (int j = 0; j < 2*toLayout.size()-1; j++) {
			if (j%3 == 0) layout.insertRow(j, TableLayout.PREFERRED);
			else layout.insertRow(j, 5);
		}
		int index = 0;
		i = toLayout.iterator();
		
		int row = 0;
		while (i.hasNext()) {
			f = (FileAnnotationData) i.next();
			content.add(buildExistingFileRow(f), index+", "+row+", f, c");
			index = index+2;
			if (index%5 == 0) {
				index = 0;
				if (row != 0) row++;
			}
		}
		return UIUtilities.buildComponentPanel(content);
	}
	
	/**
	 * Builds a UI component displaying the file to add.
	 * 
	 * @param f		The file to add.
	 * @param index	The index associated to the file.
	 * @return See above.
	 */
	private JPanel buildAddedFileRow(File f, int index)
	{
		JPanel row = new JPanel();
		IconManager icons = IconManager.getInstance();
		row.add(new JLabel(icons.getIcon(IconManager.ATTACHMENT)));
		JTextArea area = new JTextArea(f.getAbsolutePath());
		UIUtilities.setTextAreaDefault(area);
		row.add(area);
		JButton remove = new JButton(icons.getIcon(IconManager.REMOVE));
		UIUtilities.unifiedButtonLookAndFeel(remove);
		remove.setToolTipText("Remove.");
		remove.addActionListener(this);
		remove.setActionCommand(""+index);
		row.add(remove);
		return UIUtilities.buildComponentPanel(row);
	}
	
	/**
	 * Lays out the files to add.
	 * 
	 * @return See above.
	 */
	private JPanel layoutAddedFiles()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		if (addedFiles.size() == 0) return content;
		rows.clear();
		Iterator i = addedFiles.iterator();
		int index = 0;
		File f;
		while (i.hasNext()) {
			f = (File) i.next();
			rows.put(index, f);
			content.add(buildAddedFileRow(f, index));
			index++;
		}
		return content;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	AttachmentsUI(EditorModel model)
	{
		super(model);
		title = TITLE;
		initComponents();
	}
	
	/** Adds the selected file to the collection of items to remove. */
	void removeSelectedAttachment() 
	{
		if (selectedFile == null) return;
		FileAnnotationData data = toDownload.get(selectedFile);
		if (data == null) return;
		toDownload.remove(selectedFile);
		selectedFile = null;
		removedFiles.add(data);
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
		buildUI();
		revalidate();
		repaint();
	}

	/** Brings up the widget to download the archived files. */
	void downloadSelectedAttachment()
	{
		if (selectedFile == null) return;
		viewFile(selectedFile);
		selectedFile = null;
	}
	
	/**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		int n = model.getAttachmentsCount()-removedFiles.size();
		title = TITLE+LEFT+n+RIGHT;
		Border border = new TitledLineBorder(title, getBackground());
		setBorder(border);
		getCollapseComponent().setBorder(border);
		if (n > 0) 
			add(new JScrollPane(layoutAttachments()));
		
		add(layoutAddedFiles());
		add(UIUtilities.buildComponentPanel(addButton));
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }
	
	/**
	 * Returns the collection of attachments to remove.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove()
	{
		return removedFiles;
	}

	/**
	 * Returns the collection of attachments to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{
		List<AnnotationData> toAdd = new ArrayList<AnnotationData>();
		Iterator i = addedFiles.iterator();
		File f;
		while (i.hasNext()) {
			f = (File) i.next();
			toAdd.add(new FileAnnotationData(f));
		}
		return toAdd;
	}

	/**
	 * Returns <code>true</code> if annotation to save,
	 * <code>false</code> otherwise.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		List l = getAnnotationToSave();
		if (l != null && l.size() > 0) return true; 
		l = getAnnotationToRemove();
		if (l != null && l.size() > 0) return true; 
		return false;
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{
		
	}
	
	/**
	 * Adds the selected annotation to the collection of elements to remove.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		if (ADD_ACTION.equals(s)) {
			browseFile();
		} else {
			int index = Integer.parseInt(e.getActionCommand());
			File f = rows.get(index);
			if (f != null) {
				addedFiles.remove(f);
				buildUI();
				revalidate();
				repaint();
			}
		}
	}

	/**
	 * Adds the new file to the display.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File f = (File) evt.getNewValue();
			Iterator i = addedFiles.iterator();
			boolean exist = false;
			File file;
			while (i.hasNext()) {
				file = (File) i.next();
				if (file.getAbsolutePath().equals(f.getAbsolutePath())) {
					exist = true;
					break;
				}
			}
			if (exist) return;
			addedFiles.add(f);
			firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
								Boolean.TRUE);
			buildUI();
			revalidate();
			repaint();
		}
	}
	
}
