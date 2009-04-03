/*
 * org.openmicroscopy.shoola.agents.annotator.AnnotatorUIF
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

package org.openmicroscopy.shoola.agents.annotator;

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.annotator.pane.AnnotationBar;
import org.openmicroscopy.shoola.agents.annotator.pane.AnnotationPane;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

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
public class AnnotatorUIF
    extends TopWindow
{
    
    public static final Color           STEELBLUE = new Color(0x4682B4);
    
    public static final Dimension       HBOX = new Dimension(5, 0), 
                                        VBOX = new Dimension(0, 5);
    public static final Dimension       DIM_SCROLL = 
                                        new Dimension(300, 150);
    
    public static final int             ROW_TABLE_HEIGHT = 60;
    
    public static final Dimension       DIM_SCROLL_TABLE = 
                                        new Dimension(40, 60);
    
    public static final int             MAX_SCROLLPANE_HEIGHT = 400;
    
    public static final int             SCROLLPANE_HEADER = 30;
    
    public static final int             WIDTH_MAIN = 80, WIDTH_MINOR = 30;
    
    AnnotationPane                      pane;
    
    AnnotationBar                       bar;
    
    private AnnotatorCtrl               control;
    
    AnnotatorUIF(AnnotatorCtrl control, String name, String[] owners, 
                int selectedIndex, List annotations)
    {
        super("Annotator "+name);
        this.control = control;
        initComponents(name, owners, annotations, selectedIndex);
        buildGUI();
        pack();
    }
    
    private void initComponents(String name, String[] owners, List annotations,
                                    int selectedIndex)
    {
        bar = new AnnotationBar(control);
        pane = new AnnotationPane(control, name, owners, annotations, 
                                    selectedIndex); 
        synchBar(selectedIndex);
    }
    
    private  void synchBar(int index)
    {
        int userIndex = control.getUserIndex();
        if (userIndex == index) {
            if (pane.isCreation()) bar.saveEnabled(true);
            else bar.buttonsEnabled(true);
        } else bar.buttonsEnabled(false);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(initTitlePanel(), BorderLayout.NORTH);
        c.add(pane, BorderLayout.CENTER);
        c.add(bar, BorderLayout.SOUTH);
    }
    
    /** Initializes the TitlePanel according to the AnnotationIndex. */
    private TitlePanel initTitlePanel()
    {
        IconManager im = IconManager.getInstance(control.getRegistry());
        TitlePanel tp = null;
        int index = control.getAnnotationIndex();
        switch(index) {
            case Annotator.IMAGE:
                tp = new TitlePanel("Image Annotation", 
                        "Annotate the selected image.", 
                           im.getIcon(IconManager.ANNOTATE_BIG));
                break;
            case Annotator.DATASET:   
                tp = new TitlePanel("Dataset Annotation", 
                        "Annotate the selected dataset.", 
                           im.getIcon(IconManager.ANNOTATE_BIG));
        }
        return tp;
    }
    
}
