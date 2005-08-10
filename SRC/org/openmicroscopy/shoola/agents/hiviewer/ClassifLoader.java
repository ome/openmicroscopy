/*
 * org.openmicroscopy.shoola.agents.hiviewer.ClassifLoader
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.clsf.Classifier;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

/** 
 * Parent of all classes that load data asynchronously for a {@link Classifier}.
 * All these classes invoke methods of the {@link HierarchyBrowsingView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call.  This class provides
 * default implementations of some of the callbacks to notify the user in the
 * case of errors.  Subclasses should at least implement the 
 * <code>handleResult</code> method to feed the {@link Classifier} back with
 * the results.
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
public abstract class ClassifLoader
    extends DSCallAdapter
{
    
    /** Convenience reference to the agent's registry. */
    private Registry                registry;
 
    /** The {@link Classifier} this data loader is for. */
    protected Classifier            classifier;
    
    /** Convenience reference. */
    protected HierarchyBrowsingView hiBrwView;
    
    
    /**
     * Creates a new instance.
     * 
     * @param classifier The {@link Classifier} this data loader is for.
     *                   Mustn't be <code>null</code>.
     */
    public ClassifLoader(Classifier classifier)
    {
        if (classifier == null) 
            throw new NullPointerException("No classifier.");
        this.classifier = classifier;
        registry = HiViewerAgent.getRegistry();
        hiBrwView = (HierarchyBrowsingView) registry.
                    getDataServicesView(HierarchyBrowsingView.class);
    }
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #classifier}.
     */
    public void handleNullResult() 
    {
        handleException(new Exception("No data available."));
    }
    
    /**
     * Notifies the user that the data retrieval has been cancelled.
     */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
        registry.getUserNotifier().notifyInfo("Data Retrieval Cancellation", 
                                              info);
    }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #classifier}.
     */
    public void handleException(Throwable exc) 
    {
        String s = "Data Retrieval Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
        classifier.discard();
        //TODO: Change this.  What to do in the case of failure is up to
        //the classifier.  So we need to refactor this b/c the decision is
        //made in the wrong place!
    }
    
    /** Fires an asynchrnonous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel();
    
}
