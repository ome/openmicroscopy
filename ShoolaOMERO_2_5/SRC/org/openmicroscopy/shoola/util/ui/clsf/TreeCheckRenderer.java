/*
 * org.openmicroscopy.shoola.util.ui.clsf.TreeCheckRenderer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.clsf;



//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines the requirements for an object that displays a {@link TreeCheckNode}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreeCheckRenderer
    extends JPanel
    implements TreeCellRenderer
{

    /** The size of the {@link #check} component if hidden. */
    private static final Dimension NULL_SIZE = new Dimension(0, 0);
    
    /** Button to select the node in the tree. */
    protected JToggleButton     check;
    
    /** Label hosting the node. */
    protected TreeCheckLabel    label;
    
    /** Flag to indicate to add the selection box to this component. */
    private boolean             leafOnly;
    
    /** The original size of the {@link #check} component. */
    private Dimension           restoredSize;
    
    /**
     * Creates a new instance.
     * 
     * @param leafOnly Passed <code>true</code> to allow leaves selection only
     *                  <code>false</code> otherwise.
     */
    TreeCheckRenderer(boolean leafOnly)
    {
        this.leafOnly = leafOnly;
        restoredSize = null;
        setLayout(null);
        label = new TreeCheckLabel();
        check = new JCheckBox();
        check.setBackground(UIManager.getColor("Tree.textBackground"));
    }
    
    /** 
     * Initializes the {@link #check} component.
     * 
     * @param buttonType    One of the following type: {@link JRadioButton} or
     *                      {@link JCheckBox}.
     */
    void initToggleButton(Class buttonType)
    {
        if (buttonType.equals(JCheckBox.class)) check = new JCheckBox();
        else check = new JRadioButton();
    }
    
    /**
     * Overriden to set the selection box and the label.
     * @see TreeCellRenderer#getTreeCellRendererComponent(JTree, Object, 
     *                          boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, 
            boolean selected, boolean expanded, boolean leaf, int row, 
            boolean hasFocus)
    {
        setEnabled(tree.isEnabled());
        if (restoredSize == null) restoredSize = check.getPreferredSize();
        TreeCheckNode node = (TreeCheckNode) value;
        check.setSelected(node.isSelected());
        label.setFont(tree.getFont());
        label.setText(node.toString());
        label.setSelected(selected);
        label.setFocus(hasFocus);
        Icon nodeIcon = node.getNodeIcon();
        if (node.isLeafNode()) {
            if (nodeIcon == null) nodeIcon = UIManager.getIcon("Tree.leafIcon");
            check.setPreferredSize(restoredSize);
            add(check);
            add(label);
        } else {
            if (nodeIcon == null) {
                if (expanded) nodeIcon = UIManager.getIcon("Tree.openIcon");
                else nodeIcon = UIManager.getIcon("Tree.closedIcon");
            }
            if (leafOnly) check.setPreferredSize(NULL_SIZE);
            else {
                check.setPreferredSize(restoredSize);
                add(check);
            } 
            add(label);
        }
        label.setIcon(nodeIcon);
        return this;
    }
    
    /**
     * Overriden to set the ideal size of this component depending on the size
     * of the {@link #check} component and the {@link #label} component.
     * @see JPanel#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = label.getPreferredSize();
        return new Dimension(d_check.width+d_label.width,
                            (d_check.height < d_label.height ?
                                        d_label.height : d_check.height));
        
    }

    /**
     * Overriden to lay out the component since the layout is set to
     * <code>null</code>.
     * @see JPanel#doLayout()
     */
    public void doLayout()
    {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = label.getPreferredSize();
        int y_check = 0;
        int y_label = 0;
        if (d_check.height < d_label.height)
          y_check = (d_label.height-d_check.height)/2;
        else
          y_label = (d_check.height-d_label.height)/2;
        check.setBounds(0, y_check, d_check.width, d_check.height);   
        label.setBounds(d_check.width, y_label, d_label.width, d_label.height);   
    }
    
    /**
     * Overriden to set the default color.
     * @see JPanel#setBackground(Color)
     */
    public void setBackground(Color color)
    {
        if (color instanceof ColorUIResource) color = null;
        super.setBackground(color);
    }
    
}
