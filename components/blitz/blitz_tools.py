#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#   $Id$
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import os
import glob
import subprocess
from SCons.Script.SConscript import SConsEnvironment

#
# Global Directories
#
cwd = os.path.abspath(os.path.dirname(__file__))
top = os.path.abspath(os.path.join(cwd, os.path.pardir, os.path.pardir))
blitz_resources = os.path.abspath(
    os.path.join(top, "components", "blitz", "resources"))
blitz_generated = os.path.abspath(
    os.path.join(top, "components", "blitz", "generated"))
tools_include = os.path.abspath(
    os.path.join(top, "components", "tools", "target", "include"))
tools_library = os.path.abspath(
    os.path.join(top, "components", "tools", "target", "lib"))
header = os.path.join(blitz_resources, "header.txt")

# Relative
resources = os.path.abspath("resources")
generated = os.path.abspath("generated")

# Support ICE_HOME
if "ICE_HOME" in os.environ:
    ice_home = os.path.abspath(os.environ["ICE_HOME"])
    print "Using env[ICE_HOME] = %s" % (ice_home)
else:
    ice_home = None
if "SLICEPATH" in os.environ:
    slicepath = os.path.abspath(os.environ["SLICEPATH"])
    print "Using env[SLICEPATH] = %s" % (slicepath)
else:
    slicepath = None
if "SLICE2JAVA" in os.environ:
    slice2java = os.environ["SLICE2JAVA"]
    print "Using env[SLICE2JAVA] = %s" % (slice2java)
else:
    slice2java = None
if "SLICE2PY" in os.environ:
    slice2py = os.environ["SLICE2PY"]
    print "Using env[SLICE2PY] = %s" % (slice2py)
else:
    slice2py = None


def jdep(DEPMAP, target):
    """
    Uses a map named "DEPMAP" to look map from Java file
    names which don't exist ("omero/API.ice" -> "omero/API.java")
    to real Globs ("omero/api/*.java")
    """
    try:
        return DEPMAP[target]
    except KeyError:
        return target

#
# Helpers
#


def common(dir=generated):
    """
    Necessary since output for C++ does not include directories.
    """
    return ["-I%s" % generated, "-I%s" % resources, "-I%s" % slicepath,
            "--output-dir=%s" % dir]


def names(dir, ice):
    basename = os.path.basename(ice)[:-4]
    filename = '%s/%s' % (dir, basename)
    return (basename, filename)


def basenames(where, dir):
    for ice in glob.glob("%s/%s/*.ice" % (where, dir)):
        yield names(dir, ice)

#
# Define calls to slice2java and slice2py
#


def make_slice(command):
    def slice(target, source, env):
        args = command+[str(source[0].get_abspath())]
        rv = subprocess.call(args)
        if rv != 0:
            raise Exception("%s returned %s" % (str(args), str(rv)))
    return slice


def slice_java(env, where, dir):
    command = [slice2java, "--tie"] + common()
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            jdep(env["DEPMAP"], filename + '.java'),              # target
            filename + '.ice',                                    # source
            make_slice(command),                                  # command
            chdir=where)                                          # dir
        actions.append(c)
    return actions


def slice_py(env, where, dir):
    prefix = dir.replace("/", "_") + "_"
    command = [slice2py, "--prefix", "%s" % prefix] + common()
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            [prefix + basename + '_ice.py'],                      # target
            filename + '.ice',                                    # source
            make_slice(command),                                  # command
            chdir=where)                                          # dir
        actions.append(c)
    return actions

#
# Lists which can be used in a cross-product to generate
# all necessary files.
#
methods = [slice_java, slice_py]
directories = ["omero", "omero/model", "omero/api", "omero/cmd"]
where = [generated, resources]


class OmeroEnvironment(SConsEnvironment):
    """
    Wrapper class around a scons environment for properly setting up the
    build environment
    """

    def __init__(self, **kwargs):
        try:
            tools = list(kwargs.pop("tools"))
            tools.append('packaging')
        except KeyError:
            tools = ['default', 'packaging']

        # omero_quiet is for internal use in order to
        # quiet down this instance for re-use.
        try:
            self.omero_quiet = kwargs.pop("omero_quiet")
        except KeyError:
            self.omero_quiet = False

        # Very odd error: using ENV = os.environ, rather than
        # ENV = dict(os.environ) causes *sub*processes to receive a fresh
        # environment with registry values for PATH, LIB, etc. *pre*pended to
        # the variables.
        SConsEnvironment.__init__(self, ENV=dict(os.environ), tools=tools,
                                  **kwargs)
        self.Decider('MD5-timestamp')
