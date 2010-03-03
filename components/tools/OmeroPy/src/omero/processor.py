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
import traceback
import exceptions
import killableprocess as subprocess

from path import path

import Ice
import omero
import omero.clients
import omero.util
import omero.util.concurrency

from omero.util.temp_files import create_path, remove_path
from omero.util.decorators import remoted, perf, locked
from omero.rtypes import *
from omero.util.decorators import remoted, perf


class ProcessI(omero.grid.Process, omero.util.SimpleServant):
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

    If iskill is True, then on cleanup, this process will reap the
    attached session completely.
    """

    def __init__(self, ctx, interpreter, properties, params, iskill = False,\
        Popen = subprocess.Popen, callback_cast = omero.grid.ProcessCallbackPrx.uncheckedCast):
        """
        Popen and callback_Cast are primarily for testing.
        """
        omero.util.SimpleServant.__init__(self, ctx)
        self.interpreter = interpreter        #: Executable which will be used on the script
        self.properties = properties          #: Properties used to create an Ice.Config
        self.params = params                  #: JobParams for this script. Possibly None
        self.iskill = iskill                  #: Whether or not, cleanup should kill the session
        self.Popen = Popen                    #: Function which should be used for creating processes
        self.callback_cast = callback_cast    #: Function used to cast all ProcessCallback proxies
        # Non arguments (mutable state)
        self.rcode = None                     #: return code from popen
        self.callbacks = {}                   #: dictionary from id strings to callback proxies
        self.popen = None                     #: process. if None, then this instance isn't alive.
        self.pid = None                       #: pid of the process. Once set, isn't nulled.
        self.started = None                   #: time the process started
        self.stopped = None                   #: time of deactivation
        # Non arguments (immutable state)
        self.uuid = properties["omero.user"]  #: session this instance is tied to

        # More fields set by these methods
        self.make_files()
        self.make_env()
        self.make_config()
        self.logger.info("Created %s in %s" % (self.uuid, self.dir))

    #
    # Initialization methods
    #

    def make_env(self):
        self.env = omero.util.Environment("PATH", "PYTHONPATH",\
            "DYLD_LIBRARY_PATH", "LD_LIBRARY_PATH", "MLABRAW_CMD_STR", "HOME")
        # WORKAROUND
        # Currently duplicating the logic here as in the PYTHONPATH
        # setting of the grid application descriptor (see etc/grid/*.xml)
        # This should actually be taken care of in the descriptor itself
        # by having setting PYTHONPATH to an absolute value. This is
        # not currently possible with IceGrid (without using icepatch --
        # see 39.17.2 "node.datadir).
        self.env.append("PYTHONPATH", str(path.getcwd() / "lib" / "python"))
        self.env.set("ICE_CONFIG", str(self.config_path))

    def make_files(self):
        self.dir = create_path("process", ".dir", folder = True)
        self.script_path = self.dir / "script"
        self.config_path = self.dir / "config"
        self.stdout_path = self.dir / "out"
        self.stderr_path = self.dir / "err"

    def make_config(self):
        """
        Creates the ICE_CONFIG file used by the client.
        """
        config_file = open(str(self.config_path), "w")
        try:
            for key in self.properties.iterkeys():
                config_file.write("%s=%s\n"%(key, self.properties[key]))
        finally:
            config_file.close()

    def tmp_client(self):
        """
        Create a client for performing cleanup operations.
        This client should be closed as soon as possible
        by the process
        """
        try:
            client = omero.client(["--Ice.Config=%s" % str(self.config_path)])
            client.createSession().detachOnDestroy()
            self.logger.debug("client: %s" % client.sf)
            return client
        except:
            self.logger.error("Failed to create client for %s" % self.uuid)
            return None

    #
    # Activation / Deactivation
    #

    @locked
    def activate(self):
        """
        Process creation has to wait until all external downloads, etc
        are finished.
        """

        if self.isActive():
            raise omero.ApiUsageException(None, None, "Already activated")

        self.stdout = open(str(self.stdout_path), "w")
        self.stderr = open(str(self.stderr_path), "w")
        self.popen = self.Popen([self.interpreter, "./script"], cwd=str(self.dir), env=self.env(), stdout=self.stdout, stderr=self.stderr)
        self.pid = self.popen.pid
        self.started = time.time()
        self.stopped = None
        self.status("Activated")

    @locked
    def deactivate(self):
        """
        Cleans up the temporary directory used by the process, and terminates
        the Popen process if running.
        """

        if not self.isActive():
            raise omero.ApiUsageException(None, None, "Not active")

        if self.stopped:
            # Prevent recursion since we are reusing kill & cancel
            return

        self.stopped = time.time()
        d_start = time.time()
        self.status("Deactivating")

        # None of these should throw, but just in case
        try:

            self.shutdown()     # Calls cancel & kill which recall this method!
            self.popen = None   # Now we are finished

            self.cleanup_output()
            self.upload_output() # Important!
            self.cleanup_tmpdir()

        except exceptions.Exception:
            self.logger.error("FAILED TO CLEANUP pid=%s (%s)", self.pid, self.uuid, exc_info = True)

        d_stop = time.time()
        elapsed = int(self.stopped - self.started)
        d_elapsed = int(d_stop - d_start)
        self.status("Lived %ss. Deactivation took %ss." % (elapsed, d_elapsed))

    @locked
    def isActive(self):
        """
        Tests only if this instance has a non-None popen attribute. After activation
        this method will return True until the popen itself returns a non-None
        value (self.rcode) at which time it will be nulled and this method will again
        return False
        """
        return self.popen is not None

    @locked
    def wasActivated(self):
        """
        Returns true only if this instance has either a non-null
        popen or a non-null rcode field.
        """
        return self.popen is not None or self.rcode is not None

    @locked
    def isRunning(self):
        return self.popen is not None and self.rcode is None

    @locked
    def isFinished(self):
        return self.rcode is not None

    @locked
    def alreadyDone(self):
        """
        Allows short-cutting various checks if we already
        have a rcode for this popen. A non-None return value
        implies that a process was started and returned
        the given non-None value itself.
        """
        if not self.wasActivated:
            raise omero.InternalException(None, None, "Process never activated")
        return self.isFinished()

    #
    # Cleanup methods
    #

    def __del__(self):
        self.cleanup()

    @perf
    @locked
    def check(self):
        """
        Called periodically to keep the session alive. Returns
        False if this resource can be cleaned up. (Resources API)
        """

        if not self.wasActivated():
            return True # This should only happen on startup, so ignore

        try:
            self.poll()
            self.ctx.getSession().getSessionService().getSession(self.uuid)
            return True
        except:
            self.status("Keep alive failed")
            return False

    @perf
    @locked
    def cleanup(self):
        """
        Deactivates the process (if active) and cleanups the server
        connection. (Resources API)
        """

        if self.isRunning():
            self.deactivate()

        try:
            sf = self.ctx.getSession(recreate = False)
        except:
            self.logger.warn("Can't get session for cleanup")
            return

        self.status("Cleaning")
        svc = sf.getSessionService()
        obj = omero.model.SessionI()
        obj.uuid = omero.rtypes.rstring(self.uuid)
        try:
            if self.iskill:
                self.status("Killing session")
                while svc.closeSession(obj) > 0:
                    pass
            # No action to be taken when iskill == False if
            # we don't have an actual client to worry with.
        except:
            self.logger.error("Error on session cleanup, kill=%s" % self.iskill, exc_info = True)

    def cleanup_output(self):
        """
        Flush and close the stderr and stdout streams.
        """
        try:
            if hasattr(self, "stderr"):
                self.stderr.flush()
                self.stderr.close()
        except:
            self.logger.error("cleanup of sterr failed", exc_info = True)
        try:
            if hasattr(self, "stdout"):
                self.stdout.flush()
                self.stdout.close()
        except:
            self.logger.error("cleanup of sterr failed", exc_info = True)

    def upload_output(self):
        """
        If this is not a params calculation (i.e. parms != null) and the
        stdout or stderr are non-null, they they will be uploaded and
        attached to the job.
        """
        client = self.tmp_client()
        if not client:
            self.logger.error("No client: Cannot upload output for pid=%s (%s)", self.pid, self.uuid)
            return

        try:
            if self.params:
                out_format = self.params.stdoutFormat
                err_format = self.params.stderrFormat
                upload = True
            else:
                out_format = "text/plain"
                err_format = out_format
                upload = False

            self._upload(upload, client, self.stdout_path, "stdout", out_format)
            self._upload(upload, client, self.stderr_path, "stderr", err_format)
        finally:
                client.__del__() # Safe closeSession

    def _upload(self, upload, client, filename, name, format):

        if not upload or not format:
            return

        filename = str(filename) # Might be path.path
        sz = os.path.getsize(filename)
        if not sz:
            self.logger.info("No %s" % name)
            return

        try:
            ofile = client.upload(filename, name=name, type=format)
            jobid = long(client.getProperty("omero.job"))
            link = omero.model.JobOriginalFileLinkI()
            link.parent = omero.model.ScriptJobI(rlong(jobid), False)
            link.child = ofile
            client.getSession().getUpdateService().saveObject(link)
            self.status("Uploaded %s bytes of %s to %s" % (sz, filename, ofile.id.val))
        except:
            self.logger.error("Error on upload of %s for pid=%s (%s)", filename, self.pid, self.uuid, exc_info = True)

    def cleanup_tmpdir(self):
        """
        Remove all known files and finally the temporary directory.
        If other files exist, an exception will be raised.
        """
        try:
            remove_path(self.dir)
        except:
            self.logger.error("Failed to remove dir %s" % self.dir, exc_info = True)

    #
    # popen methods
    #

    def status(self, msg = ""):
        if self.isRunning():
            self.rcode = self.popen.poll()
        self.logger.info("%s : %s", self, msg)

    @perf
    @remoted
    def poll(self, current = None):
        """
        Checks popen.poll() (if active) and notifies all callbacks
        if necessary. If this method returns a non-None value, then
        the process will be marked inactive.
        """

        if self.alreadyDone():
            return rint(self.rcode)

        self.status("Polling")
        if self.rcode is None:
            # Haven't finished yet, so do nothing.
            return None
        else:
            self.deactivate()
            rv = rint(self.rcode)
            self.allcallbacks("processFinished", rv)
            return rv

    @perf
    @remoted
    def wait(self, current = None):
        """
        Waits on popen.wait() to return (if active) and notifies
        all callbacks. Marks this process as inactive.
        """

        if self.alreadyDone():
            return self.rcode

        self.status("Waiting")
        self.rcode = self.popen.wait()
        self.deactivate()
        self.allcallbacks("processFinished", self.rcode)
        return self.rcode

    def _term(self):
        """
        Attempts to cancel the process by sending SIGTERM
        (or similar)
        """
        try:
            self.status("os.kill(TERM)")
            os.kill(self.popen.pid, signal.SIGTERM)
        except AttributeError:
            self.logger.debug("No os.kill(TERM). Skipping cancel")

    def _send(self, iskill):
        """
        Helper method for sending signals. This method only
        makes a call is the process is active.
        """
        if self.isRunning():
            try:
                if self.popen.poll() is None:
                    if iskill:
                        self.status("popen.kill(True)")
                        self.popen.kill(True)
                    else:
                        self._term()

                else:
                    self.status("Skipped signal")
            except OSError, oserr:
                self.logger.debug("err on pid=%s iskill=%s : %s", self.popen.pid, iskill, oserr)

    @perf
    @remoted
    def cancel(self, current = None):
        """
        Tries to cancel popen (if active) and notifies callbacks.
        """

        if self.alreadyDone():
            return True

        self._send(iskill=False)
        finished = self.isFinished()
        if finished:
            self.deactivate()
        self.allcallbacks("processCancelled", finished)
        return finished

    @perf
    @remoted
    def kill(self, current = None):

        if self.alreadyDone():
            return True

        self._send(iskill=True)
        finished = self.isFinished()
        if finished:
            self.deactivate()
        self.allcallbacks("processKilled", finished)
        return finished

    @perf
    @remoted
    def shutdown(self, current = None):
        """
        If self.popen is active, then first call cancel, wait a period of
        time, and finally call kill.
        """

        if self.alreadyDone():
            return

        self.status("Shutdown")
        try:
            for i in range(5, 0, -1):
                if self.cancel():
                    break
                else:
                    self.logger.warning("Shutdown: %s (%s). Killing in %s seconds.", self.pid, self.uuid, 6*(i-1)+1)
                    self.stop_event.wait(6)
            self.kill()
        except:
            self.logger.error("Shutdown failed: %s (%s)", self.pid, self.uuid, exc_info = True)

    #
    # Callbacks
    #

    @remoted
    @locked
    def registerCallback(self, callback, current = None):
        try:
            id = callback.ice_getIdentity()
            key = "%s/%s" % (id.category, id.name)
            callback = callback.ice_oneway()
            callback = self.callback_cast(callback)
            if not callback:
                e = "Callback is invalid"
            else:
                self.callbacks[key] = callback
                self.logger.debug("Added callback: %s", key)
                return
        except exceptions.Exception, ex:
            e = ex
        # Only reached on failure
        msg = "Failed to add callback: %s. Reason: %s" % (callback, e)
        self.logger.debug(msg)
        raise omero.ApiUsageException(None, None, msg)

    @remoted
    @locked
    def unregisterCallback(self, callback, current = None):
        try:
            id = callback.ice_getIdentity()
            key = "%s/%s" % (id.category, id.name)
            if not key in self.callback:
                raise omero.ApiUsageException(None, None, "No callback registered with id: %s" % key)
            del self.callbacks[key]
            self.logger.debug("Removed callback: %s", key)
        except exceptions.Exception, e:
            msg = "Failed to remove callback: %s. Reason: %s" % (callback, e)
            self.logger.debug(msg)
            raise omero.ApiUsageException(None, None, msg)

    @locked
    def allcallbacks(self, method, arg):
        self.status("Callback %s" % method)
        for key, cb in self.callbacks.items():
            try:
                m = getattr(cb, method)
                m(arg)
            except:
                self.logger.error("Error calling callback %s on pid=%s (%s)" % (key, self.pid, self.uuid), exc_info = True)

    def __str__(self):
        return "<proc:%s,rc=%s,uuid=%s>" % (self.pid, self.rcode, self.uuid)

class UseSessionHolder(object):

    def __init__(self, sf):
        self.sf = sf

    def check(self):
        try:
            self.sf.keepAlive(None)
            return True
        except:
            return False

    def cleanup(self):
        pass

class ProcessorI(omero.grid.Processor, omero.util.Servant):

    def __init__(self, ctx, needs_session = True,
                 use_session = None, accepts_list = []):

        if use_session:
            needs_session = False # See discussion below

        omero.util.Servant.__init__(self, ctx, needs_session = needs_session)
        self.cfg = os.path.join(os.curdir, "etc", "ice.config")
        self.cfg = os.path.abspath(self.cfg)

        # Extensions for user-mode processors (ticket:1672)

        self.use_session = use_session
        """
        If set, this session will be returned from internal_session and
        the "needs_session" setting ignored.
        """

        self.accepts_list = accepts_list
        """
        A list of contexts which will be accepted by this user-mode
        processor.
        """

        # Keep this session alive until the processor is finished
        self.resources.add( UseSessionHolder(use_session) )

    def setProxy(self, prx):
        """
        Overrides the default action in order to register this proxy
        with the session's sharedResources to register for callbacks.
        """
        prx = omero.grid.ProcessorPrx.uncheckedCast(prx)
        omero.util.Servant.setProxy(self, prx)
        import omero_SharedResources_ice
        self.logger.info("Registering processor %s", self.prx)
        self.internal_session().sharedResources().addProcessor(self.prx)

    def internal_session(self):
        """
        Returns the session which should be used for lookups by this instance.
        Some methods will create a session based on the session parameter.
        In these cases, the session will belong to the user who is running a
        script.
        """
        if self.use_session:
            return self.use_session
        else:
            return self.ctx.getSession()

    def lookup(self, job):

        ctx = {"omero.group": str(job.details.group.id.val)}
        sf = self.internal_session()
        handle = sf.createJobHandle()
        handle.attach(job.id.val, ctx)
        if handle.jobFinished(ctx):
            raise omero.ApiUsageException("Job already finished.")

        if not self.accepts_list:
             accepts = "                and o.details.owner.id = 0"
        else:
            accepts = ""
            for a in self.accepts_list:
                if isinstance(a, omero.model.Experimenter):
                    accepts += ("            and o.details.owner.id = %s" % a.id.val)

        file = sf.getQueryService().findByQuery(\
            """select o from Job j
             join j.originalFileLinks links
             join links.child o
             join o.format
             where
                 j.id = %d
             %s
             and o.format.value = 'text/x-python'
             """ % (job.id.val, accepts), None, ctx)

        if not file:
            raise omero.ApiUsageException(\
                None, None, "Job should have one executable file attached.")

        return file

    @remoted
    def accepts(self, userContext, groupContext, scriptContext, cb, current = None):
        file = None
        try:
            self.logger.debug("Accepts called on: user:%s group:%s scriptjob:%s",
                userContext.id.val, groupContext.id.val, scriptContext.id.val)
            file = self.lookup(scriptContext)
        except:
            self.logger.debug("File lookup failed: %s", scriptContext, exc_info=1)
            file = None

        if file:
            try:
                #cb = cb.ice_oneway()
                cb = omero.grid.ProcessorAcceptsCallbackPrx.uncheckedCast(cb)
                print "CALLBACK:",cb
                cb.accepted(self.internal_session().ice_getIdentity().name,
                            str(self.prx))
            except:
                self.logger.warn("Accept callback failed", exc_info=1)

    @remoted
    def parseJob(self, session, job, current = None):

        client = omero.client(["--Ice.Config=%s" % (self.cfg)])
        try:
            client.joinSession(session).detachOnDestroy()
            return self.parse(client, session, job, current, iskill = False)
        finally:
            client.closeSession()
            del client

    @remoted
    def processJob(self, session, job, current = None):
        """
        """
        client = omero.client(["--Ice.Config=%s" % (self.cfg)])
        try:
            client.joinSession(session).detachOnDestroy()
            params = self.parse(client, session, job, current, iskill = False)
            return self.process(client, session, job, current, params, iskill = True)
        finally:
            client.closeSession()
            del client

    @perf
    def parse(self, client, session, job, current, iskill):

        self.logger.info("parseJob: Session = %s, JobId = %s" % (session, job.id.val))
        properties = {}
        properties["omero.scripts.parse"] = "true"
        process = self.process(client, session, job, current, None, properties, iskill)
        process.wait()
        rv = client.getOutput("omero.scripts.parse")
        if rv != None:
            return rv.val
        else:
            self.logger.warning("No output found for omero.scripts.parse. Keys: %s" % client.getOutputKeys())
            return None

    @perf
    def process(self, client, session, job, current, params, properties = {}, iskill = True):
        """
        session: session uuid, used primarily if client is None
        client: an omero.client object which should be attached to a session
        """

        if not session or not job or not job.id:
            raise omero.ApiUsageException("No null arguments")

        file = self.lookup(job)

        properties["omero.job"] = str(job.id.val)
        properties["omero.user"] = session
        properties["omero.pass"] = session
        properties["Ice.Default.Router"] = client.getProperty("Ice.Default.Router")

        self.logger.info("processJob: Session = %s, JobId = %s" % (session, job.id.val))
        process = ProcessI(self.ctx, "python", properties, params, iskill)
        self.resources.add(process)
        client.download(file, str(process.script_path))
        self.logger.info("Downloaded file: %s" % file.id.val)
        s = client.sha1(str(process.script_path))
        if not s == file.sha1.val:
            msg = "Sha1s don't match! expected %s, found %s" % (file.sha1.val, s)
            self.logger.error(msg)
            process.cleanup()
            raise omero.InternalException(None, None, msg)
        else:
            process.activate()

        prx = current.adapter.addWithUUID(process)
        process.setProxy( prx )
        return omero.grid.ProcessPrx.uncheckedCast(prx)
