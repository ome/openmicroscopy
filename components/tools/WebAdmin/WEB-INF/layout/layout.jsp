<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="t" uri="http://jakarta.apache.org/struts/tags-tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><t:getAsString name="title" /></title>
<link rel="stylesheet" type="text/css" href="<c:url value='/css/main.css' />">
</head>

<body>
<div id="wrapper">
<div id="header"><t:insert attribute="header" flush="false"
	ignore="false" /></div>

<div id="center"><t:insert attribute="content" flush="false"
	ignore="false" /></div>

<div id="footer"><t:insert attribute="footer" flush="false"
	ignore="false" /></div>
</div>
</body>
<!-- InstanceEnd -->
</html>

