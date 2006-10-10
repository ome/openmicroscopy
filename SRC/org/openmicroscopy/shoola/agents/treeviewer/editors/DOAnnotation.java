/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.DOAnnotation
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.ExperimenterData;

/** 
 * The component hosting the annotations.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class DOAnnotation
    extends JPanel
{
    
    /** The label of the {@link #deleteBox}. */
    private static final String         DELETE_ANNOTATION = "Delete " +
                                                        "the annotation";
    
    /** Area where to annotate the <code>DataObject</code>. */
    private JTextArea           annotationArea;
    
    /** Hosts a list of users who annotated the selected object. */
    private JList               annotatedByList;
    
    /** Box to delete the annotation. */
    private JCheckBox           deleteBox;
    
    /** The model keeping track of the users who annotated a data object. */
    private DefaultListModel    listModel;
    
    /** Maps of users who annotated the data object. */
    private Map                 ownersMap;
    
    /** The index of the current user.*/
    private int                 userIndex;
    
    /** A {@link DocumentListener} for the {@link #annotationArea}. */
    private DocumentListener    annotationAreaListener;
    
    /** Reference to the View. */
    private EditorUI            view;
    
    /** Reference to the Model. */
    private EditorModel         model;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        annotationArea = new MultilineLabel();
        annotationArea.setBackground(Color.WHITE);
        annotationArea.setBorder(new TitledBorder("Annotation"));
        deleteBox = new JCheckBox(DELETE_ANNOTATION);
        listModel = new DefaultListModel();
        annotatedByList = new JList(listModel);
        annotatedByList.setBorder(new TitledBorder("Annotated by"));
        annotatedByList.setSelectionMode(
                ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        annotatedByList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    showSingleAnnotation();
                 }
            }
        });
        annotationAreaListener = new DocumentListener() {
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
                model.setAnnotated(true);
            }
            
            /** 
             * Indicates that the object is annotated. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
                model.setAnnotated(true);
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        annotationArea.getDocument().addDocumentListener(
                            annotationAreaListener);
        setComponentsEnabled(false);
        deleteBox.setSelected(false);
    }
    
    /**
     * Builds a panel hosting the {@link #annotationArea} and the list of users
     * who annotated the data object.
     * 
     * @return See above.
     */
    private JPanel buildAnnotationPanel()
    {
        //Set panel layout and border
        JPanel p = new JPanel();
        p.setBorder(new EtchedBorder());
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.insets = new Insets(3, 3, 3, 3);
        // add annotation area
        c.weightx = 0.4;
        p.add(new JScrollPane(annotationArea), c);
        // add annotated by list
        c.gridx = 1;
        c.weightx = 0.2;
        p.add(new JScrollPane(annotatedByList), c);
               
        return p;
    }

    /**
     * Sets the specified text to the {@link #annotationArea}.
     * 
     * @param text  The text to set.
     */
    private void addAnnotationText(String text)
    {
        annotationArea.getDocument().removeDocumentListener(
                                        annotationAreaListener);
        annotationArea.setText(text);
        annotationArea.getDocument().addDocumentListener(
                                        annotationAreaListener);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(UIUtilities.buildComponentPanel(deleteBox));
        add(new JSeparator());
        add(Box.createRigidArea(EditorUI.SMALL_V_SPACER_SIZE));
        add(buildAnnotationPanel());
        add(Box.createVerticalGlue());
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
        Long ownerID = (Long) ownersMap.get(new Integer(index));
        if (ownerID == null) return new ArrayList();    //empty list
        return (List) annotations.get(ownerID);
    }
    
    /** 
     * Sets the UI components enabled.
     * 
     * @param b The enabled flag. 
     */
    private void setComponentsEnabled(boolean b)
    {
        deleteBox.setEnabled(b);
        annotationArea.setEditable(b);
        if (b) {
            annotationArea.requestFocus();
            annotationArea.selectAll();
        }
    }
    
    /** Shows a single annotation. */
    private void showSingleAnnotation()
    {
        int index = annotatedByList.getSelectedIndex();
        view.handleAnnotationAreaInsert();
        if (index == -1) {
            ExperimenterData details = model.getUserDetails();
            addAnnotationText("No annotations for "+details.getFirstName()+" "+
                                details.getLastName());
            setComponentsEnabled(true);
            deleteBox.setEnabled(false);
            return;
        }
        List list = getOwnerAnnotation(index);
        if (list.size() > 0) {
            AnnotationData data = (AnnotationData) list.get(0);
            addAnnotationText(data.getText());  
        }
        setComponentsEnabled(index == userIndex);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view      Reference to the View. Mustn't be <code>null</code>.
     * @param model     Reference to the Model. Mustn't be <code>null</code>.
     */
    DOAnnotation(EditorUI view, EditorModel model)
    {
        if (view == null)  throw new IllegalArgumentException("No View.");
        if (model == null)  throw new IllegalArgumentException("No Model.");
        this.view = view;
        this.model = model;
        userIndex = -1;
        initComponents();
        buildGUI();
    }
    
    /** Shows the annotations. */
    void showAnnotations()
    {
        ExperimenterData userDetails = model.getUserDetails();
        if (userDetails == null) return;
        Map annotations = model.getAnnotations();
        if (annotations == null) return;
        String[] owners = new String[annotations.size()];
        Iterator i = annotations.keySet().iterator();
        Long id;
        int index = 0;
        ownersMap = new HashMap();
        List list;
        ExperimenterData data;
        while (i.hasNext()) {
            id = (Long) i.next();
            list = (List) annotations.get(id);
            if (list != null || list.size() > 0) {
                data = ((AnnotationData) list.get(0)).getOwner();
                if (userDetails.getId() == id.intValue()) userIndex = index;
                owners[index] = data.getLastName();
                ownersMap.put(new Integer(index), id);
                index++;
            } 
        }
        //No annotation for the current user, so allow creation.
        if (userIndex != -1) annotatedByList.setSelectedIndex(userIndex);
        setComponentsEnabled(true);
        formatUsersList(owners);
        annotatedByList.clearSelection();
        annotatedByList.setSelectedIndex(userIndex);
        showSingleAnnotation();
    }
    
    /**
     * Returns <code>true</code> if the data object is annotated,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotable()
    { 
        if (userIndex == -1) return true;//no annotation for current user
        return (annotatedByList.getSelectedIndex() == userIndex); 
    }
    
    /**
     * Returns <code>true</code> if the data object has to be deleted,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotationDeleted() {  return deleteBox.isSelected(); }
    
    /** 
     * Returns the text of the annotation. 
     * 
     * @return See above. 
     */
    String getAnnotationText() { return annotationArea.getText(); }
    
}
