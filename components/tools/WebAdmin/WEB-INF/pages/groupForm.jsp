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
		
		<div id="addform"><h:form id="groupForm">

			<h2><h:outputText value="#{msg.groupsAddNewGroup}"
				rendered="#{not IAGManagerBean.editMode}" /></h2>
			<h2><h:outputText value="#{msg.groupsEditGroup}"
				rendered="#{IAGManagerBean.editMode}" /></h2>

			<p><h:outputText value="#{msg.generalMandatoryFields}" /></p>

			<h:inputHidden id="groupid" value="#{IAGManagerBean.group.id}"
				rendered="#{IAGManagerBean.editMode}" />

			<h:message styleClass="errorText" id="groupFormError" for="groupForm" />
			<br />

			<h:panelGrid columns="3" columnClasses="form, input">

				<h:outputText value="#{msg.groupsGroupName}*" />

				<h:inputText id="name" value="#{IAGManagerBean.group.name}"
					validator="#{IAGManagerBean.validateGroupName}" required="true"
					maxlength="255" size="30">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="nameError" for="name" />

				<h:outputText value="#{msg.groupsDescription}" />

				<h:inputText id="description" maxlength="255" size="30"
					value="#{IAGManagerBean.group.description}" />

				<h:message styleClass="errorText" id="descriptionError"
					for="description" />

				<h:outputText value="#{msg.groupsOwner}*" />

				<h:selectOneMenu id="owner"
					value="#{IAGManagerBean.owner}" required="true">
					<f:selectItems value="#{IAGManagerBean.experimenters}" />
				</h:selectOneMenu>

				<h:message styleClass="errorText" id="ownerError"
					for="owner" />


			</h:panelGrid>

			<br />

			<h:commandButton id="submitAdd" action="#{IAGManagerBean.addGroup}"
				value="#{msg.groupsSave}" rendered="#{not IAGManagerBean.editMode}" />

			<h:commandButton id="submitUpdate"
				action="#{IAGManagerBean.updateGroup}" value="#{msg.groupsSave}"
				rendered="#{IAGManagerBean.editMode}" />

		</h:form></div>
	</f:view>
</c:if>
