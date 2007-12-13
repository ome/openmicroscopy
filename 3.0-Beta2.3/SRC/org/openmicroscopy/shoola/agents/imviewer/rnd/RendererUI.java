/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.RendererUI
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;



//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
* The {@link Renderer} view. Provides a menu bar, a status bar and a 
* panel hosting various controls.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
class RendererUI
	extends JPanel//TopWindow
	implements ActionListener
{
  
	/** Identifies the {@link DomainPane}. */
	static final Integer        DOMAIN = new Integer(0);

	/** Identifies the {@link CodomainPane}. */
	static final Integer        CODOMAIN = new Integer(1);

	/** 
	 * Text display in the {@link #historyButton} when the local history
	 * is hidden.
	 */
	private static final String	SHOW_HISTORY = "Show Local History...";

	/** 
	 * Text display in the {@link #historyButton} when the local history 
	 * is shown.
	 */
	private static final String 	HIDE_HISTORY = "Hide Local History...";
	
	/** Action ID to copy the rendering settings. */
	private static final int		COPY = 0;

	/** Action ID to paste the rendering settings. */
	private static final int		PASTE = 1;
	
	/** Action ID to show or hide the history. */
	private static final int		HISTORY = 2;
	
	/** Action ID to reset the original rendering settings. */
	private static final int		RESET_DEFAULT = 3;
	
	/** Action ID to save the  the rendering settings. */
	private static final int		SAVE = 4;

	/** Horizontal space between the buttons. */
	private static final Dimension	H_SPACE = new Dimension(5, 5);
	
	/** Reference to the control. */
	private RendererControl     			controller;

	/** Reference to the model. */
	private RendererModel       			model;

	/** The map hosting the controls pane. */
	private HashMap<Integer, ControlPane>	controlPanes;

	/** Button to copy the rendering settings. */
	private JButton							copyButton;

	/** Button to paste the rendering settings. */
	private JButton							pasteButton;
	
	/** Button to display the local history. */
	private JButton							historyButton;
	
	/** Button to reset the rendering settings. */
	private JButton							resetButton;

	/** Button to save the rendering settings. */
	private JButton							saveButton;

	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		
		//copyButton = new JButton("Copy");
		copyButton = new JButton(icons.getIcon(IconManager.COPY));
		copyButton.setToolTipText(
			UIUtilities.formatToolTipText("Copies the rendering settings."));
		copyButton.setActionCommand(""+COPY);
		copyButton.addActionListener(this);
		//UIUtilities.unifiedButtonLookAndFeel(copyButton);
		//pasteButton = new JButton("Paste");
		pasteButton = new JButton(icons.getIcon(IconManager.PASTE));
		pasteButton.setEnabled(model.getParentModel().hasSettingsToPaste());
		pasteButton.setToolTipText(
			UIUtilities.formatToolTipText("Pastes the rendering settings."));
		pasteButton.setActionCommand(""+PASTE);
		pasteButton.addActionListener(this);
		//UIUtilities.unifiedButtonLookAndFeel(pasteButton);
		historyButton = new JButton();
		updateHistory(model.getParentModel().isHistoryShown());
		historyButton.setActionCommand(""+HISTORY);
		historyButton.addActionListener(this);
		resetButton = new JButton(icons.getIcon(IconManager.RESET_SETTINGS));
		resetButton.setToolTipText(
				UIUtilities.formatToolTipText("Reverts to Original Settings."));
		resetButton.setActionCommand(""+RESET_DEFAULT);
		resetButton.addActionListener(this);
		saveButton = new JButton(icons.getIcon(IconManager.SAVE_SETTINGS));
		saveButton.setToolTipText(
				UIUtilities.formatToolTipText("Save the current settings."));
		saveButton.setActionCommand(""+SAVE);
		saveButton.addActionListener(this);
		//UIUtilities.unifiedButtonLookAndFeel(resetButton);
	}

	/** Creates the panels hosting the rendering controls. */
	private void createControlPanes()
	{
		ControlPane p = new DomainPane(model, controller);
		p.addPropertyChangeListener(controller);
		controlPanes.put(DOMAIN, p);
		p = new CodomainPane(model, controller);
		p.addPropertyChangeListener(controller);
		controlPanes.put(CODOMAIN, p);
	}

	/**
	 * Creates the accept, revert buttons on the bottom on the panel.
	 * 
	 * @return See above.
	 */
	private JPanel createButtonsPanel()
	{
		JPanel bar = new JPanel();
		bar.setBorder(null);
		bar.add(historyButton);
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		tb.setRollover(true);
		tb.setBorder(null);
		tb.add(new JSeparator());
		tb.add(copyButton);
		tb.add(Box.createRigidArea(H_SPACE));
		tb.add(pasteButton);
		tb.add(Box.createRigidArea(H_SPACE));
		tb.add(resetButton);
		tb.add(Box.createRigidArea(H_SPACE));
		tb.add(saveButton);
		bar.add(tb);
		JPanel p = UIUtilities.buildComponentPanelRight(bar);
		p.setOpaque(true);
		return p;
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP,
				JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		ControlPane pane = controlPanes.get(DOMAIN);
		tabs.insertTab(pane.getPaneName(), pane.getPaneIcon(), 
				new JScrollPane(pane), pane.getPaneDescription(), 
				pane.getPaneIndex());
		pane = controlPanes.get(CODOMAIN);
		tabs.insertTab(pane.getPaneName(), pane.getPaneIcon(), 
				new JScrollPane(pane), pane.getPaneDescription(), 
				pane.getPaneIndex());
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
		add(createButtonsPanel(), BorderLayout.SOUTH);
	}

	/**
	 * Creates a new instance. The method 
	 * {@link #initialize(RendererControl, RendererModel) initialize}
	 * should be called straight after.
	 * 
	 * @param title The name of the image.
	 */
	RendererUI(String title)
	{
		//super("Display Settings:  "+title);
		controlPanes = new HashMap<Integer, ControlPane>(2);
	}

	/**
	 * Links the MVC triad.
	 * 
	 * @param controller    Reference to the Control.
	 *                      Mustn't be <code>null</code>.
	 * @param model         Reference to the Model.
	 *                      Mustn't be <code>null</code>.
	 */
	void initialize(RendererControl controller, RendererModel model)
	{
		if (controller == null) throw new NullPointerException("No control.");
		if (model == null) throw new NullPointerException("No model.");
		this.controller = controller;
		this.model = model;
		initComponents();
		//setJMenuBar(createMenuBar());
		createControlPanes();
		buildGUI();
		//pack();
	}

	/**
	 * Updates the corresponding controls when a codomain transformation
	 * is added.
	 * 
	 * @param mapType The type of codomain transformation. 
	 */
	void addCodomainMap(Class mapType)
	{
		CodomainPane pane = (CodomainPane) controlPanes.get(CODOMAIN);
		pane.addCodomainMap(mapType);
	}

	/**
	 * Updates the corresponding controls when a codomain transformation
	 * is added.
	 * 
	 * @param mapType The type of codomain transformation. 
	 */
	void removeCodomainMap(Class mapType)
	{
		CodomainPane pane = (CodomainPane) controlPanes.get(CODOMAIN);
		pane.removeCodomainMap(mapType);
	}

	/**
	 * Sets the specified channel as current.
	 * 
	 * @param c The channel's index.
	 */
	void setSelectedChannel(int c)
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.setSelectedChannel(c);
	}

	/** 
	 * Sets the color of the specified channel
	 * 
	 * @param c The channel's index.
	 */
	void setChannelButtonColor(int c)
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.setChannelButtonColor(c);
	}

	/** Sets the pixels intensity interval. */
	void setInputInterval()
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.setInputInterval();
	}

	/** Resets the UI controls. */
	void resetDefaultRndSettings()
	{
		Iterator i = controlPanes.keySet().iterator();
		ControlPane pane;
		while (i.hasNext()) {
			pane = controlPanes.get(i.next());
			pane.resetDefaultRndSettings();
		}
	}

	/**
	 * This is a method which is triggered from the {@link RendererControl} 
	 * if the colour model has changed.
	 */
	void setColorModelChanged() 
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.setColorModelChanged();
	}

	/** 
	 * Updates the UI when a new curve is selected i.e. when a new family
	 * is selected or when a new gamma value is selected.
	 */
	void onCurveChange()
	{
		DomainPane pane = (DomainPane) controlPanes.get(DOMAIN);
		pane.onCurveChange();
	}

	/**
	 * Sets the {@link #pasteButton} enable.
	 * 
	 * @param b Pass <code>true</code> to enable the button, <code>false</code>
	 * 			otherwise.
	 */
	void enablePasteButton(boolean b) { pasteButton.setEnabled(b); }

	/** Updates the text of the {@link #historyButton}. 
	 * 
	 * @param b Pass <code>true</code> to display the <code>hide</code> text,
	 * 			<code>false</code> to display the <code>show</code> text.
	 */
	void updateHistory(boolean b)
	{
		if (b) historyButton.setText(HIDE_HISTORY);
		else historyButton.setText(SHOW_HISTORY);
	}
	
	/**
	 * Copies, pastes the rendering settings
	 * or displays the local history.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case COPY:
				model.getParentModel().copyRenderingSettings();
				break;
			case PASTE:
				model.getParentModel().pasteRenderingSettings();
				break;
			case HISTORY:
				boolean b = !model.getParentModel().isHistoryShown();
				updateHistory(b);
				model.getParentModel().showHistory(b);
				break;
			case RESET_DEFAULT:
				model.getParentModel().resetDefaultRndSettings();
				break;
			case SAVE:
				model.getParentModel().saveRndSettings();
				break;
		}
	}

}
