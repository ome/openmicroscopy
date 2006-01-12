/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.DOInfo
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class DOInfo
    extends JPanel
{
   
    /**
     * Builds the panel hosting the information
     * 
     * @param details The information to display.
     * @return See above.
     */
    private JPanel buildContentPanel(Map details)
    {
       JPanel content = new JPanel();
       content.setLayout(new GridBagLayout());
       content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
       GridBagConstraints c = new GridBagConstraints();
       c.fill = GridBagConstraints.HORIZONTAL;
       c.anchor = GridBagConstraints.WEST;
       c.insets = new Insets(3, 3, 3, 3);
       Iterator i = details.keySet().iterator();
       JLabel label;
       JTextField area;
       String key, value;
       while (i.hasNext()) {
           ++c.gridy;
           c.gridx = 0;
           key = (String) i.next();
           value = (String) details.get(key);
           label = UIUtilities.setTextFont(key);
           c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
           c.fill = GridBagConstraints.NONE;      //reset to default
           c.weightx = 0.0;  
           content.add(label, c);
           area = new JTextField(value);
           area.setEditable(false);
           label.setLabelFor(area);
           c.gridx = 1;
           c.gridwidth = GridBagConstraints.REMAINDER;     //end row
           c.fill = GridBagConstraints.HORIZONTAL;
           c.weightx = 1.0;
           content.add(area, c);  
       }
       return content;
    }
    
    /** 
     * Builds and lays out the GUI.
     *
     * @param details The visualization map.
     **/
    private void buildGUI(Map details)
    {
        JPanel contentPanel = buildContentPanel(details);
        setLayout(new BorderLayout());
        setMaximumSize(contentPanel.getPreferredSize());
        setBorder(new EtchedBorder());
        add(contentPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param details The visualization map. Mustn't be <code>null</code>.
     */
    DOInfo(Map details)
    {
        if (details == null) 
            throw new IllegalArgumentException("Visualization map cannot be" +
                    " null");
        buildGUI(details);
    }
    
}
