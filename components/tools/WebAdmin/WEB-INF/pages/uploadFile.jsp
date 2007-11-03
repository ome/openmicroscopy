<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
	<f:view>
		<h2><h:outputText value="#{msg.uploadFile}" /></h2>

		<p><h:outputText value="#{msg.uploadFileName} " /><h:outputText
			value="#{UploadBean.uploadedNewFile.name}" /></p>

		<div><h:form enctype="multipart/form-data"
			id="uploadedNewFileForm" rendered="#{UploadBean.directory}">
			<h:message styleClass="errorText" id="uploadedNewFileFormError"
				for="uploadedNewFileForm" />
			<br />

			<h:outputText value="#{msg.uploadFile}" />
			<t:inputFileUpload id="uploadedNewFile"
				accept="text/plain,text/xml,application/octet-stream" storage="file"
				value="#{UploadBean.uploadedNewFile}" immediate="true">
				<f:valueChangeListener type="ome.admin.listener.UploadListener" />
			</t:inputFileUpload>
			<h:commandButton id="submitUpdate" action="#{UploadBean.uploadFile}"
				value="#{msg.uploadSave}" />
		</h:form></div>

		<c:if test="${!requestScope.UploadBean.editMode}">
			<h:outputText value="#{msg.uploadDirError}" />
		</c:if>

	</f:view>
</c:if>
