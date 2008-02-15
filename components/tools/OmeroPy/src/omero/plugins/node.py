#!/usr/bin/env python
#
# OMERO Grid node plugin
# Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
import omero.node
import shlex

def do_node(self, arg):
    """
    Syntax: node [start|status|stop|kill] [nodename]
    Node name defaults to "default" configuration if not defined.
    Configurations are defined in the etc/ directory of the install.
    """
    args = shlex.split(arg)
    node = omero.node.Control(self.client())
    print "nodename ignored"
    if len(args) < 1:
        node.dispatch("status")
    else:
        node.dispatch(args[0])

CLI.do_node = do_node
