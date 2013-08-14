#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#   $Id$
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import sys, os, glob, subprocess
import optparse
from SCons.Script.SConscript import *
from SCons.Script import AddOption, GetOption
from SCons.SConf import *
from SCons.Variables import *

#
# Global Directories
#
cwd = os.path.abspath( os.path.dirname( __file__ ) )
top = os.path.abspath( os.path.join( cwd, os.path.pardir, os.path.pardir ) )
blitz_resources = os.path.abspath( os.path.join( top, "components", "blitz", "resources") )
blitz_generated = os.path.abspath( os.path.join( top, "components", "blitz", "generated") )
tools_include = os.path.abspath( os.path.join( top, "components", "tools", "target", "include" ) )
tools_library = os.path.abspath( os.path.join( top, "components", "tools", "target", "lib" ) )
header = os.path.join( blitz_resources, "header.txt" )

# Relative
resources = os.path.abspath("resources")
generated = os.path.abspath("generated")

# Support ICE_HOME
if os.environ.has_key("ICE_HOME"):
    ice_home = os.path.abspath( os.environ["ICE_HOME"] )
    print "Using env[ICE_HOME] = %s" % (ice_home)
else:
    ice_home = None
if os.environ.has_key("SLICEPATH"):
    slicepath = os.path.abspath( os.environ["SLICEPATH"] )
    print "Using env[SLICEPATH] = %s" % (slicepath)
else:
    slicepath = None
if os.environ.has_key("SLICE2JAVA"):
    slice2java = os.environ["SLICE2JAVA"]
    print "Using env[SLICE2JAVA] = %s" % (slice2java)
else:
    slice2java = None
if os.environ.has_key("SLICE2PY"):
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

def common(dir = generated):
    """
    Necessary since output for C++ does not include directories.
    """
    return ["-I%s" % generated, "-I%s" % resources, "-I%s" % slicepath, "--output-dir=%s" % dir]

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
            raise Exception("%s returned %s" % (str(args), str(rv)) )
    return slice

def slice_java(env, where, dir):
    command  = [slice2java, "--tie"] + common()
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            jdep(env["DEPMAP"], filename + '.java' ),             # target
            filename + '.ice',                                    # source
            make_slice(command),                                  # command
            chdir = where )                                       # dir
        actions.append( c )
    return actions

def slice_py(env, where, dir):
    prefix = dir.replace("/","_") + "_"
    command = [slice2py, "--prefix", "%s" % prefix ] + common()
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            [prefix + basename + '_ice.py' ],                      # target
            filename + '.ice',                                     # source
            make_slice(command),                                  # command
            chdir = where )                                        # dir
        actions.append( c )
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

        try:
            AddOption('--release',
                  dest='release',
                  type='string',
                  nargs=1,
                  action='store',
                  metavar='RELEASE',
                  help='Release version [debug (default) or Os]')

            AddOption('--arch',
                  dest='arch',
                  type='string',
                  nargs=1,
                  action='store',
                  metavar='ARCH',
                  help='Architecture to build for [x86, x64, or detect (default)]')
        except optparse.OptionConflictError, e:
            pass  # These are global so this has been used twice

        # Very odd error: using ENV = os.environ, rather than ENV = dict(os.environ)
        # causes *sub*processes to receive a fresh environment with registry values
        # for PATH, LIB, etc. *pre*pended to the variables.
        SConsEnvironment.__init__(self, ENV = dict(os.environ), tools=tools, **kwargs)
        self.Decider('MD5-timestamp')

        # Print statements
        if not os.environ.has_key("VERBOSE"):
            self.Replace(CXXCOMSTR  = "Compiling $TARGET")
            self.Replace(LINKCOMSTR = "Linking   $TARGET")
            self.Replace(SHCXXCOMSTR  = "Compiling $TARGET")
            self.Replace(SHLINKCOMSTR = "Linking   $TARGET")

        # CXX
        if os.environ.has_key("CXX"):
            os_cxx = os.environ["CXX"]
            if (os_cxx):
                print "Overriding env[CXX] (%s) with os.env[CXX] (%s)" % (self["CXX"], os_cxx)
                self.Replace(CXX = os_cxx)

        # CXXFLAGS
        self.AppendUnique(CPPDEFINES=["OMERO_API_EXPORTS","_REENTRANT"])
        if self.isdebug():
            self.AppendUnique(CPPDEFINES=["DEBUG"])
        else:
            self.AppendUnique(CPPDEFINES=["NDEBUG"])

        if not self.iswin32():
            self.Append(CXXFLAGS=self.Split("-Wall -ansi"))
            self.Append(CXXFLAGS=self.Split("-Wno-long-long -Wnon-virtual-dtor"))
            self.Append(CXXFLAGS=self.Split("-Wno-unused-parameter -Wno-unused-function -Wunused-variable -Wunused-value"))
            # self.Append(CXXFLAGS=self.Split("-pedantic -ansi")) Ice fails pedantic due to extra ";"
            # self.Append(CXXFLAGS=self.Split("-Wno-long-long -Wctor-dtor-privacy -Wnon-virtual-dtor")) Ice fails the ctor check.
            # self.Append(CXXFLAGS=self.Split("-Werror")) http://lists.openmicroscopy.org.uk/pipermail/ome-devel/2010-February/001557.html
            if self.isdebug():
                self.Append(CXXFLAGS=self.Split("-O0 -g"))
            else:
                self.Append(CXXFLAGS=self.Split("-Os"))

        else:
            self.AppendUnique(CPPDEFINES=["WIN32_LEAN_AND_MEAN"])
            if self["CC"] == "cl":
                self.AppendUnique(CXXFLAGS=self.Split("/bigobj"))
                self.AppendUnique(CXXFLAGS=self.Split("/EHsc"))
                if self.isdebug():
                    self.Append(CXXFLAGS=["/Zi","/Od"])
                    self.AppendUnique(CXXFLAGS = ["/MDd"])
                else:
                    self.Append(CXXFLAGS=["/Os"])
                    self.AppendUnique(CXXFLAGS = ["/MD"])


                # Correcting for registry lookup under WoW64
                # Though here the PATH adheres to the values set by
                # vcvarsall etc., in subprocesses it revernts (via
                # the registry?) to the default values.
                self['LINK'] = self.which('link')
                self['AR'] = self.which('lib')
                self['CC'] = self.which('cl')
                self['CXX'] = '$CC'
                # Now CC has a non "cl" value. Can no longer use that check.

        # Now let user override
        if "CXXFLAGS" in os.environ:
            self.Append(CXXFLAGS=self.Split(os.environ["CXXFLAGS"]))

        #
        # LINKFLAGS
        #
        if self.iswin32():
            try:
                verbosity = int(os.environ.get("VERBOSE",0))
                if verbosity > 1:
                    # This is VERY verbose
                    self.AppendUnique(LINKFLAGS = ["/verbose"])
            except ValueError:
                pass

            if self.is64bit():
                self.Append(ARFLAGS = ['/MACHINE:X64'])
                self.Append(LINKFLAGS = ['/MACHINE:X64'])

            if self.is64bit():
                self.Append(LINKFLAGS = ['/DEBUG'])

        # Now let user override
        if "LINKFLAGS" in os.environ:
            self.Append(CXXFLAGS=self.Split(os.environ["LINKFLAGS"]))

    def isdebug(self):

        if hasattr(self, "_isdbg"):
            return self._isdbg

        RELEASE = GetOption("release")
        if RELEASE == "Os":
            self._isdbg = False
        else:
            self._isdbg = True
            if RELEASE not in [None, "debug"]:
                import warnings
                warnings.warn("Unknown release value. Using 'debug'")

        if not self.omero_quiet:
            print "Debug setting: %s (%s)" % (self._isdbg, RELEASE)
        return self._isdbg


    def iswin32(self):

        if hasattr(self, "_win32"):
            return self._win32

        self._win32 = self["PLATFORM"] == "win32"
        return self._win32

    def is64bit(self):

        if hasattr(self, "_bit64"):
            return self._bit64

        ARCH = GetOption("arch")
        if ARCH == "x64":
            self._bit64 = True
        elif ARCH == "x86":
            self._bit64 = False
        else:
            if ARCH not in [None,"detect"]:
                import warnings
                warnings.warn("Unknown arch value. Using 'detect'")
            if self.iswin32():
                # Work around for 32bit Windows executables
                try:
                    import win32process
                    self._bit64 = win32process.IsWow64Process()
                except:
                    import ctypes, sys
                    i = ctypes.c_int()
                    kernel32 = ctypes.windll.kernel32
                    process = kernel32.GetCurrentProcess()
                    kernel32.IsWow64Process(process, ctypes.byref(i))
                    self._bit64 = (i.value != 0)
            else:
                import platform
                self._bit64 = platform.architecture()[0] == "64bit"

        if not self.omero_quiet:
            print "64-Bit build: %s (%s)" % (self._bit64, ARCH)
        return self._bit64

    def icelibs(self):
        if self.iswin32() and self.isdebug():
            return ["Iced", "IceUtild", "Glacier2d"]
        else:
            return ["Ice", "IceUtil", "Glacier2"]

    def which(self, exe):
        from omero_ext import which
        try:
            rv = file = which.which(exe)
        except which.WhichError:
            rv = file = ""

        if file:
            if self.iswin32():
                import win32api
                try:
                    rv = win32api.GetShortPathName(file)
                    if not os.path.exists(rv):
                        rv = file
                except:
                    pass
        return rv

    def fatal_error(self, message):
        print "Fatal Error: %s" % message
        self.Exit(1)

