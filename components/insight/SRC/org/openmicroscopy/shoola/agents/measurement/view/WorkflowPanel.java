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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureListener;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.checkboxlist.CheckBoxList;
import org.openmicroscopy.shoola.util.ui.checkboxlist.CheckBoxModel;
import pojos.WorkflowData;

/**
 * Displays the worklfows.
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
class WorkflowPanel
	extends JDialog 
	implements ComponentListener
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
	
	/** Mouse listener. */
	private MouseAdapter	listener;
	
	/**
	 * Initializes the components. Create the label, workflow combobox.
	 */
	private void init()
	{
		label = new JLabel("Keywords");
		namespaceLabel = new JLabel("Namespace");
		List<String> list = model.getWorkflows();
		String[] values = new String[list.size()];
		Iterator<String> i = list.iterator();
		int index = 0;
		while (i.hasNext()) {
			values[index] = view.getWorkflowDisplay(i.next());
		}
		namespaceCombobox = new JComboBox(values);
		namespaceCombobox.addActionListener(controller.getAction(
				MeasurementViewerControl.SELECT_WORKFLOW));
		checkBoxModel = new CheckBoxModel();
		keywords = new CheckBoxList(checkBoxModel);
		listener = new MouseAdapter() {
			
			public void mouseClicked(MouseEvent e) {
				CheckBoxList checkBoxList = (CheckBoxList) e.getSource();
			    List<String> keywords = checkBoxList.getTrueValues();
			    String value = view.getWorkflowFromDisplay(
			    		(String) namespaceCombobox.getSelectedItem());
			    model.setWorkflow(value);
			    model.setKeyword(keywords);
				applyWorkflowToCollection(
						view.getDrawingView().getSelectedFigures());
				view.rebuildManagerTable();
				view.refreshInspectorTable();
			}
		};
		keywords.addMouseListener(listener);
		addComponentListener(this);
		workflowDialog = new WorkflowDialog(view, model);
	}
	
	/** Builds and lays out the UI. */
	private void buildUI()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel j = new JPanel();
		j.setLayout(new BoxLayout(j, BoxLayout.X_AXIS));
		j.add(namespaceLabel);
		Dimension d = new Dimension(200,30);
		namespaceCombobox.setSize(d);
		namespaceCombobox.setMinimumSize(d);
		namespaceCombobox.setMaximumSize(d);
		namespaceCombobox.setPreferredSize(d);
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
		bevelPanel.setBorder(
				BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		bevelPanel.add(panel, BorderLayout.CENTER);
		this.setLayout(new BorderLayout());
		this.getContentPane().add(bevelPanel, BorderLayout.CENTER);
		this.setVisible(false);
		setSize(300,200);
	}
	
	/**
	 * Applies the workflow to the collection of selected figures in the 
	 * drawing view.
	 * 
 	 * @param figures See above.
	 */
	private void applyWorkflowToCollection(Collection<Figure> figures)
	{
		ROIFigure roiFigure;
		List<FigureListener> figureListeners;
		for (Figure fig : figures)
		{
			roiFigure = (ROIFigure) fig;
			figureListeners = roiFigure.getFigureListeners();
			for (FigureListener listener : figureListeners)
				roiFigure.removeFigureListener(listener);
			applyWorkflow(roiFigure);
			for (FigureListener listener : figureListeners)
				roiFigure.addFigureListener(listener);
		}
	}

	/**
	 * Applies the current workflow to the figure.
	 * 
	 * @param fig See above.
	 */
	private void applyWorkflow(ROIFigure fig)
	{
		fig.getROI().setAnnotation(AnnotationKeys.NAMESPACE, 
				model.getWorkflow().getNameSpace());
	}
	
	/**
	 * Creates the workflow panel, allowing the selection of the 
	 * keyword from the current workflow. 
	 * 
	 * @param view The view which will indicate current workflows. 
	 * @param model The model. 
	 * @param controller The controller. 
	 */
	WorkflowPanel(MeasurementViewerUI view, MeasurementViewerModel model,
							MeasurementViewerControl controller)
	{
		this.view = view;
		this.model = model;
		this.controller = controller;
		init();
		buildUI();
	}

	/** Updates the workflow. */
	void updateWorkflow()
	{
		WorkflowData workflow = model.getWorkflow();
		if (workflow == null)
		{
			namespaceCombobox.removeActionListener(this.controller.getAction(
					MeasurementViewerControl.SELECT_WORKFLOW));
			namespaceCombobox.setSelectedItem(WorkflowData.DEFAULTWORKFLOW);
			keywords.setModel(new CheckBoxModel(new ArrayList()));
			namespaceCombobox.addActionListener(this.controller.getAction(
					MeasurementViewerControl.SELECT_WORKFLOW));
		} else {
			namespaceCombobox.removeActionListener(this.controller.getAction(
					MeasurementViewerControl.SELECT_WORKFLOW));
			namespaceCombobox.setSelectedItem(view.getWorkflowDisplay(
					workflow.getNameSpace()));
			namespaceCombobox.addActionListener(controller.getAction(
					MeasurementViewerControl.SELECT_WORKFLOW));
			
			List<String> list = workflow.getKeywordsAsList();
			CheckBoxModel tableModel = new CheckBoxModel(list);
			keywords.setModel(tableModel);
			List<String> words = model.getKeywords();
			keywords.setTrueValues(words);
			repaint();
			if (!isVisible())
				UIUtilities.setLocationRelativeToAndSizeToWindow(view, this, 
						new Dimension(this.getWidth(), this.getHeight()));
		}
	}
	
	/** Adds the workflow to the list. */
	void addedWorkflow()
	{
		namespaceCombobox.removeActionListener(this.controller.getAction(
				MeasurementViewerControl.SELECT_WORKFLOW));
		namespaceCombobox.removeAllItems();
		List<String> list = model.getWorkflows();
		Iterator<String> i = list.iterator();
		while (i.hasNext()) {
			namespaceCombobox.addItem(view.getWorkflowDisplay(i.next()));
		}
		namespaceCombobox.addActionListener(controller.getAction(
				MeasurementViewerControl.SELECT_WORKFLOW));
	}

	/**  Shows the created dialog. */
	void createWorkflow()
	{
		UIUtilities.centerAndShow(workflowDialog);
	}

	/**
	 * Called when the dialog is closed.
	 * @see ComponentListener#componentHidden(ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e)
	{
		//model.setWorkflow(WorkflowData.DEFAULTWORKFLOW);
	}

	/**
	 * Implemented as specified by the {@link ComponentListener} I/F, 
	 * no-operation implementation in our case.
	 * @see ComponentListener#componentMoved(ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {}

	/**
	 * Implemented as specified by the {@link ComponentListener} I/F, 
	 * no-operation implementation in our case.
	 * @see ComponentListener#componentResized(ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {}

	/**
	 * Implemented as specified by the {@link ComponentListener} I/F, 
	 * no-operation implementation in our case.
	 * @see ComponentListener#componentShown(ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {}
	
}
