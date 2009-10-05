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
package org.openmicroscopy.shoola.agents.editor.browser;


//Java imports
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.model.CPEsummaryExport;
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.TreeIterator;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.util.DataComponent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMETextArea;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays an entire Editor file, using code based on code from
 * {@link org.openmicroscopy.shoola.agents.metadata.editor.ImageAcquisitionComponent}
 * 
 * Children of the root node are displayed as the first level of panels
 * with labelled border, with their parameters stored in a map of 
 * AcquisitionComponents 
 * TODO Subsequent children should be displayed in additional hierarchy of 
 * panels with labelled border??
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MetadataPanelsComponent 
	extends JPanel
	implements TreeModelListener
{
	/** max number of characters to display in field value */
	public static final int 					MAX_CHARS = 25;
	
	/** Reference to the Model. */
	private TreeModel							model;
	
	/** Reference to the parent of this component. */
	private MetadataUI							parent;
	
	private String 								protTitle;	
	
	
	/** Initiliases the components. */
	private void initComponents()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// root's children
		DefaultMutableTreeNode root =  (DefaultMutableTreeNode)model.getRoot();
		
		
		IField field;
		Object userOb;
		
		userOb = root.getUserObject();
		field = (IField)userOb;
		
		protTitle = field.getAttribute(Field.FIELD_NAME);
		if (field.getContentCount() > 0) {
			String description = field.getContentAt(0).toString();
			if (description.length() > CPEsummaryExport.MAX_DESC) {
				description = description.
							substring(0, CPEsummaryExport.MAX_DESC-1) + "...";
			}
			description = "<html><span style='font-family:sans-serif;font-size:11pt'>"
				+ description + "</span></html>";
			// display description in EditorPane, because text wraps nicely!
			//MultilineLabel label = new MultilineLabel();
			//label.setText(model.getDescription());
			JEditorPane ep = new JEditorPane("text/html", description);
			ep.setEditable(false);
			ep.setBorder(new EmptyBorder(3, 5, 5, 3));
			add(ep);
		}
		
		DefaultMutableTreeNode node;
		JPanel nodePanel;
		String fieldName;
		
		Iterator<TreeNode> iterator = new TreeIterator(root);
		
		TreeNode tn;
		
		Border border;
		while (iterator.hasNext()) {
			tn = iterator.next();
			
			if (!(tn instanceof DefaultMutableTreeNode)) continue;
				
			node = (DefaultMutableTreeNode)tn;
			userOb = node.getUserObject();
			if (! (userOb instanceof IField)) continue;
					
			field = (IField)userOb;
			
			// each child node has a list of parameters 
			List<DataComponent> paramComponents = transformFieldParams(field);
			
			// don't add a field unless it has some parameters to display
			if (paramComponents.isEmpty()) continue;
			
			fieldName = TreeOutlineCellRenderer.getFieldDisplayName(field, node);
			
			int indent = (node.getLevel() -1) * 10;
			nodePanel = new JPanel();
			border = new EmptyBorder(0,indent,0,0);
			border = BorderFactory.createCompoundBorder(border, 
								BorderFactory.createTitledBorder(fieldName));
			nodePanel.setBorder(border);
			nodePanel.setBackground(UIUtilities.BACKGROUND_COLOR);
			nodePanel.setLayout(new GridBagLayout());
			
			layoutFields(nodePanel, null, paramComponents, true);
			
			add(nodePanel);
		}
	}
	
	/**
	 * Transforms the objective metadata into the corresponding UI objects.
	 * 
	 * @param params The metadata to transform.
	 */
	private List<DataComponent> transformFieldParams(IField field)
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
	
		IFieldContent content;
		IParam param;
		int contentCount = field.getContentCount();
		for (int c=0; c<contentCount; c++) {
			content = field.getContentAt(c);
			if (! (content instanceof IParam))
				continue;
			
			param = (IParam)content;
			key = param.getAttribute(AbstractParam.PARAM_NAME);
			value = param.toString();
			if ((value != null) && (value.length() > MAX_CHARS)) {
				value = value.substring(0, MAX_CHARS-1) + "...";
			}
			
			String paramType = param.getAttribute(AbstractParam.PARAM_TYPE);
			if (EnumParam.ENUM_PARAM.equals(paramType)) {
				
				List<String> options = new ArrayList<String>();
				String opts = param.getAttribute(EnumParam.ENUM_OPTIONS);
				if (opts != null) {
					String[] list = opts.split(",");
					for (int l=0; l<list.length; l++) {
						options.add(list[l].trim());
					}
				}
				OMEComboBox enumChooser = EditorUtil.createComboBox(options);
				value = param.getAttribute(TextParam.PARAM_VALUE);
				enumChooser.setSelectedItem(value);
				enumChooser.setEditedColor(UIUtilities.EDITED_COLOR);
				area = enumChooser;
			} else if (NumberParam.NUMBER_PARAM.equals(paramType)) {
				area = UIUtilities.createComponent(
						NumericalTextField.class, null);
				value = param.getAttribute(TextParam.PARAM_VALUE);
				String units = param.getAttribute(NumberParam.PARAM_UNITS);
				if (units != null) {
					key = key + " (" + units + ")";
				}
				((NumericalTextField) area).setNumberType(Float.class);
				((NumericalTextField) area).setNegativeAccepted(true);
				((NumericalTextField) area).setText(""+value); // must be number
				((NumericalTextField) area).setEditedColor(
						UIUtilities.EDITED_COLOR);
			} else {
				if (TextParam.TEXT_LINE_PARAM.equals(paramType)) {
					// if text field, value is in the value attribute
					value = param.getAttribute(TextParam.PARAM_VALUE);
					// TODO implement the 'value' for other parameter types
				}
				area = UIUtilities.createComponent(OMETextArea.class, 
						null);
				if (value == null || value.equals(""))
					value = MetadataUI.DEFAULT_TEXT;
				((OMETextArea) area).setText((String) value);
				((OMETextArea) area).setEditedColor(
						UIUtilities.EDITED_COLOR);
			}
			label = UIUtilities.setTextFont(key, Font.BOLD, sizeLabel);
			label.setBackground(UIUtilities.BACKGROUND_COLOR);
			
			area.setEnabled(false);		// don't allow editing.
			comp = new DataComponent(label, area);
			comp.setSetField(value != null);
			cmps.add(comp);
		}
		
		return cmps;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	Reference to the Parent.
	 */
	public MetadataPanelsComponent(MetadataUI parent)
	{
		// if parent is null, title will not be refreshed when model changed, OK
		this.parent = parent;
	}
	
	/**
	 * Allows the model to be set after this UI component has been 
	 * created. 
	 * 
	 * @param treeModel			new model
	 */
	public void setTreeModel(TreeModel treeModel)
	{
		if (model != null) {
			model.removeTreeModelListener(this);
		}
		model = treeModel;
		model.addTreeModelListener(this);
		
		removeAll();
		initComponents();	// also builds UI
	}
	
	/**
	 * Called when the model changes.
	 */
	private void rebuildUI() 
	{
		if (parent != null)
			parent.refreshTitle();
		
		removeAll();
		initComponents();
		
		revalidate();
		repaint();
	}

	/**
	 * Implemented as specified by {@link TreeModelListener} interface.
	 * Calls {@link #rebuildUI()}
	 * 
	 * @see TreeModelListener#treeNodesChanged(TreeModelEvent)
	 */
	public void treeNodesChanged(TreeModelEvent e) {
		rebuildUI();
	}

	/**
	 * Implemented as specified by {@link TreeModelListener} interface.
	 * Calls {@link #rebuildUI()}
	 * 
	 * @see TreeModelListener#treeNodesInserted(TreeModelEvent)
	 */
	public void treeNodesInserted(TreeModelEvent e) {
		rebuildUI();
	}

	/**
	 * Implemented as specified by {@link TreeModelListener} interface.
	 * Calls {@link #rebuildUI()}
	 * 
	 * @see TreeModelListener#treeNodesRemoved(TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e) {
		rebuildUI();
	}

	/**
	 * Implemented as specified by {@link TreeModelListener} interface.
	 * Calls {@link #rebuildUI()}
	 * 
	 * @see TreeModelListener#treeStructureChanged(TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e) {
		rebuildUI();
	}

	/** 
	 * Lays out the passed component.
	 * 
	 * @param pane 		The main component.
	 * @param button	The button to show or hide the unset fields.
	 * @param fields	The fields to lay out.
	 * @param shown		Pass <code>true</code> to show the unset fields,
	 * 					<code>false</code> to hide them.
	 */
	static void layoutFields(JPanel pane, JButton button, 
			List<DataComponent> fields, boolean shown)
	{
		pane.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
	    
		for (DataComponent comp : fields) {
	        c.gridx = 0;
	        if (comp.isSetField() || shown) {
	        	 ++c.gridy;
	        	 c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
	             c.fill = GridBagConstraints.NONE;      //reset to default
	             c.weightx = 0.0;  
	             pane.add(comp.getLabel(), c);
	             c.gridx++;
	             pane.add(Box.createHorizontalStrut(5), c); 
	             c.gridx++;
	             c.gridwidth = GridBagConstraints.REMAINDER;     //end row
	             c.fill = GridBagConstraints.HORIZONTAL;
	             c.weightx = 1.0;
	             pane.add(comp.getArea(), c);  
	        } 
	    }
	    ++c.gridy;
	    c.gridx = 0;
	    //c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
	    //c.fill = GridBagConstraints.NONE;      //reset to default
	    c.weightx = 0.0;  
	    if (button != null) pane.add(button, c);
	}
	
	public String getProtocolTitle() { 	return protTitle;	}
	
}
