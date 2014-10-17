<%@page import="species.Resource.ResourceType"%>
<%
def iconClass='';
def noOfResources = noOfResources?:instance.fetchResourceCount();
%>

<%--
<g:if test="${bottom}">
    <button id="viewThumbnails" class="footer-item" style="margin-left:0px;float:right;">
        <i class="icon-th" title="${g.message(code:'noofresources.view.thumbnails')}"></i>
    </button>
</g:if> --%>

<g:if test="${noOfResources}">
    <g:each in="${noOfResources}" var="no">
        <div class="footer-item" style="float:right;">
            <i class="${no[0].iconClass()}" title="${g.message(code:'noofresources.no',args:[no[0].value()])}"></i>
            <span class="">${no[1]}</span>
        </div>
    </g:each>
</g:if>
