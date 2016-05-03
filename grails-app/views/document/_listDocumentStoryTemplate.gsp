<%@page import="species.utils.Utils"%>
<%@ page import="content.eml.Document"%>
        <%
        // To overcome hibernate fileassist issue - http://www.intelligrape.com/blog/2012/09/21/extract-correct-class-from-hibernate-object-wrapped-with-javassist/
        documentInstance = Document.read(documentInstance.id)
        %>
    <div class="snippet tablet snippettablet">
        <a href="${uGroup.createLink([controller:'document', action:'show', id:documentInstance.id])}">
        <img src="${assetPath(src:'/all/'+'doc.png',absolute:'true' )}" alt="PDF"></a>
    </div>
<div class="observation_story">
            <div class="prop">
                <span class="name"><g:message code="default.title.label" /></span>
                <div class="value ellipsis">
                    ${raw(documentInstance.title)}
                </div>
            </div>
            <div class="prop">
                <span class="name"><g:message code="default.type.label" /></span>
                <div class="value">
                 ${documentInstance.type?.value }
                </div>
            </div>
            <g:if
            test="${documentInstance?.notes && documentInstance?.notes.trim() != ''}">
                <div class="prop">
                <span class="name"><g:message code="default.description.label" /></span>

                <div style="display:${styleVar}" class=" value ellipsis">
                    ${Utils.stripHTML(documentInstance?.notes.replaceAll('&nbsp;', ''))}  
                </div>
                </div>
            </g:if>    
            <g:if test="${documentInstance?.contributors}">
                <div class="prop">
                <span class="name"><g:message code="default.contributors.label" /></span>
                <div class="value">
                ${documentInstance?.contributors}
                </div>
                </div>
            </g:if>
            <g:if test="${documentInstance?.attribution}">
                <div class="prop">
                <span class="name"><g:message code="default.attribution.label" /></span>
                <div class="value ellipsis">
                ${raw(clickcontentVar)}
                <div style="display:${styleVar}" class="ellipsis">
                    ${documentInstance?.attribution}
                </div>    
                </div>
                </div>
            </g:if>
            <div style="float:right">
            <sUser:showUserTemplate
            model="['userInstance':documentInstance.author, 'userGroup':userGroup]" />
            </div>
</div>
