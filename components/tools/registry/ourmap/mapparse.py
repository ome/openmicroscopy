#!/usr/bin/env PYTHONPATH=/tmp/geoip/lib/python2.4/site-packages/ LD_LIBRARY_PATH=/tmp/geoip/lib/ python

import GeoIP, re

gi = GeoIP.open("//tmp/geoip/share/GeoIP/GeoIPCity.dat",GeoIP.GEOIP_STANDARD)
check_re = re.compile("^(.*)\sOMERO.*Check:\s(\S+)\s")
redirect_re = re.compile("^(.*)\sOMERO.*Redirected:\s(\S+)\s")
template = """
      var point = new GLatLng(%s,%s);
      var marker = createMarker(point,'%s');
      map.addOverlay(marker);
      """
def mapparse(filename="../access.log"):
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
                    gir = gi.record_by_addr(ipaddr)
                    lat = gir["latitude"]
                    lon = gir["longitude"]
                    txt = """ %s on %s """ % (ipaddr, date)
                    lines.append(template % (lat, lon, txt))
                except:
                    lines.append("// unknown, probably local access")
            else:
                lines.append("// nomatch, probably redirect")
    finally:
        f.close()
    return lines

if __name__ == "__main__":
    for line in mapparse():
        print line
else:

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

