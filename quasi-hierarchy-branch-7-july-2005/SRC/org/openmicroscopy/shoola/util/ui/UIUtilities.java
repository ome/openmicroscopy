/*
 * org.openmicroscopy.shoola.util.ui.UIUtilities
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

package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.BorderLayout;
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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

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
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class UIUtilities
{

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
     * Creates a modal JDialog containing the specified JComponent
     * for the specified parent.
     * The newly created dialog is then centered on the screen and made visible.
	 *
     *@param parent     The parent component.
     *@param title      The title of the dialog.
     *@param JComponent The component to display.
     *
     *@see #centerAndShow(Component)
	 */
    public static void makeForDialog(Component parent, String title, 
                                    JComponent c)
    {
        if (c == null) return;
        JDialog dialog = null;
        if (parent instanceof Frame) 
            dialog = new JDialog((Frame) parent);
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
     * Create a modal JDialog with no title. 
     * @see #makeForDialog(Component, String)
     * 
     * */
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
	 * @param   toolTipText     The textual content of the tool tip.
	 * @return  An <i>HTML</i> fomatted string to be passed to 
	 * 			<code>setToolTipText()</code>.
	 */
	public static String formatToolTipText(String toolTipText) 
	{
		StringBuffer buf = new StringBuffer(90+toolTipText.length());
		buf.append("<html><body bgcolor=#FFFCB7 text=#AD5B00>");
		//TODO: change into platform independent font
		buf.append("<font face=Arial size=2>");  
		buf.append(toolTipText);
		buf.append("</font></body></html>");
		return buf.toString();
	} 
	
    /**
     * Builds a tool tip in a fixed font and color.
     * 
     * @param title
     * @param body
     * @param maxWidth
     * @return
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
	 * @param button	button to add to the toolBar. The height of the 
	 * 					separator depends of the insets of the button.
	 * @param icon		icon to add to the button. The height of the 
	 * 					separator depends of the height of the icon.
	 * @return
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
	
    /** Set the font of the string to bold. */
    public static JLabel setTextFont(String s)
    {
        JLabel label = new JLabel(s);
        Font font = label.getFont();
        Font newFont = font.deriveFont(Font.BOLD);
        label.setFont(newFont);
        return label;
    }
    
    /** Wrap a JComponent in a JPanel. */
    public static JPanel buildComponentPanel(JComponent component)
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(component);
        return p;
    }
    
    /** Wrap a JComponent in a JPanel. */
    public static JPanel buildComponentPanelRight(JComponent component)
    {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(component);
        return p;
    }
    
}
