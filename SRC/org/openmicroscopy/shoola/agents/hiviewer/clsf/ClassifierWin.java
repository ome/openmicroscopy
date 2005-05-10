/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.ClassifierWin
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JDialog;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
abstract class ClassifierWin
    extends JDialog
{
    
    /** 
     * Bound property name indicating that a new category has 
     * been selected.
     */
    public final static String      SELECTED_CATEGORY_PROPERTY = 
                                    "selected_category";
    
    /** Bound property name indicating if the window is closed. */
    public final static String      CLOSED_PROPERTY = "closed";
    
    private static final Dimension  WIN_DIMENSION = new Dimension(300, 300);
    
    /** Horizontal space between the cells in the grid. */
    static final int                H_SPACE = 10;
    
    /** 
     * The selected category to classify the image into or to remove the
     * classification from.
     */
    private CategoryData            selected_category;
    
    /**
     * All the paths in the Category Group trees that
     * are available for classification/declassification.
     */
    protected Set                   availablePaths;
    
    /** Builds and lays out the GUI. */
    private void buildGUI() 
    {
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = new TitlePanel("Classification", getWinTitle(), 
                getWinNote(), icons.getIcon(IconManager.CATEGORY_BIG));
        //Set layout and add components
        setLayout(new BorderLayout(0, 0));
        add(tp, BorderLayout.NORTH);
        add(getClassifPanel(), BorderLayout.CENTER);
    }
    
    /** Fires a property change event and closes the window. */ 
    private void setClosed()
    {
        firePropertyChange(CLOSED_PROPERTY, Boolean.TRUE, Boolean.FALSE);
        setVisible(false);
        dispose();
    }
    
    /** Create a new instance. */
    ClassifierWin(Set availablePaths)
    {
        super();
        if (availablePaths == null)
            throw new IllegalArgumentException("no paths");
        setModal(true);
        setTitle("Classification");
        
        //AttachWindow Listener
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { setClosed(); }
        });
        buildGUI();
    }
    
    /** Returns the title. */
    protected abstract String getWinTitle();
    
    /** Returns the note displayed in the title. */
    protected abstract String getWinNote();
    
    protected abstract JComponent getClassifPanel();
    
    void setSelectedCategory(CategoryData category)
    {
        Object oldValue = selected_category;
        selected_category = category;
        firePropertyChange(SELECTED_CATEGORY_PROPERTY, oldValue, category);
        setClosed();
    }
    
    /** Brings up the window on screen and centers it. */
    void setOnScreen()
    {
        setSize(WIN_DIMENSION);
        UIUtilities.centerAndShow(this);
    }
    
}
