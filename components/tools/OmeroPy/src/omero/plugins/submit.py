#!/usr/bin/env python
"""
   submit plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import CLI, BaseControl
import cmd, sys, exceptions
import sys

prompt = "omero submit [%s]> "

class Save(exceptions.Exception):
    pass

class Cancel(exceptions.Exception):
    pass

class SubmitCLI(CLI):

    def __init__(self):
        CLI.__init__(self)
        self.queue = []
        self.prompt = prompt % str(0)

    def postcmd(self, stop, line):
        self.queue.append(line)
        self.prompt = prompt % str(len(self.queue))
        return CLI.postcmd(self, stop, line)

    def do_save(self, arg):
        raise Save()

    def do_cancel(self, arg):
        raise Cancel()

    def execute(self):
        print "Uploading"
        print submit.queue

class SubmitControl(BaseControl):

    def help(self, args = None):
        self.ctx.out("""
Syntax: %(program_name)s submit single command with args
                         submit

        When run without arguments, submit shell is opened
        which takes commands without executing them. On save,
        the file is trasferred to the server, and executed.
        """)

    def __call__(self, *args):
        args = Arguments(args)
        submit = SubmitCLI()
        if arg and len(arg) > 0:
            submit.invoke(arg)
            submit.execute()
        else:
            try:
                submit.invokeloop()
            except Save, s:
                submit.execute()
            except Cancel, c:
                l = len(submit.queue)
                if l > 0:
                    print l," items queued. Really cancel? [Yn]"

try:
    register("submit", SubmitControl)
except NameError:
    SubmitControl()._main()
