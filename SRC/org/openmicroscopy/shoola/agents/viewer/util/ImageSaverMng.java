/*
 * org.openmicroscopy.shoola.agents.viewer.util.ImageSaverMng
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

package org.openmicroscopy.shoola.agents.viewer.util;


//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.BMPFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.image.io.BMPEncoder;
import org.openmicroscopy.shoola.util.image.io.Encoder;
import org.openmicroscopy.shoola.util.image.io.TIFFEncoder;
import org.openmicroscopy.shoola.util.image.io.WriterImage;
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
class ImageSaverMng
{
    
    private static final Color[]        colorSelection;
    
    static {
        colorSelection = new Color[ImageSelection.MAX_COLOR+1];
        colorSelection[ImageSelection.RED] = Color.RED;
        colorSelection[ImageSelection.GREEN] = Color.GREEN;
        colorSelection[ImageSelection.BLUE] = Color.BLUE;
        colorSelection[ImageSelection.CYAN] = Color.CYAN;
        colorSelection[ImageSelection.MAGENTA] = Color.MAGENTA;
        colorSelection[ImageSelection.ORANGE] = Color.ORANGE;
        colorSelection[ImageSelection.PINK] = Color.PINK;
        colorSelection[ImageSelection.YELLOW] = Color.YELLOW;
    }
    
    private static final String     PIN_MSG = "No pin image displayed.",
                                    MAIN_MSG = "No image displayed",
                                    BOTH_MSG = "Must have a pin image " +
                                            "displayed on the main one.";
    private ViewerCtrl              control;
    
    private ImageSaver              view;
    
    ImageSaverMng(ImageSaver view, ViewerCtrl control)
    {
        this.view = view;
        this.control = control; 
    }
    
    ImageSaver getView() { return view; }
    
    void setDisplay(boolean b) { view.setDisplay(b); }
    
    BufferedImage getDisplayImage() { return control.getDisplayImage(); }
    
    BufferedImage getPinImage() { return control.getPinImage(); }
    
    /** Bring up the selection dialog. */
    void showSelectionDialog(String format, String fileName, String message)
    {
        IconManager im = IconManager.getInstance(control.getRegistry());
        SelectionDialog dialog = new SelectionDialog(this, format,
                                        fileName, message, 
                                        im.getIcon(IconManager.QUESTION));
        dialog.pack();  
        UIUtilities.centerAndShow(dialog);
    }
    
    void disposeView()
    {
        view.setVisible(false);
        view.dispose();
    }
    
    /** Save the current bufferedImage. */
    void saveImage(String format, String fileName, String message)
    {
        JComboBox box = view.selection.imageTypes;
        int index = box.getSelectedIndex();
        Encoder encoder = createEncoder(format);
        switch (index) {
            case ImageSaver.IMAGE:
                handleSaveImage(encoder, format, fileName, message);
                break;
            case ImageSaver.PIN_AND_IMAGE:
                handleSavePinAndImage(encoder, format, fileName, message);
                break;
            case ImageSaver.PIN_IMAGE:
                handleSavePinImage(encoder, format, fileName, message);
                break;
            case ImageSaver.PIN_ON_IMAGE:
                handleSavePinOnImage(encoder, format, fileName, message);
                break;
            case ImageSaver.PIN_ON_SIDE:
                handleSavePinOnSide(encoder, format, fileName, message);
        }
    }
    
    /** Save the main image. */
    private void handleSaveImage(Encoder encoder, String format, String name, 
                                String msg)
    {
        BufferedImage img = control.getDisplayImage();
        writeImage(img, encoder, format, name, msg, MAIN_MSG);
    }
    
    /** Save the pin image. */
    private void handleSavePinImage(Encoder encoder, String format, String name,
                                    String msg)
    {
        BufferedImage img = control.getPinImage();
        name +="_pin";
        writeImage(img, encoder, format, name, msg, PIN_MSG);
    }
    
    /** Save the pin and the main image in two separate files. */
    private void handleSavePinAndImage(Encoder encoder, String format, 
                                        String name, String msg)
    {
        BufferedImage img = null;
        JCheckBox box = view.selection.paintingOnOff;
        if (box.isSelected()) {
            int i = view.selection.colors.getSelectedIndex();
            img = control.getDisplayImageWithPinArea(true, colorSelection[i]);
        } else img = control.getDisplayImage();
        
        writeImage(img, encoder, format, name, msg, MAIN_MSG);
        img = control.getPinImage();
        name +="_pin";
        writeImage(img, encoder, format, name, msg, PIN_MSG);
    }
    
    /** Save the pin image on top of the main one. */
    private void handleSavePinOnImage(Encoder encoder, String format, 
                                    String name,  String msg)
    {
        BufferedImage img = control.getPinOnImage();
        writeImage(img, encoder, format, name, msg, BOTH_MSG);
    }
    
    /** 
     * Save the pin image and the main one in the same image. The pin
     * image is painted on the side.
     */
    private void handleSavePinOnSide(Encoder encoder, String format, 
                                            String name,  String msg)
    {
        JCheckBox box = view.selection.paintingOnOff;
        Color c = null;
        if (box.isSelected()) {
            int i = view.selection.colors.getSelectedIndex();
            c = colorSelection[i];
        } 
        BufferedImage img = control.getPinOnSide(box.isSelected(), c);   
        writeImage(img, encoder, format, name, msg, BOTH_MSG);
    }
    
    /** Write the bufferedImage. */
    private void writeImage(BufferedImage img, Encoder encoder, String format, 
                            String name, String msg, String type_msg)
    {
        UserNotifier un = control.getRegistry().getUserNotifier();
        if (img == null) 
            un.notifyError("Save image", type_msg);
        else {
            name +="."+format; //Add extension
            File f = new File(name);
            try {
                if (encoder == null) 
                    WriterImage.saveImage(f, img, format);
                else  
                    WriterImage.saveImage(f, encoder, img);
                un.notifyInfo("Image saved", msg);
            } catch (Exception e) {
                f.delete();
                un.notifyError("Save image failure", "Unable to save the image",
                                    e);
            }
        }
    }
    
    /** 
     * Create the appropriated {@link Encoder}. Return <code>null</code>
     * if no internal encoder is found.
     * @param format
     * @return The appropriated encoder. 
     */
    private Encoder createEncoder(String format)
    {
        Encoder encoder = null;
        if (format.equals(TIFFFilter.TIF)) encoder = new TIFFEncoder();
        else if (format.equals(BMPFilter.BMP)) encoder = new BMPEncoder();
        return encoder;
    }
    
}

