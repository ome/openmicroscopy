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
		<div id="addform"><h:form id="enumerationAddForm" rendered="#{not ITEManagerBean.editMode}">

			<h2><h:outputText value="#{msg.enumsAddNewEnum}"
				rendered="#{not ITEManagerBean.editMode}" /></h2>

			<p><h:outputText value="#{msg.generalMandatoryFields}" /></p>

			<h:message styleClass="errorText" id="enumerationAddFormError"
				for="enumerationAddForm" />
			<br />

			<h:panelGrid columns="3" columnClasses="form, input">

				<h:outputText value="#{msg.enumsType}*" />

				<h:selectOneMenu id="className" styleClass="enum" 
					value="#{ITEManagerBean.enumeration.className}" required="true">
					<f:selectItems value="#{ITEManagerBean.enumerationsType}" />
				</h:selectOneMenu>

				<h:message styleClass="errorText" id="classNameError"
					for="className" />


				<h:outputText value="#{msg.enumsEnumName}*" />

				<h:inputText id="event" 
					value="#{ITEManagerBean.enumeration.event}" 
					required="true"
					maxlength="255" size="40">
					<f:validateLength minimum="1" maximum="255" />
				</h:inputText>

				<h:message styleClass="errorText" id="eventError"
					for="event" />

			</h:panelGrid>

			<br />

			<h:commandButton id="submitAdd"
				action="#{ITEManagerBean.addEnumeration}" value="#{msg.enumsSave}"
				rendered="#{not ITEManagerBean.editMode}" />

			<h:commandButton id="submitUpdate"
				action="#{ITEManagerBean.updateEnumeration}"
				value="#{msg.enumsSave}" rendered="#{ITEManagerBean.editMode}" />

		</h:form>
		</div>
	</f:view>
</c:if>
