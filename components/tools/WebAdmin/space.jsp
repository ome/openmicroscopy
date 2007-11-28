<%@ taglib prefix="t" uri="http://jakarta.apache.org/struts/tags-tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:if
	test="${empty sessionScope.LoginBean.mode or !sessionScope.LoginBean.mode}">
	<t:insert definition=".login" />
</c:if>
<c:if
	test="${sessionScope.LoginBean.mode && !sessionScope.LoginBean.role}">
	<jsp:forward page="/accessdenied.jsf" />
</c:if>
<c:if
	test="${sessionScope.LoginBean.mode and sessionScope.LoginBean.mode}">
	<t:insert definition=".space" />
</c:if>
