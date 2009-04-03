/*
 * org.openmicroscopy.shoola.agents.annotator.TextAnnotationUIF
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.annotator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openmicroscopy.shoola.env.config.Registry;

/**
 * The UIF for a text annotation, not the UIF for other semantic types and
 * attributes and classifications.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class TextAnnotationUIF extends JDialog
                               implements DocumentListener
{
    private Registry registry;
    
    private AnnotationCtrl controller;
    private JTextArea annotationArea;
    private JButton saveButton;
    private JButton reloadButton;
    
    private boolean isNewAnnotation = false;
    private int annotationIndex = 0; // TODO modify/add mutator if multi-IA
    
    /**
     * Construct a new TextAnnotationUIF.
     * @param control The controller for the UI.
     * @param registry The reference to the application registry.
     */
    public TextAnnotationUIF(AnnotationCtrl control, Registry registry)
    {
        super(registry.getTaskBar().getFrame());
        // TODO more precise set bounds
        if(control == null || registry == null)
        {
            throw new IllegalArgumentException("No null arguments permitted.");
        }
        
        this.controller = control;
        this.registry = registry;
        
        buildGUI();
    }
    
    // build the UI.
    private void buildGUI()
    {
        setTitle("Edit Image Annotation");
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        
        JPanel targetPanel = new JPanel();
        targetPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel editingLabel = new JLabel("Editing: ");
        editingLabel.setFont(new Font(null,Font.BOLD,12));
        JLabel targetLabel = new JLabel(controller.getTargetDescription());
        targetPanel.add(editingLabel);
        targetPanel.add(targetLabel);
        container.add(targetPanel,BorderLayout.NORTH);
        annotationArea = new JTextArea();
        annotationArea.setLineWrap(true);
        annotationArea.setWrapStyleWord(true);
        annotationArea.getDocument().addDocumentListener(this);
        
        JScrollPane scrollPane = new JScrollPane(annotationArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        scrollPane.setPreferredSize(new Dimension(300,150));
        container.add(scrollPane,BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        reloadButton = new JButton("Reload");
        
        reloadButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if(isNewAnnotation)
                {
                    annotationArea.setText("");
                }
                else
                {
                    annotationArea.setText(controller.getAnnotation(annotationIndex));
                }
            }
        });
        saveButton = new JButton("Save");
        saveButton.setEnabled(false);
        
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if(isNewAnnotation)
                {
                    controller.newAnnotation(annotationArea.getText());
                    isNewAnnotation = false;
                    annotationIndex = 0;
                }
                else
                {
                    controller.setAnnotation(annotationIndex,
                                             annotationArea.getText());
                }
                controller.save();
                dispose();
            }
        });
        
        buttonPanel.add(reloadButton);
        buttonPanel.add(saveButton);
        container.add(buttonPanel, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        final Component refCopy = this;
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent arg0)
            {
                if(controller.isSaved())
                {
                    setVisible(false);
                    dispose();
                    controller.close();
                }
                else
                {
                    Object[] options = {"Save","Don't Save","Cancel"};
                    int status = JOptionPane.showOptionDialog(refCopy,
                                    "Would you like to keep the changes?",
                                    "Save Annotation",
                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[0]);
                    if(status == JOptionPane.YES_OPTION)
                    {
                        // TODO add extraction from text area -> controller
                        controller.save();
                        setVisible(false);
                        dispose();
                        controller.close();
                    }
                    else if(status == JOptionPane.NO_OPTION)
                    {
                        setVisible(false);
                        dispose();
                        controller.close();
                    }
                    else if(status == JOptionPane.CANCEL_OPTION)
                    {
                        // do nothing
                    }
                }
            }
        });
        
        // ok, now check to see if there are any annotations (single-annotation
        // use case here)
        List annotations = controller.getTextAnnotations();
        if(annotations.size() == 0)
        {
            isNewAnnotation = true;
            annotationArea.setText("(no annotation)");
            controller.setSaved(false);
        }
        else
        {
            String annotation = controller.getAnnotation(0);
            annotationArea.setText(annotation);
            // override change command
            controller.setSaved(true);
        }
        
        pack();

    }
    
    /**
     * Responds to a change in the text field (by marking as changed).
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent arg0)
    {
        controller.setSaved(false);
        saveButton.setEnabled(true);
    }
    
    /**
     * Responds to an insertion in the text field (by marking as changed).
     * 
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent arg0)
    {
        controller.setSaved(false);
        saveButton.setEnabled(true);
    }
    
    /**
     * Responds to a deletion in the text field (by marking as changed).
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent arg0)
    {
        controller.setSaved(false);
        saveButton.setEnabled(true);
    }
}
