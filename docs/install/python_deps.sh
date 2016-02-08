#!/bin/bash

# Install OMERO Python dependencies
# This script was originally part of the Homebrew installation script.
# It assumes the current directory is the one where Homebrew was installed,
# i.e. /usr/local in most cases

set -e
set -u

###################################################################
# PIP BASE SYSTEM
###################################################################

# Python virtualenv/pip support ===================================

# Look for pip in the PATH
if (pip --version)
then
    echo "Using existing pip found in $(which pip)"
else
    echo "No pip found in the PATH."
    echo "Install pip via easy_install or virtualenv first"
    exit
fi

###################################################################
# PIP INSTALLS
###################################################################

install(){
    PKG=$1; shift
    pip freeze "$@" | grep -q "^$PKG==" && {
        echo $PKG installed.
    } || {
        pip install $PKG
    }
}


# Numpy & Pillow
install numpy
install Pillow

# Scipy (requires Gfortran compiler)
install scipy || echo "Scipy installation failed. Make sure a fortran compiler is available."

#
# Various issues with matplotlib. See the following if you have problems:
# -----------------------------------------------------------------------
# http://superuser.com/questions/242190/how-to-install-matplotlib-on-os-x
# http://jholewinski.org/blog/installing-matplotlib-on-os-x-10-7-with-homebrew/
#
export LDFLAGS="-L/usr/X11/lib"
export CFLAGS="-I/usr/X11/include -I/usr/X11/include/freetype2 -I/usr/X11/include/libpng12"
install matplotlib

# PyTables requirements ===========================================
install Cython
install numexpr
install tables==2.4

echo "Done."

