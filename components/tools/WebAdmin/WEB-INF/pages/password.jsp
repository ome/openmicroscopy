<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
	<f:view>
		<div id="hello"><h:form id="log">
			<h1><h:outputText value="#{msg.headerHello} #{sessionScope.LoginBean.username}" />!
			<h:commandLink action="#{LoginBean.logout}"
				title="#{msg.headerLogout}">
				<h:outputText value=" #{msg.headerLogout}" />
			</h:commandLink></h1>		
		</h:form></div>
		
		<div id="addform"><h:form id="changePassword">
		
		<h2><h:outputText
				value="#{msg.experimentersChangePassword}" /></h2>
		
		<p><h:outputText value="#{msg.generalMandatoryFields}"/></p>
		
			<h:message styleClass="errorText" id="changePasswordError"
				for="changePassword" />
			<br />

			<h:inputHidden id="userid"
				value="#{IAEManagerBean.user.experimenter.id}" />

			<h:panelGrid columns="3" columnClasses="form">

				<h:outputText value="#{msg.myaccountPassword}*" />

				<h:inputSecret id="password" value="#{IAEManagerBean.password}"
					maxlength="255">
					<f:validateLength maximum="255" />
				</h:inputSecret>

				<h:message styleClass="errorText" id="passwordError" for="password" />

				<h:outputText value="#{msg.myaccountPassword2}*" />

				<h:inputSecret id="password2" value="#{IAEManagerBean.password2}"
					maxlength="255">
					<f:validateLength maximum="255" />
				</h:inputSecret>

				<h:message styleClass="errorText" id="password2Error"
					for="password2" />

			</h:panelGrid>

			<h:commandButton id="submitUpdate"
				action="#{IAEManagerBean.updatePassword}"
				value="#{msg.myaccountSave}" rendered="#{IAEManagerBean.editMode}" />
		</h:form></div>
	</f:view>
</c:if>
