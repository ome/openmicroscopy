"""
    OMERO.fs Util module.

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""

def monitorPackage():
    """
        Helper function to determine correct package to load for platform. 
        
    """
    
    # This sequence tries to check the platform and OS version. 
    #
    # At the moment a limited subset of platforms is checked for:
    #     * Mac OS 10.5 or higher
    #     * Linux kernel 2.6 then .13 or higher, ie 2.8 would fail.
    #     * Windows XP no other flavours at present, XP version irrelevant
    #
    # Some fine-tuning may need to be applied, some additional Windows platforms added.
    # If any platform-specific stuff in the imported library fails an exception will be
    # raised, a further sanity check.
    #
    # Currently supported platforms
    supported = { 
                  'MACOS_10_5+'                : 'fsMac-10-5-Monitor', 
                  'LINUX_2_6_13+pyinotify_0_7' : 'fsPyinotifyMonitor', 
                  'LINUX_2_6_13+pyinotify_0_8' : 'fsPyinotifyMonitor', 
                  'WIN_XP'                     : 'fsWin-XP-Monitor', 
                  'WIN_2003Server'             : 'fsWin-XP-Monitor', 
                  'UNKNOWN'                    : 'fsDummyMonitor', 
                }
    
    # Initial state
    current = 'UNKNOWN'
    errorString = 'Unknown error'
    
    # Determine the OS, then the version of that OS, 
    # and if necessary the version of any required packages.
    import platform
    system = platform.system()
    
    # Mac OS of some flavour.
    if system == 'Darwin':
        version = platform.mac_ver()[0].split('.')
        try:
            # Supported Mac OS version.
            if  int(version[0]) == 10 and int(version[1]) >= 5:
                current = 'MACOS_10_5+'
            # Unsupported Mac OS version.
            else:
                errorString = "Mac Os 10.5 or above required. You have: %s" % str(platform.mac_ver()[0])
        except:
            # mac_ver() on python built with macports returns a version tuple
            # full of empty strings. That's caught here but the OS version is unknown.
            # Until a better solution is found MACOS-UNKNOWN_VERSION is used to flag this.
            current = 'MACOS-UNKNOWN_VERSION'
            errorString = "Mac Os 10.5 or above required. You have an unkown version"

    # Linux of some flavour.    
    elif system == 'Linux':
        kernel = platform.platform().split('-')[1].split('.')
        # Supported Linux kernel version.
        if int(kernel[0]) == 2 and int(kernel[1]) == 6 and int(kernel[2]) >= 13:
            try:
                # pyinotify versions have slightly different APIs
                # so the version needs to be determined. They also
                # interact differently with different python versions
                # so the python version is also needed.
                import pyinotify
                import sys
                try:
                    # 0.8.x has a __version__ attribute.
                    version = pyinotify.__version__.split('.')
                    if int(version[0]) == 0 and int(version[1]) == 8:
                        try:
                            pyinotify.PyinotifyLogger
                            current = 'LINUX_2_6_13+pyinotify_0_8'
                        except AttributeError:
                            if sys.version[:3] == '2.5':
                                current = 'LINUX_2_6_13+pyinotify_0_8'
                            else:
                                errorString = "pynotify version %s is not compatible with Python 2.4. Install 0.8.5 or lower to use DropBox" % pyinotify.__version__ 
                    # This pyinotofy has a __version__ attribute but isn't 0.8.
                    else:
                        errorString = "pyinotify 0.7 or 0.8 required. Unknown version found."
                except:
                    # 0.7.x doesn't have a __version__ attribute but there is
                    # a possibility that the installed version is 0.6 or less.
                    # That isn't tested for and might be a point of failure.
                    current = 'LINUX_2_6_13+pyinotify_0_7'
            except:
                errorString = "pyinotify 0.7 or 0.8 required. Package not found."
        # Unsupported Linux kernel version.    
        else:
            errorString = "Linux kernel 2.6.13 or above required. "
            errorString += "You have: %s" % str(platform.platform().split('-')[1])

    # Windows of some flavour.
    elif system == 'Windows':
        version = platform.platform().split('-')
        if version[1] == 'XP':
            current = 'WIN_XP'
        elif version[1] == '2003Server':
            current = 'WIN_2003Server'
        else:
            errorString = "Windows XP or 2003Server required. You have: %s" % str(version)

    # Unknown OS.
    else:
        errorString = "Unsupported platform: %s" % system
    
    return supported[current], errorString
