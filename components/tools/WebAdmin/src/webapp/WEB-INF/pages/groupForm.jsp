<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode == true && sessionScope.LoginBean.role == true}">
	<f:view>

		<h1><h:outputText value="#{msg.groupsAddNewGroup}"
			rendered="#{not IAGManagerBean.editMode}" /></h1>
		<h1><h:outputText value="#{msg.groupsEditGroup}"
			rendered="#{IAGManagerBean.editMode}" /></h1>

		<h:form id="groupForm">

			<h:inputHidden id="groupid" value="#{IAGManagerBean.group.id}" />

			<h:message styleClass="errorText" id="groupFormError" for="groupForm" />
			<br />

			<h:panelGrid columns="3" columnClasses="form">

				<h:outputText value="#{msg.groupsGroupName}" />

				<h:inputText id="name" value="#{IAGManagerBean.group.name}"
					required="true">
					<f:validateLength minimum="3" maximum="25" />
				</h:inputText>

				<h:message styleClass="errorText" id="nameError" for="name" />

				<h:outputText value="#{msg.groupsDescription}" />

				<h:inputText id="description"
					value="#{IAGManagerBean.group.description}" />

				<h:message styleClass="errorText" id="descriptionError"
					for="description" />

			</h:panelGrid>

			<br />

			<h:commandButton id="submitAdd" action="#{IAGManagerBean.addGroup}"
				value="#{msg.groupsSave}" rendered="#{not IAGManagerBean.editMode}" />

			<h:commandButton id="submitUpdate"
				action="#{IAGManagerBean.updateGroup}" value="#{msg.groupsSave}"
				rendered="#{IAGManagerBean.editMode}" />

		</h:form>


	</f:view>
</c:if>
