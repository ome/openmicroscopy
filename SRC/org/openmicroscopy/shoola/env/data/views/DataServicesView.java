/*
 * org.openmicroscopy.shoola.env.data.views.DataServicesView
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

package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Just a marker for data view interfaces.
 * <p>A data services view is a logical grouping of data and operations that
 * serve a specific purpose &#151; for example to support dataset browsing
 * by providing easy access to datasets, thumbnails, classifications, etc.  
 * A data services view is defined by an interface that extends <code>
 * DataServiceView</code> and consists of a collection of asynchronous calls
 * that operate on (possibly) large portions of a data model in the background.
 * </p> 
 * <p>Agents obtain a reference to a given view through their registry by
 * specifying the view's defining interface as follows (note the <i>
 * required</i> cast on the returned reference):
 * <pre><code>
 * XxxView view = (XxxView) 
 *                  registry.getDataServicesView(XxxView.class);
 * </code></pre></p>
 * <p><code>XxxView</code> is obviously a made up name for one of the 
 * sub-interfaces of <code>DataServiceView</code> contained in this package.  
 * All calls are carried out asynchronously with respect to the caller's thread
 * and return a {@link CallHandle} object which can be used to cancel execution.
 * This object is then typically linked to a button so that the user can cancel
 * the task, like in the following example:
 * <pre><code>
 * final CallHandle handle = view.loadSomeDataInTheBg(observer);
 * 
 * //The above call returns immediately, so we don't have to wait.
 * //While the task is carried out, we allow the user to change 
 * //her mind and cancel the task:
 * 
 * cancelButton.addActionListener(new ActionListener() {
 *     public void actionPerformed(ActionEvent e) {
 *         handle.cancel();
 *     }
 * });
 * </code></pre></p>
 * <p>The <code>observer</code> argument to the above call is an instance of
 * {@link org.openmicroscopy.shoola.env.event.AgentEventListener}.  Normally
 * all calls within a view allow to specify this argument, which is used to
 * provide the caller with feedback on the progress of the task and with its
 * eventual outcome.</p>
 * <p>Specifically, as the computation proceeds in the background, 
 * {@link org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent}s are 
 * delivered to the <code>observer</code>.  These event objects have a status
 * field which contains a textual description of the activity currently being
 * carried out within the computation and a progress indicator which is set to 
 * the percentage of the work done so far.  So the indicator will be <code>0
 * </code> for the first feedback event and, if the computation runs to 
 * completion, <code>100</code> for the last feedback event, which will always 
 * have its status field set to <code>null</code> &#151; note that a <code>null
 * </code> status is also possible for the previous events if no description
 * was available at the time the event was fired.</p>
 * <p>It's important to keep in mind that the computation may not run to 
 * completion &#151; either because of an exception within the computation or
 * because the agent {@link CallHandle#cancel() cancels} execution.  In both 
 * cases, the feedback notification won't run to completion either.  However,
 * in any case a final 
 * {@link org.openmicroscopy.shoola.env.data.events.DSCallOutcomeEvent} is 
 * delivered to the <code>observer</code> to notify of the computation outcome
 * &#151; the event's methods can be used to find out the actual outcome and 
 * retrieve any result or exception.  Every call documents what is the returned
 * object and what are the possible exceptions so that the caller can later 
 * cast the returned value or exception as appropriate.</p>  
 * <p>Here's the code for a prototypical observer:
 * <pre><code>
 * public void eventFired(AgentEvent ae)
 * {
 *     if (ae instanceof DSCallFeedbackEvent) {  //Progress notification. 
 *         update((DSCallFeedbackEvent) ae);  //Inform the user.
 *     } else {  //Outcome notification.
 *         DSCallOutcomeEvent oe = (DSCallOutcomeEvent) ae;
 *         switch (oe.getState()) {
 *         case DSCallOutcomeEvent.CANCELLED:  //The user cancelled.
 *             handleCancellation();
 *             break;
 *         case DSCallOutcomeEvent.ERROR:  //The call threw an exception.
 *             handleException(oe.getException());
 *             break;
 *         case DSCallOutcomeEvent.NO_RESULT:  //The call returned no value.
 *             handleNullResult();
 *             break;
 *         case DSCallOutcomeEvent.HAS_RESULT:  //The call returned a value.
 *             handleResult(oe.getResult());
 *         }
 *     }
 * }
 * </code></pre></p>
 * <p>Because this logic is likely to be common to most of the observers, the
 * {@link org.openmicroscopy.shoola.env.data.events.DSCallAdapter} class factors
 * it out to provide a more convenient way to write obsevers.  Back to our
 * previous example, the <code>observer</code> could look something like the
 * following:
 * <pre><code>
 * observer = new DSCallAdapter() {
 *   public void update(DSCallFeedbackEvent fe) {  //Received some feedback.
 *       String status = fe.getStatus();
 *       int percDone = fe.getPercentDone();
 *       if (status == null) 
 *           status = (percDone == 100) ? "Done" :  //Else
 *                                      ""; //Description wasn't available.   
 *       statusBar.setText(status);  //A JLabel object part of the UI.
 *       progressBar.setValue(percDone); //A JProgressBar object part of the UI.
 *   }      
 *   public void onEnd() { //Called right before any of the handleXXX methods.
 *       progressBar.setVisible(false);  //Because the computation has finished.
 *   }
 *   public void handleResult(Object result) {  //Computation returned a result. 
 *       //We have a non-null return value.  Cast it to what 
 *       //loadSomeDataInTheBg() declared to return.
 *       SomeData data = (SomeData) result;
 * 
 *       //Update model, UI, etc.
 *   }
 *   public void handleCancellation() {  //Computation was cancelled.
 *       UserNotifier un = registry.getUserNotifier();
 *       un.notifyInfo("Data Loading", "SomeData task cancelled.");
 *   }
 *   public void handleException(Throwable exc) {  //An error occurred.
 *       UserNotifier un = registry.getUserNotifier();
 *       un.notifyError("Data Loading Failure",
 *                      "Couldn't retrieve SomeData.", exc);
 *   }
 * };
 * </code></pre></p>
 * 
 * <p>Note that the <code>observer</code>'s code in the example above works 
 * just like any other <i>Swing</i> listener.  In fact, all events are delivered
 * <i>sequentially</i> and wihin the <i>Swing</i> event dispatching thread.  
 * This means the <code>observer</code> can run synchronously with respect to 
 * the UI and won't need to worry about concurrency issues &#151; as long as it
 * runs within <i>Swing</i>.  Finally, also note that subsquent feedback events
 * imply computation progress and the 
 * {@link org.openmicroscopy.shoola.env.data.events.DSCallOutcomeEvent} is 
 * always the last event to be delivered in order of time.</p>
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
public interface DataServicesView
{
    
}
