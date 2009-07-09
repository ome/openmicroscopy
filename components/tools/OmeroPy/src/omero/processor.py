#!/usr/bin/env python
#
# OMERO Grid Processor
# Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
import sys
import time
import signal
import logging
import tempfile
import threading
import traceback
import subprocess
import exceptions
from path import path

import omero, Ice
import omero_api_Tables_ice
from omero.rtypes import *

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

    If kill is True, then on cleanup, this process will reap the
    attached session completely.
    """

    def __init__(self, interpreter, properties, params, kill = False):
        self.active = False
        self.dead = False
        self.interpreter = interpreter
        self.properties = properties
        self.params = params
        self.kill = kill
        # Non arguments
        self.callbacks = []
        self.lock = threading.Lock()
        self.dir = tempfile.mkdtemp()
        self.script_name = os.path.join(self.dir, "script")
        self.config_name = os.path.join(self.dir, "config")
        self.stdout_name = os.path.join(self.dir, "out")
        self.stderr_name = os.path.join(self.dir, "err")
        self.env = omero.util.Environment("PATH","PYTHONPATH","DYLD_LIBRARY_PATH","LD_LIBRARY_PATH","MLABRAW_CMD_STR")
        # WORKAROUND
        # Currently duplicating the logic here as in the PYTHONPATH
        # setting of the grid application descriptor (see etc/grid/*.xml)
        # This should actually be taken care of in the descriptor itself
        # by having setting PYTHONPATH to an absolute value. This is
        # not currently possible with IceGrid (without using icepatch --
        # see 39.17.2 "node.datadir).
        self.env.append("PYTHONPATH", str(path().getcwd() / "lib" / "python"))
        self.env.set("ICE_CONFIG", self.config_name)
        self.make_config()
        #
        # Finally we create a client which will be kept alive by
        # the processors omero.util.Resources() instance. On cleanup(),
        # an attempt will be made to completely close the session.
        #
        self.client = omero.client(["--Ice.Config=%s" % self.config_name])
        self.client.createSession().closeOnDestroy()
        self.uuid = self.client.sf.ice_getIdentity().name
        self.logger = logging.getLogger("Process")
        self.logger.info("Created " + self.uuid)

    def activate(self):
        """
        Process creation has to wait until all external downloads, etc
        are finished.
        """
        self.stdout = open(self.stdout_name, "w")
        self.stderr = open(self.stderr_name, "w")
        self.popen = subprocess.Popen([self.interpreter, "./script"], cwd=self.dir, env=self.env(), stdout=self.stdout, stderr=self.stderr)
        self.active = True
        self.logger.info("Activated " + self.uuid)

    def check(self):
        """
        Called periodically to keep the session alive. Returns
        False if this resource can be cleaned up. (Resources API)
        """
        if not self.client:
            self.logger.warning("No client for " + self.uuid)
            return False
        try:
            self.logger.debug("Checking " + self.uuid)
            self.client.sf.keepAlive(None)
            return True
        except:
            self.logger.error("Keep alive failed for %s" % self.uuid)
            self.cleanup_session()
            return False

    def cleanup(self):
        """
        Cleans up the temporary directory used by the process, and terminates
        the Popen process if running. (Resources API)
        """
        try:
            self.lock.acquire()
            if not self.dead:
                try:
                    self.logger.info("Checking " + self.uuid)
                    self.cleanup_popen()
                    self.cleanup_output()
                    self.upload_output() # Important
                    self.cleanup_tmpdir()
                    if self.kill:
                        self.cleanup_session()
                except exceptions.Exception:
                    self.logger.error("FAILED TO CLEANUP %s" % self.uuid, exc_info = True)
            else:
                self.logger.info("Already dead - " + self.uuid)
        finally:
            self.dead = True
            self.lock.release()

    def __del__(self):
        self.cleanup()

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
                    self.logger.warning("Process %s terminated cleanly." % str(self.popen.pid))
                    break
                else:
                    self.logger.warning("%s still active. Killing in %s seconds." % (str(self.popen.pid),6*(i-1)+1))
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
        if not self.client:
            self.logger.error("No client: Cannot upload output for " + self.uuid)
            return

        if self.params:
            out_format = self.params.stdoutFormat
            err_format = self.params.stderrFormat
            upload = True
        else:
            out_format = "text/plain"
            err_format = out_format
            upload = False

        if upload:
            self._upload(self.client, self.stdout_name, "stdout", out_format)
            self._upload(self.client, self.stderr_name, "stderr", err_format)
            self.logger.info("Uploaded output " + self.uuid)

    def _upload(self, client, filename, name, format):
        if format:
            if os.path.getsize(filename):
                ofile = client.upload(filename, name=name, type=format)
                jobid = long(client.getProperty("omero.job"))
                link = omero.model.JobOriginalFileLinkI()
                link.parent = omero.model.ScriptJobI(rlong(jobid), False)
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

    def cleanup_session(self):
        """
        Closes and nulls the self.client field.
        """
        self.logger.warning("SHOULD BE killing session " + self.uuid)
        #self.client.killSession()
        self.client.closeSession()
        self.client = None

    def make_config(self):
        """
        Creates the ICE_CONFIG file used by the client.
        """
        config_file = open(self.config_name, "w")
        try:
            for key in self.properties.iterkeys():
                config_file.write("%s=%s\n"%(key, self.properties[key]))
        finally:
            config_file.close()

    def poll(self, current = None):
        rv = self.popen.poll()
        if None == rv:
            self.logger.info("Poll is null " + self.uuid)
            return None
        rv = rint(rv)
        self.allcallbacks("processFinished", rv)
        self.__del__()
        return rv

    def wait(self, current = None):
        self.logger.info("Wait on " + self.uuid)
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
            self.logger.info("Callback %s for %s" % (method, self.uuid))
            for cb in self.callbacks:
                try:
                    m = getattr(cb,method)
                    m(arg)
                except:
                    self.logger.error("Error calling callback %s on %s" % (cb, self.uuid))
        finally:
            self.lock.release()

    def __str__(self):
        if hasattr(self, "uuid"):
            return "Process-%s" % self.uuid
        else:
            return "Process-uninitialized"

class ProcessorI(omero.grid.Processor):

    def __init__(self):
        self.cfg = os.path.join(os.curdir, "etc", "ice.config")
        self.cfg = os.path.abspath(self.cfg)
        self.resources = omero.util.Resources()
        self.logger = logging.getLogger("ProcessorI")
        self.logger.info("Initialized")

    def parseJob(self, session, job, current = None):
        return self.parse(session, job, current, kill = True)

    def parse(self, session, job, current, kill):
        if not session or not job or not job.id:
            raise omero.ApiUsageException("No null arguments")

        self.logger.info("parseJob: Session = %s, JobId = %s" % (session, job.id.val))
        properties = {}
        properties["omero.scripts.parse"] = "true"
        # We need to use the client twice, so initializing here.
        client = omero.client(["--Ice.Config=%s" % (self.cfg)])
        client.joinSession(session).detachOnDestroy()
        process = self.process(session, job, current, None, properties, client, kill)
        process.wait()
        rv = client.getOutput("omero.scripts.parse")
        if rv != None:
            return rv.val
        else:
            self.logger.warning("No output found for omero.scripts.parse. Keys: %s" % client.getOutputKeys())
            return None

    def processJob(self, session, job, current = None):
        """
        """
        params = self.parse(session, job, current, kill = False)
        return self.process(session, job, current, params, kill = True)

    def process(self, session, job, current, params, properties = {}, client = None, kill = True):
        """
        session: session uuid, used primarily if client is None
        client: an optional omero.client object which should be attached to a session
        """
        if not client:
            client = omero.client(["--Ice.Config=%s" % (self.cfg)])
            client.joinSession(session).detachOnDestroy()

        sf = client.getSession()
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

        process = ProcessI("python", properties, params, kill)
        self.resources.add(process)
        client.download(file, process.script_name)
        self.logger.info("Downloaded file: %s" % file.id.val)
        process.activate()

        prx = current.adapter.addWithUUID(process)
        return omero.grid.ProcessPrx.uncheckedCast(prx)

    def cleanup(self):
        """
        Cleanups all resoures created by this Processor, namely
        the Process instances.
        """
        self.logger.info("Cleaning up")
        self.resources.cleanup()

if __name__ == "__main__":
    app = omero.util.Server(ProcessorI, "ProcessorAdapter", Ice.Identity("Processor",""))
    sys.exit(app.main(sys.argv))
