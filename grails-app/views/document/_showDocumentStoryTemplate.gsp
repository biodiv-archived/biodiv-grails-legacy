<%@ page import="content.eml.Document"%>
<div class="observation_story">
<%
// To overcome hibernate fileassist issue - http://www.intelligrape.com/blog/2012/09/21/extract-correct-class-from-hibernate-object-wrapped-with-javassist/
documentInstance = Document.read(documentInstance.id)
%>
        <g:if test="${showFeatured}">
        <div class="featured_body">
        <div class="featured_title ellipsis"> 
        <div class="heading">
        <g:link url="${uGroup.createLink(controller:'document', action:'show', id:documentInstance.id, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}">
        ${showFeatured}
        <span class="ellipsis">${documentInstance.title}</span>
        </g:link>
        </div>
        </div>
        </div>
        <g:render template="/common/featureNotesTemplate" model="['instance':documentInstance, 'featuredNotes':featuredNotes]"/>
        </g:if>
<g:else>
            <g:if test="${documentInstance.uFile}">
                <% def extension = documentInstance?.uFile?.path.split("\\.")[-1] %>
                    <g:if test="${extension.toUpperCase() == 'PDF' && (showPDFViewer != null)?showPDFViewer:true}">
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
                        fileUrl=""
                        fileName="${ documentInstance?.title}"></fileManager:displayFile>
                        </div>
                        </div>
            </g:if>
            <g:else>
                <% def extension = "pdf" %>
                <g:if test="${extension.toUpperCase() == 'PDF' && (showPDFViewer != null)?showPDFViewer:true}">
                    <% url = documentInstance?.externalUrl %>              
                    <iframe id="viewer" src = "${url}" width='612' height='400' allowfullscreen webkitallowfullscreen></iframe>                
                </g:if>
            </g:else>

            <div class="sidebar_section">
            <h5><g:message code="link.coverage.information" /></h5>
            <div id="coverageInfo" class="speciesField collapse in">
            <table>
                <g:if test="${documentInstance?.speciesGroups}">
            <tr>
                <td class="prop"><span class="grid_3 name"><g:message code="default.species.groups.label" /></span></td>
                <td class="linktext"><g:each
                in="${documentInstance.speciesGroups}"
                var="speciesGroup">
                <button
                class="btn species_groups_sprites ${speciesGroup.iconClass()} active"
                id="${"group_" + speciesGroup.id}"
                value="${speciesGroup.id}" title="${speciesGroup.name}"></button>
                </g:each></td>
            </tr>
                </g:if>
            <g:if test="${documentInstance?.habitats}">
            <tr>
                <td class="prop"><span class="name"><g:message code="default.habitats.label" /></span></td>
                <td class="linktext"><g:each
                in="${documentInstance.habitats}" var="habitat">
                <button
                class="btn habitats_sprites ${habitat.iconClass()} active"
                id="${"habitat_" + habitat.id}" value="${habitat.id}"
                title="${habitat.name}"
                data-content="${message(code: 'habitat.definition.' + habitat.name)}"
                rel="tooltip" data-original-title="A Title"></button>
                </g:each></td>
            </tr>
            </g:if>
            <g:if test="${documentInstance?.placeName || documentInstance?.reverseGeocodedName}">
            <tr>
                <td class="prop"><span class="name">
                <g:message code="default.place.label" /> </span></td>
            <td>
                <g:if test="${documentInstance?.placeName}">
                <g:set var="location" value="${documentInstance.placeName}"/>
                </g:if>
                    <g:else>
                    <g:set var="location" value="${documentInstance.reverseGeocodedName}"/>
                    </g:else>
                    <div class="value ellipsis multiline" title="${location}">
                    ${location}
                    </div>
                    </td>
                    </tr>
                    </g:if>       
            </table>
            </div>
            </div>
                    <div class="sidebar_section">
                    <h5><g:message code="Details" /></h5>
                    <div id="coverageInfo" class="speciesField collapse in">
                    <div class="prop">
                    <span class="name"><g:message code="default.file.label" /></span>
                    <div class="value">  <a href="${url}"><span class="pdficon" style="display:inline-block; margin-left: 5px; margin-right:5px;"></span>${raw(documentInstance?.title)}</a></div>
                    </div>

                    <g:if test="${documentInstance.externalUrl}">
                    <div class="prop">
                    <span class="name"><g:message code="default.url.label" /></span>
                    <div class="value">
                    <span class="linktext" style="word-wrap: break-word;">
                    ${documentInstance.externalUrl}
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
            clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+documentInstance?.language?.threeLetterCode.toUpperCase()+'</a>';
            %>
            </g:if>
                <g:if
                test="${documentInstance?.notes && documentInstance?.notes.trim() != ''}">
                <div class="prop">
                <span class="name align-center"><g:message code="default.description.label" /></span>
                <div class="notes_view linktext value">
                ${raw(clickcontentVar)}
                <div style="display:${styleVar}">
                ${raw(documentInstance?.notes)}
                </div>    
                </div>
                </div>
                </g:if>
            </div>
            </div>

            <div class="sidebar_section">
                <h5><g:message code="Metadata" /></h5>
                <div id="coverageInfo" class="speciesField collapse in">
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
                <asset:image src="/all/license/${documentInstance?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png'}" absolute="true" title="${documentInstance.license.name}" />
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

            <g:if test="${showDetails}">
                <div class="prop" style="margin-bottom: 5px;">
                <div style="height: 26px;">
                <span class="name"><g:message code="default.tags.label" /></span>
                <div class="btn btn-small pull-right btn-primary add_obv_tags" style="  margin-right: 16px;">Add Tag</div>
                </div>            
                <div class="value">
                <g:render template="/project/showTagsList"
                model="['instance': documentInstance, 'controller': 'document', 'action':'browser']" />
                </div>
                <div class="signature clearfix thumbnail pull-right">
                <sUser:showUserTemplate
                model="['userInstance':documentInstance.author, 'userGroup':userGroup]" />
                </div>           
                </div>
            </g:if>
            </g:else>
            </div>
            </div>
            </div>

