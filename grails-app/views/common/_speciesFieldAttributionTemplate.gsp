<!-- field toolbar -->
<g:if test="${speciesFieldInstance}">
<!--  content attribution -->
<div class="attributionContent" style="display:none;overflow:hidden;">
    <!-- attributions -->
    <g:if test="${speciesFieldInstance.attributors?.size() > 0}">
    <div class="prop span11">
        <div class="name" style="float:none;"><g:message code="default.attributions.label" /></div>
            <ul>
                <g:each in="${speciesFieldInstance.attributors}" var="r">
                <g:if test="${r}">
                <li>
                <span class="${isSpeciesFieldContributor?'editField':''}" data-type="textarea" data-rows="2" data-name="attribution" data-original-title="Edit attribution" >${r.name}
                </span>
                </li>
                </g:if>
                </g:each>
                <g:if test="${isSpeciesFieldContributor}">
                <!--li class="hidePoint">
                    <a href="#" class="addField"  data-pk="${speciesFieldInstance.id}" data-type="textarea" data-rows="2"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="attribution" data-original-title="Add attribution" data-placeholder="Add attribution"></a>
                </li-->
                </g:if>
            </ul>
    </div>
    </g:if>
    <g:elseif test="${isSpeciesFieldContributor}">
    <div class="prop span11">
        <div class="name" style="float:none;"><g:message code="default.attributions.label" /></div>
            <ul>
                <li class="hidePoint">
                <span class="addField"  data-pk="${speciesFieldInstance.id}" data-type="textarea" data-rows="2"  data-name="attribution" data-original-title="Add attribution" data-placeholder="Add attribution"></span>
                </li>
            </ul>
    </div>
    </g:elseif>

    <g:if test="${speciesFieldInstance?.contributors}">
    <div class="prop span11">
        <div class="name" style="float:none;"><g:message code="default.contributors.label" /></div>
            <ul><g:each
                in="${ speciesFieldInstance?.contributors}" var="contributor">
                <g:if test="${contributor}">
                <li>
                <a href="${uGroup.createLink(controller:'SUser', action:'show', id:contributor.id)}" class="${isSpeciesFieldContributor?'editField':''}" data-type="autofillUsers" data-name="contributor"  data-pk="${contributor.id}" data-fieldId="${speciesFieldInstance.id}" data-value="${contributor.name}" data-original-title="Edit contributor name">${contributor.name}</a>
                </li> 
                </g:if>
                </g:each>
                <g:if test="${isSpeciesFieldContributor}">
                <!--li class="hidePoint"> 
                <a href="#" class="addField"  data-fieldId="${speciesFieldInstance.id}" data-type="autofillUsers"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="contributor" data-original-title="Add contributor name" data-placeholder="Add contributor"></a>
                </li-->
                </g:if>
            </ul>

    </div>
    </g:if>

    <g:if test="${speciesFieldInstance?.status}">
    <div class="prop span11">
        <div class="name span2" style="margin-left:0px;"><g:message code="default.status.label" /></div>
        <div class="value pull-left">
            
            <!--a href="#" class="status ${isSpeciesFieldContributor?'selector':''}" data-type="select" data-pk="${speciesFieldInstance.id}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="status"  data-value="${speciesFieldInstance?.status?.value()}" data-original-title="Edit status">
                ${speciesFieldInstance?.status?.value()}</a-->
                ${speciesFieldInstance?.status?.value()}

        </div>
    </div>
    </g:if>
    <g:if test="${speciesFieldInstance?.audienceTypes}">
    <div class="prop span11">
        <div class="span2 name" style="margin-left:0px;"><g:message code="default.audiences.label" /></div>
        <div class="value pull-left"><g:each
            in="${ speciesFieldInstance?.audienceTypes}"
            var="audienceType">

            <span class="audienceType ${isSpeciesFieldContributor?'selector':''}" data-type="select"  data-name="audienceType" data-value="${audienceType.value}" data-original-title="Edit Audience Type"> ${audienceType.value}</span>
            </g:each>
        </div>
    </div>
    </g:if>
    <g:if test="${speciesFieldInstance?.licenses?.size() > 0}">
    <div class="prop span11">
        <div class="span2 name" style="margin-left:0px;"><g:message code="default.licenses.label" /></div>
        <div class="value pull-left"><g:each status="i"
            in="${speciesFieldInstance?.licenses}" var="license">
            <span class="license ${isSpeciesFieldContributor?'selector':''}" data-type="select" data-name="license" data-value="${license.name}" data-original-title="Edit license">${license.name}</span>
            </g:each>
        </div>
    </div>
    </g:if>


    <!-- references -->
    <g:if test="${speciesFieldInstance.references?.size() > 0}">
    <div class="prop span11">
        <div class="name" style="float:none;"><g:message code="default.references.label=References" /></div>
        <div>
            <ul>
                <g:each in="${speciesFieldInstance.references}" var="r">
                <li title="${r.title?:r.url}">
                
                <span class="${isSpeciesFieldContributor?'editField':''}" data-type="textarea" data-rows="2" data-name="reference" data-original-title="Edit reference">
                <g:if
                test="${r.url}">
                 ${r.title?r.title:r.url}
                </g:if> <g:else>
                ${r.title }
                </g:else>
                </span>
                
                </li>
                </g:each>
                <g:if test="${isSpeciesFieldContributor}">
                <!--li>
                    <a href="#" class="addField"  data-pk="${speciesFieldInstance.id}" data-type="textarea" data-rows="2"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="reference" data-original-title="Add reference" data-placeholder="Add reference"></a>
                </li-->
                </g:if>

            </ul>
        </div>
    </div>
    </g:if>
    <g:elseif test="${isSpeciesFieldContributor}">
    <div class="prop span11">
        <div class="name" style="float:none;"><g:message code="default.references.label" /></div>
            <ul>
                <li class="hidePoint">
                <span class="addField"  data-pk="${speciesFieldInstance.id}" data-type="textarea" data-rows="2" data-name="reference" data-original-title="Add reference" data-placeholder="Add reference"></span>
                </li>
            </ul>
    </div>
    </g:elseif>


</div>
</g:if>
