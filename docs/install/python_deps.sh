#!/bin/bash

# Install OMERO Python dependencies
# This script was originally part of the Homebrew installation script.
# It assumes the current directory is the one where Homebrew was installed,
# i.e. /usr/local in most cases

set -e
set -u

VENV_URL=${VENV_URL:-https://raw.github.com/pypa/virtualenv/master/virtualenv.py}
TABLES_GIT=${TABLES_GIT:-git+https://github.com/PyTables/PyTables.git@master}
if [[ "${GIT_SSL_NO_VERIFY-}" == "1" ]]; then
    CURL="curl ${CURL_OPTS-} --insecure -O"
else
    CURL="curl ${CURL_OPTS-} -O"
fi

###################################################################
# PIP BASE SYSTEM
###################################################################

# Python virtualenv/pip support ===================================

# Look for pip in the PATH
if (pip --version)
then
    PIP_DIR="$(dirname $(dirname $(which pip)))"
    echo "Using existing pip installed in $PIP_DIR"

    # Move to PIP_DIR for the rest of this script
    # so that "bin/EXECUTABLE" will pick up the
    # intended executable.
    cd "$PIP_DIR"
else
    # Create a local virtual environment
    rm -rf virtualenv.py
    $CURL "$VENV_URL"
    python virtualenv.py --no-site-packages .
fi

###################################################################
# PIP INSTALLS
###################################################################

install(){
    PKG=$1; shift
    bin/pip freeze "$@" | grep -q "^$PKG==" && {
        echo $PKG installed.
    } || {
        bin/pip install $PKG
    }
}


# Numpy & PIL
install numpy
install PIL

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
export HDF5_DIR=`pwd`
install Cython
install numexpr

bin/pip freeze | grep -q tables-dev || {
    which git && bin/pip install -e $TABLES_GIT#egg=tables ||
    "Install git in order to install PyTables."
}

echo "Done."

