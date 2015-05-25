#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs Monitor module for Linux.

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""
import logging
import copy
import time

# Third party path package. It provides much of the
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid potential clashes.
import path as pathModule

__import__("omero.all")
import omero.grid.monitors as monitors
from fsAbstractPlatformMonitor import AbstractPlatformMonitor

importlog = logging.getLogger("fsserver." + __name__)
from omero_ext import pyinotify
importlog.info("Imported pyinotify version %s", str(pyinotify.__version__))


class PlatformMonitor(AbstractPlatformMonitor):

    """
        A Thread to monitor a path.

        :group Constructor: __init__
        :group Other methods: run, stop

    """

    def __init__(self, eventTypes, pathMode, pathString, whitelist, blacklist,
                 ignoreSysFiles, ignoreDirEvents, proxy):
        """
            Set-up Monitor thread.

            After initialising the superclass and some instance variables
            try to create an FSEventStream. Throw an exeption if this fails.

            :Parameters:
                eventTypes :
                    A list of the event types to be monitored.

                pathMode :
                    The mode of directory monitoring:
                    flat, recursive or following.

                pathString : string
                    A string representing a path to be monitored.

                whitelist : list<string>
                    A list of files and extensions of interest.

                blacklist : list<string>
                    A list of subdirectories to be excluded.

                ignoreSysFiles :
                    If true platform dependent sys files should be ignored.

                monitorId :
                    Unique id for the monitor included in callbacks.

                proxy :
                    A proxy to be informed of events

        """
        AbstractPlatformMonitor.__init__(
            self, eventTypes, pathMode, pathString, whitelist, blacklist,
            ignoreSysFiles, ignoreDirEvents, proxy)
        self.log = logging.getLogger("fsserver." + __name__)

        recurse = False
        follow = False
        if self.pathMode == 'Follow':
            recurse = True
            follow = True
        elif self.pathMode == 'Recurse':
            recurse = True

        self.wm = MyWatchManager()
        self.notifier = pyinotify.ThreadedNotifier(
            self.wm, ProcessEvent(
                wm=self.wm, cb=self.propagateEvents, et=self.eTypes,
                ignoreDirEvents=self.ignoreDirEvents))
        try:
            self.wm.addBaseWatch(
                self.pathsToMonitor, (pyinotify.ALL_EVENTS), rec=recurse,
                auto_add=follow)
            self.log.info('Monitor set-up on %s', str(self.pathsToMonitor))
            self.log.info('Monitoring %s events', str(self.eTypes))
        except:
            self.log.error('Monitor failed on: %s', str(self.pathsToMonitor))

    def start(self):
        """
            Start monitoring an FSEventStream.

            This method, overridden from Thread, is run by
            calling the inherited method start(). The method attempts
            to schedule an FSEventStream and then run its CFRunLoop.
            The method then blocks until stop() is called.

            :return: No explicit return value.

        """

        # Blocks
        self.notifier.start()

    def stop(self):
        """
            Stop monitoring an FSEventStream.

            This method attempts to stop the CFRunLoop. It then
            stops, invalidates and releases the FSEventStream.

            There should be a more robust approach in here that
            still kils the thread even if the first call fails.

            :return: No explicit return value.

        """
        self.notifier.stop()


class MyWatchManager(pyinotify.WatchManager):

    """
       Essentially a limited-function wrapper to WatchManager

       At present only one watch is allowed on each path. This
       may need to be fixed for applications outside of DropBox.

    """
    watchPaths = {}
    watchParams = {}
    log = logging.getLogger("fsserver." + __name__)

    def isPathWatched(self, pathString):
        return pathString in self.watchPaths.keys()

    def addBaseWatch(self, path, mask, rec=False, auto_add=False):
        try:
            res = pyinotify.WatchManager.add_watch(
                self, path, mask, rec=False, auto_add=False, quiet=False)
            self.watchPaths.update(res)
            self.watchParams[path] = WatchParameters(
                mask, rec=rec, auto_add=auto_add)
            self.log.info('Base watch created on: %s', path)
            if rec:
                for d in pathModule.path(path).dirs():
                    self.addWatch(str(d), mask)
        except Exception, e:
            self.log.error(
                'Unable to create base watch on: %s : %s', path, str(e))
            raise e

    def addWatch(self, path, mask):
        if not self.isPathWatched(path):
            try:
                res = pyinotify.WatchManager.add_watch(
                    self, path, mask, rec=False, auto_add=False, quiet=False)
                self.watchPaths.update(res)
                self.watchParams[path] = copy.copy(
                    self.watchParams[pathModule.path(path).parent])
                if self.watchParams[path].getRec():
                    for d in pathModule.path(path).dirs():
                        self.addWatch(str(d), mask)
                if self.isPathWatched(path):
                    self.log.info('Watch added on: %s', path)
                else:
                    self.log.info('Unable to add watch on: %s', path)
            except Exception, e:
                self.log.error(
                    'Unable to add watch on: %s : %s', path, str(e))

    def removeWatch(self, path):
        if self.isPathWatched(path):
            try:
                removeDict = {}
                self.log.info('Trying to remove : %s', path)
                removeDict[self.watchPaths[path]] = path
                for d in self.watchPaths.keys():
                    if d.find(path + '/') == 0:
                        self.log.info('    ... and : %s', d)
                        removeDict[self.watchPaths[d]] = d
                res = pyinotify.WatchManager.rm_watch(
                    self, removeDict.keys(), quiet=False)
                for wd in res.keys():
                    if res[wd]:
                        self.watchPaths.pop(removeDict[wd], True)
                        self.watchParams.pop(removeDict[wd], True)
                        self.log.info('Watch removed on: %s', removeDict[wd])
                    else:
                        self.log.info(
                            'Watch remove failed, wd=%s, on: %s',
                            wd, removeDict[wd])
            except Exception, e:
                self.log.error(
                    'Unable to remove watch on: %s : %s', path, str(e))

    def getWatchPaths(self):
        for (path, wd) in self.watchPaths.items():
            yield (path, wd)


class WatchParameters(object):

    def __init__(self, mask, rec=False, auto_add=False):
        self.mask = mask
        self.rec = rec
        self.auto_add = auto_add

    def getMask(self):
        return self.mask

    def getRec(self):
        return self.rec

    def getAutoAdd(self):
        return self.auto_add


class ProcessEvent(pyinotify.ProcessEvent):

    def __init__(self, **kwargs):
        pyinotify.ProcessEvent.__init__(self)
        self.log = logging.getLogger("fsserver." + __name__)
        self.wm = kwargs['wm']
        self.cb = kwargs['cb']
        self.et = kwargs['et']
        self.ignoreDirEvents = kwargs['ignoreDirEvents']
        self.waitingCreates = set([])

    def process_default(self, event):

        try:
            # pyinotify 0.8
            name = event.pathname
            maskname = event.maskname
        except:
            # pyinotify 0.7 or below
            name = pathModule.path(event.path) / pathModule.path(event.name)
            maskname = event.event_name

        el = []

        # This is a tricky one. I'm not sure why these events arise exactly.
        # It seems to happen when inotify isn't sure of the path. Yet all the
        # events seem to have otherwise good paths. So, remove the suffix and
        # allow the event to be dealt with as normal.
        if name.find('-unknown-path') > 0:
            self.log.debug(
                'Event with "-unknown-path" of type %s : %s', maskname, name)
            name = name.replace('-unknown-path', '')

        # New directory within watch area,
        # either created, moved in or modfied attributes, ie now readable.
        if (event.mask == (pyinotify.IN_CREATE | pyinotify.IN_ISDIR)
                or event.mask == (pyinotify.IN_MOVED_TO | pyinotify.IN_ISDIR)
                or event.mask == (pyinotify.IN_ATTRIB | pyinotify.IN_ISDIR)):
            self.log.info(
                'New directory event of type %s at: %s', maskname, name)
            if "Creation" in self.et:
                if name.find('untitled folder') == -1:
                    if not self.ignoreDirEvents:
                        el.append((name, monitors.EventType.Create))
                    else:
                        self.log.info('Not propagated.')
                    # Handle the recursion plus create any potentially missed
                    # Create events
                    if self.wm.watchParams[
                            pathModule.path(name).parent].getAutoAdd():
                        self.wm.addWatch(
                            name, self.wm.watchParams[
                                pathModule.path(name).parent].getMask())
                        if self.wm.isPathWatched(name):
                            if self.wm.watchParams[
                                    pathModule.path(name).parent].getRec():
                                for d in pathModule.path(name).walkdirs(
                                        errors='warn'):
                                    self.log.info(
                                        ('NON-INOTIFY event: '
                                         'New directory at: %s'),
                                        str(d))
                                    if not self.ignoreDirEvents:
                                        el.append((
                                            str(d),
                                            monitors.EventType.Create))
                                    else:
                                        self.log.info('Not propagated.')
                                    self.wm.addWatch(
                                        str(d), self.wm.watchParams[
                                            pathModule.path(
                                                name).parent].getMask())
                                for f in pathModule.path(name).walkfiles(
                                        errors='warn'):
                                    self.log.info(
                                        'NON-INOTIFY event: New file at: %s',
                                        str(f))
                                    el.append(
                                        (str(f), monitors.EventType.Create))
                            else:
                                for d in pathModule.path(name).dirs():
                                    self.log.info(
                                        ('NON-INOTIFY event: '
                                         'New directory at: %s'),
                                        str(d))
                                    if not self.ignoreDirEvents:
                                        el.append((
                                            str(d),
                                            monitors.EventType.Create))
                                    else:
                                        self.log.info('Not propagated.')
                                for f in pathModule.path(name).files():
                                    self.log.info(
                                        'NON-INOTIFY event: New file at: %s',
                                        str(f))
                                    el.append(
                                        (str(f), monitors.EventType.Create))
                else:
                    self.log.info('Created "untitled folder" ignored.')
            else:
                self.log.info('Not propagated.')

        # Deleted directory or one moved out of the watch area.
        elif (event.mask == (pyinotify.IN_MOVED_FROM | pyinotify.IN_ISDIR)
                or event.mask == (pyinotify.IN_DELETE | pyinotify.IN_ISDIR)):
            self.log.info(
                'Deleted directory event of type %s at: %s', maskname, name)
            if "Deletion" in self.et:
                if name.find('untitled folder') == -1:
                    if not self.ignoreDirEvents:
                        el.append((name, monitors.EventType.Delete))
                    else:
                        self.log.info('Not propagated.')
                    self.log.info(
                        'Files and subfolders within %s may have been deleted '
                        'without notice', name)
                    self.wm.removeWatch(name)
                else:
                    self.log.info('Deleted "untitled folder" ignored.')
            else:
                self.log.info('Not propagated.')

        # New file within watch area, either created or moved into.
        # The file may have been created but it may not be complete and closed.
        # Modifications should be watched
        elif event.mask == pyinotify.IN_CREATE:
            self.log.info('New file event of type %s at: %s', maskname, name)
            if "Creation" in self.et:
                self.waitingCreates.add(name)
            else:
                self.log.info('Not propagated.')

        # New file within watch area.
        elif event.mask == pyinotify.IN_MOVED_TO:
            self.log.info('New file event of type %s at: %s', maskname, name)
            if "Creation" in self.et:
                el.append((name, monitors.EventType.Create))
            else:
                self.log.info('Not propagated.')

        # Modified file within watch area.
        elif event.mask == pyinotify.IN_CLOSE_WRITE:
            self.log.info(
                'Modified file event of type %s at: %s', maskname, name)
            if name in self.waitingCreates:
                if "Creation" in self.et:
                    el.append((name, monitors.EventType.Create))
                    self.waitingCreates.remove(name)
                else:
                    self.log.info('Not propagated.')
            else:
                if "Modification" in self.et:
                    el.append((name, monitors.EventType.Modify))
                else:
                    self.log.info('Not propagated.')

        # Modified file within watch area, only notify if file is not
        # waitingCreate.
        elif event.mask == pyinotify.IN_MODIFY:
            self.log.info(
                'Modified file event of type %s at: %s', maskname, name)
            if name not in self.waitingCreates:
                if "Modification" in self.et:
                    el.append((name, monitors.EventType.Modify))
                else:
                    self.log.info('Not propagated.')

        # Deleted file  or one moved out of the watch area.
        elif (event.mask == pyinotify.IN_MOVED_FROM
                or event.mask == pyinotify.IN_DELETE):
            self.log.info(
                'Deleted file event of type %s at: %s', maskname, name)
            if "Deletion" in self.et:
                el.append((name, monitors.EventType.Delete))
            else:
                self.log.info('Not propagated.')

        # These are all the currently ignored events.
        elif event.mask == pyinotify.IN_ATTRIB:
            # File attributes have changed? Useful?
            self.log.debug('Ignored event of type %s at: %s', maskname, name)
        elif (event.mask == pyinotify.IN_DELETE_SELF
                or event.mask == pyinotify.IN_IGNORED):
            # This is when a directory being watched is removed, handled above.
            self.log.debug('Ignored event of type %s at: %s', maskname, name)
        elif event.mask == pyinotify.IN_MOVE_SELF:
            # This is when a directory being watched is moved out of the watch
            # area (itself!), handled above.
            self.log.debug('Ignored event of type %s at: %s', maskname, name)
        elif event.mask == pyinotify.IN_OPEN | pyinotify.IN_ISDIR:
            # Event, dir open, we can ignore for now to reduce the log volume
            # at any rate.
            self.log.debug('Ignored event of type %s at: %s', maskname, name)
        elif event.mask == pyinotify.IN_CLOSE_NOWRITE | pyinotify.IN_ISDIR:
            # Event, dir close, we can ignore for now to reduce the log volume
            # at any rate.
            self.log.debug('Ignored event of type %s at: %s', maskname, name)
        elif event.mask == pyinotify.IN_ACCESS | pyinotify.IN_ISDIR:
            # Event, dir access, we can ignore for now to reduce the log volume
            # at any rate.
            self.log.debug('Ignored event of type %s at: %s', maskname, name)
        elif event.mask == pyinotify.IN_OPEN:
            # Event, file open, we can ignore for now to reduce the log volume
            # at any rate.
            self.log.debug('Ignored event of type %s at: %s', maskname, name)
        elif event.mask == pyinotify.IN_CLOSE_NOWRITE:
            # Event, file close, we can ignore for now to reduce the log volume
            # at any rate.
            self.log.debug('Ignored event of type %s at: %s', maskname, name)
        elif event.mask == pyinotify.IN_ACCESS:
            # Event, file access, we can ignore for now to reduce the log
            # volume at any rate.
            self.log.debug('Ignored event of type %s at: %s', maskname, name)

        # Other events, log them since they really should be caught above
        else:
            self.log.info('Uncaught event of type %s at: %s', maskname, name)
            self.log.info('Not propagated.')

        if len(el) > 0:
            self.cb(el)


if __name__ == "__main__":
    class Proxy(object):
        def callback(self, eventList):
            for event in eventList:
                # pass
                log.info("EVENT_RECORD::%s::%s::%s" %
                         (time.time(), event[1], event[0]))

    log = logging.getLogger("fstestserver")
    file_handler = logging.FileHandler("/TEST/logs/fstestserver.out")
    file_handler.setFormatter(
        logging.Formatter("%(asctime)s %(levelname)s: %(name)s - %(message)s"))
    log.addHandler(file_handler)
    log.setLevel(logging.INFO)
    log = logging.getLogger("fstestserver." + __name__)

    p = Proxy()
    m = PlatformMonitor(
        [monitors.WatchEventType.Creation,
         monitors.WatchEventType.Modification],
        monitors.PathMode.Follow, "\OMERO\DropBox", [], [], True, True, p)
    try:
        m.start()
    except:
        m.stop()
