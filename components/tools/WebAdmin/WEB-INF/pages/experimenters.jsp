<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

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
		
		<h:form id="experimenters">
			<h:commandLink action="#{IAEManagerBean.addNewExperimenter}"
				title="#{msg.experimentersAddNew}">
				<h:graphicImage url="/images/add.png" />
				<h:outputText value=" #{msg.experimentersAddNew}" />
			</h:commandLink>

			<br />

			<h2><h:outputText value="#{msg.experimentersList}" /></h2>

			<h:message styleClass="errorText" id="experimentersError"
				for="experimenters" />

			<div id="main">
			<div id="list"><t:dataTable id="data" styleClass="list"
				columnClasses="action,link,desc,desc,roles" var="user"
				value="#{IAEManagerBean.users}" preserveDataModel="false" rows="15">

				<h:column>
					<f:facet name="header">
						<h:outputText value="#{msg.experimentersActions}" />
					</f:facet>
					<h:commandLink action="#{IAEManagerBean.delExperimenter}"
						onclick="if (!confirm('#{msg.experimentersConfirmation}')) return false"
						title="#{msg.experimentersDelete}">
						<h:graphicImage url="/images/del.png"
							alt="#{msg.experimentersDelete}" styleClass="action" />
					</h:commandLink>
					<h:commandLink action="#{IAEManagerBean.editExperimenter}"
						title="#{msg.experimentersEdit}">
						<h:graphicImage url="/images/edit.png"
							alt="#{msg.experimentersEdit}" styleClass="action" />
					</h:commandLink>
				</h:column>

				<h:column>

					<f:facet name="header">

						<h:panelGroup>

							<h:commandLink action="sortItems"
								actionListener="#{IAEManagerBean.sortItems}"
								title="#{msg.sortAsc}">
								<f:attribute name="sortItem" value="lastName" />
								<f:attribute name="sort" value="asc" />
								<h:graphicImage url="/images/asc.png" alt="#{msg.sortAsc}" />
							</h:commandLink>

							<h:outputText value=" #{msg.experimentersName} " />

							<h:commandLink action="sortItems"
								actionListener="#{IAEManagerBean.sortItems}"
								title="#{msg.sortDesc}">
								<f:attribute name="sortItem" value="lastName" />
								<f:attribute name="sort" value="dsc" />
								<h:graphicImage url="/images/dsc.png" alt="#{msg.sortDesc}" />
							</h:commandLink>

						</h:panelGroup>

					</f:facet>

					<h:outputText value="#{user.experimenter.lastName}, " />
					<h:outputText value=" #{user.experimenter.firstName}" />
					<h:outputText value=" #{user.experimenter.middleName}" />

				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup>

							<h:commandLink action="sortItems"
								actionListener="#{IAEManagerBean.sortItems}"
								title="#{msg.sortAsc}">
								<f:attribute name="sortItem" value="omeName" />
								<f:attribute name="sort" value="asc" />
								<h:graphicImage url="/images/asc.png" alt="#{msg.sortAsc}" />
							</h:commandLink>

							<h:outputText value=" #{msg.experimentersOmeName} " />

							<h:commandLink action="sortItems"
								actionListener="#{IAEManagerBean.sortItems}"
								title="#{msg.sortDesc}">
								<f:attribute name="sortItem" value="omeName" />
								<f:attribute name="sort" value="dsc" />
								<h:graphicImage url="/images/dsc.png" alt="#{msg.sortDesc}" />
							</h:commandLink>

						</h:panelGroup>
					</f:facet>

					<h:commandLink action="#{IAEManagerBean.editExperimenter}"
						title="#{msg.experimentersEdit}">
						<h:outputText value="#{user.experimenter.omeName}" />
					</h:commandLink>
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup>

							<h:commandLink action="sortItems"
								actionListener="#{IAEManagerBean.sortItems}"
								title="#{msg.sortAsc}">
								<f:attribute name="sortItem" value="institution" />
								<f:attribute name="sort" value="asc" />
								<h:graphicImage url="/images/asc.png" alt="#{msg.sortAsc}" />
							</h:commandLink>

							<h:outputText value=" #{msg.experimentersInstitution} " />

							<h:commandLink action="sortItems"
								actionListener="#{IAEManagerBean.sortItems}"
								title="#{msg.sortDesc}">
								<f:attribute name="sortItem" value="institution" />
								<f:attribute name="sort" value="dsc" />
								<h:graphicImage url="/images/dsc.png" alt="#{msg.sortDesc}" />
							</h:commandLink>

						</h:panelGroup>
					</f:facet>

					<h:outputText value="#{user.experimenter.institution}"
						converter="SubstringConverter" />
				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup>

							<h:outputText value=" #{msg.experimentersRole} " />

						</h:panelGroup>
					</f:facet>

					<h:graphicImage url="/images/admin.png"
						alt="#{msg.experimentersAdminRole}" styleClass="action"
						rendered="#{user.adminRole}" />
					<h:graphicImage url="/images/active.png"
						alt="#{msg.experimentersUserRole}" styleClass="action"
						rendered="#{user.userRole}"  />
					<h:graphicImage url="/images/notactive.png"
						alt="#{msg.experimentersUserRole}" styleClass="action"
						rendered="#{not user.userRole}"  />
					<h:graphicImage url="/images/ldap.png"
						alt="#{msg.experimentersLdap}" styleClass="action"
						rendered="#{not empty user.dn}"  />

				</h:column>

			</t:dataTable></div>
			<div><h:panelGrid columns="1"
				rendered="#{IAEManagerBean.scrollerMode}" styleClass="scroller">
				<t:dataScroller id="scroll_1" for="data" fastStep="10"
					styleClass="scroller" pageCountVar="pageCount"
					pageIndexVar="pageIndex" paginator="true" paginatorMaxPages="9"
					paginatorTableClass="paginator"
					paginatorActiveColumnStyle="font-weight:bold;" immediate="true"
					actionListener="#{IAEManagerBean.scrollerAction}">
					<f:facet name="first">
						<t:graphicImage url="images/arrow-first.png" border="1" />
					</f:facet>
					<f:facet name="last">
						<t:graphicImage url="images/arrow-last.png" border="1" />
					</f:facet>
					<f:facet name="previous">
						<t:graphicImage url="images/arrow-previous.png" border="1" />
					</f:facet>
					<f:facet name="next">
						<t:graphicImage url="images/arrow-next.png" border="1" />
					</f:facet>
					<f:facet name="fastforward">
						<t:graphicImage url="images/arrow-ff.png" border="1" />
					</f:facet>
					<f:facet name="fastrewind">
						<t:graphicImage url="images/arrow-fr.png" border="1" />
					</f:facet>
				</t:dataScroller>
				<t:dataScroller id="scroll_2" for="data" rowsCountVar="rowsCount"
					displayedRowsCountVar="displayedRowsCountVar" styleClass="scroller"
					firstRowIndexVar="firstRowIndex" lastRowIndexVar="lastRowIndex"
					pageCountVar="pageCount" immediate="true" pageIndexVar="pageIndex">
					<h:outputFormat value="#{msg.experimentersPages}">
						<f:param value="#{rowsCount}" />
						<f:param value="#{displayedRowsCountVar}" />
						<f:param value="#{firstRowIndex}" />
						<f:param value="#{lastRowIndex}" />
						<f:param value="#{pageIndex}" />
						<f:param value="#{pageCount}" />
					</h:outputFormat>
				</t:dataScroller>
			</h:panelGrid></div>
			</div>
		</h:form>
	</f:view>
</c:if>
