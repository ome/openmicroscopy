<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />


<c:if test="${sessionScope.LoginBean.mode}">

	<h1>${msg.mainWelcome}</h1>

</c:if>

<c:if
	test="${empty sessionScope.LoginBean.mode or sessionScope.LoginBean.mode == false}">

	<f:view>
		<h:form id="loginForm">

			<img src="<c:url value='images/logo1.png' />" />

			<h1>Login</h1>

			<h:message styleClass="errorText" id="loginFormError" for="loginForm" />

			<br />

			<h:panelGrid columns="3" columnClasses="form">

				<h:outputText value="#{msg.mainServer}" />

				<h:inputText id="server" value="#{LoginBean.server}" required="true"
					size="30">
				</h:inputText>

				<h:message styleClass="errorText" id="serverError" for="server" />

				<h:outputText value="#{msg.mainPort}" />

				<h:inputText id="port" value="#{LoginBean.port}" required="true"
					size="10">
				</h:inputText>

				<h:message styleClass="errorText" id="portError" for="port" />

				<h:outputText value="#{msg.mainLogin}" />

				<h:inputText id="username" value="#{LoginBean.username}"
					required="true">
				</h:inputText>

				<h:message styleClass="errorText" id="usernameError" for="username" />

				<h:outputText value="#{msg.mainPassword}" />

				<h:inputSecret id="password" value="#{LoginBean.password}">
				</h:inputSecret>

				<h:message styleClass="errorText" id="nameError" for="password" />

			</h:panelGrid>

			<div id="button"><h:commandButton id="submit"
				action="#{LoginBean.login}" value="#{msg.mainOk}" /></div>

			<div id="button"><a href="forgottenPassword.jsf">${msg.mainForgPasswd}</a>
			<a href="javascript:openHelp()">${msg.mainHelp}</a></div>
		</h:form>

	</f:view>

</c:if>
