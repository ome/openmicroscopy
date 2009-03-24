"""
    OMERO.fs Util module.


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
                  'LINUX_2_6_13+pyinotify_0_7' : 'fsPyinotify-0-7-Monitor', 
                  'LINUX_2_6_13+pyinotify_0_8' : 'fsPyinotify-0-8-Monitor', 
                  'WIN_XP'                     : 'fsWin-XP-Monitor', 
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
            ## mac_ver() on python built with macports returns a version tuple
            ## full of empty strings. That's caught here but the OS version is unknown.
            ## Until a better solution is found MACOS-UNKNOWN_VERSION is used to flag this.
            current = 'MACOS-UNKNOWN_VERSION'
            errorString = "Mac Os 10.5 or above required. You have an unkown version"

    # Linux of some flavour.    
    elif system == 'Linux':
        kernel = platform.platform().split('-')[1].split('.')
        # Supported Linux kernel version.
        if int(kernel[0]) == 2 and int(kernel[1]) == 6 and int(kernel[2]) >= 13:
            try:
                # pyinotify versions have slightly different APIs
                # so the version needs to be determined.
                import pyinotify
                try:
                    # 0.8.x has a __version__ attribute.
                    version = pyinotify.__version__.split('.')
                    if int(version[0]) == 0 and int(version[1]) == 8:
                        current = 'LINUX_2_6_13+pyinotify_0_8'
                    # This pyinotofy has a __version__ attribute but isn't 0.8.
                    else:
                        errorString = "Pyinotify 0.7 or above required. Unknown version found."
                except:
                    # 0.7.x doesn't have a __version__ attribute but there is
                    # a possibility that the installed version is 0.6 or less.
                    # That isn't tested for and might be a point of failure.
                    current = 'LINUX_2_6_13+pyinotify_0_7'
            except:
                errorString = "Pyinotify 0.7 or above required. Package not found."
        # Unsupported Linux kernel version.    
        else:
            errorString = "Linux kernel 2.6.13 or above required. "
            errorString += "You have: %s" % str(platform.platform().split('-')[1])
    
    # Windows of some flavour.
    elif system == 'Windows':
        version = platform.platform().split('-')
        if version[1] == 'XP':
            # Although there is a working monitor system for XP the whole blitzed
            # FS system has yet to be deployed against an XP blitz server. Thus the
            # option is forced to fail until this deployment is tested.
            current = 'WIN_XP-NOT_YET_SUPPORTED'
            errorString = "Windows XP not yet supported."
        else:
            errorString = "Windows XP required. You have: %s" % str(version)
    
    # Unknown OS.
    else:
        errorString = "Unsupported platform: %s" % system
    
    try:
        return supported[current]
    except:
        raise Exception("Libraries required by OMERO.fs monitor unavailable: " + errorString)
    
