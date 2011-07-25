#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
   chgrp plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.
"""

from omero.cli import BaseControl, CLI
import sys

HELP="""Move data between groups

Example Usage:
  omero chgrp 101 /Image:1             # Move all of Image 1 to group 101
  omero chgrp Group:101 /Image:1       # Move all of Image 1 to group 101
  omero chgrp "myLab" /Image:1         # Move all of Image 1 to group "myLab"

  What data is moved is the same as that which would be deleted by a similar
  call to "omero delete /Image:1"

"""


class ChgrpArg(object):

    def __call__(self, arg):
        parts = arg.split(":", 1)
        assert len(parts) == 2
        type = parts[0]
        id = long(parts[1])

        return omero.cmd.GraphCommand(\
                action=omero.cmd.ChgrpActionClass(),\
                type=type,\
                id=id,\
                options={})


class ChgrpControl(BaseControl):

    def _configure(self, parser):
        parser.set_defaults(func=self.chgrp)
        parser.add_argument("--wait", type=long, help="Number of seconds to"+\
                " wait for the chgrp to complete (Indefinite < 0; No wait=0).", default=-1)
        parser.add_argument("--list", action="store_true", help="""Print a list of all available chgrp specs""")
        parser.add_argument("--list-details", action="store_true",
                help="""Print a list of all available chgrp specs along with detailed info""")
        parser.add_argument("--report", action="store_true", help="""Print more detailed report of each chgrp""")
        parser.add_argument("obj", nargs="+", type=ChgrpArg(), help="""Objects to be chgrp'd in the form "<Class>:<Id>""")

    def chgrp(self, args):
        import omero
        client = self.ctx.conn(args.obj)
        handler = client.submit(commands)

try:
    register("chgrp", ChgrpControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("chgrp", ChgrpControl, HELP)
        cli.invoke(sys.argv[1:])
