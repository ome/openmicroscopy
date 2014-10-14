#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs Util module.

    Copyright 2012 University of Dundee. All rights reserved.
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
    #     * Linux kernel 2.6 then .13 or higher or kernel 3.x.y
    #     * Windows: XP, 2003Server, 2008Server, 2008ServerR2, Vista, and 7
    #
    # Some fine-tuning may need to be applied, some additional Windows platforms added.
    # If any platform-specific stuff in the imported library fails an exception will be
    # raised, a further sanity check.
    #
    # Currently supported platforms
    supported = {
                  'MACOS_10_5+'            : 'fsMac-10-5-Monitor',
                  'LINUX_2_6_13+pyinotify' : 'fsPyinotifyMonitor',
                  'WIN_XP'                 : 'fsWin-XP-Monitor',
                  'WIN_2003Server'         : 'fsWin-XP-Monitor',
                  'WIN_2008Server'         : 'fsWin-XP-Monitor',
                  'WIN_2008ServerR2'       : 'fsWin-XP-Monitor',
                  'WIN_Vista'              : 'fsWin-XP-Monitor',
                  'WIN_7'                  : 'fsWin-XP-Monitor',
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
                errorString = "Mac Os 10.5 or above required. You have: %s" % platform.platform()
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
        if int(kernel[0]) >= 3 or (int(kernel[0]) == 2 and int(kernel[1]) == 6 and int(kernel[2]) >= 13):
            current = 'LINUX_2_6_13+pyinotify'
        # Unsupported Linux kernel version.    
        else:
            errorString = "Linux kernel 2.6.13 or above required. "
            errorString += "You have: %s" % platform.platform()

    # Windows of some flavour.
    elif system == 'Windows':
        version = platform.platform().split('-')
        if version[1] == 'XP':
            current = 'WIN_XP'
        elif version[1] == '2003Server':
            current = 'WIN_2003Server'
        elif version[1] == '2008Server':
            current = 'WIN_2008Server'
        elif version[1] == '2008ServerR2':
            current = 'WIN_2008ServerR2'
        elif version[1] == 'Vista':
            current = 'WIN_Vista'
        elif version[1] == '7':
            current = 'WIN_7'
        else:
            errorString = "Windows XP, Vista, 7 or Server 2003, 2008 or 2008R2 required. You have: %s" % platform.platform()

    # Unknown OS.
    else:
        errorString = "Unsupported platform: %s" % system
    
    try:
        return supported[current]
    except:
        raise Exception("Libraries required by OMERO.fs monitor unavailable: " + errorString)
    
