<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
	<f:view>
		<h:form id="groups">

			<h:commandLink action="#{IAGManagerBean.addNewGroup}">
				<h:graphicImage url="/images/add.png" />
				<h:outputText value=" #{msg.groupsAddNewGroup}" />
			</h:commandLink>

			<br />

			<h2><h:outputText value="#{msg.groupsListGroup}" /></h2>

			<h:message styleClass="errorText" id="groupsError"
				for="groups" />

			<div id="main"><h:dataTable id="items"
				value="#{IAGManagerBean.groups}" var="group" styleClass="list"
				columnClasses="action,link,desc">

				<h:column>
					<f:facet name="header">
						<h:outputText value=" #{msg.groupsActions} " />
					</f:facet>
					
					<h:commandLink action="#{IAGManagerBean.editInGroup}">
						<h:graphicImage url="/images/editing.png"
							alt="#{msg.groupsEditIn}" />
					</h:commandLink>
					
					<h:commandLink action="#{IAGManagerBean.editGroup}">
						<h:graphicImage url="/images/edit.png"
							alt="#{msg.groupsEditGroup}" />
					</h:commandLink>

				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup>

							<h:commandLink action="sortItems"
								actionListener="#{IAGManagerBean.sortItems}">
								<f:attribute name="sortItem" value="name" />
								<f:attribute name="sort" value="asc" />
								<h:graphicImage url="/images/asc.png" alt="asc" />
							</h:commandLink>

							<h:outputText value=" #{msg.groupsName} " />

							<h:commandLink action="sortItems"
								actionListener="#{IAGManagerBean.sortItems}">
								<f:attribute name="sortItem" value="name" />
								<f:attribute name="sort" value="dsc" />
								<h:graphicImage url="/images/dsc.png" alt="dsc" />
							</h:commandLink>

						</h:panelGroup>
					</f:facet>

					<h:commandLink action="#{IAGManagerBean.editGroup}">
						<h:outputText value="#{group.name}" />
					</h:commandLink>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:outputText value="#{msg.groupsDescription}" />
					</f:facet>
					<h:outputText value="#{group.description}"
						converter="SubstringConverter" />
				</h:column>

			</h:dataTable></div>
		</h:form>
	</f:view>
</c:if>
