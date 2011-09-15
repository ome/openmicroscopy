#!/bin/bash

set -e
set -u

DIR=$1
shift

OMERO_GIT=${OMERO_URL:-git://github.com/joshmoore/homebrew.git}
OMERO_URL=${OMERO_URL:-https://github.com/joshmoore/homebrew/tarball/omero}
VENV_URL=${VENV_URL:-https://raw.github.com/pypa/virtualenv/master/virtualenv.py}
TABLES_GIT=${TABLES_GIT:-git+https://github.com/PyTables/PyTables.git@master}

###################################################################
# BREW & PIP BASE SYSTEMS
###################################################################

# Brew support ===================================================
if ($DIR/bin/brew --version)
then
    echo "Using brew installed in $DIR"
    cd $DIR
else
    if (git --version)
    then
        git clone -b omero "$OMERO_GIT" $DIR
        cd $DIR
    else
        mkdir $DIR
        cd $DIR
        curl -L "$OMERO_URL" | /usr/bin/tar --strip-components=1 -xvf -
        bin/brew install git
        bin/git init
        bin/git remote add omero "$OMERO_GIT"
        bin/git fetch omero omero
        bin/git reset FETCH_HEAD
    fi
fi

# Python virtualenv/pip support ===================================
if (bin/pip --version)
then
    echo "Using existing pip"
else
    rm -rf virtualenv.py
    curl -O "$VENV_URL"
    python virtualenv.py --no-site-packages .
fi


###################################################################
# BREW INSTALLS
###################################################################

installed(){
    bin/brew list | grep -wq $1 && {
        echo $1 installed.
    } || {
        return 1
    }
}

# OPTIONAL: this makes things faster for devs =====================
installed ccache || bin/brew install ccache
export PATH=`bin/brew --prefix ccache`:$PATH

# Basic native requirements =======================================
installed pkg-config || bin/brew install pkg-config # for matplotlib
installed hdf5 || bin/brew install hdf5 # Used by pytables
installed berkeley-db46 || bin/brew install berkeley-db46 --without-java
installed zeroc-ice33 || bin/brew install zeroc-ice33

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
# https://jholewinski.wordpress.com/2011/07/21/installing-matplotlib-on-os-x-10-7-with-homebrew/
#
export LDFLAGS="-L/usr/X11/lib"
export CFLAGS="-I/usr/X11/include -I/usr/X11/include/freetype2 -I/usr/X11/include/libpng12"
installed matplotlib || bin/pip install matplotlib

# PyTables requirements ===========================================
export HDF5_DIR=`pwd`
installed Cython || bin/pip install Cython
installed numexpr || bin/pip install numexpr
installed tables || bin/pip install -e $TABLES_GIT#egg=tables

echo "Done."
echo "You can now install OMERO with: 'bin/brew install omero43 ...'"

