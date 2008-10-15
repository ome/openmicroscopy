#
#   $Id$
#
#   Copyright 2008 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#

import os, glob

#
# At the moment, execution of this script requires,
# ant tools-init to have been run
#

env = Environment(
    CPPPATH = ['/opt/local/include','.'],
    ENV = os.environ )
env.Repository('target','src')
env.Decider('MD5-timestamp')

#
# Generate via slice2cpp
#
slice_omero = "slice2cpp --include-dir=omero       --output-dir=omero       -I. -I/opt/local/share/ice/slice %s"
slice_model = "slice2cpp --include-dir=omero/model --output-dir=omero/model -I. -I/opt/local/share/ice/slice %s"

def slice(dir, command):
    includes = []
    commands = []
    for ice in glob.glob("target/%s/*.ice"%dir):
        basename = os.path.basename(ice)[:-4]
        includes.append( """#include <%s/%s.h>\n""" % (dir, basename) )
        filename = '%s/%s' % (dir, basename)
        c = env.Command(
            ['target/' + filename + '.h', filename + '.cpp'], # source
             'target/' + filename + '.ice',                   # target
            command % filename + '.ice',                      # command
            chdir = 'target' )                                # dir
        env.Execute( c )
        commands.append( c )
    return commands, includes

omero_commands, omero_includes = slice("omero", slice_omero)
model_commands, model_includes = slice("omero/model", slice_model)
env.Alias('slice', omero_commands + model_commands)

#
# Parse the omero/Model.h
#
def model_h_func(target, source, env):
   """
   Doesn't use source as it includes the OMERO implementation classes.
   """
   f = open(str(target[0]), 'w')
   for mi in model_includes:
        f.write(mi)
   f.close()

if True:
    model_h = env.Command(
            'target/omero/Model.h',
            Glob('target/omero/model/*.h'),
            model_h_func)
    env.Execute( model_h )
    env.Alias('model', model_h)
    env.Depends('model', 'slice')

#
# Build the library
#
if True:
    library = env.SharedLibrary("OMERO_client",
        Glob("target/**/*.cpp") + Glob("src/**/*.cpp") +
        Glob("target/**/**/*.cpp") + Glob("src/**/**/*.cpp"),
        LIBS=["Ice","Glacier2","IceUtil"],
        LIBPATH=".:/opt/local/lib")
    env.Alias('lib', library)
    env.Depends('lib','model')
    env.Depends('lib','slice')


