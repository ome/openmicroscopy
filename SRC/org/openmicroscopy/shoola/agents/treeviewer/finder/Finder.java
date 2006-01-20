/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.Finder
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

package org.openmicroscopy.shoola.agents.treeviewer.finder;



//Java imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.RegExFactory;


/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class Finder
	extends JPanel
{

    /** 
     * Bound property name indicating to remove the component from the display.
     */
    public static final String	CLOSE_FINDER_PROPERTY = "closeFinder";
    
    /** Bound property indicating that some text has been entered. */
    public static final String 	TEXT_ENTERED_PROPERTY = "textEntered";
    
    /** Bound property indicating the find level. */
    public static final String 	LEVEL_PROPERTY = "level";
    
    /** Bound property indicating the number of elements retrieved. */
    public static final String	RETRIEVED_PROPERTY = "retrieved";
    
    /** The UI delegate. */
    private FinderUI 		uiDelegate;
    
    /** The Model. */
    private FinderModel		model;
    
    /** The control. */
    private FinderControl	controller;
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder());
        add(uiDelegate, BorderLayout.CENTER);
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param parentComponent 	Back pointer to the parent's model.
     * 							Mustn't be <code>null</code>.
     */
    public Finder(TreeViewer parentComponent)
    {
        if (parentComponent == null)
            throw new IllegalArgumentException("No parent component.");
        model = new FinderModel(parentComponent);
        controller = new FinderControl(this);
        uiDelegate = new FinderUI(this, controller, model);
        controller.initialize(uiDelegate);
        buildGUI();
    }
    
    /**
     * Sets to <code>true</code> if the find action applies to the name
     * field, <code>false</code> otherwise.
     * 
     * @param b The value to set.
     */
    void setNameSelected(boolean b)
    {
        if (b == model.isNameSelected()) return;
        Boolean oldValue = 
            model.isNameSelected() ? Boolean.TRUE : Boolean.FALSE,
        newValue = b ? Boolean.TRUE : Boolean.FALSE;
        model.setNameSelected(b);
        firePropertyChange(LEVEL_PROPERTY, oldValue, newValue);   
    }
    
    /**
     * Sets to <code>true</code> if the find action applies to the description
     * field, <code>false</code> otherwise.
     * 
     * @param b The value to set.
     */
    void setDescriptionSelected(boolean b)
    {
        if (b == model.isDescriptionSelected()) return;
        Boolean oldValue = 
            model.isDescriptionSelected() ? Boolean.TRUE : Boolean.FALSE,
        newValue = b ? Boolean.TRUE : Boolean.FALSE;
        model.setDescriptionSelected(b);
        firePropertyChange(LEVEL_PROPERTY, oldValue, newValue);
    }
    
    /**
     * Sets to <code>true</code> if the find action applies to the annotation
     * field, <code>false</code> otherwise.
     * 
     * @param b The value to set.
     */
    void setAnnotationSelected(boolean b)
    {
        if (b == model.isAnnotationSelected()) return;
        
        Boolean oldValue = 
            model.isAnnotationSelected() ? Boolean.TRUE : Boolean.FALSE,
        newValue = b ? Boolean.TRUE : Boolean.FALSE;
        model.setAnnotationSelected(b);
        firePropertyChange(LEVEL_PROPERTY, oldValue, newValue);
    }
    
    /** 
     * Sets the text. 
     * 
     * @param text The text to set.
     */
    void setTextUpdate(String text)
    {
        String oldValue = model.getFindText();
        model.setFindText(text);
        firePropertyChange(TEXT_ENTERED_PROPERTY, oldValue, text);
    }
    
    /**
     * Sets the value of the case sensitive field.
     * 
     * @param b The value to set.
     */
    void setCaseSensitive(boolean b)
    {
        if (b == model.isCaseSensitive()) return;
        model.setCaseSensitive(b);
    }
    
    /**
     * Returns the value of the case sensitive field.
     * 
     * @return See above.
     */
    boolean isCaseSensitive() { return model.isCaseSensitive(); }
    
    /** 
     * Returns <code>true</code> if some text is entered in the text area,
     * <code>false</code> if no text selected.
     * 
     * @return See above.
     */
    boolean isTextEmpty()
    { 
        return (model.getFindText().length() == 0);
    }
    
    /** Removes the component from the display. */
    void close()
    {
        if (!model.isDisplay()) return;
        firePropertyChange(CLOSE_FINDER_PROPERTY, Boolean.FALSE,
                    			Boolean.TRUE);
    }

    /**
     * Returns <code>true</code> if the find action applies to the name
     * field, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isNameSelected() { return model.isNameSelected(); }
    
    /**
     * Returns <code>true</code> if the find action applies to the description
     * field, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isDescriptionSelected()
    { 
        return model.isDescriptionSelected();
    }
    
    /**
     * Returns <code>true</code> if the find action applies to the annotation
     * field, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotationSelected()
    {
        return model.isAnnotationSelected();
    }
    
    /**
     * Brings up the popup menu on top of the specified component at the
     * specified point.
     * 
     * @param c The component that requested the popup menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     */
    void showMenu(Component c, Point p)
    {
        if (c == null) throw new IllegalArgumentException("No component.");
        if (p == null) throw new IllegalArgumentException("No point.");
        uiDelegate.showPopup(c, p);
    }
    
    /** Finds the phrase in the currently selected browser. */
    void find()
    {
        try {
            TreeViewer pc = model.getParentComponent();
            if (pc.getSelectedBrowser() == null) return;
            Pattern p;
            if (!model.isCaseSensitive())
                p = RegExFactory.createCaseInsensitivePattern(
                    		model.getFindText());
            else p = RegExFactory.createPattern(model.getFindText());
            RegExVisitor visitor = new RegExVisitor(this, p);
            pc.getSelectedBrowser().accept(visitor);
            Set set = visitor.getFoundNodes();
            pc.getSelectedBrowser().setFoundInBrowser(set);
            firePropertyChange(RETRIEVED_PROPERTY, new Integer(-1), 
                    			new Integer(set.size()));
        } catch (PatternSyntaxException pse) {
            UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Find", "The phrase contains non valid characters.");
        }
    }
    
    /** Finds the next occurence of the phrase. */
    void findNext()
    {
        TreeViewer pc = model.getParentComponent();
        if (pc.getSelectedBrowser() == null) return;
        pc.getSelectedBrowser().findNext();
    }
    
    /** Finds the previous occurence of the phrase. */
    void findPrevious()
    {
        TreeViewer pc = model.getParentComponent();
        if (pc.getSelectedBrowser() == null) return;
        pc.getSelectedBrowser().findPrevious();
    }
    
    
    /**
     * Sets the following value.
     * 
     * @param b The value to set.
     */
    public void setDisplay(boolean b) { model.setDisplay(b); }
    
    /**
     * Returns <code>true</code> if the {@link Finder} is visible, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isDisplay() { return model.isDisplay(); }
    
}
