<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
<c:if
	test="${not empty sessionScope.LoginBean.ldapServices}">
	<f:view>
		<div id="addform"><h:form id="searchLdap">

			<h2><h:outputText value="#{msg.searchInLdap}" /></h2>

			<p><h:outputText value="#{msg.generalMandatoryFields}" /></p>

			<h:message styleClass="errorText" id="searchLdapFormError"
				for="searchLdapForm" />
			<br />

			<s:subForm id="searchInLdapForm">

				<h:panelGrid columns="4" columnClasses="form, form, input, form">

					<h:outputText value="#{msg.searchName}*" />

					<h:selectOneMenu id="ldapAttributes"
						value="#{ISILManagerBean.searchAttribute}" required="true">
						<f:selectItems value="#{ISILManagerBean.attributes}" />
					</h:selectOneMenu>

					<h:inputText id="ldapValue" value="#{ISILManagerBean.searchField}"
						required="true" maxlength="255" size="40">
						<f:validateLength minimum="1" maximum="255" />
					</h:inputText>

					<h:message styleClass="errorText" id="ldapValueError"
						for="ldapValue" />

				</h:panelGrid>

				<br />

				<t:commandButton id="submitSearch"
					action="#{ISILManagerBean.searchInLdap}"
					value="#{msg.searchButton}" actionFor="searchInLdapForm" />
			</s:subForm>

			<br />
			<h2><h:outputText value="#{msg.searchResult}"
					rendered="#{ISILManagerBean.editMode}" /></h2>

			<c:if test="${sessionScope.ISILManagerBean.size > 0}">

				<h:dataTable id="items" value="#{ISILManagerBean.users}" var="user"
					styleClass="list" columnClasses="desc,desc,desc,action">
					<h:column>
						<f:facet name="header">
							<h:panelGroup>
								<h:outputText value=" #{msg.experimentersOmeName} " />
							</h:panelGroup>
						</f:facet>
						<h:outputText value="#{user.experimenter.omeName}" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:panelGroup>
								<h:outputText value=" #{msg.experimentersName} " />
							</h:panelGroup>
						</f:facet>
						<h:outputText value="#{user.experimenter.lastName}, " />
						<h:outputText value=" #{user.experimenter.firstName}" />
						<h:outputText value=" #{user.experimenter.middleName}" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:panelGroup>
								<h:outputText value=" #{msg.experimentersEmail} " />
							</h:panelGroup>
						</f:facet>
						<h:outputText value="#{user.experimenter.email}" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:panelGroup>
								<h:outputText value="#{msg.experimentersActions}" />
							</h:panelGroup>
						</f:facet>
						<h:selectBooleanCheckbox
							value="#{user.selectBooleanCheckboxValue}"
							rendered="#{user.selectBooleanCheckboxValue}" />
					</h:column>
				</h:dataTable>

				<br />
				<h:commandButton action="#{ISILManagerBean.saveItems}"
					value="#{msg.uploadSave}" />
			</c:if>
			<c:if test="${sessionScope.ISILManagerBean.size <= 0}">
				<p><h:outputText value="#{msg.searchNoResults}"
					rendered="#{ISILManagerBean.editMode}" /></p>
			</c:if>
		</h:form></div>

	</f:view>
</c:if>
<c:if
	test="${empty sessionScope.LoginBean.ldapServices}">
	<p  class="errorText" >Ldap is not configured.</p>
</c:if>
</c:if>
