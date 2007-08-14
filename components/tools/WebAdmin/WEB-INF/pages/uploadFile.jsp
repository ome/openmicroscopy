<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg" />

<c:if
	test="${sessionScope.LoginBean.mode && sessionScope.LoginBean.role}">
	<f:view>
		<h2><h:outputText value="#{msg.uploadFile}" /></h2>

		FileName: <h:outputText value="#{UploadBean.uploadedNewFile.name}" />

		<h:form enctype="multipart/form-data" id="uploadFileForm"
			name="uploadFileForm">
			<h:message styleClass="errorText" id="uploadFileFormError"
				for="uploadFileForm" />

			<h:outputText value="#{msg.uploadFile}" />
			<t:inputFileUpload id="myUploadedFile" storage="file"
				accept="text/plain,text/xml,application/octet-stream"
				styleClass="myStyle" value="#{UploadBean.uploadedNewFile}"
				immediate="true">
				<f:valueChangeListener type="ome.admin.listener.UploadListener" />
			</t:inputFileUpload>
			<h:commandButton id="submitUpdate" action="#{UploadBean.uploadFile}"
				value="#{msg.uploadSave}" />
		</h:form>

	</f:view>
</c:if>
