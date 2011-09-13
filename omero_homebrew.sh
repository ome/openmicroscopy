#!/bin/bash

set -e
set -u

DIR=$1
shift

# Brew support ===================================================
if ($DIR/bin/brew --version)
then
  echo "Using brew installed in $DIR"
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

#. bin/activate This fails for some reason in the script


# Python requirements =============================================
bin/pip install PIL
bin/pip install matplotlib

export HDF5_DIR=`pwd`
bin/pip install -U numpy
bin/pip install Cython
bin/pip install tables
