<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode == true && sessionScope.LoginBean.role == true}">
	<f:view>
		<h1><h:outputText value="#{msg.experimentersAddNew}"
			rendered="#{not IAEManagerBean.editMode}" /></h1>
		<h1><h:outputText value="#{msg.experimentersEdit}"
			rendered="#{IAEManagerBean.editMode}" /></h1>

		<h:form id="experimenterForm">

			<h:inputHidden id="experimenterid"
				value="#{IAEManagerBean.experimenter.id}" />

			<h:message styleClass="errorText" id="experimenterFormError"
				for="experimenterForm" />
			<br />

			<h:panelGrid columns="3" columnClasses="form">

				<h:outputText value="#{msg.experimentersOmeName}" />

				<h:inputText id="omeName" maxlength="50"
					value="#{IAEManagerBean.experimenter.omeName}" required="true"
					validator="#{IAEManagerBean.validateOmeName}">
					<f:validateLength minimum="3" maximum="50" />
				</h:inputText>

				<h:message styleClass="errorText" id="omeNameError" for="omeName" />

				<h:outputText value="#{msg.experimentersFirstName}" />

				<h:inputText id="firstName" maxlength="255"
					value="#{IAEManagerBean.experimenter.firstName}" required="true">
					<f:validateLength minimum="3" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="firstNameError"
					for="firstName" />

				<h:outputText value="#{msg.experimentersMiddleName}" />

				<h:inputText id="middleName" maxlength="255"
					value="#{IAEManagerBean.experimenter.middleName}" />

				<h:message styleClass="errorText" id="middleNameError"
					for="middleName" />

				<h:outputText value="#{msg.experimentersLastName}" />

				<h:inputText id="lastName" maxlength="255"
					value="#{IAEManagerBean.experimenter.lastName}" required="true">
					<f:validateLength minimum="3" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="lastNameError" for="lastName" />

				<h:outputText value="#{msg.experimentersEmail}" />

				<h:inputText id="email" maxlength="100"
					value="#{IAEManagerBean.experimenter.email}" required="true"
					validator="#{IAEManagerBean.validateEmail}">
					<f:validateLength minimum="6" maximum="100" />
				</h:inputText>

				<h:message styleClass="errorText" id="emailNameError" for="email" />

				<h:outputText value="#{msg.experimentersInstitution}" />

				<h:inputText id="institution" maxlength="255"
					value="#{IAEManagerBean.experimenter.institution}" required="true">
					<f:validateLength maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="institutionError"
					for="institution" />

				<h:outputText value="#{msg.experimentersAdminRole}" />

				<h:selectBooleanCheckbox title="adminRole"
					value="#{IAEManagerBean.adminRole}">
				</h:selectBooleanCheckbox>

				<h:outputText value=" " />

				<h:outputText value="#{msg.experimentersUserRole}" />

				<h:selectBooleanCheckbox title="userRole"
					value="#{IAEManagerBean.userRole}">
				</h:selectBooleanCheckbox>

				<h:outputText value=" " />

				<h:outputText value="#{msg.experimentersDefaultGroup}" />

				<h:selectOneMenu id="defaultGroup"
					value="#{IAEManagerBean.defaultGroup}" required="true">
					<f:selectItems value="#{IAEManagerBean.defaultGroups}" />
				</h:selectOneMenu>

				<h:message styleClass="errorText" id="defaultError"
					for="defaultGroup" />

				<h:outputText value="#{msg.experimentersOtherGroups}" />

				<h:selectManyListbox id="otherGroup"
					value="#{IAEManagerBean.selectedGroup}">
					<f:selectItems value="#{IAEManagerBean.otherGroups}" />
				</h:selectManyListbox>

				<h:message styleClass="errorText" id="otherGroupError"
					for="otherGroup" />

			</h:panelGrid>

			<br />

			<h:commandButton id="submitAdd"
				action="#{IAEManagerBean.addExperimenter}"
				value="#{msg.experimentersSave}"
				rendered="#{not IAEManagerBean.editMode}" />

			<h:commandButton id="submitUpdate"
				action="#{IAEManagerBean.updateExperimenter}"
				value="#{msg.experimentersSave}"
				rendered="#{IAEManagerBean.editMode}" />

		</h:form>
		<br />
		<h:commandLink action="#{IAEManagerBean.changePassword}"
			rendered="#{IAEManagerBean.editMode}">
			<h:outputText value="#{msg.experimentersChangePassword}" />
		</h:commandLink>


	</f:view>
</c:if>
