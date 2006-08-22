/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindPaneUI
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder;




//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;
import javax.swing.BorderFactory;
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

//Third-party libraries

//Application-internal dependencies

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
{
    
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
    
    /** The owner of this UI delegate. */
    private FindPane            model;
    
    /** Initializes the UI components composing the display. */
    private void initComponents()
    {
        titleLabel = new JLabel(FIND_MSG+DEFAULT);
        treeHolderPanel = new JPanel();
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
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
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
    void setFoundResults(Set results)
    {
        FindResultsPane p = new FindResultsPane(model, results);
        setMessage(p.getSizeResults());
        treeHolderPanel.removeAll();
        treeHolderPanel.add(new JScrollPane(p), BorderLayout.CENTER);
        treeHolderPanel.revalidate();
    }
    
}
