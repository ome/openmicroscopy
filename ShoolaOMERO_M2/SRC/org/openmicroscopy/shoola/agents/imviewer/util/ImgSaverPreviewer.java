/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ImgSaverPreviewer
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

package org.openmicroscopy.shoola.agents.imviewer.util;




//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Modal dialog displaying the preview images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImgSaverPreviewer
    extends JDialog
{


    /** Space between each image. */
    static final int        SPACE = 10;
    
    /** Reference to the model. */
    private ImgSaver                model;
    
    /** Button to cancel the operation and close the window. */
    private JButton                 cancelButton;
    
    /** Button to save the image. */
    private JButton                 saveButton;
    
    /** Box to paint the unit bar on the image to save. */
    private JCheckBox               unitBox;
    
    /** The canvas on which the images are painted. */
    private ImgSaverPreviewerCanvas canvas;
    
    /** The component hosting the canvas. */
    private JLayeredPane            layeredPane;
    
    /** 
     * Sets the dimensions of the various components.
     * 
     * @param w The width of the components.
     * @param h The height of the components.
     */
    private void setDimensions(int w, int h)
    {
        Dimension d = new Dimension(w, h);
        canvas.setPreferredSize(d);
        canvas.setSize(d);
        layeredPane.setPreferredSize(d);
        layeredPane.setSize(d);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 7*(screenSize.width/10);
        int height = 7*(screenSize.height/10);
        w += SPACE;
        h += SPACE;
        if (w > width) w = width;
        if (h > height) h = height;
        setSize(w, h); 
        setPreferredSize(new Dimension(w, h));
    }
    
    /** Closes and disposes. */
    private void onClose()
    {
        setVisible(false);
        dispose();
    }
    
    /** Adds listeners to the components. */
    private void initListeners()
    {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { onClose(); }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onClose(); }
        });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
                model.saveImage();
                onClose();
            }
        });
    }
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        IconManager icons = IconManager.getInstance();
        saveButton = new JButton(icons.getIcon(IconManager.SAVE));
        saveButton.setText("Save As");
        saveButton.setToolTipText(
                UIUtilities.formatToolTipText("Save the preview image."));
        cancelButton = new JButton(icons.getIcon(IconManager.CANCEL));
        cancelButton.setText("Cancel");
        cancelButton.setToolTipText(
                UIUtilities.formatToolTipText("Close without saving."));
        canvas = new ImgSaverPreviewerCanvas(this);
        layeredPane = new JLayeredPane();
        layeredPane.add(canvas, new Integer(0));
    }
    
    /**
     * Builds the tool bar hosting the controls.
     * 
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.add(cancelButton);
        bar.add(saveButton);
        return UIUtilities.buildComponentPanelRight(bar);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        getContentPane().add(buildToolBar(), BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(layeredPane), BorderLayout.CENTER);
    }
     
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    ImgSaverPreviewer(ImgSaver model)
    {
        super(model);
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        setTitle(ImgSaver.PREVIEW_TITLE);
        setModal(true);
        initComponents();
        initListeners();
    }
    
    /**
     * Displays the buffered image and its components. The components are 
     * images, one per channel composing the main image.
     */
    void initialize()
    {
        int w = SPACE;
        BufferedImage img = model.getImage();
        List l = model.getImageComponents();
        if (l != null) {
            int n = l.size();
            w += n*img.getWidth()+(n-1)*SPACE;
        }
        w += (img.getWidth()+SPACE);
        setDimensions(w, img.getHeight()+2*SPACE);
        canvas.repaint();
        buildGUI();
        pack();
    }
    
    /**
     * Returns the main image.
     * 
     * @return See above.
     */
    BufferedImage getImage() { return model.getImage(); }
    
    /**
     * Returns the images composing the main image i.e. one per channel.
     * 
     * @return See above.
     */
    List getImageComponents() { return model.getImageComponents(); }
    
}
