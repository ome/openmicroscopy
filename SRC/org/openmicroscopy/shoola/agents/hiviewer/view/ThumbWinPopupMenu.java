/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.ThumbWinPopupMenu
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.AnnotateCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ClassifyCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.PropertiesCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ViewCmd;
import pojos.ImageData;

/** 
 * The pop-up menu for the thumbnail floating windows.
 * The menu is shared among all instances of {@link ThumbWinPopupMenu} and
 * triggers the execution of commands to view an Image's properties, to
 * annotate it, to classify it, or to view it.
 * 
 * @see ThumbWinManager
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
class ThumbWinPopupMenu
    extends JPopupMenu
{

    /** The sole instance. */
    private static ThumbWinPopupMenu    singleton = new ThumbWinPopupMenu();
    
    /**
     * Pops up a menu for the specified thumbnail floating window.
     * 
     * @param win   The window. Mustn't be <code>null</code>.
     */
    public static void showMenuFor(ThumbWin win)
    {
        if (win == null) throw new NullPointerException("No window.");
        singleton.currentWin = win;
        singleton.showMenu();
    }
    
    /** Hides the popup menu. */
    public static void hideMenu() { singleton.setVisible(false); }
    
    /** The window that is currently requesting the menu. */
    private ThumbWin    currentWin;

    /** Creates a new instance. */
    private ThumbWinPopupMenu()
    {
        IconManager im = IconManager.getInstance();
        JMenuItem properties = new JMenuItem("Properties", 
                                  im.getIcon(IconManager.PROPERTIES)),
                  classify = new JMenuItem("Categorise", 
                		  				im.getIcon(IconManager.CATEGORY)),
                  declassify = new JMenuItem("Decategorise", 
                		  im.getIcon(IconManager.DECATEGORISE)),
                  view = new JMenuItem("View", im.getIcon(IconManager.VIEWER)),
                  annotate = new JMenuItem("Annotate", 
                          im.getIcon(IconManager.ANNOTATE));
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(view);
        add(new JSeparator(JSeparator.HORIZONTAL));
        add(classify);
        add(declassify);
        add(annotate);
        add(new JSeparator(JSeparator.HORIZONTAL));
        add(properties);
        properties.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                new PropertiesCmd(currentWin.getModel(), 
                        currentWin.getDataObject()).execute();
            }
        });

        annotate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                new AnnotateCmd(currentWin.getModel(), 
                        currentWin.getSelectedNode()).execute();
            }
        });

        classify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                ClassifyCmd cmd = new ClassifyCmd(currentWin.getModel(),
                        (ImageData) currentWin.getDataObject(), 
                        ClassifyCmd.CLASSIFICATION_MODE);
                cmd.execute();
            }
        });
        declassify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                ClassifyCmd cmd = new ClassifyCmd(currentWin.getModel(),
                        		(ImageData) currentWin.getDataObject(), 
                        		ClassifyCmd.DECLASSIFICATION_MODE);
                cmd.execute();
            }
        });
        view.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                new ViewCmd(currentWin.getDataObject()).execute();
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
