/*
* org.openmicroscopy.shoola.agents.measurement.view.WorkflowPanel
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

//Third-party libraries

//Application-internal dependencies
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureListener;
import org.openmicroscopy.shoola.agents.measurement.actions.MeasurementViewerAction;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.checkboxlist.CheckBoxList;
import org.openmicroscopy.shoola.util.ui.checkboxlist.CheckBoxModel;

import pojos.WorkflowData;

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class WorkflowPanel
	extends JDialog implements MouseListener, ComponentListener
{
	
	/** Label for the keyword selected. */
	private JLabel label;
	
	/** Label for the namespace selected. */
	private JLabel namespaceLabel;
	
	/** The combobox of the namespace. */
	private JComboBox namespaceCombobox;
	
	/** Checkbox of the selected workflow keywords. */
	private CheckBoxList keywords;
	
	/** The view of the main UI. */
	private MeasurementViewerUI view;
	
	/** The model for the workflowpanel. */
	private MeasurementViewerModel model;
	
	/** The control for the workflowpanel. */
	private MeasurementViewerControl controller;
	
	/** The model for the checkbox control containing the keywords for the 
	 * selected workflow.
	 */
	private CheckBoxModel checkBoxModel;
	
	/** The create workflow Dialog. */
	private WorkflowDialog workflowDialog;
	
	/**
	 * The constructor for the workflow panel, allowing the selection of the 
	 * keyword from the current workflow. 
	 * @param view The view which will indicate current workflows. 
	 * @param model The model. 
	 * @param controller The controller. 
	 */
	public WorkflowPanel(MeasurementViewerUI view, MeasurementViewerModel model,
							MeasurementViewerControl controller)
	{
		this.view = view;
		this.model = model;
		this.controller = controller;
		init();
		buildUI();
	}


	/**
	 * Initialise the components. Create the label, workflow combobox.
	 */
	private void init()
	{
		label = new JLabel("Keywords");
		namespaceLabel = new JLabel("Namespace");
		namespaceCombobox = new JComboBox(model.getWorkflows().toArray());
		namespaceCombobox.addActionListener(this.controller.getAction(
				MeasurementViewerControl.SELECTWORKFLOW));
		checkBoxModel = new CheckBoxModel();
		keywords = new CheckBoxList(checkBoxModel);
		keywords.addMouseListener(this);
		addComponentListener(this);
		workflowDialog = new WorkflowDialog(view, model);
	}
	/**
	 * Build the UI using the components created in init().
	 */
	private void buildUI()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel j = new JPanel();
		j.setLayout(new BoxLayout(j, BoxLayout.X_AXIS));
		j.add(namespaceLabel);
		j.add(namespaceCombobox);
		panel.add(j);
		JPanel k = new JPanel();
		k.setLayout(new BoxLayout(k, BoxLayout.Y_AXIS));
		k.add(label);
	    JScrollPane scrollPane = new JScrollPane(keywords);
	    scrollPane.setBorder(BorderFactory.createEtchedBorder());
		k.add(scrollPane);
		panel.add(k);
		JPanel bevelPanel = new JPanel();
		bevelPanel.setLayout(new BorderLayout());
		bevelPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		bevelPanel.add(panel, BorderLayout.CENTER);
		this.setLayout(new BorderLayout());
		this.getContentPane().add(bevelPanel, BorderLayout.CENTER);
		this.setVisible(false);
		setSize(300,200);
	}

	/**
	 * The workflow has changed, update the combobox.
	 */
	public void updateWorkflow()
	{
		WorkflowData workflow = model.getWorkflow();
		if(workflow == null)
		{
			namespaceCombobox.removeActionListener(this.controller.getAction(
					MeasurementViewerControl.SELECTWORKFLOW));
			namespaceCombobox.setSelectedItem(WorkflowData.DEFAULTWORKFLOW);
			keywords.setModel(new CheckBoxModel(new ArrayList()));
			namespaceCombobox.addActionListener(this.controller.getAction(
					MeasurementViewerControl.SELECTWORKFLOW));
		}
		else
		{
			namespaceCombobox.removeActionListener(this.controller.getAction(
					MeasurementViewerControl.SELECTWORKFLOW));
			namespaceCombobox.setSelectedItem(workflow.getNameSpace());
			namespaceCombobox.addActionListener(this.controller.getAction(
					MeasurementViewerControl.SELECTWORKFLOW));
			
			CheckBoxModel tableModel = new CheckBoxModel(workflow.getKeywordsAsList());
			keywords.setModel(tableModel);
			keywords.setTrueValues(model.getKeywords());
			repaint();
			if(!isVisible())
				UIUtilities.setLocationRelativeToAndSizeToWindow(view, this, 
						new Dimension(this.getWidth(), this.getHeight()));
		}
	}
	
	public void addedWorkflow()
	{
		namespaceCombobox.removeActionListener(this.controller.getAction(
				MeasurementViewerControl.SELECTWORKFLOW));
		namespaceCombobox.removeAllItems();
		for(String workflow : model.getWorkflows())
			namespaceCombobox.addItem(workflow);
		//namespaceCombobox.setSelectedItem(model.getWorkflow().getNameSpace());
		namespaceCombobox.addActionListener(this.controller.getAction(
				MeasurementViewerControl.SELECTWORKFLOW));
	}
	
	/**
	 * On mouse click, apply the workflow to the selected figures.
	 * @param e The mouse click event.
	 */
	public void mouseClicked(MouseEvent e)
	{
		CheckBoxList checkBoxList = (CheckBoxList)e.getSource();
	    List<String> keywords = checkBoxList.getTrueValues();
	    model.setWorkflow((String)this.namespaceCombobox.getSelectedItem());
	    model.setKeyword(keywords);
		applyWorkflowToCollection(view.getDrawingView().getSelectedFigures());
		view.rebuildManagerTable();
		view.refreshInspectorTable();
	}

	/**
	 * Apply the workflow to the collection of selected figures in the 
	 * drawing view.
 	 * @param figures See above.
	 */
	private void applyWorkflowToCollection(Collection<Figure> figures)
	{
		for(Figure fig : figures)
		{
			ROIFigure roiFigure = (ROIFigure)fig;
			List<FigureListener> figureListeners = roiFigure.getFigureListeners();
			for(FigureListener listener : figureListeners)
				roiFigure.removeFigureListener(listener);
			applyWorkflow(roiFigure);
			for(FigureListener listener : figureListeners)
				roiFigure.addFigureListener(listener);
			
		}
	}

	/**
	 * Apply the current workflow to the figure.
	 * @param fig See above.
	 */
	private void applyWorkflow(ROIFigure fig)
	{
		fig.getROI().setAnnotation(AnnotationKeys.NAMESPACE, 
										model.getWorkflow().getNameSpace());
		List<String> keywordList = model.getKeywords();
		String keywordString = "";
		for(int i = 0 ; i < keywordList.size() ; i++)
		{
			keywordString = keywordString + keywordList.get(i);
			if(i<keywordList.size()-1)
				keywordString = keywordString + ",";
		}
		fig.getROI().setAnnotation(AnnotationKeys.KEYWORDS, keywordString);
	}
	
	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}


	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}


	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}


	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Called when the dialog is closed.
	 * @param e The event, which is ignored. 
	 */
	public void componentHidden(ComponentEvent e)
	{
		model.setWorkflow(WorkflowData.DEFAULTWORKFLOW);
	}


	public void componentMoved(ComponentEvent e)
	{
		// TODO Auto-generated method stub
		
	}


	public void componentResized(ComponentEvent e)
	{
		// TODO Auto-generated method stub
		
	}


	public void componentShown(ComponentEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	/** 
	 * Show the create workflow Dialog.
	 */
	public void createWorkflow()
	{
		UIUtilities.centerAndShow(workflowDialog);
	}
	
}
