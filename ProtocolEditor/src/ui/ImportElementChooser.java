/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tree.DataFieldNode;
import util.ImageFactory;

// panel to display import tree, and allow user to select fields for import

public class ImportElementChooser extends JPanel{
	
	IModel model;
	
	ImportElementChooser(IModel model) {
		
		this.model = model;
		
		this.setLayout(new BorderLayout());
		
		Box buttonBox = Box.createVerticalBox();
		
		Icon addElementIcon = ImageFactory.getInstance().getIcon(ImageFactory.TWO_LEFT_ARROW);
		JButton addElementButton = new JButton("Add", addElementIcon);
		addElementButton.addActionListener(new InsertFieldsListener());
		buttonBox.add(addElementButton);
		
		Icon noIcon = ImageFactory.getInstance().getIcon(ImageFactory.N0);
		JButton doneButton = new JButton("Done", noIcon);
		doneButton.addActionListener(new DoneButtonListener());
		buttonBox.add(doneButton);
		
		DataFieldNode rootNode = model.getImportTreeRoot();
		JScrollPane importScrollPane = new JScrollPane(new FormDisplay(rootNode), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.add(buttonBox, BorderLayout.WEST);
		this.add(importScrollPane, BorderLayout.CENTER);
	}

	public class InsertFieldsListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			model.importFieldsFromImportTree();
		}
	}
	
	public class DoneButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			model.setImportFile(null);
		}
	}
}
