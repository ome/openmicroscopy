#!/usr/bin/env python

"""

:author: Josh Moore <josh@glencoesoftware.com>

Generator script for producing the ome.services.sharing.data classes
Copyright (c) 2008, Glencoe Software, Inc.
See LICENSE for details.

"""
import sys, os, subprocess, time

def readlink(file=sys.argv[0]):
    import stat

    file = sys.argv[0]
    while stat.S_ISLNK(os.lstat(file)[stat.ST_MODE]):
        target = os.readlink(file)
        if target[0] != "/":
            file = os.path.join(os.path.dirname(file), target)
        else:
            file = target

    file = os.path.abspath(file)
    return file

exe = readlink()
shr = os.path.join(exe, os.pardir)
shr = os.path.normpath(shr)
dat = os.path.join(shr, "data")
src = os.path.join(shr, os.pardir, os.pardir, os.pardir)
src = os.path.normpath(src)

def share():
    clean()
    slice()

def clean(dir=dat):
        if os.path.exists(dat):
            print "Removing %s. Cancel now if necessary. Waiting 5 seconds." % dat
            time.sleep(5)
            ls = os.listdir(dat)
            for file in ls:
                print "Removing %s" % file
                os.remove(os.path.join(dat,file))
            os.rmdir(dat)

def slice(dir=dat):
        os.mkdir(dat)
        README = open(os.path.join(dat,"README.txt"),"w")
        README.write("""
        THE FILES IN THIS DIRECTORY ARE GENERATE
        AND WILL BE AUTOMATICALLY DELETED
        """)
        README.flush()
        README.close()
        subprocess.call("""slice2freezej --dict ome.services.sharing.data.ShareMap,long,ome::services::sharing::data::ShareData \
	--dict-index ome.services.sharing.data.ShareMap,id \
	--dict-index ome.services.sharing.data.ShareMap,owner \
        --output-dir %s \
	Share.ice""" % src, shell = True)
        subprocess.call("""slice2freezej --dict ome.services.sharing.data.ShareItems,long,ome::services::sharing::data::ShareItem \
	--dict-index ome.services.sharing.data.ShareItems,type \
	--dict-index ome.services.sharing.data.ShareItems,share \
        --output-dir %s \
	Share.ice""" % src, shell = True)
	subprocess.call("""slice2java --output-dir %s Share.ice""" % src, shell = True)

if __name__ == "__main__":
    clean()
    share()
