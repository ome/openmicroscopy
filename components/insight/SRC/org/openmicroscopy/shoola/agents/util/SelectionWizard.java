/*
 * org.openmicroscopy.shoola.agents.util.SelectionWizard 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.TagAnnotationData;

/**
 * A modal dialog to select collection of objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class SelectionWizard
    extends JDialog
    implements ActionListener, DocumentListener, FocusListener,
    PropertyChangeListener
{

    /** Bound property indicating the selected items. */
    public static final String SELECTED_ITEMS_PROPERTY = "selectedItems";

    /** Bound property indicating to cancel the selection. */
    public static final String CANCEL_SELECTION_PROPERTY = "cancelSelection";

    /** The default text for the tag.*/
    private static final String DEFAULT_TEXT = "Tag";

    /** The default text for the tag's description.*/
    private static final String DEFAULT_DESCRIPTION = "Description";

    /** Action command ID to Accept the current field selection. */
    private static final int ACCEPT = 0;

    /** Action command ID to cancel the wizard. */
    private static final int CANCEL = 1;

    /** Action command ID to reset the current field selection. */
    private static final int RESET = 2;

    /** Action command ID to add new object to the selection. */
    private static final int ADD_NEW = 3;

    /** The default size. */
    private static final Dimension DEFAULT_SIZE = new Dimension(700, 700);

    /** The button to accept the current selection. */
    private JButton acceptButton;

    /** The button to reset the current selection. */
    private JButton resetButton;

    /** The button to cancel the current selection. */
    private JButton cancelButton;

    /** The type to handle. */
    private Class<?> type;

    /** Button to add new tag to the selection. */
    private JButton addNewButton;

    /** The component used to add a new object. */
    private JTextField addField;

    /** The component used to add a new description to the object. */
    private JTextField descriptionField;
    
    /** The component displaying the selection. */
    private SelectionWizardUI uiDelegate;

    /** The original color of a text field.*/
    private Color originalColor;

    /** The label displaying the message indicating what will be added.*/
    private JLabel addLabel;

    /** Sets the controls.*/
    private void setControls()
    {
        String text = addField.getText();
        addNewButton.setEnabled(CommonsLangUtils.isNotBlank(text) &&
                !DEFAULT_TEXT.equals(text));
    }

    /**
     * Sets the default text for the specified field.
     *
     * @param field The field to handle.
     * @param text The text to display.
     */
    private void setTextFieldDefault(JTextField field, String text)
    {
        field.getDocument().removeDocumentListener(this);
        if (text == null) {
            field.setText("");
            field.setForeground(originalColor);
        } else {
            field.setText(text);
            field.setForeground(Color.LIGHT_GRAY);
        }
        field.getDocument().addDocumentListener(this);
        setControls();
    }
    
    /**
     * Creates the filtering controls.
     *
     * @return See above.
     */
    private JPanel createFilteringControl()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel("Filter by"));
        String txt = null;
        if (TagAnnotationData.class.equals(type)) {
            txt = "tag";
        } else if (TagAnnotationData.class.equals(type)) {
            txt = "attachment";
        }
        String[] values = new String[2];
        StringBuilder builder = new StringBuilder();
        builder.append("start of ");
        if (txt != null) {
            builder.append(txt);
            builder.append(" ");
        }
        builder.append("name");
        values[0] = builder.toString();

        builder = new StringBuilder();
        builder.append("anywhere in ");
        if (txt != null) {
            builder.append(txt);
            builder.append(" ");
        }
        builder.append("name");
        values[1] = builder.toString();
        JComboBox box = new JComboBox(values);
        int selected = 0;
        if (uiDelegate.isFilterAnywhere()) selected = 1;
        box.setSelectedIndex(selected);
        box.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox src = (JComboBox) e.getSource();
                uiDelegate.setFilterAnywhere(src.getSelectedIndex() == 1);
            }
        });
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        p.add(box);
        rows.add(p);
        if (!ExperimenterData.class.equals(type)) {
          //Filter by owner
            p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel("Filter by owner"));
            values = new String[3];
            values[SelectionWizardUI.ALL] = "All";
            values[SelectionWizardUI.CURRENT] = "Owned by me";
            values[SelectionWizardUI.OTHERS] = "Owned by others";
            box = new JComboBox(values);
            box.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox src = (JComboBox) e.getSource();
                    uiDelegate.setOwnerIndex(src.getSelectedIndex());
                }
            });
            p.add(box);
            rows.add(p);
        }
        return UIUtilities.buildComponentPanel(rows);
    }

    /** 
     * Initializes the components composing the display.
     * 
     * @param userID The id of the user currently logged in.
     */
    private void initComponents()
    {
        addLabel = UIUtilities.setTextFont("");
        acceptButton = new JButton("Save");
        acceptButton.setToolTipText("Save the selection.");
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel the selection.");
        resetButton = new JButton("Reset");
        resetButton.setToolTipText("Reset the selection.");

        addNewButton = new JButton("Add");
        addNewButton.setEnabled(false);
        addNewButton.setToolTipText("Add to the selection.");
        addNewButton.setActionCommand(""+ADD_NEW);
        addNewButton.addActionListener(this);

        acceptButton.setActionCommand(""+ACCEPT);
        acceptButton.addActionListener(this);
        acceptButton.setEnabled(false);
        resetButton.setEnabled(false);
        cancelButton.setActionCommand(""+CANCEL);
        cancelButton.addActionListener(this);
        resetButton.setActionCommand(""+RESET);
        resetButton.addActionListener(this);


        //Field creation
        addField = new JTextField(10);
        addField.setToolTipText("Tag Name");
        originalColor = addField.getForeground();
        setTextFieldDefault(addField, DEFAULT_TEXT);
        addField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    if (addField.isFocusOwner()) {
                        addNewObjects();
                    }
                }
            }
        });
        descriptionField = new JTextField(15);
        descriptionField.setToolTipText("Tag Description");
        setTextFieldDefault(descriptionField, DEFAULT_DESCRIPTION);
        addField.getDocument().addDocumentListener(this);
        addField.addFocusListener(this);
        descriptionField.addFocusListener(this);
        descriptionField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    if (descriptionField.isFocusOwner()) {
                        addNewObjects();
                    }
                }
            }
        });
    }

    /** Closes and disposes. */
    private void close()
    {
        setVisible(false);
        dispose();
    }

    /** Closes and disposes. */
    private void cancel()
    {
        close();
        firePropertyChange(CANCEL_SELECTION_PROPERTY, Boolean.valueOf(false),
                Boolean.valueOf(true));
    }

    /** Fires a property change with the selected items. */
    private void accept()
    {
        Map<Class<?>, Collection<Object>>
        r = new HashMap<Class<?>, Collection<Object>>();
        Collection<Object> l = uiDelegate.getSelection();
        l.addAll(uiDelegate.getImmutableElements());
        r.put(type, l);
        firePropertyChange(SELECTED_ITEMS_PROPERTY, null, r);
        close();
    }

    /** 
     * Builds and lays out the UI.
     * 
     * @param addCreation Pass <code>true</code> to add a component
     *                    allowing creation of object of the passed type,
     *                    <code>false</code> otherwise.
     */
    private void buildUI(boolean addCreation)
    {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(uiDelegate);
        container.add(createFilteringControl());
        if (addCreation && TagAnnotationData.class.equals(type)) {
            container.add(createAdditionPane());
        }
        c.add(container, BorderLayout.CENTER);
        c.add(createControlsPane(), BorderLayout.SOUTH);
    }

    /**
     * Builds and lays out the components hosting the controls.
     * 
     * @return See above.
     */
    private JPanel createControlsPane()
    {
        JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(acceptButton);
        controlPanel.add(cancelButton);
        controlPanel.add(resetButton);
        return UIUtilities.buildComponentPanelRight(controlPanel);
    }

    /**
     * Modifies the text of the component displaying the <code>Add</code>
     * component.
     */
    private void formatAddLabelText()
    {
        String s = "";
        Collection<DataObject> list = uiDelegate.getAvailableSelectedNodes();
        if (CollectionUtils.isNotEmpty(list)) {
            DataObject data = list.iterator().next();
            if (data instanceof TagAnnotationData) {
                s = String.format(" in %s Tag set",
                        ((TagAnnotationData) data).getTagValue());
            }
        }
        String tip = String.format(
                "Add a new tag%s and select it immediately:", s);
        addLabel.setText(tip);
    }

    /**
     * Builds and lays out the component to add new objects to the selection.
     * 
     * @return See above.
     */
    private JPanel createAdditionPane()
    {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (TagAnnotationData.class.equals(type)) {
            formatAddLabelText();
            p.add(UIUtilities.buildComponentPanel(addLabel));
            JPanel pane = new JPanel();
            pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
            pane.add(addField);
            pane.add(Box.createHorizontalStrut(5));
            pane.add(descriptionField);
            pane.add(addNewButton);
            p.add(pane);
        }
        return UIUtilities.buildComponentPanel(p);
    }

    /** Adds new objects to the selection list. */
    private void addNewObjects()
    {
        if (TagAnnotationData.class.equals(type)) {
            String text = addField.getText();
            if (CommonsLangUtils.isEmpty(text)) return;
            String[] names = text.split(SearchUtil.COMMA_SEPARATOR);
            List<DataObject> objects = new ArrayList<DataObject>();
            String v;
            String description = descriptionField.getText();
            if (DEFAULT_DESCRIPTION.equals(description)) {
                description = null;
            }
            Set<DataObject> parents = uiDelegate.getAvailableSelectedNodes();
            for (int i = 0; i < names.length; i++) {
                v = names[i];
                TagAnnotationData tag;
                if (CommonsLangUtils.isNotBlank(v)) {
                    tag = new TagAnnotationData(v.trim());
                    if (description != null) {
                        tag.setTagDescription(description);
                    }
                    if (CollectionUtils.isNotEmpty(parents)) {
                        tag.setDataObjects(parents);
                    }
                    objects.add(tag);
                }
            }
            boolean reset = uiDelegate.addObjects(objects);
            if (reset) {
                addField.setCaretPosition(0);
                setTextFieldDefault(addField, DEFAULT_TEXT);
                descriptionField.setCaretPosition(0);
                setTextFieldDefault(descriptionField, DEFAULT_DESCRIPTION);
                acceptButton.requestFocus();
            }
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param available The collection of available tags.
     * @param type The type of object to handle.
     * @param user The current user.
     */
    public SelectionWizard(JFrame owner, Collection<Object> available,
            Class<?> type, ExperimenterData user)
    {
        this(owner, available, null, type, user);
    }

    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param available The collection of available tags.
     * @param type The type of object to handle.
     * @param addCreation Pass <code>true</code> to add a component
     *                    allowing creation of object of the passed type,
     *                    <code>false</code> otherwise.
     * @param user The current user.
     */
    public SelectionWizard(JFrame owner, Collection<Object> available,
            Class<?> type, boolean addCreation, ExperimenterData user)
    {
        this(owner, available, null, type, addCreation, user);
    }

    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param available The collection of available items.
     * @param selected The collection of selected items.
     * @param type The type of object to handle.
     * @param user The current user.
     */
    public SelectionWizard(JFrame owner, Collection<Object> available,
            Collection<Object> selected, Class<?> type,
            ExperimenterData user)
    {
        this(owner, available, selected, type, false, user);
    }

    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param available The collection of available items.
     * @param selected The collection of selected items.
     * @param type The type of object to handle.
     * @param addCreation Pass <code>true</code> to add a component
     *                    allowing creation of object of the passed type,
     * 						<code>false</code> otherwise.
     * @param user The the current user.
     */
    public SelectionWizard(JFrame owner, Collection<Object> available,
            Collection<Object> selected, Class<?> type,
            boolean addCreation, ExperimenterData user)
    {
        super(owner);
        setModal(true);
        uiDelegate = new SelectionWizardUI(this, available, selected, type, user);
        uiDelegate.addPropertyChangeListener(this);
        this.type = type;
        initComponents();
        buildUI(addCreation);
        setSize(DEFAULT_SIZE);
    }

    /**
     * Sets the collection of nodes that cannot be removed.
     * 
     * @param immutable The collection to set.
     */
    public void setImmutableElements(Collection immutable)
    {
        uiDelegate.setImmutableElements(immutable);
    }

    /**
     * Sets the title, the text and the icon displayed in the header.
     * 
     * @param title The title to set.
     * @param text The text to set.
     */
    public void setTitle(String title, String text)
    {
        setTitle(title, text, null);
    }

    /**
     * Sets the title, the text and the icon displayed in the header.
     * 
     * @param title The title to set.
     * @param text The text to set.
     * @param titleIcon The icon to set.
     */
    public void setTitle(String title, String text, Icon titleIcon)
    {
        setTitle(title);
        if (titleIcon == null) {
            IconManager icons = IconManager.getInstance();
            titleIcon = icons.getIcon(IconManager.WIZARD_48);
        }
        TitlePanel titlePanel = new TitlePanel(title, text, titleIcon);
        getContentPane().add(titlePanel, BorderLayout.NORTH);
    }

    /**
     * Sets the groups.
     * 
     * @param groups The groups to set.
     */
    public void setGroups(Collection<GroupData> groups)
    {
        uiDelegate.setGroups(groups);
    }

    /**
     * Reacts to event fired by the various controls.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent evt)
    {
        int id = Integer.parseInt(evt.getActionCommand());
        switch (id) {
        case ACCEPT:
            accept();
            break;
        case CANCEL:
            cancel();
            break;
        case RESET:
            uiDelegate.reset();
            break;
        case ADD_NEW:
            addNewObjects();
        }
    }

    /**
     * Sets the text of the {@link #acceptButton}.
     * 
     * @param text The value to set.
     */
    public void setAcceptButtonText(String text)
    {
        if (acceptButton != null) acceptButton.setText(text);
    }

    /**
     * Sets the enabled flag of the {@link #addNewButton}.
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) { setControls(); }

    /**
     * Sets the enabled flag of the {@link #addNewButton}.
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) { setControls(); }

    /**
     * Sets the controls.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (SelectionWizardUI.SELECTION_CHANGE.equals(name)) {
            Boolean b = (Boolean) evt.getNewValue();
            acceptButton.setEnabled(b.booleanValue());
            resetButton.setEnabled(b.booleanValue());
            acceptButton.requestFocus();
        } else if (SelectionWizardUI.AVAILABLE_SELECTION_CHANGE.equals(name)) {
            formatAddLabelText();
        }
    }

    /**
     * Resets the values when losing the focus
     * @see FocusListener#focusLost(FocusEvent)
     */
    public void focusLost(FocusEvent evt) {
        Object src = evt.getSource();
        if (src == addField) {
            String value = addField.getText();
            if (CommonsLangUtils.isBlank(value)) {
                setTextFieldDefault(addField, DEFAULT_TEXT);
            }
        } else if (src == descriptionField) {
            String value = descriptionField.getText();
            if (CommonsLangUtils.isBlank(value)) {
                setTextFieldDefault(descriptionField, DEFAULT_DESCRIPTION);
            }
        }
    }

    /**
     * Resets the values when losing the focus
     * @see FocusListener#focusGained(FocusEvent)
     */
    public void focusGained(FocusEvent evt)
    {
        Object src = evt.getSource();
        if (src == addField) {
            String value = addField.getText();
            if (DEFAULT_TEXT.equals(value)) {
                addField.setCaretPosition(0);
                setTextFieldDefault(addField, null);
            }
        } else if (src == descriptionField) {
            String value = descriptionField.getText();
            if (DEFAULT_DESCRIPTION.equals(value)) {
                descriptionField.setCaretPosition(0);
                setTextFieldDefault(descriptionField, null);
            }
        }
    }

    /**
     * Required by the {@link DocumentListener} I/F but no-op implementation 
     * in our case.
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {}

}
