/*
 * ome.formats.importer.GuiCommonElements
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer.util;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import layout.TableLayout;


/**
 * Uses TableLayout (see java website)
 * @author TheBrain
 *
 */
public class GuiCommonElements
{
    
    public JPanel addMainPanel(Container container, double tableSize[][], 
            int margin_top, int margin_left, int margin_bottom, int margin_right,
            boolean debug)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
              
        TableLayout layout = new TableLayout(tableSize);
        panel.setLayout(layout);       
        panel.setBorder(BorderFactory.createEmptyBorder(margin_top,margin_left,
                margin_bottom,margin_right));

        if (debug == true)
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                panel.getBorder()));
        
        return panel;
    }
    
    public JTextPane addTextPane(Container container, String text, 
            String placement, boolean debug)
    {
        StyleContext context = new StyleContext();
        StyledDocument document = new DefaultStyledDocument(context);

        Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);

        try
        {
            document.insertString(document.getLength(), text, style);
        } catch (BadLocationException e)
        {
            System.err
                    .println("BadLocationException inserting text to document.");
        }

        JTextPane textPane = new JTextPane(document);
        textPane.setOpaque(false);
        textPane.setEditable(false);
        textPane.setFocusable(false);
        container.add(textPane, placement);
        
        if (debug == true)
        textPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                textPane.getBorder()));

        return textPane;
    }

    public JTextField addTextField(Container container, String name,
            String initialValue, int mnemonic, String tooltip, 
            String suffix, double labelWidth, String placement, boolean debug)
    {

        double[][] size = null;
        
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        if (suffix == "")
            size = new double[][]{{labelWidth,TableLayout.FILL},{30}};
        else
            size = new double[][] 
                   {{labelWidth,TableLayout.FILL, TableLayout.PREFERRED},{30}};
     
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);       

        JLabel label = new JLabel(name);
        label.setDisplayedMnemonic(mnemonic);
        
        JTextField result = new JTextField(20);
        label.setLabelFor(result);
        label.setOpaque(false);
        result.setToolTipText(tooltip);
        if (initialValue != null) result.setText(initialValue);


        panel.add(label, "0, 0, r, c");        
        panel.add(result, "1, 0, f, c");

        if (suffix.length() != 0)
        {
            JLabel suffixLabel = new JLabel(" " + suffix);
            panel.add(suffixLabel, "2,0, l, c");
        }
            
        
        if (debug == true)
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                panel.getBorder()));
        
        container.add(panel, placement);
        return result;
    }

    public JTextArea addTextArea(Container container, String name, 
            String text, int mnemonic, String placement, boolean debug)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        double size[][] = {{TableLayout.FILL},{20, TableLayout.FILL}};
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);
        
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        
        JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        if (debug == true)
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                panel.getBorder()));
        
        if (name != "")
        {
            JLabel label = new JLabel(name);
            label.setOpaque(false);
            label.setDisplayedMnemonic(mnemonic);
            panel.add(label, "0, 0, l, c");
            panel.add(areaScrollPane, "0, 1, f, f");
        } else {
            panel.add(areaScrollPane, "0, 0, 0, 1");            
        }
        

        container.add(panel, placement);
        
        return textArea;
    }

    public JTextPane addTextPane(Container container, String name, 
            String text, int mnemonic, String placement, boolean debug)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        double size[][] = {{TableLayout.FILL},{20, TableLayout.FILL}};
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);
        
        StyleContext context = new StyleContext();
        StyledDocument document = new DefaultStyledDocument(context);

        JTextPane textPane = new JTextPane(document);
        textPane.setOpaque(false);

        // Create one of each type of tab stop
        java.util.List list = new ArrayList();
        
        // Create a left-aligned tab stop at 100 pixels from the left margin
        float pos = 15;
        int align = TabStop.ALIGN_LEFT;
        int leader = TabStop.LEAD_NONE;
        TabStop tstop = new TabStop(pos, align, leader);
        list.add(tstop);
        
        // Create a right-aligned tab stop at 200 pixels from the left margin
        pos = 15;
        align = TabStop.ALIGN_RIGHT;
        leader = TabStop.LEAD_NONE;
        tstop = new TabStop(pos, align, leader);
        list.add(tstop);
        
        // Create a center-aligned tab stop at 300 pixels from the left margin
        pos = 15;
        align = TabStop.ALIGN_CENTER;
        leader = TabStop.LEAD_NONE;
        tstop = new TabStop(pos, align, leader);
        list.add(tstop);
        
        // Create a decimal-aligned tab stop at 400 pixels from the left margin
        pos = 15;
        align = TabStop.ALIGN_DECIMAL;
        leader = TabStop.LEAD_NONE;
        tstop = new TabStop(pos, align, leader);
        list.add(tstop);
        
        // Create a tab set from the tab stops
        TabStop[] tstops = (TabStop[])list.toArray(new TabStop[0]);
        TabSet tabs = new TabSet(tstops);
        
        // Add the tab set to the logical style;
        // the logical style is inherited by all paragraphs
        Style style = textPane.getLogicalStyle();
        StyleConstants.setTabSet(style, tabs);
        textPane.setLogicalStyle(style);
        
        JScrollPane areaScrollPane = new JScrollPane(textPane);
        areaScrollPane.
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        if (debug == true)
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                panel.getBorder()));
        
        if (name != "")
        {
            JLabel label = new JLabel(name);
            label.setDisplayedMnemonic(mnemonic);
            panel.add(label, "0, 0, l, c");
            panel.add(areaScrollPane, "0, 1, f, f");
        } else {
            panel.add(areaScrollPane, "0, 0, 0, 1");            
        }
        

        container.add(panel, placement);
        
        return textPane;
    }

    /**
     * @param doc
     * @param style
     * @param text
     * appends text to the end of a styled document
     */
    public void appendTextToDocument(StyledDocument doc, Style style, String text)
    {
        try
        {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }
    
    public JButton addButton(Container container, String label,
            int mnemonic, String tooltip, String placement, boolean debug)
    {
        JButton button = new JButton(label);
        button.setMnemonic(mnemonic);
        button.setOpaque(false);
        container.add(button, placement);
        
        if (debug == true)
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.red),
                    button.getBorder()));
        
        return button;
    }
    
    // Fires a button click when using the enter key
    public void enterPressesWhenFocused(JButton button) {

        button.registerKeyboardAction(
            button.getActionForKeyStroke(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)), 
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), 
                JComponent.WHEN_FOCUSED);

        button.registerKeyboardAction(
            button.getActionForKeyStroke(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)), 
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), 
                JComponent.WHEN_FOCUSED);

    }
}
