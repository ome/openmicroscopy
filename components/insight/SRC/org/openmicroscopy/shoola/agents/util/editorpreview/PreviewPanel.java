/*
 * org.openmicroscopy.shoola.agents.editor.browser.FieldPanelsComponent
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
package org.openmicroscopy.shoola.agents.util.editorpreview;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays an entire Editor file.
 * This is simply the display panel, and does not include E.g the JXTaskPane 
 * Other classes may display this panel in a JXTaskPane, and use the
 * {@link #getTitle()} method to set the JXTaskPane title. 
 * 
 * Steps are displayed as panels with labeled border, with their
 * parameters displayed as name-value pairs. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class PreviewPanel 
	extends JPanel
	implements PropertyChangeListener
{
	
	/** Bound property indicating to open the file. */
	public static final String		OPEN_FILE_PROPERTY = "openFile";
	
	/** Bound property indicating modifications of fields. */
	public static final String		PREVIEW_EDITED_PROPERTY = "previewEdited";
	
	/** max number of characters to display in field value */
	public static final int			MAX_CHARS = 50;

	/** The element that holds the value of a parameter */
	public static final String 		VALUE = "v";

	/** The element that defines a parameter within a step */
	public static final String 		PARAMETER = "p";

	/** The attribute holding the 'level' of a single step in the hierarchy */
	public static final String 		LEVEL = "l";

	/** The element defining a single step */
	public static final String 		STEP = "s";

	/** The element holding the list of steps for the protocol */
	public static final String 		STEPS = "ss";

	/** The element holding the description/abstract of the protocol */
	public static final String 		DESCRIPTION = "d";

	/** The element/attribute holding the name of a parameter or protocol */
	public static final String 		NAME = "n";

	/** Default text if no data entered. */
	private static final String		DEFAULT_TEXT = "None";
	
	/**
	 * The Model passes the XML description. Then the steps and the name and
	 * description/abstract of the protocol can be retrieved. 
	 */
	private PreviewModel		model;
	
	/** The id of the file. */
	private long				fileID;

	/** Collection of fields to update. */
	private List<DataComponent> fields;
	
	/**
	 * Lays out the title and the a button to open the file.
	 * 
	 * @return See above.
	 */
	private JPanel layoutTiTle()
	{
		IconManager icons = IconManager.getInstance();
		JButton open = new JButton(icons.getIcon(IconManager.FILE_EDITOR));
		open.setOpaque(false);
		UIUtilities.unifiedButtonLookAndFeel(open);
		open.setBackground(UIUtilities.BACKGROUND_COLOR);
		open.setToolTipText("Open the file.");
		open.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				if (fileID >= 0)
					firePropertyChange(OPEN_FILE_PROPERTY, -1, fileID);
			}
		});
		JToolBar bar = new JToolBar();
    	bar.setBorder(null);
    	bar.setFloatable(false);
    	bar.setBackground(UIUtilities.BACKGROUND_COLOR);
    	bar.add(open);
    	
    	JPanel p = new JPanel();
    	p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    	if (fileID > 0) p.add(bar);
    	p.add(UIUtilities.setTextFont(getTitle()));
    	JPanel content = UIUtilities.buildComponentPanel(p, 0, 0);
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	return content;
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		String description = "";
		List<String> values = getFormattedDesciption();
		if (values.size() == 0)
			description = "Description and Summary Not available";
		else {
			//TODO: externalize that
			Iterator<String> i = values.iterator();
			StringBuffer buffer = new StringBuffer();
			while (i.hasNext()) {
				buffer.append(i.next());
				buffer.append("<br>");
			}
			description = "<html>" +
					"<span style='font-family:sans-serif;font-size:11pt'>"
							+ buffer.toString() + "</span></html>";
			// display description in EditorPane, because text wraps nicely!
			JEditorPane ep = new JEditorPane("text/html", description);
			ep.setEditable(false);
			ep.setBorder(new EmptyBorder(3, 5, 5, 3));
			p.add(ep);
		}
		
		List<StepObject> protocolSteps = model.getSteps();
		
		String stepName;
		JPanel nodePanel;
		Border border;
		int indent;
		fields = new ArrayList<DataComponent>();
		List<DataComponent> paramComponents;
		for (StepObject stepObject : protocolSteps) {

			// each child node has a list of parameters 
			paramComponents = transformFieldParams(stepObject);
			
			// don't add a field unless it has some parameters to display
			if (paramComponents.isEmpty()) continue;
			
			fields.addAll(paramComponents);
			stepName = stepObject.getName();
			
			indent = (stepObject.getLevel())*10;
			nodePanel = new JPanel();
			border = new EmptyBorder(0, indent, 0, 0);
			border = BorderFactory.createCompoundBorder(border, 
								BorderFactory.createTitledBorder(stepName));
			nodePanel.setBorder(border);
			nodePanel.setBackground(UIUtilities.BACKGROUND_COLOR);
			nodePanel.setLayout(new GridBagLayout());
			
			layoutFields(nodePanel, null, paramComponents);
			
			p.add(nodePanel);
		}
		setLayout(new BorderLayout());
		add(layoutTiTle(), BorderLayout.NORTH);
		add(p, BorderLayout.CENTER);
	}
	
	/** Invokes when the model changes. */
	private void rebuildUI() 
	{	
		removeAll();
		initComponents();
		revalidate();
		repaint();
	}

	/** 
	 * Lays out the passed component.
	 * 
	 * @param pane 		The main component.
	 * @param button	The button to show or hide the unset fields.
	 * @param fields	The fields to lay out.
	 */
	private void layoutFields(JPanel pane, JButton button, 
			List<DataComponent> fields)
	{
		pane.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		//c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
	    
		for (DataComponent comp : fields) {
	        c.gridx = 0;
	        if (comp.isSetField()) {
	        	 ++c.gridy;
	        	 c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
	             c.fill = GridBagConstraints.NONE;      //reset to default
	             c.weightx = 0.0;  
	             pane.add(comp.getLabel(), c);
	             c.gridx++;
	             pane.add(Box.createHorizontalStrut(5), c); 
	             c.gridx++;
	             //c.gridwidth = GridBagConstraints.REMAINDER;     //end row
	             //c.fill = GridBagConstraints.HORIZONTAL;
	             c.weightx = 1.0;
	             pane.add(comp.getArea(), c);  
	        } 
	    }
	    ++c.gridy;
	    c.gridx = 0;
	    c.weightx = 0.0;  
	    if (button != null) pane.add(button, c);
	}

	/**
	 * Transforms the Step into the corresponding UI objects.
	 * 
	 * @param step The step to transform.
	 * @return See above.
	 */
	private List<DataComponent> transformFieldParams(StepObject step)
	{
		List<DataComponent> cmps = new ArrayList<DataComponent>();
		DataComponent comp;
		JLabel label;
		JComponent area;
		String key;
		String value;
		label = new JLabel();
		Font font = label.getFont();
		int sizeLabel = font.getSize()-2;
		
		List<StepObject.Param> params = step.getParams();
		for (StepObject.Param param : params) {
			key = param.getName();
			value = param.getValue();
			if ((value != null) && (value.length() > MAX_CHARS)) {
				value = value.substring(0, MAX_CHARS-1) + "...";
			}
			
			area = UIUtilities.createComponent(OMETextArea.class, 
					null);
			if (value == null || value.equals(""))
				value = DEFAULT_TEXT;
			((OMETextArea) area).setText(value);
			((OMETextArea) area).setEditedColor(
					UIUtilities.EDITED_COLOR);
			
			label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
			label.setBackground(UIUtilities.BACKGROUND_COLOR);
			
			comp = new DataComponent(label, area);
			comp.attachListener(this);
			comp.setSetField(value != null);
			comp.setEnabled(false);
			cmps.add(comp);
		}
		
		return cmps;
	}
	
	/** Invokes when a field is modified. */
	private void onFieldModified()
	{
		boolean dirty = hasDataToSave();
		firePropertyChange(PREVIEW_EDITED_PROPERTY, Boolean.valueOf(!dirty), 
				Boolean.valueOf(dirty));
	}
	
	/**
	 * Creates a new instance and sets the content with the XML summary. 
	 * 
	 * @param xmlDescription A preview summary in XML. 
	 * @param fileID The id of the file.
	 */
	public PreviewPanel(String xmlDescription, long fileID)
	{
		this.fileID = fileID;
		setDescriptionXml(xmlDescription);
	}
	
	/**
	 * Creates an instance without setting the content. 
	 * Use {@link #setDescriptionXml(String)} to set the content with XML
	 */
	public PreviewPanel() { fileID = -1; }
	
	/**
	 * Sets the XML description (XML that summarizes an OMERO.editor file),
	 * as retrieved from the description of a File-Annotation for an Editor file. 
	 * 
	 * @param xmlDescription
	 */
	public void setDescriptionXml(String xmlDescription) 
	{
		model = new PreviewModel(xmlDescription);
		rebuildUI();
	}
	
	/**
	 * Allows classes that want to display this panel to get access to the 
	 * protocol title, which is not displayed in this panel. 
	 * 
	 * @return See above
	 */
	public String getTitle()
	{
		if (model == null) return "OMERO.editor";
		return model.getTitle();
	}

	/**
	 * Allows classes that want to display this panel to get access to the 
	 * protocol description, which is not displayed in this panel. 
	 * 
	 * @return See above.
	 */
	public String getDescription()
	{
		if (model == null) return "";
		return model.getDescription();
	}
	
	/**
	 * Returns the formatted description. Each element of the array is a
	 * string with maximum <code>10</code> words. 
	 * 
	 * @return See above.
	 */
	public List<String> getFormattedDesciption()
	{
		List<String> list = new ArrayList<String>();
		String description = getDescription();
		if (description == null || description.length() == 0) return list;
		String[] values = description.split(" ");
		StringBuffer buffer = null;
		int n = 0;
		for (int i = 0; i < values.length; i++) {
			if (n == 0) buffer = new StringBuffer();
			buffer.append(values[i]+" ");
			n++;
			if (n == 10) {
				n = 0;
				list.add(buffer.toString());
			}
		}
		return list;
	}
	
	/**
	 * Returns <code>true</code> if the preview has been modified,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasDataToSave()
	{
		boolean dirty = false;
		Iterator<DataComponent> i = fields.iterator();
		DataComponent comp;
		while (i.hasNext()) {
			comp = i.next();
			if (comp.isDirty()) {
				dirty = true;
				break;
			}
		}
		return dirty;
	}
	
	/**
	 * Updates the description and returns it.
	 * 
	 * @return See above.
	 */
	public String prepareDataToSave()
	{
		return null;
	}
	
	/** 
	 * Listens to property fired by the {@link DataComponent}s.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (DataComponent.DATA_MODIFIED_PROPERTY.equals(name)) {
			onFieldModified();
		}
	}
	
}
