#!/usr/bin/env python
# -*- coding: utf-8 -*-
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
from types import StringTypes

logger = logging.getLogger(__name__)

import struct
import time
import os
import re
import shutil
import stat
size_of_double = len(struct.pack('d', 0))
# string_type = type('')

CACHE = getattr(settings, 'WEBGATEWAY_CACHE', None)
TMPROOT = getattr(settings, 'WEBGATEWAY_TMPROOT', None)
THUMB_CACHE_TIME = 3600  # 1 hour
THUMB_CACHE_SIZE = 20*1024  # KB == 20MB
IMG_CACHE_TIME = 3600  # 1 hour
IMG_CACHE_SIZE = 512*1024  # KB == 512MB
JSON_CACHE_TIME = 3600  # 1 hour
JSON_CACHE_SIZE = 1*1024  # KB == 1MB
TMPDIR_TIME = 3600 * 12  # 12 hours


class CacheBase (object):  # pragma: nocover
    """
    Caching base class - extended by L{FileCache} for file-based caching.
    Methods of this base class return None or False providing a no-caching
    implementation if needed
    """

    def __init__(self):
        """ not implemented """
        pass

    def get(self, k):
        return None

    def set(self, k, v, t=0, invalidateGroup=None):
        return False

    def delete(self, k):
        return False

    def wipe(self):
        return False


class FileCache(CacheBase):
    """
    Implements file-based caching within the directory specified in
    constructor.
    """
    _purge_holdoff = 4

    def __init__(self, dir, timeout=60, max_entries=0, max_size=0):
        """
        Initialises the class.

        @param dir:         Path to directory to place cached files.
        @param timeout:     Cache timeout in secs
        @param max_entries: If specified, limits number of items to cache
        @param max_size:    Maxium size of cache in KB
        """

        super(FileCache, self).__init__()
        self._dir = dir
        self._max_entries = max_entries
        self._max_size = max_size
        self._last_purge = 0
        self._default_timeout = timeout
        if not os.path.exists(self._dir):
            self._createdir()

    def add(self, key, value, timeout=None, invalidateGroup=None):
        """
        Adds data to cache, returning False if already cached. Otherwise
        delegating to L{set}

        @param key:                 Unique key for cache
        @param value:               Value to cache - must be String
        @param timeout:             Optional timeout - otherwise use default
        @param invalidateGroup:     Not used?
        """

        if key in self:
            return False

        self.set(key, value, timeout, invalidateGroup=invalidateGroup)
        return True

    def get(self, key, default=None):
        """
        Gets data from cache

        @param key:     cache key
        @param default: default value to return
        @return:        cache data or default if timout has passed
        """
        fname = self._key_to_file(key)
        try:
            f = open(fname, 'rb')
            if not self._check_entry(f):
                f.close()
                self._delete(fname)
            else:
                return f.read()
        except (IOError, OSError, EOFError, struct.error):
            pass
        return default

    def set(self, key, value, timeout=None, invalidateGroup=None):
        """
        Adds data to cache, overwriting if already cached.

        @param key:                 Unique key for cache
        @param value:               Value to cache - must be String
        @param timeout:             Optional timeout - otherwise use default
        @param invalidateGroup:     Not used?
        """

        if not isinstance(value, StringTypes):
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
            if timeout > 0:
                exp = time.time() + timeout + (timeout / 5 * random())
            else:
                exp = 0
            f.write(struct.pack('d', exp))
            f.write(value)
            f.close()
        except (IOError, OSError):  # pragma: nocover
            pass

    def delete(self, key):
        """
        Attempt to delete the cache data referenced by key
        @param key:     Cache key
        """

        try:
            self._delete(self._key_to_file(key))
        except (IOError, OSError):  # pragma: nocover
            pass

    def _delete(self, fname):
        """
        Tries to delete the data at the specified absolute file path

        @param fname:   File name of data to delete
        """

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

    def wipe(self):
        """ Deletes everything in the cache """

        shutil.rmtree(self._dir)
        self._createdir()
        return True

    def _check_entry(self, fname):
        """
        Verifies if a specific cache entry (provided as absolute file path) is
        expired.
        If expired, it gets deleted and method returns false.
        If not expired, returns True.

        If fname is a file object, fpos will advance size_of_double bytes.

        @param fname:   File path or file object
        @rtype Boolean
        @return True if entry is valid, False if expired
        """
        try:
            if isinstance(fname, StringTypes):
                f = open(fname, 'rb')
                exp = struct.unpack('d', f.read(size_of_double))[0]
            else:
                f = None
                exp = struct.unpack('d', fname.read(size_of_double))[0]
            if self._default_timeout > 0 and exp > 0:
                now = time.time()
                if exp < now:
                    if f is not None:
                        f.close()
                        self._delete(fname)
                    return False
                else:
                    return True
            return True
        except (IOError, OSError, EOFError, struct.error):  # pragma: nocover
            return False

    def has_key(self, key):
        """
        Returns true if the cache has the specified key
        @param key:     Key to look for.
        @rtype:         Boolean
        """
        fname = self._key_to_file(key)
        return self._check_entry(fname)

    def _du(self):
        """
        Disk Usage count on the filesystem the cache is based at

        @rtype: int
        @return: the current usage, in KB
        """
        cmd = 'du -sk %s' % os.path.join(os.getcwd(), self._dir)
        return int(os.popen(cmd).read().split('\t')[0].strip())

    def _full(self, _on_retry=False):
        """
        Checks whether the cache is full, either because we have exceeded max
        number of entries or the cache space is full.

        @param _on_retry:   Flag allows calling this method again after
                            purge() without recursion
        @return:            True if cache is full
        @rtype:             Boolean
        """

        # Check nr of entries
        if self._max_entries:
            try:
                x = int(os.popen('find %s -type f | wc -l'
                                 % self._dir).read().strip())
                if x >= self._max_entries:
                    if not _on_retry:
                        self._purge()
                        return self._full(True)
                    logger.warn('caching limits reached on %s: max entries %d'
                                % (self._dir, self._max_entries))
                    return True
            except ValueError:  # pragma: nocover
                logger.error('Counting cache entries failed')
        # Check for space usage
        if self._max_size:
            try:
                x = self._du()
                if x >= self._max_size:
                    if not _on_retry:
                        self._purge()
                        return self._full(True)
                    logger.warn('caching limits reached on %s: max size %d'
                                % (self._dir, self._max_size))
                    return True
            except ValueError:  # pragma: nocover
                logger.error('Counting cache size failed')
        return False

    def _purge(self):
        """
        Iterate the whole cache structure searching and cleaning expired
        entries.
        this method may be expensive, so only call it when really necessary.
        """
        now = time.time()
        if now-self._last_purge < self._purge_holdoff:
            return
        self._last_purge = now

        logger.debug('entering purge')
        count = 0
        for p, _, files in os.walk(self._dir):
            for f in files:
                if not self._check_entry(os.path.join(p, f)):
                    count += 1
        logger.debug('purge finished, removed %d files' % count)

    def _createdir(self):
        """
        Creates a directory for the root dir of the cache.
        """
        try:
            os.makedirs(self._dir)
        except OSError:  # pragma: nocover
            raise EnvironmentError("Cache directory '%s' does not exist and"
                                   " could not be created'" % self._dir)

    def _key_to_file(self, key):
        """
        Uses the key to construct an absolute path to the cache data.
        @param key:     Cache key
        @return:        Path
        @rtype:         String
        """

        if key.find('..') > 0 or key.startswith('/'):
            raise ValueError('Invalid value for cache key: "%s"' % key)
        return os.path.join(self._dir, key)

    def _get_num_entries(self):
        """
        Returns the number of files in the cache
        @rtype:     int
        """
        count = 0
        for _, _, files in os.walk(self._dir):
            count += len(files)
        return count
    _num_entries = property(_get_num_entries)

FN_REGEX = re.compile('[#$,|]')


class WebGatewayCache (object):
    """
    Caching class for webgateway.
    """

    def __init__(self, backend=None, basedir=CACHE):
        """
        Initialises cache

        @param backend:     The cache class to use for caching. E.g.
                            L{FileCache}
        @param basedir:     The base location for all caches. Sub-dirs created
                            for json/ img/ thumb/
        """

        self._basedir = basedir
        self._lastlock = None
        if backend is None or basedir is None:
            self._json_cache = CacheBase()
            self._img_cache = CacheBase()
            self._thumb_cache = CacheBase()
        else:
            self._json_cache = backend(dir=os.path.join(basedir, 'json'),
                                       timeout=JSON_CACHE_TIME,
                                       max_entries=0,
                                       max_size=JSON_CACHE_SIZE)
            self._img_cache = backend(dir=os.path.join(basedir, 'img'),
                                      timeout=IMG_CACHE_TIME,
                                      max_entries=0,
                                      max_size=IMG_CACHE_SIZE)
            self._thumb_cache = backend(dir=os.path.join(basedir, 'thumb'),
                                        timeout=THUMB_CACHE_TIME,
                                        max_entries=0,
                                        max_size=THUMB_CACHE_SIZE)

    def _updateCacheSettings(self, cache, timeout=None, max_entries=None,
                             max_size=None):
        """
        Updates the timeout, max_entries and max_size (if specified) for the
        given cache

        @param cache:       Cache or caches to update.
        @type cache:        L{CacheBase} or list of caches
        """

        if isinstance(cache, CacheBase):
            cache = (cache,)
        for c in cache:
            if timeout is not None:
                c._default_timeout = timeout
            if max_entries is not None:
                c._max_entries = max_entries
            if max_size is not None:
                c._max_size = max_size

    def __del__(self):
        """
        Tries to remove the lock on this cache.
        """
        if self._lastlock:
            try:
                logger.debug('removing cache lock file on __del__')
                os.remove(self._lastlock)
            except:
                pass
            self._lastlock = None

    def tryLock(self):
        """
        simple lock mechanisn to avoid multiple processes on the same cache to
        step on each other's toes.

        @rtype: boolean
        @return: True if we created a lockfile or already had it. False
                 otherwise.
        """
        lockfile = os.path.join(
            self._basedir, '%s_lock'
            % datetime.datetime.now().strftime('%Y%m%d_%H%M'))
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

    def handleEvent(self, client_base, e):
        """
        Handle one event from blitz.onEventLogs.

        Meant to be overridden, this implementation just logs.

        @param client_base:     TODO: docs!
        @param e:
        """
        logger.debug('## %s#%i %s user #%i group #%i(%i)' % (
            e.entityType.val, e.entityId.val, e.action.val,
            e.details.owner.id.val, e.details.group.id.val, e.event.id.val))

    def eventListener(self, client_base, events):
        """
        handle events coming our way from blitz.onEventLogs.

        Because all processes will be listening to the same events, we use a
        simple file lock mechanism to make sure the first process to get the
        event will be the one handling things from then on.

        @param client_base:     TODO: docs!
        @param events:
        """
        for e in events:
            if self.tryLock():
                self.handleEvent(client_base, e)
            else:
                logger.debug("## ! ignoring event %s" % str(e.event.id.val))

    def clear(self):
        """
        Clears all the caches.
        """
        self._json_cache.wipe()
        self._img_cache.wipe()
        self._thumb_cache.wipe()

    def _cache_set(self, cache, key, obj):
        """ Calls cache.set(key, obj) """

        logger.debug('   set: %s' % key)
        cache.set(key, obj)

    def _cache_clear(self, cache, key):
        """ Calls cache.delete(key) """

        logger.debug(' clear: %s' % key)
        cache.delete(key)

    def invalidateObject(self, client_base, user_id, obj):
        """
        Invalidates all caches for this particular object

        @param client_base:     The server_id
        @param user_id:         OMERO user ID to partition caching upon
        @param obj:             The object wrapper. E.g.
                                L{omero.gateway.ImageWrapper}
        """

        if obj.OMERO_CLASS == 'Image':
            self.clearImage(None, client_base, user_id, obj)
        else:
            logger.debug('unhandled object type: %s' % obj.OMERO_CLASS)
            self.clearJson(client_base, obj)

    ##
    # Thumb

    def _thumbKey(self, r, client_base, user_id, iid, size):
        """
        Generates a string key for caching the thumbnail, based on the above
        parameters

        @param r:       not used
        @param client_base:     server-id, forms stem of the key
        @param user_id:         OMERO user ID to partition caching upon
        @param iid:             image ID
        @param size:            size of the thumbnail - tuple. E.g. (100,)
        """
        pre = str(iid)[:-4]
        if len(pre) == 0:
            pre = '0'
        if size is not None and len(size):
            return 'thumb_user_%s/%s/%s/%s/%s' % (
                client_base, pre, str(iid), user_id,
                'x'.join([str(x) for x in size]))
        else:
            return 'thumb_user_%s/%s/%s/%s' % (
                client_base, pre, str(iid), user_id)

    def setThumb(self, r, client_base, user_id, iid, obj, size=()):
        """
        Puts thumbnail into cache.

        @param r:               for cache key - Not used?
        @param client_base:     server_id for cache key
        @param user_id:         OMERO user ID to partition caching upon
        @param iid:             image ID for cache key
        @param obj:             Data to cache
        @param size:            Size used for cache key. Tuple
        """

        k = self._thumbKey(r, client_base, user_id, iid, size)
        self._cache_set(self._thumb_cache, k, obj)
        return True

    def getThumb(self, r, client_base, user_id, iid, size=()):
        """
        Gets thumbnail from cache.

        @param r:               for cache key - Not used?
        @param client_base:     server_id for cache key
        @param user_id:         OMERO user ID to partition caching upon
        @param iid:             image ID for cache key
        @param size:            Size used for cache key. Tuple
        @return:                Cached data or None
        @rtype:                 String
        """

        k = self._thumbKey(r, client_base, user_id, iid, size)
        r = self._thumb_cache.get(k)
        if r is None:
            logger.debug('  fail: %s' % k)
        else:
            logger.debug('cached: %s' % k)
        return r

    def clearThumb(self, r, client_base, user_id, iid, size=None):
        """
        Clears thumbnail from cache.

        @param r:               for cache key - Not used?
        @param client_base:     server_id for cache key
        @param user_id:         OMERO user ID to partition caching upon
        @param iid:             image ID for cache key
        @param size:            Size used for cache key. Tuple
        @return:                True
        """
        k = self._thumbKey(r, client_base, user_id, iid, size)
        self._cache_clear(self._thumb_cache, k)
        return True

    ##
    # Image

    def _imageKey(self, r, client_base, img, z=0, t=0):
        """
        Returns a key for caching the Image, based on parameters above,
        including rendering settings specified in the http request.

        @param r:               http request - get rendering params 'c', 'm',
                                'p'
        @param client_base:     server_id for cache key
        @param img:             L{omero.gateway.ImageWrapper} for ID
        @param obj:             Data to cache
        @param size:            Size used for cache key. Tuple
        """

        iid = img.getId()
        pre = str(iid)[:-4]
        if len(pre) == 0:
            pre = '0'
        if r:
            r = r.REQUEST
            c = FN_REGEX.sub('-', r.get('c', ''))
            m = r.get('m', '')
            p = r.get('p', '')
            if p and not isinstance(
                    omero.gateway.ImageWrapper.PROJECTIONS.get(p, -1),
                    omero.constants.projection.ProjectionType
                    ):  # pragma: nocover
                p = ''
            q = r.get('q', '')
            region = r.get('region', '')
            tile = r.get('tile', '')
            rv = 'img_%s/%s/%s/%%s-c%s-m%s-q%s-r%s-t%s' % (
                client_base, pre, str(iid), c, m, q, region, tile)
            if p:
                return rv % ('%s-%s' % (p, str(t)))
            else:
                logger.info('rv: {0} {1}'.format(rv, type(rv)))
                logger.info('z: {0} {1}'.format(str(z), type(z)))
                logger.info('t: {0} {1}'.format(str(t), type(t)))
                zt = '%sx%s' % (str(z), str(t))
                return rv % (zt)
        else:
            return 'img_%s/%s/%s' % (client_base, pre, str(iid))

    def setImage(self, r, client_base, img, z, t, obj, ctx=''):
        """
        Puts image data into cache.

        @param r:               http request for cache key
        @param client_base:     server_id for cache key
        @param img:             ImageWrapper for cache key
        @param z:               Z index for cache key
        @param t:               T index for cache key
        @param obj:             Data to cache
        @param ctx:             Additional string for cache key
        """

        k = self._imageKey(r, client_base, img, z, t) + ctx
        self._cache_set(self._img_cache, k, obj)
        return True

    def getImage(self, r, client_base, img, z, t, ctx=''):
        """
        Gets image data from cache.

        @param r:               http request for cache key
        @param client_base:     server_id for cache key
        @param img:             ImageWrapper for cache key
        @param z:               Z index for cache key
        @param t:               T index for cache key
        @param ctx:             Additional string for cache key
        @return:                Image data
        @rtype:                 String
        """
        k = self._imageKey(r, client_base, img, z, t) + ctx
        r = self._img_cache.get(k)
        if r is None:
            logger.debug('  fail: %s' % k)
        else:
            logger.debug('cached: %s' % k)
        return r

    def clearImage(self, r, client_base, user_id, img, skipJson=False):
        """
        Clears image data from cache using default rendering settings (r=None)
        T and Z indexes ( = 0).
        TODO: Doesn't clear any data stored WITH r, t, or z specified in cache
        key?
        Also clears thumbnail (but not thumbs with size specified) and json
        data for this image.

        @param r:               http request for cache key
        @param client_base:     server_id for cache key
        @param user_id:         OMERO user ID to partition caching upon
        @param img:             ImageWrapper for cache key
        @param obj:             Data to cache
        @param rtype:           True
        """

        k = self._imageKey(None, client_base, img)
        self._cache_clear(self._img_cache, k)
        # do the thumb too
        self.clearThumb(r, client_base, user_id, img.getId())
        # and json data
        if not skipJson:
            self.clearJson(client_base, img)
        return True

    def setSplitChannelImage(self, r, client_base, img, z, t, obj):
        """ Calls L{setImage} with '-sc' context """
        return self.setImage(r, client_base, img, z, t, obj, '-sc')

    def getSplitChannelImage(self, r, client_base, img, z, t):
        """
        Calls L{getImage} with '-sc' context
        @rtype:     String
        """
        return self.getImage(r, client_base, img, z, t, '-sc')

    def setOmeTiffImage(self, r, client_base, img, obj):
        """ Calls L{setImage} with '-ometiff' context """
        return self.setImage(r, client_base, img, 0, 0, obj, '-ometiff')

    def getOmeTiffImage(self, r, client_base, img):
        """
        Calls L{getImage} with '-ometiff' context
        @rtype:     String
        """
        return self.getImage(r, client_base, img, 0, 0, '-ometiff')

    ##
    # hierarchies (json)

    def _jsonKey(self, r, client_base, obj, ctx=''):
        """
        Creates a cache key for storing json data based on params above.

        @param r:               http request - not used
        @param client_base:     server_id
        @param obj:             ObjectWrapper
        @param ctx:             Additional string for cache key
        @return:                Cache key
        @rtype:                 String
        """

        if obj:
            return 'json_%s/%s_%s/%s' % (client_base, obj.OMERO_CLASS, obj.id,
                                         ctx)
        else:
            return 'json_%s/single/%s' % (client_base, ctx)

    def getJson(self, r, client_base, obj, ctx=''):
        """
        Gets data from the json cache

        @param r:               http request - not used
        @param client_base:     server_id for cache key
        @param obj:             ObjectWrapper for cache key
        @param ctx:             context string used for cache key
        @rtype:                 String or None
        """
        k = self._jsonKey(r, client_base, obj, ctx)
        r = self._json_cache.get(k)
        if r is None:
            logger.debug('  fail: %s' % k)
        else:
            logger.debug('cached: %s' % k)
        return r

    def setJson(self, r, client_base, obj, data, ctx=''):
        """
        Adds data to the json cache

        @param r:               http request - not used
        @param client_base:     server_id for cache key
        @param obj:             ObjectWrapper for cache key
        @param data:            Data to cache
        @param ctx:             context string used for cache key
        @rtype:                 True
        """
        k = self._jsonKey(r, client_base, obj, ctx)
        self._cache_set(self._json_cache, k, data)
        return True

    def clearJson(self, client_base, obj, ctx=''):
        """
        TODO: document
        WAS: Only handles Dataset obj, calling L{clearDatasetContents}
        """
        k = self._jsonKey(None, client_base, obj, ctx)
        self._cache_clear(self._json_cache, k)
        return True
        # logger.debug('clearjson')
        # if obj.OMERO_CLASS == 'Dataset':
        #    self.clearDatasetContents(None, client_base, obj)

    def setDatasetContents(self, r, client_base, ds, data):
        """
        Adds data to the json cache using 'contents' as context

        @param r:               http request - not used
        @param client_base:     server_id for cache key
        @param ds:              ObjectWrapper for cache key
        @param data:            Data to cache
        @rtype:                 True
        """
        return self.setJson(r, client_base, ds, data, 'contents')

    def getDatasetContents(self, r, client_base, ds):
        """
        Gets data from the json cache using 'contents' as context

        @param r:               http request - not used
        @param client_base:     server_id for cache key
        @param ds:              ObjectWrapper for cache key
        @rtype:                 String or None
        """
        return self.getJson(r, client_base, ds, 'contents')

    def clearDatasetContents(self, r, client_base, ds):
        """
        Clears data from the json cache using 'contents' as context

        @param r:               http request - not used
        @param client_base:     server_id for cache key
        @param ds:              ObjectWrapper for cache key
        @rtype:                 True
        """

        k = self._jsonKey(r, client_base, ds, 'contents')
        self._cache_clear(self._json_cache, k)
        return True

webgateway_cache = WebGatewayCache(FileCache)


class AutoLockFile (file):
    """
    Class extends file to facilitate creation and deletion of lock file.
    """

    def __init__(self, fn, mode):
        """ creates a '.lock' file with the spicified file name and mode """
        super(AutoLockFile, self).__init__(fn, mode)
        self._lock = os.path.join(os.path.dirname(fn), '.lock')
        file(self._lock, 'a').close()

    def __del__(self):
        """ tries to delete the lock file """
        try:
            os.remove(self._lock)
        except:
            pass

    def close(self):
        """ tries to delete the lock file and close the file """
        try:
            os.remove(self._lock)
        except:
            pass
        super(AutoLockFile, self).close()


class WebGatewayTempFile (object):
    """
    Class for handling creation of temporary files
    """

    def __init__(self, tdir=TMPROOT):
        """
        Initialises class, setting the directory to be used for temp files.
        """
        self._dir = tdir
        if tdir and not os.path.exists(self._dir):
            self._createdir()

    def _createdir(self):
        """
        Tries to create the directories required for the temp file base dir
        """
        try:
            os.makedirs(self._dir)
        except OSError:  # pragma: nocover
            raise EnvironmentError("Cache directory '%s' does not exist and"
                                   " could not be created'" % self._dir)

    def _cleanup(self):
        """
        Tries to delete all the temp files that have expired their cache
        timeout.
         """
        now = time.time()
        for f in os.listdir(self._dir):
            try:
                ts = os.path.join(self._dir, f, '.timestamp')
                if os.path.exists(ts):
                    ft = float(file(ts).read()) + TMPDIR_TIME
                else:
                    ft = float(f) + TMPDIR_TIME
                if ft < now:
                    shutil.rmtree(os.path.join(self._dir, f),
                                  ignore_errors=True)
            except ValueError:
                continue

    def newdir(self, key=None):
        """
        Creates a new directory using key as the dir name, and adds a
        timestamp file with its creation time. If key is not specified, use a
        unique key based on timestamp.

        @param key:     The new dir name
        @return:        Tuple of (path to new directory, key used)
        """

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
        key = key.replace('/', '_').decode('utf8').encode('ascii', 'ignore')
        dn = os.path.join(self._dir, key)
        if not os.path.isdir(dn):
            os.makedirs(dn)
        file(os.path.join(dn, '.timestamp'), 'w').write(stamp)
        return dn, key

    def abort(self, fn):
        logger.debug(fn)
        logger.debug(os.path.dirname(fn))
        logger.debug(self._dir)
        if fn.startswith(self._dir):
            shutil.rmtree(os.path.dirname(fn), ignore_errors=True)

    def new(self, name, key=None):
        """
        Creates a new directory if needed, see L{newdir} and checks whether
        this contains a file 'name'. If not, a file lock is created for this
        location and returned.

        @param name:    Name of file we want to create.
        @param key:     The new dir name
        @return:        Tuple of (abs path to new directory, relative path
                        key/name, L{AutoFileLock} or True if exists)
        """

        if not self._dir:
            return None, None, None
        dn, stamp = self.newdir(key)
        name = name.replace('/', '_').replace('#', '_').decode(
            'utf8').encode('ascii', 'ignore')
        if len(name) > 255:
            # Try to be smart about trimming and keep up to two levels of
            # extension (ex: .ome.tiff)
            # We do limit the extension to 16 chars just to keep things sane
            fname, fext = os.path.splitext(name)
            if fext:
                if len(fext) <= 16:
                    fname, fext2 = os.path.splitext(fname)
                    if len(fext+fext2) <= 16:
                        fext = fext2 + fext
                    else:
                        fname += fext2
                else:
                    fname = name
                    fext = ''
            name = fname[:-len(name)+255] + fext
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
