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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
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
    
    private static final int        WIDTH = 150, HEIGHT = 150;

    private static final String     MAX_LETTER = "1000%";
    
    static final int                PIXELS = 0, MICRONS = 1, MAX = 1;
    
    private static final String[]   UNITS;
    
    static {
        UNITS = new String[MAX+1];
        UNITS[PIXELS] = "pixels";
        UNITS[MICRONS] = "microns";
    }
    
    private boolean                 active;
    
    JButton                         magPlus, magMinus, magFit;
    
    JScrollPane                     scrollPane;
    
    JLayeredPane                    layer;
    
    JTextField                      magText;
    
    JComboBox                       units;

    double                          unitFactor;
    
    private int                     width, height;
    
    private JLabel                  xyInfo, whInfo;
    
    private ROIImageCanvas          roiCanvas;
    
    private ROIViewerMng            mng;

    public ROIViewer(ROIAgtCtrl control, int index)
    {
        super(control.getReferenceFrame(), "ROI #"+index);
        initComponents(IconManager.getInstance(control.getRegistry()));
        unitFactor = 1;
        mng = new ROIViewerMng(this, control);
        buildGUI(mng);
        setSize(WIDTH, HEIGHT);
    }
    
    public void setWidgetName(int index)
    { 
        setTitle("ROI #"+index);
    }
    
    public void resetMagnificationFactor()
    {
        mng.resetMagnificationFactor();
    }
    
    //NOTE: pa copy of the original drawn on screen. 
    public void setImage(BufferedImage img, PlaneArea pa)
    {
        if (pa == null) {
            width = 0;
            height = 0;
            setWHInfo(0, 0);
        } else {
            Rectangle r = pa.getBounds();
            setXYInfo(r.x, r.y);
            width = r.width;
            height = r.height;
            setWHInfo((int) (width*unitFactor), (int) (height*unitFactor));
            //Set the bounds for the clip
            pa.setBounds(0, 0, r.width, r.height);
        }
        if (!active) {
            if (img != null) {
                int w = (int) (img.getWidth()*mng.getFactor()+
                                2*ROIAgtUIF.START), 
                    h = (int) (img.getHeight()*mng.getFactor()+
                                2*ROIAgtUIF.START);
                setComponentsSize(w, h);
                active = true;
            }
        }
        roiCanvas.paintImage(img, pa); 
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
    
    void setWHInfo()
    { 
        setWHInfo((int) (width*unitFactor), (int) (height*unitFactor));
    }
    
    private void setComponentsSize(int w, int h)
    {
        Dimension d = new Dimension(w, h);
        roiCanvas.setPreferredSize(d);
        roiCanvas.setSize(d);
        layer.setPreferredSize(d);
        layer.setSize(d);
    }

    private void setXYInfo(int x, int y)
    {
        String  html = "<html><table colspan=0 rowspan=0 border=0><tr>";
        html += "<td>x:<br>y:</td>";
        html += "<td align=right>"+x+"<br>"+y+"</td>";
        html += "</tr></table><html>";
        xyInfo.setText(html);
    }
    
    private void setWHInfo(int w, int h)
    {
        String  html = "<html><table colspan=0 rowspan=0 border=0><tr>";
        html += "<td>width:<br>height:</td>";
        html += "<td align=right>"+w+"<br>"+h+"</td>";
        html += "</tr></table><html>";
        whInfo.setText(html);
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
        units = new JComboBox(UNITS);
        units.setToolTipText(
                UIUtilities.formatToolTipText("Reset."));
        xyInfo = new JLabel();
        setXYInfo(0, 0);
        whInfo = new JLabel();
        setWHInfo(0, 0); 
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
        container.add(buildBottomBar(), BorderLayout.SOUTH);
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
        bar.add(magPlus);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(magFit);
        return bar;
    }
    
    private JPanel buildBottomBar()
    {
        JPanel p = new JPanel(), textPanel = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
        textPanel.add(xyInfo);
        textPanel.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        textPanel.add(whInfo);
        p.add(UIUtilities.buildComponentPanel(units));
        p.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        p.add(textPanel);
        return UIUtilities.buildComponentPanel(p);
    }
}
