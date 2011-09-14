#!/bin/bash

set -e
set -u

DIR=$1
shift

OMERO_GIT=${OMERO_URL:-git://github.com/joshmoore/homebrew.git}
OMERO_URL=${OMERO_URL:-https://github.com/joshmoore/homebrew/tarball/omero}
VENV_URL=${VENV_URL:-https://raw.github.com/pypa/virtualenv/master/virtualenv.py}

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
exit 0

# Python virtualenv/pip support ===================================
if (bin/pip --version)
then
    echo "Using existing pip"
else
    rm -rf virtualenv.py
    curl -O "$VENV_URL"
    python virtualenv.py .
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
installed hdf5 || bin/brew install hdf5 # Used by pytables
installed berkeley-db46 || bin/brew install berkeley-db46 --without-java
installed omero43 || bin/brew install omero43 "$@" # For psql, cpp, etc.


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
installed PIL || bin/pip install PIL
installed matplotlib || bin/pip install matplotlib

# PyTables requirements ===========================================
export HDF5_DIR=`pwd`
installed numpy -l || bin/pip install -U numpy # Must be newer!
installed Cython || bin/pip install Cython
installed tables || bin/pip install tables

echo "Done."
