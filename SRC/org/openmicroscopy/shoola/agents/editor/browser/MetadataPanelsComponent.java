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
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
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
class MetadataPanelsComponent 
	extends JPanel
	implements TreeModelListener
{
	/** max number of characters to display in field value */
	public static final int 					MAX_CHARS = 25;
	
	/** Reference to the Model. */
	private TreeModel							model;
	
	/** Reference to the parent of this component. */
	private MetadataUI					parent;
	
	
	/** Initiliases the components. */
	private void initComponents()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// root's children
		DefaultMutableTreeNode root =  (DefaultMutableTreeNode)model.getRoot();
		int rootChildCount = root.getChildCount();
		
		IField field;
		DefaultMutableTreeNode node;
		JPanel nodePanel;
		String fieldName;
		for (int i=0; i<rootChildCount; i++) {
			
			node = (DefaultMutableTreeNode)root.getChildAt(i);
			field = (IField)node.getUserObject();
			
			// each child node has a list of parameters 
			List<MetadataComponent> paramComponents = 
				transformFieldParams(field);
			
			fieldName = TreeOutlineCellRenderer.getFieldDisplayName(field);
			
			nodePanel = new JPanel();
			nodePanel.setBorder(BorderFactory.createTitledBorder(fieldName));
			nodePanel.setBackground(UIUtilities.BACKGROUND_COLOR);
			nodePanel.setLayout(new GridBagLayout());
			
			parent.layoutFields(nodePanel, null, paramComponents, true);
			
			add(nodePanel);
		}
	}
	
	/**
	 * Transforms the objective metadata into the corresponding UI objects.
	 * 
	 * @param params The metadata to transform.
	 */
	private List<MetadataComponent> transformFieldParams(IField field)
	{
		List<MetadataComponent> cmps = new ArrayList<MetadataComponent>();
		MetadataComponent comp;
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
				((NumericalTextField) area).setText(""+value); // must be number
				((NumericalTextField) area).setNegativeAccepted(true);
				((NumericalTextField) area).setEditedColor(
						UIUtilities.EDITED_COLOR);
			} else {
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
			
			comp = new MetadataComponent(label, area);
			comp.setSetField(value != null);
			// fieldsObjective.put(key, comp);
			cmps.add(comp);
		}
		
		return cmps;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	Reference to the Parent. Mustn't be <code>null</code>.
	 * @param model		Reference to the Model. Mustn't be <code>null</code>.
	 */
	MetadataPanelsComponent(MetadataUI parent, TreeModel model)
	{
		//if (model == null)
		//	throw new IllegalArgumentException("No model.");
		if (parent == null)
			throw new IllegalArgumentException("No parent.");
		this.parent = parent;
		this.model = model;
	}
	
	/**
	 * Allows the model to be set after this UI component has been 
	 * created. 
	 * 
	 * @param treeModel			new model
	 */
	void setTreeModel(TreeModel treeModel)
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
	
}
