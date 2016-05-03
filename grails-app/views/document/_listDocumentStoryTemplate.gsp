<%@ page import="content.eml.Document"%>
        <%
        // To overcome hibernate fileassist issue - http://www.intelligrape.com/blog/2012/09/21/extract-correct-class-from-hibernate-object-wrapped-with-javassist/
        documentInstance = Document.read(documentInstance.id)
        %>
    <div class="snippet tablet snippettablet">
        <a href="${uGroup.createLink([controller:'document', action:'show', id:documentInstance.id])}">
        <img src="${assetPath(src:'/all/'+'doc.png',absolute:'true' )}" alt="PDF"></a>
    </div>
     <div class="showObvDetails">
            <div class="prop">
                <span class="name"><i class=" icon-list-alt"></i><g:message code="default.title.label" /></span>
                <div class="value ellipsis">
                    ${raw(documentInstance.title)}
                </div>
            </div>
            <div class="prop">
                <span class="name"><i class=" icon-file"></i><g:message code="default.type.label" /></span>
                <div class="value">
                 ${documentInstance.type?.value }
                </div>
            </div>
            <g:if
            test="${documentInstance?.notes && documentInstance?.notes.trim() != ''}">
                <div class="prop">
                <span class="name"><i class=" icon-comment"></i><g:message code="default.description.label" /></span>

                <div style="display:${styleVar}" class=" value twoellipse">
                    ${raw(documentInstance?.notes)}
                </div>
                </div>
            </g:if>    

            <g:if test="${documentInstance?.attribution}">
                <div class="prop">
                <span class="name"><i class="icon-th-large"></i><g:message code="default.attribution.label" /></span>
                <div class="value ellipsis">
                ${raw(clickcontentVar)}
                <div style="display:${styleVar}" class="ellipsis">
                    ${documentInstance?.attribution}
                </div>    
                </div>
                </div>
            </g:if>

            <div style=" bottom: 3px;position: absolute;width: 100%;">
            <div style="float:right;">
            <sUser:showUserTemplate
            model="['userInstance':documentInstance.author, 'userGroup':userGroup]" />
            </div>
            <div style="float:left;margin-left:30px;">
                <g:if test="${documentInstance?.speciesGroups}">
                <g:each
                in="${documentInstance.speciesGroups}"
                var="speciesGroup">
                <button
                class="btn species_groups_sprites ${speciesGroup.iconClass()} active"
                id="${"group_" + speciesGroup.id}"
                value="${speciesGroup.id}" title="${speciesGroup.name}"></button>
                </g:each>
                </g:if>
                <g:else>
                <button
                class="btn habitats_sprites all_gall_th active"
                id="${"group_" + 267835}"
                value="267835}" title="${speciesGroup?.name}"></button>
                </g:else>
                <g:if test="${documentInstance?.habitats}">
                <g:each
                in="${documentInstance.habitats}" var="habitat">
                <button
                class="btn habitats_sprites ${habitat.iconClass()} active"
                id="${"habitat_" + habitat.id}" value="${habitat.id}"
                title="${habitat.name}"
                data-content="${message(code: 'habitat.definition.' + habitat.name)}"
                rel="tooltip" data-original-title="A Title"></button>
                </g:each>
                </g:if>
                <g:else>
                <button
                class="btn habitats_sprites all_gall_th active"
                id="${"group_" + 267835}"
                value="267835}" title="${speciesGroup?.name}"></button>
                </g:else>
            </div>

        </div>
</div>
