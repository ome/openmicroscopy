/*
 * org.openmicroscopy.shoola.agents.hiviewer.HiViewerUIF
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
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.actions.AnnotateAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ClassifyAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ClearAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ExitAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.FindAnnotatedAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.FindWithAnnotationAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.FindWithTitleAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.FindwSTAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.PropertiesAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.SaveLayoutAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.SquaryLayoutAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.TreeLayoutAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewCGCIAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ViewPDIAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomFitAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomInAction;
import org.openmicroscopy.shoola.agents.hiviewer.actions.ZoomOutAction;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.hiviewer.layout.Layout;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

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
public class HiViewerUIF
{

    /*** Default background color. */
    public static final Color   BACKGROUND = new Color(250, 253, 255);
    
    /** Identifies the exit action within hierarchy menu. */
    public static final int     EXIT = 0;
    
    /** Identifies the view P/D/I action within hierarchy menu. */
    public static final int     VIEW_PDI = 1;
    
    /** Identifies the view CG/C/I action within hierarchy menu. */
    public static final int     VIEW_CGCI = 2;
    
    /** Identifies the find Annotated action within find menu. */
    public static final int     FIND_ANNOTATED = 3;
    
    /** Identifies the find with title action within find menu. */
    public static final int     FIND_W_TITLE = 4;
    
    /** Identifies the find with annotation action within find menu. */
    public static final int     FIND_W_ANNOTATION = 5;
    
    /** Identifies the clear action within find menu. */
    public static final int     CLEAR = 6;
    
    /** Identifies the squary layout action within layout menu. */
    public static final int     SQUARY = 7;
    
    /** Identifies the tree layout action within layout menu. */
    public static final int     TREE = 8;
    
    /** Identifies the save layout action within layout menu. */
    public static final int     SAVE = 9;
    
    /** Identifies the properties action within actions menu. */
    public static final int     PROPERTIES = 10;
    
    /** Identifies the annotate action within actions menu. */
    public static final int     ANNOTATE = 11;
    
    /** Identifies the classify action within actions menu. */
    public static final int     CLASSIFY = 12;
    
    /** Identifies the view action within actions menu. */
    public static final int     VIEW = 13;
    
    /** Identifies the zoom in action within actions menu. */
    public static final int     ZOOM_IN = 14;
    
    /** Identifies the zoom out action within actions menu. */
    public static final int     ZOOM_OUT = 15;
    
    /** Identifies the zoom out action within actions menu. */
    public static final int     ZOOM_FIT = 16;
    
    /** Identifies the properties action within actions menu. */
    public static final int     FIND_W_ST = 17;
    
    /** The maximum ID used for the action IDs. */
    private static final int    MAX_ID = 17;
    
    /** List of created browsers. */
    private static Map          browsers;
    
    /** The sole instance. */
    private static HiViewerUIF  singleton;
    
    private static HiViewerCtrl agentCtrl;
    
    /** Returns the <code>HiViewerUIF</code> object. */
    public static HiViewerUIF getInstance(HiViewerCtrl agentCtrl)
    {
        if (singleton == null) singleton = new HiViewerUIF(agentCtrl);
        return singleton;
    }
    
    private HiViewerUIF(HiViewerCtrl control)
    {
        agentCtrl = control;
        browsers= new HashMap();
    }
    
    /**
     * Helper method to create all actions for the various menus within
     * the menu bar.
     */
    private static Action[] createActions()
    {
        Action[] actions = new Action[MAX_ID+1];
        actions[EXIT] = new ExitAction(agentCtrl);
        actions[VIEW_CGCI] = new ViewCGCIAction(agentCtrl);
        actions[VIEW_PDI] = new ViewPDIAction(agentCtrl);
        actions[FIND_ANNOTATED] = new FindAnnotatedAction(agentCtrl);
        actions[FIND_W_TITLE] = new FindWithTitleAction(agentCtrl);
        actions[FIND_W_ANNOTATION] = new FindWithAnnotationAction(agentCtrl);
        actions[CLEAR] = new ClearAction(agentCtrl);
        actions[SQUARY] = new SquaryLayoutAction(agentCtrl);
        actions[TREE] = new TreeLayoutAction(agentCtrl);
        actions[SAVE] = new SaveLayoutAction(agentCtrl);
        actions[PROPERTIES] = new PropertiesAction(agentCtrl);
        actions[ANNOTATE] = new AnnotateAction(agentCtrl);
        actions[CLASSIFY] = new ClassifyAction(agentCtrl);
        actions[VIEW] = new ViewAction(agentCtrl);
        actions[ZOOM_IN] = new ZoomInAction(agentCtrl);
        actions[ZOOM_OUT] = new ZoomOutAction(agentCtrl);
        actions[ZOOM_FIT] = new ZoomFitAction(agentCtrl);
        actions[FIND_W_ST] = new FindwSTAction(agentCtrl);
        return actions;
    }
    
    /** 
     * Retrieve the HiViewer linked to the specified browser
     * 
     * @param browser   The specified browser.
     * @return See above.
     */
    HiViewer getViewer(Browser browser)
    {
        if (browser == null) return null;
        return (HiViewer) browsers.get(browser);
    }
    
    HiViewer createHiViewer()
    {
        HiViewer hiViewer = new HiViewer(createActions(), 
                            agentCtrl.getRegistry());
        hiViewer.setOnScreen();
        return hiViewer;
    }
    
    /**
     * Create a new Browser with the specified set of nodes
     * 
     * @param topNodes  Set of nodes to display in the browser.
     */
    Browser createBrowserFor(Set topNodes, HiViewer hiViewer)
    {
        Browser browser = BrowserFactory.createBrowser(topNodes);
        
        //Do layout.
        Layout squary = LayoutFactory.createLayout(LayoutFactory.SQUARY_LAYOUT);
        browser.accept(squary);
        
        //Connect the action controllers.
        hiViewer.linkActionsTo(browser);
        
        //Add the browser's View to the HiViewer component's View.
        hiViewer.setBrowserView(browser.getUI());
        
        //Register this component.
        browsers.put(browser, hiViewer);
        
        return browser;
    }
    
    /**
     * Remove the specified browser.
     * 
     * @param browser   Browser to remove.
     */
    void removeBrowser(Browser browser)
    {
        HiViewer hiViewer = (HiViewer) browsers.get(browser);
        hiViewer.closeViewer();
        browsers.remove(browser);
    }
    
}
