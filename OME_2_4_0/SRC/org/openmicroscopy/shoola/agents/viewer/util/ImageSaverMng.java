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
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;
import org.openmicroscopy.shoola.env.config.Registry;
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
    
    private static final String     PIN_MSG = "No lens image displayed.",
                                    MAIN_MSG = "No image displayed",
                                    BOTH_MSG = "Must have a lens image " +
                                            "displayed on the main one.";
    
    private String                  fileName, message, format, saveMessage;
    
    private BufferedImage           lensImage, image;
    
    private Encoder                 encoder;
    
    private ViewerCtrl              control;
    
    private ImageSaver              view;
    
    private Preview                 preview;
    
    ImageSaverMng(ImageSaver view, ViewerCtrl control)
    {
        this.view = view;
        this.control = control; 
        setDefault();
        preview = new Preview(this);
    }
    
    Registry getRegistry() { return control.getRegistry(); }
    
    ViewerUIF getReferenceFrame() { return control.getReferenceFrame(); }
    
    ImageSaver getView() { return view; }
    
    void setDisplay(boolean b) { view.setDisplay(b); }
    
    BufferedImage getDisplayImage() { return control.getDisplayImage(); }
    
    BufferedImage getPinImage() { return control.getPinImage(); }
    
    /** Bring up the selection dialog. */
    void showSelectionDialog(String format, String fileName, String message)
    {
        if (format == null || fileName == null) {
            UserNotifier un = control.getRegistry().getUserNotifier();
            un.notifyError("Save image", "name cannot be null");
            return;
        }
        this.format = format;
        this.fileName = fileName;
        this.message = message;
        IconManager im = IconManager.getInstance(control.getRegistry());
        SelectionDialog dialog = new SelectionDialog(this, 
                                        im.getIcon(IconManager.QUESTION));
        dialog.pack();  
        UIUtilities.centerAndShow(dialog);
    }
    
    /** Close the {@link ImageSaver } widget. */
    void disposeView()
    {
        view.setVisible(false);
        view.dispose();
    }
    
    void saveImage() { saveImage(format, fileName, message); }
    
    /** Save the preview image .*/
    void savePreviewImage()
    {
        if (image ==  null) {
            UserNotifier un = control.getRegistry().getUserNotifier();
            un.notifyError("Save image", saveMessage);
            cancelPreviewSaveImage();
            return;
        }
        if (image != null && lensImage == null)
            writeImage(image, fileName);
        else if (image != null && lensImage != null) {
            writeImage(image, fileName);
            fileName +="_lens";
            writeImage(image, fileName);
        }
        cancelPreviewSaveImage();
    }
    
    /** 
     * Don't save the preview image. Keep the {@link ImageSaver} widget 
     * visible.
     */
    void cancelPreviewSaveImage()
    {
        setDefault();
        preview.setVisible(false);
        preview.dispose();
    }
    
    /** Save the current bufferedImage. */
    void saveImage(String format, String fileName, String message)
    {   
        if (format == null || fileName == null) {
            UserNotifier un = control.getRegistry().getUserNotifier();
            un.notifyError("Save image", "name cannot be null");
            return;
        }
        this.format = format;
        this.fileName = fileName;
        this.message = message;
        JComboBox box = view.selection.imageTypes;
        int index = box.getSelectedIndex();
        encoder = createEncoder(format);
        switch (index) {
            case ImageSaver.IMAGE:
                handleSaveImage();
                break;
            case ImageSaver.PIN_AND_IMAGE:
                handleSavePinAndImage();
                break;
            case ImageSaver.PIN_IMAGE:
                handleSavePinImage();
                break;
            case ImageSaver.PIN_ON_IMAGE:
                handleSavePinOnImage();
                break;
            case ImageSaver.PIN_ON_SIDE_TOP_LEFT:
                handleSavePinTopLeft();
                break;
            case ImageSaver.PIN_ON_SIDE_TOP_RIGHT:
                handleSavePinTopRight();
                break;
            case ImageSaver.PIN_ON_SIDE_BOTTOM_LEFT:
                handleSavePinBottomLeft();
                break;
            case ImageSaver.PIN_ON_SIDE_BOTTOM_RIGHT:
                handleSavePinBottomRight();
                break;
            case ImageSaver.IMAGE_AND_ROI:
                handleSaveImageAndROI();
                break;   
        }
    }
    
    private void handleSaveImageAndROI()
    {
        image = control.getImageAndROIs();
        lensImage = null;
        saveMessage = MAIN_MSG;
        preview.setImage(image);
        UIUtilities.centerAndShow(preview);
    }
    
    
    /** Save the main image. */
    private void handleSaveImage()
    {
        image = control.getDisplayImage();
        lensImage = null;
        saveMessage = MAIN_MSG;
        preview.setImage(image);
        UIUtilities.centerAndShow(preview);
    }
    
    /** Save the pin image. */
    private void handleSavePinImage()
    {
        image = control.getPinImage();
        lensImage = null;
        saveMessage = PIN_MSG;
        preview.setImage(image);
        UIUtilities.centerAndShow(preview);
    }
    
    /** Save the pin and the main image in two separate files. */
    private void handleSavePinAndImage()
    {
        JCheckBox box = view.selection.paintingOnOff;
        if (box.isSelected()) {
            int i = view.selection.colors.getSelectedIndex();
            image = control.getDisplayImageWithPinArea(true, colorSelection[i]);
        } else image = control.getDisplayImage();
        saveMessage = BOTH_MSG;
        lensImage = control.getPinImage();
        preview.setImages(image, lensImage);
        UIUtilities.centerAndShow(preview);
    }
    
    /** Save the pin image on top of the main one. */
    private void handleSavePinOnImage()
    {
        image = control.getPinOnImage();
        saveMessage = BOTH_MSG;
        lensImage = null;
        preview.setImage(image);
        UIUtilities.centerAndShow(preview);
    }
    
    /** 
     * Save the pin image and the main one in the same image. The pin
     * image is painted in the top-left corner.
     */
    private void handleSavePinTopLeft()
    {
        JCheckBox box = view.selection.paintingOnOff;
        Color c = null;
        if (box.isSelected()) {
            int i = view.selection.colors.getSelectedIndex();
            c = colorSelection[i];
        } 
        image = control.getPinOnSideTopLeft(box.isSelected(), c);   
        lensImage = null;
        saveMessage = BOTH_MSG;
        preview.setImage(image);
        UIUtilities.centerAndShow(preview);
    }
    
    /** 
     * Save the pin image and the main one in the same image. The pin
     * image is painted in the top-right corner.
     */
    private void handleSavePinTopRight()
    {
        JCheckBox box = view.selection.paintingOnOff;
        Color c = null;
        if (box.isSelected()) {
            int i = view.selection.colors.getSelectedIndex();
            c = colorSelection[i];
        } 
        image = control.getPinOnSideTopRight(box.isSelected(), c);   
        lensImage = null;
        saveMessage = BOTH_MSG;
        preview.setImage(image);
        UIUtilities.centerAndShow(preview);
    }
    
    /** 
     * Save the pin image and the main one in the same image. The pin
     * image is painted in the bottom-left corner.
     */
    private void handleSavePinBottomLeft()
    {
        JCheckBox box = view.selection.paintingOnOff;
        Color c = null;
        if (box.isSelected()) {
            int i = view.selection.colors.getSelectedIndex();
            c = colorSelection[i];
        } 
        image = control.getPinOnSideBottomLeft(box.isSelected(), c);   
        lensImage = null;
        saveMessage = BOTH_MSG;
        preview.setImage(image);
        UIUtilities.centerAndShow(preview);
    }
    
    /** 
     * Save the pin image and the main one in the same image. The pin
     * image is painted in the bottom-right corner.
     */
    private void handleSavePinBottomRight()
    {
        JCheckBox box = view.selection.paintingOnOff;
        Color c = null;
        if (box.isSelected()) {
            int i = view.selection.colors.getSelectedIndex();
            c = colorSelection[i];
        } 
        image = control.getPinOnSideBottomRight(box.isSelected(), c);   
        lensImage = null;
        saveMessage = BOTH_MSG;
        preview.setImage(image);
        UIUtilities.centerAndShow(preview);
    }
    
    /** Write the bufferedImage. */
    private void writeImage(BufferedImage img, String name)
    {
        UserNotifier un = control.getRegistry().getUserNotifier();
        name +="."+format; //Add extension
        File f = new File(name);
        try {
            if (encoder == null) 
                WriterImage.saveImage(f, img, format);
            else  
                WriterImage.saveImage(f, encoder, img);
            un.notifyInfo("Image saved", message);
        } catch (Exception e) {
            f.delete();
            un.notifyError("Save image failure", "Unable to save the image", e);
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
    
    private void setDefault()
    {
        image = null;
        lensImage = null;
        format = null;
        fileName = null;
    }
    
}

