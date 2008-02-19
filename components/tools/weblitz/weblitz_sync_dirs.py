#
# weblitz_sync_dirs.py - Deal with static file delivery on multiple weblitz django apps
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

import os
from glob import glob

PATHS = (#'templates',
         'media/css',
         'media/img',
         'media/js',
         'media/html',
         )

if os.path.exists('server'):
    target_dir = 'server/static'
else:
    target_dir = 'blitzcon'

for f in glob('blitzcon*'):
    for p in PATHS:
        thispath = os.path.join(f,p)
        if os.path.isdir(thispath):
            for linkin_stuff in filter(lambda x: not x.startswith('.') and os.path.isdir(os.path.join(thispath, x)) and not os.path.islink(os.path.join(thispath, x)), os.listdir(thispath)):
                source = os.path.join(thispath, linkin_stuff)
                target = os.path.join(target_dir, p)
                if not os.path.exists(target):
                    os.makedirs(target)
                abs_target = os.path.abspath(os.path.join(target, linkin_stuff))
                print "1) %s\n2) %s\n3) %s"  % (source, target, abs_target)
                if os.path.lexists(abs_target):
                    if os.path.islink(abs_target):
                        print "**", abs_target
                        os.remove(abs_target)
                    else:
                        print "--", abs_target
                        continue
                print "symlink %s -> %s" % (os.path.abspath(source), abs_target)
                os.symlink(os.path.abspath(source), abs_target)
