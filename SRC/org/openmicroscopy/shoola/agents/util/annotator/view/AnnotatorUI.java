/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util.annotator.view;


//Java imports
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
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
import org.openmicroscopy.shoola.agents.util.annotator.actions.FinishAction;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.DataObject;


/** 
* Displays the list of annotations if some of the <code>DataObject</code>s
* have already been annotated and a text area to enter the annotation.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
class AnnotatorUI
	extends JPanel
{

	/** The message describing the component. */
	private static final String TITLE = "Enter the textual annotation "+
		"in the area below.";
	
	/** The message describing the component. */
	private static final String MESSAGE = "The list on the right displayed "+
		"the annotated items.";
	
	/** The message describing the component. */
	private static final String BODY = "Click on an item to view the " +
					"annotation.";
	
	/**
	 * A reduced size for the invisible components used to separate widgets
	 * vertically.
	 */
	private static final Dimension SMALL_V_SPACER_SIZE = new Dimension(1, 6);
  
	/** The UI component hosting the list of annotations. */
	private JScrollPane			listComponent;
  
	/** The UI component displaying message. */
	private JPanel				messageComponent;
  
	/** Area where to annotate the <code>DataObject</code>. */
	private JTextArea           annotationArea;
  
	/** List of already annotated <code>DataObject</code>s. */
	private JList               annotatedList;
  
	/** The list model used to display the annotated object. */
	private DefaultListModel	listModel;
  
	/** Map of annotated <code>DataObject</code>s. */
	private Map					annotatedMap;
  
	/** Reference to the <code>Model</code>. */
	private AnnotatorModel 		model;
  
	/** Reference to the <code>Control</code>. */
	private AnnotatorControl	controller;
  
	/**
	 * Enables the <code>Finish</code> action depending on the 
	 * length of the text entered.
	 */
	private void handleAnnotationAreaInsert()
	{
		FinishAction action = (FinishAction) 
					controller.getAction(AnnotatorControl.FINISH);
		action.setEnabled(!(getAnnotationText().length() == 0));
	}
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		messageComponent = new JPanel();
		messageComponent.setLayout(new BoxLayout(messageComponent, 
  								BoxLayout.Y_AXIS));
		messageComponent.add(new JLabel(MESSAGE));
		messageComponent.add(new JLabel(BODY));
		messageComponent.setVisible(false);
		annotatedMap = new HashMap();
		annotationArea = new MultilineLabel();
		annotationArea.setBorder(new TitledBorder("Annotation"));
		annotationArea.setEditable(true);
		listModel = new DefaultListModel();
		annotatedList = new JList(listModel);
		listComponent = new JScrollPane(annotatedList);
		listComponent.setVisible(false);
		annotatedList.setBorder(new TitledBorder("Annotated items"));
		annotatedList.setSelectionMode(
              ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		annotatedList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
              if (e.getClickCount() == 1) {
                  showSingleAnnotation();
               }
			}
		});
		annotationArea.getDocument().addDocumentListener(
  			 new DocumentListener() {
  		            
  		            /** 
  		             * Indicates that the object is annotated. 
  		             * @see DocumentListener#insertUpdate(DocumentEvent)
  		             */
  		            public void insertUpdate(DocumentEvent de)
  		            {
  		            	handleAnnotationAreaInsert();
  		            }
  		            
  		            /** 
  		             * Indicates that the object is annotated. 
  		             * @see DocumentListener#removeUpdate(DocumentEvent)
  		             */
  		            public void removeUpdate(DocumentEvent de)
  		            {
  		            	handleAnnotationAreaInsert();
  		            }

  		            /** 
  		             * Required by I/F but no-op implementation in our case. 
  		             * @see DocumentListener#changedUpdate(DocumentEvent)
  		             */
  		            public void changedUpdate(DocumentEvent de) {}
  		            
  		        });
	}
  
	/**
	 * Builds a panel hosting the {@link #annotationArea} and the list of users
	 * who annotated the data object.
	 * 
	 * @return See above.
	 */
	private JPanel buildAnnotationPanel()
	{
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
		p.add(listComponent, c);      
	    return p;
	}
  
	/**
	 * Builds the UI component hosting the message.
	 * 
	 * @return See above.
	 */
	private JPanel buildCommentsPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(new JLabel(TITLE));
		p.add(messageComponent);
		return UIUtilities.buildComponentPanel(p);
	}
  
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(buildCommentsPanel());
		add(Box.createRigidArea(SMALL_V_SPACER_SIZE));
		add(new JSeparator());
		add(Box.createRigidArea(SMALL_V_SPACER_SIZE));
		add(buildAnnotationPanel());
		add(Box.createVerticalGlue());
	}
  
	/** Shows a single annotation. */
	private void showSingleAnnotation()
	{
		int index = annotatedList.getSelectedIndex();
		if (index == -1) return;
		List list = getDataObjectAnnotation(index);
		if (list.size() > 0) {
			AnnotationData data = (AnnotationData) list.get(0);
			annotationArea.setText(data.getText());
		}
	}
  
	/**
	 * Returns the list of annotations associated to the 
	 * <code>DataObject</code>.
	 * 
	 * @param index The index of the annotated  <code>DataObject</code>.
	 * @return See below.
	 */
	private List getDataObjectAnnotation(int index)
	{ 
		Map annotations = model.getAnnotations();
		Long ownerID = (Long) annotatedMap.get(new Integer(index));
		if (ownerID == null) return new ArrayList();    //empty list
		return (List) annotations.get(ownerID);
	}
  
	/**
   	* Displays the name of the data object and the time when annotated.
   	* 
   	* @param objects Array of annotated <code>DataObject</code>.
   	*/
	private void formatList(String[] objects)
	{
		// remove all users from list before adding new
		listModel.removeAllElements();
      
		// add each user to list
		Timestamp date;
		DateFormat df = DateFormat.getDateInstance();
		AnnotationData data;
		List list;
		for (int i = 0; i < objects.length; i++) {
         	list =  getDataObjectAnnotation(i);
         	data = ((AnnotationData) list.get(0));
         	date = data.getLastModified();
         	if (date == null) date = new Timestamp(new Date().getTime());;
            listModel.addElement(objects[i]+" ("+df.format(date)+")");   
		}
	}
  
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	 Reference to the model. Mustn't be <code>null</code>.
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 */
	AnnotatorUI(AnnotatorModel model, AnnotatorControl controller)
	{
		if (model == null) 
			throw new IllegalArgumentException("No model.");
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		this.model = model;
		this.controller = controller;
		initComponents();
		buildGUI();
	}
	
	/** Builds the list of already annotated <code>DataObject</code>s. */
	void showAnnotations()
	{
		Map annotations = model.getAnnotations();
      if (annotations == null) return;
      String[] objects = new String[annotations.size()];
      Iterator i = annotations.keySet().iterator();
      Long id;
      List list;
      DataObject data;
      int index = 0;
      while (i.hasNext()) {
          id = (Long) i.next();
          list = (List) annotations.get(id);
          if (list != null || list.size() > 0) {
              data = ((AnnotationData) list.get(0)).getAnnotatedObject();
              objects[index] = model.getDataObjectName(data);
              annotatedMap.put(new Integer(index), id);
              index++;
          } 
      }
      formatList(objects);
      annotatedList.setSelectedIndex(0);
      showSingleAnnotation();
      listComponent.setVisible(true);
      messageComponent.setVisible(true);
	}
	
	/**
	 * Returns the textual annotation.
	 * 
	 * @return See above.
	 */
	String getAnnotationText() { return annotationArea.getText().trim(); }

}
