For the purpose of this text, the following assumptions are made:
- DIST is the full path to the directory where you installed omero
- Only tested on Mac, so paths and path separators may be different
- You have omero and omeroweb fully installed and running

Using webmobile as part of omeroweb:

- add the webmobile media to the webgateway media repository using a symbolic link. 
$ cd DIST/lib/python/omeroweb/media
$ ln -s DIST/lib/python/omeroweb/webmobile/media/webmobile webmobile

- edit DIST/lib/python/omeroweb/settings.py, adding "omeroweb.webmobile" to INSTALLED_APPS
- edit DIST/lib/python/omeroweb/urls.py, adding "(r'(?i)^webmobile/', include('omeroweb.webmobile.urls'))" to urlpatterns
- restart omeroweb


