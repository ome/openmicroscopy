<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
	<f:view>

		<h2><h:outputText value="#{msg.uploadImport}" /></h2>
		<p><h:outputText value="#{TreeBean.fileName}" /></p>

		<h:form>

			<h:message styleClass="errorText" id="experimentersError"
				for="experimenters" />

			<div id="main"><h:dataTable id="items"
				value="#{TreeBean.experimenters}" var="experimenter"
				styleClass="list" columnClasses="desc,desc,desc,desc,action">
				<h:column>
					<f:facet name="header">
						<h:panelGroup>
							<h:outputText value=" #{msg.experimentersOmeName} " />
						</h:panelGroup>
					</f:facet>
					<h:outputText value="#{experimenter.omeName}" />
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:panelGroup>
							<h:outputText value=" #{msg.experimentersName} " />
						</h:panelGroup>
					</f:facet>
					<h:outputText value="#{experimenter.lastName}, " />
					<h:outputText value=" #{experimenter.firstName}" />
					<h:outputText value=" #{experimenter.middleName}" />
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:panelGroup>
							<h:outputText value=" #{msg.experimentersEmail} " />
						</h:panelGroup>
					</f:facet>
					<h:outputText value="#{experimenter.email}" />
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:panelGroup>
							<h:outputText value=" #{msg.experimentersInstitution} " />
						</h:panelGroup>
					</f:facet>
					<h:outputText value="#{experimenter.institution}"
						converter="SubstringConverter" />
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:panelGroup>
							<h:outputText value="#{msg.experimentersActions}" />
						</h:panelGroup>
					</f:facet>
					<h:selectBooleanCheckbox
						value="#{experimenter.selectBooleanCheckboxValue}"
						rendered="#{experimenter.selectBooleanCheckboxValue}" />
				</h:column>
			</h:dataTable> <br />
			<h:commandButton action="#{TreeBean.saveItems}"
				value="#{msg.uploadSave}" /></div>

		</h:form>

	</f:view>
</c:if>
