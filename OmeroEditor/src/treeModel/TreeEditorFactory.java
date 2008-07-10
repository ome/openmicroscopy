 /*
 * treeModel.EditorTreeFactory 
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
package treeModel;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import fields.Field;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TreeEditorFactory {

	public static ITreeEditor createTreeEditor() {
		
		TreeModel model = new TreeModel(createDummyTree());
		
		TreeEditorComponent comp = new TreeEditorComponent(model);
		
		comp.initialise();
		
		return comp;
	}
	
	
	public static void main(String[] args) {
		
		JFrame frame = new JFrame("TreeEditor test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComponent view = createTreeEditor().getUI();
        frame.setContentPane(view);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	
	public static TreeNode createDummyTree() {
		
		Field rootField = new Field("Title", "My Experiment");
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootField);
		
		DefaultMutableTreeNode p1Name = new DefaultMutableTreeNode(new Field("Protocol", "10-2-08"));
		DefaultMutableTreeNode p2Name = new DefaultMutableTreeNode(new Field("Temperature", "37'C"));
		DefaultMutableTreeNode c1Name = new DefaultMutableTreeNode(new Field("Cells", "HeLa"));
		DefaultMutableTreeNode c2Name = new DefaultMutableTreeNode(new Field("DNA", "GFP-H2B"));
		DefaultMutableTreeNode c3Name = new DefaultMutableTreeNode(new Field("Incubation time", "3 hrs"));
		DefaultMutableTreeNode c4Name = new DefaultMutableTreeNode(new Field("On ice", "10 mins"));
        
        rootNode.add(p1Name);
        rootNode.add(p2Name);
        p1Name.add(c1Name);
        p1Name.add(c2Name);
        p2Name.add(c3Name);
        p2Name.add(c4Name);
		
		return rootNode;
	}
}
