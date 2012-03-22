INSTALL
=======

Install OS X Developer Tools. This procedure has been tested with the the following XCode distributions:

    xcode_3.2.6_and_ios_sdk_4.3.dmg

On Mac OS X versions:

    10.6.8

On the following Hardware:

    MacBook
    MacBookPro1,1 (Intel Core Duo, 2.16GHz, 2GB RAM)
    MacBookPro8,2 (Intel Core i7, 2.3 GHz, 8 GB RAM)
    MacMini1,1 (Intel Core Duo, 1.66GHz, 2GB RAM)

Install homebrew:

    $ ruby -e "$(curl -fsSLk https://raw.github.com/mxcl/homebrew/master/Library/Contributions/install_homebrew.rb)"
    $ brew install git

Prepare a place to store the OMERO homebrew scripts, e.g.

    $ mkdir -p ~/code/projects/omero
    $ cd ~/code/projects/omero

Clone the OMERO homebrew repo:

    $ git clone git://gist.github.com/1213688.git OMERO.homebrew

Prepare a place to store the OMERO prerequisites, e.g.

    $ mkdir -p ~/apps/OMERO.libs

Run OMERO.homebrew script, specifying an existing directory to install into, e.g.

    $ cd OMERO.homebrew
    $ chmod +x omero_homebrew.sh
    $ ./omero_homebrew.sh ~/apps/OMERO.libs

NB. The omero_homebrew.sh script may need to be run several times before it completes, albeit successfully. This is due to the homebrew script pulling code archives from many different places as it retrieves the various components that you have asked it to install. Occasionally the remote repositories are temporarily unavailable and can cause the script to fail. Under normal circumstances simply rerunning the script should be sufficient. Occasionally you may have to wait for a short period then try running the script again. Rarely you may have to find a different location for the remote repository (NB. This should involve getting in touch with the homebrew project/OMERO.homebrew team members so that homebrew formulae can be updated in the event of a permanent failure of a resource).

Install PostGres

    $ brew update
    $ brew install postgres

NB. If you get the following error message
Warning: Xcode is not installed! Builds may fail!
==> Installing postgresql dependency: readline
Error: No such file or directory - /usr/bin/cc
1) make sure Xcode is installed
2) make sure Xcode Command Line Tools are installed (https://github.com/mxcl/homebrew/issues/10244#issuecomment-4013781)

At this point you have a choice. If you just want a deployment of the current release of OMERO.server then a simple homebrew install is sufficient, e.g.

    $ ~/app/OMERO.libs/bin/brew install omero43

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

    export OMEROLIBS=~/apps/OMERO.libs

    export OMERO_HOME=$OMEROLIBS/Cellar/omero43/4.3/
    export ICE_CONFIG=$OMERO_HOME/etc/ice.config
    export PYTHONPATH=$OMERO_HOME/lib/python:$OMEROLIBS/Cellar/zeroc-ice33/3.3.1/python

    export ICE_HOME=$OMEROLIBS/Cellar/zeroc-ice33

    export PATH=/usr/local/bin:$OMEROLIBS/bin:$OMERO_HOME/bin:/usr/local/lib/node_modules:$ICE_HOME/bin:$PATH
    export DYLD_LIBRARY_PATH=$OMEROLIBS/lib

CONFIG
======

    $ initdb /usr/local/var/postgres
    $ cp /usr/local/Cellar/postgresql/9.0.4/org.postgresql.postgres.plist ~/Library/LaunchAgents/
    $ launchctl load -w ~/Library/LaunchAgents/org.postgresql.postgres.plist
    $ pg_ctl -D /usr/local/var/postgres/ -l /usr/local/var/postgres/server.log start


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

