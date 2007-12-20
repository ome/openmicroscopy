#!/usr/bin/env python
#
# OMERO Registry Map Generator
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#

import re
import db

template = """
      var point = new GLatLng(%s,%s);
      var marker = createMarker(point,'%s');
      map.addOverlay(marker);
      """

def gethits(accessdb):
    hits = []
    for hit in accessdb:
        if hit[0] != "unknown":
            hits.append("new YGeoPoint(%s, %s)" % (lat, lon))

def yahoo(locations = ["Dundee, Scotland"]):
    # Corey Goldberg, April 2007 (corey@goldb.org)
    marker_size = 13
    zoom_level = 15
    height = '550px'
    width = '800px'
    center = 'Dundee, Scotland'  # city or zip
    map_type = 'YAHOO_MAP_HYB'  # YAHOO_MAP_REG, YAHOO_MAP_SAT, YAHOO_MAP_HYB

    fh = open('site/geomap.html', 'w')

    fh.write("""\
<html>
    <head>
        <title>Geolocation Map</title>
        <script type="text/javascript"
            src="http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=YahooDemo">
        </script>
        <style type="text/css">
            body { padding: 0; margin: 0; }
            #mapContainer { height: %s; width: %s; }
        </style>
    </head>
    <body>
    <div id="mapContainer"></div>
        <script type="text/javascript">
            function createYahooMarker(geopoint, size) {
                var myImage = new YImage();
                myImage.src = './marker.gif';
                myImage.size = new YSize(size, size);
                var marker = new YMarker(geopoint, myImage);
                return marker;
            }
            var map = new  YMap(document.getElementById('mapContainer'), %s);
            map.addPanControl();
            map.addZoomLong();
            map.drawZoomAndCenter("%s", %d);""" % (height, width, map_type, center, zoom_level))

    for location in locations:
        fh.write("""
            map.addOverlay(new createYahooMarker(%s, %s));""" % (location.strip(), marker_size))

    fh.write("""
        </script>
    </body>
    </html>""")

if __name__ == "__main__":
    for line in mapparse():
        print line
else:

    yahoo(gethits())

    import os
    dir = os.path.join(os.path.abspath('.'),"site")
    html = os.path.join(dir, "ourmap.html")
    dynamic = open(html,"w")
    header = open("top.html","r")
    footer = open("bottom.html","r")
    try:
        for line in header:
            dynamic.write(line)
        for line in mapparse():
            dynamic.write(line)
        for line in footer:
            dynamic.write(line)
    except e:
        header.close()
        footer.close()
        dynamic.close()
        raise e

    from twisted.application import internet, service
    from twisted.web import static, server
    from twisted.web.static import File

    root = File(dir)
    application = service.Application('web')
    site = server.Site(root)
    sc = service.IServiceCollection(application)
    i = internet.TCPServer(9999, site)
    i.setServiceParent(sc)

