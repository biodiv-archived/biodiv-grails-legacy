
<%@page import="species.Resource.ResourceType"%>
<%
def iconClass='';
def noOfResources = instance.fetchResourceCount();
%>
<g:if test="${noOfResources}">
    <g:each in="${noOfResources}" var="no">
    <div class="footer-item pull-left">
        <i class="${no[0].iconClass()}" title="No of ${no[0].value()}s"></i>
        <span class="">${no[1]}</span>
    </div>
    </g:each>
</g:if>


