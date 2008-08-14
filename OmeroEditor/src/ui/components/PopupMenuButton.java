package ui.components;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import uiComponents.CustomButton;

public class PopupMenuButton extends CustomButton {
	
	JPopupMenu popupMenu;

	public PopupMenuButton(String toolTipText, Icon icon, Action[] actions) {
		
		super(icon);
		setToolTipText(toolTipText);
		
		popupMenu = new JPopupMenu();
		
		
		for (int i=0; i<actions.length; i++) {
			JMenuItem menuItem = new JMenuItem(actions[i]);
			popupMenu.add(menuItem);
		}
		
		
		this.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			private void maybeShowPopup(MouseEvent e) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
	}
}
