/*
 * org.openmicroscopy.shoola.agents.hiviewer.PDILoader
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
class PDILoader
    extends DSCallAdapter
{

    private HiViewerAgent   abstraction;
    private HiViewer        view;
    private Set             images;
    private boolean         loadingTree;
    private Map             thumbProviders;
    
    
    PDILoader(HiViewerAgent abstraction, HiViewer view, Set images)
    {
        this.abstraction = abstraction;
        this.view = view;
        this.images = images;
        thumbProviders = new HashMap();
    }
    
    void load()
    {
        loadingTree = true;
        Registry reg = abstraction.getRegistry();
        HierarchyBrowsingView hbw = (HierarchyBrowsingView)
                        reg.getDataServicesView(HierarchyBrowsingView.class);
        hbw.findPDIHierarchies(images, this);
    }
    
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
            ThumbnailProvider prv = (ThumbnailProvider) 
                                        thumbProviders.get(
                                                new Integer(td.getImageID()));
            prv.setFullScaleThumb(td.getThumbnail());
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
            
            //Create browser so that we can start displaying the vis tree.
            List dataObjects = new ArrayList((Set) result); 
            Set topNodes = HiTranslator.transformHierarchy(dataObjects);
            HiViewerUIF presentation = HiViewerUIF.getInstance(
                                                    abstraction.getControl());
            Browser brw = presentation.createBrowserFor(topNodes, view);
            
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
                thumbProviders.put(new Integer(is.getID()), 
                                   node.getThumbnail());
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
        String s = "Can't browse the selected hierarchy: ";
        Registry reg = abstraction.getRegistry();
        reg.getLogger().error(abstraction, s+exc);
        reg.getUserNotifier().notifyError("Data Retrieval Failure", s, exc);
        view.closeViewer();
    }
    
}
