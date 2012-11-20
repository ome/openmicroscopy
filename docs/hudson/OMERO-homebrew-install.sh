#!/bin/bash
# Main Homebrew installation script

set -e
set -u
set -x

export ICE_VERSION=${ICE_VERSION:-zeroc-ice33}
export OMERO_ALT=${OMERO_ALT:-ome/alt}
export BREW_DIR=${BREW_DIR:-/tmp/homebrew}
export PSQL_DIR=${PSQL_DIR:-/tmp/var/postgres}
export OMERO_DATA_DIR=${OMERO_DATA_DIR:-/tmp/var/OMERO.data}

# Remove existing formulas and ome/alt tap
if ($BREW_DIR/bin/brew --version)
then
    cd $BREW_DIR
    if (bin/pip --version)
    then
        echo "Removing pip-installed packages"

        # Remove tables manually
        bin/pip freeze -l | grep tables && bin/pip uninstall -y tables

        # Solve Cython uninstallation error exit
        (bin/pip freeze -l | grep Cython && bin/pip uninstall -y Cython) || echo "Cython uninstalled"

        for plugin in $(bin/pip freeze -l); do
            packagename=$(echo "$plugin" | awk -F == '{print $1}')
            echo "Uninstalling $packagename..."
            bin/pip uninstall -y $packagename
        done
    fi

    echo "Removing Homebrew installation"
    rm -rf $BREW_DIR
fi

# Install Homebrew in BREW_DIR
mkdir $BREW_DIR && curl -L https://github.com/mxcl/homebrew/tarball/master | tar xz --strip 1 -C $BREW_DIR
cd $BREW_DIR

# Clean cache before any operation to test full installation
rm -rf $(bin/brew --cache)

# Re-install git and update homebrew
bin/brew install git
bin/brew update

export PATH=$(bin/brew --prefix)/bin:$PATH

# Install homebrew dependencies
curl -fsSLk 'https://raw.github.com/openmicroscopy/openmicroscopy/develop/docs/install/homebrew/omero_homebrew.sh' > /tmp/omero_homebrew.sh
chmod +x /tmp/omero_homebrew.sh
. /tmp/omero_homebrew.sh

# Install omero
bin/brew install omero

# Set environment variables
export ICE_CONFIG=$(bin/brew --prefix omero)/etc/ice.config
export ICE_HOME=$(bin/brew --prefix $OMERO_ALT/$ICE_VERSION)
export PYTHONPATH=$(bin/brew --prefix omero)/lib/python:$ICE_HOME/python
export PATH=$(bin/brew --prefix)/bin:$(bin/brew --prefix)/sbin:/usr/local/lib/node_modules:$ICE_HOME/bin:$PATH
export DYLD_LIBRARY_PATH=$ICE_HOME/lib:$ICE_HOME/python:${DYLD_LIBRARY_PATH-}


# Create database
if [ -d "$PSQL_DIR" ]; then
    rm -rf $PSQL_DIR
fi
bin/initdb $PSQL_DIR
bin/brew services restart postgresql
bin/pg_ctl -D $PSQL_DIR -l $PSQL_DIR/server.log start


# Set database
bin/omero config set omero.db.name omero_database
bin/omero config set omero.db.user db_user
bin/omero config set omero.db.pass db_password

# Create user and databse
bin/createuser -w -D -R -S db_user
bin/createdb -O db_user omero_database
bin/psql -h localhost -U db_user -l

bin/omero db script "" "" root_password
bin/psql -h localhost -U db_user omero_database < OMERO4.4__0.sql

# Set up the data directory
mkdir -p $OMERO_DATA_DIR
bin/omero config set omero.data.dir $OMERO_DATA_DIR

# Start the server
bin/omero admin start