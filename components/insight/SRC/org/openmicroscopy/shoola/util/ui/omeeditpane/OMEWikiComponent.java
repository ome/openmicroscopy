/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.util.ArrayList;
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
import javax.swing.text.Document;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
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
 * @since 3.0-Beta4
 */
public class OMEWikiComponent 
	extends JPanel
	implements ActionListener
{

	/** Bounds property indicating that a data object has been selected. */
	public static final String WIKI_DATA_OBJECT_PROPERTY = "wikiDataObject";
	
	/** Bounds property indicating that a data object has been selected. */
	public static final String WIKI_DATA_OBJECT_ONE_CLICK_PROPERTY =
		"wikiDataObjectOneClick";

	/** Bounds property indicating that the text has been updated. */
    public static final String TEXT_UPDATE_PROPERTY = "textUpdate";

	/** Regular expression for text. */
	public static final String TEXTREGEX = OMEWikiConstants.TEXTREGEX;
	
	/** Regular for a sentence. */
	public static final String SENTENCEREGEX = OMEWikiConstants.SENTENCEREGEX;
	
	/** Regular for a sequence of characters. */
	public static final String CHARACTERREGEX = OMEWikiConstants.CHARACTERREGEX;
	
	/** Regular expression for a <code>Wiki</code> link. */
	public static final String WIKILINKREGEX = OMEWikiConstants.WIKILINKREGEX;
	
	/** Regular expression defining Thumbnail. */
	public static final String THUMBNAILREGEX = OMEWikiConstants.THUMBNAILREGEX;
	
	/** Regular expression defining Dataset. */
	public static final String DATASETREGEX = OMEWikiConstants.DATASETREGEX;
	
	/** Regular expression defining Project. */
	public static final String PROJECTREGEX = OMEWikiConstants.PROJECTREGEX;

	/** Regular expression defining Image. */
	public static final String IMAGEREGEX = OMEWikiConstants.IMAGEREGEX;
	
	/** Regular expression defining Wiki Heading. */
	public static final String HEADINGREGEX = OMEWikiConstants.HEADINGREGEX;
	
	/** Regular expression for a bullet list. */
	public static final String BULLETREGEX = OMEWikiConstants.BULLETREGEX;

	/** Regular expression for bold. */
	public static final String BOLDREGEX = OMEWikiConstants.BOLDREGEX;

	/** Italic regular expression. */
	public static final String ITALICREGEX = OMEWikiConstants.ITALICREGEX;

	/** Italic and bold regular expression. */
	public static final String ITALICBOLDREGEX = 
										OMEWikiConstants.ITALICBOLDREGEX;

	/** Indent regular expression. */
	public static final String INDENTREGEX = OMEWikiConstants.INDENTREGEX;
		
	/** regular expression defining a URL. */
	public static final String URLREGEX = OMEWikiConstants.URLREGEX;
	
	/** regular expression for names linked. */
	public static final String NAMEDLINKREGEX = OMEWikiConstants.NAMEDLINKREGEX;

	/** Action id to create an hyperlink. */
	private static final int	HYPERLINK = 0;
	
	/** Action id to create an image's entry. */
	private static final int	IMAGE = 1;
	
	/** Action id to create a protocol's entry. */
	private static final int	PROTOCOL = 2;

	/** The number of columns before splitting text.*/
    private static final int	COLUMNS = 45;
    
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
	
	/** Flag indicating that one click is supported if <code>true</code>. */
	private boolean			allowOneClick;
	
	/** Flag indicating to wrap or not the word.*/
	private boolean			wrapWord;
	
	/** The default number of columns before formatting the text.*/
	private int				columns;

    /**
     * Replaces the line separator by space when saving the data.
     * 
     * @param value 		The value to handle.
     * @param removeSpace 	Pass <code>true</code> to remove the spaces,
     * 						<code>false</code> otherwise.
     * @return See above.
     */
    public static String prepare(String value, boolean removeSpace)
    {
    	String v = value.replaceAll(CommonsLangUtils.LINE_SEPARATOR, " ");
    	if (removeSpace) return v.replaceAll(" ", "");
    	return v;
    }
    
	/** Installs the default actions.  */
	private void installDefaultAction()
	{
		toolBarActions = new ArrayList<JButton>();
		IconManager icons = IconManager.getInstance();
		JButton b = new JButton(icons.getIcon(IconManager.HYPERLINK));
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
		UIUtilities.unifiedButtonLookAndFeel(b);
		toolBarActions.add(b);
		*/
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
		columns = COLUMNS;
		wrapWord = true;
		defaultText = "";
		pane = new OMEEditPane(this, formatters);
		pane.setMaximumSize(new Dimension(100,100));
		
		if (toolbar) {
			installDefaultAction();
			toolBar = new JToolBar();
			toolBar.setBackground(UIUtilities.BACKGROUND_COLOR);
			toolBar.setBorder(null);
			toolBar.setFloatable(false);
			
			for (JButton button : toolBarActions) {
				toolBar.add(button);
			}
		}
		setBackground(UIUtilities.BACKGROUND);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
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
		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
	}
	
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
	 * Returns the default text or an empty string.
	 * 
	 * @return See above.
	 */
	String getDefaultText() { return defaultText; }
	
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
		for (Entry<String,FormatSelectionAction> formatterMap : DEFAULT_FORMATTERS.entrySet()) {
			String key = formatterMap.getKey();
			
			if (!formatters.containsKey(key))
				formatters.put(key, formatterMap.getValue());
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
		int ref = 2;
		if (!isEnabled()) {
			if (action instanceof URLLaunchAction && count == 1) {
				action.onSelection(text);
			} else {
				if ((action instanceof ElementSelectionAction)) {
					action.onSelection(text);
					ElementSelectionAction a = (ElementSelectionAction) action;
					int index = a.getWikiDataObjectIndex();
					long id = a.getObjectID();
					if (id >= 0) {
						WikiDataObject object = new WikiDataObject(index, id);
						if (allowOneClick) {
							if (count == 1) {
								
								firePropertyChange(
										WIKI_DATA_OBJECT_ONE_CLICK_PROPERTY, 
										null, object);
							}
							else if (count == 2) 
								firePropertyChange(WIKI_DATA_OBJECT_PROPERTY, 
										null, object);
							
						} else {
							if (count == 1) 
								firePropertyChange(WIKI_DATA_OBJECT_PROPERTY, 
										null, object);
						}
					}
				}
			}
		} else {
			if (action instanceof ElementSelectionAction && count == ref) {
				action.onSelection(text);
				ElementSelectionAction a = (ElementSelectionAction) action;
				int index = a.getWikiDataObjectIndex();
				long id = a.getObjectID();
				if (id >= 0) {
					WikiDataObject object = new WikiDataObject(index, id);
					firePropertyChange(WIKI_DATA_OBJECT_PROPERTY, null, object);
				}
			} else if (action instanceof URLLaunchAction) {
			    if (allowOneClick) ref = 1;
			    if (count == ref) action.onSelection(text);
			}
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
	 * Installs formatter for various objects. 
	 */
	public void installObjectFormatters()
	{
		pane.addFormatter(IMAGEREGEX, 
				new FormatSelectionAction(
						new ColourFormatter(Formatter.DEFAULT_LINK, false), 
						new ElementSelectionAction(WikiDataObject.IMAGE)));
		pane.addFormatter(DATASETREGEX, 
				new FormatSelectionAction(
						new ColourFormatter(Formatter.DEFAULT_LINK, false), 
						new ElementSelectionAction(WikiDataObject.DATASET)));
		pane.addFormatter(PROJECTREGEX, 
				new FormatSelectionAction(
						new ColourFormatter(Formatter.DEFAULT_LINK, false), 
						new ElementSelectionAction(WikiDataObject.PROJECT)));
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
	 * Selects all the text in the <code>TextComponent</code>.
     * Does nothing on a <code>null</code> or empty document.
	 */
	public void selectAll() { pane.selectAll(); }
	
	/**
	 * Sets the position of the text insertion caret for the 
     * <code>TextComponent</code>.
     *
	 * @param n The position.
	 */
	public void setCaretPosition(int n) { pane.setCaretPosition(n); }
	
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
	 * Sets the value indicating that the one click is supported if the 
	 * <code>enabled</code> flag is <code>false</code>.
	 * 
	 * @param allowOneClick Pass <code>true</code> to allow one click event,
	 * 						<code>false</code> otherwise.
	 */
	public void setAllowOneClick(boolean allowOneClick)
	{
		this.allowOneClick = allowOneClick;
	}
	
	/**
	 * Indicates to wrap the text or not.
	 * 
	 * @param wrapWord Pass <code>true</code> to wrap the text,
	 * 				   <code>false</code> otherwise.
	 */
	public void setWrapWord(boolean wrapWord)
	{
		this.wrapWord = wrapWord;
	}
	
	/** 
	 * Invokes when the text needs to be wrapped.
	 * 
	 * @param width The width to use.
	 * @param newLineStr The string used for new line.
	 */
	public void wrapText(int width, String newLineStr)
	{
		if (pane == null) 
		    return;
		String value = getText();
		if (value == null) return;
		value = prepare(value, false);
		FontMetrics fm = getFontMetrics(getFont());
		int charWidth = fm.charWidth('m');
		columns = (int) (1.5 * width) / charWidth;
		setText(CommonsLangUtils.wrap(value, columns, newLineStr, wrapWord));
	}
	
	/**
	 * Overridden to set the flag for all components.
	 * @see JPanel#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if (pane != null) pane.setEditable(enabled);
		
		if(toolBarActions != null)
		{
			for (JButton toolBarButton : toolBarActions) {
				toolBarButton.setEnabled(enabled);
			}
		}
	}
	
	/**
	 * Overridden to set the background color of all the components.
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
	
	@Override
	public void addFocusListener(FocusListener l) {
	    pane.addFocusListener(l);
	}
	
	/** Get reference to the underlying {@link Document} */
	public Document getDocument() {
	    return pane.getDocument();
	}

	/** Invokes when the text is modified.*/
    void onUpdate() {
        firePropertyChange(TEXT_UPDATE_PROPERTY, Boolean.FALSE, Boolean.TRUE);
    }
}
