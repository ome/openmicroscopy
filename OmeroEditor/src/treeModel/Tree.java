
/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

//package components;

/*
 * This code is based on an example provided by Richard Stanford, 
 * a tutorial reader.
 */

package treeModel;


import java.awt.Toolkit;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

public class Tree extends JTree {
    protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel treeModel;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();

    public Tree(DefaultTreeModel tModel) {
        
    	super (tModel);
        rootNode = (DefaultMutableTreeNode)tModel.getRoot();
        treeModel = tModel;

        this.setEditable(true);
       // this.getSelectionModel().setSelectionMode
        //        (TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        this.setSelectionModel(new ContiguousChildSelectionModel());
        this.setShowsRootHandles(true);
    }



   
    
    /** Add child to the currently selected node. */
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = getSelectionPath();

        if (parentPath == null) {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode)
                         (parentPath.getLastPathComponent());
        }

        return addObject(parentNode, child, true);
    }

    /** Add sibling after the currently selected node. */
    public DefaultMutableTreeNode addSiblingObject(Object child) {
        DefaultMutableTreeNode siblingNode = null;
        TreePath currentPath = getSelectionPath();

        if (currentPath == null) {
            siblingNode = rootNode;
        } else {
            siblingNode = (DefaultMutableTreeNode)
                         (currentPath.getLastPathComponent());
        }

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)
        	siblingNode.getParent();
        int indexToAdd = 0;
        
        if (parentNode == null) {
        	parentNode = rootNode;
        	indexToAdd = rootNode.getChildCount();
        } else {
        	indexToAdd = parentNode.getIndex(siblingNode) + 1;
        }
         
        
        return addObject(parentNode, child, true, indexToAdd);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {
        return addObject(parent, child, false);
    }
    
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
            Object child, 
            boolean shouldBeVisible) {
    	
    	if (parent == null) {
            parent = rootNode;
        }
    	
    	return addObject(parent, child, 
    			shouldBeVisible, parent.getChildCount());
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child, 
                                            boolean shouldBeVisible,
                                            int insertIndex) {
        DefaultMutableTreeNode childNode = 
                new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = rootNode;
        }
	
	//It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, parent, insertIndex);

        //Make sure the user can see the lovely new node.
        if (shouldBeVisible) {
        	TreePath treePath = new TreePath(childNode.getPath());
            scrollPathToVisible(treePath);
            this.setSelectionPath(treePath);
        }
        return childNode;
    }

    class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            DefaultMutableTreeNode node;
            node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());

            /*
             * If the event lists children, then the changed
             * node is the child of the node we've already
             * gotten.  Otherwise, the changed node and the
             * specified node are the same.
             */

                int index = e.getChildIndices()[0];
                node = (DefaultMutableTreeNode)(node.getChildAt(index));

            System.out.println("The user has finished editing the node.");
            System.out.println("New value: " + node.getUserObject());
        }
        public void treeNodesInserted(TreeModelEvent e) {
        }
        public void treeNodesRemoved(TreeModelEvent e) {
        }
        public void treeStructureChanged(TreeModelEvent e) {
        }
    }
}
