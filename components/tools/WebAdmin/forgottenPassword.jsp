<%@ taglib prefix="t" uri="http://jakarta.apache.org/struts/tags-tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:if
	test="${empty sessionScope.LoginBean.passwordMode or sessionScope.LoginBean.passwordMode==false 
		or sessionScope.LoginBean.mode}">
	<jsp:forward page="/accessdenied.jsf" />
</c:if>
<c:if
	test="${(empty sessionScope.LoginBean.mode or sessionScope.LoginBean.mode == false) 
	    && !sessionScope.LoginBean.role && sessionScope.LoginBean.passwordMode}">
	<t:insert definition=".forgottenPassword" />
</c:if>
