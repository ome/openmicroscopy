package org.openmicroscopy.shoola.agents.editor.uiComponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class CustomPopupMenu extends JPopupMenu {
	
	public static final String ITEM_NAME = "itemName";
	
	public CustomPopupMenu (String[] items) {
	
		JCheckBoxMenuItem menuItem;
		
		ActionListener itemListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (source instanceof JCheckBoxMenuItem) {
					((JCheckBoxMenuItem)source).setSelected(true);
					String itemText = ((JMenuItem)source).getText();
					setSelectedItem(itemText);	// not done automatically
					CustomPopupMenu.this.firePropertyChange(ITEM_NAME, "", itemText);
				}
			}
		};
	
		for (int i=0; i<items.length; i++) {
			
			menuItem = new JCheckBoxMenuItem(items[i]);
			menuItem.addActionListener(itemListener);
			this.add(menuItem);
		}
	}
	
	public void setSelectedItem(String itemText) {
		for (int i=0; i<getComponentCount(); i++) {
			Object component = getComponent(i);
			if (component instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem)component;
				if (item.getText().equals(itemText))
					item.setSelected(true);
				else
					item.setSelected(false);
			}
		}
	}
	
}
