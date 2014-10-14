#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs Abstract Monitor module.

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""
import threading

class AbstractPlatformMonitor(threading.Thread):
    """
        A Thread to monitor a path.
        
        :group Constructor: __init__
        :group Other methods: run, stop

    """

    def __init__(self, eventTypes, pathMode, pathString, whitelist, blacklist, ignoreSysFiles, ignoreDirEvents, proxy):
        """
            Set-up Monitor thread.
        """
        threading.Thread.__init__(self)
        
        self.eTypes = []
        for eT in eventTypes:
            self.eTypes.append(str(eT))

        self.pathMode = str(pathMode)
        self.pathsToMonitor = pathString
        self.whitelist = whitelist
        self.blacklist = blacklist
        self.ignoreSysFiles = ignoreSysFiles
        self.ignoreDirEvents = ignoreDirEvents
        self.proxy = proxy
                
    def run(self):
        """
            Start monitoring.
            
            :return: No explicit return value.
        """
        # pass
        
    def stop(self):        
        """
            Stop monitoring 
            
            :return: No explicit return value.
        """
        # pass
        
    def propagateEvents(self, eventList):
        """
            Propagate events to proxy.
        
            :Parameters:
                    
                eventPath : List
                    events.
                    
            :return: No explicit return value.
            
        """
        if len(eventList) > 0:
            try:          
                self.log.info('Event notification : %s', str(eventList))
                self.proxy.callback(eventList)
            except:
                self.log.exception("Notification failed : ")
        else:
            self.log.info('No notifications propagated')    


            
