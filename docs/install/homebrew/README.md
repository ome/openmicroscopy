INSTALL
=======

The instructions and scripts provided here depend on Homebrew 0.9
or later, including support for the `brew tap' command.

This procedure has been tested on the following Mac OS X versions:

    10.6.8
    10.7.3

Install OS X Developer Tools. This procedure has been tested with the the following Xcode distributions:

    xcode_3.2.6_and_ios_sdk_4.3.dmg
    Xcode 4.3.2 (for Mac OS X 10.7.3)

on the following Hardware:

    MacBook
    MacBookPro1,1 (Intel Core Duo, 2.16GHz, 2GB RAM)
    MacBookPro8,2 (Intel Core i7, 2.3 GHz, 8 GB RAM)
    MacMini1,1 (Intel Core Duo, 1.66GHz, 2GB RAM)

Homebrew installation
---------------------

Follow the instructions for installing homebrew available at https://github.com/mxcl/homebrew/wiki/installation.
All requirements for OMERO will be installed in this location (e.g. /usr/local). For example:

    $ ruby -e "$(curl -fsSLk https://raw.github.com/mxcl/homebrew/master/Library/Contributions/install_homebrew.rb)"
    $ brew install git

Grab the omero_homebrew.sh installation script:

    $ curl -fsSLk 'https://raw.github.com/joshmoore/openmicroscopy/homebrew-merge/docs/install/homebrew/omero_homebrew.sh' > omero_homebrew.sh

Run OMERO.homebrew script to install OMERO requirements:

    $ chmod +x omero_homebrew.sh
    $ ./omero_homebrew.sh

NB. The omero_homebrew.sh script may need to be run several times before it completes, albeit successfully. This is due to the homebrew script pulling code archives from many different places as it retrieves the various components that you have asked it to install. Occasionally the remote repositories are temporarily unavailable and can cause the script to fail. Under normal circumstances simply rerunning the script should be sufficient. Occasionally you may have to wait for a short period then try running the script again. Rarely you may have to find a different location for the remote repository (NB. This should involve getting in touch with the homebrew project/OMERO team members so that homebrew formulae can be updated in the event of a permanent failure of a resource).

Install PostGres if you do not have another PostGres installation that you can use.

    $ brew install postgres

Common issues
------------
If you run into problems with Homebrew, you can always run

    $ brew doctor


Below is a non-exhaustive list of errors/warnings

1. Warning: Xcode is not installed! Builds may fail!
Solution: install Xcode

2. Warning: It appears you have MacPorts or Fink installed.
Follow uninstall instructions [[link](http://guide.macports.org/chunked/installing.macports.uninstalling.html)]

3. ==> Installing postgresql dependency: readline
 Error: No such file or directory - /usr/bin/cc`
For Xcode 4.3.2 make sure Xcode Command Line Tools are installed [[link](https://github.com/mxcl/homebrew/issues/10244#issuecomment-4013781)]

4. Error: You must `brew link ossp-uuid' before postgresql can be installed
Try brew cleanup then brew link ossp-uuid

5. Error: Failed executing: cd cpp && make MCPP_HOME=/Users/sebastien/apps/OMERO.libs/Cellar/mcpp/2.7.2 DB_HOME=/Users/sebastien/apps/OMERO.libs/Cellar/berkeley-db46/4.6.21 OPTIMIZE=yes prefix=/Users/sebastien/apps/OMERO.libs/Cellar/zeroc-ice33/3.3 embedded_runpath_prefix=/Users/sebastien/apps/OMERO.libs/Cellar/zeroc-ice33/3.3 install
We have had problems building zeroc-ice33 under MacOS 10.7.3 [[ticket](http://trac.openmicroscopy.org.uk/ome/ticket/8075)]. If you will be developing OMERO rather than installing omero43, you can try installing `ice' (Ice 3.4) instead

6. ==> Installing hdf5 dependency: szip
   ==> Downloading http://www.hdfgroup.org/ftp/lib-external/szip/2.1/src/szip-2.1.tar.gz
   Already downloaded: /Users/moore/Library/Caches/Homebrew/szip-2.1.tar.gz
   Error: MD5 mismatch
   Expected: 902f831bcefb69c6b635374424acbead
   Got: 0d6a55bb7787f9ff8b9d608f23ef5be0
   Archive: /Users/moore/Library/Caches/Homebrew/szip-2.1.tar.gz
   (To retry an incomplete download, remove the file above.)
   Manually remove the archived version (Here: /Users/moore/Library/Caches/Homebrew/szip-2.1.tar.gz) since the maintainer may have updated the file.


OMERO server
-----------

At this point you have a choice. If you just want a deployment of the current release of OMERO.server then a simple homebrew install is sufficient, e.g.

    $ brew install omero43

However if you wish to pull OMERO.server from the git repo for development purposes then it is worth setting up OMERO.server manually rather than using homebrew. Prepare a place for your OMERO code to live, e.g.

    $ mkdir -p ~/code/projects/OMERO
    $ cd ~/code/projects/OMERO

Now clone the OMERO git repo:

    $ git clone git://github.com/openmicroscopy/openmicroscopy

NB. If you have a github account & you plan to develop code for OMERO then you should make a fork into your own account then clone to your local development machine, e.g.

    $ git clone git://github.com/YOURNAMEHERE/openmicroscopy


ENV
===

Edit your .profile as appropriate. NB. The following are indicators of required entries:

    export BREW_DIR=/usr/local

    export OMERO_HOME=$BREW_DIR/Cellar/omero43/4.3/
    export ICE_CONFIG=$OMERO_HOME/etc/ice.config
    export ICE_HOME=$BREW_DIR/Cellar/zeroc-ice33
    export PYTHONPATH=$OMERO_HOME/lib/python:$ICE_HOME/3.3.1/python

    export PATH=/usr/local/bin:$BREW_DIR/bin:$OMERO_HOME/bin:/usr/local/lib/node_modules:$ICE_HOME/bin:$PATH
    export DYLD_LIBRARY_PATH=$BREW_DIR/lib

NB: if you installed `ice' (Ice 3.4), use the following paths

    export ICE_HOME=$OMEROLIBS/Cellar/ice
    export PYTHONPATH=$OMERO_HOME/lib/python:$ICE_HOME/3.4.2/python

CONFIG
======

    $ initdb /usr/local/var/postgres
    $ cp /usr/local/Cellar/postgresql/9.0.4/org.postgresql.postgres.plist ~/Library/LaunchAgents/
    $ launchctl load -w ~/Library/LaunchAgents/org.postgresql.postgres.plist
    $ pg_ctl -D /usr/local/var/postgres/ -l /usr/local/var/postgres/server.log start

NB: under Mac OS X 10.7.3, installed postgresql version is now 9.1.3 and the file is called homebrew.mxcl.postgresql.plist

    $ createuser -P -D -R -S omero
    $ createdb -O omero omero
    $ createlang plpgsql omero

    $ psql -h localhost -U omero -l

Should give similar output to the following:
                                
                            List of databases
       
       Name    | Owner | Encoding |  Collation  |    Ctype    | Access privileges
    -----------+-------+----------+-------------+-------------+-------------------
     omero     | omero | UTF8     | en_GB.UTF-8 | en_GB.UTF-8 |
     postgres  | simon | UTF8     | en_GB.UTF-8 | en_GB.UTF-8 |
     template0 | simon | UTF8     | en_GB.UTF-8 | en_GB.UTF-8 | =c/simon         +
               |       |          |             |             | simon=CTc/simon
     template1 | simon | UTF8     | en_GB.UTF-8 | en_GB.UTF-8 | =c/simon         +
               |       |          |             |             | simon=CTc/simon
    (4 rows)


Now tell OMERO.server about our database

    $ omero config set omero.db.name omero
    $ omero config set omero.db.user omero
    $ omero config set omero.db.pass omero

    $ omero db script

    $ psql -h localhost -U omero omero < OMERO4.3__0.sql

Now create a location to store OMERO data, e.g.

    $ mkdir -p ~/var/OMERO.data

and tell OMERO.server this location:

    $ omero config set omero.data.dir ~/var/OMERO.data

We can inspect the OMERO.server configuration settings using:

    $ omero config get

Now Start OMERO.server

    $ omero admin {start|stop}

Now connect to your OMERO.server using insight with the following credentials:
    
    U: root
    P: omero

