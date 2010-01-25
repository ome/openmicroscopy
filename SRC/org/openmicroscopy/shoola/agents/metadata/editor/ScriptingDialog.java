/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ScriptingDialog 
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
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.util.ScriptComponent;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Builds up a dialog to collect the parameters associated to the script.
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
class ScriptingDialog 
	extends JDialog
	implements ActionListener
{

	/** Bound property indicating to run the script. */
	static final String RUN_SCRIPT_PROPERTY = "runScript";
	
	/** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    /** Title of the dialog. */
    private static final String		TITLE = "Run Script";
    
    /** The text displayed in the header. */
    private static final String		TEXT = "Set the parameters for the " +
    		"script ";
    
    /** The text displayed in the header. */
    private static final String		TEXT_END = ".\n"+ScriptComponent.REQUIRED +
    		"indicates the required parameter."+
    		"\nIf (List) is indicated next to a parameter, use spaces " +
    		"to separate values.\n"+
    		"If (Map) is indicated next to a parameter, use "+
    		ScriptComponent.MAP_SEPARATOR+
    		"to separate (key, value) pair and spaces to separate pairs.\n";
    
	/** Indicates to close the dialog. */
	private static final int CANCEL = 0;
	
	/** Indicates to run the script. */
	private static final int APPLY = 1;
	
	/** Close the dialog. */
	private JButton cancelButton;
	
	/** Run the script. */
	private JButton applyButton;
	
	/** The object to handle. */
	private ScriptObject script;
	
	/** The components to display. */
	private Map<String, ScriptComponent> components;
	
	/** Closes the dialog. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Collects the data and fires a property.*/
	private void runScript()
	{
		Entry entry;
		ScriptComponent c;
		Iterator i = components.entrySet().iterator();
		Map<String, Object> values = new HashMap<String, Object>();
		
		while (i.hasNext()) {
			entry = (Entry) i.next();
			c = (ScriptComponent) entry.getValue();
			values.put((String) entry.getKey(), c.getValue());
		}
		script.setParameterValues(values);
		firePropertyChange(RUN_SCRIPT_PROPERTY, null, script);
		close();
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close the dialog.");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		applyButton = new JButton("Run");
		applyButton.setToolTipText("Run the script.");
		applyButton.setActionCommand(""+APPLY);
		applyButton.addActionListener(this);
		components = new LinkedHashMap<String, ScriptComponent>();
		Map<String, Class> types = script.getParameterTypes();
		if (types == null) return;
		Entry entry;
		Iterator i = types.entrySet().iterator();
		Class type;
		JComponent comp;
		ScriptComponent c;
		String name;
		while (i.hasNext()) {
			comp = null;
			entry = (Entry) i.next();
			type = (Class) entry.getValue();
			name = (String) entry.getKey();
			if (Long.class.equals(type) || Integer.class.equals(type)) {
				comp = new NumericalTextField();
				comp.setToolTipText("Number expected");
				((NumericalTextField) comp).setNumberType(type);
			} else if (String.class.equals(type)) {
				comp = new JTextField();
				comp.setToolTipText("String expected");
			} else if (Boolean.class.equals(type)) {
				comp = new JCheckBox();
				((JCheckBox) comp).setSelected(true);
			} else if (Map.class.equals(type)) {
				comp = new JTextField();
				name += " (Map)";
			} else if (List.class.equals(type)) {
				comp = new JTextField();
				name += " (List)";
			}
			if (comp != null) {
				c = new ScriptComponent(comp, name);
				c.setRequired(true);
				components.put((String) entry.getKey(), c);
			}
		}
	}
	
	/**
	 * Builds the panel hosting the components.
	 * 
	 * @return See above.
	 */
	private JPanel buildControlPanel()
	{
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(null);
		controlPanel.add(applyButton);
		controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		controlPanel.add(cancelButton);
		controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
		bar.add(controlPanel);
		bar.add(Box.createVerticalStrut(10));
		JPanel p = UIUtilities.buildComponentPanelRight(bar);
		return p;
	}
	
	/** 
	 * Builds the component displaying the parameters.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		double[] columns = {TableLayout.PREFERRED, 5, TableLayout.FILL};
		TableLayout layout = new TableLayout();
		layout.setColumn(columns);
		p.setLayout(layout);
		int row = 0;
		Entry entry;
		Iterator i = components.entrySet().iterator();
		ScriptComponent comp;
		JLabel label;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			comp = (ScriptComponent) entry.getValue();
			layout.insertRow(row, TableLayout.PREFERRED);
			p.add(comp.getLabel(), "0,"+row);
			p.add(comp.getComponent(), "2, "+row);
			row++;
		}
		return p;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		String text = TEXT+script.getName()+TEXT_END;
		TitlePanel tp = new TitlePanel(TITLE, text, script.getIconLarge());
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(tp, BorderLayout.NORTH);
		c.add(buildBody(), BorderLayout.CENTER);
		c.add(buildControlPanel(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the frame.
	 * @param script
	 */
	ScriptingDialog(JFrame parent, ScriptObject script)
	{
		super(parent);
		if (script == null)
			throw new IllegalArgumentException("No script specified");
		this.script = script;
		initComponents();
		buildGUI();
		pack();
	}

	/**
	 * Closes or runs the scripts.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				close();
				break;
			case APPLY:
				runScript();
		}
		
	}
	
}
