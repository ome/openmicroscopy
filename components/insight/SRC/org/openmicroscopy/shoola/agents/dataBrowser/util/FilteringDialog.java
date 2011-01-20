/*
 * org.openmicroscopy.shoola.agents.dataBrowser.util.FilteringDialog 
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
package org.openmicroscopy.shoola.agents.dataBrowser.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;
import org.jdesktop.swingx.JXDatePicker;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagCellRenderer;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagItem;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;
import pojos.DataObject;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;

/** 
 * Modal dialog used to filter data.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FilteringDialog 
	extends JDialog
	implements ActionListener, DocumentListener, PropertyChangeListener
{

	/** Bound property indicating to load the existing tags. */
	public static final String	LOAD_TAG_PROPERTY = "loadTag";
	
	/** Bound property indicating to filter the data. */
	public static final String	FILTER_PROPERTY = "filter";
	
	/** The title of the dialog. */
	private static final String TITLE = "Filtering images in Workspace";
	
	/** Text indicating how to use the tag entries. */
	private static final String DESCRIPTION = "Separate tags with " +
												""+SearchUtil.COMMA_SEPARATOR;
	
	/** Action id indicating to close the dialog. */
	private static final int	CANCEL = 0;
	
	/** Action id indicating to filter the data. */
	private static final int	FILTER = 1;
	
	/** ID to filter by higher rate. */
	private static final int	HIGHER_RATING = 0;
	
	/** ID to filter by lower rate. */
	private static final int	LOWER_RATING = 1;
	
	/** ID to filter by the exact rate. */
	private static final int	EXACT_RATING = 2;
	
	/** The maximum number of options. */
	private static final int	MAX = 2;
	
	/** Store the rating options. */
	private static final String[]	RATING;
	
	static {
		RATING = new String[MAX+1];
		RATING[HIGHER_RATING] = "greater or equal to";
		RATING[LOWER_RATING] = "lower or equal to";
		RATING[EXACT_RATING] = "equal to";
	}
	
	/** The component to select the rating. */
	private JCheckBox		ratingBox;
	
	/** The component to select the comments. */
	private JCheckBox		commentsBox;
	
	/** The component to select the name. */
	private JCheckBox		nameBox;
	
	/** The component to select the tags. */
	private JCheckBox		tagsBox;
	
	/** The component to select a time interval. */
	private JCheckBox		calendarBox;
	
	/** Used to select one the rating options. */
	private JComboBox		ratingOptions;
	
	/** Date used to specify the beginning of the time interval. */
	private JXDatePicker	fromDate;
	
	/** Date used to specify the ending of the time interval. */
	private JXDatePicker	toDate;
	
	/** The rating component. */
	private RatingComponent	rating;
	
	/** The field area to collect the tags. */
	private JTextField		tagsArea;
	
	/** The field area to collect the comments. */
	private JTextField		commentsArea;
	
	/** The field area to collect the name. */
	private JTextField		nameArea;
	
	/** Button to close the dialog. */
	private JButton			cancelButton;
	
	/** Button to filter the data. */
	private JButton			filterButton;
	
    /** The dialog displaying the existing tags. */
    private HistoryDialog	tagsDialog;

    /** The collection of existing tags. */
    private Collection		existingTags;
    
    /**
     * Enters the tag.
     * 
     * @param tag The tag value to enter.
     */
    private void enterTag(TagAnnotationData tag)
    {
    	String text = tag.getTagValue();
    	List<String> l = SearchUtil.splitTerms(tagsArea.getText(), 
				SearchUtil.COMMA_SEPARATOR);
    	String result = SearchUtil.formatString(text, l);
    	tagsArea.getDocument().removeDocumentListener(this);
    	tagsArea.setText(result);
    	tagsArea.getDocument().addDocumentListener(this);
    }
    
    /** Loads the tags and adds code completion. */
    private void handleTagInsert()
    {
    	if (existingTags == null) {
    		firePropertyChange(LOAD_TAG_PROPERTY, Boolean.FALSE, Boolean.TRUE);
    		return;
    	}
		codeCompletion();
		
		if (tagsDialog == null) return;
		String name = tagsArea.getText();
		String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
        int length = names.length;
        if (length > 0) {
        	if (tagsDialog.setSelectedTextValue(names[length-1].trim())) {
        		Rectangle r = tagsArea.getBounds();
        		tagsDialog.show(tagsArea, 0, r.height);
        		tagsArea.requestFocus();
        	} else tagsDialog.setVisible(false);
        }	
    }
    
    /** Initializes the {@link HistoryDialog} used for code completion. */
    private void codeCompletion()
    {
    	if (tagsDialog != null) return;
    	Rectangle r = tagsArea.getBounds();
		Object[] data = null;
		if (existingTags != null && existingTags.size() > 0) {
			data = new Object[existingTags.size()];
			Iterator j = existingTags.iterator();
			DataObject object;

			TagItem item;
			int i = 0;
			while (j.hasNext()) {
				object = (DataObject) j.next();
				item = new TagItem(object);
				data[i] = item;
				i++;
			}
			long id = MetadataViewerAgent.getUserDetails().getId();
			tagsDialog = new HistoryDialog(data, r.width);
			tagsDialog.setListCellRenderer(new TagCellRenderer(id));
			tagsDialog.addPropertyChangeListener(
					HistoryDialog.SELECTION_PROPERTY, this);
		}
    }
    
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setTitle(TITLE);
		setModal(true);
	}
	
	/** Handles text entered in the tagging area. */
	private void handleEnter()
	{
		if (tagsDialog == null || !tagsDialog.isVisible())
			return;
		String name = tagsArea.getText();
		if (name == null) return;
		TagItem o = (TagItem) tagsDialog.getSelectedTextValue();
		if (o == null) return;
		DataObject ho = o.getDataObject();
		if (ho instanceof TagAnnotationData) {
			enterTag((TagAnnotationData) ho);
		}
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		ratingBox = new JCheckBox("Rating:");
		//ratingBox.setSelected(true);
		calendarBox = new JCheckBox("Calendar:");
		commentsBox = new JCheckBox("Comments");
		nameBox = new JCheckBox("Name");
		tagsBox = new JCheckBox("Tags");
		ratingOptions = new JComboBox(RATING);
		rating = new RatingComponent(5, RatingComponent.HIGH_SIZE);
		fromDate = UIUtilities.createDatePicker(false);
		toDate = UIUtilities.createDatePicker(false);
		tagsArea = new JTextField();
		tagsArea.setColumns(15);
		tagsArea.getDocument().addDocumentListener(this);
		tagsArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e)
            {
            	Object source = e.getSource();
            	if (source != tagsArea) return;
            	switch (e.getKeyCode()) {
            		case KeyEvent.VK_ENTER:
            			handleEnter();
            			break;
					case KeyEvent.VK_UP:
						if (tagsDialog != null && tagsDialog.isVisible())
							tagsDialog.setSelectedIndex(false);
						break;
					case KeyEvent.VK_DOWN:
						if (tagsDialog != null && tagsDialog.isVisible())
							tagsDialog.setSelectedIndex(true);
				}
                
            }
        });
		
		commentsArea = new JTextField();
		commentsArea.setColumns(15);
		nameArea = new JTextField();
		nameArea.setColumns(15);
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close.");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		filterButton = new JButton("Filter");
		filterButton.setToolTipText("Filter the data.");
		filterButton.setActionCommand(""+FILTER);
		filterButton.addActionListener(this);
		//getRootPane().setDefaultButton(filterButton);
	}

	/** Organizes the filtering parameters. */
	private void filter()
	{
		FilterContext context = new FilterContext();
		boolean filter = false;
		if (ratingBox.isSelected()) {
			int index = -1;
			switch (ratingOptions.getSelectedIndex()) {
				case HIGHER_RATING:
					index = FilterContext.HIGHER;
					break;
				case LOWER_RATING:
					index = FilterContext.LOWER;
					break;
				case EXACT_RATING:
					index = FilterContext.EQUAL;
			};
			filter = true;
			context.setRate(index, rating.getCurrentValue());
		}
		if (calendarBox.isSelected()) {
			Date d = fromDate.getDate();
			Timestamp start = null;
			if (d != null) start =  new Timestamp(d.getTime());
			Timestamp end = null;
			d = toDate.getDate();
			if (d != null) end =  new Timestamp(d.getTime());
			context.setTimeInterval(start, end);
			filter = true;
		}
		if (tagsBox.isSelected()) {
			List<String> l = SearchUtil.splitTerms(tagsArea.getText(), 
					SearchUtil.COMMA_SEPARATOR);
			if (l != null && l.size() > 0) {
				context.addAnnotationType(TagAnnotationData.class, l);
				filter = true;
			}
		}
		if (commentsBox.isSelected()) {
			List<String> l = SearchUtil.splitTerms(commentsArea.getText(), 
					SearchUtil.COMMA_SEPARATOR);
			if (l != null && l.size() > 0) {
				context.addAnnotationType(TextualAnnotationData.class, l);
				filter = true;
			}
		}
		if (nameBox.isSelected()) {
			List<String> l = SearchUtil.splitTerms(nameArea.getText(), 
					SearchUtil.COMMA_SEPARATOR);
			if (l != null && l.size() > 0) {
				context.addName(l);
				filter = true;
			}
		}
		//Get the text to filter by
		if (filter)
			firePropertyChange(FILTER_PROPERTY, null, context);
		setVisible(false);
	}
	
	/**
	 * Builds and lays out the components used to select the rating level.
	 * 
	 * @return See above.
	 */
	private JPanel buildRatingPane()
	{
		JPanel p = new JPanel();
		p.add(ratingBox);
		p.add(ratingOptions);
		p.add(rating);
		return UIUtilities.buildComponentPanel(p, 0, 0);
	}
	
	/**
	 * Builds and lays out the components used to select a time range.
	 * 
	 * @return See above.
	 */
	private JPanel buildCalendarPane()
	{
		JPanel date = new JPanel();
		date.add(UIUtilities.setTextFont("From: "));
		date.add(fromDate);
		date.add(UIUtilities.setTextFont("To: "));
		date.add(toDate);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(calendarBox);
		p.add(date);
		return UIUtilities.buildComponentPanel(p, 0, 0);
	}
	
	/**
	 * Builds and lays out the components used to select the tags.
	 * 
	 * @return See above.
	 */
	public JPanel buildTagsPane()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		p.add(tagsBox, c);
		c.gridx++;
		p.add(Box.createHorizontalStrut(5), c);
		c.gridx++;
		c.weightx = 0.5;
		p.add(tagsArea, c);
		c.gridy++;
		p.add(UIUtilities.setTextFont(DESCRIPTION, Font.ITALIC, 10), c);
		//p.add(tagsBox);
		//p.add(tagsArea);
		return UIUtilities.buildComponentPanel(p, 5, 5);
	}
	
	/**
	 * Builds and lays out the components used to select the comments.
	 * 
	 * @return See above.
	 */
	public JPanel buildCommentsPane()
	{
		JPanel p = new JPanel();
		p.add(commentsBox);
		p.add(commentsArea);
		return UIUtilities.buildComponentPanel(p, 0, 0);
	}
	
	/**
	 * Builds and lays out the components used to select the name.
	 * 
	 * @return See above.
	 */
	public JPanel buildNamePane()
	{
		JPanel p = new JPanel();
		p.add(nameBox);
		p.add(nameArea);
		return UIUtilities.buildComponentPanel(p, 0, 0);
	}
	
	/** 
	 * Builds the selection component.
	 * 
	 * @return See above.
	 */
	private JPanel buildSelectionPane()
	{
		JPanel p = new JPanel();
		double[][] size = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, 
			TableLayout.PREFERRED, 5,
				TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, 
				TableLayout.PREFERRED, 5}};
		p.setLayout(new TableLayout(size));
		int i = 0;
		p.add(buildTagsPane(), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		i++;
		p.add(buildNamePane(), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		i++;
		p.add(buildCommentsPane(), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		i++;
		p.add(buildRatingPane(), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		i++;
		p.add(buildCalendarPane(), "0, "+i);
		i++;
		p.add(new JSeparator(JSeparator.HORIZONTAL), "0, "+i);
		return p;
	}
	
	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel p = new JPanel();
		p.add(filterButton);
		p.add(Box.createHorizontalStrut(5));
		p.add(cancelButton);
		return UIUtilities.buildComponentPanelRight(p);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		//IconManager icons = IconManager.getInstance();
		//TitlePanel tp = new TitlePanel(TITLE, "foo", 
		//				icons.getIcon(IconManager.FILTERING_48));
		Container c = getContentPane();
		//c.add(tp, BorderLayout.NORTH);
		c.add(buildSelectionPane(), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the frma.ce
	 */
	public FilteringDialog(JFrame owner)
	{
		super(owner);
		setProperties();
		initComponents();
		buildGUI();
		pack();
	}

	/**
	 * Sets the text of the {@link #tagsArea}.
	 * 
	 * @param text The value to set.
	 */
	public void setTagsText(String text)
	{
		if (text == null) return;
		text = text.trim();
		tagsArea.getDocument().removeDocumentListener(this);
		tagsArea.setText(text);
		tagsArea.getDocument().addDocumentListener(this);
		tagsBox.setSelected(true);
	}
	
	/**
	 * Sets the text of the {@link #commentsArea}.
	 * 
	 * @param text The value to set.
	 */
	public void setCommentsText(String text)
	{
		if (text == null) return;
		text = text.trim();
		commentsArea.setText(text);
		commentsBox.setSelected(true);
	}
	
	/**
	 * Sets the value of the {@link #rating} component.
	 * 
	 * @param value The value to set.
	 */
	public void setRatingLevel(int value)
	{
		rating.setValue(value);
		ratingBox.setSelected(true);
	}
	
	/**
	 * Sets the collection of tags and brings up the completion dialog 
	 * if any.
	 * 
	 * @param tags The value to set.
	 */
	public void setTags(Collection tags)
	{
		if (tags == null) return;
		existingTags = tags;
		codeCompletion();
	}
	
	/**
	 * Reacts to various controls.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				setVisible(false);
				break;
			case FILTER:
				filter();
		}
	}

	/**
	 * Sets the tag value.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (HistoryDialog.SELECTION_PROPERTY.equals(name)) {
			Object item = evt.getNewValue();
			if (!(item instanceof TagItem)) return;
			DataObject ho = ((TagItem) item).getDataObject();
			if (ho instanceof TagAnnotationData) {
				enterTag((TagAnnotationData) ho);
			}
		}
	}
	
	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		handleTagInsert();
	}

	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) {}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
