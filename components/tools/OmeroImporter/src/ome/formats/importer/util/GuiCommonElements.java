/*
 * ome.formats.importer.GuiCommonElements
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ome.formats.importer.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import ome.formats.importer.Main;

import layout.TableLayout;


/**
 * Uses TableLayout (see java website)
 * @author TheBrain
 *
 */
public class GuiCommonElements
{
    public boolean lafOpaque = true; // Hack for macness
    public boolean offsetButtons = false; //Another hack for macness
    public boolean motif = false;
    
    static IniFileLoader    ini = IniFileLoader.getIniFileLoader();
    public Rectangle       bounds = ini.getUIBounds();
    
    public GuiCommonElements()
    {
        String laf = UIManager.getLookAndFeel().getClass().getName();
        if (laf.equals("apple.laf.AquaLookAndFeel") 
                || laf.equals("ch.randelshofer.quaqua.QuaquaLookAndFeel")) 
        {
            lafOpaque = false;
            offsetButtons = true;
        }
        if (laf.equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel"))
        {
            motif = true;
        }
    }

    public boolean getIsMac() 
    {
        String laf = UIManager.getLookAndFeel().getClass().getName();
        if (laf.equals("apple.laf.AquaLookAndFeel") 
                || laf.equals("ch.randelshofer.quaqua.QuaquaLookAndFeel")) 
            return true;
        else
            return false;
    }
    
    // return main frame bound info
    public Rectangle getUIBounds()
    {
        return bounds;
    }
    
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
    
    public JPanel addBorderedPanel(Container container, double tableSize[][], 
            String name,
            boolean debug)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
              
        TableLayout layout = new TableLayout(tableSize);
        panel.setLayout(layout);       
        panel.setBorder(BorderFactory.createTitledBorder(name));

        if (debug == true)
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                panel.getBorder()));
        
        return panel;
    }
    
    public JPanel addPlanePanel(Container container, double tableSize[][], 
            boolean debug)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
              
        TableLayout layout = new TableLayout(tableSize);
        panel.setLayout(layout);       
        panel.setBorder(BorderFactory.createEmptyBorder());

        if (debug == true)
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.red),
                panel.getBorder()));
        
        return panel;
    }
    
    public WholeNumberField addWholeNumberField(Container container, String prefexStr,
            String initialValue, String suffexStr, int mnemonic, String tooltip,
            int maxChars, int fieldWidth, String placement, boolean debug)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        double table[][] = 
            {{TableLayout.PREFERRED, fieldWidth, 5, TableLayout.PREFERRED}, // columns
            {TableLayout.PREFERRED}}; // rows 
        
        TableLayout layout = new TableLayout(table);
        panel.setLayout(layout);  

        JLabel prefex = new JLabel(prefexStr);
        prefex.setDisplayedMnemonic(mnemonic);
        panel.add(prefex,"0,0");

        WholeNumberField result = new WholeNumberField(0, maxChars);
        result.setHorizontalAlignment(JTextField.CENTER);
        prefex.setLabelFor(result);
        result.setToolTipText(tooltip);
        if (initialValue != null) result.setText(initialValue);

        panel.add(result,"1,0");

        JLabel suffex = new JLabel(suffexStr);
        panel.add(suffex,"3,0");
        
        container.add(panel, placement);
        
        return result;
    }
    
    public DecimalNumberField addDecimalNumberField(Container container, String prefexStr,
            String initialValue, String suffexStr, int mnemonic, String tooltip,
            int maxChars, int fieldWidth, String placement, boolean debug)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        double table[][] = 
            {{TableLayout.PREFERRED, fieldWidth, 5, TableLayout.PREFERRED}, // columns
            {TableLayout.PREFERRED}}; // rows 
        
        TableLayout layout = new TableLayout(table);
        panel.setLayout(layout);  

        JLabel prefex = new JLabel(prefexStr);
        prefex.setDisplayedMnemonic(mnemonic);
        panel.add(prefex,"0,0");

        DecimalNumberField result = new DecimalNumberField(0.0f, maxChars);
        result.setHorizontalAlignment(JTextField.CENTER);
        prefex.setLabelFor(result);
        result.setToolTipText(tooltip);
        if (initialValue != null) result.setText(initialValue);

        panel.add(result,"1,0");

        JLabel suffex = new JLabel(suffexStr);
        panel.add(suffex,"3,0");
        
        container.add(panel, placement);
        
        return result;
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

    // A version of addTextPane that lets you add a style and context
    public JTextPane addTextPane(Container container, String text, 
            String placement, StyleContext context, Style style, boolean debug)
    {
        StyledDocument document = new DefaultStyledDocument(context);

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

    public JPasswordField addPasswordField(Container container, String name,
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
        
        JPasswordField result = new JPasswordField(20);
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
        textArea.setOpaque(true);
        
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
        java.util.List<TabStop> list = new ArrayList<TabStop>();
        
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
        button.setOpaque(lafOpaque);
        container.add(button, placement);
        
        if (debug == true)
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.red),
                    button.getBorder()));
        
        return button;
    }
    
    public JButton addIconButton(Container container, String label, String image,
            Integer width, Integer height, Integer mnemonic, String tooltip, String placement, 
            boolean debug)
    {
        JButton button = null;
        
        if (image == null) 
        {
            button = new JButton(label);
        } else {
            java.net.URL imgURL = Main.class.getResource(image);
            if (imgURL != null && label.length() > 0)
            {
                button = new JButton(label, new ImageIcon(imgURL));
            } else if (imgURL != null)
            {
                button = new JButton(null, new ImageIcon(imgURL));
            } else {
                button = new JButton(label);
                System.err.println("Couldn't find icon: " + image);
            }
        }
        button.setMaximumSize(new Dimension(width, height));
        button.setPreferredSize(new Dimension(width, height));
        button.setMinimumSize(new Dimension(width, height));
        button.setSize(new Dimension(width, height));
        if (mnemonic != null) button.setMnemonic(mnemonic);
        button.setOpaque(lafOpaque);
        container.add(button, placement);
        if (motif == true) 
            {
                Border b = BorderFactory.createLineBorder(Color.gray);
                button.setMargin(new Insets(0,0,0,0)); 
                button.setBorder(b);
            }
        
        if (debug == true)
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.red),
                    button.getBorder()));
        
        return button;
    }
    
    public JComboBox addComboBox(Container container, String label,
         Object[] initialValues, int mnemonic, String tooltip, 
         double labelWidth, String placement, boolean debug)
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        double size[][] = {{labelWidth,TableLayout.FILL},{TableLayout.PREFERRED}};
        TableLayout layout = new TableLayout(size);
        panel.setLayout(layout);

        JLabel labelTxt = new JLabel(label);
        labelTxt.setDisplayedMnemonic(mnemonic);
        panel.add(labelTxt, "0,0,l,c");

        JComboBox result = null;
        if (initialValues != null)
        {
            result = new JComboBox(initialValues);
        } else {
            result = new JComboBox();
        }
        labelTxt.setLabelFor(result);
        result.setToolTipText(tooltip);
        panel.add(result, "1,0,f,c");
        container.add(panel, placement);
        result.setOpaque(false);
        return result;
    }

    public JRadioButton addRadioButton(Container container, String label, 
            int mnemonic, String tooltip, String placement, boolean debug)
    {
        JRadioButton button = new JRadioButton(label);
        container.add(button, placement);  
        button.setOpaque(false);
        return button;
        
    }

    public JCheckBox addCheckBox(Container container, 
            String string, String placement, boolean debug)
    {
        JCheckBox checkBox = new JCheckBox(string);
        container.add(checkBox, placement);
        checkBox.setOpaque(false);
        return checkBox;
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
    
    public ImageIcon getImageIcon(String path)
    {
        java.net.URL imgURL = Main.class.getResource(path);
        if (imgURL != null) { return new ImageIcon(imgURL); } 
        else { System.err.println("Couldn't find icon: " + imgURL); }
        return null;
    }

    public boolean quitConfirmed(Component frame, String message) {
        if (message == null)
        {
            message = "Do you really want to quit?\n" +
            "Doing so will cancel any running imports.";
        }
        String s1 = "Quit";
        String s2 = "Don't Quit";
        Object[] options = {s1, s2};
        int n = JOptionPane.showOptionDialog(frame,
                message,
                "Quit Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                s2);
        if (n == JOptionPane.YES_OPTION) {
            //System.err.println("quitConfirmed returned true");
            return true;
        } else {
            return false;
        }
    }
    
    public void restartNotice(Component frame, String message) {
        if (message == null)
        {
            message = "You must restart before your changes will take effect.";
        }
        JOptionPane.showMessageDialog(frame, message);
    }
    
    public class WholeNumberField extends JTextField {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private Toolkit toolkit;
        private NumberFormat integerFormatter;

        public WholeNumberField(int value, int columns) {
            super(columns);
            toolkit = Toolkit.getDefaultToolkit();
            integerFormatter = NumberFormat.getNumberInstance(Locale.US);
            integerFormatter.setParseIntegerOnly(true);
            setValue(value);
        }

        public int getValue() {
            int retVal = 0;
            try {
                retVal = integerFormatter.parse(getText()).intValue();
            } catch (ParseException e) {
                // This should never happen because insertString allows
                // only properly formatted data to get in the field.
                toolkit.beep();
            }
            return retVal;
        }

        public void setValue(int value) {
            setText(integerFormatter.format(value));
        }

        protected Document createDefaultModel() {
            return new WholeNumberDocument();
        }

        protected class WholeNumberDocument extends PlainDocument {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void insertString(int offs, String str, AttributeSet a) 
            throws BadLocationException {

                char[] source = str.toCharArray();
                char[] result = new char[source.length];
                int j = 0;

                for (int i = 0; i < result.length; i++) {
                    if (Character.isDigit(source[i]))
                    {
                        result[j++] = source[i];
                    }
                    else {
                        toolkit.beep();
                        //System.err.println("insertString: " + source[i]);
                    }
                }
                super.insertString(offs, new String(result, 0, j), a);


            }

        }

    }
    
    public class DecimalNumberField extends JTextField {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private Toolkit toolkit;
        private NumberFormat floatFormatter;

        public DecimalNumberField(float avaluelue, int columns) {
            super(columns);
            toolkit = Toolkit.getDefaultToolkit();
            floatFormatter = NumberFormat.getNumberInstance(Locale.US);
            floatFormatter.setParseIntegerOnly(false);
            setValue(avaluelue);
        }

        public float getValue() {
            float retVal = 0;
            try {
                retVal = floatFormatter.parse(getText()).floatValue();
            } catch (ParseException e) {
                // This should never happen because insertString allows
                // only properly formatted data to get in the field.
                toolkit.beep();
            }
            return retVal;
        }

        public void setValue(float value) {
            setText(floatFormatter.format(value));
        }

        protected Document createDefaultModel() {
            return new DecimalNumberDocument();
        }

        protected class DecimalNumberDocument extends PlainDocument {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void insertString(int offs, String str, AttributeSet a) 
            throws BadLocationException {

                char[] source = str.toCharArray();
                char[] result = new char[source.length];
                int j = 0;

                for (int i = 0; i < result.length; i++) {
                    if (Character.isDigit(source[i]) || Character.toString(source[i]).equals("."))
                    {
                        result[j++] = source[i];
                    }
                    else {
                        toolkit.beep();
                        //System.err.println("insertString: " + source[i]);
                    }
                }
                super.insertString(offs, new String(result, 0, j), a);


            }

        }

    }

    public JPanel addImagePanel(JPanel container, String imageString,
            String placement, boolean debug)
    {
        ImageIcon icon = null;
        java.net.URL imgURL = Main.class.getResource(imageString);
        if (imgURL != null)
        {
            icon = new ImageIcon(imgURL);
        }
        JPanel panel = new ImagePanel(icon);
        
        if (debug == true)
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.red),
                    panel.getBorder()));
        else 
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.gray),
                    panel.getBorder()));            
        
        container.add(panel, placement);
        
        return panel;

    }
    

    public class ImagePanel extends JPanel
    {
        private static final long serialVersionUID = 1L;
        ImageIcon image;
    
        public ImagePanel(ImageIcon icon)
        {
            super();
            this.image = icon;
        }
        
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            if(image != null)
            {
                g.drawImage(image.getImage(), 0, 0, this);
            }
        }
    }
    
}


