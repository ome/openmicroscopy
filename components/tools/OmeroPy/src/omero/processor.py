#!/usr/bin/env python
#
# OMERO Grid Processor
# Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero, Ice
import os, signal, subprocess, sys, threading, tempfile

CONFIG="""
omero.user=%s
omero.pass=%s
Ice.Default.Router=%s
Ice.ACM.Client=0
Ice.MonitorConnections=60
Ice.RetryIntervals=-1
Ice.Warn.Connections=1
Ice.ImplicitContext=Shared
Ice.GC.Interval=60
"""

class ProcessI(omero.grid.Process):
    """
    Wrapper around a subprocess.Popen instance. Returned by ProcessorI
    when a job is submitted. This implementation uses the given
    interpreter to call a file that must be named "script" in the 
    generated temporary directory.

    Call is equivalent to:

    cd TMP_DIR
    ICE_CONFIG=./config interpreter ./script

    """

    def __init__(self, interpreter, properties, log):
        self.interpreter = interpreter
        self.properties = properties
        self.log = log
        # Non arguments
        self.callbacks = []
        self.lock = threading.Lock()
        self.dir = tempfile.mkdtemp()
        self.script_name = os.path.join(self.dir, "script")
        self.config_name = os.path.join(self.dir, "config")
        self.env = {}
        self.env["ICE_CONFIG"] = self.config_name
        self.make_config()

    def activate(self):
        """
        Process creation has to wait until all external downloads, etc
        are finished.
        """
        self.popen = subprocess.Popen([self.interpreter, "./script"], cwd=self.dir, env=self.env)

    def __del__(self):
        """
        Cleans up the temporary directory used by the process, and terminates
        the Popen process if running.
        """
        if not self.proc.poll():
            self.log.warning("Sending %s SIGTERM." % str(proc.pid))
            os.kill(proc.pid, signal.SIGTERM)

            for i in range(5,0,-1):
                time.sleep(6)
                if proc.poll():
                    self.log.warning("Process %s terminated cleanly." % str(proc.pid))
                    break
                self.log.warning("%s still active. Killing in %s seconds." % (str(proc.pid),6*(i-1)+1))

        os.removedirs(self.dir)

        if not proc.poll():
            self.log.error("Killing %s..." % str(proc.pid))
            try:
                os.kill(proc.pid, signal.SIGKILL)
            except OSError, oserr:
                pass

    def make_config(self):
        config_file = open(self.config_name, "w")
        try:
            name = self.properties["name"]
            pasw = self.properties["pasw"]
            conn = self.properties["conn"]
            config_file.write(CONFIG % (name,pasw,conn))
        finally:
            config_file.close()

    def poll(self, current = None):
        rv = self.popen.poll()
        if not rv:
            return None

        rv = omero.RInt(rv)
        self.allcallbacks("processFinished", rv)
        return rv

    def wait(self, current = None):
        rv = self.popen.wait()
        self.allcallbacks("processFinished",rv)
        return rv

    def cancel(self, current = None):
        os.kill(self.popen.pid)
        self.allcallbacks("processCancelled", True)

    def kill(self, current = None):
        os.kill(self.popen.pid)
        self.allcallbacks("processKilled", True)

    def registerCallback(self, callback, current = None):
        self.lock.acquire()
        try:
            self.callbacks.append(callback)
        finally:
            self.lock.release()

    def unregisterCallback(self, callback, current = None):
        self.lock.acquire()
        try:
            self.callbacks.remove(callback)
        finally:
            self.lock.release()

    def allcallbacks(self, method, arg):
        self.lock.acquire()
        try:
            for cb in self.callbacks:
                m = getattr(cb,method)
                m(arg)
        finally:
            self.lock.release()

class ProcessorI(omero.grid.Processor):

    def __init__(self, log):
        self.log = log
        self.cfg = os.path.join(os.curdir, "etc", "ice.config")
        self.cfg = os.path.abspath(self.cfg)
        print "init"

    def processJob(self, session, job, current = None):
        """
        """
        print "process"
        client = omero.client(["--Ice.Config=%s" % (self.cfg)])
        sf = client.createSession(session, session)
        ec = sf.getAdminService().getEventContext()

        print "checking"
        # Should actually reload the Job here.
        if not job.originalFileLinksLoaded or \
                len(job.originalFileLinks) != 1:
            raise omero.ApiUsageException(\
                None, None, "Job should have one file")

        file = job.originalFileLinks[0].child
        file = sf.getQueryService().findByQuery(\
            """select o from OriginalFile o
             join fetch o.format where o.id = %d 
             and o.details.owner.id = 0
             and o.format.value = 'text/x-python' 
             """ % file.id.val, None)

        if not file:
            raise omero.ApiUsageException(\
                None, None, "File does not match processor criteria")

        properties = {}
        properties["name"] = ec.userName
        properties["pasw"] = session
        properties["conn"] = client.getProperty("Ice.Default.Router")

        print "creating process"
        process = ProcessI("python", properties, self.log)
        client.download(file, process.script_name)
        process.activate()

        prx = current.adapter.addWithUUID(process)
        return omero.grid.ProcessPrx.uncheckedCast(prx)

class Server(Ice.Application):
    """
    Basic server implementation
    """
    def run(self,args):
        self.shutdownOnInterrupt()
        self.objectfactory = omero.ObjectFactory()
        self.objectfactory.registerObjectFactory(self.communicator())
        self.adapter = self.communicator().createObjectAdapter("ProcessorAdapter")
        p = ProcessorI(self.communicator().getLogger())
        p.serverid = self.communicator().getProperties().getProperty("Ice.ServerId")

        self.adapter.add(p, Ice.stringToIdentity("Processor"))
        self.adapter.activate()
        self.communicator().waitForShutdown()
        if self.interrupted():
            print self.appName()+": terminating"
            return 0
        self.adapter

if __name__ == "__main__":
    app=Server()
    sys.exit(app.main(sys.argv))
