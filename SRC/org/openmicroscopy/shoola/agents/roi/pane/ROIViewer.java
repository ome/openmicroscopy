/*
 * org.openmicroscopy.shoola.agents.roi.pane.ROIViewer
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

package org.openmicroscopy.shoola.agents.roi.pane;

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.canvas.ROIImageCanvas;
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
public class ROIViewer
    extends JDialog
{

    /** Background color. */
    private static final Color      STEELBLUE = new Color(0x4682B4);
    
    private static final int        WIDTH = 80, HEIGHT = 120;

    private static final String     MAX_LETTER = "1000%";
    
    private boolean                 active;
    
    JButton                         magPlus, magMinus, magFit;
    
    JScrollPane                     scrollPane;
    
    JLayeredPane                    layer;
    
    JTextField                      magText;
    
    private ROIImageCanvas          roiCanvas;
    
    private ROIViewerMng            mng;
    
    public ROIViewer(ROIAgtCtrl control, int index)
    {
        super(control.getReferenceFrame(), "ROI #"+index);
        initComponents(IconManager.getInstance(control.getRegistry()));
        mng = new ROIViewerMng(this);
        buildGUI(mng);
        setSize(WIDTH, HEIGHT);
    }
    
    public void setWidgetName(int index)
    {
        setTitle("ROI #"+index);
    }
    
    public void setImage(BufferedImage img)
    {
        if (!active) {
            if (img != null) {
                setComponentsSize((int) (img.getWidth()*mng.getFactor()),
                                (int) (img.getHeight()*mng.getFactor()));
                active = true;
            }
        }
        roiCanvas.paintImage(img); 
    }
    
    void magnify(double f)
    {
        BufferedImage img = roiCanvas.getROIImage();
        if (img != null) {
            setComponentsSize((int) (img.getWidth()*f),
                                (int) (img.getHeight()*f));
        }
        roiCanvas.magnify(f);
    }
    
    void setComponentsSize(int w, int h)
    {
        Dimension d = new Dimension(w, h);
        roiCanvas.setPreferredSize(d);
        roiCanvas.setSize(d);
        layer.setPreferredSize(d);
        layer.setSize(d);
    }
    
    private void initComponents(IconManager im)
    {
        String s = ""+(int)(ROIViewerMng.MIN_MAG*100)+"%";
        magText = new JTextField(s, MAX_LETTER.length());
        magText.setEditable(false);
        magText.setForeground(STEELBLUE);
        magText.setToolTipText(
                UIUtilities.formatToolTipText("Zooming percentage.")); 
        magPlus = new JButton(im.getIcon(IconManager.ZOOM_IN));
        magPlus.setToolTipText(
            UIUtilities.formatToolTipText("Zoom in.")); 
        magMinus = new JButton(im.getIcon(IconManager.ZOOM_OUT));
        magMinus.setToolTipText(
            UIUtilities.formatToolTipText("Zoom out."));
        magFit = new JButton(im.getIcon(IconManager.ZOOM_FIT));
        magFit.setToolTipText(
            UIUtilities.formatToolTipText("Reset."));
    }
    
    private void buildGUI(ROIViewerMng mng)
    {
        Container container = getContentPane();
        layer = new JLayeredPane();
        roiCanvas = new ROIImageCanvas(mng);
        layer.add(roiCanvas, new Integer(0));
        //Default
        setComponentsSize(1, 1);
        scrollPane = new JScrollPane(layer);
        container.add(buildBar(), BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel buildBar()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(buildToolBar());
        p.add(UIUtilities.buildComponentPanel(magText));
        return p;
    }
    
    /** Build the toolBar. */
    private JToolBar buildToolBar() 
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(magMinus);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(magFit);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(magPlus);
        return bar;
    }
    
}
