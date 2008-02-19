<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://sourceforge.net/projects/jsf-comp" prefix="ch"%>

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
		
		<div id="chart"><h:form id="form1">
			<ch:chart id="chart1" antialias="true"
				datasource="#{IRIPieChartBean.pieDataSet}" legend="false"
				width="550" height="413" background="#ffffff" colors="#3467a7"
				title="#{msg.spaceTitle}" type="pie" is3d="false"
				imgTitle="#{msg.spaceTitle}">
			</ch:chart>
		</h:form></div>
		
		<div id="imageTitle">
			<h:outputText value="#{msg.spaceFree} "/>
			<h:outputText value="#{IRIPieChartBean.freeSpace}"/> [KB]
			<br/>
			<h:outputText value="#{msg.spaceUsed} "/>
			<h:outputText value="#{IRIPieChartBean.usedSpace}"/> [KB]
		</div>
	</f:view>
</c:if>
