#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# omero_fuse.py - FUSE based OMERO client
# 
# Copyright (c) 2009 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>


from errno import ENOENT
from stat import S_IFDIR, S_IFLNK, S_IFREG
from sys import argv, exit, stdout
from time import time
from types import StringTypes
from array import array
import os

from fuse import FUSE, Operations, LoggingMixIn

import logging
logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(levelname)s %(message)s',
                    stream=stdout,
                    )

import omero

class BlitzStorage(LoggingMixIn, Operations):
    """
    This is a demo implementation of a FUSE client for OMERO.
    Done using MacFUSE, and only tested in OSX 10.5.
    It's really just a proof of concept at this stage, so use it at your own risk.
    - The performance is really bad, compared to what it can be if simple caching is implemented, because for every path
      operation the object tree needs to be traversed, and every time a jpeg representation of an image is accessed
      (for reading or simply for checking the size) the image is rendered.
    - Only simple access is implemented. You get the Project list at the mount root, dataset and image from there.
    - Images can be rendered as full size image or thumbnail, both as jpeg.
    - the 'packedint' file has an RGB representation of the image.
    - No provision for setting the rendering options, other than defining the plane, has been implemented, but this can
      be easily achieved.
    - Directory names are $ObjectType$ID.
    - Metadata is not accessible, but again that is easily implemented.
    I tried to quickly implement TIFF access to the images, but couldn't do multiple planes on a single tiff using the
    available python libs. Maybe shoot for OME-TIFF directly wouldn't be a bad idea ;)
    """
    def __init__(self, username, password):
        self.client = omero.client_wrapper(username, password)
        self.client.connect()
        self.files = {}
        self.fd = []
        now = time()
        self.files['/'] = dict(st_mode=(S_IFDIR | 0755), st_ctime=now,
            st_mtime=now, st_atime=now)

    def _resolvePath (self, path, fd=None):
        if isinstance(path, StringTypes):
            path = path.split('/')
        print path
        if path[0] == '':
            if len(path) == 1:
                return None
            fd = self.client
            path.pop(0)
        gp = PATH_REGEX.match(path[0]).groups()
        if hasattr(fd, 'CHILD_WRAPPER_CLASS'):
            if fd.OMERO_CLASS == 'Image':
                children = None
                if fd._fs_meta['context']:
                    fd._fs_meta[fd._fs_meta['context']] = long(path[0])
                    fd._fs_meta['context'] = None
                elif path[0] in fd._fs_meta['available_context']:
                    fd._fs_meta['available_context'].pop(fd._fs_meta['available_context'].index(path[0]))
                    fd._fs_meta['context'] = path[0]
            else:
                children = fd.listChildren()
        else:
            children = fd.listProjects()
        if children:
            for e in  children:
                if e.OMERO_CLASS==gp[0] and e.id == long(gp[1]):
                    fd = e
                    if fd.OMERO_CLASS == 'Image':
                        fd._fs_meta = {'Z':0, 'T':0, 'context':None, 'available_context':['Z', 'T']}
                    break
                else:
                    fd = None
        if len(path) == 1:
            if path[0] == 'image.jpg':
                def t ():
                    return fd.renderJpeg(fd._fs_meta['Z'],fd._fs_meta['T'])
                return t
            elif path[0] == 'thumb.jpg':
                return fd.getThumbnail
            elif path[0] == 'packedint':
                def t ():
                    rv = array('i')
                    fd._prepareRenderingEngine()
                    rv.fromlist(fd._re.renderAsPackedInt(fd._pd))
                    return rv.tostring()
                return t
            return fd
        else:
            return self._resolvePath(path[1:], fd)

    def _stat (self, bobj):
        if not bobj:
            return None
        if callable(bobj):
            mode = S_IFREG
            st_size = len(bobj())
        else:
            mode = S_IFDIR
            st_size = 1
        return dict(st_mode=(mode | 0755), st_ctime=time(), st_mtime=time(), st_atime=time(),
                    st_size=st_size)

#    def chmod(self, path, mode):
#        return 0
#
#    def chown(self, path, uid, gid):
#        return 0
#    
#    def create(self, path, mode):
#        return self.fd

    def getattr(self, path, fh=None):
        st  = {}
        if path == '/':
            # Add 2 for `.` and `..` , subtruct 1 for `/`
            st = self.files[path]
            st = self.files[path]
            st['st_nlink'] = len(self.files) + 1
        else:
            st = self._stat(self._resolvePath(path))
        if not st:
            raise OSError(ENOENT)
        return st

#    def mkdir(self, path, mode):
#        return 0

    def open(self, path, flags):
        # TODO: I'm always assuming read, but not verifying
        if None in self.fd:
            idx = self.fd.index(None)
            self.fd[idx] = self._resolvePath(path)()
            rv = idx
        else:
            self.fd.append(self._resolvePath(path)())
            rv = len(self.fd)-1
        return rv

    def read(self, path, size, offset, fh):
        return self.fd[fh][offset:offset+size]

    def readdir(self, path, fh):
        if path == '/':
            files = ['Project%i' % x.id for x in self.client.listProjects()]
        else:
            p = self._resolvePath(path)
            if p:
                if p.OMERO_CLASS == 'Image':
                    meta = getattr(p, '_fs_meta', {})
                    print meta
                    if meta.get('context', None) == 'Z':
                        files = ['%i' % x for x in range(p.getSizeZ())]
                    elif meta.get('context', None) == 'T':
                        files = ['%i' % x for x in range(p.getSizeT())]
                    else:
                        files = ['image.jpg', 'thumb.jpg', 'packedint']
                        files.extend(meta.get('available_context', []))
                else:
                    files = ['%s%i' % (x.OMERO_CLASS, x.id) for x in p.listChildren()]
            else:
                files = []
        return ['.','..'] + files

    def release(self, path, fh):
        self.fd[fh] = None
        return 0


#    def readlink(self, path):
#        return self.data[path]

#    def rename(self, old, new):
#        return 0
#    
#    def rmdir(self, path):
#        return 0

    def statfs(self, path):
        return dict(f_bsize=512, f_blocks=4096, f_bavail=2048)
    
#    def symlink(self, target, source):
#        return 0
#    
#    def truncate(self, path, length, fh=None):
#        return 0
#
#    def unlink(self, path):
#        return 0
#    
#    def utimens(self, path, times=None):
#        return 0
#    
#    def write(self, path, data, offset, fh):
#        return self.fd[fh].write(data, offset)

if __name__ == "__main__":
    if len(argv) != 4:
        print 'usage: %s user password <mountpoint>' % argv[0]
        exit(1)
    fuse = FUSE(BlitzStorage(argv[1],argv[2]), argv[3], foreground=True)
