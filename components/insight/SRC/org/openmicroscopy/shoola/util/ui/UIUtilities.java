/*
 * org.openmicroscopy.shoola.util.ui.UIUtilities
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

package org.openmicroscopy.shoola.util.ui;

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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.VolatileImage;
import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;


//Third-party libraries
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;

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
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class UIUtilities
{

	/** Letter corresponding to number. */
	public static final Map<Integer, String> LETTERS;
	
	/** The maximum number of characters in a line for the tool tip. */
	public static final int					MAX_CHARACTER = 40;
	
	/** A light grey colour for line borders etc. */
	public static final Color 				LIGHT_GREY = 
												new Color(200, 200, 200);
	
	/** The default width of an icon. */
	public static final int					DEFAULT_ICON_WIDTH = 16;
	
	/** The default height of an icon. */
	public static final int					DEFAULT_ICON_HEIGHT = 16;
	
	/** The default background color. */
    public static final Color				HYPERLINK_COLOR = Color.BLUE;
    
	/** The default background color. */
    public static final Color				BACKGROUND_COLOR = Color.WHITE;
    
    /** The default background color. */
    public static final Color				WINDOW_BACKGROUND_COLOR = 
    										new Color(248, 248, 248);
    
    /** The color of the text when editing. */
    public static final Color				EDITED_COLOR = Color.red;
    
	/** The default color of the description. */
    public static final Color				DEFAULT_FONT_COLOR = Color.GRAY;

	/**
	 * This property should be exposed but is NOT.
	 */
	public static final String				COLLAPSED_PROPERTY_JXTASKPANE = 
											"collapsed";
	
	/** 
     * The highlight color to use for the inner border surrounding the
     * frame's contents.
     */
    public static final Color      			INNER_BORDER_HIGHLIGHT = 
    											new Color(240, 240, 240);
    
    /** 
     * The shadow color to use for the inner border surrounding the
     * frame's contents.
     */
    public static final Color      			INNER_BORDER_SHADOW = 
    											new Color(200, 200, 200);
    
	/** The selected date format. */
	public static final String				DATE_FORMAT = "yy/MM/dd";
	
	/** Background color of an even row. */
	public final static Color 				BACKGROUND_COLOUR_EVEN = 
												new Color(232, 242, 254);
	
	/** Background color of an odd row. */
	public final static Color 				BACKGROUND_COLOUR_ODD = 
												new Color(255, 255, 255);
	
	/** Background color of the selected row */
	public final static Color 				SELECTED_BACKGROUND_COLOUR = 
												new Color(180, 213, 255);
	
	/** Foreground color of a cell.*/
	public final static Color 				FOREGROUND_COLOUR = new Color(0, 0, 
																			0);
	
	/** The starting color of the gradient used in the track. */
	public static final Color 				TRACK_GRADIENT_START = 
													new Color(76, 76, 76);

	/** The final color of the gradient used in the track. */
	public static final Color 				TRACK_GRADIENT_END = 
												new Color(176, 176, 176);
	
	/** The color of the line drawn on the knobs. */
	public static final Color  				LINE_COLOR = Color.BLACK;

	/** A day in milliseconds. */
	public static final long				DAY = 86400000;
	
	/** Unicode for the degrees symbol. */
	public final static String 				DEGREES_SYMBOL = "\u00B0";
	
	
	/** Unicode for the microns symbol. */
	public final static String  			MICRONS_SYMBOL = "\u00B5m";

	/** Unicode for the squared symbol. */
	public final static String  			SQUARED_SYMBOL =  "\u00B2";
	
	/** Unicode for the squared symbol. */
	public final static String  			DELTA_SYMBOL =  "\u0394";
	
	/** Pixels string. */
	public final static String  			PIXELS_SYMBOL = "px";
	
    /** String to representing the nanometer symbol. */
    public static final String              NANOMETER = " \u00B5m";
    
    /** String to represent the micron symbol. */
    public static final String 				MICRONS = "(in \u00B5)";
    
	/** Background color of the hightlighted node. */
	public static final Color				HIGHLIGHT = new Color(204, 255, 
															204);
	/** Background color of the even rows. */
	public static final Color				BACKGROUND = Color.WHITE;
	
	/** Background color of the add rows. */
	public static final Color				BACKGROUND_ONE = new Color(236, 243,
																		254);
    /** 
     * The string displayed before an item name if the name has been
     * truncated.
     */
    public static final String              DOTS = "...";
    
    /** The Steelblue color. */
    public static final Color               STEELBLUE = new Color(0x4682B4);
    
    /** The default text color. */
    public static final Color               DEFAULT_TEXT = Color.WHITE;
    
    /** Width of the dialog window. */
    public static final int                 DIALOG_WIDTH = 500;
    
    /** Height of the dialog window. */
    public static final int                 DIALOG_HEIGHT = 500;
    
	/** Width of the separator. */
	public static final int                 SEPARATOR_WIDTH = 2;
	
    /** Maximum width of the table. */
    public static final int                 TABLE_WIDTH = 200;
    
    /** The value of the increment factor. */
    public static final int					INCREMENT = 15;

    /** The number of bytes in megabyte, used when working with memory methods.*/
	public static final long		MEGABYTE = 1048567;
    
	/** Key value for the default folder. */
    private static final String 			DEFAULT_FOLDER = "defaultFolder";
    
    /** The default mac L&F. */
   //private static final String				MAC_L_AND_F = 
    //											"apple.laf.AquaLookAndFeel";
    
    /** The pattern to format date. */
    private static final String				WDMY_FORMAT = 
    											"E dd MMM yyyy, HH:mm:ss";
    
    /** The tooltip of the calendar button. */
	private static final String		DATE_TOOLTIP = "Bring up a calendar.";
	
	/** The maximum width of the text when wrapping up text. */
	private static final int		WRAP_UP_MAX_WIDTH = 50;

	static {
		LETTERS = new HashMap<Integer, String>();
		LETTERS.put(1, "A");
		LETTERS.put(2, "B");
		LETTERS.put(3, "C");
		LETTERS.put(4, "D");
		LETTERS.put(5, "E");
		LETTERS.put(6, "F");
		LETTERS.put(7, "G");
		LETTERS.put(8, "H");
		LETTERS.put(9, "I");
		LETTERS.put(10, "J");
		LETTERS.put(11, "K");
		LETTERS.put(12, "L");
		LETTERS.put(13, "M");
		LETTERS.put(14, "N");
		LETTERS.put(15, "O");
		LETTERS.put(16, "P");
		LETTERS.put(17, "Q");
		LETTERS.put(18, "R");
		LETTERS.put(19, "S");
		LETTERS.put(20, "T");
		LETTERS.put(21, "U");
		LETTERS.put(22, "V");
		LETTERS.put(23, "W");
		LETTERS.put(24, "X");
		LETTERS.put(25, "Y");
		LETTERS.put(26, "Z");
	}
	
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
		if (window == null) return;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle ed = window.getBounds();
		window.setLocation((screenSize.width-ed.width)/2, 
							(screenSize.height-ed.height)/2);
	}
	
	/**
	 * Shows the specified window on the screen, It will appear at the specified
	 * location. If the location is <code>null</code>, it will appear in the 
	 * middle of the screen. 
	 * 
	 * @param window	The component to show.
	 * @param location	The location of the specified component if 
	 * 					<code>null</code> it will appear in the middle of the 
	 * 					screen.
	 */
	public static void showOnScreen(Component window, Point location) 
	{
		if (window == null) return;
		if (location == null) centerOnScreen(window);
		else {
			window.setLocation(location);
			window.setVisible(true);
		}
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
	 * Centers the specified component on the parent and then makes it visible.
	 * This method is mainly useful for windows, frames and dialogs. 
	 * 
	 * @param parent    The visible parent.
     * @param child     The child to display.
	 */
	public static void centerAndShow(Component parent, Component child)
	{
		if (parent == null || child == null) return;
		Rectangle bounds = parent.getBounds();
		Rectangle ed = child.getBounds();
		child.setLocation(bounds.x+(bounds.width-ed.width)/2, 
						bounds.y+(bounds.height-ed.height)/2);
		child.setVisible(true);
	}
	
    /**
     * Sets the location of the specified child relative to the location
     * of the specified parent and then makes it visible.
     * This method is mainly useful for windows, frames and dialogs. 
     * 
     * @param parent    The visible parent.
     * @param child     The child to display.
     */
    public static void setLocationRelativeToAndShow(Component parent, 
                                                Component child)
    {
        setLocationRelativeTo(parent, child);
    }
    
    /**
     * Sets the location of the specified child relative to the location
     * of the specified parent and then makes it visible.
     * This method is mainly useful for windows, frames and dialogs. 
     * 
     * @param parent    The visible parent.
     * @param child     The child to display.
     */
    public static void setLocationRelativeTo(Component parent, 
                                                Component child)
    {
    	if (parent == null || child == null) return;
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
     * Sets the location of the specified child relative to the location
     * of the specified parent and then makes it visible, and size to fill 
     * the window.
     * This method is mainly useful for windows, frames and dialogs. 
     * 
     * @param parent    The visible parent.
     * @param child     The child to display.
     * @param max		The maximum size of the window.
     */
    public static void setLocationRelativeToAndSizeToWindow(Component parent, 
                                                Component child, Dimension max)
    {
    	setLocationRelativeToAndSizeToWindow(parent.getBounds(), child, max);
    }
    
    /**
     * Sets the location of the specified child relative to the location
     * of the specified parent and then makes it visible, and size to fill window.
     * This method is mainly useful for windows, frames and dialogs. 
     * 
     * @param parentBounds    The bounds of the visible parent.
     * @param child     The child to display.
     * @param max		The maximum size of the window.
     */
    public static void setLocationRelativeToAndSizeToWindow(
    		Rectangle parentBounds, Component child, Dimension max)
    {
    	if (child == null) return;
    	if (parentBounds == null) parentBounds = new Rectangle(0, 0, 5, 5);
    	if (max == null) max = new Dimension(5, 5);
        int x = (int) (parentBounds.getX()+ parentBounds.getWidth());
        int y = (int) parentBounds.getY();
        int childWidth = child.getWidth();
        int childHeight = child.getHeight();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (x+childWidth > screenSize.getWidth()) {
            if (childWidth < parentBounds.getX()) 
            	x = (int) (parentBounds.getX())-childWidth;
            else x = (int) (screenSize.getWidth()-childWidth);
        } 
        child.setLocation(x, y);
        int newHeight = (int) screenSize.getHeight()-y-10;
        int newWidth = (int) screenSize.getWidth()-x-10;
        
        if (newWidth > childWidth) childWidth = newWidth;
        if (newHeight > childHeight) childHeight = newHeight;
        
        if (childWidth > max.getWidth()) childWidth = (int) max.getWidth();
        if (childHeight > max.getHeight()) childHeight = (int) max.getHeight();
        
        child.setSize(childWidth, childHeight);
        child.setVisible(true);
    }
    
    /**
     * Sets the location of the specified child relative to the passed 
     * bounds.
     * This method is mainly useful for windows, frames and dialogs. 
     * 
     * @param parentBounds  The bounds of the parent.
     * @param child     	The child to display.
     */
    public static void setLocationRelativeTo(Rectangle parentBounds, 
                                                Component child)
    {
    	if (child == null) return;
    	if (parentBounds == null) parentBounds = new Rectangle(0, 0, 5, 5);

        int x = parentBounds.x+parentBounds.width;
        int y = parentBounds.y;
        int childWidth = child.getWidth();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (x+childWidth > screenSize.getWidth()) {
            if (childWidth < parentBounds.x) x = parentBounds.x-childWidth;
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
        child.setLocation(bounds.x+INCREMENT, bounds.y+INCREMENT);
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
        else if (dialog == null) 
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
		if (toolTipText == null) toolTipText = "";
		StringBuffer buf = new StringBuffer(90+toolTipText.length());
		//buf.
		buf.append("<html><body bgcolor=#FFFCB7 text=#AD5B00>");
		//TODO: change into platform independent font
		buf.append("<font face=Arial size=2>");  
		buf.append(toolTipText);
		buf.append("</font></body></html>");
		return toolTipText;
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
	public static String formatToolTipText(List<String> toolTipText) 
	{
		if (toolTipText == null) toolTipText = new ArrayList<String>();
		StringBuffer buf = new StringBuffer();
		//buf.
		//buf.append("<html><body bgcolor=#FFFCB7 text=#AD5B00>");
		buf.append("<html><body>");
		buf.append("<font face=Arial size=2>");  
		Iterator<String> i = toolTipText.iterator();
		buf.append("<p>");
		while (i.hasNext()) {
			buf.append(i.next());
			buf.append("<br>");
		}
		buf.append("</p>");
		buf.append("</font></body></html>");
		return buf.toString();
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
		if (button == null) return separator;
		Insets i = button.getInsets();
		int h = 0;
		if (icon != null) h = icon.getIconHeight();
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
    	return UIUtilities.setTextFont(s, Font.BOLD);
    }
    
    /**
     * Displays the specified string into a {@link JLabel} and sets 
     * the font to <code>bold</code>.
     * 
     * @param s 		The string to display.
     * @param fontStyle The style of the font.
     * @return See above.
     */
    public static JLabel setTextFont(String s, int fontStyle)
    {
    	if (s == null) s = "";
        JLabel label = new JLabel(s);
        Font font = label.getFont();
        Font newFont = font.deriveFont(fontStyle);
        label.setFont(newFont);
        return label;
    }
    
    /**
     * Displays the specified string into a {@link JLabel} and sets 
     * the font to <code>bold</code>.
     * 
     * @param s 		The string to display.
     * @param fontStyle The style of font.
     * @param fontSize	The size of the font.
     * @return See above.
     */
    public static JLabel setTextFont(String s, int fontStyle, int fontSize)
    {
    	if (s == null) s = "";
        JLabel label = new JLabel(s);
        Font font = label.getFont();
        label.setFont(font.deriveFont(fontStyle, fontSize));
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
    	return buildComponentPanel(component, 5, 5, true);
    }
    
    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a left flowlayout.
     * 
     * @param component The component to add.
     * @param isOpaque  Pass <code>true</code> if this component should be 
     * 					opaque, <code>false</code> otherwise.
     * @return See below.
     */
    public static JPanel buildComponentPanel(JComponent component, 
    					boolean isOpaque)
    {
    	return buildComponentPanel(component, 5, 5, isOpaque);
    }

    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a left flowlayout.
     * 
     * @param component The component to add.
     * @param hgap    	The horizontal gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @param	vgap    The vertical gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @return See below.
     */
    public static JPanel buildComponentPanel(JComponent component, 
    										int hgap, int vgap)
    {
    	return buildComponentPanel(component, hgap, vgap, true);
    }
    
    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a left flowlayout.
     * 
     * @param component The component to add.
     * @param hgap    	The horizontal gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @param vgap    	The vertical gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @param isOpaque  Pass <code>true</code> if this component should be 
     * 					opaque, <code>false</code> otherwise.
     * @return See below.
     */
    public static JPanel buildComponentPanel(JComponent component, 
    									int hgap, int vgap, boolean isOpaque)
    {
        JPanel p = new JPanel();
        if (component == null) return p;
        if (hgap < 0) hgap = 0;
        if (vgap < 0) vgap = 0;
        p.setLayout(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
        p.add(component);
        p.setOpaque(isOpaque);
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
       return buildComponentPanelRight(component, true);
    }
    
    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a right flowlayout.
     * 
     * @param component The component to add.
     * @param hgap    	The horizontal gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @param vgap    	The vertical gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @param isOpaque  Pass <code>true</code> if this component should be 
     * 					opaque, <code>false</code> otherwise.
     * @return See below.
     */
    public static JPanel buildComponentPanelRight(JComponent component, 
    							int hgap, int vgap, boolean isOpaque)
    {
        JPanel p = new JPanel();
        if (component == null) return p;
        if (hgap < 0) hgap = 0;
        if (vgap < 0) vgap = 0;
        p.setLayout(new FlowLayout(FlowLayout.RIGHT, hgap, vgap));
        p.add(component);
        p.setOpaque(isOpaque);
        return p;
    }
    
    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a right flowlayout.
     * 
     * @param component The component to add.
     * @param isOpaque  Pass <code>true</code> if this component should be 
     * 					opaque, <code>false</code> otherwise.
     * @return See below.
     */
    public static JPanel buildComponentPanelRight(JComponent component, 
    											boolean isOpaque)
    {
        return buildComponentPanelRight(component, 5, 5, isOpaque);
    }

    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a right flowlayout.
     * 
     * @param component The component to add.
     * @return See below.
     */
    public static JPanel buildComponentPanelCenter(JComponent component)
    {
        JPanel p = new JPanel();
        if (component == null) return p;
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
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
        //b.setMargin(new Insets(0, 2, 0, 3));
        //b.setBorderPainted(false);
        //b.setFocusPainted(false);
    	if (b != null)
    		b.setBorder(new EmptyBorder(2, 2, 2, 2));
    }

    /**
     * Sets the opacity of the specified button depending on the 
     * system look and Feel.
     * 
     * @param b The button to handle.
     */
    public static void opacityCheck(AbstractButton b)
    {
    	if (b == null) return;
    	//String laf = UIManager.getSystemLookAndFeelClassName();
    	String osName = System.getProperty("os.name");
    	b.setContentAreaFilled(!(osName.startsWith("Mac OS")));
    	//b.setContentAreaFilled(!(MAC_L_AND_F.equals(laf)));
    }
    
    /**
     * Sets the defaults for the specified area.
     * 
     * @param area The text area.
     */
    public static void setTextAreaDefault(JComponent area)
    {
    	if (area == null) return;
        area.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        //area.setForeground(STEELBLUE);
        area.setBackground(BACKGROUND);
        area.setOpaque(true);
        if (area instanceof JTextComponent) 
        	((JTextComponent) area).setEditable(true);
    }
    
    /**
     * Formats a double to two decimal places and returns as a string. 
     * 
     * @param val The number to be formatted. 
     * @return The formatted string. 
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
     * Formats the text and displays it in a {@link JEditorPane}.
     * 
     * @param text	The text to display.
     * @return See above.
     */
    public static JEditorPane buildTextEditorPane(String text)
    {
    	if (text == null) text = "";
        JEditorPane textPane = new JEditorPane();
        textPane.setContentType("text/html");
        textPane.setText(text);
        textPane.setOpaque(false);
        textPane.setEditable(false);
        textPane.setFocusable(false);
        return textPane;
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
    	if (text == null) text = "";
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
     * @param button The button to handle.
     */
    public static void enterPressesWhenFocused(JButton button)
    {
    	if (button == null) return;
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
    
    /**
     * Returns the pathname string of the default folder.
     * 
     * @return See above.
     */
    public static String getDefaultFolderAsString()
    {
    	Preferences  prefs = Preferences.userNodeForPackage(UIUtilities.class);
    	if (prefs == null) return null;
    	return prefs.get(DEFAULT_FOLDER, null);
    }
    
    /**
     * Sets the pathname string of the default folder.
     * 
     * @param f The value to set.
     */
    public static void setDefaultFolder(String f)
    {
    	Preferences  prefs = Preferences.userNodeForPackage(UIUtilities.class);
    	if (prefs == null) return;
    	if (f == null) f = "";
    	prefs.put(DEFAULT_FOLDER, f);
    }
    
    /**
     * Returns the default folder.
     *  
     * @return See above.
     */
    public static File getDefaultFolder()
    {
    	String f = UIUtilities.getDefaultFolderAsString();
    	if (f == null || f == "") return null; 
    	return new File(f);
    }
    
    /**
     * Sets the preferred size, minimum and maximum size of the specified 
     * component.
     * 
     * @param component	The component to handle.
     * @param dim		The dimension to set.
     */
    public static void setDefaultSize(Component component, Dimension dim)
    {
    	if (component == null) return;
    	if (dim == null) dim = new Dimension(5, 5);
    	component.setPreferredSize(dim);
    	component.setMaximumSize(dim);
    	component.setMinimumSize(dim);
    }
    
    /**
     * Finds the component identified by the specified class contained in the 
     * passed component. Returns the found component or <code>null</code> if 
     * none found.
     * 
     * @param comp	The component to visit. Mustn't be <code>null</code>.
     * @param c		The class identifying the component to find.
     * @return See above.
     */
    public static Component findComponent(Component comp, Class c)
    {
    	if (c == null || comp == null)
    		throw new IllegalArgumentException("The parameters cannot be " +
    				"null");
    	if (c.isAssignableFrom(comp.getClass())) return comp;
		
		if (comp instanceof Container) {
			Component[] comps = ((Container)comp).getComponents();
			Component child;
			for (int i = 0; i < comps.length; i++) {
				child = findComponent(comps[i], c);
				if (child != null) return child;
			}
		}
		return null;
    }
    
    /**
     * Finds the components identified by the specified class contained in the 
     * passed component. Returns a collection of found component or 
     * <code>null</code> if none found.
     * 
     * @param comp	The component to visit. Mustn't be <code>null</code>.
     * @param c		The class identifying the component to find.
     * @return See above.
     */
    public static List<Component> findComponents(Component comp, Class c)
    {
    	List<Component> l = null;
    	if (c == null || comp == null)
    		throw new IllegalArgumentException("The parameters cannot be " +
    				"null");
    	if (c.isAssignableFrom(comp.getClass())) {
    		l = new ArrayList<Component>(1);
    		l.add(comp);
    		return l;
    	}
    	if (comp instanceof Container) {
			Component[] comps = ((Container)comp).getComponents();
			Component child;
			l = new ArrayList<Component>(comps.length);
			for (int i = 0; i < comps.length; i++) {
				child = findComponent(comps[i], c);
				if (child != null) l.add(child);
			}
			return l;
		}
		return null;
    }
    
    /**
     * Returns <code>true</code> if the passed color is a dark color,
     * <code>false</code> otherwise.
     * 
     * @param c The color to handle. Mustn't be <code>null</code>.
     * @return See above.
     */
    public static boolean isDarkColor(Color c)
    {
    	if (c == null) return false;
    	return (c.getRed()+c.getGreen()+c.getBlue())/3 < 128;
    }

    /**
     * Creates a default timestamp.
     * 
     * @return See above.
     */
    public static Timestamp getDefaultTimestamp()
    {
        return new Timestamp(new Date().getTime());
    }
   
    /**
     * Formats as a <code>String</code> the specified time.
     * 
     * @param time The timestamp to format.
     * @return Returns the stringified version of the passed timestamp.
     */
    public static String formatTime(Timestamp time) 
    {
    	if (time == null) return "";
    	return DateFormat.getDateInstance().format(time);  
    }
    
    /**
     * Formats as a <code>String</code> the specified time.
     * 
     * @param time The timestamp to format.
     * @return Returns the stringified version of the passed timestamp.
     */
    public static String formatDateTime(Timestamp time) 
    {
    	if (time == null) return "";
    	return DateFormat.getDateTimeInstance().format(time);  
    }
	
    /**
     * Formats as a <code>String</code> the specified time.
     * 
     * @param time The timestamp to format.
     * @return Returns the stringified version of the passed timestamp.
     */
    public static String formatShortDateTime(Timestamp time) 
    {
    	if (time == null) return "";
    	return DateFormat.getDateTimeInstance(
    			DateFormat.SHORT, DateFormat.SHORT).format(time);  
    }
    
    /**
     * Formats as a <code>String</code> the specified time.
     * format: E dd MMM yyyy, HH:mm:ss
     * 
     * @param time The timestamp to format.
     * @return Returns the stringified version of the passed timestamp.
     */
    public static String formatWDMYDate(Timestamp time) 
    {
    	if (time == null) return "";
    	SimpleDateFormat formatter = new SimpleDateFormat(WDMY_FORMAT);
    	return formatter.format(time);  
    }
    
    /**
     * Formats the string to be two decimal places. 
     * 
	 * @param value The value to be formatted. 
     * @return See above.
     */
    public static String formatToDecimal(double value) 
    {
    	try { 
    		return String.format("%.2f",value);
		} catch (Exception e) { return ""; }
    }
    
    /**
     * Rounds the passed value to two decimals after decimal point.
     * 
     * @param value 	The value to round.
     * @return The rounded value.
     */
    public static double roundTwoDecimals(double value)
    {
    	return round(value, 2);
    }
    
    /**
     * Rounds the passed value to the specified number of decimals.
     * 
     * @param value 	The value to round.
     * @param decimal 	The number of figures after decimal point.
     * @return The rounded value.
     */
    public static double round(double value, int decimal)
    {
    	if (decimal <= 0) return value;
    	double p = Math.pow(10, decimal);
    	value = value*p;
    	return Math.round(value)/p;
    }
    
    /**
     * Returns the partial name of the image's name
     * 
     * @param originalName The original name.
     * @return See above.
     */
    public static String[] splitString(String originalName)
    {
    	String[] l = null;
    	if (originalName == null) return l;
        if (Pattern.compile("/").matcher(originalName).find()) {
            l = originalName.split("/", 0);
        } else if (Pattern.compile("\\\\").matcher(originalName).find()) {
            l = originalName.split("\\\\", 0);
        } 
        return l;
    }
    
    /** 
     * Returns the separator or <code>null</code>.
     * 
     * @param originalName The original name.
     * @return See above.
     */
    public static String getStringSeparator(String originalName)
    {
    	if (originalName == null) return null;
    	String[] l = null;
        if (Pattern.compile("/").matcher(originalName).find()) {
            l = originalName.split("/", 0);
            if (l.length > 0) return "/";
        } else if (Pattern.compile("\\\\").matcher(originalName).find()) {
            l = originalName.split("\\\\", 0);
            if (l.length > 0) return "\\";
        } 
        return null;
    }
    
	/** 
	 * Builds the collapse component.
	 * 
	 * @param title The title displayed in the border.
	 * @return See above.
	 */
	public static JPanel buildCollapsePanel(String title)
	{
		if (title == null) title = "";
		JPanel p = new JPanel();
		p.setBorder(new TitledLineBorder(title));
		return p;
	}
	
	/**
	 * Formats and sets the title border of the passed component.
	 * 
	 * @param title The title.
	 * @param p		The component to handle.
	 */
	public static void setBoldTitledBorder(String title, JComponent p)
	{
		if (title == null) title = "";
		if (p == null) return;
		TitledBorder border = new TitledBorder(title);
		border.setTitleFont(p.getFont().deriveFont(Font.BOLD));
		p.setBorder(border);
	}
	
	/**
	 * Formats the passed URL.
	 * 
	 * @param url The value to format.
	 * @return See above.
	 */
	public static String formatURL(String url)
	{
		if (url == null) url = "";
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body>");
		buf.append("<a href=\"");
		buf.append(url);
		buf.append("\"");
		buf.append(">");
		buf.append(url);
		buf.append("</a>");
		buf.append("</body></html>");
		return buf.toString();
	}
	
	/**
	 * Converts the passed value into a string in Mb and returns a string 
	 * version of it.
	 * 
	 * @param v The value to convert.
	 * @return See above.
	 */
	public static String formatFileSize(long v)
	{
		if (v < 0) return "";

		//if (v < 1000) 
		//	return NumberFormat.getInstance().format(v)+" b";
		long value = v;///1000;
		if (value <= 1000) 
			return NumberFormat.getInstance().format(value)+" Kb";
		value = value/1000;
		if (value <= 1000)
			return NumberFormat.getInstance().format(value)+" Mb";
		value = value/1000;
		return NumberFormat.getInstance().format(value)+" Gb";
	}
	
	/**
	 * Creates a date picker.
	 * 
	 * @param editable Pass <code>true</code> to allow users to modify the date
	 * 				   from the editor, <code>false</code> otherwise. The
	 *				   default value is <code>true</code>.
	 * @return See above.
	 */
	public static JXDatePicker createDatePicker(boolean editable)
	{
		String[] dateFormats = new String[1];
		dateFormats[0] = UIUtilities.DATE_FORMAT;
		JXDatePicker picker = new JXDatePicker();
		picker.setToolTipText(DATE_TOOLTIP);
		picker.setFormats(dateFormats);
		picker.getEditor().setBackground(BACKGROUND);
		picker.getEditor().setColumns(6);
		picker.getEditor().setEditable(editable);
		return picker;
	}
	
	/**
	 * Creates a date picker.
	 * 
	 * @return See above.
	 */
	public static JXDatePicker createDatePicker()
	{
		return createDatePicker(true);
	}
	
	/**
	 * Wraps up the passed text at at word boundaries (whitespace) if they are 
	 * too long to fit within the allocated width i.e. 
	 * {@link #WRAP_UP_MAX_WIDTH}.
	 * Returns a collection of blocks of text.
	 * 
	 * @param text The text to handle.
	 * @return See above.
	 */
	public static List<String> wrapStyleWord(String text)
	{
		return wrapStyleWord(text, WRAP_UP_MAX_WIDTH);
	}
	
	/**
	 * Wraps up the passed text at at word boundaries (whitespace) if they are 
	 * too long to fit within the allocated width.
	 * Returns a collection of blocks of text.
	 * 
	 * @param text 		The text to handle.
	 * @param maxWidth 	The allocated width.
	 * @return See above.
	 */
	public static List<String> wrapStyleWord(String text, int maxWidth)
	{
		List<String> l = new ArrayList<String>();
		if (text == null) return l;
		text = text.trim();
		if (maxWidth <= 0) maxWidth = WRAP_UP_MAX_WIDTH;
		String sep = " ";
		String[] values = text.split(sep);
		String v = "";
		String value, tmp;
		for (int i = 0; i < values.length; i++) {
			value = values[i];
			tmp = v+sep+value;
			if (tmp.length() < maxWidth) {
				v += sep+value;
			} else {
				l.add(v);
				v = value;
			}
		}
		if (!v.equals("")) l.add(v);
		return l;
	}
	
	/**
	 * Initializes a <code>JXTaskPane</code>.
	 * 
	 * @param title The title of the component.
	 * @param background The background color.
	 * @return See above.
	 */
	public static JXTaskPane createTaskPane(String title, Color background)
	{
		JXTaskPane taskPane = new JXTaskPane();
		if (isLinuxOS()) taskPane.setAnimated(false);
		Container c = taskPane.getContentPane();
		if (background != null) {
			c.setBackground(background);
			taskPane.setBackground(background);
		}
		if (c instanceof JComponent) 
			((JComponent) c).setBorder(BorderFactory.createEmptyBorder(
					1, 1, 1, 1));
		taskPane.setTitle(title);
		taskPane.setCollapsed(true);
		Font font = taskPane.getFont();
		taskPane.setFont(font.deriveFont(font.getSize2D()-2));
		return taskPane;
	}
	
	/**
	 * Creates a new label.
	 * 
	 * @param c 	The foreground color if not <code>null</code>.
	 * @return See above.
	 */
	public static JLabel createComponent(Color c)
	{
		return (JLabel) createComponent(JLabel.class, c);
	}
	
	/**
	 * Creates a new label.
	 * 
	 * @param type 	The type of component to create. Default type is JLabel.
	 * @param color The foreground color if not <code>null</code>.
	 * @return See above.
	 */
	public static JComponent createComponent(Class type, Color color)
	{
		if (type == null) type = JLabel.class;
		JComponent comp = null;
		if (JLabel.class.equals(type)) comp = new JLabel();
		else if (OMETextField.class.equals(type)) comp = new OMETextField();
		else if (OMETextArea.class.equals(type)) comp = new OMETextArea();
		else if (NumericalTextField.class.equals(type)) {
			comp = new NumericalTextField();
			((NumericalTextField) comp).setNegativeAccepted(true);
			comp.setBorder(null);
		}
				
		if (comp == null) comp = new JLabel();
		comp.setBackground(BACKGROUND_COLOR);
		Font font = comp.getFont();
		comp.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
		if (color != null) comp.setForeground(color);
		return comp;
	}
	
	/**
	 * Converts the passed string into a number.
	 * 
	 * @param value The value to parse.
	 * @param type	The type of number.
	 * @return See above.
	 */
	public static Number extractNumber(String value, Class type)
	{
		if (value == null) return null;
		try {
			if (Integer.class.equals(type)) 
				return Integer.parseInt(value);
			else if (Float.class.equals(type)) 
				return Float.parseFloat(value);
			else if (Double.class.equals(type)) 
				return Double.parseDouble(value);
		} catch (Exception e) {}
		return null;
	}

	/**
	 * Get the largest available memory available for graphics.
	 * @return see above.
	 */
	public static int getGraphicsMemory()
	{
		int bytesMax = 0;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try 
		{
			GraphicsDevice[] gs = ge.getScreenDevices();
	    
			for (int i=0; i<gs.length; i++) 
			{
			    VolatileImage im = gs[i].getDefaultConfiguration().createCompatibleVolatileImage(1, 1);
			    bytesMax = Math.max(bytesMax,gs[i].getAvailableAcceleratedMemory());
			}
			return bytesMax;
		}
		catch(HeadlessException e)
		{
			return 0;
		}
	}
	
	/**
	 * Get the free memory available in the system.
	 * @return see above.
	 */
	public static long getFreeMemory()
	{
		Runtime r = Runtime.getRuntime();
    	return r.freeMemory();
	}
	
	/**
	 * Get the total memory available to the JVM.
	 * @return see above.
	 */
	public static long getTotalMemory()
	{
		Runtime r = Runtime.getRuntime();
		return r.totalMemory();
	}
	
	/**
	 * Return the amount of memory used in JVM.
	 * @return see above.
	 */
	public static long getUsedMemory()
	{
		return getTotalMemory()-getFreeMemory();
	}

	/**
	 * Creates a separator of the specified height.
	 * 
	 * @param h The desired height.
	 * @return See above.
	 */
	public static JSeparator createSeparator(int h)
	{
		if (h <= 0) h = DEFAULT_ICON_HEIGHT;
		JSeparator s = new JSeparator(JSeparator.VERTICAL);
		Dimension d = s.getPreferredSize();
		s.setMaximumSize(new Dimension(d.width, h));
		return s;
	}
	
    /**
     * Removes the extension if any of the passed image's name.
     * 
     * @param originalName The name to handle.
     * @return See above.
     */
    public static String removeFileExtension(String originalName)
    {
    	String name = originalName;
    	String[] l = UIUtilities.splitString(originalName);
    	if (l != null) {
    		 int n = l.length;
             if (n >= 1) name = l[n-1]; 
    	} else {
    		if (Pattern.compile("\\.").matcher(name).find()) {
        		l = name.split("\\.");
        		if (l.length >= 1) {
        			name = "";
        			int n = l.length-1;
            		for (int i = 0; i < n; i++) {
        				name += l[i];
        				if (i < (n-1)) name += ".";
        			}
        		}
        	}
    		if (name.length() == 0) name = originalName;
    		return name;
    	}
    	   	
    	if (Pattern.compile("\\.").matcher(name).find()) {
    		l = name.split("\\.");
    		if (l.length >= 1) {
    			name = "";
    			int n = l.length-1;
        		for (int i = 0; i < n; i++) {
    				name += l[i];
    				if (i < (n-1)) name += ".";
    			}
    		}
    	}
    	if (name.length() == 0) name = originalName;
        return name;
    }
    
	/**
	 * Returns the name to display for a file.
	 * 
	 * @param fullPath The file's absolute path.
	 * @param number The number of folder to set the name.
	 * @return See above.
	 */
	public static String getDisplayedFileName(String fullPath, Integer number)
	{
		if (fullPath == null) return fullPath;
		String[] l = UIUtilities.splitString(fullPath);
    	String extension = null;
    	if (fullPath.endsWith("\\")) extension = "\\";
    	else if (fullPath.endsWith("/")) extension = "/";
    	String start = null;
    	if (fullPath.startsWith("\\")) start = "\\";
    	else if (fullPath.startsWith("/")) start = "/";
    	String sep = UIUtilities.getStringSeparator(fullPath);
    	if (sep == null) sep = "";
    	String text = "";
    	int folder = -1;
    	if (number != null && number >= 0) folder = (Integer) number;
    	if (folder == -1) return null;
    	if (l != null && l.length > 1) {
    		int n = 0;
    		if (folder < l.length) n = l.length-folder-2;
    		if (n < 0) n = 0;
    		int m = l.length-1;
    		for (int i = l.length-1; i > n; i--) {
    			if (i == m) text = l[i];
    			else text = l[i]+sep+text;
			}
    		if (n == 0 && start != null) text = start+text;
    		if (extension != null) text = text+extension;
    		return text;
    	}
    	return null;
	}
	
	/**
	 * Formats the passed string.
	 * 
	 * @param name The string to format.
	 * @param max  The maximum number of characters per line.
	 * @return See above.
	 */
	public static final String formatString(String name, int max) 
	{
		if (name == null) return "";
		if (max <= 0) max = MAX_CHARACTER;
		StringBuffer buf = new StringBuffer();
		int index = 0;
		for (int i = 0; i < name.length(); i++) {
			if (index == max) {
				index = 0;
				buf.append("<br>");
			}
			buf.append(name.charAt(i));
			index++;
		}
		return buf.toString();
	}
	
	/**
	 * Returns <code>true</code> if the OS is MAC, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isMacOS()
	{
		 String osName = System.getProperty("os.name").toLowerCase();
		 return osName.startsWith("mac os");
	}
	
	/**
	 * Returns <code>true</code> if the OS is Windows, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isWindowsOS()
	{
		 String osName = System.getProperty("os.name").toLowerCase();
		 return osName.startsWith("windows");
	}
	
	/**
	 * Returns <code>true</code> if the OS is Linux, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isLinuxOS()
	{
		if (isWindowsOS()) return false;
		if (isMacOS()) return false;
		return true;
	}
	
	/**
	 * Converts the passed color.
	 * 
	 * @param c The color to handle.
	 * @return See above.
	 */
	public static int convertColor(Color c)
	{
		int alpha = c.getAlpha();
		if (alpha == 0) alpha = 255;
		return ((alpha & 0xFF) << 24) |
    	((c.getRed() & 0xFF) << 16) |
    	((c.getGreen() & 0xFF) << 8)  |
    	((c.getBlue() & 0xFF) << 0);
	}
	
}
