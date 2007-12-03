#!/usr/bin/env PYTHONPATH=/tmp/geoip/lib/python2.4/site-packages/ LD_LIBRARY_PATH=/tmp/geoip/lib/ python -v

import re

check_re = re.compile("^(.*)\sOMERO.*Check:\s(\S+)\s")
redirect_re = re.compile("^(.*)\sOMERO.*Redirected:\s(\S+)\s")
template = """
      var point = new GLatLng(%s,%s);
      var marker = createMarker(point,'%s');
      map.addOverlay(marker);
      """

hi_url = "http://api.hostip.info/get_html.php?position=true&ip="
lat_re = re.compile("^Latitude:\s(\S*)")
lon_re = re.compile("^Longitude:\s(\S*)")

def GeoIp(ipaddr):
    import GeoIP
    if not hasattr(GeoIP,"gi"):
        GeoIP.gi = GeoIP.open("//tmp/geoip/share/GeoIP/GeoIPCity.dat",GeoIP.GEOIP_STANDARD)

    gir = GeoIP.gi.record_by_addr(ipaddr)
    lat = gir["latitude"]
    lon = gir["longitude"]
    return (lat,lon)

def hostinfo(ipaddr):
    import urllib, exceptions
    sock = urllib.urlopen(hi_url + ipaddr)
    value = sock.read()
    sock.close()
    for line in value.splitlines():
        print line
        m_lat = lat_re.match(line)
        m_lon = lon_re.match(line)
        if m_lat:
            lat = m_lat.group(1)
        elif m_lon:
            long = m_lon.group(1)
    if not lat or not lon:
        raise exceptions.Exception("No lat or lon found")
    return (lat,lon)

def mapparse(filename="../access.log", onlyLocations=False, iplookup=GeoIp):
    f = open(filename,"r")
    lines = []
    try:
        for line in f:
            line = line.strip()
            match = check_re.match(line)
            if match:
                date = match.group(1)
                ipaddr = match.group(2)
                try:
                    (lat,lon) = iplookup(ipaddr)

                    if onlyLocations:
                        lines.append("new YGeoPoint(%s, %s)" % (lat, lon))
                    else:
                        txt = """ %s on %s """ % (ipaddr, date)
                        lines.append(template % (lat, lon, txt))
                except:
                    import traceback
                    traceback.print_exc()
                    if not onlyLocations:
                        lines.append("// unknown, probably local access")
            else:
                print "redirect"
                if not onlyLocations:
                    lines.append("// nomatch, probably redirect")
    finally:
        f.close()
    return lines

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

    yahoo(mapparse(onlyLocations=True))

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

