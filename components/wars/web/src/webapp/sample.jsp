<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jstl/xml" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jstl/sql" %>
<jsp:useBean id="now" class="java.util.Date" />
<html>
<body>
Hello world JSP on<fmt:formatDate value="${now}" dateStyle="full" />
</body>
</html>
