<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />


<c:if test="${sessionScope.LoginBean.mode}">
	<f:view>
		<h1><h:outputText value="#{msg.myaccountEdit}" /></h1>
		<h:form id="experimenterForm">

			<h:inputHidden id="experimenterid"
				value="#{IAMAManagerBean.experimenter.id}" />

			<h:message styleClass="errorText" id="experimenterFormError"
				for="experimenterForm" />
			<br />

			<h:panelGrid columns="3" columnClasses="form">

				<h:outputText value="#{msg.myaccountOmeName}" />

				<h:outputText value="#{IAMAManagerBean.experimenter.omeName}" />
				<h:outputText value=" " />

				<h:outputText value="#{msg.myaccountFirstName}" />

				<h:inputText id="firstName" maxlength="255"
					value="#{IAMAManagerBean.experimenter.firstName}" required="true">
					<f:validateLength minimum="3" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="firstNameError"
					for="firstName" />

				<h:outputText value="#{msg.myaccountMiddleName}" />

				<h:inputText id="middleName" maxlength="255"
					value="#{IAMAManagerBean.experimenter.middleName}" />

				<h:message styleClass="errorText" id="middleNameError"
					for="middleName" />

				<h:outputText value="#{msg.myaccountLastName}" />

				<h:inputText id="lastName" maxlength="255"
					value="#{IAMAManagerBean.experimenter.lastName}" required="true">
					<f:validateLength minimum="3" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="lastNameError" for="lastName" />

				<h:outputText value="#{msg.myaccountEmail}" />

				<h:inputText id="email" maxlength="100"
					value="#{IAMAManagerBean.experimenter.email}" required="true"
					validator="#{IAMAManagerBean.validateEmail}">
					<f:validateLength minimum="6" maximum="100" />
				</h:inputText>

				<h:message styleClass="errorText" id="emailNameError" for="email" />

				<h:outputText value="#{msg.myaccountInstitution}" />

				<h:inputText id="institution" maxlength="255"
					value="#{IAMAManagerBean.experimenter.institution}" required="true">
					<f:validateLength maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="institutionError"
					for="institution" />

				<h:outputText value="#{msg.myaccountDefaultGroup}" />

				<h:selectOneMenu id="defaultGroup"
					value="#{IAMAManagerBean.defaultGroup}" required="true">
					<f:selectItems value="#{IAMAManagerBean.myGroups}" />
				</h:selectOneMenu>

				<h:message styleClass="errorText" id="defaultError"
					for="defaultGroup" />

			</h:panelGrid>

			<h:commandButton id="submitUpdate"
				action="#{IAMAManagerBean.updateExperimenter}"
				value="#{msg.myaccountSave}" />

		</h:form>

		<h:form id="changePassword">

			<h:message styleClass="errorText" id="changePasswordError"
				for="changePassword" />
			<br />

			<h:inputHidden id="experimenterid"
				value="#{IAMAManagerBean.experimenter.id}" />

			<h:panelGrid columns="3" columnClasses="form">

				<h:outputText value="#{msg.myaccountPassword}" />

				<h:inputSecret id="password" value="#{IAMAManagerBean.password}"
					maxlength="100">

				</h:inputSecret>

				<h:message styleClass="errorText" id="passwordError" for="password" />

				<h:outputText value="#{msg.myaccountPassword2}" />

				<h:inputSecret id="password2" value="#{IAMAManagerBean.password2}"
					maxlength="100">

				</h:inputSecret>

				<h:message styleClass="errorText" id="password2Error"
					for="password2" />


			</h:panelGrid>
			<h:commandButton id="submitUpdate"
				action="#{IAMAManagerBean.updateMyPassword}"
				value="#{msg.myaccountSave}" />

		</h:form>
	</f:view>
</c:if>
