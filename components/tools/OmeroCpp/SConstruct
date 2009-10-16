#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import sys, os, glob
blitz = os.path.abspath( os.path.join(os.path.curdir, os.path.pardir, os.path.pardir, "blitz") )
sys.path.append( blitz )
from blitz_tools import *

BOOST_ALL=[
    # 1.39
    "boost_unit_test_framework-mt-d",
    "boost_unit_test_framework-mt",
    "boost_unit_test_framework-xgcc40-mt",
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

#
# Build the library
#
srcs = env.Glob("target/**/**/*.cpp") + \
       env.Glob("target/**/*.cpp") + \
       env.Glob("src/**/**/*.cpp") + \
       env.Glob("src/**/*.cpp")

target = "omero_client"
if sys.platform == "win32":
    target += ".dll"

library = env.SharedLibrary(\
    target = target,
    source = srcs,
    LIBS = ["Ice","Glacier2","IceUtil"])

env.Alias('lib', library)

install = env.Install('../target/lib', library)
env.Alias('install', install)

#
# Visual Studio
#

if target.endswith("dll"):
    msproj = env.MSVSProject(target = 'omero_client' + env['MSVSPROJECTSUFFIX'],
        srcs = srcs,
        buildtarget = lib,
        variant = 'Release')
    env.Alias('msproj', msproj)

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
            LIBS=["Ice","Glacier2","IceUtil","omero_client"]+boost_libs)
        return test

    unit = define_test("unit")
    integration = define_test("integration")
