#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import sys, os, glob, time
blitz = os.path.abspath( os.path.join(os.path.curdir, os.path.pardir, os.path.pardir, "blitz") )
sys.path.append( blitz )
from blitz_tools import *

BOOST_ALL=[
    # 1.39
    "boost_unit_test_framework-mt-d",
    "boost_unit_test_framework-mt",
    "boost_unit_test_framework-xgcc40-mt",
    "boost_unit_test_framework-vc90-mt-gd-1_39",
    # 1.40
    "boost_unit_test_framework"
    ]

#
# At the moment, execution of this script requires,
# ant tools-init to have been run
#

env = OmeroEnvironment(CPPPATH=["src","target"])

boost_check = """
//
// boost_check function from OmeroCpp/SConstruct
// Checks for the existnce of the unit_test.hpp
// header in the current INCLUDE paths. If found,
// A search will be made for the following
// dynamic libs:
//
// %s
//
// INCLUDE paths: %s
// LIBRARY paths: %s
//
#define BOOST_TEST_MAIN
#define BOOST_TEST_DYN_LINK
#include <boost/test/included/unit_test.hpp>
""" % (BOOST_ALL, env["CPPPATH"], env["LIBPATH"])

def CheckBoost(context):
    context.Message('Checking for boost_unit_test...')
    result = context.TryLink(boost_check, '.cpp')
    context.Result(result)
    return result

boost_libs = []

if not env.GetOption('clean'):
    conf = Configure(env, custom_tests = {'CheckBoost':CheckBoost})
    if not conf.CheckCXXHeader(os.path.join("Ice","Ice.h")):
        print 'Ice/Ice.h not found'
        env.Exit(1)
    has_boost = conf.CheckBoost()
    if has_boost:
        found = False
        for b in BOOST_ALL:
            if conf.CheckLib(b):
                print "Using %s" % b
                boost_libs.append(b)
                found = True
                break
        if not found:
            print "*"*50
            print " boost_unit_test header found but no library!"
            print " checked: %s" % BOOST_ALL
            print "*"*50
            env.Exit(1)
    conf.Finish()
else:
    has_boost = True

f = open("scons.log", "w")
f.write(env.Dump())
f.close()

compiler_env = dict()
compiler_env["timestamp"] = time.ctime()
compiler_env["ARCH"] = (env.is64bit() and "64" or "32")
compiler_env["RELEASE"] = (env.isdebug() and "dbg" or "rel")
compiler_env["LIBPATH"] = env.get("LIBPATH","unknown")
compiler_env["CPPPATH"] = env.get("CPPPATH","unknown")
compiler_env["CPPFLAGS"] = env.get("CPPFLAGS","unknown")
try:
    compiler_env["CXX"] = env["CC"] # Windows
except KeyError:
    compiler_env["CXX"] = env.get("CXX","unknown")
try:
    compiler_env["CXXVERSION"] = env["MSVS_VERSION"] # Windows
except KeyError:
    compiler_env["CXXVERSION"] = env.get("CXXVERSION", "unknown")

f = open("compiler.log", "w")
f.write("""
#
# Scons Compile Log : %(timestamp)s
#
CPPFLAGS=%(CPPFLAGS)s
CPPPATH=%(CPPPATH)s
CXX=%(CXX)s
CXXVERSION=%(CXXVERSION)s
LIBPATH=%(LIBPATH)s
ARCH=%(ARCH)s
RELEASE=%(RELEASE)s
""" % compiler_env)
f.close()

#
# Build the library
#
srcs = env.Glob("target/**/**/*.cpp") + \
       env.Glob("target/**/*.cpp") + \
       env.Glob("src/**/**/*.cpp") + \
       env.Glob("src/**/*.cpp")

target = "omero_client"
if env.iswin32():
    target += ".dll"

library = env.SharedLibrary(\
    target = target,
    source = srcs,
    LIBS = env.icelibs())
env.Alias('lib', library)

#
# Visual Studio
#

if target.endswith("dll"):
    msproj = env.MSVSProject(target = 'omero_client' + env['MSVSPROJECTSUFFIX'],
        srcs = [ str(x) for x in srcs ],
        buildtarget = library[0],
        variant = 'Release')
    env.Alias('msproj', msproj)

    install = env.Install('target/lib', library[0])
    install2 = env.Install('target/lib', library[1])
    env.Depends(install, install2)
    env.Alias('install', install)

else:
    install = env.Install('target/lib', library)
    env.Alias('install', install)

#
# Build tests
#
if not has_boost:
    if "test" in COMMAND_LINE_TARGETS:
        print "*" * 55
        print " WARNING: boost_unit_test_framework not installed"
        print "*" * 55
        # env.Exit(1)
else:
    tenv = env.Clone()
    tenv["CPPPATH"].append("test")
    tenv["ENV"]["BOOST_TEST_DYN_LINK"] = "1"

    main = tenv.Object("test/boost_main.cpp")
    fixture = tenv.Object("test/boost_fixture.cpp")

    def define_test(dir):
        test =  tenv.Program("test/%s.exe" % dir,
            [main, fixture] + tenv.Glob("test/%s/*.cpp" % dir),
            LIBS = ["omero_client"]+env.icelibs()+boost_libs)
        return test

    unit = define_test("unit")
    integration = define_test("integration")
