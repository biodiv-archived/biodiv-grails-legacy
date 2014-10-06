<%@ page import="content.eml.Document"%>
<div class="observation_story">
    <%
    // To overcome hibernate fileassist issue - http://www.intelligrape.com/blog/2012/09/21/extract-correct-class-from-hibernate-object-wrapped-with-javassist/
    documentInstance = Document.read(documentInstance.id)
    %>
    <div>
        <g:if test="${showFeatured}">

        <div class="featured_body">
        <div class="featured_title ellipsis"> 
            <div class="heading">
                <g:link url="${uGroup.createLink(controller:'document', action:'show', id:documentInstance.id, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}"
                <span class="ellipsis">${documentInstance.title}</span>
                </g:link>
            </div>
        </div>
        <g:render template="/common/featureNotesTemplate" model="['instance':documentInstance, 'featuredNotes':featuredNotes]"/>
    </div>
        </g:if>
        <g:else>

        <g:if test="${documentInstance.uFile}">

        <% def extension = documentInstance?.uFile?.path.split("\\.")[-1] %>
            <g:if test="${extension.toUpperCase() == 'PDF'}">
                <% url = grailsApplication.config.speciesPortal.content.serverURL
                   //url = "/content"                                       
                   url = url+documentInstance?.uFile?.path  %>              
                <iframe id="viewer" src = "${grailsApplication.config.grails.serverURL}/ViewerJS/index.html#${url}" width='612' height='400' allowfullscreen webkitallowfullscreen></iframe>                
            </g:if>


        <div class="prop">
            <span class="name"><g:message code="default.file.label" /></span>
            <div class="value">

                <fileManager:displayFile
                filePath="${ documentInstance?.uFile?.path}"
                fileName="${ documentInstance?.title}"></fileManager:displayFile>
            </div>
        </div>
        </g:if>

        <g:if test="${documentInstance.uri}">
        <div class="prop">
            <span class="name"><g:message code="default.url.label" /></span>
            <div class="value">
                <span class="linktext" style="word-wrap: break-word;">
                    ${documentInstance.uri}
                </span>
            </div>
        </div>
        </g:if>

        <div class="prop">
            <span class="name"><g:message code="default.type.label" /></span>
            <div class="value">
                ${documentInstance.type?.value }
            </div>
        </div>

        <%  def styleVar = 'block';
            def clickcontentVar = '' 
        %> 
        <g:if test="${documentInstance?.language?.id != userLanguage?.id}">
            <%  
              styleVar = "none"
              clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+documentInstance?.language?.twoLetterCode.toUpperCase()+'</a>';
            %>
        </g:if>
                

        <g:if
        test="${documentInstance?.notes && documentInstance?.notes.trim() != ''}">
        <div class="prop">
            <span class="name"><g:message code="default.description.label" /></span>
            <div class="notes_view linktext value">
                ${raw(clickcontentVar)}
                <div style="display:${styleVar}">
                    ${raw(documentInstance?.notes)}
                </div>    
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
            <div class="value">
                ${raw(clickcontentVar)}
                <div style="display:${styleVar}">
                    ${documentInstance?.attribution}
                </div>    
            </div>
        </div>
        </g:if>
        <g:if test="${documentInstance?.license}">
        <div class="prop">
            <span class="name"><g:message code="default.licenses.label" /></span>

            <div class="value">
                <img
                src="${resource(dir:'images/license',file:documentInstance?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
                title="${documentInstance.license.name}" />
            </div>
        </div>
        </g:if>

        <g:if test="${showDetails && documentInstance?.fetchSource()}">
        <div class="prop">
            <span class="name"><g:message code="default.source.label" /></span>
            <%	
            def sourceObj = documentInstance.fetchSource()
            def className = sourceObj.class.getSimpleName()
            %>
            <div class="value">
                <a
                    href="${uGroup.createLink(controller: className.toLowerCase(), action:"show", id:sourceObj.id, 'userGroupWebaddress':params?.webaddress)}"><b>
                        ${className + ": "}
                    </b>
                    ${sourceObj}</a>
            </div>
        </div>
        </g:if>

        <g:if test="${showDetails && documentInstance?.tags}">
        <div class="prop">
            <span class="name"><g:message code="default.tags.label" /></span>

            <div class="value">
                <g:render template="/project/showTagsList"
                model="['instance': documentInstance, 'controller': 'document', 'action':'browser']" />
            </div>
        </div>
        </g:if>
        </g:else>
    </div>
</div>

