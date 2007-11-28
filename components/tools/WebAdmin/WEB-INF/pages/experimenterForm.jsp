<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
	<f:view>
		<div id="addform"><h:form id="experimenterForm">

			<h2><h:outputText value="#{msg.experimentersAddNew}"
				rendered="#{not IAEManagerBean.editMode}" /></h2>
			<h2><h:outputText value="#{msg.experimentersEdit}"
				rendered="#{IAEManagerBean.editMode}" /></h2>

			<p><h:outputText value="#{msg.generalMandatoryFields}" /></p>

			<h:inputHidden id="userid"
				value="#{IAEManagerBean.user.experimenter.id}"
				rendered="#{IAEManagerBean.editMode}" />

			<h:message styleClass="errorText" id="experimenterFormError"
				for="experimenterForm" />
			<br />

			<h:panelGrid columns="3" columnClasses="form, input">

				<h:outputText value="#{msg.experimentersOmeName}*" />

				<h:inputText id="omeName" maxlength="255"
					value="#{IAEManagerBean.user.experimenter.omeName}" required="true"
					validator="#{IAEManagerBean.validateOmeName}">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="omeNameError" for="omeName" />

				<h:outputText value="#{msg.experimentersFirstName}*" />

				<h:inputText id="firstName" maxlength="255"
					value="#{IAEManagerBean.user.experimenter.firstName}"
					required="true">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="firstNameError"
					for="firstName" />

				<h:outputText value="#{msg.experimentersMiddleName}" />

				<h:inputText id="middleName" maxlength="255"
					value="#{IAEManagerBean.user.experimenter.middleName}">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="middleNameError"
					for="middleName" />

				<h:outputText value="#{msg.experimentersLastName}*" />

				<h:inputText id="lastName" maxlength="255"
					value="#{IAEManagerBean.user.experimenter.lastName}"
					required="true">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="lastNameError" for="lastName" />

				<h:outputText value="#{msg.experimentersEmail}*" />

				<h:inputText id="email" maxlength="255"
					value="#{IAEManagerBean.user.experimenter.email}"
					validator="#{IAEManagerBean.validateEmail}" required="true">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="emailNameError" for="email" />

				<h:outputText value="#{msg.experimentersInstitution}" />

				<h:inputText id="institution" maxlength="255"
					value="#{IAEManagerBean.user.experimenter.institution}">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="institutionError"
					for="institution" />

				<h:outputText value="#{msg.experimentersAdminRole}" />

				<h:selectBooleanCheckbox title="adminRole"
					value="#{IAEManagerBean.user.adminRole}">
				</h:selectBooleanCheckbox>

				<h:outputText value=" " />

				<h:outputText value="#{msg.experimentersUserRole}" />

				<h:selectBooleanCheckbox title="userRole"
					value="#{IAEManagerBean.user.userRole}">
				</h:selectBooleanCheckbox>

				<h:outputText value=" " />

				<h:outputText value="#{msg.experimentersDefaultGroup}*" />

				<h:selectOneMenu id="defaultGroup"
					value="#{IAEManagerBean.user.defaultGroup}" required="true">
					<f:selectItems value="#{IAEManagerBean.defaultGroups}" />
				</h:selectOneMenu>

				<h:message styleClass="errorText" id="defaultError"
					for="defaultGroup" />

				<h:outputText value="#{msg.experimentersOtherGroups}" />

				<h:selectManyListbox id="otherGroup" size="5"
					value="#{IAEManagerBean.user.selectedGroups}">
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

		</h:form> <c:if test="${empty sessionScope.IAEManagerBean.user.dn}">
			<h:form id="passwd">

				<br />
				<h:graphicImage url="/images/add.png"
					rendered="#{IAEManagerBean.editMode}" />
				<h:commandLink action="#{IAEManagerBean.changePassword}"
					rendered="#{IAEManagerBean.editMode}">
					<h:outputText value="#{msg.experimentersChangePassword}" />
				</h:commandLink>
			</h:form>
		</c:if> <br />
		<c:if test="${not empty sessionScope.IAEManagerBean.user.dn}">
			<h:outputText value="#{msg.experimentersLdapInfo}" />
		</c:if></div>

	</f:view>
</c:if>
