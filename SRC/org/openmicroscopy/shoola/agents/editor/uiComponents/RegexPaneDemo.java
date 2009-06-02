 /*
 * jEditorPane.regexEditor.RegexPaneDemo 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports

//Third-party libraries

//Application-internal dependencies

/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import org.openmicroscopy.shoola.util.ui.omeeditpane.ChemicalNameFormatter;
import org.openmicroscopy.shoola.util.ui.omeeditpane.ChemicalSymbolsEditer;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMERegexFormatter;
import org.openmicroscopy.shoola.util.ui.omeeditpane.Position;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiView;

public class RegexPaneDemo extends JFrame 
	implements DocumentListener {
	
	Map <String, SimpleAttributeSet> attrs;
	
	SimpleAttributeSet plainText;
	
	public static final String PARAM_REGEX = "\\[.*?\\]";
	
	/**
	 * Formatter for changing appearance of text according to regex matching
	 */
	private OMERegexFormatter 			regexFormatter;
	
	private ChemicalNameFormatter 		chemicalNameFormatter;
	
	private ChemicalSymbolsEditer	chemicalSymbolEditer;
	
	/** List of the positions of parameters */
	private	List<Position> paramPositionList;
	
	/** Map holding the point(start and end) locations of the 
	 * parameters, as Identified by ID */
	private Map<Position, Integer>		paramLocations; 	
	
	/** Allow user to turn on/off the auto editing of symbols. */
	private JCheckBoxMenuItem			toggleSymbolEdit;
	
    JTextPane textPane;
    StyledDocument doc;
    static final int MAX_CHARACTERS = 300;
    JTextArea changeLog;
    String newline = "\n";
    HashMap<Object, Action> actions;

    //undo helpers
    protected UndoAction undoAction;
    protected RedoAction redoAction;
    protected UndoManager undo = new UndoManager();

    public RegexPaneDemo() {
        super("TextComponentDemo");

        //Create the text pane and configure it.
        textPane = new JTextPane();
        textPane.setCaretPosition(0);
        textPane.setMargin(new Insets(5,5,5,5));
        
        doc = textPane.getStyledDocument();
        
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        //Create the text area for the status log and configure it.
        changeLog = new JTextArea(5, 30);
        changeLog.setEditable(false);
        JScrollPane scrollPaneForLog = new JScrollPane(changeLog);

        //Create a split pane for the change log and the text area.
        JSplitPane splitPane = new JSplitPane(
                                       JSplitPane.VERTICAL_SPLIT,
                                       scrollPane, scrollPaneForLog);
        splitPane.setOneTouchExpandable(true);

        //Create the status area.
        JPanel statusPane = new JPanel(new GridLayout(1, 1));
        

        //Add the components.
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusPane, BorderLayout.PAGE_END);

        //Set up the menu bar.
        actions=createActionTable(textPane);
        JMenu editMenu = createEditMenu();
        JMenu styleMenu = createStyleMenu();
        JMenuBar mb = new JMenuBar();
        mb.add(editMenu);
        mb.add(styleMenu);
        setJMenuBar(mb);

        //Add some key bindings.
        addBindings();

        //Put the initial text into the text pane.
        plainText = new SimpleAttributeSet();
        StyleConstants.setFontFamily(plainText, "SansSerif");
        StyleConstants.setFontSize(plainText, 14);
        initDocument();
        textPane.setCaretPosition(0);

        //Start watching for undoable edits and caret changes.
        doc.addUndoableEditListener(new MyUndoableEditListener());
        doc.addDocumentListener(this);
        
        regexFormatter = new OMERegexFormatter(plainText);
        
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, Color.blue);
        StyleConstants.setBold(set, true);
        regexFormatter.addRegex(PARAM_REGEX, set);
        regexFormatter.parseRegex(doc, false);
        
        chemicalNameFormatter = new ChemicalNameFormatter(plainText);
        chemicalSymbolEditer = new ChemicalSymbolsEditer(plainText); 
        
        // initialise lists of param locations etc. 
  
        paramLocations = new HashMap<Position, Integer>();
        paramPositionList = new ArrayList<Position>();
        
        // updates the list of existing regex matches.
        String text = "";
        try {
			text = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        WikiView.findExpressions(text, PARAM_REGEX, paramPositionList);
    	paramLocations = new HashMap<Position, Integer>();
    	int i = 0;
    	for (Position p: paramPositionList) {
    		paramLocations.put(p, i++);
    	}
    }

    

    //This one listens for edits that can be undone.
    protected class MyUndoableEditListener
                    implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {
            //Remember the edit and update the menus.
            undo.addEdit(e.getEdit());
            undoAction.updateUndoState();
            redoAction.updateRedoState();
        }
    }

    
    public void insertUpdate(DocumentEvent e) {
    	changeLog.append("INSERT-UPDATE\n");
    	parseRegex(e.getOffset());
    }
    public void removeUpdate(DocumentEvent e) {
    	changeLog.append("REMOVE-UPDATE\n");
    	parseRegex(e.getOffset());
    }
    public void changedUpdate(DocumentEvent e) {
    	// changeLog.append("CHANGE-UPDATE\n");
    	// parseRegex(e, CHANGE_UPDATE);
    }
    
    
    /**
     * Called whenever the document is edited
     * Parses regex. 
     */
    private void parseRegex(int index)
    {
    	
    	// now apply formatting according to regex patterns
    	chemicalNameFormatter.parseRegex(doc, true);	// true: clear formatting
    	regexFormatter.parseRegex(doc, false);	// false: don't clear formatting
    	
    	// edit symbols according to regex
    	chemicalSymbolEditer.parseRegex(doc, 
    						(toggleSymbolEdit.isSelected() ? index : 0));
    	
    	String text = "";
    	
    	try {
			text = doc.getText(0, doc.getLength());
		} catch (BadLocationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	
		List<Position> newPositionList = new ArrayList<Position>();
		WikiView.findExpressions(text, PARAM_REGEX, newPositionList);
		
    	// need to know if and which parameter removed. 
		if (paramPositionList.size() > newPositionList.size()) {
			changeLog.append("Param deleted!\n");
    	
		} else if (paramPositionList.size() < newPositionList.size()) {
			changeLog.append("Param created!\n");
		} else {
			// check if edit is within a param
			
			Iterator<Position> 
			positionIterator = paramLocations.keySet().iterator();
			Position p;
			String regex;
			while (positionIterator.hasNext())
			{
				p = positionIterator.next();
				if (p.contains(index, index))
				{
					changeLog.append("Param "+ paramLocations.get(p) + " Edited!\n");
				}
			} 
		}
			
    	paramPositionList = newPositionList;	
    	
    	/* Make a fake list */
    	paramLocations = new HashMap<Position, Integer>();
    	int i = 0;
    	for (Position p: paramPositionList) {
    		paramLocations.put(p, i++);
    	}
    }

    //Add a couple of emacs key bindings for navigation.
    protected void addBindings() {
        InputMap inputMap = textPane.getInputMap();

        //Ctrl-b to go backward one character
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.backwardAction);

        //Ctrl-f to go forward one character
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.forwardAction);

        //Ctrl-p to go up one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.upAction);

        //Ctrl-n to go down one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.downAction);
    }

    //Create the edit menu.
    protected JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");

        //Undo and redo are actions of our own creation.
        undoAction = new UndoAction();
        menu.add(undoAction);

        redoAction = new RedoAction();
        menu.add(redoAction);

        menu.addSeparator();

        //These actions come from the default editor kit.
        //Get the ones we want and stick them in the menu.
        menu.add(getActionByName(DefaultEditorKit.cutAction));
        menu.add(getActionByName(DefaultEditorKit.copyAction));
        menu.add(getActionByName(DefaultEditorKit.pasteAction));

        menu.addSeparator();
        
        toggleSymbolEdit = new JCheckBoxMenuItem("Auto-Edit Symbols");
        toggleSymbolEdit.setSelected(true);
        menu.add(toggleSymbolEdit);
        
        menu.addSeparator();

        menu.add(getActionByName(DefaultEditorKit.selectAllAction));
        return menu;
    }

    //Create the style menu.
    protected JMenu createStyleMenu() {
        JMenu menu = new JMenu("Style");

        Action action = new StyledEditorKit.BoldAction();
        action.putValue(Action.NAME, "Bold");
        menu.add(action);

        action = new StyledEditorKit.ItalicAction();
        action.putValue(Action.NAME, "Italic");
        menu.add(action);

        action = new StyledEditorKit.UnderlineAction();
        action.putValue(Action.NAME, "Underline");
        menu.add(action);

        menu.addSeparator();

        menu.add(new StyledEditorKit.FontSizeAction("12", 12));
        menu.add(new StyledEditorKit.FontSizeAction("14", 14));
        menu.add(new StyledEditorKit.FontSizeAction("18", 18));

        menu.addSeparator();

        menu.add(new StyledEditorKit.FontFamilyAction("Serif",
                                                      "Serif"));
        menu.add(new StyledEditorKit.FontFamilyAction("SansSerif",
                                                      "SansSerif"));

        menu.addSeparator();

        menu.add(new StyledEditorKit.ForegroundAction("Red",
                                                      Color.red));
        menu.add(new StyledEditorKit.ForegroundAction("Green",
                                                      Color.green));
        menu.add(new StyledEditorKit.ForegroundAction("Blue",
                                                      Color.blue));
        menu.add(new StyledEditorKit.ForegroundAction("Black",
                                                      Color.black));

        return menu;
    }

    protected void initDocument() {
        String initString[] =
                { "Use the mouse to place the caret.",
                  "Use the [edit menu] to cut, copy, paste, and select text.",
                  "Also to undo and redo changes. 20 ul. 20 microlitre µl",
                  "Use the style menu to change the H2O of the text with MgCl2.",
                  "Use the arrow keys on the keyboard or these emacs key bindings to move the caret:",
                  "Ctrl-f, Ctrl-b, Ctrl-n, Ctrl-p." };

        
        try {
            for (int i = 0; i < initString.length; i ++) {
            	SimpleAttributeSet set = plainText;
                doc.insertString(doc.getLength(), initString[i] + newline, set);
            }
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text.");
        }
    }

    

    //The following two methods allow us to find an
    //action provided by the editor kit by its name.
    private HashMap<Object, Action> createActionTable(JTextComponent textComponent) {
        HashMap<Object, Action> actions = new HashMap<Object, Action>();
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
	return actions;
    }

    private Action getActionByName(String name) {
        return actions.get(name);
    }

    class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        final RegexPaneDemo frame = new RegexPaneDemo();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    //The standard main method.
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
	        UIManager.put("swing.boldMetal", Boolean.FALSE);
		createAndShowGUI();
            }
        });
    }
}


