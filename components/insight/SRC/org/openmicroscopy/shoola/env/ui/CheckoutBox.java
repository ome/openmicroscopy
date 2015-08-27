/*
 * org.openmicroscopy.shoola.env.ui.CheckoutBox 
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
package org.openmicroscopy.shoola.env.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays the instances of <code>Agent</code> that can be saved before
 * switching group or closing the application.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class CheckoutBox 
	extends MessageBox
{

	/** The elements to save. */
	private Map<Agent, AgentSaveInfo> map;
	
	/** Component to save all instances. */
	private JCheckBox saveAll;
	
	/** The components to handle. */
	private Map<Agent, List<CheckOutItem>> components;
	
	/** Sets the <code>enabled</code> flag of the <code>CheckOutItem</code>. */
	private void handleSelection()
	{
		if (components == null) return;
		Entry entry;
		Iterator<Entry<Agent, List<CheckOutItem>>> 
		i = components.entrySet().iterator();
		List<CheckOutItem> l;
		Iterator<CheckOutItem> j;
		CheckOutItem item;
		boolean selected = saveAll.isSelected();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			l = (List<CheckOutItem>) entry.getValue();
			if (l != null) {
				j = l.iterator();
				while (j.hasNext()) {
					j.next().setEnabled(selected);
				}
			}
		}
	}
	
	/** Initializes the display. */
	private void initComponents()
	{
		saveAll = new JCheckBox("Save Changes");
		if (map != null && map.size() > 0) saveAll.setSelected(true);
		saveAll.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent evt) {
				handleSelection();
			}
		});
	}
	
	/**
	 * Builds and lays out the component displaying instances of a given agent
	 * to save.
	 * 
	 * @param agent The agent the instances are related to.
	 * @param info Hosts information about an instance of an agent to save.
	 * @return See above.
	 */
	private JComponent buildAgentEntry(Agent agent, AgentSaveInfo info)
	{
		List<CheckOutItem> items = new ArrayList<CheckOutItem>();
		List<Object> instances = info.getInstances();
		Iterator<Object> i = instances.iterator();
		CheckOutItem box;
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		while (i.hasNext()) {
			box = new CheckOutItem(info.getName(), i.next());
			items.add(box);
			p.add(box);
		}
		components.put(agent, items);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		if (map == null || map.size() == 0) return;
		components = new HashMap<Agent, List<CheckOutItem>>();
		Entry entry;
		Iterator i = map.entrySet().iterator();
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(UIUtilities.buildComponentPanel(saveAll));

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JXTaskPane pane = UIUtilities.createTaskPane("List of Changes", null);
		pane.setCollapsed(false);
		while (i.hasNext()) {
			entry = (Entry) i.next();
			content.add(buildAgentEntry((Agent) entry.getKey(),
					(AgentSaveInfo) entry.getValue()));
		}
		pane.add(content);
		p.add(pane);
		addBodyComponent(p);
	}
	
	/**
	 * Creates  a new instance.
	 * 
	 * @param owner		The parent window.
	 * @param title		The title to display on the title bar.
	 * @param message	The notification message.
	 * @param map 		Contains the instances to save.
	 */
	public CheckoutBox(JFrame owner, String title, String message, Icon icon,
			Map<Agent, AgentSaveInfo> map)
	{
		super(owner, title, message, icon);
		this.map = map;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Creates  a new instance.
	 * 
	 * @param owner		The parent window.
	 * @param title		The title to display on the title bar.
	 * @param message	The notification message.
	 * @param map 		Contains the instances to save.
	 */
	public CheckoutBox(JFrame owner, String title, String message, 
			Map<Agent, AgentSaveInfo> map)
	{
		this(owner, title, message, null, map);
	}
	
	/**
	 * Returns the instances of <code>Agent</code> to save.
	 * 
	 * @return See above.
	 */
	Map<Agent, AgentSaveInfo> getInstancesToSave()
	{ 
		if (!saveAll.isSelected()) return null;
		Map<Agent, AgentSaveInfo> toSave = new HashMap<Agent, AgentSaveInfo>();
		Entry entry;
		Iterator i = components.entrySet().iterator();
		Iterator j;
		CheckOutItem item;
		List list;
		String name;
		List<Object> instances;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			list = (List) entry.getValue();
			j = list.iterator();
			instances = new ArrayList<Object>();
			name = "";
			while (j.hasNext()) {
				item = (CheckOutItem) j.next();
				if (item.isSelected()) {
					name = item.getRefName();
					instances.add(item.getInstance());
				}
			}
			if (instances.size() > 0) 
				toSave.put((Agent) entry.getKey(), 
						new AgentSaveInfo(name, instances));
		}
		return toSave; 
	}
	
	/**
	 * Inner class used to handle the instances to save.
	 */
	class CheckOutItem 
		extends JCheckBox
	{
		
		/** The instance to handle. */
		private Object instance;
		
		/** The name of reference. */
		private String refName;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param refName The name of reference.
		 * @param instance The instance to handle.
		 */
		CheckOutItem(String refName, Object instance)
		{
			this.refName = refName;
			this.instance = instance;
			setText(instance.toString());
			setSelected(true);
		}
		
		/**
		 * Returns the instance
		 * 
		 * @return See above.
		 */
		Object getInstance() { return instance; }
		
		/**
		 * Returns the name of reference.
		 * 
		 * @return See above.
		 */
		String getRefName() { return refName; }
		
	}

}
