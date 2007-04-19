<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
	<f:view>

		<h:form id="changePassword">

			<h:message styleClass="errorText" id="changePasswordError"
				for="changePassword" />
			<br />

			<h:inputHidden id="experimenterid"
				value="#{IAEManagerBean.experimenter.id}" />

			<h:panelGrid columns="3" columnClasses="form">

				<h:outputText value="#{msg.myaccountPassword}" />

				<h:inputSecret id="password" value="#{IAEManagerBean.password}"
					maxlength="100">

				</h:inputSecret>

				<h:message styleClass="errorText" id="passwordError" for="password" />

				<h:outputText value="#{msg.myaccountPassword2}" />

				<h:inputSecret id="password2" value="#{IAEManagerBean.password2}"
					maxlength="100">

				</h:inputSecret>

				<h:message styleClass="errorText" id="password2Error"
					for="password2" />

			</h:panelGrid>

			<h:commandButton id="submitUpdate"
				action="#{IAEManagerBean.updatePassword}"
				value="#{msg.myaccountSave}" rendered="#{IAEManagerBean.editMode}" />
		</h:form>
	</f:view>
</c:if>
