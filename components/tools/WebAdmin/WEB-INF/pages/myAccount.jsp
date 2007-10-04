<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />


<c:if test="${sessionScope.LoginBean.mode}">
	<f:view>
		<div id="addform"><h:form id="experimenterForm">

			<h2><h:outputText value="#{msg.myaccountEdit}" /></h2>

			<h:inputHidden id="userid" 
				value="#{IAMAManagerBean.user.experimenter.id}" />

			<h:message styleClass="errorText" id="experimenterFormError"
				for="experimenterForm" />
			<br />

			<h:panelGrid columns="3" columnClasses="form, input">

				<h:outputText value="#{msg.myaccountOmeName}" />

				<h:outputText value="#{IAMAManagerBean.user.experimenter.omeName}" />

				<h:outputText value=" " />

				<h:outputText value="#{msg.myaccountFirstName}*" />

				<h:inputText id="firstName" maxlength="255"
					value="#{IAMAManagerBean.user.experimenter.firstName}" required="true">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="firstNameError"
					for="firstName" />

				<h:outputText value="#{msg.myaccountMiddleName}" />

				<h:inputText id="middleName" maxlength="255"
					value="#{IAMAManagerBean.user.experimenter.middleName}">
					<f:validateLength maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="middleNameError"
					for="middleName" />

				<h:outputText value="#{msg.myaccountLastName}*" />

				<h:inputText id="lastName" maxlength="255"
					value="#{IAMAManagerBean.user.experimenter.lastName}" required="true">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="lastNameError" for="lastName" />

				<h:outputText value="#{msg.myaccountEmail}*" />

				<h:inputText id="email" maxlength="255"
					value="#{IAMAManagerBean.user.experimenter.email}"
					validator="#{IAMAManagerBean.validateEmail}">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="emailNameError" for="email" />

				<h:outputText value="#{msg.myaccountInstitution}" />

				<h:inputText id="institution" maxlength="255"
					value="#{IAMAManagerBean.user.experimenter.institution}">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="institutionError"
					for="institution" />

				<h:outputText value="#{msg.myaccountDefaultGroup}*" />

				<h:selectOneMenu id="defaultGroup"
					value="#{IAMAManagerBean.user.defaultGroup}" required="true">
					<f:selectItems value="#{IAMAManagerBean.myGroups}" />
				</h:selectOneMenu>

				<h:message styleClass="errorText" id="defaultError"
					for="defaultGroup" />

			</h:panelGrid>
			<br/>
			<h:commandButton id="submitUpdate"
				action="#{IAMAManagerBean.updateExperimenter}"
				value="#{msg.myaccountSave}" />
				
		</h:form>
		 
		<c:if test="${empty sessionScope.IAMAManagerBean.user.dn}">
			<h:form id="passwd">
				<br />
				<h:graphicImage url="/images/add.png" />
				<h:commandLink action="#{IAMAManagerBean.changeMyPassword}">
					<h:outputText value="#{msg.myaccountChangePassword}" />
				</h:commandLink>
			</h:form>
		</c:if>
		
		<c:if test="${sessionScope.IAEManagerBean.user.dn}">
			<h:outputText value="#{msg.myaccountLdapInfo}"/>
		</c:if></div>

	</f:view>
</c:if>
