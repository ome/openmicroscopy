"""
    OMERO.fs Abstract Monitor module.


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
        

            
