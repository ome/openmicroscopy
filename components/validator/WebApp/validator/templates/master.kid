<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<?python import sitetemplate ?>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#" py:extends="sitetemplate">

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
		<div id="footer"> All material &copy;2007 OMERO Team </div>
	</div>

</body>

</html>
