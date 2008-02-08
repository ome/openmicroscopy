package ui.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


import tree.DataField;
import ui.XMLView;
import util.ImageFactory;

public class FindToolBar extends JPanel {
	
	XMLView view;
	
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
	
	ArrayList<DataField> findTextSearchHits = new ArrayList<DataField>();
	int findTextHitIndex; 	// the current hit
	
	public static final String HIDE_FIND_BOX = "HideFindBox";
	public static final String NEXT_FIND_HIT = "NextFindHit";
	public static final String PREV_FIND_HIT = "PrevFindHit";
	
	public FindToolBar(XMLView view) {
		
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
		
		
		ActionListener findTextListener = new FindTextListener();
		findButton = new JButton(findIcon);
		findButton.setToolTipText("Find a word in the current file");
		findButton.setBorder(fileManagerToolBarBorder);
		findButton.addActionListener(findTextListener);
		
		findTextField = new JTextField();
		findTextField.setColumns(10);
		Dimension findTextFieldDim = new Dimension(200, 22);
		findTextField.setMinimumSize(findTextFieldDim);
		findTextField.setMaximumSize(findTextFieldDim);
		findTextField.setPreferredSize(findTextFieldDim);
		findTextField.addActionListener(findTextListener);
		
		JButton hideFindBoxButton = new JButton(fileCloseIcon);
		hideFindBoxButton.setToolTipText("Hide the 'Find' text field");
		hideFindBoxButton.setActionCommand(HIDE_FIND_BOX);
		hideFindBoxButton.addActionListener(findTextListener);
		hideFindBoxButton.setBorder(new EmptyBorder(1,1,1,1));
		
		findNextButton = new JButton(nextDownIcon);
		findNextButton.setToolTipText("Find next occurrence of search word");
		findNextButton.setActionCommand(NEXT_FIND_HIT);
		findNextButton.addActionListener(findTextListener);
		findNextButton.setBorder(new EmptyBorder(1,1,1,1));
		findNextButton.setBackground(null);
		
		findPrevButton = new JButton(previousUpIcon);
		findPrevButton.setToolTipText("Find previous occurrence of search word");
		findPrevButton.setActionCommand(PREV_FIND_HIT);
		findPrevButton.addActionListener(findTextListener);
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
	
	/* takes all events from the find text functionality */
	public class FindTextListener implements ActionListener {
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
		
		
		findTextSearchHits = view.getSearchResults(searchWord);
			
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

}
