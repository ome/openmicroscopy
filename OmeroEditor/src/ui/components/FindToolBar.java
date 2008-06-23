package ui.components;

/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import tree.DataField;
import ui.AbstractComponent;
import ui.IModel;
import ui.XMLView;
import util.ImageFactory;

/**
 * This class defines a small tool-bar containing a "find" UI.
 * Initially it displays only a single "find" button.
 * When this is clicked, a text-field is revealed (and a hide button), as well as a hit-counter and
 * 2 buttons for moving up and down through the search results. 
 * 
 * 
 * 
 * @author will
 *
 */
public class FindToolBar 
	extends JPanel 
	implements 
	ChangeListener, 
	ActionListener
	{
	
	XMLView view;
	IModel model;
	
	// always visible
	JButton findButton;
	
	// other controls in horizontalBox
	Box horizontalBox;
	JTextField findTextField;
	JButton findNextButton;
	JButton findPrevButton;
	JLabel hitsCountLabel;
	
	Icon findIcon;
	Icon fileCloseIcon;
	Icon previousUpIcon;
	Icon nextDownIcon;
	
	List<DataField> findTextSearchHits = new ArrayList<DataField>();
	int findTextHitIndex; 	// the current hit
	
	public static final String HIDE_FIND_BOX = "HideFindBox";
	public static final String NEXT_FIND_HIT = "NextFindHit";
	public static final String PREV_FIND_HIT = "PrevFindHit";
	
	public FindToolBar(XMLView view, IModel model) {
		
		this.model = model;
		
		if (model instanceof AbstractComponent) {
			((AbstractComponent)model).addChangeListener(this);
		}
		
		this.view = view;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		buildUI();
	}
	
	public void buildUI() {
		
		Border fileManagerToolBarBorder = new EmptyBorder(2,5,2,5);
		
		
		findIcon = ImageFactory.getInstance().getIcon(ImageFactory.FIND_ICON);
		fileCloseIcon = ImageFactory.getInstance().getIcon(ImageFactory.FILE_CLOSE_ICON);
		previousUpIcon = ImageFactory.getInstance().getIcon(ImageFactory.PREVIOUS_UP_ICON);
		nextDownIcon = ImageFactory.getInstance().getIcon(ImageFactory.NEXT_DOWN_ICON);
		
		
		findButton = new JButton(findIcon);
		findButton.setToolTipText("Find a word in the current file");
		findButton.setBorder(fileManagerToolBarBorder);
		findButton.addActionListener(this);
		
		findTextField = new JTextField();
		findTextField.setColumns(10);
		Dimension findTextFieldDim = new Dimension(200, 22);
		findTextField.setMinimumSize(findTextFieldDim);
		findTextField.setMaximumSize(findTextFieldDim);
		findTextField.setPreferredSize(findTextFieldDim);
		findTextField.addActionListener(this);
		
		JButton hideFindBoxButton = new JButton(fileCloseIcon);
		hideFindBoxButton.setToolTipText("Hide the 'Find' text field");
		hideFindBoxButton.setActionCommand(HIDE_FIND_BOX);
		hideFindBoxButton.addActionListener(this);
		hideFindBoxButton.setBorder(new EmptyBorder(1,1,1,1));
		
		findNextButton = new JButton(nextDownIcon);
		findNextButton.setToolTipText("Find next occurrence of search word");
		findNextButton.setActionCommand(NEXT_FIND_HIT);
		findNextButton.addActionListener(this);
		findNextButton.setBorder(new EmptyBorder(1,1,1,1));
		findNextButton.setBackground(null);
		
		findPrevButton = new JButton(previousUpIcon);
		findPrevButton.setToolTipText("Find previous occurrence of search word");
		findPrevButton.setActionCommand(PREV_FIND_HIT);
		findPrevButton.addActionListener(this);
		findPrevButton.setBackground(null);
		findPrevButton.setBorder(new EmptyBorder(1,1,1,1));
		
		hitsCountLabel = new JLabel("");
		hitsCountLabel.setBorder(new EmptyBorder(1,1,1,1));
		
		// add components - most are inside the hide/show horizontalBox
		this.add(findButton);
		horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(hideFindBoxButton);
		horizontalBox.add(findTextField);
		horizontalBox.add(findPrevButton);
		horizontalBox.add(findNextButton);
		horizontalBox.add(hitsCountLabel);
		this.setBorder(new EmptyBorder(1,1,1,1));
		this.add(horizontalBox);
		horizontalBox.setVisible(false);
		
	}
	

	public void actionPerformed(ActionEvent event) {
		
		// first use of this function is to display textField if hidden - 
		// search is also performed (see below) in case the file was edited 
		if (!horizontalBox.isVisible()) {
			showFindTextControls();
		}
		String actionCommand = event.getActionCommand();
		// actionEvent from hide button, hide 
		if (actionCommand.equals(HIDE_FIND_BOX)) {
			hideFindTextControls();
		} else if (actionCommand.equals(NEXT_FIND_HIT)) {
			findTextHitIndex++;
			DataField field = findTextSearchHits.get(findTextHitIndex);
			displayThisDataField(field);
			updateFindNextPrev();
		} else if (actionCommand.equals(PREV_FIND_HIT)) {
			findTextHitIndex--;
			DataField field = findTextSearchHits.get(findTextHitIndex);
			displayThisDataField(field);
			updateFindNextPrev();
		}
		// otherwise, do search
		else findTextInOpenFile();
	}
	
	
	public void displayThisDataField(DataField field) {
		view.displayThisDataField(field);
	}
	
	/*
	 * "Find" function to highlight fields containing a search-word. 
	 */
	public void findTextInOpenFile() {

		String searchWord = findTextField.getText().trim();
		if (searchWord.length() == 0) {
			findTextSearchHits.clear();
			// update the findNext and findPrev buttons
			updateFindNextPrev();
			return;
		}
		
		
		findTextSearchHits = model.getSearchResults(searchWord);
			
		findTextHitIndex = 0;
		
		// if you have at least one result, display it
		if (findTextSearchHits != null && !findTextSearchHits.isEmpty()) {

			DataField field = findTextSearchHits.get(findTextHitIndex);
			displayThisDataField(field);
		}
		// update the findNext and findPrev buttons
		updateFindNextPrev();
	}
	
	
	/* hide the findTextControlls - eg when user closes panel */
	public void hideFindTextControls() {
		horizontalBox.setVisible(false);
	}
	/* show the findTextControls */
	public void showFindTextControls() {
		horizontalBox.setVisible(true);
		this.validate();
		findTextField.requestFocusInWindow();
	}
	
	public void updateFindNextPrev() {
		hitsCountLabel.setText(findTextSearchHits.size() + ((findTextSearchHits.size()==1) ? " hit": " hits" ));
		if (findTextSearchHits.isEmpty()) {
			findNextButton.setEnabled(false);
			findPrevButton.setEnabled(false);
			return;
		}
		
		findNextButton.setEnabled(findTextSearchHits.size() > findTextHitIndex+1);
		findPrevButton.setEnabled(findTextHitIndex > 0);	
	}
	
	/* clear the search results - eg when user edits file */
	public void clearFindTextHits() {
		findTextSearchHits.clear();
		updateFindNextPrev();
	}

	// if the xml has been edited (treeNeedsRefreshing) clear search results
	public void stateChanged(ChangeEvent e) {
		if (model.treeNeedsRefreshing()) {
			clearFindTextHits();
		}
		
		String[] fileList = model.getOpenFileList();
		
		findButton.setEnabled(!(fileList.length == 0));
		findTextField.setEnabled(!(fileList.length == 0));
		
	}

}
