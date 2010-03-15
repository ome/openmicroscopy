#!/usr/bin/env python
"""
   script plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The script plugin is used to run arbitrary blitz scripts which
   take as their sole input Ice configuration arguments, including
   --Ice.Config=file1,file2.

   The first parameter, the script itself, should be natively executable
   on a given platform. I.e. invokable by subprocess.call([file,...])

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, os, sys, signal, time
from omero.cli import BaseControl, Arguments
from omero.util.temp_files import create_path
from omero_ext.strings import shlex
from omero_ext.functional import wraps


def wrapper(func):
    """
    Allows us to use def func(args) rather than def func(*args)
    in some of the below methods for standard configuration
    """
    def wrap(*args, **kwargs):
        self = args[0]
        rest = args[1:]
        args = Arguments(rest)
        self.ctx.conn(args)
        return func(self, args)
    wrap = wraps(func)(wrap)
    return wrap

class ScriptControl(BaseControl):

    def help(self, args = None):
        self.ctx.out("""
Syntax: %(program_name)s script file [configuration parameters]
        Executes a file as a script. Can be used to test scripts
        for later deployment on the grid.

        set
        set file=[id|name]
        set user=[id|name]
        set group=[id|name]
        set user=

        Note: Some of the other actions call set internally.


        cat
        cat file=[id|name]
        mv
        rm
        cp
        --replace / --overwrite

        register
        publish
        processors
        chain
        edit

        jobs
        jobs user
        jobs group

        launch
        launch file=[id|name]

        list
        list user
        list group
        list user=[id|name]
        list group=[id|name]

        log
        log file
        log user
        log group
        log file=[id|name]
        log user=[id|name]
        log group=[id|name]

        params
        params file=[id|name]

        serve
        serve user
        serve group
        serve user=[id|name]
        serve group=[id|name]
        serve group=[id|name] user=[id|name]
        serve background=[true|false]
        serve timeout={min}

        upload file=/tmp/my_script.py

        #
        # Other
        #

        run
        """)

    def TODO(self, args):

        if hasattr(self, "secure"):
            self.ctx.err("Secure cli cannot execture python scripts")

    @wrapper
    def cat(self, args):
        pass

    @wrapper
    def jobs(self, args):
        pass

    @wrapper
    def launch(self, args):
        client = self.ctx.conn()
        script_id = self._file(args, client)

        import omero_SharedResources_ice
        job = omero.model.ScriptJobI()
        job.linkOriginalFile(omero.model.OriginalFileI(script_id, False))
        interactive = client.sf.sharedResources().acquireProcessor(job, 10)
        if not interactive:
            self.ctx.err("No processor found")
        else:
            proc = interactive.execute(omero.rtypes.rmap())
            job = interactive.getJob()
            self.ctx.out("Job %s ready" % job.id.val)
            self.ctx.out("Waiting....")
            count = 0
            while proc.poll() is None:
                count += 1
                if (count%10) == 0:
                    self.ctx.out(".", newline=False)
                time.sleep(1)
            rmap = interactive.getResults(proc)
            rv = rmap and rmap.val or {}

            def p(m):
                class handle(object):
                    def write(this, val):
                        self.ctx.out(str(val), newline=False)
                    def close(this):
                        pass

                f = rv.get(m, None)
                if f and f.val:
                    self.ctx.out("*** %s ***" % m)
                    try:
                        client.download(ofile=f.val, filehandle=handle())
                    except:
                        self.ctx.err("Failed to display %s" % m)

            p("stdout")
            p("stderr")
            self.ctx.out("*** out parameters ***")
            for k, v in rv.items():
                if k not in ("stdout", "stderr", "omero.scripts.parse"):
                    if v is not None:
                        v = v.val # Unwrap rtypes
                    self.ctx.out("%s=%s" % (k, v))

    @wrapper
    def list(self, args):
        user = args.get("user", None)
        client = self.ctx.conn()
        sf = client.sf
        if user:
            id = sf.getAdminService().getEventContext().userId
            q = sf.getQueryService()
            objs = q.findAllByQuery("select s from OriginalFile s where s.format.value = 'text/x-python' and s.details.owner.id = %s" % id, None)
            scripts = {}
            for obj in objs:
                scripts[obj.id.val] = obj.name.val
            self._parse(scripts, "Scripts for user=%s" % id)
        else:
            import omero_api_IScript_ice
            svc = sf.getScriptService()
            scripts = svc.getScripts()
            self._parse(scripts, "Primary scripts")

    @wrapper
    def log(self, args):
        pass

    @wrapper
    def params(self, args):
        client = self.ctx.conn(args)
        script_id = self._file(args, client)
        import omero_api_IScript_ice
        svc = client.sf.getScriptService()
        job_params = svc.getParams(script_id)
        if job_params:
            self.ctx.out("")
            self.ctx.out("id=%s" % script_id)
            self.ctx.out("name=%s" % job_params.name)
            self.ctx.out("description=%s" % job_params.description)
            self.ctx.out("namespaces=%s" % ", ".join(job_params.namespaces))
            self.ctx.out("stdout=%s" % job_params.stdoutFormat)
            self.ctx.out("stderr=%s" % job_params.stderrFormat)
            print job_params.inputs
            def print_params(which, params):
                import omero
                self.ctx.out(which)
                for k, v in params.items():
                    self.ctx.out("  %s - %s" % (v.name, (v.description and v.description or "(no description)")))
                    self.ctx.out("    Optional: %s" % v.optional)
                    self.ctx.out("    Type: %s" % v.prototype.ice_staticId())
                    if isinstance(v.prototype, omero.RCollection):
                        self.ctx.out("    Subtype: %s" % v.prototype.val[0].ice_staticId())
                    elif isinstance(v.prototype, omero.RMap):
                        self.ctx.out("    Subtype: %s" % v.prototype.val.values[0].ice_staticId())
                    self.ctx.out("    Min: %s" % (v.min and v.min.val or ""))
                    self.ctx.out("    Max: %s" % (v.max and v.max.val or ""))
                    self.ctx.out("    Values: %s" % (v.values and ", ".join(v.values) or ""))
            print_params("Inputs:", job_params.inputs)
            print_params("Outputs:", job_params.outputs)

    @wrapper
    def serve(self, args):
        debug = args.getBool("debug", False)
        background = args.getBool("background", False)
        timeout = args.getInt("timeout", 0)
        user = args.get("user", None)

        import omero
        client = self.ctx.conn()
        accepts_list = []
        if user is not None:
            if user is True:
                user = client.sf.getAdminService().getEventContext().userId
            else:
                user = long(user)
            accepts_list.append(omero.model.ExperimenterI(user, False))

        # Similar to omero.util.Server starting here
        import logging
        original = list(logging._handlerList)
        levels = [o.level for o in original]
        for o in original:
            o.level = 500

        from omero.util import ServerContext, configure_logging
        from omero.processor import ProcessorI
        lvl = debug and 10 or 20
        configure_logging(loglevel=lvl)

        try:
            try:
                stop_event = omero.util.concurrency.get_event()
                serverid = "omero.scripts.serve"
                ctx = ServerContext(serverid, client.ic, stop_event)
                impl = ProcessorI(ctx, use_session=client.sf, accepts_list=accepts_list)
                impl.setProxy( client.adapter.addWithUUID(impl) )
            except exceptions.Exception, e:
                self.ctx.die(100, "Failed initialization")

            if background:
                REGISTER_CLEANUP(timeout)
            else:
                try:
                    def handler(signum, frame):
                        raise SystemExit()
                    old = signal.signal(signal.SIGALRM, handler)
                    signal.alarm(timeout)
                    self.ctx.input("Press any key to exit...\n")
                    signal.alarm(0)
                finally:
                    self.ctx.dbg("DONE")
                    signal.signal(signal.SIGTERM, old)
                    impl.cleanup()
        finally:
            for o,l in zip(original,levels):
                o.level = l


    @wrapper
    def upload(self, args):
        args.insert(0, "upload")
        self.ctx.pub(args, strict=True)

    @wrapper
    def replace(self, args):

        if len(args) != 2:
            self.ctx.die(111, "Usage: <original file id> <path to file")

        ofile = long(args.args[0])
        fpath = str(args.args[1])

        client = self.ctx.conn()
        ofile = client.sf.getQueryService().get("OriginalFile", ofile)
        client.upload(fpath, ofile=ofile)

    #
    # Other
    #
    def run(self, *args):
        args = Arguments(args)
        first, other = args.firstOther()
        if not first:
            self.ctx.err("No file given")
        elif len(other) == 0:
            if not os.path.exists(first):
                self.ctx.error("No such file: %s" % first)

            env = os.environ
            env["PYTHONPATH"] = self.ctx.pythonpath()
            p = subprocess.Popen(args,env=os.environ)
            p.wait()
            if p.poll() != 0:
                self.ctx.die(p.poll(), "Execution failed.")

    #
    # Helpers
    #

    def _parse(self, scripts, msg):
        ids = list(scripts.keys())
        ids.sort()
        self.ctx.out(msg)
        self.ctx.out("="*40)
        for id in ids:
            self.ctx.out("%8.8s - %s" % (id, scripts[id]))

    def _file(self, args, client):
        f = args.get("file", None)
        if f is None:
            self.ctx.die(100, "No script provided")
        try:
            script_id = long(f)
        except:
            q = client.sf.getQueryService()
            script_name = str(f)
            p = omero.sys.ParametersI()
            p.addString("name", script_name)
            scripts = q.findAllByQuery("select s from OriginalFile s where s.name = :name", p)
            if len(scripts) != 1:
                self.ctx.err("Didn't find a single script, but %s" % len(scripts))
            script_id = scripts[0].id.val
        return script_id

try:
    register("script", ScriptControl)
except NameError:
    ScriptControl()._main()
