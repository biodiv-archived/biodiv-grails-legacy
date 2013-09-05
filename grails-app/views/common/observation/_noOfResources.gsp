
<%@page import="species.Resource.ResourceType"%>
<%
def iconClass='';
def noOfResources = instance.fetchResourceCount();
%>

    <g:if test="${bottom}">
    <button id="viewThumbnails" class="footer-item" style="margin-left:0px;">
        <i class="icon-th" title="View thumbnails"></i>
    </button>
    </g:if>

    <g:if test="${noOfResources}">
        <g:each in="${noOfResources}" var="no">
        <div class="footer-item">
            <i class="${no[0].iconClass()}" title="No of ${no[0].value()}s"></i>
            <span class="">${no[1]}</span>
        </div>
        </g:each>
    </g:if>
