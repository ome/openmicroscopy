In order to enable the EMAN2 and caching functionality required for some pages of webemdb, you need to install EMAN2 so that 
from EMAN2 import *
produces no errors and then configure the following options:
$ bin/omero config set omero.web.use_eman2 True
$ bin/omero config set omero.web.cache_backend 'file:///var/tmp/django_cache'
