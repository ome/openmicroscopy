<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<f:loadBundle basename="ome.admin.bundle.messages" var="msg"/>

    <div class="top_row"><div class="logo">
        <img src="<c:url value='/images/logo.jpg' />" alt="Omero Admin" /></div>
    </div>
    
    <c:if test="${sessionScope.LoginBean.mode == true}">
        <h1>${msg.headerHello} <c:out value="${sessionScope.LoginBean.username}" />!</h1> 
    </c:if>

       <div  class="nav">
            <div id="navigation">
                <ul>
                    <c:if test="${sessionScope.LoginBean.role == true && sessionScope.LoginBean.mode == true}">
                    <li><a href="./experimenters.faces">${msg.headerExperimenters}</a> </li>
                    <li><a href="./groups.faces">${msg.headerGroups}</a></li>
                    </c:if>
                    <c:if test="${sessionScope.LoginBean.mode == true}">
                    <li><a href="./myAccount.faces">${msg.headerMyAccount}</a></li>
                    </c:if>
                </ul>
                
            </div>
        </div>
   