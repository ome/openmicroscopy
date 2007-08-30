/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.DOBasic
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;

//Java imports
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.archived.view.Downloader;
import org.openmicroscopy.shoola.agents.util.archived.view.DownloaderFactory;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * Component displaying the minimum information on the currently edited 
 * <code>DataObject</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class DOBasic
    extends JPanel
{
    
    /** Title of the annotation pane. */
    static final String     ANNOTATION = "Annotation";
    
    /** Title of the classification pane. */
    static final String     CLASSIFICATION = "Categorisation";   
    
    /** Text of the {@link #download} button. */
    private final String	DOWNLOAD = "Download";
    
    /** Description of the {@link #download} button. */
    private final String	DOWNLOAD_DESCRIPTION = "Download the " +
    												"archived files";
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    JTextField                  nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    JTextArea                   descriptionArea;
    
    /** Button to download the archived files. */
    private JButton				download;
    
    /** The tabbed pane hosting the annotation pane or classified pane. */
    private JTabbedPane         tabbedPane;
    
    /** 
     * The component hosting the annotation, <code>null</code> if the data 
     * object is not annotable.
     */
    //private AnnotatorEditor		annotator;
    
    /** 
     * The component hosting the classification. <code>null</code> if the data 
     * object hasn't been clasified.
     */
    //private DOClassification    classifier;
    
    /** Component hosting the object permissions. */
    private DOInfo				permissionsInfo;
    
    /** A {@link DocumentListener} for the {@link #nameArea}. */
    private DocumentListener    nameAreaListener;
    
    /** Reference to the View. */
    private EditorUI            view;
    
    /** Reference to the Model. */
    private EditorModel         model;
    
    /** Reference to the Control. */
    private EditorControl       controller;

    /** Downloads the archived files. */
    void download()
    { 
    	Downloader dl = DownloaderFactory.getDownloader(
				model.getParentModel().getUI(), TreeViewerAgent.getRegistry(), 
				model.getPixelsID());
		dl.activate();
    }
    
    /** Initializes the components composing this display. */
    private void initComponents()
    {
    	tabbedPane = new JTabbedPane();
        nameArea = new JTextField();
        UIUtilities.setTextAreaDefault(nameArea);
        descriptionArea = new MultilineLabel();
        UIUtilities.setTextAreaDefault(descriptionArea);
        if (model.getEditorType() == Editor.PROPERTIES_EDITOR) {
            nameArea.setText(model.getDataObjectName());
            descriptionArea.setText(model.getDataObjectDescription());
            boolean b = model.isWritable();
            nameArea.setEnabled(b);
            descriptionArea.setEnabled(b);
            descriptionArea.getDocument().addDocumentListener(
                    new DocumentListener() {

                /** 
                 * Handles text insert. 
                 * @see DocumentListener#insertUpdate(DocumentEvent)
                 */
                public void insertUpdate(DocumentEvent de)
                {
                    view.handleDescriptionAreaInsert();
                }
                
                /** 
                 * Handles text removal. 
                 * @see DocumentListener#removeUpdate(DocumentEvent)
                 */
                public void removeUpdate(DocumentEvent de)
                {
                    view.handleDescriptionAreaInsert();
                }

                /** 
                 * Required by I/F but no-op implementation in our case. 
                 * @see DocumentListener#changedUpdate(DocumentEvent)
                 */
                public void changedUpdate(DocumentEvent de) {}
                
            });
            if (model.getHierarchyObject() instanceof ImageData) {
            	download = new JButton(DOWNLOAD);
            	download.setToolTipText(DOWNLOAD_DESCRIPTION);
            	download.setEnabled(model.isReadable());
            	download.addActionListener(new ActionListener() {
            		public void actionPerformed(ActionEvent e)
            		{ 
            			download(); 
            		}
				});
            }
            /*
            IconManager im = IconManager.getInstance();
            if (model.isAnnotatable()) {	
                //annotator = new DOAnnotation(view, model);
            	annotator = model.createAnnotator();
            	annotator.addPropertyChangeListener(controller);
                tabbedPane.addTab(ANNOTATION, 
                            im.getIcon(IconManager.ANNOTATION), 
                            annotator.getUI());
            }
            if (model.getHierarchyObject() instanceof ImageData) {
            	download = new JButton(DOWNLOAD);
            	download.setToolTipText(DOWNLOAD_DESCRIPTION);
            	download.setEnabled(model.isWritable());
            	download.addActionListener(new ActionListener() {
            		public void actionPerformed(ActionEvent e)
            		{ 
            			download(); 
            		}
				});
                classifier = new DOClassification(model, controller);
                tabbedPane.addTab(CLASSIFICATION, 
                			im.getIcon(IconManager.CATEGORY), classifier);
                tabbedPane.setSelectedIndex(EditorFactory.getSubSelectedPane());
            }
            */
            ExperimenterData exp = model.getDataObjectOwner();
        	
            Map details = EditorUtil.transformExperimenterData(exp);
            permissionsInfo = new DOInfo(view, model, details, true, 
                                DOInfo.OWNER_TYPE);
            
        } //end if editor type
        nameAreaListener = new DocumentListener() {
            
            /** 
             * Updates the editor's controls when some text is inserted. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
                view.handleNameAreaInsert();
            }
            
            /** 
             * Displays an error message when the data object has no name. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
                view.handleNameAreaRemove(de.getDocument().getLength());
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        nameArea.getDocument().addDocumentListener(nameAreaListener);
        nameArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e)
            {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER)) {
                    Object source = e.getSource();
                    if (source instanceof JTextField) {
                        JTextField field = (JTextField) source;
                        if (field.getText() != null && 
                                field.getText().length() > 0)
                            view.finish();
                    }
                }
            }
        });
        if (tabbedPane == null) return;
        tabbedPane.addChangeListener(new ChangeListener() {
            
            /**
             * Retrieves the classification when the classification tabbed pane
             * is selected for the first time.
             * @see ChangeListener#stateChanged(ChangeEvent)
             */
            public void stateChanged(ChangeEvent ce)
            {
                JTabbedPane pane = (JTabbedPane) ce.getSource();
                int index = pane.getSelectedIndex();
                EditorFactory.setSubSelectedPane(index);
                switch (index) {
					case Editor.ANNOTATION_INDEX:
						if (model.isAnnotatable()) 
	                        controller.retrieveAnnotations();
						break;
					case Editor.CLASSIFICATION_INDEX:
						if (model.isClassified()) {
	                        if (!model.isClassificationLoaded())
	                            controller.loadClassifications();
	                    }
						break;
				}
            };
        });
    }   
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}. If the <code>DataOject</code>
     * is annotable and if we are in the {@link Editor#PROPERTIES_EDITOR} mode,
     * we display the annotation pane. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
        int height = 100;
        if (model.isAnnotatable()) height = 50;
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{0, TableLayout.PREFERRED, 5, 0, height} }; //rows
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel l;
        if (model.getEditorType() != Editor.CREATE_EDITOR) { 
            layout.setRow(0, TableLayout.PREFERRED);
            content.add(UIUtilities.setTextFont("ID"), "0, 0, l, c");
            l = new JLabel(""+model.getDataObjectID());
            l.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
            content.add(l, "1, 0, f, c");
        }
        content.add(UIUtilities.setTextFont("Name"), "0, 1, l, c");
        content.add(nameArea, "1, 1, f, c");
        content.add(new JLabel(), "0, 2, 1, 2");
        l = UIUtilities.setTextFont("Description");
        int h = l.getFontMetrics(l.getFont()).getHeight()+5;
        layout.setRow(3, h);
        content.add(l, "0, 3, l, c");
        JScrollPane pane = new JScrollPane(descriptionArea);
        content.add(pane, "1, 3, 1, 4");
        return content;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}.
     */
    private void buildGUI()
    {
        setLayout(new BorderLayout());
        if (permissionsInfo != null) {
        	JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(buildContentPanel());
            p.add(permissionsInfo);
            add(p, BorderLayout.NORTH);
        } else add(buildContentPanel(), BorderLayout.NORTH);
        add(view.buildBasicToolBar(download), BorderLayout.SOUTH);
    }

    /**
     * Creates a new instance.
     * 
     * @param view          Reference to the {@link EditorUI}.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the {@link EditorModel}.
     *                      Mustn't be <code>null</code>.  
     * @param controller    Reference to the {@link EditorControl}.
     *                      Mustn't be <code>null</code>.                           
     */
    DOBasic(EditorUI view, EditorModel model, EditorControl controller)
    {
        if (view == null) throw new IllegalArgumentException("No View.");
        if (model == null)  throw new IllegalArgumentException("No Model.");
        if (controller == null) 
            throw new IllegalArgumentException("No Control.");
        this.view = view;
        this.model = model;
        this.controller = controller;
        initComponents();
        buildGUI();
    }   

    /**
     * Returns the value of the {@link #nameArea}.
     * 
     * @return See above.
     */
    String getNameText() { return nameArea.getText().trim(); }
    
    /**
     * Returns the value of the {@link #descriptionArea}.
     * 
     * @return See above.
     */
    String getDescriptionText() { return descriptionArea.getText().trim(); }
    
    /** 
     * Sets the text to <code>null</code> but we first need to remove the 
     * {@link DocumentListener} attached to the {@link #nameArea}.
     * This method is invoked when the user tries to save a object which 
     * name is only made of spaces.
     */
    void resetNameArea()
    {
        nameArea.getDocument().removeDocumentListener(nameAreaListener);
        nameArea.setText(null);
        nameArea.getDocument().addDocumentListener(nameAreaListener);
    }

    /** Shows the classifications. */
    void showClassifications()
    {
        //if (classifier == null) return;
        //classifier.showClassifications();
    }

    /** 
     * Resets the value of the {@link #nameArea} when a wrong value 
     * has been entered.
     */
    void resetName()
    {
    	nameArea.getDocument().removeDocumentListener(nameAreaListener);
    	if (model.getEditorType() == Editor.PROPERTIES_EDITOR) 
            nameArea.setText(model.getDataObjectName());
        else nameArea.setText("");
    	nameArea.getDocument().addDocumentListener(nameAreaListener);
    }
    
    /**
     * Checks if the name and/or description have been modified.
     * Returns <code>true</code> if modified, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasDataToSave()
    {
    	// heck if the name and the description have been modified first.
    	String s = getNameText();
    	String name = model.getDataObjectName();
    	if (!(name.equals(s))) return true;
    	String d = getDescriptionText();
    	String description = model.getDataObjectDescription();
    	
    	if (d == null) return (description != null);
    	return (!(d.equals(description)));
    }

    /**
     * Adds the passed nodes for annotation.
     * 
     * @param nodes	The nodes to pass.
     */
	void addSiblings(List nodes)
	{
		//if (annotator != null) annotator.addSelectedNodes(nodes);
	}
    
}
