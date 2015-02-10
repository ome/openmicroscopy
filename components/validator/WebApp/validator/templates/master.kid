<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<?python import sitetemplate ?>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#" py:extends="sitetemplate">
    <!--
    * Copyright (C) 2007-2012 University of Dundee & Open Microscopy Environment.
    * All Rights Reserved. 
    -->
<head py:match="item.tag=='{http://www.w3.org/1999/xhtml}head'" py:attrs="item.items()">
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title py:replace="''">Your title goes here</title>
    <meta py:replace="item[:]"/>
    <style type="text/css" media="screen">
@import "${tg.url('/static/css/style.css')}";
</style>
</head>

<body py:match="item.tag=='{http://www.w3.org/1999/xhtml}body'" py:attrs="item.items()">
	<div id="wrapper">
		<div id="banner">
			<a href="${tg.url('http://www.ome-xml.org/')}"><img src="/static/images/ome_xml_trac_banner.png" alt="OME-XML" /></a>
			<div id="navigation" class="nav"><ul>
				<li><a href="${tg.url('/fileslist')}">File list</a></li>
				<li><a href="${tg.url('/')}">Validator</a></li>
			</ul></div>
		</div>
		<div id="main_content">
			<div id="status_block" class="flash" py:if="value_of('tg_flash', None)" py:content="tg_flash"></div>
			<div py:replace="[item.text]+item[:]"/>
		</div>
        <div id="problem" style=>
            <h2>Problem with validator</h2>
            <p>We are experiencing a problem with the online validator hanging while validating files. This appears to be a problem related to the combination of XML parser and web application toolkit we are using. Unfortunately we cannot get this to work reliably so will have to completely re-write the validator. If you need to validate files we recommend to do so using the Bio-Formats command line <code>bftools</code>.</p>
            <p>For further information check the <a href="https://www.openmicroscopy.org/site/support/ome-model/ome-tiff/tools.html#validating-ome-xml">OME Model and Formats Documentation</a>. </p>
            <p>We are sorry for the inconvenience this may cause. </p>
        </div>
		<div id="footer"> Copyright &copy;2007-2012 University of Dundee &amp; Open Microscopy Environment.</div>
	</div>

</body>

</html>
