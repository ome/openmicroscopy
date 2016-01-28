/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TextFieldLimit;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DatasetData;
import omero.gateway.model.PlateData;

/**
 * Edits the channels.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
class ChannelEditUI
    extends JPanel
    implements ActionListener, DocumentListener
{

    /** Bound property indicating to apply the renaming to all the images.*/
    static final String APPLY_TO_ALL_PROPERTY = "ApplyToAll";

    /** Bound property indicating to cancel the renaming.*/
    static final String CANCEL_PROPERTY = "Cancel";

    /** Bound property indicating to save the renaming for the image.*/
    static final String SAVE_PROPERTY = "Save";

    /** 
     * Message displayed before apply the renaming to all images in the dataset.
     */
    private static final String WARNING_DATASET =
            "Update channel names for all images in the dataset? " +
                    "This cannot be undone.";

    /** 
     * Message displayed before apply the renaming to all images in the plate.
     */
    private static final String WARNING_PLATE = 
            "Update channel names for all images in the plate? " +
                    "This cannot be undone.";

    /** Action id indicating to save the changes if any.*/
    private static final int SAVE = 0;

    /** Action id indicating to cancel the saving.*/
    private static final int CANCEL = 1;

    /** Action id indicating to apply the changes to all.*/
    private static final int APPLY_TO_ALL = 2;

    /** The text of the {@link #applyToAll}.*/
    private static final String APPLY_TO_ALL_TEXT = "Apply to All";

    /** The text of the {@link #save}.*/
    private static final String SAVE_TEXT = "Save";

    /** The text of the {@link #save}.*/
    private static final String CONTINUE_TEXT = "Continue";

    /** The text of the {@link #cancelButton}.*/
    private static final String CANCEL_TEXT = "Cancel";

    /** The tool tip text of the {@link #applyToAll}.*/
    private static final String APPLY_TO_ALL_TIP =
            "Save and apply to all images.";

    /** The tool tip text of the {@link #save}.*/
    private static final String SAVE_TIP = "Save Channel Names.";

    /** The tool tip text of the {@link #cancelButton}.*/
    private static final String CANCEL_TIP = "Cancel.";

    /** Map hosting the fields used to edit the corresponding channel.*/
    private Map<JTextField, ChannelData> fields;

    /** Save the changes if any.*/
    private JButton saveButton;

    /** Cancel the saving.*/
    private JButton cancelButton;

    /** Apply the changes to all the images.*/
    private JButton applyToAll;

    /** The data object hosting all the images to update.*/
    private Object parent;

    /** Component used to display a warning before saving.*/
    private JTextArea messageLabel;

    /** The channels to display.*/
    private Map channels;

    /** Initializes the components composing the display.*/
    private void initComponents()
    {
        fields = new LinkedHashMap<JTextField, ChannelData>(channels.size());
        ChannelData channel;
        JTextField field;

        Iterator k = channels.keySet().iterator();
        while (k.hasNext()) {
            channel = (ChannelData) k.next();
            field = new TextFieldLimit(EditorUtil.MAX_CHAR-1);
            field.setBackground(UIUtilities.BACKGROUND_COLOR);
            field.setText(channel.getChannelLabeling());
            field.getDocument().addDocumentListener(this);
            fields.put(field, channel);
        }
        saveButton = new JButton(SAVE_TEXT);
        saveButton.setToolTipText(SAVE_TIP);
        saveButton.addActionListener(this);
        saveButton.setActionCommand(""+SAVE);
        saveButton.setEnabled(false);

        cancelButton = new JButton(CANCEL_TEXT);
        cancelButton.setToolTipText(CANCEL_TIP);
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(""+CANCEL);

        applyToAll = new JButton(APPLY_TO_ALL_TEXT);
        applyToAll.setToolTipText(APPLY_TO_ALL_TIP);
        applyToAll.addActionListener(this);
        applyToAll.setActionCommand(""+APPLY_TO_ALL);

        Border border = new CompoundBorder(
                BorderFactory.createLineBorder(Color.gray),
                new EmptyBorder(2, 2, 2, 2));

        saveButton.setOpaque(true);
        saveButton.setBackground(UIUtilities.LIGHT_GREY);
        saveButton.setBorder(border);
        cancelButton.setOpaque(true);
        cancelButton.setBackground(UIUtilities.LIGHT_GREY);
        cancelButton.setBorder(border);

        applyToAll.setOpaque(true);
        applyToAll.setBackground(UIUtilities.LIGHT_GREY);
        applyToAll.setBorder(border);

        messageLabel = new MultilineLabel();
        messageLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        saveButton.setVisible(!(parent instanceof PlateData));
    }

    /** Builds and lays out the UI.*/
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIUtilities.BACKGROUND_COLOR);
        Iterator<Entry<JTextField, ChannelData>> 
        i = fields.entrySet().iterator();
        while (i.hasNext()) {
            add(i.next().getKey());
        }
        add(messageLabel);
        add(buildControls());
    }

    /**
     * Builds and lays out the controls.
     *
     * @return See above.
     */
    private JPanel buildControls()
    {
        JPanel bar = new JPanel();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(UIUtilities.BACKGROUND_COLOR);
        bar.add(saveButton);
        if (parent instanceof DatasetData || parent instanceof PlateData)
            bar.add(applyToAll);
        bar.add(cancelButton);
        return bar;
    }

    /** Saves the changes.*/
    private void save()
    {
        Entry<JTextField, ChannelData> e;
        Iterator<Entry<JTextField, ChannelData>>
        i = fields.entrySet().iterator();
        List<ChannelData> channels = new ArrayList<ChannelData>(fields.size());
        ChannelData channel;
        while (i.hasNext()) {
            e = i.next();
            channel = e.getValue();
            channel.setName(e.getKey().getText());
            channels.add(channel);
        }
        //Apply the
        if (!applyToAll.isVisible())
            firePropertyChange(APPLY_TO_ALL_PROPERTY, null, channels);
        else firePropertyChange(SAVE_PROPERTY, null, channels);
        resetControls();
    }

    /** Resets the controls.*/
    private void resetControls()
    {
        saveButton.setEnabled(false);
        saveButton.setVisible(!(parent instanceof PlateData));
        saveButton.setText(SAVE_TEXT);
        messageLabel.setText("");
        applyToAll.setVisible(true);
    }

    /**
     * Displays a warning before saving the changes depending on the
     * {@link #parent} object.
     */
    private void applyToAll()
    {
        if (parent instanceof DatasetData)
            messageLabel.setText(WARNING_DATASET);
        else if (parent instanceof PlateData)
            messageLabel.setText(WARNING_PLATE);
        applyToAll.setVisible(false);
        saveButton.setEnabled(true);
        saveButton.setVisible(true);
        saveButton.setText(CONTINUE_TEXT);
        repaint();
    }

    /** Cancel the saving.*/
    private void cancel()
    {
        resetControls();
        //Reset the fields' values.
        Entry<JTextField, ChannelData> e;
        Iterator<Entry<JTextField, ChannelData>>
        i = fields.entrySet().iterator();
        while (i.hasNext()) {
            e = i.next();
            e.getKey().setText(e.getValue().getChannelLabeling());
        }
        firePropertyChange(CANCEL_PROPERTY, Boolean.valueOf(false),
                Boolean.valueOf(true));
    }

    /**
     * Enables or not the {@link #saveButton} depending on the value entered.
     */
    private void handleNameChange()
    {
        Entry<JTextField, ChannelData> e;
        Iterator<Entry<JTextField, ChannelData>> 
        i = fields.entrySet().iterator();
        boolean enabled = false;
        while (i.hasNext()) {
            e = i.next();
            if (!e.getKey().getText().equals(
                    e.getValue().getChannelLabeling())) {
                enabled = true;
                break;
            }
        }
        saveButton.setEnabled(enabled);
    }

    /**
     * Creates a new instance.
     *
     * @param channels The channels to handle.
     * @param parent The data object hosting all the images to update.
     */
    ChannelEditUI(Map channels, Object parent)
    {
        if (channels == null ||  channels.size() == 0)
            throw new IllegalArgumentException("No Channels specified.");
        this.parent = parent;
        this.channels = channels;
        initComponents();
        buildGUI();
    }

    /**
     * Handles event fired by the controls.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent evt)
    {
        int index = Integer.parseInt(evt.getActionCommand());
        switch (index) {
        case SAVE:
            save();
            break;
        case CANCEL:
            cancel();
            break;
        case APPLY_TO_ALL:
            applyToAll();
        }
    }

    /** 
     * Enables or not the {@link #saveButton} depending on the value entered.
     *
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
    public void insertUpdate(DocumentEvent evt) { handleNameChange(); }

    /**
     * Enables or not the {@link #saveButton} depending on the value entered.
     *
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
    public void removeUpdate(DocumentEvent evt) { handleNameChange(); }

    /**
     * Implemented as specified by {@link DocumentListener} I/F but
     * no operation in our case.
     */
    public void changedUpdate(DocumentEvent evt) {}

}
