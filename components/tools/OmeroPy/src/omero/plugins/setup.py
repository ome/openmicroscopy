#!/usr/bin/env python
"""
   setup plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   The setup plugin is used during install and upgrade to
   properly configure a system.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, optparse, os, sys
from omero.cli import BaseControl

class Question:
    """
    Question which knows how to ask itself to the user via raw_input, as well
    as how to insert itself into optparse.OptionParser.
    """
    def __init__(self, parser, question, default, prompt = """%s [%s] """):
        self.prompt = prompt % ( question, default )
        self.parser = parser
        self.question = question
        self.default = default
        self.result = None

    def ask(self):
        if not self.result:
            self.result = raw_input(self.prompt).lower_case()
            if not self.result or len(self.result) == 0:
                self.result = self.default

class SetupControl(BaseControl):

    def help(self, args = None):
        self.ctx.out(
        """
Syntax: %(program_name)s setup [simple|intermediate|advanced] [+|-psql] [+|-django] [+|-jboss] [dirs]
        """)

    def __call__(self, *args):
        args = Arguments(*args)

        p = optparse.OptionParser()
        g = optparse.OptionGroup(p, "Skill level","Number of questions asked by setup")
        p.add_option("-s", "--simple", dest="simple",
                     help="Ask simple questions only")
        p.add_option_group(g)

        qs = {
            "psql":Question(p,"Do you want to install Postgres?","n"),
            "django":Question(p,"Do you want to install django?","n"),
            "jboss":Question(p,"Do you want to install JBoss?","y"),
            "bust":Question(p,"Would you like to have your logfiles and other state in a separate dir?","n")
            }

        (options, args) = p.parse_args(args)

        try:
            for key, q in qs.items():
                q.ask()
        except KeyboardError, ke:
            return

        """
        import getpass
        password = getpass.getpass()
        print password

        print getpass.getuser()
        """

try:
    # DISABLED register("setup", SetupControl)
    pass
except NameError:
    SetupControl()._main()
