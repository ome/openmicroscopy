/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindPane
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder;



//Java imports
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Point;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardPane;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.RegExFactory;


/** 
 * The UI Component allowing the user to find a pattern in the title or 
 * description of the <code>DataObject</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 3201 $ $Date: 2006-02-27 09:20:41 +0000 (Mon, 27 Feb 2006) $)
 * </small>
 * @since OME2.2
 */
public class FindPane
    extends ClipBoardPane
{

    /**
     * Bound property indicating that a node is selected in the tree of
     * results.
     */
    public static final String  SELECTED_PROPERTY = "showMenu";
    
    /** Bound property indicating that some text has been entered. */
    static final String         TEXT_ENTERED_PROPERTY = "textEntered";
    
    /**
     * Flag to indicate if the pattern is case sensitive or not.
     * <code>true</code> if the case sensitive, <code>false</code>
     * otherwise.
     */
    private boolean                 caseSensitive;
    
    /** The phrase to find. */
    private String                  findText;
    
    /** Object holding the find levels. */
    private FindData                findData;
    
    /** The component hosting the display. */
    private FindPaneUI              uiDelegate;
    
    /** The popup menu displaying the level of the find action. */
    private FindPopupMenu           popupMenu;
    
    
    /** The currently selected node in the tree. */
    private DefaultMutableTreeNode  selectedNode;
    
    /**
     * Creates a new instance.
     * 
     * @param model The <code>ClipBoardModel</code> Model.
     */
    public FindPane(ClipBoard model)
    {
        super(model);
        findData = new FindData();
        uiDelegate = new FindPaneUI(this);
        popupMenu = new FindPopupMenu(this);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(uiDelegate);
    }
    
    /**
     * Sets the value of the case sensitive field.
     * 
     * @param b The value to set.
     */
    void setCaseSensitive(boolean b)
    {
        if (b == caseSensitive) return;
        caseSensitive = b;
    }
    
    /**
     * Sets to <code>true</code> if the find action applies to the name
     * field, <code>false</code> otherwise.
     * 
     * @param b The value to set.
     */
    void setNameSelected(boolean b)
    {
        if (b == findData.nameSelected) return;
        findData.nameSelected = b;
    }
    
    /**
     * Returns <code>true</code> if the find action applies to the name
     * field, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isNameSelected() { return findData.nameSelected; }
    
    /**
     * Returns <code>true</code> if the find action applies to the description
     * field, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isDescriptionSelected() { return findData.descriptionSelected; }
    
    /**
     * Sets to <code>true</code> if the find action applies to the description
     * field, <code>false</code> otherwise.
     * 
     * @param b The value to set.
     */
    void setDescriptionSelected(boolean b)
    {
        if (b == findData.descriptionSelected) return;
        findData.descriptionSelected = b;
    }
    
    /** 
     * Returns <code>true</code> if there is some text is entered in the text
     * area, <code>false</code> if no text selected.
     * 
     * @return See above.
     */
    boolean isTextEmpty()
    { 
        if (findText == null) return true;
        return (findText.length() == 0);
    }
    
    /** 
     * Sets the text. 
     * 
     * @param text The text to set.
     */
    void setTextUpdate(String text)
    {
        String oldValue = findText;
        findText = text;
        firePropertyChange(TEXT_ENTERED_PROPERTY, oldValue, text);
    }
    
    /**
     * Brings up the popup menu on top of the specified component at the
     * specified point.
     * 
     * @param c The component that requested the popup menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     */
    void showFindMenu(Component c, Point p)
    {
        if (c == null) throw new IllegalArgumentException("No component.");
        if (p == null) throw new IllegalArgumentException("No point.");
        popupMenu.show(c, p.x, p.y);
    }
    
    /** 
     * Finds the selected patterns.
     */
    void find()
    {
        if (findText == null || findText.length() == 0) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Pattern p;
            if (caseSensitive) p = RegExFactory.createPattern(findText);
            else p = RegExFactory.createCaseInsensitivePattern(findText);
            model.find(p, findData);
        } catch (PatternSyntaxException pse) {
            UserNotifier un = HiViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Find", "The phrase contains non valid characters.");
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    /** Clears the result of a previous find. */
    void clear()
    {
        model.clear();
        uiDelegate.clear();
    }
    
    /**
     * Brings up the popup menu for the specified {@link ImageDisplay} node.
     * 
     * @param invoker   The component in whose space the popup menu is to
     *                  appear.
     * @param p         The coordinate in invoker's coordinate space at which 
     *                  the popup menu is to be displayed.
     * @param node      The {@link ImageDisplay} object.
     */
    void showMenu(JComponent invoker, Point p, ImageDisplay node)
    {
        model.showMenu(invoker, p, node);
    }
    
    /**
     * Sets the last selected node and fires an event to bring the
     * selected {@link ImageDisplay} node on screen.
     * 
     * @param node The selected node.
     */
    void setSelectedNode(DefaultMutableTreeNode node)
    {
        if (node == null) return;
        Object uo = node.getUserObject();
        if (uo instanceof String) return;
        ImageDisplay  newObject = (ImageDisplay) uo;
        ImageDisplay  oldObject = null;
        if (selectedNode != null)
            oldObject = (ImageDisplay) selectedNode.getUserObject();
        firePropertyChange(SELECTED_PROPERTY, oldObject, newObject);
    }
    
    /**
     * Displays the results of the search action.
     * 
     * @param foundNodes The set of nodes to display.
     */
    public void setResults(Set foundNodes)
    {
        if (foundNodes == null) return;
        uiDelegate.setMessage(foundNodes.size()); //TODO REVIEW
        uiDelegate.setFoundResults(foundNodes);
    }
    
    /**
     * Overriden to update the UI components when a new node is selected in the
     * <code>Browser</code>.
     * @see ClipBoardPane#onDisplayChange(ImageDisplay)
     */
    public void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (model.getSelectedPaneIndex() != ClipBoard.FIND_PANE) return;
        uiDelegate.clear();
        if (selectedDisplay == null || selectedDisplay instanceof ImageNode) {
           uiDelegate.onSelectedDisplay(false, null);
        } else {
            if (selectedDisplay.getParentDisplay() == null)
                uiDelegate.onSelectedDisplay(true, FindPaneUI.IN_ALL_MSG);
            else uiDelegate.onSelectedDisplay(true, selectedDisplay.getTitle());
        }
    }
    
    /**
     * Overriden to return the name of this UI component.
     * @see ClipBoardPane#getPaneName()
     */
    public String getPaneName() { return "Find"; }

    /**
     * Overriden to return the icon related to this UI component.
     * @see ClipBoardPane#getPaneIcon()
     */
    public Icon getPaneIcon()
    {
        return IconManager.getInstance().getIcon(IconManager.FIND);
    }
    
    /**
     * Overriden to return the index of this UI component.
     * @see ClipBoardPane#getPaneIndex()
     */
    public int getPaneIndex() { return ClipBoard.FIND_PANE; }

    /**
     * Overriden to return the index of this UI component.
     * @see ClipBoardPane#getPaneDescription()
     */
    public String getPaneDescription() { return "Find"; }
    
}

