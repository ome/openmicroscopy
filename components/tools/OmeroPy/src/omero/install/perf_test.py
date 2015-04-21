#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Add: screen/plate
# Add: plotting
#

import re
import os
import sys
import path
import time
import omero
import logging

import omero.cli
import omero.util
import omero.util.temp_files
import uuid

command_pattern = "^\s*(\w+)(\((.*)\))?(:(.*))?$"
command_pattern_compiled = re.compile(command_pattern)
log = logging.getLogger("omero.perf")

FILE_FORMAT = """
File format:
    <blank>                                     Ignored
    # comment                                   Ignored
    ServerTime(repeat=100)                      Retrieve the server time 100 \
times
    Import:<file>                               Import given file
    Import(Screen:<id>):<file>                  Import given file into screen
    Import(Dataset:<id>):<file>                 Import given file into \
project/dataset
    Import(Project:<id>,Dataset:<id>):<file>    Import given file into \
project/dataset
    Import(Dataset:some name):<file>            Import given file into a new \
dataset
    Import(Dataset):<file>                      Import given file into last \
created dataset (or create a new one)

    #
    # "Import" is the name of a command available in the current context
    # Use the "--list" command to print them all. All lines must be of the
    # form: %s

""" % command_pattern


#
# Main classes
#

class ItemException(Exception):
    pass


class BadCommand(ItemException):
    pass


class BadLine(ItemException):
    pass


class BadPath(ItemException):
    pass


class BadImport(ItemException):
    pass


class Item(object):
    """
    Single line-item in the configuration file
    """

    def __init__(self, line):
        self.line = line.strip()
        if not self.comment():
            match = command_pattern_compiled.match(self.line)
            if not match:
                raise BadLine("Unexpected line: %s" % line)
            self.command = match.group(1)
            self.arguments = match.group(3)
            self.path = match.group(5)
            self.props = dict()

            if self.arguments:
                args = self.arguments.split(",")
                for arg in args:
                    parts = arg.split("=", 1)
                    value = (len(parts) == 2 and parts[1] or "")
                    self.props[parts[0]] = value

            log.debug("Found line: %s, %s, %s, %s", self.command,
                      self.arguments, self.path, self.props)

    def repeat(self):
        return int(self.props.get("repeat", "1"))

    def comment(self):
        if len(self.line) == 0:
            return True
        elif self.line.startswith("#"):
            return True

    def execute(self, ctx):
        if self.comment():
            return
        m = getattr(self, "_op_%s" % self.command, None)
        if m is None:
            raise BadCommand("Unknown command: %s" % self.command)

        m(ctx)

    def create_obj(self, ctx, name):
        id = None
        id_path = ctx.dir / ("%s.id" % name)
        prop = self.props.get(name)
        # Do nothing if not in props
        if prop is None:
            return None
        # If an integer, use that as an id
        try:
            id = int(prop)
            log.debug("Using specified %s:%s" % (name, id))
        except:
            # Otherwise, create/re-use
            if prop == "":
                try:
                    id = int(id_path.lines()[0])
                except Exception, e:
                    log.debug("No %s.id: %s", name, e)
                    prop = str(uuid.uuid4())
            # Now, if there's still no id, create one
            if id is not None:
                log.debug("Using reload %s:%s" % (name, id))
            else:
                kls = getattr(omero.model, "%sI" % name)
                obj = kls()
                obj.name = omero.rtypes.rstring(prop)
                obj = ctx.update_service().saveAndReturnObject(obj)
                id = obj.id.val
                log.debug("Created obj %s:%s" % (name, id))
        id_path.write_text(str(id))
        return id

    def create_link(self, ctx, kls_name, parent, child):
        link = ctx.query_service().findByQuery(
            "select link from %s link where link.parent.id = %s and"
            " link.child.id = %s" % (kls_name, parent.id.val, child.id.val),
            None)
        if link:
            log.debug("Found link %s:%s" % (kls_name, link.id.val))
        else:
            kls = getattr(omero.model, "%sI" % kls_name)
            obj = kls()
            obj.parent = parent
            obj.child = child
            obj = ctx.update_service().saveAndReturnObject(obj)
            log.debug("Created link %s:%s" % (kls_name, obj.id.val))

    def _op_Import(self, ctx):
        p = path.path(self.path)
        if not p.exists():
            raise BadPath("File does not exist: %s" % self.path)

        f = str(p.abspath())
        out = ctx.dir / ("import_%s.out" % ctx.count)
        err = ctx.dir / ("import_%s.err" % ctx.count)

        args = ["import", "---file=%s" % str(out), "---errs=%s" % str(err),
                "-s", ctx.host(), "-k", ctx.key(), f]
        s_id = self.create_obj(ctx, "Screen")
        if s_id:
            args.extend(["-r", str(s_id)])
        p_id = self.create_obj(ctx, "Project")
        d_id = self.create_obj(ctx, "Dataset")
        if p_id and d_id:
            self.create_link(
                ctx, "ProjectDatasetLink", omero.model.ProjectI(p_id, False),
                omero.model.DatasetI(d_id, False))
        if d_id:
            args.extend(["-d", str(d_id)])

        ctx.cli.invoke(args)
        if ctx.cli.rv != 0:
            raise BadImport("Failed import: rv=%s" % ctx.cli.rv)
        num_pix = len(out.lines())
        log.debug("Import count: %s", num_pix)

    def _op_ServerTime(self, ctx):
        ctx.config_service().getServerTime()

    def _op_LoadFormats(self, ctx):
        ctx.query_service().findAll("Format", None)


class Context(object):
    """
    Login context which can be used by any handler
    for connecting to a single session.
    """

    def __init__(self, id, reporter=None, client=None):
        self.reporters = []
        self.count = 0
        self.id = id
        if client is None:
            self.client = omero.client(id)
            self.client.setAgent("OMERO.perf_test")
            self.client.createSession()
        else:
            self.client = client
        self.services = {}
        self.cli = omero.cli.CLI()
        self.cli.loadplugins()
        self.setup_dir()
        log.debug("Running performance tests in %s", self.dir)

    def add_reporter(self, reporter):
        self.reporters.append(reporter)

    def setup_dir(self):
        self.dir = path.path(".") / ("perfdir-%s" % os.getpid())
        if self.dir.exists():
            raise Exception("%s exists!" % self.dir)
        self.dir.makedirs()

        # Adding a file logger
        handler = logging.handlers.RotatingFileHandler(
            str(self.dir / "perf.log"), maxBytes=10000000, backupCount=5)
        handler.setLevel(logging.DEBUG)
        formatter = logging.Formatter(omero.util.LOGFORMAT)
        handler.setFormatter(formatter)
        logging.getLogger().addHandler(handler)
        # log.addHandler(handler)
        log.debug("Started: %s", time.ctime())

    def incr(self):
        self.count += 1

    def host(self):
        return self.client.getProperty("omero.host")

    def key(self):
        return self.client.sf.ice_getIdentity().name

    def report(self, command, start, stop, loops, rv):
        for reporter in self.reporters:
            reporter.report(command, start, stop, loops, rv)

    def _stateless(self, name, prx):
        svc = self.services.get(name)
        if svc:
            return svc
        else:
            svc = self.client.sf.getByName(name)
            svc = prx.uncheckedCast(svc)
            self.services[name] = svc
            return svc

    def query_service(self):
        return self._stateless(omero.constants.QUERYSERVICE,
                               omero.api.IQueryPrx)

    def config_service(self):
        return self._stateless(omero.constants.CONFIGSERVICE,
                               omero.api.IConfigPrx)

    def update_service(self):
        return self._stateless(omero.constants.UPDATESERVICE,
                               omero.api.IUpdatePrx)


class PerfHandler(object):

    def __init__(self, ctx=None):
        self.ctx = ctx

    def __call__(self, line):

        (self.ctx.dir/"line.log").write_text(line, append=True)

        item = Item(line)
        if item.comment():
            return

        values = {}
        total = 0.0
        self.ctx.incr()
        start = time.time()
        loops = item.repeat()
        for i in range(loops):
            try:
                item.execute(self.ctx)
            except ItemException:
                log.exception("Error")
                sys.exit(1)
            except Exception:
                log.debug("Error during execution: %s" % item.line.strip(),
                          exc_info=True)
                errs = values.get("errs", 0)
                errs += 1
                values["errs"] = errs

        if loops > 1:
            values["avg"] = total / loops

        stop = time.time()
        total += (stop - start)
        self.ctx.report(item.command, start, stop, loops, values)


#
# Reporter hierarchy
#


class Reporter(object):
    """
    Abstract base class of all reporters
    """

    def report(self, command, start, stop, loops, rv):
        raise Exception("Not implemented")


class CsvReporter(Reporter):

    def __init__(self, dir=None):
        if dir is None:
            self.stream = sys.stdout
        else:
            self.file = str(dir / "report.csv")
            self.stream = open(self.file, "w")
        print >>self.stream, "Command,Start,Stop,Elapsed,Average,Values"

    def report(self, command, start, stop, loops, values):
        print >>self.stream, "%s,%s,%s,%s,%s,%s" % (
            command, start, stop, (stop-start), (stop-start)/loops, values)
        self.stream.flush()


class HdfReporter(Reporter):

    def __init__(self, dir):
        import tables
        self.file = str(dir / "report.hdf")
        self.hdf = tables.openFile(self.file, "w")
        self.tbl = self.hdf.createTable("/", "report", {
            "Command": tables.StringCol(pos=0, itemsize=64),
            "Start": tables.Float64Col(pos=1),
            "Stop": tables.Float64Col(pos=2),
            "Elapsed": tables.Float64Col(pos=3),
            "Average": tables.Float64Col(pos=4),
            "Values": tables.StringCol(pos=5, itemsize=1000)
            })
        self.row = self.tbl.row

    def report(self, command, start, stop, loops, values):
        self.row["Command"] = command
        self.row["Start"] = start
        self.row["Stop"] = stop
        self.row["Elapsed"] = (stop-start)
        self.row["Average"] = (stop-start)/loops
        self.row["Values"] = values
        self.row.append()
        self.hdf.flush()


class PlotReporter(Reporter):

    def __init__(self):
        return
        # import matplotlib.pyplot as plt
        # self.fig = plt.figure()
        # self.ax = fig.add_subplot(111)

    def report(self, command, start, stop, loops, values):
        return
        # ax.set_ylim(-2,25)
        # ax.set_xlim(0, (last-first))
        # plt.show()

########################################################

#
# Functions for the execution of this module
#


def handle(handler, files):
    """
    Primary method used by the command-line execution of
    this module.
    """

    log.debug("Running perf on files: %s", files)
    for file in files:
        for line in file:
            handler(line)
    log.debug("Handled %s lines" % handler.ctx.count)
