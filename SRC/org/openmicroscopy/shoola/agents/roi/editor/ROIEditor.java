/*
 * org.openmicroscopy.shoola.agents.roi.editor.ROIEditor
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

package org.openmicroscopy.shoola.agents.roi.editor;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.defs.ROIShape;
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
public class ROIEditor
    extends JDialog
{
    
    private static final Dimension  AREA = new Dimension(300, 150), 
                                    HBOX = new Dimension(10, 0);
    
    JTextArea   annotationArea;
    
    JButton     save, cancel;
    
    public ROIEditor(ROIAgtCtrl control, ROIShape roi)
    {
        super(control.getReferenceFrame(), "ROI Edit", true);
        IconManager im = IconManager.getInstance(control.getRegistry());
        initComponents(roi.getAnnotation(), im);
        new ROIEditorMng(this, control, roi);
        buildGUI(im, roi.getLabel());
        pack();
    }
    
    /** Initializes the components. */
    private void initComponents(String annotation, IconManager im)
    {
        annotationArea = new JTextArea();
        annotationArea.setLineWrap(true);
        annotationArea.setWrapStyleWord(true);
        annotationArea.setText(annotation);
        cancel = new JButton(im.getIcon(IconManager.CLOSE));
        save = new JButton(im.getIcon(IconManager.SAVE));
        cancel.setToolTipText(
                UIUtilities.formatToolTipText("Close the window."));
        save.setToolTipText(
                UIUtilities.formatToolTipText("Save the annotation.")); 
        save.setEnabled(false);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI(IconManager im, String txt)
    {
        TitlePanel tp = new TitlePanel("ROI Editor", 
                        "Annotate the selection: "+txt, 
                        im.getIcon(IconManager.ANNOTATE_BIG));
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(tp, BorderLayout.NORTH);
        getContentPane().add(buildAreaPane(), BorderLayout.CENTER);
        getContentPane().add(buildBar(), BorderLayout.SOUTH);
    }
    
    /** Put the text area in a JScrollPane. */
    private JScrollPane buildAreaPane()
    {
        JScrollPane pane = new JScrollPane(annotationArea);
        pane.setPreferredSize(AREA);
        pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return pane;
    }
    
    /** Build toolBar with JButtons. */
    private JPanel buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.setFloatable(true);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(save);
        bar.add(Box.createRigidArea(HBOX));
        bar.add(cancel);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(bar);
        return p;
    }
    
}
