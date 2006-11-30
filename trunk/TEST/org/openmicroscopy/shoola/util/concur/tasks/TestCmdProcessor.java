/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestCmdProcessor
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

import junit.framework.TestCase;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Routine unit test for {@link CmdProcessor}.
 * We verify that the various <code>exec</code> methods correclty build and
 * transfer the {@link java.lang.Runnable} command.
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
public class TestCmdProcessor
    extends TestCase
{

    //Object under test.  Note that for the purpose of these tests, we can 
    //consider this an instance of CmdProcessor as the only addition is the
    //possibility to inspect the command created by the exec methods.
    private FakeCmdProcessor    target;  
    
    //The command extracted from target by verifyCommand() -- this method
    //has to be called by all tests right after invoking exec().
    private ExecCommand         command;
    
    
    private void verifyCommand(ExecHandle returnedHandle)
    {
        Runnable cmd = target.getCommand();
        assertNotNull("Should never transfer a null command.", cmd);
        assertTrue("Should have transferred an ExecCommand.", 
                cmd instanceof ExecCommand);
        command = (ExecCommand) cmd;
        ExecHandle handle = command.getFuture();
        assertTrue("Broken bi-directional link between "+
                "ExecHandle and ExecCommand.", 
                command == handle.getCommand());
        assertTrue("Returned handle not the same as the one linked to the "+
                "command.", handle == returnedHandle);
        
    }
    
    private void verifyTaskAdapter(Runnable originalTask)
    {
        MultiStepTask task = command.getTask();
        //Skip test for null (done in ExecCommand constructor's tests).
        assertTrue("Command should have been linked to a TaskAdapter.", 
                task instanceof TaskAdapter);
        assertTrue("TaskAdapter not linked to the original task.", 
                ((TaskAdapter) task).getTask() == originalTask);
    }
    
    private void verifyInvocationAdapter(Invocation originalCall)
    {
        MultiStepTask task = command.getTask();
        //Skip test for null (done in ExecCommand constructor's tests).
        assertTrue("Command should have been linked to an InvocationAdapter.", 
                task instanceof InvocationAdapter);
        assertTrue("InvocationAdapter not linked to the original Invocation.", 
                ((InvocationAdapter) task).getCall() == originalCall);
    }
    
    private void verifyInvocationChainAdapter(Invocation[] originalChain)
    {
        MultiStepTask task = command.getTask();
        //Skip test for null (done in ExecCommand constructor's testsr).
        assertTrue("Command should have been linked to an "+
                "InvocationChainAdapter.", 
                task instanceof InvocationChainAdapter);
        Invocation[] chain = ((InvocationChainAdapter) task).getChain();
        assertEquals("InvocationChainAdapter not linked to the original"+
                " chain (different lengths).", 
                originalChain.length, chain.length);
        for (int i = 0; i < chain.length; i++)
            assertTrue("InvocationChainAdapter not linked to the original"+
                    " chain (different element at "+i+").", 
                    originalChain[i] == chain[i]);
    }
    
    private void verifyMultiStepTask(MultiStepTask originalTask)
    {
        assertTrue("Command not linked to the original MultiStepTask.", 
                command.getTask() == originalTask);
    }
    
    private void verifyResultAssembler(ResultAssembler originalRA)
    {
        //Skip test for null (done in ExecCommand constructor's tests).
        assertTrue("Command not linked to the original ResultAssembler.", 
                command.getAssembler() == originalRA);
    }
    
    private void verifyPlainResultAssembler()
    {
        ResultAssembler assembler = command.getAssembler();
        //Skip test for null (done in ExecCommand constructor's tests).
        assertTrue("Command should have been linked to a PlainAssembler.", 
                assembler instanceof PlainAssembler);
    }
    
    private void verifyListResultAssembler()
    {
        ResultAssembler assembler = command.getAssembler();
        //Skip test for null (done in ExecCommand constructor's tests).
        assertTrue("Command should have been linked to a ListAssembler.", 
                assembler instanceof ListAssembler);
    }
    
    private void verifyNullResultAssembler()
    {
        ResultAssembler assembler = command.getAssembler();
        //Skip test for null (done in ExecCommand constructor's tests).
        assertTrue("Command should have been linked to a "+
                "NullResultAssembler.", 
                assembler instanceof NullResultAssembler);
    }
    
    private void verifyNotNullFuture()
    {
        Future future = command.getFuture();
        //Skip test for null (done in ExecCommand constructor's tests).
        assertFalse("Command should not have been linked to a NullFuture.", 
                future instanceof NullFuture);
    }
    
    private void verifyNullFuture()
    {
        Future future = command.getFuture();
        //Skip test for null (done in ExecCommand constructor's tests).
        assertTrue("Command should have been linked to a NullFuture.", 
                future instanceof NullFuture);
    }
    
    private void verifyExecMonitor(ExecMonitor originalEM)
    {
        //Skip test for null (done in ExecCommand constructor's tests).
        assertTrue("Command not linked to the original ExecMonitor.", 
                command.getObserver() == originalEM);
    }
    
    private void verifyNullExecMonitor()
    {
        ExecMonitor observer = command.getObserver();
        //Skip test for null (done in ExecCommand constructor's tests).
        assertTrue("Command should have been linked to a NullExecMonitor.", 
                observer instanceof NullExecMonitor);
    }
    
    
    public void setUp()
    {
        target = new FakeCmdProcessor();
        command = null;
    }
    
    
    //exec(Runnable)
    public void testExecRunnable()
    {
        Runnable task = new MockExecCommand();
        ExecHandle hanlde = target.exec(task);
        verifyCommand(hanlde);
        verifyTaskAdapter(task);
        verifyNullResultAssembler();
        verifyNullFuture();
        verifyNullExecMonitor();
    }

    //exec(Runnable, ExecMonitor)
    public void testExecRunnableExecMonitor()
    {
        Runnable task = new MockExecCommand();
        ExecMonitor observer = new NullExecMonitor();
        ExecHandle hanlde = target.exec(task, observer);
        verifyCommand(hanlde);
        verifyTaskAdapter(task);
        verifyNullResultAssembler();
        verifyNullFuture();
        verifyExecMonitor(observer);
    }

    //exec(Invocation)
    public void testExecInvocation()
    {
        Invocation call = new MockInvocation();
        ExecHandle hanlde = target.exec(call);
        verifyCommand(hanlde);
        verifyInvocationAdapter(call);
        verifyPlainResultAssembler();
        verifyNotNullFuture();
        verifyNullExecMonitor();
    }

    //exec(Invocation, ExecMonitor)
    public void testExecInvocationExecMonitor()
    {
        Invocation call = new MockInvocation();
        ExecMonitor observer = new NullExecMonitor();
        ExecHandle hanlde = target.exec(call, observer);
        verifyCommand(hanlde);
        verifyInvocationAdapter(call);
        verifyPlainResultAssembler();
        verifyNotNullFuture();
        verifyExecMonitor(observer);
    }

    //exec(Invocation[])
    public void testExecInvocationArray()
    {
        Invocation[] chain = 
            new Invocation[] {new MockInvocation()};
        ExecHandle hanlde = target.exec(chain);
        verifyCommand(hanlde);
        verifyInvocationChainAdapter(chain);
        verifyListResultAssembler();
        verifyNotNullFuture();
        verifyNullExecMonitor();
    }

    //exec(Invocation[], ExecMonitor)
    public void testExecInvocationArrayExecMonitor()
    {
        Invocation[] chain = 
            new Invocation[] {new MockInvocation(), new MockInvocation()};
        ExecMonitor observer = new NullExecMonitor();
        ExecHandle hanlde = target.exec(chain, observer);
        verifyCommand(hanlde);
        verifyInvocationChainAdapter(chain);
        verifyListResultAssembler();
        verifyNotNullFuture();
        verifyExecMonitor(observer);
    }

    //exec(Invocation[], ResultAssembler)
    public void testExecInvocationArrayResultAssembler()
    {
        Invocation[] chain = 
            new Invocation[] {new MockInvocation(), new MockInvocation(),
                                new MockInvocation()};
        ResultAssembler assembler = new NullResultAssembler();
        ExecHandle hanlde = target.exec(chain, assembler);
        verifyCommand(hanlde);
        verifyInvocationChainAdapter(chain);
        verifyResultAssembler(assembler);
        verifyNotNullFuture();
        verifyNullExecMonitor();
    }

    //exec(Invocation[], ResultAssembler, ExecMonitor)
    public void testExecInvocationArrayResultAssemblerExecMonitor()
    {
        Invocation[] chain = new Invocation[] {new MockInvocation()};
        ResultAssembler assembler = new NullResultAssembler();
        ExecMonitor observer = new NullExecMonitor();
        ExecHandle hanlde = target.exec(chain, assembler, observer);
        verifyCommand(hanlde);
        verifyInvocationChainAdapter(chain);
        verifyResultAssembler(assembler);
        verifyNotNullFuture();
        verifyExecMonitor(observer);
    }

    //exec(MultiStepTask)
    public void testExecMultiStepTask()
    {
        MultiStepTask task = new NullMultiStepTask();
        ExecHandle hanlde = target.exec(task);
        verifyCommand(hanlde);
        verifyMultiStepTask(task);
        verifyListResultAssembler();
        verifyNotNullFuture();
        verifyNullExecMonitor();
    }

    //exec(MultiStepTask, ExecMonitor)
    public void testExecMultiStepTaskExecMonitor()
    {
        MultiStepTask task = new NullMultiStepTask();
        ExecMonitor observer = new NullExecMonitor();
        ExecHandle hanlde = target.exec(task, observer);
        verifyCommand(hanlde);
        verifyMultiStepTask(task);
        verifyListResultAssembler();
        verifyNotNullFuture();
        verifyExecMonitor(observer);
    }

    //exec(MultiStepTask, ResultAssembler)
    public void testExecMultiStepTaskResultAssembler()
    {
        MultiStepTask task = new NullMultiStepTask();
        ResultAssembler assembler = new NullResultAssembler();
        ExecHandle hanlde = target.exec(task, assembler);
        verifyCommand(hanlde);
        verifyMultiStepTask(task);
        verifyResultAssembler(assembler);
        verifyNotNullFuture();
        verifyNullExecMonitor();
    }

    //exec(MultiStepTask, ResultAssembler, ExecMonitor)
    public void testExecMultiStepTaskResultAssemblerExecMonitor()
    {
        MultiStepTask task = new NullMultiStepTask();
        ResultAssembler assembler = new NullResultAssembler();
        ExecMonitor observer = new NullExecMonitor();
        ExecHandle hanlde = target.exec(task, assembler, observer);
        verifyCommand(hanlde);
        verifyMultiStepTask(task);
        verifyResultAssembler(assembler);
        verifyNotNullFuture();
        verifyExecMonitor(observer);
    }

}
