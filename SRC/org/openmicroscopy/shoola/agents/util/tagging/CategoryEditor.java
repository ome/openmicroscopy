/*
 * org.openmicroscopy.shoola.agents.util.tagging.CategoryEditor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.tagging;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.HistoryDialog;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;

/** 
 * A modal dialog used to create or edit category.
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
public class CategoryEditor
	extends JDialog
	implements ActionListener, PropertyChangeListener
	
{

	/** Bounds property indicating to create a category. */
	public static final String		CREATE_CATEGORY_PROPERTY = "createCategory";
	
	/** The title of the dialog. */
	private static final String 	TITLE = "Category editor";
	
	/** Separator between words. */
	private static final String		SEPARATOR =",";
	
	/** Note explaining how to create more than one. */
	private static final String		NOTE = "To add more than one category, " +
			"separate each category with a comma.\n Or select one or more " +
			"existing categories from the menu.";
	
	/** Text displayed in the title. */
	private static final String		TEXT = "Add category to image.";
	
	/** Action command indicating to cancel the operation. */
	private static final int 		CANCEL = 0;
	
	/** Action command indicating to save the data. */
	private static final int 		FINISH = 1;
	
	/** Action command indicating to save the data. */
	private static final int 		POPUP = 2;
	
	/** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    /** The default size of the dialog. */
    private static final Dimension  WIN_DIM = new Dimension(600, 350);
    
    /** The maximum length of the name. */
    private static final int		MAX_NAME = 255;
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    private JTextField			nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    private JTextArea			descriptionArea;
    
    /** Button to finish the operation. */
    private JButton				finishButton;
    
    /** Button to cancel the object creation. */
    private JButton				cancelButton;
    
    /** Button to display the history dialog. */
    private JButton				popupButton;

    /** A {@link DocumentListener} for the {@link #nameArea}. */
    private DocumentListener	nameAreaListener;
    
    /** A {@link DocumentListener} for the {@link #descriptionArea}. */
    private DocumentListener	descriptionAreaListener;
    
    /** The dialog displaying the available categories. */
    private HistoryDialog		historyDialog;
        
    /** Collection of categories already used. */
    private List				usedCategories;
    
    /** Collection of available categories. */
    private List				categories;
    
    /** Collection of available category Groups. */
    private List				categoryGroups;
    
    /** 
     * The panel hosting the {@link #nameArea} and the {@link #popupButton}.
     */
    private JPanel				namePanel;
    
    /** 
     * Flag indicating that the user is currently entering text in the
     * {@link #nameArea}.
     */
    private boolean				typing;
    
    /** Helper reference to the font metrics. */
    private FontMetrics			metrics;
    
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		setModal(true);
		setTitle(TITLE);
	}
	
	/**  Initializes the component composing the display. */
	private void initComponents()
	{
		nameArea = new JTextField();
        UIUtilities.setTextAreaDefault(nameArea);
        metrics = nameArea.getFontMetrics(nameArea.getFont());
        descriptionArea = new MultilineLabel();
        UIUtilities.setTextAreaDefault(descriptionArea);
        nameAreaListener = new DocumentListener() {
            
            /** 
             * Updates the editor's controls when some text is inserted. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
            	String textInsert = "";
            	try {
            		textInsert = de.getDocument().getText(de.getOffset(), 
            												de.getLength());
				} catch (Exception e) {}
            	
                handleNameAreaInsert(textInsert);
            }
            
            /** 
             * Displays an error message when the data object has no name. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
                handleNameAreaRemove(de.getDocument().getLength());
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
        
        descriptionArea.getDocument().addDocumentListener(
        							descriptionAreaListener);
        nameArea.getDocument().addDocumentListener(nameAreaListener);
        nameArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e)
            {
            	Object source = e.getSource();
            	if (source != nameArea) return;
            	switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						handleEnter();
						break;
					case KeyEvent.VK_UP:
						if (historyDialog != null && historyDialog.isVisible())
							historyDialog.setSelectedIndex(false);
						break;
					case KeyEvent.VK_DOWN:
						if (historyDialog != null && historyDialog.isVisible())
							historyDialog.setSelectedIndex(true);
						break;
				}
                
            }
        });
        nameArea.addMouseListener(new MouseAdapter() {
		
			/**
			 * Displays the description of the category 
			 * @see MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {
				handleMousePressed(e.getPoint());
			}
		
		});
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(""+CANCEL);
        finishButton = new JButton("Save");
        finishButton.setEnabled(false);
        finishButton.addActionListener(this);
        finishButton.setActionCommand(""+FINISH);
        IconManager icons = IconManager.getInstance();
        popupButton = new JButton(icons.getIcon(IconManager.FILTER_MENU));
        UIUtilities.setTextAreaDefault(popupButton);
        popupButton.setBorder(null);
        nameArea.setBorder(null);
        popupButton.addActionListener(this);
        popupButton.setActionCommand(""+POPUP);
        popupButton.setEnabled((categories != null && categories.size() > 0));
        addWindowListener(new WindowAdapter()
        {
        	public void windowOpened(WindowEvent e) {
        		if (nameArea != null) nameArea.requestFocus();
        	} 
        });
	}
	
	/**
	 * 
	 * Displays the description of the category if it exists.
	 * 
	 * @param p The location of the mouse pressed.
	 */
	private void handleMousePressed(Point p)
	{
		String name = nameArea.getText();
		if (name == null || name.length() == 0) return;
		String[] names = name.split(SEPARATOR);
		Rectangle2D r;
		Graphics context = nameArea.getGraphics();
		int l = 0;
		String v;
		int vl;
		Iterator j, k;
		CategoryData item;
		Set set;
		for (int i = 0; i < names.length; i++) {
			v = names[i];
			r = metrics.getStringBounds(v, context);
			vl = (int) r.getWidth();
			if (p.x >= l && p.x <= (l+vl)) {
				v = v.trim();
				j = categories.iterator();
				while (j.hasNext()) {
					item = (CategoryData) j.next();
					if (item.getName().equals(v)) {
						descriptionArea.setText(item.getDescription());
						return;
					}
				}
				j = categoryGroups.iterator();
				while (j.hasNext()) {
					set = ((CategoryGroupData) j.next()).getCategories();
					if (set != null) {
						k = set.iterator();
						while (k.hasNext()) {
							item = (CategoryData) k.next();
							if (item.getName().equals(v)) {
								descriptionArea.setText(item.getDescription());
								return;
							}
						}
					}
					
				}
				/*
				j = categoryItems.iterator();
				while (j.hasNext()) {
					item = (CategoryItem) j.next();
					if (item.getObjectName().equals(v)) {
						currentItem = item;
						descriptionArea.setText(item.getObjectDescription());
						break;
						//descriptionArea.setEnabled(false);
					}
				}
				*/
				descriptionArea.setText("");
				return;
			}
			l += vl;
		}
	}
	
	/** 
	 * Displays the selected item in the name area when the user
	 * presses the <code>Enter</code> key.
	 */
	private void handleEnter()
	{
		if (historyDialog == null || !historyDialog.isVisible())
			return;
		String name = nameArea.getText();
		if (name == null) return;
		CategoryItem o = (CategoryItem) historyDialog.getSelectedTextValue();
		DataObject ho = o.getDataObject();
		if (ho instanceof CategoryData) {
			handleCategoryEnter((CategoryData) ho);
		} else if (ho instanceof CategoryGroupData) {
			handleCategoryGroupEnter((CategoryGroupData) ho);
			/*
			Set categories = ((CategoryGroupData) ho).getCategories();
			if (categories == null) return;
			Iterator i = categories.iterator();
			while (i.hasNext()) 
				handleCategoryEnter((CategoryData) i.next());
			*/
		}
	}

	/**
	 * Invokes when a category group is selected by pressing the 
	 * <code>enter</code> key.
	 * 
	 * @param data The selected value.
	 */
	private void handleCategoryGroupEnter(CategoryGroupData data)
	{
		String name = nameArea.getText();
		String[] names = name.split(SEPARATOR);
		Set categories = data.getCategories();
		Iterator j = categories.iterator();
		CategoryData category;
		String s;
		String n;
		String text = "";
		boolean exist = false;
		int l = names.length-1;
		for (int i = 0; i < l; i++) {
			n = names[i].trim();
			text += n;
			text += SEPARATOR+" ";
		}
		while (j.hasNext()) {
			category = (CategoryData) j.next();
			s = category.getName();
			for (int i = 0; i < l; i++) {
				n = names[i].trim();
				if (s.equals(n)) {
					exist = true;
				}
			}
			if (!exist) {
				text += s+SEPARATOR+" ";
				descriptionArea.setText(category.getDescription());
			}
		}
		if (text.length() > 2)
			text = text.substring(0, text.length()-2);
		setNameAreaValue(text);
	}
	
	/**
	 * Invokes when a category is selected by pressing the 
	 * <code>enter</code> key.
	 * 
	 * @param data The selected value.
	 */
	private void handleCategoryEnter(CategoryData data)
	{
		String name = nameArea.getText();
		String[] names = name.split(SEPARATOR);
		String s = data.getName();
		String n;
		String text = "";
		boolean exist = false;
		int l = names.length-1;
		for (int i = 0; i < l; i++) {
			n = names[i].trim();
			text += n;
			if (s.equals(n)) {
				if (i != (l-1))
					text += SEPARATOR+" ";
				exist = true;
			} else text += SEPARATOR+" ";
		}
		if (!exist) {
			descriptionArea.setText(data.getDescription());
			descriptionArea.setEnabled(false);
			text += s;
		} else text = text.substring(0, text.length()-2);
		setNameAreaValue(text);
	}
	
	/** Initializes the {@link historyDialog}. */
	private void initDialog()
	{
		Rectangle r = namePanel.getBounds();
		if (historyDialog == null) {
			Object[] data, data2 = null;
			int i = 0;
			Iterator j;
			if (categories == null || categories.size() == 0) {
				data = new Object[0];
			} else {
				data = new Object[categories.size()]; 
				j = categories.iterator();
				while (j.hasNext()) {
					data[i] = new CategoryItem((DataObject) j.next());
					i++;
				}
			}
			if (categoryGroups != null && categoryGroups.size() > 0) {
				i = 0;
				data2 = new Object[categoryGroups.size()]; 
				j = categoryGroups.iterator();
				while (j.hasNext()) {
					data2[i] = new CategoryItem((DataObject) j.next());
					i++;
				}
			}
			if (data2 != null) {
				historyDialog = new HistoryDialog(data, data2, r.width);
				historyDialog.setListCellRenderer(new CategoryCellRenderer(), 
												new CategoryCellRenderer());
			} else {
				historyDialog = new HistoryDialog(data, r.width);
				historyDialog.setListCellRenderer(new CategoryCellRenderer());
			}
			historyDialog.addPropertyChangeListener(
					HistoryDialog.SELECTION_PROPERTY, this);
		}   
	}
	
	/** Displays the dialog. */
	private void showDialog()
	{
		if (historyDialog == null) return;
		Rectangle r = namePanel.getBounds();
		historyDialog.show(namePanel, 0, r.height);
	}
	
	/**
     * Displays an error message when the length of the inserted name is
     * <code>0</code>.
     * 
     * @param length The length of the inserted text.
     */
    private void handleNameAreaRemove(int length)
    { 
    	descriptionArea.setText("");
		descriptionArea.setEnabled(true);
    	typing = true;
    	finishButton.setEnabled(length != 0);
    	String name = nameArea.getText();
    	String[] names = name.split(SEPARATOR);
    	if (length == 0 && historyDialog != null)
    		historyDialog.setVisible(false);
    	else 
    		setSelectedTextValue(names);
    }
    
    /**
     * Sets the selected value in the history list.
     * 
     * @param names The names already set.
     */
    private void setSelectedTextValue(String[] names)
    {
    	initDialog();
        if (historyDialog == null) return;
        int l = names.length;
        if (l > 0) {
        	if (historyDialog.setSelectedTextValue(names[l-1].trim())) {
        		showDialog();
        		nameArea.requestFocus();
        	} else historyDialog.setVisible(false);
        }	
    }
       
    /**
     * Enables the {@link #finishButton} and removes the warning message
     * when the name of the <code>DataObject</code> is valid.
     * Sets the {@link #typing} flag to <code>true</code>.
     * 
     * @param insert	The text inserted.
     */
    private void handleNameAreaInsert(String insert)
    {
    	typing = true;
        finishButton.setEnabled(true);
        String name = nameArea.getText();
        String[] names = name.split(SEPARATOR);
        setSelectedTextValue(names);
    }
    
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Controls if the passed name is not too long.
	 * 
	 * @param name The value to check.
	 */
	private void checkName(String name)
	{
		if (name.length() > MAX_NAME) {
			/*
        	UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
        	un.notifyInfo("Editor", "The name is too long. Cannot be more " +
        			"than "+MAX_NAME+" characters long.");
        			*/
        	setNameAreaValue("");
        	finishButton.setEnabled(false);
        	return;
        }
	}
	
	/**
	 * Sets the text of the {@link #nameArea}. First removes the
	 * documnet listener otherwise an event is fired.
	 * 
	 * @param v The value to set.
	 */
	private void setNameAreaValue(String v)
	{
		nameArea.getDocument().removeDocumentListener(nameAreaListener);
    	nameArea.setText(v);
    	nameArea.getDocument().addDocumentListener(nameAreaListener);
	}
	
	/**
	 * Returns the <code>CategoryData</code> if the passed value is a name of 
	 * an existing category, <code>null</code> otherwise.
	 * 
	 * @param n	The value to handle.
	 * @return See above.
	 */
	private CategoryData checkCategoryName(String n)
	{
		Iterator i = categories.iterator();
		CategoryData item;
		while (i.hasNext()) {
			item = (CategoryData) i.next();
			if (item.getName().equals(n)) return item;
		}
		return null;
	}
	
	/**
	 * Returns the <code>CategoryData</code> if the passed value is a name of 
	 * o category the image is classified into, <code>null</code> otherwise.
	 * 
	 * @param n	The value to handle.
	 * @return See above.
	 */
	private CategoryData checkUsedCategory(String n)
	{
		Iterator i = usedCategories.iterator();
		CategoryData item;
		while (i.hasNext()) {
			item = (CategoryData) i.next();
			if (item.getName().equals(n)) return item;
		}
		return null;
	}
	
	/** Sends a property change. */
	private void finish()
	{
		Set<CategoryData> categoriesToUpdate = new HashSet<CategoryData>();
		Set<CategoryData> categoriesToCreate = new HashSet<CategoryData>();
		CategorySaverDef def = new CategorySaverDef(categoriesToCreate, 
											categoriesToUpdate);
		CategoryData data = new CategoryData();
		String name = nameArea.getText();
		CategoryData item;
		if (name == null) return;
		String[] names = name.split(SEPARATOR);
		int l = names.length;
		if (names != null && l > 0) {
			String v; 
			String d;
			for (int i = 0; i < l; i++) {
				v = names[i];
				if (v != null) {
					v = v.trim();
					if (v.length() > 0) {
						checkName(v);
						item = checkCategoryName(v);
						data = new CategoryData();
						if (item == null) {
							if (checkUsedCategory(v) == null) {
								data.setName(v);
								categoriesToCreate.add(data);
							}
						} else {
							if (checkUsedCategory(v) == null) {
								data.setId(item.getId());
								data.setName(v);
								//data.setDescription(item.getDescription())
								if (l == 1) {
									d = descriptionArea.getText();
									if (d != null) 
										data.setDescription(d.trim());
								}
								categoriesToUpdate.add(data);
							}
						}
					}
					
				}
			}
		} else {
			/*
			name = name.trim();
			checkName(name);
			item = checkCategoryName(name);
			if (item == null) {
				String description = descriptionArea.getText();
				if (description != null) description = description.trim();
				data.setName(name);
				data.setDescription(description);
				categoriesToCreate.add(data);
			} else {
				data.setId(item.getObjectID());
				categoriesToUpdate.add(data);
			}
			*/
		}
		firePropertyChange(CREATE_CATEGORY_PROPERTY, null, def);
		close();
	}
	
	/**
	 * Handles the selection of a category group via the {@link HistoryDialog}.
	 * 
	 * @param item The item to handle.
	 */
	private void handleCategoryGroupSelection(CategoryGroupData item)
	{
		if (item == null) return;
		Set categories = item.getCategories();
		if (categories == null) return;
		Iterator i = categories.iterator();
		while (i.hasNext())
			handleCategorySelection((CategoryData) i.next());
	}
	
	/**
	 * Handles the selection of a category via the {@link HistoryDialog}.
	 * 
	 * @param item The item to handle.
	 */
	private void handleCategorySelection(CategoryData item) 
	{
		if (item == null) return;
		String itemName = item.getName();
		String name = nameArea.getText();
		String text = "";
		boolean exist = false;
		
		int length = name.length();
		if (length >= 1) {
			String[] names = name.split(SEPARATOR);
			String n;
			int l = names.length;
			if (typing) l = l-1;
			if (names != null && l > 0) {
				for (int i = 0; i < l; i++) {
					n = names[i].trim();
					text += n;
					if (itemName.equals(n)) {
						if (i != (l-1))
							text += SEPARATOR+" ";;
						exist = true;
					} else text += SEPARATOR+" ";;
				}
			}
		}
		if (!exist) text += itemName;
		else text = text.substring(0, text.length()-2);
		
		descriptionArea.setText(item.getDescription());
		setNameAreaValue(text);
		finishButton.setEnabled(true);
		typing = false;
	}
	
	/**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #finishButton}.
     * 
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JPanel bar = new JPanel();
        bar.setBorder(null);
        bar.add(finishButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}. If the <code>DataOject</code>
     * is annotable and if we are in the {@link Editor#PROPERTIES_EDITOR} mode,
     * we display the annotation pane. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
        int height = 100;
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{0, TableLayout.PREFERRED, 5, 0, height} }; //rows
        namePanel = new JPanel();
        double[][] pl = {{TableLayout.FILL, TableLayout.PREFERRED}, //columns
			{TableLayout.PREFERRED} }; //rows
        
        namePanel.setLayout(new TableLayout(pl));
        namePanel.setBorder(
        		BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        namePanel.add(nameArea, "0, 0, f, c");
        namePanel.add(popupButton, "1, 0, f, c");
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel l;
        content.add(UIUtilities.setTextFont("Name"), "0, 1, l, c");
        content.add(namePanel, "1, 1, f, c");
        //content.add(popupButton, "2, 1, l, c");
        content.add(new JLabel(), "0, 2, 1, 2");
        
        l = UIUtilities.setTextFont("Description");
        int h = l.getFontMetrics(l.getFont()).getHeight()+5;
        layout.setRow(3, h);
        content.add(l, "0, 3, l, c");
        JScrollPane pane = new JScrollPane(descriptionArea);
        content.add(pane, "1, 3, 1, 4");
        return content;
    }
    
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel editor = new JPanel();
		IconManager im = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TEXT, NOTE, 
    					im.getIcon(IconManager.CLASSIFICATION_48));
        editor.setLayout(new BorderLayout(0, 0));
        editor.setOpaque(true);
        editor.add(tp, BorderLayout.NORTH);
        JComponent c = buildContentPanel();
        c.setOpaque(true);
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        editor.add(c, BorderLayout.CENTER);
        editor.add(buildToolBar(), BorderLayout.SOUTH);
        getContentPane().add(editor, BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner				The dialog's owner.
	 * @param categories		The existing categories.
	 * @param usedCategories	The categories not available.
	 * @param categoryGroups	The categories groups if any.
	 */
	public CategoryEditor(JFrame owner, List categories, List usedCategories,
						List categoryGroups)
	{
		super(owner);
		this.usedCategories = usedCategories;
		this.categories = categories;
		this.categoryGroups = categoryGroups;
		setProperties();
		initComponents();
		buildGUI();
		setSize(WIN_DIM);
	}

	/**
	 * Cancels or saves the data.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				close();
				break;
			case FINISH:
				finish();
				break;
			case POPUP:
				initDialog();
				historyDialog.resetListData();
				showDialog();
				nameArea.requestFocus();
		}
	}
	
	/** 
	 * Handles the selection of a new item in the list.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		Object item = evt.getNewValue();
		if (!(item instanceof CategoryItem)) return;
		DataObject ho = ((CategoryItem) item).getDataObject();
		if (ho instanceof CategoryData) 
			handleCategorySelection((CategoryData) ho);
		else if (ho instanceof CategoryGroupData)
			handleCategoryGroupSelection((CategoryGroupData) ho);
	}
	
}
