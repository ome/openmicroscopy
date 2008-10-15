#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import os, glob

#
# Directories to be used during code generation
#
resources = os.path.abspath("resources")
generated = os.path.abspath("generated")
ice_slice = os.path.abspath("/opt/local/share/ice/slice") # FIXME

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
    return "-I%s -I%s -I%s --output-dir=%s %%s" % ( generated, resources, ice_slice, dir )

def names(dir, ice):
    basename = os.path.basename(ice)[:-4]
    filename = '%s/%s' % (dir, basename)
    return (basename, filename)

def basenames(where, dir):
    for ice in glob.glob("%s/%s/*.ice" % (where, dir)):
        yield names(dir, ice)

#
# Define calls to slice2java, slice2cpp, and slice2py
#

def slice_cpp(env, where, dir):
    command = "slice2cpp --include-dir=%s " + common( "%s/%s" % (generated, dir) )
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            [filename + '.h', filename + '.cpp'],                 # target
            filename + '.ice',                                    # source
            command % (dir, filename + '.ice'),                   # command
            chdir = where )                                       # dir
        actions.append( c )
    return actions

def slice_java(env, where, dir):
    command  = "slice2java  --tie " + common()
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            jdep(env["DEPMAP"], filename + '.java' ),             # target
            filename + '.ice',                                    # source
            command % filename + '.ice',                          # command
            chdir = where )                                       # dir
        actions.append( c )
    return actions

def slice_py(env, where, dir):
    prefix = dir.replace("/","_") + "_"
    command = "slice2py --prefix %s " + common()
    actions = []
    for basename, filename in basenames(where, dir):
        c = env.Command(
            [prefix + basename + '_ice.py' ],                      # target
            filename + '.ice',                                     # source
            command % (prefix, filename + '.ice'),                 # command
            chdir = where )                                        # dir
        actions.append( c )
    return actions

#
# Lists which can be used in a cross-product to generate
# all necessary files.
#
methods = [slice_java, slice_cpp, slice_py]
directories = ["omero", "omero/model", "omero/api"]
where = [generated, resources]



