#!/bin/bash
# Main Homebrew installation script

set -e
set -u
set -x

export PSQL_DIR=${PSQL_DIR:-/usr/local/var/postgres}
export OMERO_DATA_DIR=${OMERO_DATA_DIR:-/tmp/var/OMERO.data}
export SCRIPT_NAME=${SCRIPT_NAME:-OMERO.sql}
export ICE=${ICE:-3.5}

# Test whether this script is run in a job environment
JOB_NAME=${JOB_NAME:-}
if [[ -n $JOB_NAME ]]; then
    DEFAULT_TESTING_MODE=true
else
    DEFAULT_TESTING_MODE=false
fi
TESTING_MODE=${TESTING_MODE:-$DEFAULT_TESTING_MODE}

###################################################################
# Homebrew installation
###################################################################

# Install Homebrew in /usr/local
ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
cd /usr/local

# Install git if not already installed
bin/brew list | grep "\bgit\b" || bin/brew install git

# Update Homebrew
bin/brew update

# Run brew doctor
export PATH=$(bin/brew --prefix)/bin:$PATH
bin/brew doctor

###################################################################
# Python pip installation
###################################################################

# Install Homebrew python
# Alternately, the system Python can be used but installing Python
# dependencies may require sudo
bin/brew install python

# Tap homebrew-science library (HDF5)
bin/brew tap homebrew/science || echo "Already tapped"

# Tap ome-alt library
bin/brew tap ome/alt || echo "Already tapped"

if [ $TESTING_MODE ]; then
    # Install scc tools
    bin/pip install -U scc || echo "scc installed"

    # Merge homebrew-alt PRs
    cd Library/Taps/ome/homebrew-alt
    /usr/local/bin/scc merge master

    # Repair formula symlinks after merge
    /usr/local/bin/brew tap --repair
fi

cd /usr/local

###################################################################
# Bio-Formats installation
###################################################################

# Install Bio-Formats
bin/brew install bioformats44
showinf -version

###################################################################
# OMERO installation
###################################################################

# Install PostgreSQL and OMERO
OMERO_PYTHONPATH=$(bin/brew --prefix omero)/lib/python
if [ "$ICE" == "3.3" ]; then
    bin/brew install omero44 --with-ice33
    ICE_HOME=$(bin/brew --prefix zeroc-ice33)
    export PYTHONPATH=$OMERO_PYTHONPATH:$ICE_HOME/python
    export DYLD_LIBRARY_PATH=$ICE_HOME/lib
elif [ "$ICE" == "3.4" ]; then
    bin/brew install omero44 --with-ice34
    ICE_HOME=$(bin/brew --prefix zeroc-ice34)
    export PYTHONPATH=$OMERO_PYTHONPATH:$ICE_HOME/python
    export DYLD_LIBRARY_PATH=$ICE_HOME/lib
else
    bin/brew install omero44
    export PYTHONPATH=$OMERO_PYTHONPATH
fi
bin/brew install postgres

# Fix for PIL installation
ln -s /usr/local/include/freetype2 /usr/local/include/freetype

# Hack to workaround Pytables 3.1.1/Cython 0.2.1 incompatibility
bin/pip install Cython==0.20

# Install OMERO Python dependencies
bash bin/omero_python_deps

# Set additional environment variables
export ICE_CONFIG=$(bin/brew --prefix omero44)/etc/ice.config

# Note: If postgres startup fails it's probably because there was an old
# process still running.
# Create PostgreSQL database
if [ -d "$PSQL_DIR" ]; then
    rm -rf $PSQL_DIR
fi
bin/initdb $PSQL_DIR
bin/pg_ctl -D $PSQL_DIR -l $PSQL_DIR/server.log -w start

# Create user and database
bin/createuser -w -D -R -S db_user
bin/createdb -O db_user omero_database
bin/psql -h localhost -U db_user -l

# Set database
bin/omero config set omero.db.name omero_database
bin/omero config set omero.db.user db_user
bin/omero config set omero.db.pass db_password

# Run DB script
bin/omero db script "" "" root_password -f $SCRIPT_NAME
bin/psql -h localhost -U db_user omero_database < $SCRIPT_NAME
rm $SCRIPT_NAME

# Set up the data directory
mkdir -p $OMERO_DATA_DIR
bin/omero config set omero.data.dir $OMERO_DATA_DIR

# Start the server
bin/omero admin start

# Test simple fake import
bin/omero login -s localhost -u root -w root_password
touch test.fake
bin/omero import test.fake
bin/omero logout