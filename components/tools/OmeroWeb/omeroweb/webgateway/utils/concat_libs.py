#
# webgateway/utils/concat_libs - prepares a concatenated and minimized version of the JS and CSS needed for webgateway
# 
# Copyright (c) 2008, 2009 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>

import os, sys

script_pwd = os.path.dirname(os.path.abspath(sys.argv[0]))

sys.path.append(script_pwd)

from jsmin import jsmin

js_dir = os.path.join(script_pwd, '../media/js')
css_dir = os.path.join(script_pwd, '../media/css')
out_dir = 'concat'

build_list = {
    'weblitz_minimal.js': (
        '3rdparty/jquery-1.2.6.js',
        'weblitz-viewport.js',
        'jquery-plugin-viewportImage.js',
        'jquery-plugin-slider.js',
        'gs_utils.js',
        ),
    'weblitz_minimal.css': (
        'jquery-plugin-slider.css',
        'weblitz-viewport.css',
        ),
}

for k,v in build_list.items():
    count = 0
    print "Doing %s" % k
    ext = os.path.splitext(k)[1][1:]
    basepath = locals()[ext+'_dir']
    outpath = os.path.join(basepath, out_dir)
    if not os.path.exists(outpath):
        os.makedirs(outpath)
    f = open(os.path.join(outpath, k), 'wb')
    for e in v:
        print "  + %s" % e
        count += 1
        r = open(os.path.join(basepath, e), 'rb')
        f.write(r.read())
        r.close()
    f.close()
    print "  = %d files" % count
    if ext == 'js':
        print "  * minimifying..."
        fin = open(os.path.join(outpath, k), 'rb')
        fout = open(os.path.join(outpath, k[:-3] + '_min.js'), 'wb')
        fout.write(jsmin(fin.read()))
        fin.close()
        fout.close()
