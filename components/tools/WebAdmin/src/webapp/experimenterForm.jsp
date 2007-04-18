<%@ taglib prefix="t" uri="http://jakarta.apache.org/struts/tags-tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:if
	test="${empty sessionScope.LoginBean.mode or sessionScope.LoginBean.mode == false}">
	<t:insert definition=".main" />
</c:if>
<c:if
	test="${sessionScope.LoginBean.mode == true && sessionScope.LoginBean.role == true}">
	<t:insert definition=".experimenterForm" />
</c:if>
