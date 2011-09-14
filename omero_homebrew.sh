#!/bin/bash

set -e
set -u

DIR=$1
shift


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
        git clone -b omero git://github.com/joshmoore/homebrew.git $DIR
        cd $DIR
    else
        mkdir $DIR
        cd $DIR
        curl -L https://github.com/joshmoore/homebrew/tarball/omero |\
            /usr/bin/tar --strip-components=1 -xvf -
        bin/brew install git
    fi
fi

# Python virtualenv/pip support ===================================
if (bin/pip --version)
then
    echo "Using existing pip"
else
    rm -rf virtualenv.py
    curl -O https://raw.github.com/pypa/virtualenv/master/virtualenv.py
    python virtualenv.py .
fi


###################################################################
# BREW INSTALLS
###################################################################

installed(){
    bin/brew list | grep -wq $1
    echo $1 installed.
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
    bin/pip freeze "$@" | grep -q "^$PKG=="
    echo $PKG installed.
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