/*
 * org.openmicroscopy.shoola.agents.datamng.DataTreeCellRenderer
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

package org.openmicroscopy.shoola.agents.datamng;



//Java imports
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.CategorySummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class DataTreeCellRenderer
	extends DefaultTreeCellRenderer
{
    
	/** ID used to select the appropriated icon. */
	static final int    			    ROOT_ICON = 0;   
	static final int    			    PROJECT_ICON = 1;
	static final int    			    DATASET_ICON = 2;
	static final int    			    IMAGE_ICON = 3;
    static final int                    CATEGORY_GROUP_ICON = 4;
    static final int                    CATEGORY_ICON = 5;
    static final int                    ANNOTATED_DATASET_ICON = 6;
    static final int                    ANNOTATED_IMAGE_ICON = 7;
	static final int                    NO_ICON = 8; 
	
	private Registry	                registry;
    
	DataTreeCellRenderer(Registry registry)
	{
		this.registry = registry;
	}
    
	public Component getTreeCellRendererComponent(JTree tree, Object value,
						boolean sel, boolean expanded, boolean leaf,
						int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
												row, hasFocus);
		IconManager im = IconManager.getInstance(registry);
		int index = getIconID(value);
		try {
			switch (index) {
				case ROOT_ICON:
					setIcon(im.getIcon(IconManager.ROOT)); break;
				case PROJECT_ICON:
					setIcon(im.getIcon(IconManager.PROJECT)); break;
				case DATASET_ICON:
					setIcon(im.getIcon(IconManager.DATASET)); break;
				case IMAGE_ICON:
					setIcon(im.getIcon(IconManager.IMAGE)); break;
                case CATEGORY_ICON:
                    setIcon(im.getIcon(IconManager.CATEGORY)); break;
                case CATEGORY_GROUP_ICON:
                    setIcon(im.getIcon(IconManager.CATEGORY_GROUP)); break;
                case ANNOTATED_DATASET_ICON:
                    setIcon(im.getIcon(IconManager.ANNOTATED_DATASET)); break;
                case ANNOTATED_IMAGE_ICON:
                    setIcon(im.getIcon(IconManager.ANNOTATED_IMAGE)); break;
				case NO_ICON:
					setIcon(null);
			}									
		} catch(NumberFormatException nfe) {   
			throw new Error("Invalid Action ID "+index, nfe);
		} 
		return this;
	}
	
	private int getIconID(Object value)
	{
		DefaultMutableTreeNode  node = (DefaultMutableTreeNode) value;
		Object usrObject = node.getUserObject();
		int id = ROOT_ICON;
		if (node.getLevel() != 0) {
			if (usrObject instanceof ProjectSummary)  id = PROJECT_ICON;
			else if (usrObject instanceof DatasetSummary)
                id = DATASET_ICON;
			else if (usrObject instanceof ImageSummary) {
               if (((ImageSummary) usrObject).getAnnotation() == null)
                   id = IMAGE_ICON;
               else id = ANNOTATED_IMAGE_ICON;
            }
            else if (usrObject instanceof CategoryGroupData) 
                id = CATEGORY_GROUP_ICON;
            else if (usrObject instanceof CategorySummary) id = CATEGORY_ICON;
			else if (usrObject instanceof String) id = NO_ICON;
		}
		return id;
	}

}
