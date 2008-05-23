#!/usr/bin/env python
#
# OMERO Grid Processor
# Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero, Ice
import os, signal, subprocess, sys, threading, tempfile, time, traceback
from omero_model_OriginalFileI import OriginalFileI

CONFIG="""
Ice.ACM.Client=0
Ice.MonitorConnections=60
Ice.RetryIntervals=-1
Ice.Warn.Connections=1
Ice.ImplicitContext=Shared
Ice.GC.Interval=60
"""

class Resources:
    """
    Container class for storing resources which should be
    cleaned up on close.
    """
    def __init__(self):
        self.stuff = []
    def add(self, object, cleanupMethod = "cleanup"):
        lock = threading.RLock()
        lock.acquire()
        try:
            self.stuff.append((object,cleanupMethod))
        finally:
            lock.release()
    def cleanupNext(self):
        lock = threading.RLock()
        lock.acquire()
        try:
            try:
                if len(self.stuff) > 0:
                    m = self.stuff.pop(0)
                    method = getattr(m[0],m[1])
                    method()
                    return len(self.stuff) > 0
                else:
                    return False
            except:
                print "Error cleaning resource:",m
                traceback.print_exc()
        finally:
            lock.release()

class Environment:
    """
    Simple class for creating an executable environment
    """

    def __init__(self,*args):
        """
        Takes an number of environment variable names which
        should be copied to the target environment if present
        in the current execution environment.
        """
        self.env = {}
        for arg in args:
            if os.environ.has_key(arg):
                self.env[arg] = os.environ[arg]
    def __call__(self):
        """
        Returns the environment map when called.
        """
        return self.env

    def set(self, key, value):
        """
        Manually sets a value in the target environment.
        """
        self.env[key] = value

    def append(self, key, addition):
        """
        Manually adds a value to the environment string
        """
        if self.env.has_key(key):
            self.env[key] = os.pathsep.join([self.env[key], addition])
        else:
            self.set(key, addition)

class ProcessI(omero.grid.Process):
    """
    Wrapper around a subprocess.Popen instance. Returned by ProcessorI
    when a job is submitted. This implementation uses the given
    interpreter to call a file that must be named "script" in the
    generated temporary directory.

    Call is equivalent to:

    cd TMP_DIR
    ICE_CONFIG=./config interpreter ./script >out 2>err &

    The properties argument is used to generate the ./config file.
    
    The params argument may be null in which case this process
    is being used solely to calculate the parameters for the script
    ("omero.scripts.parse=true")

    """

    def __init__(self, interpreter, properties, params, log):
        self.active = False
        self.dead = False
        self.interpreter = interpreter
        self.properties = properties
        self.params = params
        self.log = log
        # Non arguments
        self.callbacks = []
        self.lock = threading.Lock()
        self.dir = tempfile.mkdtemp()
        self.script_name = os.path.join(self.dir, "script")
        self.config_name = os.path.join(self.dir, "config")
        self.stdout_name = os.path.join(self.dir, "out")
        self.stderr_name = os.path.join(self.dir, "err")
        self.env = Environment("PATH","PYTHONPATH","DYLD_LIBRARY_PATH","LD_LIBRARY_PATH","MLABRAW_CMD_STR")
        self.env.set("ICE_CONFIG", self.config_name)
        # WORKAROUND
        # Currently duplicating the logic here as in the PYTHONPATH
        # setting of the grid application descriptor (see etc/grid/*.xml)
        # This should actually be taken care of in the descriptor itself
        # by having setting PYTHONPATH to an absolute value. This is
        # not currently possible with IceGrid (without using icepatch --
        # see 39.17.2 "node.datadir).
        self.env.append("PYTHONPATH", os.path.join(os.getcwd(), "lib"))
        self.make_config()

    def activate(self):
        """
        Process creation has to wait until all external downloads, etc
        are finished.
        """
        self.stdout = open(self.stdout_name, "w")
        self.stderr = open(self.stderr_name, "w")
        self.popen = subprocess.Popen([self.interpreter, "./script"], cwd=self.dir, env=self.env(), stdout=self.stdout, stderr=self.stderr)
        self.active = True

    def __del__(self):
        """
        Cleans up the temporary directory used by the process, and terminates
        the Popen process if running.
        """
        try:
            self.lock.acquire()
            if not self.dead:
                self.cleanup_popen()
                self.cleanup_output()
                self.upload_output()
                self.cleanup_tmpdir()
        finally:
            self.dead = True
            self.lock.release()

    def cleanup_popen(self):
        """
        If self.popen is active, then first call cancel, wait a period of
        time, and finally call kill.
        """
        if hasattr(self, "popen") and None == self.popen.poll():
            self.cancel()

            for i in range(5,0,-1):
                time.sleep(6)
                if None != self.popen.poll():
                    self.log.warning("Process %s terminated cleanly." % str(self.popen.pid))
                    break
                else:
                    self.log.warning("%s still active. Killing in %s seconds." % (str(self.popen.pid),6*(i-1)+1))
            self.kill()

    def cleanup_output(self):
        """
        Flush and close the stderr and stdout streams.
        """
        if hasattr(self, "stderr"):
            self.stderr.flush()
            self.stderr.close()
        if hasattr(self, "stdout"):
            self.stdout.flush()
            self.stdout.close()

    def upload_output(self):
        """
        If this is not a params calculation (i.e. parms != null) and the
        stdout or stderr are non-null, they they will be uploaded and
        attached to the job.
        """
        if self.params:
            client = omero.client(["--Ice.Config=%s" % self.config_name])
            client.createSession()
            self._upload(client, self.stdout_name, "stdout", self.params.stdoutFormat)
            self._upload(client, self.stderr_name, "stderr", self.params.stderrFormat)

    def _upload(self, client, filename, name, format):
        if format:
            if os.path.getsize(filename):
                ofile = client.upload(filename, name=name, type=format)
                jobid = long(client.getProperty("omero.job"))
                link = omero.model.JobOriginalFileLinkI()
                link.parent = omero.model.ScriptJobI(omero.RLong(jobid), False)
                link.child = ofile
                client.getSession().getUpdateService().saveObject(link)

    def cleanup_tmpdir(self):
        """
        Remove all known files and finally the temporary directory.
        If other files exist, an exception will be raised.
        """
        for path in [self.config_name, self.stdout_name, self.stderr_name, self.script_name]:
            if os.path.exists(path):
                os.remove(path)
        os.removedirs(self.dir)

    def make_config(self):
        """
        Creates the ICE_CONFIG file used by the client.
        """
        config_file = open(self.config_name, "w")
        try:
            config_file.write(CONFIG)
            for key in self.properties.iterkeys():
                config_file.write("%s=%s\n"%(key, self.properties[key]))
        finally:
            config_file.close()

    def poll(self, current = None):
        rv = self.popen.poll()
        if None == rv:
            return None
        rv = omero.RInt(rv)
        self.allcallbacks("processFinished", rv)
        self.__del__()
        return rv

    def wait(self, current = None):
        rv = self.popen.wait()
        self.allcallbacks("processFinished",rv)
        self.__del__()
        return rv

    def _send(self, sig):
        if None == self.popen.poll():
            try:
                os.kill(self.popen.pid, sig)
            except OSError, oserr:
                # Already gone
                pass
        return self.popen.poll() != None

    def cancel(self, current = None):
        rv = self._send(signal.SIGTERM)
        self.allcallbacks("processCancelled", rv)
        if rv:
            self.__del__()
        return rv

    def kill(self, current = None):
        rv = self._send(signal.SIGKILL)
        self.allcallbacks("processKilled", rv)
        self.__del__()
        return rv

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
                    try:
                        m = getattr(cb,method)
                        m(arg)
                    except:
                        print "Error calling callback " + str(cb)
        finally:
            self.lock.release()

class ProcessorI(omero.grid.Processor):

    def __init__(self, log):
        self.log = log
        self.cfg = os.path.join(os.curdir, "etc", "ice.config")
        self.cfg = os.path.abspath(self.cfg)
        self.resources = Resources()

    def parseJob(self, session, job, current = None):
        properties = {}
        properties["omero.scripts.parse"] = "true"
        process = self.process(session, job, current, None, properties)
        process.wait()
        client = omero.client(["--Ice.Config=%s" % (self.cfg)])
        client.joinSession(session)
        rv = client.getOutput("omero.scripts.parse")
        if rv != None:
            return rv.val
        return None

    def processJob(self, session, job, current = None):
        """
        """
        params = self.parseJob(session, job, current)
        return self.process(session, job, current, params)

    def process(self, session, job, current, params, properties = {}):
        client = omero.client(["--Ice.Config=%s" % (self.cfg)])
        sf = client.createSession(session, session)
        ec = sf.getAdminService().getEventContext()

        handle = sf.createJobHandle()
        handle.attach(job.id.val)
        if handle.jobFinished():
            raise omero.ApiUsageException("Job already finished.")

        file = sf.getQueryService().findByQuery(\
            """select o from Job j
             join j.originalFileLinks links
             join links.child o
             join o.format
             where
                 j.id = %d
             and o.details.owner.id = 0
             and o.format.value = 'text/x-python'
             """ % job.id.val, None)


        if not file:
            raise omero.ApiUsageException(\
                None, None, "Job should have one executable file attached.")

        properties["omero.job"]  = str(job.id.val)
        properties["omero.user"] = session
        properties["omero.pass"] = session
        properties["Ice.Default.Router"] = client.getProperty("Ice.Default.Router")

        process = ProcessI("python", properties, params, self.log)
        self.resources.add(process,"__del__")
        client.download(file, process.script_name)
        process.activate()

        prx = current.adapter.addWithUUID(process)
        return omero.grid.ProcessPrx.uncheckedCast(prx)

    def cleanup(self):
        """
        Cleanups all resoures created by this Processor, namely
        the Process instances.
        """
        while self.resources.cleanupNext():
            pass

class Server(Ice.Application):
    """
    Basic server implementation
    """
    def run(self,args):
        self.shutdownOnInterrupt()
        self.objectfactory = omero.ObjectFactory()
        self.objectfactory.registerObjectFactory(self.communicator())
        self.adapter = self.communicator().createObjectAdapter("ProcessorAdapter")
        self.p = ProcessorI(self.communicator().getLogger())
        self.p.serverid = self.communicator().getProperties().getProperty("Ice.ServerId")

        self.adapter.add(self.p, Ice.stringToIdentity("Processor"))
        self.adapter.activate()
        self.communicator().waitForShutdown()
        self.cleanup()

    def cleanup(self):
        """
        Cleans up all resources that were created by this server.
        Primarily the one ProcessorI instance.
        """
        if hasattr(self,"p"):
            try:
                self.p.cleanup()
            finally:
                self.p = None

if __name__ == "__main__":
    app=Server()
    sys.exit(app.main(sys.argv))


