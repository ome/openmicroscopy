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
		<li><a href="./space.jsf"
			<c:if test="${sessionScope.LoginBean.page == 'space.jsf'}" >class="menu"</c:if>>${msg.headerSpace}</a></li>
		<li><a href="./imports.jsf"
			<c:if test="${sessionScope.LoginBean.page == 'imports.jsf' || sessionScope.LoginBean.page == 'uploadFile.jsf' || sessionScope.LoginBean.page == 'importForm.jsf'}" >class="menu"</c:if>>${msg.headerUpload}</a></li>
		<li><a href="./experimenters.jsf"
			<c:if test="${sessionScope.LoginBean.page == 'experimenters.jsf' || sessionScope.LoginBean.page == 'experimenterForm.jsf'}" >class="menu"</c:if>>${msg.headerExperimenters}</a></li>
		<li><a href="./groups.jsf"
			<c:if test="${sessionScope.LoginBean.page == 'groups.jsf' || sessionScope.LoginBean.page == 'groupsForm.jsf' || sessionScope.LoginBean.page == 'editInGroup.jsf'}" >class="menu"</c:if>>${msg.headerGroups}</a></li>
	</c:if>
	<c:if test="${sessionScope.LoginBean.mode}">
		<li><a href="./myAccount.jsf"
			<c:if test="${sessionScope.LoginBean.page == 'myAccount.jsf'}" >class="menu"</c:if>>${msg.headerMyAccount}</a></li>
		<li><a href="./logout">${msg.headerLogout}</a></li>
	</c:if>

</ul>

</div>

<c:if test="${sessionScope.LoginBean.mode}">
	<div id="hello">
	<h1>${msg.headerHello} <c:out
		value="${sessionScope.LoginBean.username}" />!</h1>
	</div>
</c:if></div>
