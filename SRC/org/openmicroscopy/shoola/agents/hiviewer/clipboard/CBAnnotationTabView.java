/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.CBAnnotationTabView
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;



//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.AnnotationEditor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import pojos.AnnotationData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * The <code>Annotation</code> panel
 *
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class CBAnnotationTabView
    extends ClipBoardTab
{

    /** The <code>Create</code> annotation button. */
    JButton                         createAnnotation;
    
    /** The <code>Update</code> annotation button. */
    JButton                         saveAnnotation;
     
    /** The <code>Remove</code> annotation button. */
    JButton                         removeAnnotation;
    
    /** Hosts a list of user who annotated the selected object. */
    JList                           annotatedByList;
    
    /** The model keeping track of the users who annotated a data object. */
    private DefaultListModel        listModel;

    /** The annotation text area. */
    private JTextArea               annotationText;
    
    /** Flag to determine if its a creation or an update of annotation. */
    private boolean                 annotated;
    
    /** Maps of users who annotated the data object. */
    private Map                     ownersMap;
    
    /** The index of the current user.*/
    private int                     userIndex;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        removeAnnotation = new JButton("Delete");
        createAnnotation = new JButton("Annotate selected item");
        saveAnnotation = new JButton("Save");
        annotationText = new JTextArea();
        annotationText.setBorder(new TitledBorder("Annotation"));
        annotationText.setLineWrap(true);
        annotationText.setWrapStyleWord(true);
        listModel = new DefaultListModel();
        annotatedByList = new JList(listModel);
        annotatedByList.setBorder(new TitledBorder("Annotated by"));
        annotatedByList.setSelectionMode(
                ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setComponentsEnabled(false);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI() 
    {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        add(buildButtonPanel(), c); 
        
        // add text boxes
        c.gridx = 1;
        c.weightx = 1;
        add(buildAnnotationPanel(), c);
    }
    
    /**
     * Builds the panel hosting the buttons.
     * 
     * @return See below.
     */
    private JPanel buildButtonPanel()
    {
        // set panel layout and border
        JPanel p = new JPanel();
        p.setBorder(new EtchedBorder());
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        //Create button
        p.add(createAnnotation, c);
        
        //Save button
        c.gridy = 1;
        p.add(saveAnnotation, c);
        
        //Delete button
        c.gridy = 2;
        p.add(removeAnnotation, c);
        
        // filler panel
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        p.add(new JPanel(), c);
        
        return p;
    }
 
    /**
     * Builds the annotation panel.
     * 
     * @return See below.
     */
    private JPanel buildAnnotationPanel()
    {
        // set panel layout and border
        JPanel p = new JPanel();
        p.setBorder(new EtchedBorder());
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        JScrollPane annotationTextScroller = new JScrollPane(annotationText);
        JScrollPane annotatedByListScroller = new JScrollPane(annotatedByList);
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.insets = new Insets(3, 3, 3, 3);
        
        // add annotation text
        c.weightx = 0.4;
        p.add(annotationTextScroller, c);
        
        // add annotated by list
        c.gridx = 1;
        c.weightx = 0.2;
        p.add(annotatedByListScroller, c);
               
        return p;
    }  
    
    /**
     * Displays the users' name in a list box.
     * 
     * @param owners Array of users who annotated the selected item.
     */
    private void formatUsersList(String[] owners)
    {
        // remove all users from list before adding new
        listModel.removeAllElements();
        
        // add each user to list
        Timestamp date;
        DateFormat df = DateFormat.getDateInstance();
        AnnotationData data;
        List list;
        for (int i = 0; i < owners.length; i++) {
            list =  getOwnerAnnotation(i);
            data = ((AnnotationData) list.get(0));
            date = data.getLastModified();
            if (date == null)
                date = new Timestamp((new java.util.Date()).getTime()); 
            listModel.addElement(owners[i]+" ("+df.format(date)+")");   
        }
    }
    
    /**
     * Returns the list of annotations made by the selected user.
     * 
     * @param index The index of the selected user.
     * @return See below.
     */
    private List getOwnerAnnotation(int index)
    { 
        Map annotations = model.getAnnotations();
        Integer ownerID = (Integer) ownersMap.get(new Integer(index));
        if (ownerID == null) return new ArrayList();    //empty list
        return (List) annotations.get(ownerID);
    }
    
    /**
     * Sets the enabled status of the components.
     * 
     * @param b The enabled flag.
     */
    private void setComponentsEnabled(boolean b)
    {
        createAnnotation.setEnabled(b);
        saveAnnotation.setEnabled(b);
        removeAnnotation.setEnabled(b);
        annotationText.setEditable(b);
    }
    
    /**
     * Enables the UI components to allow or not the creation of a new
     * annotation for the data object.
     * 
     * @param b Boolean flag.
     */
    private void allowOwnerAnnotation(boolean b)
    {
        createAnnotation.setEnabled(b);
        if (b) {
            annotationText.requestFocus();
            annotationText.selectAll();
        }
    }

    /**
     * Allows or not to update or delete and existing annotation.
     * 
     * @param b Pass <code>true</code> to allow the action,
     *          <code>false</code> otherwise
     */
    private void allowUpdate(boolean b)
    {
        saveAnnotation.setEnabled(b);
        removeAnnotation.setEnabled(b);
        annotationText.setEditable(b);
        createAnnotation.setEnabled(!b);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model The {@link ClipBoardModel} model.
     * @param view The {@link ClipBoardUI} hosting this sub-component.
     * @param controller The <code>ClipBoardControl</code> control.
     */
    CBAnnotationTabView(ClipBoardModel model, ClipBoardUI view, ClipBoardControl
                    controller)
    {
        super(model, view, controller);
        annotated = false;
        userIndex = -1;
        initComponents();
        new CBAnnotationTabMng(this);
        buildGUI();
    }
    
    /**
     * Shows the annotation entered by the selected user.
     */
    void showSingleAnnotation()
    {
        int index = annotatedByList.getSelectedIndex();
        if (index == -1) {
            ExperimenterData details = model.getParentModel().getUserDetails();
            annotationText.setText("No annotations for "+details.getLastName()); 
            annotated = false;
            return;
        }
        List list = getOwnerAnnotation(index);
        if (list.size() > 0) {
            AnnotationData data = (AnnotationData) list.get(0);
            annotationText.setText(data.getText());
            annotated = true;             
        }
    }
    
    /** 
     * Focus on the current user annotation. 
     * Needs to clear the selection b/c the user might not have annotated 
     * the data object.
     */
    void focusOnOwnerAnnotation()
    {
        annotatedByList.clearSelection();
        annotatedByList.setSelectedIndex(userIndex);
    }

    /**
     * Allows to save the newly created annotation.
     * 
     * @param b Pass <code>true</code> to allow the action,
     *          <code>false</code> otherwise
     */
    void allowCreation(boolean b)
    {
        saveAnnotation.setEnabled(b);
        if (b) {
            annotationText.setEditable(b);
            annotationText.requestFocus();
            annotationText.selectAll();
        }
    }

    /**
     * Allows the current user to edit his/her annotation.
     */
    void allowUserUpdate() 
    {
        int i = annotatedByList.getSelectedIndex();
        saveAnnotation.setEnabled(i==userIndex);
        removeAnnotation.setEnabled(i==userIndex);
        annotationText.setEditable(i==userIndex);
    }
    
    /** Displays the annotations. */
    void showAnnotations()
    {
        ExperimenterData userDetails = model.getParentModel().getUserDetails();
        if (userDetails == null) return;
        Map annotations = model.getAnnotations();
        if (annotations == null) return;
        String[] owners = new String[annotations.size()];
        Iterator i = annotations.keySet().iterator();
        Integer id;
        int index = 0;
        ownersMap = new HashMap();
        List list;
        ExperimenterData data;
        while (i.hasNext()) {
            id = (Integer) i.next();
            list = (List) annotations.get(id);
            data = ((AnnotationData) list.get(0)).getOwner();
            if (userDetails.getId() == id.intValue()) userIndex = index;
            owners[index] = data.getLastName();
            ownersMap.put(new Integer(index), id);
            index++;
        }
        //No annotation for the current user, so allow creation.
        if (userIndex == -1) allowOwnerAnnotation(true);
        else {
            annotatedByList.setSelectedIndex(userIndex);
            allowUpdate(true);
        }
        formatUsersList(owners);
        focusOnOwnerAnnotation();
        showSingleAnnotation();
    }
    
    /** Updates the currently selected annotation. */
    void update()
    {
        List list = getOwnerAnnotation(userIndex);
        AnnotationData data = (AnnotationData) list.get(0);
        data.setText(annotationText.getText().trim());
        controller.updateAnnotation(data);
    }
    
    /** Creates a new annotation for the selected object. */
    void create()
    {
        controller.createAnnotation(annotationText.getText().trim());
    }
    
    /** Deletes the currently selected annotation. */
    void delete()
    {
        List list = getOwnerAnnotation(userIndex);
        AnnotationData data = (AnnotationData) list.get(0);
        controller.deleteAnnotation(data);
    }
    
    /**
     * Returns <code>true</code> if an annotation has already been made
     * by the current user, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotated() { return annotated; }
    
    /**
     * Synchronizes the UI components according to the annotation status.
     */
    void manageAnnotation()
    {
        userIndex = -1;
        switch (model.getAnnotationStatus()) {
            case ClipBoardModel.CREATE:
                allowUpdate(true);
                break;
            case ClipBoardModel.DELETE:
                allowOwnerAnnotation(true);
                allowUpdate(false);
                break;
        }
    }
    
    /**
     * Updates the UI components and retrieves the annotation when a node is
     * selected in the <code>Browser</code>.
     * @see ClipBoardTab#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (model.getPaneIndex() != ClipBoard.ANNOTATION_PANEL) return;
        if (selectedDisplay == null) {
            setComponentsEnabled(false);
            return;
        }
        setComponentsEnabled(false);
        Object hierarchyObject = selectedDisplay.getHierarchyObject();
        if (hierarchyObject == null) return; //root
        userIndex = -1;
        if (hierarchyObject instanceof ImageData) {
            //Make sure that any ongoing annotationLoading is cancelled
            controller.discardAnnotation();
            int id = ((ImageData) hierarchyObject).getId();
            controller.retrieveAnnotations(id,
                    AnnotationEditor.IMAGE_ANNOTATION);
        } else if (hierarchyObject instanceof DatasetData) {
            //Make sure that any ongoing annotationLoading is cancelled
            controller.discardAnnotation();
            int id = ((DatasetData) hierarchyObject).getId();
            controller.retrieveAnnotations(id,
                    AnnotationEditor.DATASET_ANNOTATION);
        }
    }
    
}
