webfigure
=========

OMERO.web app for creating figures from images in OMERO.


Requirements
============

* OMERO 4.4.0 or later

Development Installation
========================

Clone the repository in to your OMERO.web installation:

    cd components/tools/OmeroWeb/omeroweb/
    git clone git://github.com/will-moore/webfigure.git
    path/to/bin/omero config set omero.web.apps '["webfigure"]'

Now start up (or restart) OMERO.web as normal in your development environment.

Production Installation
=======================

Install the latest version of OMERO.server and OMERO.web and then:

    cd $OMERO_HOME/lib/python/omeroweb
    wget -O master.zip https://github.com/will-moore/webfigure/zipball/master
    unzip master.zip
    mv openmicroscopy-webfigure-* webfigure
    path/to/bin/omero config set omero.web.apps '["webfigure"]'

Restart your web server


Pdf-generation script
=====================

In order to export figures as pdf documents, you also need to upload the Figure_To_Pdf.py script.
This script requires the reportlab python libraray: http://www.reportlab.com/software/opensource/

    cd webfigure
    path/to/bin/omero script upload webfigure_scripts/Figure_To_Pdf.py --official

    pip install reportlab    # or easy_install reportlab

