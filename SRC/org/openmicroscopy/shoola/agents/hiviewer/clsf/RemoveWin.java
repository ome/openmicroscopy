/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.RemoveWin
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

package org.openmicroscopy.shoola.agents.hiviewer.clsf;


//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Builds a Panel displaying the CategoryGroup>Category in which the 
 * selected image has been classified.
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
class RemoveWin
    extends ClassifierWin
{

    private static final String     PANEL_TITLE = "Remove From Category";
    private static final String     PANEL_TEXT = "Deselect to declassify.";
    private static final String     PANEL_NOTE = 
                                         "The image is currently classified "+
                                         "under the following categories.";
    private static final String     UNCLASSIFIED_TEXT = "The selected image " +
                                            "hasn't been classified";
    
    
    RemoveWin(Set availablePaths, JFrame owner)
    {
        super(availablePaths, owner);
        buildGUI();
    }

    protected String getPanelTitle() { return PANEL_TITLE; }

    protected String getPanelText() { return PANEL_TEXT; }
    
    protected String getPanelNote() { return PANEL_NOTE; }
    
    /** Builds the main panel displayed in ClassifierWin. */
    protected JComponent getClassifPanel()
    {
        JPanel main = new JPanel();
        main.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagLayout gridbag = new GridBagLayout();
        main.setLayout(gridbag);
        GridBagConstraints cst = new GridBagConstraints();
        cst.ipadx = ClassifierWin.H_SPACE;
        cst.anchor = GridBagConstraints.EAST;
        Iterator i = availablePaths.iterator();
        int index = 0;
        while (i.hasNext()) 
            addRow(gridbag, cst, main, index, (DataObject) i.next());
        return new JScrollPane(UIUtilities.buildComponentPanel(main));
    }
    
    /** Add a row to the the GridBagLayout. */
    private void addRow(GridBagLayout gridbag, GridBagConstraints c, 
                        JPanel main, int index, DataObject data)
    {
        //The image has not been classified.
        if (data instanceof ImageSummary) {
            main.add(new JLabel(UNCLASSIFIED_TEXT));
            return;
        }
        JCheckBox box = new JCheckBox();
        box.setSelected(true);
        String name = "";
        if (data instanceof CategoryGroupData) {
            CategoryGroupData cg = (CategoryGroupData) data;
            name += cg.getName()+">";
            List l = cg.getCategories();
            if (l.size() == 1) {
                final CategoryData category = (CategoryData) l.get(0);
                name += category.getName();
                box.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e)
                    {
                        setSelectedCategory(category);
                    }
                });
            }
        } else if (data instanceof CategoryData) { 
            //i.e. the Category is orphan, shouldn't happen
            final CategoryData category = (CategoryData) data;
            name += category.getName();
            box.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e)
                {
                    setSelectedCategory(category);
                }
            });
        }
        //init JLabel
        JLabel label = new JLabel(name);
        c.gridx = 0;
        c.gridy = index;
        gridbag.setConstraints(box, c);
        main.add(box);
        c.gridx = 1;
        gridbag.setConstraints(label, c);
        main.add(label);
    }

}
