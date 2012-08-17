#!/bin/bash
# Main Homebrew installation script

set -e
set -u
set -x

export ICE_VERSION=${ICE_VERSION:-zeroc-ice33}
export OMERO_ALT=${OMERO_ALT:-ome/alt}
export BREW_DIR=${BREW_DIR:-/tmp/homebrew}

# Remove existing formulas and ome/alt tap
if ($BREW_DIR/bin/brew --version)
then
	echo "Removing Homebrew installation"
	rm -rf $BREW_DIR
fi

# Remove pip installed packages
if (pip --version)
then
    # Remove tables manually 
	pip freeze -l | grep tables && pip uninstall -y tables

	# Solve Cython uninstallation error exit
	(pip freeze -l | grep Cython && pip uninstall -y Cython) || echo "Cython uninstalled"

	for plugin in $(pip freeze -l); do
   		packagename=$(echo "$plugin" | awk -F == '{print $1}')
   		echo "Uninstalling $packagename..."
		pip uninstall -y $packagename
	done
fi

mkdir $BREW_DIR && curl -L https://github.com/mxcl/homebrew/tarball/master | tar xz --strip 1 -C $BREW_DIR
cd $BREW_DIR

# Re-install git and update homebrew
bin/brew install git
bin/brew update


export PATH=$(bin/brew --prefix)/bin:$PATH

# Run brew doctor
#brew_status=$(brew doctor)
#if echo $brew_status | grep "Error"
#then
#	echo "Please fix brew doctor first."
#	exit 1
#fi

# Install homebrew dependencies
curl -fsSLk 'https://raw.github.com/openmicroscopy/openmicroscopy/develop/docs/install/homebrew/omero_homebrew.sh' > /tmp/omero_homebrew.sh
chmod +x /tmp/omero_homebrew.sh
. /tmp/omero_homebrew.sh

# Install postgres and omero
bin/brew install postgresql
bin/brew install omero

# Set environment variables
export ICE_CONFIG=$(bin/brew --prefix omero)/etc/ice.config
export ICE_HOME=$(bin/brew --prefix $OMERO_ALT/$ICE_VERSION)
export PYTHONPATH=$(bin/brew --prefix omero)/lib/python:$ICE_HOME/python
export PATH=$(bin/brew --prefix)/bin:$(bin/brew --prefix)/sbin:/usr/local/lib/node_modules:$ICE_HOME/bin:$PATH
export DYLD_LIBRARY_PATH=$ICE_HOME/lib:$ICE_HOME/python:${DYLD_LIBRARY_PATH-}

# Set database
bin/omero config set omero.db.name omero_database
bin/omero config set omero.db.user db_user
bin/omero config set omero.db.pass db_password

# Set up the data directory
mkdir -p ~/var/OMERO.data
bin/omero config set omero.data.dir ~/var/OMERO.data

# Start the server
bin/omero admin start

# Set config for OMERO web
#omero config set omero.web.application_server "development"
#omero config set omero.web.debug True

# Start web
#omero web start