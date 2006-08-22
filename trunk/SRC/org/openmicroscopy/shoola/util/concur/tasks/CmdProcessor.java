/*
 * org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor
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

package org.openmicroscopy.shoola.util.concur.tasks;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Exposes the interface to trigger the execution of a service.
 * This interface is made up by the different flavors of the <code>exec</code>
 * method, which is overloaded to accept a service object (an instance of
 * {@link Runnable}, {@link Invocation}, {@link MultiStepTask}, or an 
 * {@link Invocation} chain) and optionally an {@link ExecMonitor} (to get
 * feedback about the execution progress) and/or a {@link ResultAssembler}
 * (to provide a specific way to assemble the computatation results from 
 * partial results).
 * <p>This is an abstract class which delegates the service execution to 
 * subclasses.  A concrete subclass encapsulates an execution policy and 
 * enforces a specific threads management.</p> 
 * <p>Concrete sublcasses normally execute the service in its own thread, so
 * execution is asynchronous with respect to the client thread that called the
 * <code>exec</code> method.  However, this is not an absolute requirement.
 * For example, subclasses could enforce an execution policy by which execution
 * takes place in the client thread if no threading resources are available at
 * the moment of triggering execution.</p>  
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
public abstract class CmdProcessor
{
    
    /**
     * Subclasses have to implement this method in order to execute the
     * service.
     * Note that the service workflow is completely encapsulated by the
     * <code>cmd</code> object, so a subclass will only have to call its
     * <code>run</code> method in order to execute the service.  However,
     * no attempt should ever be made to execute the <code>run</code> 
     * method twice or by more than one thread &#151; an {@link Error}
     * would be thrown.  A concrete <code>CmdProcessor</code> should also
     * check the interrupted status of the executing thread after the 
     * <code>run</code> method returns as cancellation results in the 
     * interrupted status being set.
     * 
     * @param cmd   Enapsulates the service workflow.
     */
    protected abstract void doExec(Runnable cmd);
    
    /**
     * Executes the specified <code>task</code>.
     *   
     * @param task  The task to execute. Mustn't be <code>null</code>.
     * @return An {@link ExecHandle} which can be used to cancel execution.
     */
    public ExecHandle exec(Runnable task) { return exec(task, null); }
    
    /**
     * Executes the specified <code>task</code>.
     *   
     * @param task  The task to execute. Mustn't be <code>null</code>.
     * @param observer  To get feedback about the execution progress.
     * @return An {@link ExecHandle} which can be used to cancel execution.
     */
    public ExecHandle exec(Runnable task, ExecMonitor observer)
    {
        //Adaptation.  We need to have a cmd linked to exactly one
        //MultiStepTask, ResultAssembler, Future, and ExecMonitor.
        MultiStepTask adapter = new TaskAdapter(task);
        ResultAssembler nullRA = new NullResultAssembler();
        if (observer == null) observer = new NullExecMonitor();
        Future execHandle = new NullFuture();  //Not in a legal state yet.
        ExecCommand cmd = 
            new ExecCommand(adapter, nullRA, execHandle, observer);
        execHandle.setCommand(cmd);  //OK, init completed now (two-step init).
        
        //Transfer command.
        doExec(cmd);  //Normally executed in a different thread.  
        
        //Allow client to cancel execution.
        return execHandle;
    }
    
    /**
     * Executes the specified <code>call</code>.
     *   
     * @param call  The call to execute.  Mustn't be <code>null</code>.
     * @return A {@link Future} to collect the result of the call.  It can also
     *          be used to cancel execution.
     */
    public Future exec(Invocation call) { return exec(call, null); }
    
    /**
     * Executes the specified <code>call</code>.
     *   
     * @param call  The call to execute.  Mustn't be <code>null</code>.
     * @param observer  To get feedback about the execution progress.
     * @return A {@link Future} to collect the result of the call.  It can also
     *          be used to cancel execution.
     */
    public Future exec(Invocation call, ExecMonitor observer)
    {
        //Adaptation.  We need to have a cmd linked to exactly one
        //MultiStepTask, ResultAssembler, Future, and ExecMonitor.
        MultiStepTask adapter = new InvocationAdapter(call);
        ResultAssembler plainRA = new PlainAssembler();
        if (observer == null) observer = new NullExecMonitor();
        Future future = new Future();  //Not in a legal state yet.
        ExecCommand cmd = 
            new ExecCommand(adapter, plainRA, future, observer);
        future.setCommand(cmd);  //OK, init completed now (two-step init).
        
        //Transfer command.
        doExec(cmd);  //Normally executed in a different thread.  
        
        //Allow client to retrieve result and cancel execution.
        return future;
    }
    
    /**
     * Executes the specified {@link Invocation} <code>chain</code>.
     * The {@link Invocation} objects that make up the chain are invoked
     * following the array order (<code>chain[0]</code>, <code>chain[1]</code>,
     * and so on). 
     *   
     * @param chain The {@link Invocation} chain to execute. Mustn't be 
     *              <code>null</code> or <code>0</code>-length and none of
     *              its elements must be <code>null</code> either.
     * @return A {@link Future} to collect the result of the invocation.  In
     *          this case, the object returned by the <code>getResult</code>
     *          method is a {@link java.util.List} whose first element is
     *          the result of the fisrt call in the chain (<code>chain[0]</code>
     *          ), the second element is the result of the second call in the 
     *          chain (<code>chain[1]</code>), and so on.  The {@link Future}
     *          object can also be used to cancel execution.
     */
    public Future exec(Invocation[] chain) 
    { 
        return exec(chain, null, null); 
    }
    
    /**
     * Executes the specified {@link Invocation} <code>chain</code>.
     * The {@link Invocation} objects that make up the chain are invoked
     * following the array order (<code>chain[0]</code>, <code>chain[1]</code>,
     * and so on). 
     *   
     * @param chain  The {@link Invocation} chain to execute.  Mustn't be 
     *                  <code>null</code> or <code>0</code>-length and none of
     *                  its elements must be <code>null</code> either.
     * @param observer  To get feedback about the execution progress.
     * @return A {@link Future} to collect the result of the invocation.  In
     *          this case, the object returned by the <code>getResult</code>
     *          method is a {@link java.util.List} whose first element is
     *          the result of the fisrt call in the chain (<code>chain[0]</code>
     *          ), the second element is the result of the second call in the 
     *          chain (<code>chain[1]</code>), and so on.  The {@link Future}
     *          object can also be used to cancel execution.
     */
    public Future exec(Invocation[] chain, ExecMonitor observer)
    {
        return exec(chain, null, observer);
    }
    
    /**
     * Executes the specified {@link Invocation} <code>chain</code>.
     * The {@link Invocation} objects that make up the chain are invoked
     * following the array order (<code>chain[0]</code>, <code>chain[1]</code>,
     * and so on). 
     *   
     * @param chain  The {@link Invocation} chain to execute.  Mustn't be 
     *                  <code>null</code> or <code>0</code>-length and none of
     *                  its elements must be <code>null</code> either.
     * @param rAsm  To provide a specific way to assemble the computatation 
     *              results from partial results (that is, from the results
     *              returned by each call in the chain).
     * @return A {@link Future} to collect the result of the invocation.  In
     *          this case, the object returned by the <code>getResult</code>
     *          method is whatever object was returned by the 
     *          {@link ResultAssembler#assemble() assemble} method of 
     *          <code>rAsm</code>.  The {@link Future} object can also be used
     *          to cancel execution.
     */
    public Future exec(Invocation[] chain, ResultAssembler rAsm)
    {
        return exec(chain, rAsm, null);
    }
    
    /**
     * Executes the specified {@link Invocation} <code>chain</code>.
     * The {@link Invocation} objects that make up the chain are invoked
     * following the array order (<code>chain[0]</code>, <code>chain[1]</code>,
     * and so on). 
     *   
     * @param chain  The {@link Invocation} chain to execute.  Mustn't be 
     *                  <code>null</code> or <code>0</code>-length and none of
     *                  its elements must be <code>null</code> either.
     * @param rAsm  To provide a specific way to assemble the computatation 
     *              results from partial results (that is, from the results
     *              returned by each call in the chain).
     * @param observer  To get feedback about the execution progress.
     * @return A {@link Future} to collect the result of the invocation.  In
     *          this case, the object returned by the <code>getResult</code>
     *          method is whatever object was returned by the 
     *          {@link ResultAssembler#assemble() assemble} method of 
     *          <code>rAsm</code>.  The {@link Future} object can also be used
     *          to cancel execution.
     */
    public Future exec(Invocation[] chain, ResultAssembler rAsm, 
                        ExecMonitor observer)
    {
        //Adaptation.  We need to have a cmd linked to exactly one
        //MultiStepTask, ResultAssembler, Future, and ExecMonitor.
        MultiStepTask adapter = new InvocationChainAdapter(chain);
        if (rAsm == null) rAsm = new ListAssembler();
        if (observer == null) observer = new NullExecMonitor();
        Future future = new Future();  //Not in a legal state yet.
        ExecCommand cmd = 
            new ExecCommand(adapter, rAsm, future, observer);
        future.setCommand(cmd);  //OK, init completed now (two-step init).
        
        //Transfer command.
        doExec(cmd);  //Normally executed in a different thread.  
        
        //Allow client to retrieve result and cancel execution.
        return future;
    }
    
    /**
     * Executes the specified multi-step <code>task</code>.
     * The {@link MultiStepTask#doStep() doStep} method is invoked in a loop
     * until the {@link MultiStepTask#isDone() isDone} method returns
     * <code>true</code>.
     *   
     * @param task  The multi-step task to execute.  Mustn't be 
     *              <code>null</code>.
     * @return A {@link Future} to collect the result of the invocation.  In
     *          this case, the object returned by the <code>getResult</code>
     *          method is a {@link java.util.List} whose first element is
     *          the result of the fisrt step in the computation (that is, the
     *          first call to the <code>doStep</code> method), the second 
     *          element is the result of the second step in the computation,
     *          and so on.  The {@link Future} object can also be used to
     *          cancel execution.
     */
    public Future exec(MultiStepTask task) 
    { 
        return exec(task, null, null); 
    }
    
    /**
     * Executes the specified multi-step <code>task</code>.
     * The {@link MultiStepTask#doStep() doStep} method is invoked in a loop
     * until the {@link MultiStepTask#isDone() isDone} method returns
     * <code>true</code>.
     *   
     * @param task  The multi-step task to execute.  Mustn't be 
     *              <code>null</code>.
     * @param observer  To get feedback about the execution progress.
     * @return A {@link Future} to collect the result of the invocation.  In
     *          this case, the object returned by the <code>getResult</code>
     *          method is a {@link java.util.List} whose first element is
     *          the result of the fisrt step in the computation (that is, the
     *          first call to the <code>doStep</code> method), the second 
     *          element is the result of the second step in the computation,
     *          and so on.  The {@link Future} object can also be used to
     *          cancel execution.
     */
    public Future exec(MultiStepTask task, ExecMonitor observer)
    {
        return exec(task, null, observer);
    }
    
    /**
     * Executes the specified multi-step <code>task</code>.
     * The {@link MultiStepTask#doStep() doStep} method is invoked in a loop
     * until the {@link MultiStepTask#isDone() isDone} method returns
     * <code>true</code>.
     *   
     * @param task  The multi-step task to execute.  Mustn't be 
     *              <code>null</code>.
     * @param rAsm  To provide a specific way to assemble the computatation 
     *              results from partial results (that is, from the results
     *              returned by each call the <code>doStep</code> method).
     * @return A {@link Future} to collect the result of the invocation.  In
     *          this case, the object returned by the <code>getResult</code>
     *          method is whatever object was returned by the 
     *          {@link ResultAssembler#assemble() assemble} method of 
     *          <code>rAsm</code>.  The {@link Future} object can also be used
     *          to cancel execution.
     */
    public Future exec(MultiStepTask task, ResultAssembler rAsm)
    {
        return exec(task, rAsm, null);
    }
    
    /**
     * Executes the specified multi-step <code>task</code>.
     * The {@link MultiStepTask#doStep() doStep} method is invoked in a loop
     * until the {@link MultiStepTask#isDone() isDone} method returns
     * <code>true</code>.
     *   
     * @param task  The multi-step task to execute.  Mustn't be 
     *              <code>null</code>.
     * @param rAsm  To provide a specific way to assemble the computatation 
     *              results from partial results (that is, from the results
     *              returned by each call the <code>doStep</code> method).
     * @param observer  To get feedback about the execution progress.
     * @return A {@link Future} to collect the result of the invocation.  In
     *          this case, the object returned by the <code>getResult</code>
     *          method is whatever object was returned by the 
     *          {@link ResultAssembler#assemble() assemble} method of 
     *          <code>rAsm</code>.  The {@link Future} object can also be used
     *          to cancel execution.
     */
    public Future exec(MultiStepTask task, ResultAssembler rAsm, 
                        ExecMonitor observer)
    {
        if (task == null) throw new NullPointerException("No task.");
        
        //Adaptation.  We need to have a cmd linked to exactly one
        //MultiStepTask, ResultAssembler, Future, and ExecMonitor.
        if (rAsm == null) rAsm = new ListAssembler();
        if (observer == null) observer = new NullExecMonitor();
        Future future = new Future();  //Not in a legal state yet.
        ExecCommand cmd = 
            new ExecCommand(task, rAsm, future, observer);
        future.setCommand(cmd);  //OK, init completed now (two-step init).
        
        //Transfer command.
        doExec(cmd);  //Normally executed in a different thread.  
        
        //Allow client to retrieve result and cancel execution.
        return future;
    }

}
