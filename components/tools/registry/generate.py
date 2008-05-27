#!/usr/bin/env python
#
# OMERO Registry Map Generator
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
#

import os
import re
import db

template = """
      var point = new GLatLng(%s,%s);
      var marker = createMarker(point,'%s');
      map.addOverlay(marker);
      """

def gethits():
    accessdb = db.accessdb()
    cur = accessdb.conn.cursor()
    cur.execute("SELECT hit.agent, ip.latitude, ip.longitude, count(hit.ip), hit.ip FROM IP ip, HIT hit where hit.ip = ip.id group by hit.agent, hit.ip")
    hits = []
    try:
        for hit in cur:
            if hit[1] != "unknown":
	    	if hit[0] == "OMERO.server":
			hits.append(("./server.png","new YGeoPoint(%s, %s)" % (hit[1], hit[2]), hit[3]))
		elif hit[0] == "OMERO.insight":
			hits.append(("./insight.png","new YGeoPoint(%s, %s)" % (hit[1], hit[2]), hit[3]))
		elif hit[0] == "OMERO.editor":
			hits.append(("./editor.png","new YGeoPoint(%s, %s)" % (hit[1], hit[2]), hit[3]))
		elif hit[0] == "OMERO.importer":
			hits.append(("./importer.png","new YGeoPoint(%s, %s)" % (hit[1], hit[2]), hit[3]))
		else:
			pass
			#hits.append(("./YellowDot.png","new YGeoPoint(%s, %s)" % (hit[1], hit[2]), hit[3]))
    finally:
        accessdb.close()
    return hits


def yahoo(locations = {}):
    # Corey Goldberg, April 2007 (corey@goldb.org)
    zoom_level = 16
    center = 'Dundee, Scotland'  # city or zip
    map_type = 'YAHOO_MAP_SAT'  # YAHOO_MAP_REG, YAHOO_MAP_SAT, YAHOO_MAP_HYB

    fh = open('ourmap/site/geomap.html', 'w')

    fh.write("""\
<html>
    <head>
        <title>OMERO.registry</title>

<script>
<!--

/*
Textual Tooltip Script- 
(C) Dynamic Drive (www.dynamicdrive.com)
For full source code, installation instructions,
100's more DHTML scripts, and Terms Of
Use, visit dynamicdrive.com
*/


var content=new Array()
""")

    i = 0
    for imagePath, location, count in locations:
    	if count < 10:
		marker_size = 25
	elif count < 100:
		marker_size = 50
	else:
		marker_size = 75
        fh.write("""
	content[%d]='<span class="infotitle">Info: %d </span><span class="infotext">starts for that site. </span>';""" % (i, count))
	i = i + 1

    fh.write("""\

function regenerate(){
window.location.reload()
}
function regenerate2(){
if (document.layers){
appear()
setTimeout("window.onresize=regenerate",450)
}
}

function changetext(whichcontent){

if (document.all||document.getElementById){
cross_el=document.getElementById? document.getElementById("descriptions"):document.all.descriptions
cross_el.innerHTML='<font face="Verdana"><small>'+whichcontent+'<font></small>'
}
else if (document.layers){
document.d1.document.d2.document.write('<font face="Verdana"><small>'+whichcontent+'</small></font>')
document.d1.document.d2.document.close()
}

}

function appear(){
document.d1.visibility='show'
}

window.onload=regenerate2


//-->
</script>

        <script type="text/javascript"
            src="http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=otFF743V34GGErRPpNlWOIHdUNkm5hLPHxQS3_ZoCX5gdFZVRlvmGKg2HGHPE_iuFF8pqOI-">
        </script>
        <style type="text/css">

	    .infotitle { font-family: Verdana,Helvetica,sans serif; font-weight: bold; }
	    .infotext {}
	    
	    #logo { position: absolute; top: 0px; left: 0px; }
	    #text { position: absolute; top: 33px; left: 250px; width: 500px; }
            #mapContainer { position: absolute; top: 210px; left: 0px; width: 100%%; height:100%%; margin: 0 auto; }
            body { padding: 0; margin: 0; }
        </style>
    </head>
    <body>

    <div id="logo">
    	<map name="GraffleExport">
            <area shape=rect coords="58,118,117,177" href="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroImporter">
	    <area shape=rect coords="125,118,184,177" href="https://trac.openmicroscopy.org.uk/shoola/wiki/OmeroEditor">
	    <area shape=rect coords="125,51,184,110" href="https://trac.openmicroscopy.org.uk/shoola">
	    <area shape=rect coords="58,51,117,110" href="https://trac.openmicroscopy.org.uk/omero/">
	</map>
	<img border=0 src="./logo2.png" usemap="#GraffleExport">
    </div>

    <div id="text">
    	<p>On each startup up, all OMERO software products check back with the url <a href="http://upgrade.openmicroscopy.org.uk/">http://upgrade.openmicroscopy.org.uk/</a> which logs the access with the <a href="https://trac.openmicroscopy.org.uk/omero/wiki/OmeroRegistry">OMERO.registry</a> which generates this page.</p>
    	<p>Each dot represents several application starts at a particular IP address. More information is available on the <a href="./statistics.html">statistics page</a>.</p>

      <ilayer id="d1" width="120" height="120" visibility="hide">
        <layer id="d2" width="120" height="120">
          <div id="descriptions" align="left">

           <p> <span class="infotitle">Info: </span><span class="infotext">Information about each location on mouseover.</span> </p>

	  </div>
        </layer>
      </ilayer>
    
    </div>

    <div id="mapContainer"></div>

    <script type="text/javascript">
            function createYahooMarker(imagePath, geopoint, i, size) {
                var myImage = new YImage();
                myImage.src = imagePath;
                myImage.size = new YSize(size, size);
                var marker = new YMarker(geopoint, myImage);
		YEvent.Capture(marker, EventsList.MouseOver, showCount); 
		function showCount() { changetext(content[i]); }
                return marker;
            }
            var map = new  YMap(document.getElementById('mapContainer'), %s);

            map.addPanControl();
            map.addZoomLong();
	    map.addTypeControl();
            map.drawZoomAndCenter("%s", %d);""" % (map_type, center, zoom_level))

    i = 0
    for imagePath, location, count in locations:
    	if count < 10:
		marker_size = 25
	elif count < 100:
		marker_size = 50
	else:
		marker_size = 75
        fh.write("""
        map.addOverlay(new createYahooMarker('%s', %s, %s, %s));""" % (imagePath, location.strip(), i, marker_size))
        i = i + 1

    fh.write("""
    </script>
  </body>
</html>""")
    fh.close()

    fh2 = open('ourmap/site/statistics.html', 'w')
    fh2.write("""
	<h1>Statistics</h1>
	<pre>""")
    fh2.flush()
    import subprocess, os
    p = subprocess.Popen(["./count.py"],stdout=fh2)
    os.waitpid(p.pid,0)
    fh2.flush()
    fh2.write("""
	</pre>
    </body>
    </html>""")
    fh2.close()

if __name__ == "__main__":
    for line in gethits():
        print line
else:

    yahoo(gethits())

    from twisted.application import internet, service
    from twisted.web import static, server
    from twisted.web.static import File

    dir = os.path.join(os.path.abspath('.'),"ourmap","site")
    root = File(dir)
    application = service.Application('web')
    site = server.Site(root)
    sc = service.IServiceCollection(application)
    i = internet.TCPServer(9999, site)
    i.setServiceParent(sc)

