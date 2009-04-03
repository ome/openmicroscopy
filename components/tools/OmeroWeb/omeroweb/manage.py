#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

import sys
from django.core.management import execute_manager

try:
    import settings # Assumed to be in the same directory.
except ImportError:
    sys.stderr.write("Error: Can't find the file 'settings.py' in the directory containing %r. It appears you've customized things.\nYou'll have to run django-admin.py, passing it your settings module.\n(If the file settings.py does indeed exist, it's causing an ImportError somehow.)\n" % __file__)
    sys.exit(1)

import os
import logging
import logging.handlers

try:
    LOGDIR = settings.LOGDIR
except:
    LOGDIR = os.path.dirname(__file__).replace('\\','/')

LOGFILE = ('OMEROweb.log')
logging.basicConfig(level=logging.INFO,
                format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                datefmt='%a, %d %b %Y %H:%M:%S',
                filename=os.path.join(LOGDIR, LOGFILE),
                filemode='w')

fileLog = logging.handlers.TimedRotatingFileHandler(os.path.join(LOGDIR, LOGFILE),'midnight',1)

# Windows will not allow renaming (or deleting) a file that's open. 
# There's nothing the logging package can do about that.
try:
    sys.getwindowsversion()
except:
    fileLog.doRollover()

fileLog.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s %(name)-12s: %(levelname)-8s %(message)s')
fileLog.setFormatter(formatter)
logging.getLogger().addHandler(fileLog)

logger = logging.getLogger()

# upgrade check:
# -------------
# On each startup OMERO.web checks for possible server upgrades
# and logs the upgrade url at the WARNING level. If you would
# like to disable the checks, change the following to
#
#   if False:
#
# For more information, see
# http://trac.openmicroscopy.org.uk/omero/wiki/UpgradeCheck
#
try:
    from omero.util.upgrade_check import UpgradeCheck
    check = UpgradeCheck("web")
    check.run()
    if check.isUpgradeNeeded():
        logger.error("Upgrade is available. Please visit http://trac.openmicroscopy.org.uk/omero/wiki/MilestoneDownloads.\n")
except Exception, x:
    logger.error("Upgrade check error: %s" % x)

if not settings.EMAIL_NOTIFICATION:
    logger.error("Settings.py has not been configured. EmailServerError: The application will not send any emails. Sharing notification is not available.\n" )

if __name__ == "__main__":
    logger = logging.getLogger()
    logger.info("Application Starting...")
    
    execute_manager(settings)
