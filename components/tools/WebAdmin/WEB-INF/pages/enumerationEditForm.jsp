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
		
		<div id="addform"><h:form id="enumerationEditForm"
			rendered="#{ITEManagerBean.editMode}">

			<h2><h:outputText value="#{msg.enumsEditEnum}"
				rendered="#{ITEManagerBean.editMode}" /> <h:outputText
				value="#{ITEManagerBean.enumeration.className}" /></h2>

			<p><h:outputText value="#{msg.generalMandatoryFields}" /></p>

			<h:inputHidden id="enumerationClassName"
				value="#{ITEManagerBean.enumeration.className}" />

			<h:message styleClass="errorText" id="enumerationEditFormError"
				for="enumerationEditForm" />

			<div id="entryList"><h:dataTable
				value="#{ITEManagerBean.entrys}" var="entry" styleClass="entryList"
				columnClasses="desc,desc,action">

				<h:column>
					<h:outputText value="*" />
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup>
							<h:outputText value="#{msg.enumsListEnum}" />
						</h:panelGroup>
					</f:facet>

					<h:inputText id="value" value="#{entry.value}" required="true"
						maxlength="255" size="30">
						<f:validateLength minimum="1" maximum="255" />
					</h:inputText>

				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="#{msg.enumsActions}" />
					</f:facet>

					<h:commandLink action="#{ITEManagerBean.delEnumeration}"
						onclick="if (!confirm('#{msg.enumsConfirmation}')) return false"
						title="#{msg.enumsDeleteEnum}">
						<h:graphicImage url="/images/del.png" alt="#{msg.enumsDeleteEnum}"
							styleClass="action" />
					</h:commandLink>

				</h:column>

			</h:dataTable></div>

			<br />
			<h:commandButton id="submitUpdate"
				action="#{ITEManagerBean.updateEnumerations}"
				value="#{msg.enumsSave}" rendered="#{ITEManagerBean.editMode}" />

		</h:form></div>
	</f:view>
</c:if>
