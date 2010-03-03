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
        f = args.get("file", None)
        if f is None:
            self.ctx.err("No script provided")
            return

        client = self.ctx.conn()
        sf = client.sf
        q = sf.getQueryService()
        try:
            script_id = long(f)
        except:
            script_name = str(f)
            p = omero.sys.ParametersI()
            p.addString("name", script_name)
            scripts = q.findAllByQuery("select s from OriginalFile s where s.name = :name", p)
            if len(scripts) != 1:
                self.ctx.err("Didn't find a single script, but %s" % len(scripts))
            script_id = scripts[0].id.val

        import omero_SharedResources_ice
        job = omero.model.ScriptJobI()
        job.linkOriginalFile(omero.model.OriginalFileI(script_id, False))
        interactive = sf.sharedResources().acquireProcessor(job, 10)
        print "INTER",interactive
        if not interactive:
            self.ctx.err("No processor found")
        else:
            proc = interactive.execute(omero.rtypes.rmap())
            print "PROC",proc
            job = proc.getJob()
            print "JOB", job
            self.ctx.out("Job %s ready" % job.id.val)
            self.ctx.out("Waiting....")
            count = 0
            while proc.poll() is None:
                count += 1
                if (count%10) == 0:
                    self.ctx.out(".", newline=False)
                time.sleep(1)

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
        pass

    @wrapper
    def serve(self, args):
        background = args.getBool("background", False)
        timeout = args.getInt("timeout", 0)

        # Similar to omero.util.Server starting here
        import omero
        from omero.util import ServerContext
        from omero.processor import ProcessorI

        try:
            client = self.ctx.conn()
            stop_event = omero.util.concurrency.get_event()
            serverid = "omero.scripts.serve"
            ctx = ServerContext(serverid, client.ic, stop_event)
            impl = ProcessorI(ctx, use_session=client.sf, accepts_list=[])
            impl.setProxy( client.adapter.addWithUUID(impl) )
        except exceptions.Exception, e:
            import traceback as tb
            print tb.format_exc()
            self.ctx.die(100, "Failed initialization")

        if background:
            REGISTER_CLEANUP(timeout)
        else:
            try:
                def handler(signum, frame):
                    self.ctx.dbg("SIGNAL")
                    stop_event.set()
                old = signal.signal(signal.SIGTERM, handler)
                if timeout:
                    stop_event.wait(timeout)
                else:
                    stop_event.wait()
            finally:
                self.ctx.dbg("DONE")
                signal.signal(signal.SIGTERM, old)
                impl.cleanup()

    @wrapper
    def upload(self, args):
        file, other = args.firstOther()
        self.ctx.pub(["upload"]+other.args, strict=True)

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
        self.ctx.out("="*100)
        for id in ids:
            self.ctx.out("%8.8s - %s" % (id, scripts[id]))

try:
    register("script", ScriptControl)
except NameError:
    ScriptControl()._main()
