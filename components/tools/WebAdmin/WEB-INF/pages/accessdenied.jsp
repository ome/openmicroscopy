<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<h1>${msg.errorAccessDenied}</h1>

<p>${msg.errorAccessDeniedMsg} <a href="./myAccount.jsf">${msg.myaccountEdit}</a>,
<a href="./logout">${msg.headerLogout}</a>, <a href="javascript:openHelp()">${msg.headerHelp}</a></p>

