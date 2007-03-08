/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindPaneUI
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
 *------------------------------------------------------------------------------s
 */

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder;




//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import layout.TableLayout;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The UI delegate for the {@link FindPane}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class FindPaneUI
    extends JPanel
    implements MouseListener, PropertyChangeListener
{
	
    /** The horizontal space. */
    private static final Dimension H_SPACE_DIM = new Dimension(10, 5);
    
    /** The message displayed when the phrase isn't found. */
    private static final String     NO_PHRASE_MSG = "Phrase not found.";
    
    /** The message displayed when the phrase is found for than one time. */
    private static final String     OCCURENCES_MSG = " occurences found";
    
    /** The message displayed when the phrase is found 0 or 1 time. */
    private static final String     OCCURENCE_MSG = " occurence found";
    
    /** The default text of the title border. */
    private static final String     DEFAULT = "No selected context";
    
    /** The message presenting the context of the find action. */
    private static final String     FIND_MSG = "Find in: ";
    
    /** The message presenting the results found. */
    private static final String     RESULTS_MSG = "Results: ";
    
    /** The find context show when the root node is selected. */
    static final String             IN_ALL_MSG = "browser";
    
    /** The default width of the {@link #findArea}. */
    private static final int        WIDTH = 150;
    
    /** The label presenting the annotation context. */
    private JLabel              titleLabel;
    
    /** The panel hosting the controls. */
    private JPanel              controlsPanel;
    
    /** The text area hosting the pattern to find. */
    private JTextField          findArea;
    
    /** Check box to match the case or not. */
    private JCheckBox           caseSensitive;
    
    /** The panel hosting the tree displaying the result. */
    private JPanel              treeHolderPanel;
    
    /** Finds the next occurence of the found phrase. */
    private JButton				findNextButton;
    
    /** Finds the previous occurence of the found phrase. */
    private JButton				findPreviousButton;
    
    /** The UI component displaying the found nodes. */
    private FindResultsPane		resultsPane;
    
    /** The owner of this UI delegate. */
    private FindPane            model;
    
    /** The scrollPane hosting the results. */
    private JScrollPane			scrollPane;
    
    /** The results message. */
    private JLabel				resultMessage;
    
    /** Initializes the UI components composing the display. */
    private void initComponents()
    {
    	findNextButton = new JButton(new FindNextAction(model));
    	findPreviousButton = new JButton(new FindPreviousAction(model));
        titleLabel = new JLabel(FIND_MSG+DEFAULT);
        scrollPane = new JScrollPane();
        treeHolderPanel = new JPanel();
        resultMessage = new JLabel(RESULTS_MSG);
        treeHolderPanel.setBorder(new TitledBorder(RESULTS_MSG));
        //treeHolderPanel.setLayout(new BorderLayout()); 
        caseSensitive = new JCheckBox("Match case");
        caseSensitive.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JCheckBox source = (JCheckBox) e.getSource();
                model.setCaseSensitive(source.isSelected());
            }
        });
        findArea = new JTextField();
        findArea.setBorder(BorderFactory.createBevelBorder(
                            BevelBorder.LOWERED));
        findArea.setBackground(Color.WHITE);
        findArea.setOpaque(true);
        findArea.setEditable(true);
        int h = getFontMetrics(getFont()).getHeight()+4;
        findArea.setPreferredSize(new Dimension(WIDTH, h));
        findArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e)
            {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER)) {
                    Object source = e.getSource();
                    if (source instanceof JTextField) {
                        JTextField f = (JTextField) source;
                        String text = f.getText();
                        if (text != null && text.trim().length() > 0) 
                            model.find();
                    }
                }
            }
        });
        
        findArea.getDocument().addDocumentListener(new DocumentListener() {
            
            /**
             * Sets the finder's controls enabled if no phrase entered.
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
                Document d = de.getDocument();
                try {
                    model.setTextUpdate(d.getText(0, d.getLength()));
                } catch (Exception e) {}
            }

            /**
             * Sets the finder's controls enabled if no phrase entered.
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            { 
                Document d = de.getDocument();
                try {
                    model.setTextUpdate(d.getText(0, d.getLength()));
                } catch (Exception e) {}
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        });
    }
    
    /** 
     * Helper method to create a menu bar. 
     *     
     * @return The toolbar hosting the controls. 
     */
    private JToolBar createRightMenuBar()
    {
        JToolBar controlsBar = new JToolBar();
        controlsBar.setBorder(null);
        controlsBar.setRollover(true);
        controlsBar.setFloatable(false);
        controlsBar.add(findNextButton);
        controlsBar.add(findPreviousButton);
        FilterMenuAction action = new FilterMenuAction(model);
        JButton button = new JButton(action);
        button.addMouseListener(action);
        controlsBar.add(button);
        return controlsBar;
    }
    
    /** 
     * Helper method to create a menu bar. 
     * 
     * @return The toolbar hosting the controls. 
     */
    private JToolBar createLeftMenuBar()
    {
        JToolBar controlsBar = new JToolBar();
        controlsBar.setBorder(null);
        controlsBar.setRollover(true);
        controlsBar.setFloatable(false);
        JButton button = new JButton(new ClearAction(model));
        controlsBar.add(button);
        return controlsBar;
    }
    
    /** Builds and lays out the UI if the clipBoard is horizontal. */
    private void buildGUIHorizontalSplit()
    {
    	GridBagConstraints c = new GridBagConstraints();      

    	// create tree holder panel, which will be displayed to the right of the
    	// find window
        treeHolderPanel = new JPanel();
        treeHolderPanel.setBorder(new TitledBorder(RESULTS_MSG));
        treeHolderPanel.setLayout(new BorderLayout());  
        
        // create select panel which will contain find text entry box, match 
        // case check box 
        JPanel selectPanel = new JPanel();
        selectPanel.add(createLeftMenuBar());
        selectPanel.add(Box.createRigidArea(H_SPACE_DIM));
        selectPanel.add(new JLabel("Find: "));
        selectPanel.add(findArea);
        selectPanel.add(createRightMenuBar());
        selectPanel.add(caseSensitive);
                
        // title label is the search results status bar which will be displayed 
        // at top of findpane 
        titleLabel = new JLabel(FIND_MSG+DEFAULT);
        
        // controls panel is the container for all search criterion and title
        // label. It will use a gridbaglayout and have title followed by
        // separator then find text box and match case on the same line follow-
        // -ing it. 
        controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridBagLayout());
        
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;
        c.gridy = 1;
        controlsPanel.add(titleLabel, c);
        
        c.gridy = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 10);
        
        controlsPanel.add(new JSeparator(), c);
        c.ipadx = 0; // reset ipadx = 20;
        
        c.gridy = 3;
        controlsPanel.add(selectPanel, c);
        c.insets = new Insets(0, 0, 0, 0);
        // the find pane itself has a gridbag layout and will incorporate the
        // controlsPanel on the left and search results (treeHolder) on the 
        // right
        setLayout(new GridBagLayout());
        
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.1;
        c.weighty = 0.5;
        add(controlsPanel, c);
        
        // add tree panel
        c.weightx = 100;
        c.fill = GridBagConstraints.BOTH;
        add(treeHolderPanel, c);
    }
    
    /** Builds and lays out the UI if the clipBoard is vertical. */
    private void buildGUIVerticalSplit()
    {
    	JPanel rowOne = new JPanel();
    	rowOne.add(createLeftMenuBar());
    	rowOne.add(Box.createRigidArea(H_SPACE_DIM));
    	rowOne.add(new JLabel("Find: "));
    	rowOne.add(findArea);
        
        JPanel rowTwo = new JPanel();
        rowTwo.add(createRightMenuBar());
        rowTwo.add(caseSensitive);
        
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.add(UIUtilities.buildComponentPanel(rowOne));
        rows.add(rowTwo);
        //title label is the search results status bar which will be displayed 
        // at top of findpane 
        titleLabel = new JLabel(FIND_MSG+DEFAULT);
        
//      controls panel is the container for all search criterion and title
        // label. It will use a gridbaglayout and have title followed by
        // separator then find text box and match case on the same line follow-
        // -ing it. 
        /*
        controlsPanel.setLayout(new GridBagLayout());
        
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;
        c.gridy = 1;
        controlsPanel.add(titleLabel, c);
        
        c.gridy = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 10);
        
        controlsPanel.add(new JSeparator(), c);
        c.ipadx = 0; // reset ipadx = 20;
        
        c.gridy = 3;
        controlsPanel.add(UIUtilities.buildComponentPanel(rows), c);
        c.insets = new Insets(0, 0, 0, 0);
        // the find pane itself has a gridbag layout and will incorporate the
        // controlsPanel on the left and search results (treeHolder) on the 
        // right
         * */
        /*
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();  
        // griddy constraints
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.1;
        c.weighty = 0.5;
        c.gridy = 0;
        add(controlsPanel, c);
        c.gridy = 1;
        // add tree panel
        c.weightx = 100;
        c.fill = GridBagConstraints.BOTH;
        add(treeHolderPanel, c);
        */
        double[][] tl = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 
        		TableLayout.PREFERRED, 5, 300}}; 
        setLayout(new TableLayout(tl));
        add(UIUtilities.buildComponentPanel(titleLabel), "0, 0, f, t");
        add(new JSeparator(), "0, 1, f, t");
        add(UIUtilities.buildComponentPanel(rows), "0, 2, f, t");
        add(UIUtilities.buildComponentPanel(resultMessage), "0, 3, f, t");
        add(new JSeparator(), "0, 4, f, t");
        add(scrollPane, "0, 5");
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	if (ClipBoard.HORIZONTAL_SPLIT) buildGUIHorizontalSplit();
    	else buildGUIVerticalSplit();
    }
    
    /**
     * Displays a message depending on the value of the specified parameter.
     * 
     * @param n The number of found nodes.
     */
    private void setMessage(int n)
    {
        String s = RESULTS_MSG;
        if (n == 0) s += NO_PHRASE_MSG; 
        else if (n > 0) {
            if (n > 1) s += n+OCCURENCES_MSG;
            else s += n+OCCURENCE_MSG;
        }
        resultMessage.setText(s);
        treeHolderPanel.setBorder(new TitledBorder(s));
    }
    
    /**
     * Creates a new UI delegate for the specified <code>owner</code>.
     * 
     * @param model The component that will own this UI delegate.  
     *              Mustn't be <code>null</code>.
     */
    FindPaneUI(FindPane model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
        buildGUI();
        onSelectedDisplay(true, IN_ALL_MSG);
    }

    /** Clears the results of a previous find action. */
    void clear()
    {
        setMessage(-1);
        findArea.setText("");
        treeHolderPanel.removeAll();
        treeHolderPanel.repaint();
    }
    
    /**
     * Updates the UI when a new node is selected in the browser.
     * 
     * @param findText  Passed <code>true</code> if the {@link #findArea} is
     *                  editable. 
     * @param context   The context of the find. If <code>null</code>, the 
     *                  the {@link #DEFAULT} message is shown.
     */
    void onSelectedDisplay(boolean findText, String context)
    {
        findArea.setEditable(findText);
        if (context == null) titleLabel.setText(FIND_MSG+DEFAULT);
        else titleLabel.setText(FIND_MSG+context);
    }
    
    /**
     * Displays the results of the find action.
     * 
     * @param results The collection of nodes to display.
     */ 
    void setFoundResults(List results)
    {
    	onFoundOccurences(results.size());
    	resultsPane = new FindResultsPane(model, results);
        setMessage(resultsPane.getSizeResults());
       
        scrollPane.getViewport().add(resultsPane);
        if (ClipBoard.HORIZONTAL_SPLIT) {
        	 treeHolderPanel.removeAll();
             treeHolderPanel.add(scrollPane, BorderLayout.CENTER);
             treeHolderPanel.revalidate();
        }
    }

    /** Finds the next occurence. */
	void findNext()
	{
		if (resultsPane != null) resultsPane.findNext();
	}
	
	 /** Finds the next occurence. */
	void findPrevious()
	{
		if (resultsPane != null) resultsPane.findPrevious();
	}
    
    /** Reacts to text entered. */
    void onTextSelected()
    {
    	boolean b = model.isTextEmpty();
    	findPreviousButton.setEnabled(b);
    	findNextButton.setEnabled(b);
    }
    
    /** Reacts to level selection. */
    void onLevelChanged()
    {
    	boolean b = true;
    	if (!model.isNameSelected() && !model.isDescriptionSelected()) 
    		b = false;
    	findPreviousButton.setEnabled(b);
    	findNextButton.setEnabled(b); 
    }
    
    /**
     * Reacts to the number of found occurences of the phrases.
     * 
     * @param n The number of found occurences.
     */
    void onFoundOccurences(int n)
    {
    	findPreviousButton.setEnabled(n !=0);
    	findNextButton.setEnabled(n !=0); 
    }
    
    /**
     * Reacts to property fired by the {@link HistoryDialog}.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent) 
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        findArea.setText((String) evt.getNewValue());
    }
    
    /**
     * Displays the previously searched values.
     * @see MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e)
    {
        if (findArea.getDocument().getLength() == 0) {
            String[] h = model.getHistory();
            if (h.length != 0) {
                Rectangle r = findArea.getBounds();
                HistoryDialog d = new HistoryDialog(h, r.width);
                d.show(findArea, 0, r.height);
                d.addPropertyChangeListener(HistoryDialog.SELECTION_PROPERTY, 
                                            this);
            }   
        }
    }
    
    /**
     * Required by the {@link MouseListener} I/F but no-op in our case.
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * Required by the {@link MouseListener} I/F but no-op in our case.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {}

    /**
     * Required by the {@link MouseListener} I/F but no-op in our case.
     * @see MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Required by the {@link MouseListener} I/F but no-op in our case.
     * @see MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {}
    
}
