#
# webgateway/webgateway_cache - web cache handler for webgateway
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

from django.conf import settings
import omero
import logging

logger = logging.getLogger('cache')

import struct, time, os, re, shutil
size_of_double = len(struct.pack('d',0))
string_type = type('')

CACHE=getattr(settings, 'WEBGATEWAY_CACHE', None)
THUMB_CACHE_TIME = 3600 # 1 hour
THUMB_CACHE_SIZE = 1024 # KB == 1MB
IMG_CACHE_TIME= 3600 # 1 hour
IMG_CACHE_SIZE = 512*1024 # KB == 512MB

class CacheBase (object):
    def __init__ (self):
        pass

    def get (self, k):
        return None

    def set (self, k, v, t=0):
        return False

    def delete (self, k):
        return False

class FileCache(CacheBase):
    _purge_holdoff = 4

    def __init__(self, dir, timeout=60, max_entries=0, max_size=0):
        """ max_size in KB """
        super(FileCache, self).__init__()
        self._dir = dir
        self._max_entries = max_entries
        self._max_size = max_size
        self._last_purge = 0
        self._default_timeout=timeout
        if not os.path.exists(self._dir):
            self._createdir()

    def add(self, key, value, timeout=None):
        if self.has_key(key):
            return False

        self.set(key, value, timeout)
        return True

    def get(self, key, default=None):
        fname = self._key_to_file(key)
        try:
            f = open(fname, 'rb')
            exp = struct.unpack('d',f.read(size_of_double))[0]
            now = time.time()
            if exp < now:
                f.close()
                self._delete(fname)
            else:
                return f.read()
        except (IOError, OSError, EOFError, struct.error):
            pass
        return default

    def set(self, key, value, timeout=None):
        if type(value) != string_type:
            raise ValueError("%s not a string, can't cache" % type(value))
        fname = self._key_to_file(key)
        dirname = os.path.dirname(fname)

        if timeout is None:
            timeout = self._default_timeout

        if self._full():
            # Maybe we already have this one cached, and we need the space
            try:
                self._delete(fname)
            except OSError:
                pass
            if self._full():
                return

        try:
            if not os.path.exists(dirname):
                os.makedirs(dirname)

            f = open(fname, 'wb')
            exp = time.time() + timeout
            f.write(struct.pack('d', exp))
            f.write(value)
        except (IOError, OSError):
            pass

    def delete(self, key):
        try:
            self._delete(self._key_to_file(key))
        except (IOError, OSError):
            pass

    def _delete(self, fname):
        logger.debug('requested delete for "%s"' % fname)
        if os.path.isdir(fname):
            shutil.rmtree(fname, ignore_errors=True)
        else:
            os.remove(fname)
            try:
                # Remove the 2 subdirs if they're empty
                dirname = os.path.dirname(fname)
                os.rmdir(dirname)
                os.rmdir(os.path.dirname(dirname))
            except (IOError, OSError):
                pass

    def _check_entry (self, fname):
        """
        Verifies if a specific cache entry (provided as absolute file path) is expired.
        If expired, it gets deleted and method returns false.
        If not expired, returns True.
        """
        try:
            f = open(fname, 'rb')
            exp = struct.unpack('d',f.read(size_of_double))[0]
            now = time.time()
            if exp < now:
                f.close()
                self._delete(fname)
                return False
            else:
                return True
        except (IOError, OSError, EOFError, struct.error):
            return False

    def has_key(self, key):
        fname = self._key_to_file(key)
        return self._check_entry(fname)

    def _full(self, _on_retry=False):
        # Check nr of entries
        if self._max_entries:
            try:
                x = int(os.popen('find %s -type f | wc -l' % self._dir).read().strip())
                if x >= self._max_entries:
                    if not _on_retry:
                        self._purge()
                        return self._full(True)
                    logger.warn('caching limits reached on %s: max entries %d' % (self._dir, self._max_entries))
                    return True
            except ValueError:
                logger.error('Counting cache entries failed')
        # Check for space usage
        if self._max_size:
            try:
                x = int(os.popen('du -sk %s' % self._dir).read().split('\t')[0].strip())
                if x >= self._max_size:
                    if not _on_retry:
                        self._purge()
                        return self._full(True)
                    logger.warn('caching limits reached on %s: max size %d' % (self._dir, self._max_size))
                    return True
            except ValueError:
                logger.error('Counting cache size failed')
        return False

    def _purge (self):
        """
        Iterate the whole cache structure searching and cleaning expired entries.
        this method may be expensive, so only call it when really necessary.
        """
        now = time.time()
        if now-self._last_purge < self._purge_holdoff:
            return
        self._last_purge = now

        logger.debug('entering purge')
        count = 0
        for p,_,files in os.walk(self._dir):
            for f in files:
                if not self._check_entry(os.path.join(p, f)):
                    count += 1
        logger.debug('purge finished, removed %d files' % count)

    def _createdir(self):
        try:
            os.makedirs(self._dir)
        except OSError:
            raise EnvironmentError, "Cache directory '%s' does not exist and could not be created'" % self._dir

    def _key_to_file(self, key):
        if key.find('..') > 0 or key.startswith('/'):
            raise ValueError('Invalid value for cache key: "%s"' % key)
        return os.path.join(self._dir, key)

    def _get_num_entries(self):
        
        count = 0
        for _,_,files in os.walk(self._dir):
            count += len(files)
        return count
    _num_entries = property(_get_num_entries)

FN_REGEX = re.compile('[#$,|]')
class WebGatewayCache (object):
    def __init__ (self, backend=None, basedir=CACHE):
        if backend is None or basedir is None:
            self._img_cache = CacheBase()
            self._thumb_cache = CacheBase()
        else:
            self._img_cache = backend(dir=os.path.join(basedir,'img'),
                                      timeout=IMG_CACHE_TIME, max_entries=0, max_size=IMG_CACHE_SIZE)
            self._thumb_cache = backend(dir=os.path.join(basedir,'thumb'),
                                        timeout=THUMB_CACHE_TIME, max_entries=0, max_size=THUMB_CACHE_SIZE)

    ##
    # Thumb

    def _thumbKey (self, r, client_base, iid):
        return 'thumb_%s/%s' % (client_base, str(iid))

    def setThumb (self, r, client_base, iid, obj):
        k = self._thumbKey(r, client_base, iid)
        logger.debug('   set: %s' % k)
        self._thumb_cache.set(k, obj)
        return True

    def getThumb (self, r, client_base, iid):
        k = self._thumbKey(r, client_base, iid)
        r = self._thumb_cache.get(k)
        if r is None:
            logger.debug('  fail: %s' % k)
        else:
            logger.debug('cached: %s' % k)
        return r

    def clearThumb (self, r, client_base, iid):
        k = self._thumbKey(r, client_base, iid)
        logger.debug(' clear: %s' % k)
        self._thumb_cache.delete(k)
        return True

    ##
    # Image

    def _imageKey (self, r, client_base, iid, z=0, t=0):
        if r:
            r = r.REQUEST
            c = FN_REGEX.sub('-',r.get('c', ''))
            m = r.get('m', '')
            p = r.get('p', '')
            if p and not isinstance(omero.gateway.ImageWrapper.PROJECTIONS.get(p, -1), omero.constants.projection.ProjectionType):
                p = ''
            q = r.get('q', '')
            rv = 'img_%s/%s/%%s-c%s-m%s-q%s' % (client_base, str(iid), c, m, q)
            if p:
                return rv % ('%s-%s' % (p, str(t)))
            else:
                return rv % ('%sx%s' % (str(z), str(t)))
        else:
            return 'img_%s/%s/' % (client_base, str(iid))

    def setImage (self, r, client_base, iid, z, t, obj):
        k = self._imageKey(r, client_base, iid, z, t)
        logger.debug('   set: %s' % k)
        self._img_cache.set(k, obj)
        return True

    def getImage (self, r, client_base, iid, z, t):
        k = self._imageKey(r, client_base, iid, z, t)
        r = self._img_cache.get(k)
        if r is None:
            logger.debug('  fail: %s' % k)
        else:
            logger.debug('cached: %s' % k)
        return r

    def clearImage (self, r, client_base, iid):
        k = self._imageKey(None, client_base, iid)
        logger.debug(' clear: %s' % k)
        self._img_cache.delete(k)
        # do the thumb too
        self.clearThumb(r, client_base, iid)
        return True

    def setSplitChannelImage (self, r, client_base, iid, z, t, obj):
        k = 'sc'+self._imageKey(r, client_base, iid, z, t)
        logger.debug('   set: %s' % k)
        self._img_cache.set(k, obj)
        return True

    def getSplitChannelImage (self, r, client_base, iid, z, t):
        k = 'sc'+self._imageKey(r, client_base, iid, z, t)
        r = self._img_cache.get(k)
        if r is None:
            logger.debug('  fail: %s' % k)
        else:
            logger.debug('cached: %s' % k)
        return r

    def clearSplitChannelImage (self, r, client_base, iid):
        k = 'sc'+self._imageKey(None, client_base, iid)
        logger.debug(' clear: %s' % k)
        self._img_cache.delete(k)
        # do the thumb too
        self.clearThumb(r, client_base, iid)
        return True

webgateway_cache = WebGatewayCache(FileCache)
