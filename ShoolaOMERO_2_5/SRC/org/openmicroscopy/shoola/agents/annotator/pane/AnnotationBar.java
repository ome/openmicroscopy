/*
 * org.openmicroscopy.shoola.agents.annotator.editors.AnnotationBar
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

package org.openmicroscopy.shoola.agents.annotator.pane;


//Java imports
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.annotator.Annotator;
import org.openmicroscopy.shoola.agents.annotator.AnnotatorCtrl;
import org.openmicroscopy.shoola.agents.annotator.AnnotatorUIF;
import org.openmicroscopy.shoola.agents.annotator.IconManager;
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
public class AnnotationBar
    extends JPanel
{

    JButton         save, cancel, delete, saveWithRS;
    
    public AnnotationBar(AnnotatorCtrl control)
    {
        IconManager im = IconManager.getInstance(control.getRegistry());
        initButtons(im, control.getAnnotationIndex());      
        new AnnotationBarMng(this, control);
        buildGUI();
    }
    
    public void saveEnabled(boolean b)
    {
        save.setEnabled(b);
        delete.setEnabled(!b);
    }
    
    public void buttonsEnabled(boolean b)
    {
        save.setEnabled(b);
        delete.setEnabled(b);
    }
    
    /** Initializes the buttons. */
    private void initButtons(IconManager im, int index)
    {
        save = new JButton(im.getIcon(IconManager.SAVE));
        save.setToolTipText(
                UIUtilities.formatToolTipText("Save the Annotation."));
        cancel = new JButton(im.getIcon(IconManager.CANCEL));
        cancel.setToolTipText(
                UIUtilities.formatToolTipText("Close the dialog."));
        delete = new JButton(im.getIcon(IconManager.DELETE));
        delete.setToolTipText(
                UIUtilities.formatToolTipText("Delete the annotation."));
        saveWithRS = new JButton(im.getIcon(IconManager.SAVEWITHRS));
        saveWithRS.setToolTipText(
                UIUtilities.formatToolTipText("Save the annotation and " +
                        "the settings."));
        if (index == Annotator.DATASET) saveWithRS.setEnabled(false);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(buildButtonsBar());
    }

    /** Build a toolBar with buttons. */
    private JToolBar buildButtonsBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(delete);
        bar.add(Box.createRigidArea(AnnotatorUIF.HBOX));
        bar.add(save);
        bar.add(Box.createRigidArea(AnnotatorUIF.HBOX));
        bar.add(saveWithRS);
        bar.add(Box.createRigidArea(AnnotatorUIF.HBOX));
        bar.add(cancel);
        return bar;
    } 

}
