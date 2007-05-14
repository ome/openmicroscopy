<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib prefix="t" uri="http://jakarta.apache.org/struts/tags-tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title><t:getAsString name="title" /></title>

<link rel="stylesheet" href="<c:url value='css/main.css' />"
	type="text/css">
<link rel="shortcut icon" href="<c:url value='images/ome.ico' />"
	type="image/x-icon" />
</head>
<body>

<div id="login"><t:insert attribute="content" flush="false"
	ignore="false" /></div>
<div id="bottom"><t:insert attribute="footer" flush="false"
	ignore="false" /></div>

</body>
</html>
