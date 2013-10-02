/*
 * org.openmicroscopy.shoola.util.ui.UIUtilities
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
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
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;


//Third-party libraries
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
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
	
	/** Bound property indicating that the font changes.*/
	public static final String HINTS_PROPERTY = "awt.font.desktophints";
	
	/** The default background color.*/
	public static final Color TOOLTIP_COLOR = new Color(255, 252, 180, 200);
	
	
	/** The default number of characters for the partial name.*/
	public static final int DEFAULT_NUMBER_OF_CHARACTERS = 30;
	
	/** Indicates that the image is predominantly <code>red</code>.*/
	public static final int RED_COLOR = 0;
	
	/** Indicates that the image is predominantly <code>green</code>.*/
	public static final int GREEN_COLOR = 1;
	
	/** Indicates that the image is predominantly <code>blue</code>.*/
	public static final int BLUE_COLOR = 2;
	
	/** The maximum number read at once. */
	public static final int	BYTES = 1024;
	
	/** The value used to compare double and float. */
	public final static double EPSILON = 0.00001;
	
	/** The number of lines displayed for error. */
	public static final int MAX_LINES_EXCEPTION = 20;
	
	/** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
	public static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
	/** Letter corresponding to number. */
	public static final Map<Integer, String> LETTERS;
	
	/** The maximum number of characters in a line for the tool tip. */
	public static final int					MAX_CHARACTER = 40;
	
	/** A light grey color for line borders etc. */
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
    
    /** The color of the required fields. */
    public static final Color				REQUIRED_FIELDS_COLOR = Color.RED;
    
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

	/** Unicode for the squared symbol. */
	public final static String  			SQUARED_SYMBOL =  "²";
	
	/** Unicode for the squared symbol. */
	public final static String  			DELTA_SYMBOL =  "Δ";
	
	/** Pixels string. */
	public final static String  			PIXELS_SYMBOL = "px";
    
	/** Background color of the highlighted node. */
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
	public static final long		MEGABYTE = 1048576;
    
	/** Key value for the default folder. */
    private static final String 			DEFAULT_FOLDER = "defaultFolder";
    
    /** The pattern to format date. */
    public static final String				WDMY_FORMAT = 
    											"E dd MMM yyyy, HH:mm:ss";

    /** The pattern to format date. */
    public static final String				D_M_Y_FORMAT = "dd-MM-yyyy";
    
    /** The text displayed in the tool tip of the calendar button. */
	private static final String		DATE_TOOLTIP = "Bring up a calendar.";
	
	/** The maximum width of the text when wrapping up text. */
	private static final int		WRAP_UP_MAX_WIDTH = 50;

	/** The standard multiplier prefixes. */
	private static final String UNIT_PREFIXES = "KMGTPEZY";

	private static final List<String> CHARACTERS;
	
	/** The fonts used for ROI.*/
	private static final Map<String, String> FONTS;

	static {
		CHARACTERS = new ArrayList<String>();
		CHARACTERS.add("[");
		CHARACTERS.add("]");
		CHARACTERS.add("\"");
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
		LETTERS.put(27, "AA");
		LETTERS.put(28, "AB");
		LETTERS.put(29, "AC");
		LETTERS.put(30, "AD");
		LETTERS.put(31, "AE");
		LETTERS.put(32, "AF");
		LETTERS.put(33, "AG");
		LETTERS.put(34, "AH");
		LETTERS.put(35, "AI");
		LETTERS.put(36, "AJ");
		LETTERS.put(37, "AK");
		LETTERS.put(38, "AL");
		LETTERS.put(39, "AM");
		LETTERS.put(40, "AN");
		LETTERS.put(41, "AO");
		LETTERS.put(42, "AP");
		LETTERS.put(43, "AQ");
		LETTERS.put(44, "AR");
		LETTERS.put(45, "AS");
		LETTERS.put(46, "AT");
		LETTERS.put(47, "AU");
		LETTERS.put(48, "AV");
		LETTERS.put(49, "AW");
		LETTERS.put(50, "AX");
		LETTERS.put(51, "AY");
		LETTERS.put(52, "AZ");
		LETTERS.put(53, "BA");
		LETTERS.put(54, "BB");
		LETTERS.put(55, "BC");
		LETTERS.put(56, "BD");
		LETTERS.put(57, "BE");
		LETTERS.put(58, "BF");
		LETTERS.put(59, "BG");
		LETTERS.put(60, "BH");
		LETTERS.put(61, "BI");
		LETTERS.put(62, "BJ");
		LETTERS.put(63, "BK");
		LETTERS.put(64, "BL");
		LETTERS.put(65, "BM");
		LETTERS.put(66, "BN");
		LETTERS.put(67, "BO");
		LETTERS.put(68, "BP");
		LETTERS.put(69, "BQ");
		LETTERS.put(70, "BR");
		LETTERS.put(71, "BS");
		LETTERS.put(72, "BT");
		LETTERS.put(73, "BU");
		LETTERS.put(74, "BV");
		LETTERS.put(75, "BW");
		LETTERS.put(76, "BX");
		LETTERS.put(77, "BY");
		LETTERS.put(78, "BZ");
		
		FONTS = new HashMap<String, String>();
		FONTS.put("Arial", "sans-serif");
		FONTS.put("Arial Black", "sans-serif");
		FONTS.put("Book Antiqua", "serif");
		FONTS.put("Charcoal", "sans-serif");
		FONTS.put("Comic Sans",	"cursive");
		FONTS.put("Comic Sans MS",	"cursive");
		FONTS.put("Courier", "monospace");
		FONTS.put("Courier New", "monospace");
		FONTS.put("Gadget",	"sans-serif");
		FONTS.put("Geneva",	"sans-serif");
		FONTS.put("Georgia", "serif");
		FONTS.put("Helvetica",	"sans-serif");
		FONTS.put("Impact",	"sans-serif");
		FONTS.put("Lucida Console", "monospace");
		FONTS.put("Lucida Grande", "sans-serif");
		FONTS.put("Lucida Sans Unicode", "sans-serif");
		FONTS.put("Monaco", "monospace");
		FONTS.put("MS Sans Serif", "sans-serif");
		FONTS.put("MS Serif", "serif");
		FONTS.put("New York", "serif");
		FONTS.put("Palatino", "serif");
		FONTS.put("Palatino Linotype", "serif");
		FONTS.put("Tahoma", "sans-serif");
		FONTS.put("Times", "serif");
		FONTS.put("Times New Roman", "serif");
		FONTS.put("Trebuchet MS", "sans-serif");
		FONTS.put("Verdana", "sans-serif");
		FONTS.put("Roman", "serif");
		FONTS.put("Swis", "sans-serif");
		FONTS.put("Script", "cursive");
		FONTS.put("Decorative", "fantasy");
		FONTS.put("serif", "serif");
		FONTS.put("sans-serif", "sans-serif");
		FONTS.put("cursive", "cursive");
		FONTS.put("fantasy", "fantasy");
		FONTS.put("monospace", "monospace");
		FONTS.put("Andale Mono", "sans-serif");
		FONTS.put("Antiqua", "serif");
		FONTS.put("Avqest", "serif");
		FONTS.put("Blackletter", "serif");
		FONTS.put("Calibri", "sans-serif");
		FONTS.put("Fraktur", "serif");
		FONTS.put("Frosty", "serif");
		FONTS.put("Garamond", "serif");
		FONTS.put("Minion", "serif");
		FONTS.put("Monotype.com", "sans-serif");
		FONTS.put("Bitstream Vera Sans", "sans-serif");
		FONTS.put("Bitstream Vera Sans Mono", "monospace");
		FONTS.put("Bitstream Vera Serif", "serif");
		FONTS.put("Caslon Roman", "serif");
		FONTS.put("Charis SIL", "serif");
		FONTS.put("DejaVu Sans", "sans-serif");
		FONTS.put("DejaVu Sans Mono", "monospace");
		FONTS.put("DejaVu Serif", "serif");
		FONTS.put("Doulos SIL", "serif");
		FONTS.put("Droid Sans", "sans-serif");
		FONTS.put("Droid Sans Mono", "monospace");
		FONTS.put("Droid Serif", "serif");
		FONTS.put("FreeMono", "monospace");
		FONTS.put("FreeSans", "sans-serif");
		FONTS.put("FreeSerif", "serif");
		FONTS.put("Gentium", "serif");
		FONTS.put("GNU Unifont", "monospace");
		FONTS.put("Junicode", "serif");
		FONTS.put("Liberation Mono", "monospace");
		FONTS.put("Liberation Sans", "sans-serif");
		FONTS.put("Liberation Sans Narrow", "sans-serif");
		FONTS.put("Liberation Serif", "serif");
		FONTS.put("Linux Biolinum", "sans-serif");
		FONTS.put("Linux Libertine", "serif");
		FONTS.put("Luxi Mono", "monospace");
		FONTS.put("Luxi Sans", "sans-serif");
		FONTS.put("Luxi Serif", "serif");
		FONTS.put("American Typewriter", "serif");
		FONTS.put("Apple Casual", "cursive");
		FONTS.put("Apple Chancery", "cursive");
		FONTS.put("Apple Garamond", "serif");
		FONTS.put("Baskerville", "serif");
		FONTS.put("Big Caslon", "serif");
		FONTS.put("Brush Script", "cursive");
		FONTS.put("Chalkboard", "sans-serif");
		FONTS.put("Chicago", "sans-serif");
		FONTS.put("Cochin", "serif");
		FONTS.put("Cooper", "serif");
		FONTS.put("Copperplate", "serif");
		FONTS.put("Didot", "serif");
		FONTS.put("Futura", "sans-serif");
		FONTS.put("Gill Sans", "sans-serif");
		FONTS.put("Helvetica Neue", "sans-serif");
		FONTS.put("Herculanum", "cursive");
		FONTS.put("Hoefler Text", "serif");
		FONTS.put("LiSong Pro", "serif");
		FONTS.put("Marker Felt", "cursive");
		FONTS.put("Menlo", "sans-serif");
		FONTS.put("New York", "sans-serif");
		FONTS.put("Optima", "sans-serif");
		FONTS.put("Papyrus", "sans-serif");
		FONTS.put("Sand", "cursive");
		FONTS.put("Skia", "sans-serif");
		FONTS.put("Techno", "sans-serif");
		FONTS.put("Textile", "cursive");
		FONTS.put("Zapf Chancery", "cursive");
		FONTS.put("Zapfino", "cursive");
	}
	
	/**
     * Returns <code>true</code> if the passed value is textual,
     * <code>false</code> otherwise.
     * 
     * @param value See above.
     * @return See above.
     */
    private static boolean isTextOnly(String value)
    {
    	Iterator<String> i = CHARACTERS.iterator();
    	while (i.hasNext()) {
    		if (value.contains(i.next()))
    			return false;
		}
    	return true;
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
		if (location == null) centerAndShow(window);
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
	 * @return An <i>HTML</i> formatted string to be passed to 
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
	 * @return An <i>HTML</i> formatted string to be passed to 
	 * 			<code>setToolTipText()</code>.
	 */
	public static String formatToolTipText(List<String> toolTipText) 
	{
		if (toolTipText == null) return "";
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
	 * You pass the tool tip text and get back an <i>HTML</i> string to be
	 * passed, in turn, to the <code>setToolTipText</code> method of a 
	 * {@link javax.swing.JComponent}.
	 *
	 * @param toolTipText     The textual content of the tool tip.
	 * @return An <i>HTML</i> formatted string to be passed to 
	 * 			<code>setToolTipText()</code>.
	 */
	public static String formatToolTipText(String[] toolTipText) 
	{
		if (toolTipText == null) return "";
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body>");
		buf.append("<font face=Arial size=2>");  
		buf.append("<p>");
		for (int i = 0; i < toolTipText.length; i++) {
			buf.append(toolTipText[i]);
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
     * with a left flow layout.
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
     * with a left flow layout.
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
     * with a left flow layout.
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
     * with a left flow layout.
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
     * with a right flow layout.
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
     * with a right flow layout.
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
     * with a right flow layout.
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
     * with a right flow layout.
     * 
     * @param component The component to add.
     *  @param hgap    	The horizontal gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @param vgap    	The vertical gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @return See below.
     */
    public static JPanel buildComponentPanelCenter(JComponent component, int 
    		hgap, int vgap)
    {
        return buildComponentPanelCenter(component, hgap, vgap, true);
    }
    
    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a right flow layout.
     * 
     * @param component The component to add.
     *  @param hgap    	The horizontal gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @param vgap    	The vertical gap between components and between the 
     * 					components and the borders of the 
     * 					<code>Container</code>.
     * @return See below.
     */
    public static JPanel buildComponentPanelCenter(JComponent component, int 
    		hgap, int vgap, boolean opaque)
    {
        JPanel p = new JPanel();
        if (component == null) return p;
        if (hgap < 0) hgap = 0;
        if (vgap < 0) vgap = 0;
        p.setLayout(new FlowLayout(FlowLayout.CENTER, hgap, vgap));
        p.add(component);
        p.setOpaque(opaque);
        return p;
    }
    
    /**
     * Adds the specified {@link JComponent} to a {@link JPanel} 
     * with a right flow layout.
     * 
     * @param component The component to add.
     * @return See below.
     */
    public static JPanel buildComponentPanelCenter(JComponent component)
    {
        return buildComponentPanelCenter(component, 5, 5);
    }
    
    /**
     * Sets the UI properties of the button to unify the L&F.
     * 
     * @param b The button.
     */
    public static void unifiedButtonLookAndFeel(JComponent b)
    {
    	if (b == null) return;
        //b.setMargin(new Insets(0, 2, 0, 3));
        //b.setBorderPainted(false);
        //b.setFocusPainted(false);
    	b.setOpaque(false);
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
    	b.setContentAreaFilled(!isMacOS());
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
    public static String formatShortDateTime(Timestamp time) 
    {
    	if (time == null)  
    		time = getDefaultTimestamp();
    	return DateFormat.getDateTimeInstance(
    			DateFormat.SHORT, DateFormat.SHORT,
    			Locale.getDefault()).format(time);
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
    	return formatDate(time, WDMY_FORMAT);
    }
    
    /**
     * Formats as a <code>String</code> the specified time.
     * format: E dd MMM yyyy, HH:mm:ss
     * 
     * @param time The timestamp to format.
     * @param pattern The format pattern
     * @return Returns the stringified version of the passed timestamp.
     */
    public static String formatDate(Timestamp time, String pattern) 
    {
    	if (time == null) time = getDefaultTimestamp();
    	if (pattern == null || pattern.length() == 0) 
    		pattern = WDMY_FORMAT;
    	DateFormat df;
    	if (WDMY_FORMAT.equals(pattern))
    		df = DateFormat.getDateTimeInstance(
        			DateFormat.FULL, DateFormat.LONG, Locale.getDefault());
    	else df = 
    		DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
    	return df.format(time);
    }
    
    /**
     * Converts the time in seconds into hours, minutes and seconds.
     * 
     * @param timeInMilliSeconds The time in milliseconds to convert.
     * @return See above.
     */
    public static String calculateHMSFromMilliseconds(long timeInMilliSeconds)
    {
    	return calculateHMS((int) (timeInMilliSeconds/1000));
    }
    
    /**
     * Converts the time in seconds into hours, minutes and seconds.
     * 
     * @param timeInSeconds The time in seconds to convert.
     * @return See above.
     */
    public static String calculateHMS(int timeInSeconds)
    {
        int hours = timeInSeconds/3600;
        timeInSeconds = timeInSeconds-(hours*3600);
        int minutes = timeInSeconds/60;
        timeInSeconds = timeInSeconds-(minutes*60);
        int seconds = timeInSeconds;
        String text = "";
        if (hours > 0) {
        	text += hours;
        	text += " hour";
        }
        if (hours > 1) text += "s";
        if (minutes > 0) {
        	text += " "; 
        	text += minutes;
        	text += " minute";
        	if (minutes > 1) text += "s";
        }
        if (seconds > 0) {
        	text += " "; 
        	text += seconds;
        	text += " second";
        	if (seconds > 1) text += "s";
        }
        return text;
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
     * Returns the maximum number of decimal places which still result in a
     * non-zero rounded value.
     *
     * @param value The value to handle.
     * @param decimal The starting number of decimal places to test.
     * @return See above.
     */
    public static final int findDecimal(double value, int decimal)
    {
    	double testValue = Math.abs(value);
    	double v = round(testValue, decimal);
    	if (v > 0) return decimal;
    	decimal++;
    	return findDecimal(testValue, decimal);
    }

    /**
     * Rounds the passed value to the specified number of decimals.
     * 
     * @param value 	The value to round.
     * @param decimal 	The number of figures after decimal point.
     * @return The rounded value.
     */
    public static double ceil(double value, int decimal)
    {
    	if (decimal <= 0) return value;
    	double p = Math.pow(10, decimal);
    	value = value*p;
    	return Math.ceil(value)/p;
    }
    
    /**
     * Rounds the passed value to the specified number of decimals.
     * 
     * @param value 	The value to round.
     * @param decimal 	The number of figures after decimal point.
     * @return The rounded value.
     */
    public static double floor(double value, int decimal)
    {
    	if (decimal <= 0) return value;
    	double p = Math.pow(10, decimal);
    	value = value*p;
    	return Math.floor(value)/p;
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
	 * Converts the passed value into a string in KB, MB, etc.,
	 * and returns a string version of it.
	 * 
	 * @param v The value to convert.
	 * @return See above.
	 */
	public static String formatFileSize(long v)
	{
		final long prefixStep = 1 << 10;
		final String s;
		if (v < 1) {
		    s = "0 bytes";
		} else if (v == 1) {
		    s = "1 byte";
		} else if (v < prefixStep) {
			s = v + " bytes";
		} else {
		    double vd = v;
		    final int maxIndex = UNIT_PREFIXES.length() - 1;
		    int index;
		    for (index = -1; vd >= prefixStep && index < maxIndex; vd /= prefixStep, index++);
		    s = String.format("%.1f %cB", vd, UNIT_PREFIXES.charAt(index));
		}
		return s;
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
			((NumericalTextField) comp).setHorizontalAlignment(JTextField.LEFT);
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
    	return FilenameUtils.removeExtension(originalName);
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
		if (number == null || number.intValue() < 0)
			return fullPath;
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
	public static String formatString(String name, int max) 
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
		//String osName = System.getProperty("os.name").toLowerCase();
		//return osName.startsWith("mac os");
		return (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX);
	}
	
	/**
	 * Returns <code>true</code> if the OS is Windows, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isWindowsOS()
	{
		//String osName = System.getProperty("os.name").toLowerCase();
		//return osName.startsWith("windows");
		return (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_WINDOWS_2000 ||
				SystemUtils.IS_OS_WINDOWS_7 || SystemUtils.IS_OS_WINDOWS_95 || 
				SystemUtils.IS_OS_WINDOWS_98 || SystemUtils.IS_OS_WINDOWS_ME|| 
				SystemUtils.IS_OS_WINDOWS_NT || SystemUtils.IS_OS_WINDOWS_VISTA ||
				SystemUtils.IS_OS_WINDOWS_XP);
	}
	
	/**
	 * Returns <code>true</code> if the OS is MAC, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isLinuxOS()
	{
		return SystemUtils.IS_OS_LINUX;
	}
	
	/**
     * Returns the partial name of the image's name
     * 
     * @param originalName The original name.
     * @return See above.
     */
    public static String getPartialName(String originalName)
    {
    	if (originalName == null) return null;
    	String[] l = UIUtilities.splitString(originalName);
    	String extension = null;
    	if (originalName.endsWith("\\")) extension = "\\";
    	else if (originalName.endsWith("/")) extension = "/";
    	String sep = UIUtilities.getStringSeparator(originalName);
    	if (sep == null) sep = "";
    	if (l != null) {
    		int n = l.length;
    		switch (n) {
				case 0: return originalName;
				case 1: 
					if (extension != null) return l[0]+extension;
					return l[0];
				case 2: 
					if (extension != null) 
						return l[n-2]+sep+l[n-1]+extension;
					return l[n-2]+sep+l[n-1];
				default:
					//Check of this is actually a path.
					for (int i = 0; i < l.length; i++) {
						if (!isTextOnly(l[i]))
							return originalName;
					}
					if (extension != null) 
						return UIUtilities.DOTS+l[n-2]+sep+l[n-1]+extension;
					return UIUtilities.DOTS+l[n-2]+sep+l[n-1]; 
			}
    	}
        return originalName;
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
	
	/**
	* Converts a list to a CSV string.
	*
	* @param list The list to convert.
	* @return See above.
	*/
	public static String listToCSV(List<String> list)
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = 0 ; i < list.size() ; i++) {
			buffer.append(list.get(i));
			if (i < list.size()-1)
				buffer.append(",");
		}
		return buffer.toString();
	}
	
	/**
	* Converts a CSV string to a list of strings.
	*
	* @param str The CSV string to convert.
	* @return See above.
	*/
	public static List<String> CSVToList(String str)
	{
		List<String> list = new ArrayList<String>();
		String[] valueString = str.split(",");
		for (String value : valueString)
			if (!value.equals("[]"))
				list.add(value);
		return list;
	}
	
	/**
	 * Makes sure the paths for the script are platform agnostic.
	 * 
	 * @param path The path
	 * @return The corrected path
	 */
	public static String toUnix(String path)
	{
		return path.replace('\\', '/');
	}
	
	/**
	 * Converting the passed value to make sure we can use it for OpenGL.
	 * 
	 * @param n The value to convert.
	 * @return See above.
	 */
    public static int ceilingPow2(int n)
    {
		int pow2 = 1;
		while (n > pow2) 
			pow2 = pow2<<1;
		return pow2;
	}
	
	/**
	 * Utility method to print an error message
	 * 
	 * @param e The exception to handle.
	 * @return  See above.
	 */
    public static String printErrorText(Throwable e) 
	{
		if (e == null) return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
    
    /**
     * Formats the exception to display in tool tip. Displays the first
     * {@link #MAX_LINES_EXCEPTION} lines.
     * 
     * @param ex The exception to handle.
     * @return See above.
     */
    public static String formatExceptionForToolTip(Throwable ex)
    {
    	return formatExceptionForToolTip(ex, MAX_LINES_EXCEPTION);
    }
    
    /**
     * Formats the exception to display in tool tip. Displays the specified
     * number of lines.
     * 
     * @param ex The exception to handle.
     * @param n  The number of lines to display.
     * @return See above.
     */
    public static String formatExceptionForToolTip(Throwable ex, int n)
    {
    	if (ex == null) return "";
    	if (n <= 0) n = MAX_LINES_EXCEPTION;
    	//ex.printStackTrace();
    	String s;
    	if (ex.getCause() != null) {
    		s = UIUtilities.printErrorText(ex.getCause());
    	} else s = UIUtilities.printErrorText(ex);
   		
   		String[] values = s.split("\n");
   		//Display the first 20 lines
   		String[] lines = values;
   		if (values.length > MAX_LINES_EXCEPTION) {
   			lines = new String[MAX_LINES_EXCEPTION+1];
   			for (int i = 0; i < lines.length-1; i++) {
   				lines[i] = values[i];
   			}
   			lines[lines.length-1] = 
   				"... "+(values.length-MAX_LINES_EXCEPTION)+" more";
   		}
   		return formatToolTipText(lines);
    }
    
    /**
     * Returns <code>true</code> if the passed colors are the same, 
     * <code>false</code> otherwise.
     * 
     * @param c1 One of the colors to check.
     * @param c2 One of the colors to check.
     * @param alpha Pass <code>true</code> to take into account the 
     * 				alpha component, <code>false</code> otherwise.
     * @return See above.
     */
    public static boolean isSameColors(Color c1, Color c2, boolean alpha)
    {
    	if (c1 == null || c2 == null) return false;
    	if (c1.getRed() != c2.getRed()) return false;
    	if (c1.getGreen() != c2.getGreen()) return false;
    	if (c1.getBlue() != c2.getBlue()) return false;
    	if (alpha) {
    		if (c1.getAlpha() != c2.getAlpha()) return false;
    	}
    	return true;
    }
    
    /**
     * Creates a button looking like an hyper-link.
     * 
     * @param text The text to display
     * @return See above.
     */
    public static JButton createHyperLinkButton(String text)
    {
    	if (text == null || text.trim().length() == 0)
    		text = "hyperlink";
    	JButton b = new JButton(text);
    	Font f = b.getFont();
    	b.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
		b.setOpaque(false);
		b.setForeground(UIUtilities.HYPERLINK_COLOR);
		unifiedButtonLookAndFeel(b);
		return b;
    }
    
    /**
     * Creates a button looking like an hyper-link.
     * 
     * @param text The text to display
     * @return See above.
     */
    public static JMenuItem createHyperLinkMenuItem(String text)
    {
    	if (text == null || text.trim().length() == 0)
    		text = "hyperlink";
    	JMenuItem b = new JMenuItem(text);
    	//Font f = b.getFont();
    	//b.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
		b.setOpaque(false);
		b.setForeground(UIUtilities.HYPERLINK_COLOR);
		unifiedButtonLookAndFeel(b);
		return b;
    }
    
    /**
     * Converts the font.
     * 
     * @param family The value to convert.
     * @return See above.
     */
    public static String convertFont(String family)
    {
    	if (family == null) return "";
    	String value = FONTS.get(family);
    	if (value == null || value.trim().length() == 0)
    		return "";
    	return value;
    }
    
	/** Builds the UI component displaying the exception.*/
	public static JTextPane buildExceptionArea()
	{
		StyleContext context = new StyleContext();
        StyledDocument document = new DefaultStyledDocument(context);

        JTextPane textPane = new JTextPane(document);
        textPane.setOpaque(false);
        textPane.setEditable(false);

        // Create one of each type of tab stop
        List<TabStop> list = new ArrayList<TabStop>();
        
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
        TabSet tabs = new TabSet(list.toArray(new TabStop[0]));
        
        // Add the tab set to the logical style;
        // the logical style is inherited by all paragraphs
        Style style = textPane.getLogicalStyle();
        StyleConstants.setTabSet(style, tabs);
        textPane.setLogicalStyle(style);
        Style debugStyle = document.addStyle("StyleName", null);
        StyleConstants.setForeground(debugStyle, Color.BLACK);
        StyleConstants.setFontFamily(debugStyle, "SansSerif");
        StyleConstants.setFontSize(debugStyle, 12);
        StyleConstants.setBold(debugStyle, false);
        return textPane;
	}

	/**
	 * Returns the converted value.
	 * 
	 * @param rgba The RGBA value to convert.
	 * @return See above.
	 */
	public static int convertRgbaToArgb(int rgba)
	{
		return (rgba >>> 8) | (rgba << (32-8));
	}
	
	/**
	 * Returns the converted value.
	 * 
	 * @param argb The ARGB value to convert.
	 * @return See above.
	 */
	public static int convertArgbToRgba(int argb)
	{
		return (argb << 8) | (argb >>> (32-8));
	}
	
	/**
	 * Returns {@link #RED}, {@link #RED} or {@link #RED} to indicate the
	 * range of the color.
	 * 
	 * @param color The color to handle.
	 * @return See above
	 */
	public static int getColorRange(Color color)
	{
		if (color == null) return -1;
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		if (g < r/2 && b < r/2) return RED_COLOR;
		if (r < g/2 && b < g/2) return GREEN_COLOR;
		return BLUE_COLOR;
	}
	
	/**
	 * Displays the end of the name if the name is longer that the number
	 * of specified characters.
	 * 
	 * @param name The name of handle.
	 * @param numberOfCharacters The number of characters.
	 * @return See above.
	 */
	public static String formatPartialName(String name, int numberOfCharacters)
	{
		if (name == null) return null;
		int n = DOTS.length()+numberOfCharacters;
		int m = name.length();
		if (m <= n) return name;
		return DOTS+name.substring(m-n, m);
	}
	
	/**
	 * Displays the end of the name if the name is longer that the number
	 * of specified characters.
	 * 
	 * @param name The name of handle.
	 * @param numberOfCharacters The number of characters.
	 * @return See above.
	 */
	public static String formatPartialName(String name)
	{
		return formatPartialName(name, DEFAULT_NUMBER_OF_CHARACTERS);
	}
	
    
    /**
     * Creates a new button and formats it using settings from action.
     * 
     * @param a The action to use.
     */
    public static JButton formatButtonFromAction(Action a)
    {
    	JButton button = new JButton();
    	button.setToolTipText((String) a.getValue(Action.SHORT_DESCRIPTION));
    	button.setIcon((Icon) a.getValue(Action.SMALL_ICON));
    	return button;
    }
    
    /**
	 * Transforms the size and returns the value and units.
	 * 
	 * @param value The value to transform.
	 * @return See above.
	 */
	public static UnitsObject transformSize(Double value)
	{
		double v = value.doubleValue();
		String units = UnitsObject.MICRONS;
		/* TODO: check if we want to introduce that.
		if (v < 1) {
			units = UnitsObject.NANOMETER;
			v *= 1000;
			if (v < 1) {
				units = UnitsObject.ANGSTROM;
				v *= 10;
			}
			return new UnitsObject(units, v);
		}
		*/
		if (v > 1000) {
			units = UnitsObject.MILLIMETER;
			v /= 1000;
		}
		if (v > 1000) {
			units = UnitsObject.CENTIMETER;
			v /= 1000;
		}
		if (v > 1000) {
			units = UnitsObject.METER;
			v /= 1000;
		}
		return new UnitsObject(units, v);
	}
	
	/**
     * Formats the passed value in seconds.
     * 
     * @param v The value to transform.
     * @return See above.
     */
    public static String formatTimeInSeconds(int v)
    {
    	if (v <= 0) return "";
    	int hours = v/3600;
    	int remainder = v%3600;
    	int minutes = remainder/60;
    	int seconds = remainder%60;
    	String text = "";
    	if (hours > 0) text += hours+"h";
    	if (minutes > 0) {
    		text += minutes+"min";
    		if (seconds > 0) text += seconds+"s";
    	} else text +=  seconds+"s";
	
		return text;
    }
}
