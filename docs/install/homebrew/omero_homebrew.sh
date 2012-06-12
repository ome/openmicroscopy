#!/bin/bash

set -e
set -u

OMERO_ALT=${OMERO_ALT:-openmicroscopy/alt}
VENV_URL=${VENV_URL:-https://raw.github.com/pypa/virtualenv/master/virtualenv.py}
TABLES_GIT=${TABLES_GIT:-git+https://github.com/PyTables/PyTables.git@master}
if [[ "${GIT_SSL_NO_VERIFY-}" == "1" ]]; then
    CURL_OPTS=${CURL_OPTS:-"--insecure"}
fi


###################################################################
# BREW & PIP BASE SYSTEMS
###################################################################

# Brew support ===================================================

brew --version || {
    echo "Please install brew first"
    exit 1
}

BREW_DIR="$(dirname $(dirname $(which brew)))"
echo "Using brew installed in $BREW_DIR"

# Move to BREW_DIR for the rest of this script
# so that "bin/EXECUTABLE" will pick up the
# intended executable.
cd "$BREW_DIR"

# Next we must either add the "tap" which will provide
# the formulae for OMERO if it hasn't been tapped already.
bin/brew tap | grep -q "$OMERO_ALT" || {
    bin/brew tap "$OMERO_ALT"
}

# Python virtualenv/pip support ===================================
if (bin/pip --version)
then
    echo "Using existing pip"
else
    rm -rf virtualenv.py
    curl "$CURL_OPTS" -O "$VENV_URL"
    python virtualenv.py --no-site-packages .
fi


###################################################################
# BREW INSTALLS
###################################################################

installed(){
    bin/brew info $1 | grep -wq "Not installed" && {
        return 1
    } || {
	echo $1 installed
    }
}

# Setup PATH ======================================================
# ccache is optional but makes things faster for devs
installed ccache || bin/brew install ccache
export PATH=`bin/brew --prefix ccache`:`pwd`/bin/:$PATH

# Basic native requirements =======================================
installed pkg-config || bin/brew install pkg-config # for matplotlib
installed hdf5 || bin/brew install hdf5 # Used by pytables
installed berkeley-db46 || bin/brew install berkeley-db46 --without-java
installed zeroc-ice33 || bin/brew install zeroc-ice33
# Requirements for PIL ============================================
installed libjpeg || bin/brew install libjpeg

###################################################################
# PIP INSTALLS
###################################################################

installed(){
    PKG=$1; shift
    bin/pip freeze "$@" | grep -q "^$PKG==" && {
        echo $PKG installed.
    } || {
        return 1
    }
}

# Python requirements =============================================
installed numpy  || bin/pip install numpy
installed PIL || bin/pip install PIL
#
# Various issues with matplotlib. See the following if you have problems:
# -----------------------------------------------------------------------
# http://superuser.com/questions/242190/how-to-install-matplotlib-on-os-x
# http://jholewinski.org/blog/installing-matplotlib-on-os-x-10-7-with-homebrew/
#
export LDFLAGS="-L/usr/X11/lib"
export CFLAGS="-I/usr/X11/include -I/usr/X11/include/freetype2 -I/usr/X11/include/libpng12"
installed matplotlib || bin/pip install matplotlib

# PyTables requirements ===========================================
export HDF5_DIR=`pwd`
installed Cython || bin/pip install Cython
installed numexpr || bin/pip install numexpr
bin/pip freeze | grep -q tables-dev || bin/pip install -e $TABLES_GIT#egg=tables

echo "Done."
echo "You can now install OMERO with: 'bin/brew install omero ...'"

