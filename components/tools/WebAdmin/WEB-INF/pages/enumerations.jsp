<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
	<f:view>
		<h:form id="enumerations">

			
				<h:graphicImage url="/images/add.png" />
				<h:outputText value=" #{msg.enumsAddNewEnum}" />
			

			<br />

			<h2><h:outputText value="#{msg.enumsListEnum}" /></h2>

			<h:message styleClass="errorText" id="enumerationsError" for="enumerations" />

			<div id="main">
			<div id="list"><t:dataTable id="data" styleClass="list"
				columnClasses="action,link" var="enumeration"
				value="#{ITEManagerBean.enumerations}" preserveDataModel="false"
				rows="15">

				<h:column>
					<f:facet name="header">
						<h:outputText value=" #{msg.enumsActions} " />
					</f:facet>

					
						<h:graphicImage url="/images/edit.png"
							alt="#{msg.enumsEditEnum}" styleClass="action" />
					

				</h:column>

				<h:column>
					<f:facet name="header">
						<h:panelGroup>

							<h:commandLink action="sortItems"
								actionListener="#{ITEManagerBean.sortItems}"
								title="#{msg.sortAsc}">
								<f:attribute name="sortItem" value="className" />
								<f:attribute name="sort" value="asc" />
								<h:graphicImage url="/images/asc.png" alt="#{msg.sortAsc}" />
							</h:commandLink>

							<h:outputText value=" #{msg.enumsName} " />

							<h:commandLink action="sortItems"
								actionListener="#{ITEManagerBean.sortItems}"
								title="#{msg.sortDesc}">
								<f:attribute name="sortItem" value="className" />
								<f:attribute name="sort" value="dsc" />
								<h:graphicImage url="/images/dsc.png" alt="#{msg.sortDesc}" />
							</h:commandLink>

						</h:panelGroup>
					</f:facet>

					
						<h:outputText value="#{enumeration.className}" />
					
				</h:column>

			</t:dataTable></div>
			<div><h:panelGrid columns="1"
				rendered="#{ITEManagerBean.scrollerMode}" styleClass="scroller">
				<t:dataScroller id="scroll_1" for="data" fastStep="10"
					styleClass="scroller" pageCountVar="pageCount"
					pageIndexVar="pageIndex" paginator="true" paginatorMaxPages="9"
					paginatorTableClass="paginator"
					paginatorActiveColumnStyle="font-weight:bold;" immediate="true"
					actionListener="#{ITEManagerBean.scrollerAction}">
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
					<h:outputFormat value="#{msg.groupsPages}">
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
