#!/bin/bash

set -e
set -u

DIR=$1
shift

# Brew support ===================================================
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


# OPTIONAL: this makes things faster for devs =====================
bin/brew install ccache
export PATH=`bin/brew --prefix ccache`:$PATH


# Basic native requirements =======================================
bin/brew install hdf5 # Used by pytables
bin/brew install berkeley-db46 --without-java
bin/brew install omero43 "$@" # For psql, cpp, etc.


# Python virtualenv/pip support ===================================
curl -O https://raw.github.com/pypa/virtualenv/master/virtualenv.py
python virtualenv.py .
. bin/activate


# Python requirements =============================================
pip install PIL
pip install matplotlib

export HDF5_DIR=`pwd`
pip install tables