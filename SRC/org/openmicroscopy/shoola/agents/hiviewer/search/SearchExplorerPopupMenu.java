/*
 * org.openmicroscopy.shoola.agents.hiviewer.search.SearchExplorerPopupMenu
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

package org.openmicroscopy.shoola.agents.hiviewer.search;


//Java imports
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.AnnotateCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ClassifyCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.PropertiesCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.env.data.model.DataObject;

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
public class SearchExplorerPopupMenu
    extends JPopupMenu
{

    public static final String  VIEW = "view", BROWSE = "browse";
    
    /** The sole instance. */
    private static SearchExplorerPopupMenu    
        singleton = new SearchExplorerPopupMenu();
    
    
    /**
     * Pops up a menu for the specified search explorer floating window.
     * 
     * @param win The window.  Mustn't be <code>null</code>.
     */
    public static void showMenuFor(SearchExplorer win)
    {
        if (win == null) throw new NullPointerException("No window.");
        singleton.currentWin = win;
        singleton.showMenu();
    }
    
    /** Hides the popup menu. */
    public static void hideMenu() { singleton.setVisible(false); }
    
    /** 
     * Sets the text of the views menu item
     * 
     * @param txt   text of the menuItem.
     */
    public static void setViewText(String txt)
    {
        singleton.view.setText(txt);
    }
    
    public static void setClassifyEnabled(boolean b)
    {
        singleton.classify.setEnabled(b);
    }
    
    /** The window that is currently requesting the menu. */
    private SearchExplorer      currentWin;
    
    private JMenuItem           view, classify;
    
    /**
     * Creates a new instance.
     */
    private SearchExplorerPopupMenu()
    {
        IconManager im = IconManager.getInstance();
        JMenuItem properties = new JMenuItem("Properties", 
                                  im.getIcon(IconManager.PROPERTIES)),
                  annotate = new JMenuItem("Annotate", 
                                  im.getIcon(IconManager.ANNOTATE));
        classify = new JMenuItem("Classify", im.getIcon(IconManager.CLASSIFY));
        view = new JMenuItem(VIEW, im.getIcon(IconManager.VIEWER));
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(properties);
        add(annotate);
        add(classify);
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(view);
        properties.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                DataObject object = currentWin.getDataObject();
                if (object != null) new PropertiesCmd(object).execute();
            }
        });
        annotate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                DataObject object = currentWin.getDataObject();
                if (object != null) new AnnotateCmd(object).execute();
            }
        });
        classify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                DataObject object = currentWin.getDataObject();
                if (object != null) new ClassifyCmd(object).execute();
            }
        });
        view.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                DataObject object = currentWin.getDataObject();
                if (object != null) new ViewCmd(object).execute();
            }
        });
    }
    
    /** Brings up the menu for the {@link #currentWin}. */
    private void showMenu()
    {
        Point p = currentWin.getPopupPoint();
        Point pNew = SwingUtilities.convertPoint(currentWin, p.x, p.y, null); 
        show(currentWin.getParent(), pNew.x, pNew.y);
    }
    
}