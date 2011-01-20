/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * The Wiki component.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class OMEWikiComponent 
	extends JPanel
	implements ActionListener
{

	/** Bounds property indicating that a data object has been selected. */
	public static final String WIKI_DATA_OBJECT_PROPERTY = "wikiDataObject";
	
	/** Regex expression for text. */
	public static final String TEXTREGEX = OMEWikiConstants.TEXTREGEX;
	
	/** Regex for a sentence. */
	public static final String SENTENCEREGEX = OMEWikiConstants.SENTENCEREGEX;
	
	/** Regex for a sequence of characters. */
	public static final String CHARACTERREGEX = OMEWikiConstants.CHARACTERREGEX;
	
	/** Regex for a wiki link. */
	public static final String WIKILINKREGEX = OMEWikiConstants.WIKILINKREGEX;
	
	/** Regex expression defining Thumbnail [Thumbnail: 30]. */
	public static final String THUMBNAILREGEX = OMEWikiConstants.THUMBNAILREGEX;
	
	/** Regex expression defining Dataset [Dataset: 30]. */
	public static final String DATASETREGEX = OMEWikiConstants.DATASETREGEX;
	
	/** Regex expression defining Project [Project: 30]. */
	public static final String PROJECTREGEX = OMEWikiConstants.PROJECTREGEX;
	
	/** Regex expression defining Protocol [Protocol: 30]. */
	public static final String PROTOCOLREGEX = OMEWikiConstants.PROTOCOLREGEX;
	
	/** Regex expression defining Image [Image: 30]. */
	public static final String IMAGEREGEX = OMEWikiConstants.IMAGEREGEX;
	
	/** Regex expression defining Wiki Heading. */
	public static final String HEADINGREGEX = OMEWikiConstants.HEADINGREGEX;
	
	/** Regex for a bullet list. */
	public static final String BULLETREGEX = OMEWikiConstants.BULLETREGEX;

	/** Regex for bold. */
	public static final String BOLDREGEX = OMEWikiConstants.BOLDREGEX;

	/** Italic regex. */
	public static final String ITALICREGEX = OMEWikiConstants.ITALICREGEX;

	/** Italic and bold regex. */
	public static final String ITALICBOLDREGEX = 
										OMEWikiConstants.ITALICBOLDREGEX;

	/** Indent regex. */
	public static final String INDENTREGEX = OMEWikiConstants.INDENTREGEX;
		
	/** Regex expression defining url. */
	public static final String URLREGEX = OMEWikiConstants.URLREGEX;
	
	/** Regex for names linked regex. */
	public static final String NAMEDLINKREGEX = OMEWikiConstants.NAMEDLINKREGEX;

	/** Action id to create an hyperlink. */
	private static final int	HYPERLINK = 0;
	
	/** Action id to create an image's entry. */
	private static final int	IMAGE = 1;
	
	/** Action id to create a protocol's entry. */
	private static final int	PROTOCOL = 2;
	
	/** The formatters installed by default. */
	private static Map<String, FormatSelectionAction> DEFAULT_FORMATTERS;
	
	static {
		DEFAULT_FORMATTERS = new LinkedHashMap<String, FormatSelectionAction>();
		DEFAULT_FORMATTERS.put(URLREGEX, 
				new FormatSelectionAction(
				new ColourFormatter(Formatter.DEFAULT_URL, false), 
				new URLLaunchAction()));
		/*
		DEFAULT_FORMATTERS.put(IMAGEREGEX, 
				new FormatSelectionAction(
				new ColourFormatter(Formatter.DEFAULT_LINK), 
				new ElementSelectionAction(WikiDataObject.IMAGE)));
		DEFAULT_FORMATTERS.put(PROTOCOLREGEX, 
				new FormatSelectionAction(
				new ColourFormatter(Formatter.PROTOCOL_LINK), 
				new ElementSelectionAction(WikiDataObject.PROTOCOL)));
				*/
	}
	
	/** The edit pane. */
	private OMEEditPane 	pane;
	
	/** The tool bar. */
	private JToolBar 		toolBar;
	
	/** The default components displayed in the tool bar. */
	private List<JButton>	toolBarActions;
	
	/** This text will be removed when starting typing. */
	private String			defaultText;
	
	/** Installs the default actions.  */
	private void installDefaultAction()
	{
		toolBarActions = new ArrayList<JButton>();
		IconManager icons = IconManager.getInstance();
		JButton b = new JButton(icons.getIcon(IconManager.HYPERLINK));
		//UIUtilities.unifiedButtonLookAndFeel(b);
		b.addActionListener(this);
		b.setActionCommand(""+HYPERLINK);
		b.setToolTipText(OMEWikiConstants.HYPERLINK_TOOLTIP);
		toolBarActions.add(b);
		/*
		b = new JButton(icons.getIcon(IconManager.IMAGE));
		b.setToolTipText(OMEWikiConstants.IMAGE_TOOLTIP);
		b.addActionListener(this);
		b.setActionCommand(""+IMAGE);
		toolBarActions.add(b);
		b = new JButton(icons.getIcon(IconManager.FILE_EDITOR));
		b.setToolTipText(OMEWikiConstants.PROTOCOL_TOOLTIP);
		b.addActionListener(this);
		b.setActionCommand(""+PROTOCOL);
		//UIUtilities.unifiedButtonLookAndFeel(b);
		 *
		 */
		toolBarActions.add(b);
	}
	
	/**
	 * Creates a new component.
	 * 
	 * @param formatters 	The formatters for the text.
	 * @param toolbar		Pass <code>true</code> to install a default toolbar, 
	 * 						<code>false</code> otherwise.
	 */
	private void initComponents(Map<String, FormatSelectionAction> formatters,
								boolean toolbar)
	{
		defaultText = "";
		pane = new OMEEditPane(this, formatters);
		installDefaultAction();
		if (toolbar) {
			toolBar = new JToolBar();
			toolBar.setBackground(UIUtilities.BACKGROUND_COLOR);
			toolBar.setBorder(null);
			toolBar.setFloatable(false);
			Iterator<JButton> b = toolBarActions.iterator();
			while (b.hasNext()) 
				toolBar.add(b.next());
		}
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		if (toolBar != null) {
			JPanel p = new JPanel();
			p.setBackground(UIUtilities.BACKGROUND_COLOR);
			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
			p.add(Box.createVerticalStrut(5));
			JPanel bar = UIUtilities.buildComponentPanel(toolBar, 0, 0);
			bar.setBackground(UIUtilities.BACKGROUND_COLOR);
			p.add(bar);
			p.add(Box.createVerticalStrut(2));
			add(p, BorderLayout.NORTH);
		}
		add(pane, BorderLayout.CENTER);
	}
	
	/** 
	 * Returns the default text or an empty string.
	 * 
	 * @return See above.
	 */
	String getDefaultText() { return defaultText; }
	
	/** Creates a default new instance. */
	public OMEWikiComponent()
	{
		this(DEFAULT_FORMATTERS, true);
	}
	
	/**
	 * Creates a new instance with default formatters.
	 * 
	 * @param toolBar 	Pass <code>true</code> to install a default toolbar, 
	 * 					<code>false</code> otherwise.
	 */
	public OMEWikiComponent(boolean toolBar)
	{
		this(DEFAULT_FORMATTERS, toolBar);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param formatters 	The formatters for the text.
	 * @param toolBar		Pass <code>true</code> to install a default toolbar,
	 * 						<code>false</code> otherwise.
	 */
	public OMEWikiComponent(Map<String, FormatSelectionAction> formatters, 
			boolean toolBar)
	{
		Entry entry;
		Iterator i = DEFAULT_FORMATTERS.entrySet().iterator();
		String key;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			if (!formatters.containsKey(key))
				formatters.put(key, (FormatSelectionAction) entry.getValue());
		}
		initComponents(formatters, toolBar);
		buildGUI();
	}
	
	/**
	 * Called when the mouse is pressed and calls the actionPerformed event for
	 * the appropriate SelectionAction of the formatter.value.
	 * 
	 * @param action 	The action to handle.
	 * @param text		The selected text.
	 * @param count		The number of count.
	 */
	void onSelection(SelectionAction action, String text, int count)
	{
		if (action == null) return;
		//depending on the type of action.
		if ((action instanceof ElementSelectionAction) && count == 2) {
			action.onSelection(text);
			ElementSelectionAction a = (ElementSelectionAction) action;
			int index = a.getWikiDataObjectIndex();
			long id = a.getObjectID();
			if (id >= 0) {
				WikiDataObject object = new WikiDataObject(index, id);
				firePropertyChange(WIKI_DATA_OBJECT_PROPERTY, null, object);
			}
		} else if ((action instanceof URLLaunchAction) && count == 2) {
			action.onSelection(text);
		}
	}

	/**
	 * Returns <code>true</code> if the passed text is the default text,
	 * <code>false</code> otherwise.
	 * 
	 * @param text The value to check.
	 * @return See above.
	 */
	boolean isDefaultText(String text)
	{
		if (text == null) return false;
		return (defaultText.equals(text.trim()));
	}
	
	/**
	 * Sets the default text.
	 * 
	 * @param text The value to set.
	 */
	public void setDefaultText(String text)
	{
		if (text == null) return;
		defaultText = text.trim();
	}
	
	/**
	 * Sets the text.
	 * 
	 * @param text The value to set.
	 */
	public void setText(String text) { pane.setText(text); }
	
	/**
	 * Returns the text.
	 * 
	 * @return See above.
	 */
	public String getText() { return pane.getText(); }
	
	/**
	 * Adds the specified listener to the {@link #pane}.
	 * 
	 * @param listener The listener to add.
	 */
	public void addDocumentListener(DocumentListener listener)
	{
		if (listener != null) pane.getDocument().addDocumentListener(listener);
	}
	
	/**
	 * Removes the specified listener from the {@link #pane}.
	 * 
	 * @param listener The listener to add.
	 */
	public void removeDocumentListener(DocumentListener listener)
	{
		if (listener != null) 
			pane.getDocument().removeDocumentListener(listener);
	}
	
	/**
	 * Sets the border of the component.
	 * 
	 * @param border The component to set.
	 */
	public void setComponentBorder(Border border)
	{
		pane.setBorder(border);
	}
	
	/**
	 * Overridden to set the backgound color of all the components.
	 * @see JPanel#setBackground(Color)
	 */
	public void setBackground(Color color)
	{
		super.setBackground(color);
		Component[] comp = getComponents();
		if (comp != null) {
			for (int i = 0; i < comp.length; i++) {
				if (comp[i] instanceof JComponent) {
					((JComponent) comp[i]).setBackground(color);
				}
			}
		}
		
		if (pane != null) pane.setBackground(color);
		if (toolBar != null) {
			toolBar.setBackground(color);
			comp = toolBar.getComponents();
			for (int i = 0; i < comp.length; i++) {
				if (comp[i] instanceof JComponent) {
					((JComponent) comp[i]).setBackground(color);
				}
			}
		}
	}
	
	/**
	 * Overridden to set the foreground color of the {@link #pane}.
	 * @see JPanel#setForeground(Color)
	 */
	public void setForeground(Color color)
	{
		if (pane != null) pane.setForeground(color);
	}

	/**
	 * Overridden to set the font of {@link #pane}.
	 * @see JPanel#setFont(Font)
	 */
	public void setFont(Font font)
	{
		if (pane != null) pane.setFont(font);
	}
	
	/**
	 * Overridden to set the font of {@link #pane}.
	 * @see JPanel#getFont()
	 */
	public Font getFont()
	{
		if (pane == null) return super.getFont();
		return pane.getFont();
	}
	
	/**
	 * Inserts text depending on the action.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		String s = getText();
		switch (index) {
			case IMAGE:
				if (isDefaultText(s))
					setText(OMEWikiConstants.DEFAULT_IMAGE);
				else setText(s+" "+OMEWikiConstants.DEFAULT_IMAGE);
				pane.requestFocus();
				break;
			case HYPERLINK:
				if (isDefaultText(s))
					setText(OMEWikiConstants.DEFAULT_HYPERLINK);
				else setText(s+" "+OMEWikiConstants.DEFAULT_HYPERLINK);
				pane.requestFocus();
				break;
			case PROTOCOL:
				if (isDefaultText(s))
					setText(OMEWikiConstants.DEFAULT_PROTOCOL);
				else setText(s+" "+OMEWikiConstants.DEFAULT_PROTOCOL);
				pane.requestFocus();
		}
	}
	
}
