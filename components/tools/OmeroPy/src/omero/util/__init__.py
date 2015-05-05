#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Utilities package
#
# Copyright 2009 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
import sys
import Ice
import path
import omero
import IcePy
import IceGrid
import logging
import platform
import Glacier2
import threading
import logging.handlers
import omero.util.concurrency
import uuid
import omero.ObjectFactoryRegistrar as ofr

from omero.util.decorators import locked

LOGDIR = os.path.join("var", "log")
LOGFORMAT = "%(asctime)s %(levelname)-5.5s [%(name)40s] " \
            "(%(threadName)-10s) %(message)s"
LOGLEVEL = logging.INFO
LOGSIZE = 500000000
LOGNUM = 9
LOGMODE = "a"

orig_stdout = sys.stdout
orig_stderr = sys.stderr


def make_logname(self):
    """
    Generates a logname from the given instance using the module
    and name from its class
    """
    log_name = "%s.%s" % (self.__class__.__module__, self.__class__.__name__)
    return log_name


def configure_logging(logdir=None, logfile=None, loglevel=LOGLEVEL,
                      format=LOGFORMAT, filemode=LOGMODE, maxBytes=LOGSIZE,
                      backupCount=LOGNUM, time_rollover=False):

    if logdir is None or logfile is None:
        handler = logging.StreamHandler()
    elif not time_rollover:
        handler = logging.handlers.RotatingFileHandler(
            os.path.join(logdir, logfile), maxBytes=maxBytes,
            backupCount=backupCount)
    else:
        handler = logging.handlers.TimedRotatingFileHandler(
            os.path.join(logdir, logfile), 'midnight', 1)
        # Windows will not allow renaming (or deleting) a file that's open.
        # There's nothing the logging package can do about that.
        try:
            sys.getwindowsversion()
        except:
            handler.doRollover()

    handler.setLevel(loglevel)
    formatter = logging.Formatter(format)
    handler.setFormatter(formatter)
    rootLogger = logging.getLogger()
    rootLogger.setLevel(loglevel)
    rootLogger.addHandler(handler)
    return rootLogger


def configure_server_logging(props):
    """
    Takes an Ice.Properties instance and configures logging
    """
    program_name = props.getProperty("Ice.Admin.ServerId")
    # Using Ice.ProgramName on Windows failed
    log_dir = props.getPropertyWithDefault("omero.logging.directory", LOGDIR)
    log_name = program_name + ".log"
    log_timed = props.getPropertyWithDefault(
        "omero.logging.timedlog", "False")[0] in ('T', 't')
    log_num = int(
        props.getPropertyWithDefault("omero.logging.lognum", str(LOGNUM)))
    log_size = int(
        props.getPropertyWithDefault("omero.logging.logsize", str(LOGSIZE)))
    log_num = int(
        props.getPropertyWithDefault("omero.logging.lognum", str(LOGNUM)))
    log_level = int(
        props.getPropertyWithDefault("omero.logging.level", str(LOGLEVEL)))
    configure_logging(log_dir, log_name, loglevel=log_level,
                      maxBytes=log_size, backupCount=log_num,
                      time_rollover=log_timed)

    sys.stdout = StreamRedirect(logging.getLogger("stdout"))
    sys.stderr = StreamRedirect(logging.getLogger("stderr"))


class StreamRedirect(object):

    """
    Since all server components should exclusively using the logging module
    any output to stdout or stderr is caught and logged at "WARN". This is
    useful, especially in the case of Windows, where stdout/stderr is eaten.
    """

    def __init__(self, logger):
        self.logger = logger
        self.internal = logging.getLogger("StreamRedirect")
        self.softspace = False

    def flush(self):
        pass

    def write(self, msg):
        msg = msg.strip()
        if msg:
            self.logger.warn(msg)

    def __getattr__(self, name):
        self.internal.warn("No attribute: %s" % name)


class Dependency(object):

    """
    Centralized logic for declaring and logging a service
    dependency on a non-shipped library. This is called
    lazily from the run method of the application to give
    logging time to be initialized.

    See #4566
    """

    def __init__(self, key):
        self.key = key

    def get_version(self, target):
        """
        Get version method which returns a string
        representing. Should be overwritten by
        subclasses for packages/modules with no
        __version__ field.
        """
        return target.__version__

    def check(self, logger):
        try:
            target = __import__(self.key)
            version = self.get_version(target)
            logger.info("Loaded dependency %s (%s)" % (self.key, version))
            return True
        except ImportError:
            logger.error("Failed to load: '%s'" % self.key)
            return False


def internal_service_factory(communicator, user="root", group=None, retries=6,
                             interval=10, client_uuid=None, stop_event=None):
    """
    Try to return a ServiceFactory from the grid.

    Try a number of times then give up and raise the
    last exception returned. This method will only
    work internally to the grid, i.e. behind the Glacier2
    firewall. It is intended for internal servers to
    be able to create sessions for accessing the database. ::

        communicator := Ice.Communicator used to find the registry
        user         := Username which should have a session created
        group        := Group into which the session should be logged
        retries      := Number of session creation retries before throwing
        interval     := Seconds between retries
        client_uuid  := Uuid of the client which should be used
    """
    log = logging.getLogger("omero.utils")
    if stop_event is None:
        stop_event = omero.util.concurrency.get_event(
            name="internal_service_factory")

    tryCount = 0
    excpt = None
    query = communicator.stringToProxy("IceGrid/Query")
    query = IceGrid.QueryPrx.checkedCast(query)

    implicit_ctx = communicator.getImplicitContext()
    implicit_ctx.put(omero.constants.AGENT, "Python service")
    if client_uuid is not None:
        implicit_ctx.put(omero.constants.CLIENTUUID, client_uuid)
    else:
        if not implicit_ctx.containsKey(omero.constants.CLIENTUUID):
            client_uuid = str(uuid.uuid4())
            implicit_ctx.put(omero.constants.CLIENTUUID, client_uuid)

    while tryCount < retries:
        if stop_event.isSet():  # Something is shutting down, exit.
            return None
        try:
            blitz = query.findAllObjectsByType("::Glacier2::SessionManager")[0]
            blitz = Glacier2.SessionManagerPrx.checkedCast(blitz)
            sf = blitz.create(user, None)
            # Group currently unused.
            return omero.api.ServiceFactoryPrx.checkedCast(sf)
        except Exception, e:
            tryCount += 1
            log.info("Failed to get session on attempt %s", str(tryCount))
            excpt = e
            stop_event.wait(interval)

    log.warn("Reason: %s", str(excpt))
    if excpt:
        raise excpt


def create_admin_session(communicator):
    """
    """
    reg = communicator.stringToProxy("IceGrid/Registry")
    reg = IceGrid.RegistryPrx.checkedCast(reg)
    adm = reg.createAdminSession('null', '')
    return adm


def add_grid_object(communicator, obj):
    """
    """
    communicator.identityToString(obj.ice_getIdentity())
    adm = create_admin_session(communicator)
    prx = adm.getAdmin()
    try:
        try:
            prx.addObject(obj)
        except IceGrid.ObjectExistsException:
            prx.updateObject(obj)
    finally:
        adm.destroy()


def long_to_path(id, root=""):
    """
    Converts a long to a path such that for all directiories only
    a 1000 files and a 1000 subdirectories will be returned.

    This method duplicates the logic in
    ome.io.nio.AbstractFileSystemService.java:getPath()
    """
    suffix = ""
    remaining = id
    dirno = 0

    if id is None or id == "":
        raise Exception("Expecting a not-null id.")

    id = long(id)

    if id < 0:
        raise Exception("Expecting a non-negative id.")

    while (remaining > 999):
        remaining /= 1000

        if remaining > 0:
            dirno = remaining % 1000
            suffix = os.path.join("Dir-%03d" % dirno, suffix)

    return os.path.join(root, "%s%s" % (suffix, id))


def load_dotted_class(dotted_class):
    """
    Load a Python class of the form "pkg.mod.Class"
    via __import__ and return it. No ctor or similar
    is called.
    """
    try:
        parts = dotted_class.split(".")
        pkg = ".".join(parts[0:-2])
        mod = str(parts[-2])
        kls = parts[-1]
        got = __import__(pkg, fromlist=[mod])
        got = getattr(got, mod)
        return getattr(got, kls)
    except Exception, e:
        raise Exception("""Failed to load: %s
        previous excetion: %s""" % (dotted_class, e))


class ServerContext(object):

    """
    Context passed to all servants.

    server_id, communicator, and stop_event will be
    constructed by the top-level Server instance.

    A context instance may also be configured to hold
    on to an internal session (ServiceFactoryPrx) and
    keep it alive.

    This instance obeys the Resources API and calls
    sf.keepAlive(None) on every check call, but does
    nothing on cleanup. The sf instance must be manually
    cleaned as the final operation of a servant.

    (Note: cleanup of the server context indicates
    server shutdown, so should be infrequent)
    """

    def __init__(self, server_id, communicator, stop_event,
                 on_newsession=None):
        self._lock = threading.RLock()
        self.logger = logging.getLogger("omero.util.ServerContext")
        self.server_id = server_id
        self.communicator = communicator
        self.stop_event = stop_event
        self.servant_map = dict()
        self.on_newsession = None

    @locked
    def add_servant(self, adapter_or_current, servant, ice_identity=None):
        oa = adapter_or_current
        if isinstance(adapter_or_current, (Ice.Current, IcePy.Current)):
            oa = oa.adapter
        if ice_identity is None:
            prx = oa.addWithUUID(servant)
        else:
            prx = oa.add(servant, ice_identity)

        servant.setProxy(prx)
        self.servant_map[prx] = servant
        return prx

    def newSession(self):
        self.session = internal_service_factory(
            self.communicator, stop_event=self.stop_event)
        if callable(self.on_newsession):
            self.on_newsession(self.session)

    def hasSession(self):
        return hasattr(self, "session")

    @locked
    def getSession(self, recreate=True):
        """
        Returns the ServiceFactoryPrx configured for the context if
        available. If the context was not configured for sessions,
        an ApiUsageException will be thrown: servants should know
        whether or not they were configured for sessions.
        See Servant(..., needs_session = True)

        Otherwise, if there is no ServiceFactoryPrx, an attempt will
        be made to create one if recreate == True. If the value is None
        or non can be recreated, an InternalException will be thrown.

        TODO : currently no arguments are provided for re-creating these,
        but also not in Servant.__init__
        """
        if not self.hasSession():
            raise omero.ApiUsageException(
                "Not configured for server connection")

        if self.session:
            try:
                self.session.keepAlive(None)
            except Ice.CommunicatorDestroyedException:
                self.session = None  # Ignore
            except Exception, e:
                self.logger.warn("Connection failure: %s" % e)
                self.session = None

        if self.session is None and recreate:
            try:
                self.newSession()
                self.logger.info("Established connection: %s" % self.session)
            except Exception, e:
                self.logger.warn("Failed to establish connection: %s" % e)

        if self.session is None:
            raise omero.InternalException("No connection to server")

        return self.session

    def check(self):
        """
        Calls getSession() but always returns True. This keeps the context
        available in the resources for later uses, and tries to re-establish
        a connection in case Blitz goes down.
        """
        try:
            self.getSession()
        except:
            pass
        return True

    def cleanup(self):
        """
        Does nothing. Context clean up must happen manually
        since later activities may want to reuse it. Servants using
        a server connection should cleanup the instance *after* Resources
        is cleaned up
        """
        pass


class Server(Ice.Application):

    """
    Basic server implementation which can be used for
    implementing a standalone python server which can
    be started from icegridnode.

    The servant implementation MUST have a constructor
    which takes a single ServerContext argument AND
    have a cleanup() method

    Logging is configured relative to the current directory
    to be in var/log by default.

    Usage::

        if __name__ == "__main__":
            app=Server(
                ServicesI, "ServicesAdapter", Ice.Identity("Services",""))
            sys.exit(app.main(sys.argv))

    app.impl now points to an instance of ServicesI

    """

    def __init__(self, impl_class, adapter_name, identity, logdir=LOGDIR,
                 dependencies=()):

        self.impl_class = impl_class
        self.adapter_name = adapter_name
        self.identity = identity
        self.logdir = logdir
        self.stop_event = omero.util.concurrency.get_event(name="Server")
        self.dependencies = dependencies

    def waitOnStartup(self):
        ms = 10000  # 10 seconds by default
        try:
            i = os.environ.get("OMERO_STARTUP_WAIT", "10000")
            ms = int(i)
        except:
            self.logger.debug(exc_info=1)

        try:
            self.logger.info("Waiting %s ms on startup" % ms)
            self.stop_event.wait(ms / 1000)
        except:
            self.logger.debug(exc_info=1)

    def run(self, args):

        from omero.rtypes import ObjectFactories as rFactories
        from omero.columns import ObjectFactories as cFactories

        props = self.communicator().getProperties()
        configure_server_logging(props)

        self.logger = logging.getLogger("omero.util.Server")
        self.logger.info("*" * 80)
        self.waitOnStartup()
        self.logger.info("Starting")

        failures = 0
        for x in self.dependencies:
            if not x.check(self.logger):
                failures += 1
        if failures:
            self.logger.error("Missing dependencies: %s" % failures)
            sys.exit(50)

        self.shutdownOnInterrupt()

        try:

            ofr.registerObjectFactory(self.communicator(), None)  # No client
            for of in rFactories.values() + cFactories.values():
                of.register(self.communicator())

            try:
                serverid = self.communicator().getProperties().getProperty(
                    "Ice.ServerId")
                ctx = ServerContext(
                    serverid, self.communicator(), self.stop_event)
                self.impl = self.impl_class(ctx)
                getattr(self.impl, "cleanup")  # Required per docs
            except:
                self.logger.error("Failed initialization", exc_info=1)
                sys.exit(100)

            try:
                self.adapter = self.communicator().createObjectAdapter(
                    self.adapter_name)
                self.adapter.activate()
                # calls setProxy
                ctx.add_servant(self.adapter, self.impl, self.identity)
                # ticket:1978 for non-collocated registries
                prx = self.adapter.createDirectProxy(self.identity)
                # This must happen _after_ activation
                add_grid_object(self.communicator(), prx)
            except:
                self.logger.error("Failed activation", exc_info=1)
                sys.exit(200)

            self.logger.info("Entering main loop")
            self.communicator().waitForShutdown()
        finally:
            self.stop_event.set()  # Let's all waits shutdown
            self.logger.info("Cleanup")
            self.cleanup()
            self.logger.info("Stopped")
            self.logger.info("*" * 80)

    def cleanup(self):
        """
        Cleans up all resources that were created by this server.
        Primarily the one servant instance.
        """
        if hasattr(self, "impl"):
            try:
                self.impl.cleanup()
            finally:
                del self.impl


class SimpleServant(object):

    """
    Base servant initialization. Doesn't create or try to cleanup
    a top-level Resources thread. This is useful for large numbers
    of servants. For servers and other singleton-like servants,
    see "Servant"
    """

    def __init__(self, ctx):
        self._lock = threading.RLock()
        self.prx = None  # Proxy which points to self
        self.ctx = ctx
        self.stop_event = ctx.stop_event
        self.communicator = ctx.communicator
        self.logger = logging.getLogger(make_logname(self))
        self.logger.debug("Created")

    def setProxy(self, prx):
        """
        Should be overwritten for post-initialization activities.
        The reason this method exists is that the implementation
        must be complete before registering it with the adapter.
        """
        self.prx = prx


class Servant(SimpleServant):

    """
    Abstract servant which can be used along with a slice2py
    generated dispatch class as the base type of high-level servants.
    These provide resource cleanup as per the omero.util.Server
    class.

    By passing "needs_session = True" to this constructor, an internal
    session will be created and stored in ServerContext as well as
    registered with self.resources
    """

    def __init__(self, ctx, needs_session=False):
        SimpleServant.__init__(self, ctx)
        self.resources = omero.util.Resources(
            sleeptime=60, stop_event=self.stop_event)
        if needs_session:
            self.ctx.newSession()
            self.resources.add(self.ctx)

    def cleanup(self):
        """
        Cleanups all resoures created by this servant. Calling
        cleanup multiple times should be safe.
        """
        resources = self.resources
        self.resources = None
        if resources is not None:
            self.logger.info("Cleaning up")
            resources.cleanup()
            self.logger.info("Done")
        if self.ctx.hasSession():
            try:
                sf = self.ctx.getSession(recreate=False)
                self.logger.debug("Destroying %s" % sf)
                sf.destroy()
            except:
                pass

    def __del__(self):
        self.cleanup()


class Resources:

    """
    Container class for storing resources which should be
    cleaned up on close and periodically checked. Use
    stop_event.set() to stop the internal thread.
    """

    def __init__(self, sleeptime=60, stop_event=None):
        """
        Add resources via add(object). They should have a no-arg cleanup()
        and a check() method.

        The check method will be called periodically (default: 60 seconds)
        on each resource. The cleanup method will be called on
        Resources.cleanup()
        """

        self.stuff = []
        self._lock = threading.RLock()
        self.logger = logging.getLogger("omero.util.Resources")
        self.stop_event = stop_event
        if not self.stop_event:
            self.stop_event = omero.util.concurrency.get_event(
                name="Resources")

        if sleeptime < 5:
            raise Exception(
                "Sleep time should be greater than 5: %s" % sleeptime)

        self.sleeptime = sleeptime

        class Task(threading.Thread):

            """
            Internal thread used for checking "stuff"
            """

            def run(self):
                ctx = self.ctx  # Outer class
                ctx.logger.info("Starting")
                while not ctx.stop_event.isSet():
                    try:
                        ctx.logger.debug("Executing")
                        copy = ctx.copyStuff()
                        remove = ctx.checkAll(copy)
                        ctx.removeAll(remove)
                    except:
                        ctx.logger.error(
                            "Exception during execution", exc_info=True)

                    ctx.logger.debug("Sleeping %s" % ctx.sleeptime)
                    # ticket:1531 - Attempting to catch threading issues
                    try:
                        ctx.stop_event.wait(ctx.sleeptime)
                    except ValueError:
                        pass

                if isinstance(ctx.stop_event,
                              omero.util.concurrency.AtExitEvent):
                    if ctx.stop_event.atexit:
                        return  # Skipping log. See #3260

                ctx.logger.info("Halted")

        self.thread = Task()
        self.thread.ctx = self
        self.thread.start()

    @locked
    def copyStuff(self):
        """
        Within a lock, copy the "stuff" list and reverse it.
        The list is reversed so that entries added
        later, which may depend on earlier added entries
        get a chance to be cleaned up first.
        """
        copy = list(self.stuff)
        copy.reverse()
        return copy

    # Not locked
    def checkAll(self, copy):
        """
        While stop_event is unset, go through the copy
        of stuff and call the check method on each
        entry. Any that throws an exception or returns
        a False value will be returned in the remove list.
        """
        remove = []
        for m in copy:
            if self.stop_event.isSet():
                return  # Let cleanup handle this
            self.logger.debug("Checking %s" % m[0])
            method = getattr(m[0], m[2])
            rv = None
            try:
                rv = method()
            except:
                self.logger.warn("Error from %s" % method, exc_info=True)
            if not rv:
                remove.append(m)
        return remove

    @locked
    def removeAll(self, remove):
        """
        Finally, within another lock, call the "cleanup"
        method on all the entries in remove, and remove
        them from the official stuff list. (If stop_event
        is set during execution, we return with the assumption
        that Resources.cleanup() will take care of them)
        """
        for r in remove:
            if self.stop_event.isSet():
                return  # Let cleanup handle this
            self.logger.debug("Removing %s" % r[0])
            self.safeClean(r)
            self.stuff.remove(r)

    @locked
    def add(self, object, cleanupMethod="cleanup", checkMethod="check"):
        entry = (object, cleanupMethod, checkMethod)
        self.logger.debug("Adding object %s" % object)
        self.stuff.append(entry)

    @locked
    def cleanup(self):
        self.stop_event.set()
        for m in self.stuff:
            self.safeClean(m)
        self.stuff = None
        self.logger.debug("Cleanup done")

    def safeClean(self, m):
        try:
            self.logger.debug("Cleaning %s" % m[0])
            method = getattr(m[0], m[1])
            method()
        except:
            self.logger.error("Error cleaning resource: %s" % m[0], exc_info=1)

    def __del__(self):
        self.cleanup()


class Environment:

    """
    Simple class for creating an executable environment
    """

    def __init__(self, *args):
        """
        Takes an number of environment variable names which
        should be copied to the target environment if present
        in the current execution environment.
        """
        if sys.platform == "win32":
            # Prevents SocketException. See ticket:1518
            self.env = os.environ.copy()
        else:
            self.env = {}
        for arg in args:
            if arg in os.environ:
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
        if key in self.env.keys():
            self.env[key] = os.pathsep.join([self.env[key], addition])
        else:
            self.set(key, addition)

#
# Miscellaneious utilities
#


def get_user(default=None):
    """
    Returns the username. For most purposes, this value
    will be the same as getpass.getuser on \*nix and
    win32api.GetUserName on Windows, but in some situations
    (when running without a terminal, etc) getuser may throw
    a KeyError. In which case, or if the username resolves to
    False, the default value be returned.

    Any unexpected exceptions will be thrown.

    See ticket:6307
    """
    rv = None
    try:
        import getpass
        rv = getpass.getuser()  # Uses environment variable or pwd
    except KeyError:  # 6307, probably system
        pass
    except ImportError:  # No pwd on Windows
        import win32api
        rv = win32api.GetUserName()

    if not rv:
        return default
    else:
        return rv


def get_omero_userdir():
    """Returns the OMERO user directory"""
    omero_userdir = os.environ.get('OMERO_USERDIR', None)
    if omero_userdir:
        return path.path(omero_userdir)
    else:
        return path.path(get_user_dir()) / "omero"


def get_user_dir():
    exceptions_to_handle = (ImportError)
    try:
        from pywintypes import com_error
        from win32com.shell import shellcon, shell
        exceptions_to_handle = (ImportError, com_error)
        homeprop = shell.SHGetFolderPath(0, shellcon.CSIDL_APPDATA, 0, 0)
    except exceptions_to_handle:
        homeprop = os.path.expanduser("~")

    if "~" == homeprop:
        # ticket:5583
        raise Exception("Unexpanded '~' from expanduser: see ticket:5583")

    return homeprop


def edit_path(path_or_obj, start_text):
    f = path.path(path_or_obj)
    editor = os.getenv("VISUAL") or os.getenv("EDITOR")
    if not editor:
        if platform.system() == "Windows":
            editor = "Notepad.exe"
        else:
            editor = "vi"
    f.write_text(start_text)

    # If absolute, then use the path
    # as is (ticket:4246). Otherwise,
    # use which.py to find it.
    editor_obj = path.path(editor)
    if editor_obj.isabs():
        editor_path = editor
    else:
        from omero_ext.which import which
        editor_path = which(editor)

    pid = os.spawnl(os.P_WAIT, editor_path, editor_path, f)
    if pid:
        re = RuntimeError("Couldn't spawn editor: %s" % editor)
        re.pid = pid
        raise re

# From: http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/157035


def tail_lines(filename, linesback=10, returnlist=0):
    """Does what "tail -10 filename" would have done
       Parameters:

            filename   file to read
            linesback  Number of lines to read from end of file
            returnlist Return a list containing the lines instead of a string

    """
    avgcharsperline = 75

    file = open(filename, 'r')
    while 1:
        try:
            file.seek(-1 * avgcharsperline * linesback, 2)
        except IOError:
            file.seek(0)
        if file.tell() == 0:
            atstart = 1
        else:
            atstart = 0

        lines = file.read().split("\n")
        if (len(lines) > (linesback + 1)) or atstart:
            break
        # The lines are bigger than we thought
        avgcharsperline = avgcharsperline * 1.3  # Inc avg for retry
    file.close()

    if len(lines) > linesback:
        start = len(lines) - linesback - 1
    else:
        start = 0
    if returnlist:
        return lines[start:len(lines) - 1]

    out = ""
    for l in lines[start:len(lines) - 1]:
        out = out + l + "\n"
    return out
