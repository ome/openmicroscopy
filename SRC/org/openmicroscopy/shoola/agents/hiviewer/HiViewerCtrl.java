/*
 * org.openmicroscopy.shoola.agents.hiviewer.HiViewerCtrl
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

package org.openmicroscopy.shoola.agents.hiviewer;




//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.actions.BrowserAction;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ClearVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.FindAnnotatedVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExTitleVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.hiviewer.view.RegExFinder;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
public class HiViewerCtrl
{

    /** Layout actions command ID. */
    public static final int TREE_LAYOUT = LayoutFactory.TREE_LAYOUT;
    public static final int SQUARY_LAYOUT = LayoutFactory.SQUARY_LAYOUT;
    
    /** Hierarchy actions command ID. */
    public static final int VIEW_IN_PDI = 0;
    public static final int VIEW_IN_CGCI = 1;
    
    private HiViewerAgent abstraction;
    
    HiViewerCtrl(HiViewerAgent abstraction)
    {
        this.abstraction = abstraction;
    }
    
    public Registry getRegistry() { return abstraction.getRegistry(); }
    
    /** Forward the call to the {@link HiViewerAgent abstraction}. */
    public void annotate(Object target) { abstraction.annotate(target); }

    /** Forward action to the {@link HiViewerAgent abstraction}. */
    public void showProperties(Object target, Browser browser)
    {
        HiViewerUIF presentation = HiViewerUIF.getInstance(this);
        abstraction.showProperties(target, presentation.getViewer(browser));
    }
    
    /** Forward action to the {@link HiViewerAgent abstraction}. */
    public void classify(ImageSummary target)
    {
        abstraction.classify(target);
    }
    
    public void zoomIn(Object target, Browser browser)
    {
        
    }
    
    public void zoomOut(Object target, Browser browser)
    {
        
    }
    
    /**
     * View or browse the specified target.
     * Forward action to the {@link HiViewerAgent abstraction}.
     * @param target
     */
    public void view(Object target)
    {
        if (target instanceof DatasetSummary)
            abstraction.browseDataset(((DatasetSummaryLinked) target));
        else if (target instanceof ProjectSummary)
            abstraction.browseProject(((ProjectSummary) target));
        else if (target instanceof CategoryGroupData)
            abstraction.browseCategoryGroup(((CategoryGroupData) target));
        else if (target instanceof CategoryData)
            abstraction.browseCategory(((CategoryData) target));
        else if (target instanceof ImageSummary)
            abstraction.viewImage(((ImageSummary) target));
    }
    
    /** 
     * Set the layout of the specified browser according to the index
     * 
     * @param layoutIndex   index of the selected layout.
     * @param browser       
     */
    public void doLayout(int layoutIndex, Browser browser)
    {
        if (browser == null) return;
        switch (layoutIndex) {
            case LayoutFactory.SQUARY_LAYOUT:
                browser.accept(LayoutFactory.createLayout(
                        LayoutFactory.SQUARY_LAYOUT));
                break;
            case LayoutFactory.TREE_LAYOUT:
                browser.accept(LayoutFactory.createLayout(
                                LayoutFactory.TREE_LAYOUT));
        }
    }
    
    /** Save the current layout of the browser. */
    public void saveLayout(Browser browser)
    {
        if (browser == null) return;
    }
    
    /** Remove the specified browser. */
    public void exit(Browser browser)
    {
        HiViewerUIF presentation = HiViewerUIF.getInstance(this);
        presentation.removeBrowser(browser);
    }
    
    /** Switch from one hierarchy to an other. */
    public void viewHierarchy(int index, BrowserAction action)
    {
        if (action == null) return;
        Browser browser = action.getBrowser();
        Set set = browser.getImages();
        switch (index) {
            case VIEW_IN_PDI:
                abstraction.viewInPDI(set); break;
            case VIEW_IN_CGCI:
                abstraction.viewInCGCI(set); break;
        }
    }
    
    /** Handle the FindAnnotatedAction. */
    public void findAnnotated(BrowserAction action)
    {
        if (action == null) return;
        //We first clear.
        clear(action);
        //Then apply visitor
        Browser browser = action.getBrowser();
        browser.accept(new FindAnnotatedVisitor());
    }
    
    /** Handle the FindAnnotatedAction. */
    public void findWithAnnotation(BrowserAction action)
    {
        if (action == null) return;
        //We first clear.
        clear(action);
        //Then we apply the filter.
        HiViewerUIF presentation = HiViewerUIF.getInstance(this);
        HiViewer hi = presentation.getViewer(action.getBrowser());
        UIUtilities.centerAndShow(
                new RegExFinder(RegExFinder.FOR_ANNOTATION, 
                        action.getBrowser(), hi));
    }
    
    /** Handle the FindAnnotatedAction. */
    public void findWithST(BrowserAction action)
    {
        if (action == null) return;
    }
    
    /** Handle the FindAnnotatedAction. */
    public void findWithTitle(BrowserAction action)
    {
        if (action == null) return;
        //We first clear.
        clear(action);
        //Then we apply the filter.
        HiViewerUIF presentation = HiViewerUIF.getInstance(this);
        HiViewer hi = presentation.getViewer(action.getBrowser());
        UIUtilities.centerAndShow(new RegExFinder(RegExFinder.FOR_TITLE,
                action.getBrowser(), hi));
    }
    
    /** Handle the FindAnnotatedAction. */
    public void clear(BrowserAction action)
    {
        if (action == null) return;
        Browser browser = action.getBrowser();
        browser.accept(new ClearVisitor());
    }
    
}
