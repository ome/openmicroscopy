/*
 * org.openmicroscopy.shoola.agents.hiviewer.HiLoader
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

/** 
 * Abstract data controller to initiate and monitor a data loading.
 * TODO: TMP class; it also tracks model state and holds data.  All this should
 * go into the HiViewer Model, when we have one!
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
abstract class HiLoader
    extends DSCallAdapter
{

    private HiViewerAgent   abstraction;
    private HiViewer        view;
    private boolean         loadingTree;
    private Map             thumbProviders;
    
    
    protected HiLoader(HiViewerAgent abstraction, HiViewer view)
    {
        this.abstraction = abstraction;
        this.view = view;
        thumbProviders = new HashMap();
    }
    
    void load()
    {
        loadingTree = true;
        Registry reg = abstraction.getRegistry();
        HierarchyBrowsingView hbw = (HierarchyBrowsingView)
                        reg.getDataServicesView(HierarchyBrowsingView.class);
        loadHierarchies(hbw);
    }
    
    protected abstract void loadHierarchies(HierarchyBrowsingView hbw);
    protected abstract Set getVisRootNodes(Object result);
    
    public void update(DSCallFeedbackEvent fe) 
    {
        String status = fe.getStatus();
        int percDone = fe.getPercentDone();
        if (loadingTree) percDone = 5;  //We've got to load all thumbs yet.
        if (status == null) 
            status = (percDone == 100) ? "Done" :  //Else
                                       ""; //Description wasn't available.   
        view.setStatus(status, false, percDone);
        if (!loadingTree) {  //We're loading tumbnails.
            ThumbnailData td = (ThumbnailData) fe.getPartialResult();
            if (td == null) return;  //last fe has null object.
            List providers = (List) thumbProviders.get(
                                                new Integer(td.getImageID()));
            Iterator p = providers.iterator();
            while (p.hasNext()) {
                ThumbnailProvider prv = (ThumbnailProvider) p.next();
                prv.setFullScaleThumb(td.getThumbnail());
            }
        }
    }
    
    public void onEnd()  //Called twice: 1 for tree, 2 for end of thumbs loadin.
    {
        if (!loadingTree)  //We're done this time!
            view.setStatus(null, true, 0);
    }
    
    //Called twice: 1 for tree, 2 for end of thumbs loadin.
    public void handleResult(Object result) 
    {
        if (loadingTree) {  
            
            //Create browser so that we can start displaying the vis trees.
            Set roots = getVisRootNodes(result);
            HiViewerUIF presentation = HiViewerUIF.getInstance(
                                                    abstraction.getControl());
            Browser brw = presentation.createBrowserFor(roots, view);
            
            //Done with tree, load thumbnails.  Build the thumbProviders map,
            //so that we can then connect (see update) a thumb to an image node.
            Set imgs = new HashSet();
            Iterator i = brw.getImageNodes().iterator();
            ImageNode node;
            ImageSummary is;
            while (i.hasNext()) {
                node = (ImageNode) i.next();
                is = (ImageSummary) node.getHierarchyObject();
                imgs.add(is);
                Integer id = new Integer(is.getID());
                List providers = (List) thumbProviders.get(id);
                if (providers == null) {
                    providers = new ArrayList();
                    thumbProviders.put(id, providers);
                    //TODO: Providers should be shared across different
                    //ImageNodes that represent the *same* image.  We're
                    //wasting (potentially) a huge amount of memory here!
                }
                providers.add(node.getThumbnail());
            }
            Registry reg = abstraction.getRegistry();
            HierarchyBrowsingView hbw = (HierarchyBrowsingView)
                           reg.getDataServicesView(HierarchyBrowsingView.class);
            hbw.loadThumbnails(imgs, ThumbnailProvider.THUMB_MAX_WIDTH,
                                ThumbnailProvider.THUMB_MAX_WIDTH, this);
            
            //After the first call we're loading thumbs.
            loadingTree = false;
        }
    }
    
    public void handleNullResult() {}
    
    public void handleCancellation() {}
    
    public void handleException(Throwable exc) 
    {
        String s = "Data Retrieval Failure: ";
        Registry reg = abstraction.getRegistry();
        reg.getLogger().error(this, s+exc);
        reg.getUserNotifier().notifyError("Data Retrieval Failure", s, exc);
        view.closeViewer();
    }
    
}
