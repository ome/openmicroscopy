/*
 * org.openmicroscopy.shoola.util.ui.UIUtilities
 *
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
 */

package ome.formats.importer.util;

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

//Third-party libraries

//Application-internal dependencies

/** 
 * A collection of static methods to perform common UI tasks.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4730 $ $Date: 2007-01-19 14:25:18 +0000 (Fri, 19 Jan 2007) $)
 * </small>
 * @since OME2.2
 */
public class UIUtilities
{

    /** String to representing the nanometer symbol. */
    public static final String              NANOMETER = " \u00B5m";
    
    /** 
     * The string displayed before an item name if the name has been
     * truncated.
     */
    public static final String              DOTS = "...";
    
    /** The Steelblue color. */
    public static final Color               STEELBLUE = new Color(0x4682B4);
    
    /** Width of the dialog window. */
    public static final int                 DIALOG_WIDTH = 500;
    
    /** Height of the dialog window. */
    public static final int                 DIALOG_HEIGHT = 500;
    
	/** Width of the separator. */
	public static final int                 SEPARATOR_WIDTH = 2;
	
    /** Maximum width of the table. */
    public static final int                 TABLE_WIDTH = 200;
    
	/**
	 * Centers the specified component on the screen.
	 * The location of the specified component is set so that it will appear
	 * in the middle of the screen when made visible.
	 * This method is mainly useful for windows, frames and dialogs.
	 * 
	 * @param window	The component to center.
	 */
	public static void centerOnScreen(Component window)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle ed = window.getBounds();
		window.setLocation((screenSize.width-ed.width)/2, 
							(screenSize.height-ed.height)/2);
	}
	
	/**
	 * Centers the specified component on the screen and then makes it visible.
	 * This method is mainly useful for windows, frames and dialogs. 
	 * 
	 * @param window	The component to center.
	 * @see	#centerOnScreen(Component)
	 */
	public static void centerAndShow(Component window)
	{
		centerOnScreen(window);
		window.setVisible(true);
	}
    

    /**
     * Sets the lcoation of the specified child relative to the location
     * of the specified parent and then makes it visible.
     * This method is mainly useful for windows, frames and dialogs. 
     * 
     * @param parent    The visible parent.
     * @param child     The child to display.
     */
    public static void setLocationRelativeToAndShow(Component parent, 
                                                Component child)
    {
        int x = parent.getX()+parent.getWidth();
        int y = parent.getY();
        int childWidth = child.getWidth();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (x+childWidth > screenSize.getWidth()) {
            if (childWidth < parent.getX()) x = parent.getX()-childWidth;
            else x = (int) (screenSize.getWidth()-childWidth);
        } 
        child.setLocation(x, y);
        child.setVisible(true);
    }
    
    /**
     * Sets the location of the passed component relative to the specified 
     * bounds.
     * 
     * @param bounds    The bounds of the main component.
     * @param child     The location of the child
     */
    public static void incrementRelativeToAndShow(Rectangle bounds, 
                                                Component child)
    {
        if (bounds == null) {
            UIUtilities.centerAndShow(child);
            return;
        }
        child.setLocation(bounds.x+10, bounds.y+10);
        child.setVisible(true);
    }
    
	/**
     * Creates a modal JDialog containing the specified JComponent
     * for the specified parent.
     * The newly created dialog is then centered on the screen and made visible.
	 *
     * @param parent The parent component.
     * @param title  The title of the dialog.
     * @param c      The component to display.
     * @see #centerAndShow(Component)
	 */
    public static void makeForDialog(Component parent, String title, 
                                    JComponent c)
    {
        if (c == null) return;
        JDialog dialog = null;
        if (parent instanceof Frame) dialog = new JDialog((Frame) parent);
        else if (parent instanceof Dialog) 
            dialog = new JDialog((Dialog) parent);
        else if (dialog == null || parent == null) 
            dialog = new JDialog(); //no parent
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.setTitle(title);
        dialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        Container container = dialog.getContentPane();
        container.setLayout(new BorderLayout(0, 0));
        container.add(c, BorderLayout.CENTER);
        centerAndShow(dialog);
    }
    
    /** 
     * Creates a modal JDialog with no title. 
     * 
     * @param parent The parent component.
     * @param c      The component to display.
     * @see #makeForDialog(Component, JComponent)
     */
    public static void makeForDialog(Component parent, JComponent c)
    {
        makeForDialog(parent, "", c);
    }
    
	/** 
	 * Builds a tool tip in a fixed font and color.
	 * You pass the tool tip text and get back an <i>HTML</i> string to be
	 * passed, in turn, to the <code>setToolTipText</code> method of a 
	 * {@link javax.swing.JComponent}.
	 *
	 * @param toolTipText     The textual content of the tool tip.
	 * @return An <i>HTML</i> fomatted string to be passed to 
	 * 			<code>setToolTipText()</code>.
	 */
	public static String formatToolTipText(String toolTipText) 
	{
		StringBuffer buf = new StringBuffer(90+toolTipText.length());
		//buf.
		buf.append("<html><body bgcolor=#FFFCB7 text=#AD5B00>");
		//TODO: change into platform independent font
		buf.append("<font face=Arial size=2>");  
		buf.append(toolTipText);
		buf.append("</font></body></html>");
		return toolTipText;
		//return buf.toString();
	} 
	
    /**
     * Builds a tool tip in a fixed font and color.
     * 
     * @param title     The title to format.
     * @param body      The body to format.
     * @param maxWidth  The maximum width of the <code>HTML</code> table.
     * @return See below.
     */
    public static String makeParagraph(String title, String body, int maxWidth)
    {
        if (title != null && body == null) 
            return formatToolTipText(title);
        //title.
        StringBuffer buf = new StringBuffer();
        buf.append("<html><body bgcolor=#FFFCB7 text=#AD5B00>");
        //TODO: change into platform independent font
        if (title != null && body != null) {
            String s = "<table width="+maxWidth+"><tr>";
            buf.append(s);
            buf.append("<td><b>");
            buf.append(title);
            buf.append("</b><hr size=1>");
            buf.append("<font face=Arial size=2>"); 
            buf.append(body);
            buf.append("</font>");
            buf.append("</td></tr></table>");
        } else if (title == null && body != null) {
            String s = "<table width="+maxWidth+"><tr>";
            buf.append(s);
            buf.append("<td>");
            buf.append("<font face=Arial size=2>"); 
            buf.append(body);
            buf.append("</font>");
            buf.append("</td></tr></table>");
        }
        buf.append("</body></html>");
        return buf.toString();  
    }
    
	/** 
	 * Create a separator to add to a toolbar. The separator needs to be 
	 * set when the layout of the toolbar is reset.
	 * 
	 * @param button   The button to add to the toolBar. The height of the 
	 * 				   separator depends of the insets of the button.
	 * @param icon     The icon to add to the button. The height of the 
     *                 separator depends of the height of the icon.
	 * @return See below.
	 */
	public static JSeparator toolBarSeparator(JButton button, Icon icon)
	{
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		Insets i = button.getInsets();
		int h = icon.getIconHeight();
		Dimension d = new Dimension(SEPARATOR_WIDTH, i.top+h+i.bottom);
		separator.setPreferredSize(d);
		separator.setSize(d);
		return separator;
	}
	
    /**
     * Displays the specified string into a {@link JLabel} and sets 
     * the font to <code>bold</code>.
     * 
     * @param s The string to display.
     * @return See above.
     */
    public static JLabel setTextFont(String s)
    {
        JLabel label = new JLabel(s);
        Font font = label.getFont();
        Font newFont = font.deriveFont(Font.BOLD);
        label.setFont(newFont);
        return label;
    }
    
    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a left flowlayout.
     * 
     * @param component The component to add.
     * @return See below.
     */
    public static JPanel buildComponentPanel(JComponent component)
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(component);
        return p;
    }
    
    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a right flowlayout.
     * 
     * @param component The component to add.
     * @return See below.
     */
    public static JPanel buildComponentPanelRight(JComponent component)
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(component);
        return p;
    }
    
    /**
     * Sets the UI properties of the button to unify the L&F.
     * 
     * @param b The button.
     */
    public static void unifiedButtonLookAndFeel(AbstractButton b)
    {
        b.setMargin(new Insets(0, 2, 0, 3));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
    }

    /**
     * Sets the defaults for the specified area.
     * 
     * @param area The text area.
     */
    public static void setTextAreaDefault(JTextComponent area)
    {
        area.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        area.setForeground(STEELBLUE);
        area.setBackground(Color.WHITE);
        area.setOpaque(true);
        area.setEditable(true);
    }
    
    /**
     * format a double to 2 decimal places and return as a string. 
     * 
     * @param val number to be formatted. 
     * @return formatted string. 
     */
    public static String twoDecimalPlaces(double val)
    {
    	double v = val;
    	String value;
    	double c = v;
    	if (v < 0) return null;
    	if ((c-Math.floor(c)) > 0) value = ""+Math.round(c*100)/100f; 
    	else  value = ""+(int) c;
    	if (value.equals("0")) return null;
    	return value; 
	}
    
    /**
     * Formats the text and displays it in a {@link JTextPane}.
     * 
     * @param text The text to display.
     * @return See above.
     */
    public static JTextPane buildTextPane(String text)
    {
    	return buildTextPane(text, null);
    }
    
    /**
     * Formats the text and displays it in a {@link JTextPane}.
     * 
     * @param text 			The text to display.
     * @param foreground	The foreground color.
     * @return See above.
     */
    public static JTextPane buildTextPane(String text, Color foreground)
    {
    	StyleContext context = new StyleContext();
        StyledDocument document = new DefaultStyledDocument(context);

        Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
        if (foreground != null)
        	StyleConstants.setForeground(style, foreground);
        try {
            document.insertString(document.getLength(), text, style);
        } catch (BadLocationException e) {}

        JTextPane textPane = new JTextPane(document);
        textPane.setOpaque(false);
        textPane.setEditable(false);
        textPane.setFocusable(false);
        
        return textPane;
    }
    /** 
     * Sets the focus default for the specified button.
     * 
     * @param button
     */
    public static void enterPressesWhenFocused(JButton button)
    {
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
