<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<div class="top_row">
<div id="logo"><img src="<c:url value='images/logo.jpg' />"
	alt="Omero Admin" /></div>
</div>
<div class="nav">
<div id="navigation">
<ul>
	<c:if
		test="${sessionScope.LoginBean.role && sessionScope.LoginBean.mode}">
		<li><a href="./space.jsf">${msg.headerSpace}</a></li>
		<li><a href="./imports.jsf">${msg.headerUpload}</a></li>
		<li><a href="./experimenters.jsf">${msg.headerExperimenters}</a>
		</li>
		<li><a href="./groups.jsf">${msg.headerGroups}</a></li>
	</c:if>
	<c:if test="${sessionScope.LoginBean.mode}">
		<li><a href="./myAccount.jsf">${msg.headerMyAccount}</a></li>
		<li><a href="./logout">${msg.headerLogout}</a></li>
	</c:if>

</ul>

</div>

<c:if test="${sessionScope.LoginBean.mode}">
	<div id="hello">
	<h1>${msg.headerHello} <c:out
		value="${sessionScope.LoginBean.username}" />!</h1>
	</div>
</c:if>

</div>
