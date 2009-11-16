#!/usr/bin/env python

import os
import Ice
import sys
import path
import time
import omero
import logging
import optparse
import fileinput
import exceptions
import omero.util
import omero.util.temp_files
import omero_Constants_ice

log = logging.getLogger("omero.perf")


FILE_FORMAT = """
File format:
    <blank>                                     Ignored
    # comment                                   Ignored
    ServerTime(repeat=100)                      Retrieve the server time 100 times
    Import:<file>                               Import given file
    Import(Screen:<id>):<file>                  Import given file into screen
    Import(Dataset:<id>):<file>                 Import given file into project/dataset
    Import(Project:<id>,Dataset:<id>):<file>    Import given file into project/dataset
    # "Import" is the name of a command available in the current context
"""

parser = optparse.OptionParser(usage = """usage: %%prog [options] file1 file2

Use "-" as a file name to read from stdin.

%s""" % FILE_FORMAT)

parser.add_option('-l', '--list', action='store_true', help='list available commands')
parser.add_option('-d', '--debug', action='store_true', help='use DEBUG log level')
parser.add_option('-H', '--omero.host', action='store', help='host for connection')
parser.add_option('-P', '--omero.port', action='store', help='port for connection')
parser.add_option('-W', '--omero.pass', action='store', help='pass for connection')
parser.add_option('-C', '--Ice.Config', action='store', help='config file for connection')
#parser.add_option('-c', '--class', action='store', metavar='CLASS', help='special handler CLASS to instantiate')


#
# Support classes
#


class Item(object):
    """
    Single line-item in the configuration file
    """

    def __init__(self, line):
        self.line = line.strip()

    def comment(self):
        rv = bool(self.line)
        rv &= self.line.startswith("#")
        return rv


class Context(object):
    """
    Login context which can be used by any handler
    for connecting to a single session.
    """

    def __init__(self, id, reporter = None):

        if reporter is None:
            reporter = CsvReporter()
        self.reporter = reporter

        self.client = omero.client(id)
        self.client.createSession()
        self.services = {}

    def report(self, handler, start, stop, rv):
        self.reporter.report(handler, start, stop, rv)

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
        return self._stateless(omero.constants.QUERYSERVICE, omero.api.IQueryPrx)

    def config_service(self):
        return self._stateless(omero.constants.CONFIGSERVICE, omero.api.IConfigPrx)

#
# Abstract handlers
#


class PerfHandler(object):
    """
    Abstract base class of all handlers
    """

    def __init__(self, ctx = None):
        self.ctx = ctx
        self.count = 0

    def __call__(self, line):

        item = Item(line)
        if item.comment():
            return

        self.count += 1
        start = time.time()
        try:
            rv = self.handle(item)
        except exceptions.Exception, e:
            rv = e
        stop = time.time()
        self.ctx.report(self, start, stop, rv)

    def handle(self, item):
        raise exceptions.Exception("Not implemented")


class RepeatHandler(PerfHandler):

    def __init__(self, ctx = None, repeat = 10):
        PerfHandler.__init__(self, ctx)
        self.repeat = repeat

    def handle(self, item):
        for i in range(self.repeat):
            try:
                self.single(item)
            except:
                log.exception("Error during repetition %s", i)
        return {"repeats": self.repeat}

    def single(self, item):
        raise exceptions.Exception("Not implemented")


class CompositeHandler(PerfHandler):

    def __init__(self, handlers = []):
        self.handlers = handlers
        self.count = 0

    def __call__(self, line):
        self.count += 1
        for handler in self.handlers:
            try:
                handler(line)
            except:
                log.exception("Error in handler %s", handler)



#
# Individual handlers performing a single task
#


class ServerTimeHandler(RepeatHandler):
    """
    Handler intended primarily for testing.
    """

    def single(self, item):
        self.ctx.config_service().getServerTime()


class LoadEnumsHandler(RepeatHandler):
    """
    Handler intended primarily for testing.
    """

    def single(self, item):
        self.ctx.query_service().findAll("Format", None)


class ThrowingHandler(PerfHandler):
    """
    Handler intended primarily for testing.
    """

    def handle(self, item):
        raise exceptions.Exception("Throwing...")


#
# Composite types which run full test suites
#


class FullPerfTest(CompositeHandler):

    def __init__(self, ctx):
        handlers = list()
        handlers.append(ServerTimeHandler(ctx))
        handlers.append(LoadEnumsHandler(ctx))
        #handlers.append(HierarchySizeHandler(ctx))
        #handlers.append(ImportHandler(ctx))
        #handlers.append(RepositorySizeHandler(ctx))
        CompositeHandler.__init__(self, handlers)

#
# Reporter hierarchy
#


class Reporter(object):
    """
    Abstract base class of all reporters
    """

    def report(self, handler, start, stop, rv):
        raise exceptions.Exception("Not implemented")


class CsvReporter(Reporter):

    def __init__(self, stream = sys.stdout):
        self.stream = stream
        print "Start,Stop,HandlerClass,Success,ReturnValue"

    def report(self, handler, start, stop, rv):
        success = not isinstance(rv, exceptions.Exception)
        print >>self.stream, "%s,%s,%s,%s,%s,%s" % (handler.count, handler.__class__.__name__, start, stop, success, rv)


########################################################

#
# Functions for the execution of this module
#


def usage(prog = sys.argv[0]):
    return parser.format_help()


def setup_dir():
    perf_dir = path.path(".") / ("perfdir-%s" % os.getpid())
    if perf_dir.exists():
        raise exceptions.Exception("%s exists!" % perf_dir)
    perf_dir.makedirs()

    # Adding a file logger
    handler = logging.handlers.RotatingFileHandler(str(perf_dir / "perf.log"), maxBytes = 10000000, backupCount = 5)
    handler.setLevel(logging.DEBUG)
    formatter = logging.Formatter(omero.util.LOGFORMAT)
    handler.setFormatter(formatter)
    log.addHandler(handler)
    log.debug("Started: %s", time.ctime())

    return perf_dir


def handle(handler, files):
    """
    Primary method used by the command-line execution of
    this module.
    """
    setup_dir()

    for line in fileinput.input(files):
        handler(line)
    log.debug("Handled %s lines" % handler.count)


def main(args, prog = sys.argv[0]):

    parser.prog = prog
    opts, files = parser.parse_args(args)
    if opts.list:
        import inspect
        import pprint
        for k, v in globals().items():
            if isinstance(v, type):
                found = False
                parent = v
                while not found:
                    parent = inspect.getclasstree([parent])[0][0]
                    if parent == object:
                        found = True
                    elif parent == PerfHandler:
                        print k
                        found = True
        sys.exit(0)
    elif not files:
        print "No files"
        print usage()
        sys.exit(2)

    level = opts.debug and logging.DEBUG or logging.INFO
    logging.basicConfig(level=level)

    props = [""]+["--%s=%s" % (x, getattr(opts, x, "")) for x in ["omero.host", "omero.port", "omero.pass"]]
    config = getattr(opts, "Ice.Config", "")
    if config:
        props.append("--Ice.Config="+config)
    id = Ice.InitializationData()
    id.properties = Ice.createProperties(props)

    reporter = CsvReporter()
    context = Context(id, reporter)
    handler = FullPerfTest(context)

    log.debug("Running perf on files: %s", files)
    handle(handler, files)

if __name__ == "__main__":
    args = sys.argv[1:]
    main(args)
