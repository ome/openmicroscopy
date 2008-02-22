<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${!sessionScope.LoginBean.mode && !sessionScope.LoginBean.role}">
	<f:view>
		<div id="addform">
		<h2><h:outputText value="#{msg.forgottenPassword}" /></h2>

		<p><h:outputText value="#{msg.forgottenPasswordInfo}" /></p>
		<p><h:outputText value="#{msg.generalMandatoryFields}" /></p>

		<h:form id="forgottenPassword" rendered="#{not PasswordBean.typeForm}">

			<h:message styleClass="errorText" id="forgottenPasswordError"
				for="forgottenPassword" />

			<h:panelGrid columns="3" columnClasses="form">

				<h:outputText value="#{msg.forgottenServer}*" />

				<h:inputText id="server" maxlength="255" size="30"
					value="#{PasswordBean.server}" required="true">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="serverError" for="server" />

				<h:outputText value="#{msg.forgottenPort}*" />

				<h:inputText id="port" maxlength="255" size="30"
					value="#{PasswordBean.port}" required="true">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="portError" for="port" />

				<h:outputText value="#{msg.forgottenOmeName}*" />

				<h:inputText id="omeName" maxlength="255" size="30"
					value="#{PasswordBean.omeName}" required="true">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="omeNameError" for="omeName" />

				<h:outputText value="#{msg.forgottenEmail}*" />

				<h:inputText id="email" maxlength="255" size="30"
					value="#{PasswordBean.email}" required="true">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="emailError" for="email" />

			</h:panelGrid>

			<br />

			<div id="button"><h:commandButton id="submitChange"
				action="#{PasswordBean.resetPassword}" value="#{msg.myaccountSave}" /></div>

		</h:form></div>
	</f:view>
</c:if>
