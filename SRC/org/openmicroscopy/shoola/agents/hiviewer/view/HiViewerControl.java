/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerControl
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;

/** 
 * The HiViewer's Controller.
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
class HiViewerControl
    implements ChangeListener, PropertyChangeListener
{
    
    /** Identifies the exit action within hierarchy menu. */
    public static final Integer     EXIT = new Integer(0);
    
    /** Identifies the view P/D/I action within hierarchy menu. */
    public static final Integer     VIEW_PDI = new Integer(1);
    
    /** Identifies the view CG/C/I action within hierarchy menu. */
    public static final Integer     VIEW_CGCI = new Integer(2);
    
    /** Identifies the find Annotated action within find menu. */
    public static final Integer     FIND_ANNOTATED = new Integer(3);
    
    /** Identifies the find with title action within find menu. */
    public static final Integer     FIND_W_TITLE = new Integer(4);
    
    /** Identifies the find with annotation action within find menu. */
    public static final Integer     FIND_W_ANNOTATION = new Integer(5);
    
    /** Identifies the find with ST action within find menu. */
    public static final Integer     FIND_W_ST = new Integer(6);
    
    /** Identifies the clear action within find menu. */
    public static final Integer     CLEAR = new Integer(7);
    
    /** Identifies the squary layout action within layout menu. */
    public static final Integer     SQUARY = new Integer(8);
    
    /** Identifies the tree layout action within layout menu. */
    public static final Integer     TREE = new Integer(9);
    
    /** Identifies the save layout action within layout menu. */
    public static final Integer     SAVE = new Integer(10);
    
    /** Identifies the properties action within actions menu. */
    public static final Integer     PROPERTIES = new Integer(11);
    
    /** Identifies the annotate action within actions menu. */
    public static final Integer     ANNOTATE = new Integer(12);
    
    /** Identifies the classify action within actions menu. */
    public static final Integer     CLASSIFY = new Integer(13);
    
    /** Identifies the view action within actions menu. */
    public static final Integer     VIEW = new Integer(14);
    
    /** Identifies the zoom in action within actions menu. */
    public static final Integer     ZOOM_IN = new Integer(15);
    
    /** Identifies the zoom out action within actions menu. */
    public static final Integer     ZOOM_OUT = new Integer(16);
    
    /** Identifies the zoom fit action within actions menu. */
    public static final Integer     ZOOM_FIT = new Integer(17);
    
    
    /** 
     * Reference to the {@link HiViewer} component, which, in this context,
     * is regarded as the Model.
     */
    private HiViewer       model;
    
    /** Reference to the View. */
    private HiViewerWin    view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map            actionsMap;
    
    
    /**
     * Helper method to create all the UI actions.
     */
    private void createActions()
    {
        actionsMap.put(EXIT, new ExitAction(model));
        actionsMap.put(VIEW_CGCI, new ViewCGCIAction(model));
        actionsMap.put(VIEW_PDI, new ViewPDIAction(model));
        actionsMap.put(FIND_ANNOTATED, new FindAnnotatedAction(model));
        actionsMap.put(FIND_W_TITLE, new FindWithTitleAction(model));
        actionsMap.put(FIND_W_ANNOTATION, new FindWithAnnotationAction(model));
        actionsMap.put(CLEAR, new ClearAction(model));
        actionsMap.put(SQUARY, new SquaryLayoutAction(model));
        actionsMap.put(TREE, new TreeLayoutAction(model));
        actionsMap.put(SAVE, new SaveLayoutAction(model));
        actionsMap.put(PROPERTIES, new PropertiesAction(model));
        actionsMap.put(ANNOTATE, new AnnotateAction(model));
        actionsMap.put(CLASSIFY, new ClassifyAction(model));
        actionsMap.put(VIEW, new ViewAction(model));
        actionsMap.put(ZOOM_IN, new ZoomInAction(model));
        actionsMap.put(ZOOM_OUT, new ZoomOutAction(model));
        actionsMap.put(ZOOM_FIT, new ZoomFitAction(model));
        actionsMap.put(FIND_W_ST, new FindwSTAction(model));
    }
    
    /** 
     * Attaches a window listener to the view to discard the model when 
     * the user closes the window.
     */
    private void attachWinListener()
    {
        view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { model.discard(); }
        });
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straigh 
     * after to link this Controller to the other MVC components.
     */
    HiViewerControl() {}
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param model  Reference to the {@link HiViewer} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     * @param view   Reference to the View.  Mustn't be <code>null</code>.
     */
    void initialize(HiViewer model, HiViewerWin view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        actionsMap = new HashMap();
        createActions();
        model.addChangeListener(this);   
        attachWinListener();
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return (Action) actionsMap.get(id); }

    /**
     * Detects when the {@link Browser} is ready and then registers for
     * property change notification.
     */
    public void stateChanged(ChangeEvent ce)
    {
        int state = model.getState();
        switch (state) {
            case HiViewer.LOADING_THUMBNAILS:
                Browser browser = model.getBrowser();
                browser.addPropertyChangeListener(Browser.POPUP_POINT_PROPERTY, 
                                              this);
                browser.addPropertyChangeListener(
                        Browser.THUMB_SELECTED_PROPERTY, this); 
                view.setBrowserView(browser.getUI());
                break;
            case HiViewer.DISCARDED:
                view.setVisible(false);
                view.dispose();
                break;
        }
    }
    
    /** Reacts to property changes in the {@link Browser}. */
    public void propertyChange(PropertyChangeEvent pce)
    {
        Browser browser = model.getBrowser();
        ImageDisplay d = browser.getSelectedDisplay();
        if (Browser.POPUP_POINT_PROPERTY.equals(pce.getPropertyName())) {
            Point p = browser.getPopupPoint();
            if (d != null && p != null) view.showPopup(d, p);
        } else {  //THUMB_SELECTED_PROPERTY
            ThumbWinManager.display((ImageNode) d);
        }
    }
    
}
