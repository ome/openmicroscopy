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
from random import random
import datetime


logger = logging.getLogger('cache')

import struct, time, os, re, shutil, stat
size_of_double = len(struct.pack('d',0))
string_type = type('')

CACHE=getattr(settings, 'WEBGATEWAY_CACHE', None)
TMPROOT=getattr(settings, 'WEBGATEWAY_TMPROOT', None)
THUMB_CACHE_TIME = 3600 # 1 hour
THUMB_CACHE_SIZE = 20*1024 # KB == 20MB
IMG_CACHE_TIME= 3600 # 1 hour
IMG_CACHE_SIZE = 512*1024 # KB == 512MB
JSON_CACHE_TIME= 3600 # 1 hour
JSON_CACHE_SIZE = 1*1024 # KB == 1MB
TMPDIR_TIME = 3600 * 12 # 12 hours

class CacheBase (object): #pragma: nocover
    def __init__ (self):
        pass

    def get (self, k):
        return None

    def set (self, k, v, t=0, invalidateGroup=None):
        return False

    def delete (self, k):
        return False

    def wipe (self):
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
#
    def add(self, key, value, timeout=None, invalidateGroup=None):
        if self.has_key(key):
            return False

        self.set(key, value, timeout, invalidateGroup=invalidateGroup)
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

    def set(self, key, value, timeout=None, invalidateGroup=None):
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
            exp = time.time() + timeout + (timeout / 5 * random()) 
            f.write(struct.pack('d', exp))
            f.write(value)
        except (IOError, OSError): #pragma: nocover
            pass

    def delete(self, key):
        try:
            self._delete(self._key_to_file(key))
        except (IOError, OSError): #pragma: nocover
            pass

    def _delete(self, fname):
        logger.debug('requested delete for "%s"' % fname)
        if os.path.isdir(fname):
            shutil.rmtree(fname, ignore_errors=True)
        else:
            os.remove(fname)
            try:
                # Remove the parent subdirs if they're empty
                dirname = os.path.dirname(fname)
                while dirname != self._dir:
                    os.rmdir(dirname)
                    dirname = os.path.dirname(fname)
            except (IOError, OSError):
                pass

    def wipe (self):
        shutil.rmtree(self._dir)
        self._createdir()
        return True

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
        except (IOError, OSError, EOFError, struct.error): #pragma: nocover
            return False

    def has_key(self, key):
        fname = self._key_to_file(key)
        return self._check_entry(fname)

    def _du (self):
        """
        Disk Usage count on the filesystem the cache is based at
        
        @rtype: int
        @return: the current usage, in KB
        """
        return int(os.popen('du -sk %s' % os.path.join(os.getcwd(),self._dir)).read().split('\t')[0].strip())

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
            except ValueError: #pragma: nocover
                logger.error('Counting cache entries failed')
        # Check for space usage
        if self._max_size:
            try:
                x = self._du()
                if x >= self._max_size:
                    if not _on_retry:
                        self._purge()
                        return self._full(True)
                    logger.warn('caching limits reached on %s: max size %d' % (self._dir, self._max_size))
                    return True
            except ValueError: #pragma: nocover
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
        except OSError: #pragma: nocover
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
        self._basedir = basedir
        self._lastlock = None
        if backend is None or basedir is None:
            self._json_cache = CacheBase()
            self._img_cache = CacheBase()
            self._thumb_cache = CacheBase()
        else:
            self._json_cache = backend(dir=os.path.join(basedir,'json'),
                                      timeout=JSON_CACHE_TIME, max_entries=0, max_size=JSON_CACHE_SIZE)
            self._img_cache = backend(dir=os.path.join(basedir,'img'),
                                      timeout=IMG_CACHE_TIME, max_entries=0, max_size=IMG_CACHE_SIZE)
            self._thumb_cache = backend(dir=os.path.join(basedir,'thumb'),
                                        timeout=THUMB_CACHE_TIME, max_entries=0, max_size=THUMB_CACHE_SIZE)

    def _updateCacheSettings (self, cache, timeout=None, max_entries=None, max_size=None):
        if isinstance(cache, CacheBase):
            cache = (cache,)
        for c in cache:
            if timeout is not None:
                c._default_timeout = timeout
            if max_entries is not None:
                c._max_entries = max_entries
            if max_size is not None:
                c._max_size = max_size

    def __del__ (self):
        if self._lastlock:
            try:
                logger.debug('removing cache lock file on __del__')
                os.remove(self._lastlock)
            except:
                pass
            self._lastlock = None

    def tryLock (self):
        """
        simple lock mechanisn to avoid multiple processes on the same cache to
        step on each other's toes.

        @rtype: boolean
        @return: True if we created a lockfile or already had it. False otherwise.
        """
        lockfile = os.path.join(self._basedir, '%s_lock' % datetime.datetime.now().strftime('%Y%m%d_%H%M'))
        if self._lastlock:
            if lockfile == self._lastlock:
                return True
            try:
                os.remove(self._lastlock)
            except:
                pass
            self._lastlock = None
        try:
            fd = os.open(lockfile, os.O_CREAT | os.O_EXCL)
            os.close(fd)
            self._lastlock = lockfile
            return True
        except OSError:
            return False

    def handleEvent (self, client_base, e):
        """
        Handle one event from blitz.onEventLogs.

        Meant to be overridden, this implementation just logs.
        """
        logger.debug('## %s#%i %s user #%i group #%i(%i)' % (e.entityType.val,
                                                             e.entityId.val,
                                                             e.action.val,
                                                             e.details.owner.id.val,
                                                             e.details.group.id.val,
                                                             e.event.id.val))

    def eventListener (self, client_base, events):
        """
        handle events coming our way from blitz.onEventLogs.
        
        Because all processes will be listening to the same events, we use a simple file
        lock mechanism to make sure the first process to get the event will be the one
        handling things from then on.
        """
        for e in events:
            if self.tryLock():
                self.handleEvent(client_base, e)
            else:
                logger.debug("## ! ignoring event %s" % str(e.event.id.val))

    def clear (self):
        self._json_cache.wipe()
        self._img_cache.wipe()
        self._thumb_cache.wipe()

    def _cache_set (self, cache, key, obj):
        logger.debug('   set: %s' % key)
        cache.set(key, obj)

    def _cache_clear (self, cache, key):
        logger.debug(' clear: %s' % key)
        cache.delete(key)

    def invalidateObject (self, client_base, obj):
        """ Invalidates all caches for this particular object """
        if obj.OMERO_CLASS == 'Image':
            self.clearImage(None, client_base, obj)
        else:
            logger.debug('unhandled object type: %s' % obj.OMERO_CLASS)
            self.clearJson(client_base, obj)

    ##
    # Thumb

    def _thumbKey (self, r, client_base, iid, size):
        if size is not None:
            return 'thumb_%s/%s/%s' % (client_base, str(iid), 'x'.join([str(x) for x in size]))
        else:
            return 'thumb_%s/%s' % (client_base, str(iid))

    def setThumb (self, r, client_base, iid, obj, size=()):
        k = self._thumbKey(r, client_base, iid, size)
        self._cache_set(self._thumb_cache, k, obj)
        return True

    def getThumb (self, r, client_base, iid, size=()):
        k = self._thumbKey(r, client_base, iid, size)
        r = self._thumb_cache.get(k)
        if r is None:
            logger.debug('  fail: %s' % k)
        else:
            logger.debug('cached: %s' % k)
        return r

    def clearThumb (self, r, client_base, iid, size=None):
        k = self._thumbKey(r, client_base, iid, size)
        self._cache_clear(self._thumb_cache, k)
        return True

    ##
    # Image

    def _imageKey (self, r, client_base, img, z=0, t=0):
        iid = img.getId()
        if r:
            r = r.REQUEST
            c = FN_REGEX.sub('-',r.get('c', ''))
            m = r.get('m', '')
            p = r.get('p', '')
            if p and not isinstance(omero.gateway.ImageWrapper.PROJECTIONS.get(p, -1),
                                    omero.constants.projection.ProjectionType): #pragma: nocover
                p = ''
            q = r.get('q', '')
            rv = 'img_%s/%s/%%s-c%s-m%s-q%s' % (client_base, str(iid), c, m, q)
            if p:
                return rv % ('%s-%s' % (p, str(t)))
            else:
                return rv % ('%sx%s' % (str(z), str(t)))
        else:
            return 'img_%s/%s/' % (client_base, str(iid))

    def setImage (self, r, client_base, img, z, t, obj, ctx=''):
        k = self._imageKey(r, client_base, img, z, t) + ctx
        self._cache_set(self._img_cache, k, obj)
        return True

    def getImage (self, r, client_base, img, z, t, ctx=''):
        k = self._imageKey(r, client_base, img, z, t) + ctx
        r = self._img_cache.get(k)
        if r is None:
            logger.debug('  fail: %s' % k)
        else:
            logger.debug('cached: %s' % k)
        return r

    def clearImage (self, r, client_base, img):
        k = self._imageKey(None, client_base, img)
        self._cache_clear(self._img_cache, k)
        # do the thumb too
        self.clearThumb(r, client_base, img.getId())
        # and json data
        self.clearJson(client_base, img)
        return True

    def setSplitChannelImage (self, r, client_base, img, z, t, obj):
        return self.setImage(r, client_base, img, z, t, obj, '-sc')

    def getSplitChannelImage (self, r, client_base, img, z, t):
        return self.getImage(r, client_base, img, z, t, '-sc')

    def setOmeTiffImage (self, r, client_base, img, obj):
        return self.setImage(r, client_base, img, 0, 0, obj, '-ometiff')

    def getOmeTiffImage (self, r, client_base, img):
        return self.getImage(r, client_base, img, 0, 0, '-ometiff')

    ##
    # hierarchies (json)

    def _jsonKey (self, r, client_base, obj, ctx=''):
        if obj:
            return 'json_%s/%s_%s/%s' % (client_base, obj.OMERO_CLASS, obj.id, ctx)
        else:
            return 'json_%s/single/%s' % (client_base, ctx)

    def clearJson (self, client_base, obj):
        logger.debug('clearjson')
        if obj.OMERO_CLASS == 'Dataset':
            self.clearDatasetContents(None, client_base, obj)
    
    def setDatasetContents (self, r, client_base, ds, data):
        k = self._jsonKey(r, client_base, ds, 'contents')
        self._cache_set(self._json_cache, k, data)
        return True

    def getDatasetContents (self, r, client_base, ds):
        k = self._jsonKey(r, client_base, ds, 'contents')
        r = self._json_cache.get(k)
        if r is None:
            logger.debug('  fail: %s' % k)
        else:
            logger.debug('cached: %s' % k)
        return r

    def clearDatasetContents (self, r, client_base, ds):
        k = self._jsonKey(r, client_base, ds, 'contents')
        self._cache_clear(self._json_cache, k)
        return True

webgateway_cache = WebGatewayCache(FileCache)

class AutoLockFile (file):
    def __init__ (self, fn, mode):
        super(AutoLockFile, self).__init__(fn, mode)
        self._lock = os.path.join(os.path.dirname(fn), '.lock')
        file(self._lock, 'a').close()
    def __del__ (self):
        try:
            os.remove(self._lock)
        except:
            pass
    def close (self):
        try:
            os.remove(self._lock)
        except:
            pass
        super(AutoLockFile, self).close()

class WebGatewayTempFile (object):
    def __init__ (self, tdir=TMPROOT):
        self._dir = tdir
        if tdir and not os.path.exists(self._dir):
            self._createdir()

    def _createdir(self):
        try:
            os.makedirs(self._dir)
        except OSError: #pragma: nocover
            raise EnvironmentError, "Cache directory '%s' does not exist and could not be created'" % self._dir

    def _cleanup (self):
        now = time.time()
        for f in os.listdir(self._dir):
            try:
                ts = os.path.join(self._dir, f, '.timestamp')
                if os.path.exists(ts):
                    ft = float(file(ts).read()) + TMPDIR_TIME
                else:
                    ft = float(f) + TMPDIR_TIME
                if ft < now:
                    shutil.rmtree(os.path.join(self._dir, f), ignore_errors=True)
            except ValueError:
                continue

    def newdir (self, key=None):
        if not self._dir:
            return None, None
        self._cleanup()
        stamp = str(time.time())
        if key is None:
            dn = os.path.join(self._dir, stamp)
            while os.path.exists(dn):
                stamp = str(time.time())
                dn = os.path.join(self._dir, stamp)
            key = stamp
        dn = os.path.join(self._dir, key)
        if not os.path.isdir(dn):
            os.makedirs(dn)
        file(os.path.join(dn, '.timestamp'), 'w').write(stamp)
        return dn, key

    def new (self, name, key=None):
        if not self._dir:
            return None, None, None
        dn, stamp = self.newdir(key)
        name = name.decode('utf8').encode('ascii', 'ignore')
        fn = os.path.join(dn, name)
        rn = os.path.join(stamp, name)
        lf = os.path.join(dn, '.lock')
        cnt = 30
        fsize = 0
        while os.path.exists(lf) and cnt > 0:
            time.sleep(1)
            t = os.stat(fn)[stat.ST_SIZE]
            if (t == fsize):
                cnt -= 1
                logger.debug('countdown %d' % cnt)
            else:
                fsize = t
                cnt = 30
        if cnt == 0:
            return None, None, None
        if os.path.exists(fn):
            return fn, rn, True
        return fn, rn, AutoLockFile(fn, 'wb')

webgateway_tempfile = WebGatewayTempFile()
