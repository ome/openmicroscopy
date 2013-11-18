/*
 * org.openmicroscopy.shoola.agents.metadata.util.UploadPictureDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.GIFFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.GenericFileChooser;

/** 
 * Dialog to select the picture to upload.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class UploadPictureDialog
	extends JDialog
	implements ActionListener, DocumentListener
{

    /** Bound property indicating to upload the photo. */
    public static final String UPLOAD_PHOTO_PROPERTY = "uploadPhoto";

    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension H_SPACER_SIZE = new Dimension(3, 10);

    /** The maximum size of the image. */
    private static final int MAX_SIZE_BYTES = 204800;

    /** The maximum size of the image. */
    private static final String MAX_SIZE_STRING = UIUtilities.formatFileSize(MAX_SIZE_BYTES);

    /** The title of the dialog. */
    private static final String TITLE = "Upload Photo";

    /** The text of the dialog. */
    private static final String TEXT = "Select the photo (JPEG, GIF or PNG) " +
            "to upload. Maximum size " + MAX_SIZE_STRING + ".";

    /** Action ID indicating to upload the script to the server. */
    private static final int SAVE = 0;

    /** Action ID indicating to close and disposes of the dialog. */
    private static final int CANCEL = 1;

    /** Collection of supported filters. */
    private static final List<CustomizedFileFilter> FILTERS;

    static {
        FILTERS = new ArrayList<CustomizedFileFilter>();
        FILTERS.add(new GIFFilter());
        FILTERS.add(new JPEGFilter());
        FILTERS.add(new PNGFilter());
    }

    /** Chooser used to select the file. */
    private GenericFileChooser chooser;

    /** 
     * Replaces the <code>ApproveButton</code> provided by the
     * {@link JFileChooser} class.
     */
    private JButton saveButton;

    /** 
     * Replaces the <code>CancelButton</code> provided by the
     * {@link JFileChooser} class.
     */
    private JButton cancelButton;

    /** The text area where to enter the name of the file to save. */
    private JTextField scriptArea;

    /** Sets the properties of the dialog. */
    private void setProperties()
    {
        setTitle(TITLE);
        setModal(true);
    }

    /** Initializes the components. */
    private void initComponents()
    {
        chooser = new GenericFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        Iterator<CustomizedFileFilter> i = FILTERS.iterator();
        while (i.hasNext()) {
            chooser.addChoosableFileFilter(i.next());
        }
        chooser.setControlButtonsAreShown(false);
        saveButton = new JButton("Upload");
        saveButton.setToolTipText(
                UIUtilities.formatToolTipText("Upload the selected photo " +
                        "to the server."));
        saveButton.addActionListener(this);
        saveButton.setActionCommand(""+SAVE);
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText(
                UIUtilities.formatToolTipText("Closes the dialog."));
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(""+CANCEL);
        scriptArea = (JTextField) UIUtilities.findComponent(chooser,
                JTextField.class);
        if (scriptArea != null) {
            scriptArea.getDocument().addDocumentListener(this);
            saveButton.setEnabled(false);
        }
        JLabel label = (JLabel) UIUtilities.findComponent(chooser, JLabel.class);
        if (label != null)
            label.setText("Photo to Upload:");
    }

    /** Closes and disposes. */
    private void close()
    {
        setVisible(false);
        dispose();
    }

    /**
     * Sets the <code>enabled</code> flag of not the <code>Save</code> option 
     * depending on the length of the text entered in the {@link #scriptArea}.
     */
    private void handleTextUpdate()
    {
        if (scriptArea == null) return; //should happen
        String text = scriptArea.getText();
        boolean b = false;
        if (text != null && text.trim().length() > 0) {
            b = true;
            Iterator<CustomizedFileFilter> i = FILTERS.iterator();
            boolean supported = false;
            CustomizedFileFilter filter;
            while (i.hasNext()) {
                filter = i.next();
                if (filter.accept(text)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                saveButton.setEnabled(false);
                return;
            }
        }
        saveButton.setEnabled(b);
    }

    /** Uploads the photo. */
    private void upload()
    {
        UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
        File f;
        if (scriptArea != null)
            f = new File(chooser.getCurrentDirectory().toString(),
                    scriptArea.getText());
        else f = chooser.getSelectedFile();
        if (f == null) {
            un.notifyInfo(TITLE, "Please select a photo.");
            return;
        }
        Iterator<CustomizedFileFilter> i = FILTERS.iterator();
        boolean supported = false;
        CustomizedFileFilter filter;
        String format = "";
        while (i.hasNext()) {
            filter = i.next();
            if (filter.accept(f)) {
                format = filter.getMIMEType();
                supported = true;
                break;
            }
        }
        if (!supported) {
            un.notifyInfo(TITLE, "Only JPEG, GIF or PNG files " +
                    "can be uploaded.");
            return;
        }
        if (f.length() > MAX_SIZE_BYTES) {
            un.notifyInfo(TITLE, "The file is too big, maximum size " + MAX_SIZE_STRING);
            return;
        }
        List<Object> l = new ArrayList<Object>();
        l.add(f);
        l.add(format);
        firePropertyChange(UPLOAD_PHOTO_PROPERTY, null, l);
        setVisible(false);
        dispose();
    }

    /**
     * Builds the tool bar.
     * 
     * @return See above
     */
    private JPanel buildToolbar()
    {
        JPanel bar = new JPanel();
        bar.setBorder(null);
        bar.add(cancelButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(saveButton);
        JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
    }

    /**
     * Builds and lays out the controls.
     * 
     * @return See above.
     */
    private JPanel buildControls()
    {	
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(chooser, BorderLayout.CENTER);
        p.add(buildToolbar(), BorderLayout.SOUTH);
        return p;
    }

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TITLE, TEXT,
                icons.getIcon(IconManager.USER_PHOTO_48));
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(buildControls(), BorderLayout.CENTER);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations =
                    UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations)
                getRootPane().setWindowDecorationStyle(
                        JRootPane.FILE_CHOOSER_DIALOG);
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param owner The owner of the dialog.
     */
    public UploadPictureDialog(JFrame owner)
    {
        super(owner);
        setProperties();
        initComponents();
        buildGUI();
    }

    /**
     * Uploads the script or closes the dialog.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        switch (index) {
        case CANCEL:
            close();
            break;
        case SAVE:
            upload();
        }
    }

    /**
     * Enables or not the <code>Save</code> option depending on the text
     * entered in the {@link #scriptArea}.
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) { handleTextUpdate(); }

    /**
     * Enables or not the <code>Save</code> option depending on the text
     * entered in the {@link #scriptArea}.
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) { handleTextUpdate(); }

    /**
     * Required by the {@link DocumentListener} I/F but no-operation
     * implementation in our case.
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {}

}
