<!-- field toolbar -->
<g:if test="${speciesFieldInstance}">
<g:if test="${speciesFieldInstance?.description}">
<!--  content attribution -->
<div class="attributionContent" style="display:none;overflow:hidden;">
    <!-- attributions -->
    <g:if test="${speciesFieldInstance.attributors?.size() > 0}">
    <div class="prop span11">
        <div class="name" style="float:none;">Attributions</div>
            <ul>
                <g:each in="${speciesFieldInstance.attributors}" var="r">
                <g:if test="${r}">
                <li>
                <a href="#" class="${isSpeciesContributor?'editField':''}" data-type="textarea" data-rows="2"  data-pk="${speciesFieldInstance.id}" data-params="{cid:${r.id}}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="attributor" data-original-title="Edit attributor name" title="Click to edit">${r.name}
                </a>
                </li>
                </g:if>
                </g:each>
                <g:if test="${isSpeciesContributor}">
                <li class="hidePoint">
                    <a href="#" class="addField"  data-pk="${speciesFieldInstance.id}" data-type="textarea" data-rows="2"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="attributor" data-original-title="Add attributor name" data-placeholder="Add attributor"></a>
                </li>
                </g:if>
            </ul>
    </div>
    </g:if>
    <g:elseif test="${isSpeciesContributor}">
    <div class="prop span11">
        <div class="name" style="float:none;">Attributions</div>
            <ul>
                <li class="hidePoint">
                <a href="#" class="addField"  data-pk="${speciesFieldInstance.id}" data-type="textarea" data-rows="2"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="attributor" data-original-title="Add attributor name" data-placeholder="Add attributor"></a>
                </li>
            </ul>
    </div>
    </g:elseif>

    <g:if test="${speciesFieldInstance?.contributors}">
    <div class="prop span11">
        <div class="name" style="float:none;">Contributors</div>
            <ul><g:each
                in="${ speciesFieldInstance?.contributors}" var="contributor">
                <g:if test="${contributor}">
                <li>
                <a href="#" class="${isSpeciesContributor?'editField':''}" data-type="textarea" data-rows="2"  data-pk="${speciesFieldInstance.id}" data-params="{cid:${contributor.id}}"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="contributor" data-original-title="Edit contributor name" title="Click to edit">${contributor.name}</a>
                </li> 
                </g:if>
                </g:each>
                <g:if test="${isSpeciesContributor}">
                <li class="hidePoint"> 
                <a href="#" class="addField"  data-pk="${speciesFieldInstance.id}" data-type="textarea" data-rows="2"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="contributor" data-original-title="Add contributor name" data-placeholder="Add contributor"></a>
                </li>
                </g:if>
            </ul>
    </div>
    </g:if>

    <g:if test="${speciesFieldInstance?.status}">
    <div class="prop span11">
        <div class="name span2" style="margin-left:0px;">Status</div>
        <div class="value">
            
            <a href="#" class="status ${isSpeciesContributor?'selector':''}" data-type="select" data-pk="${speciesFieldInstance.id}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="status" data-original-title="Edit status">
                ${speciesFieldInstance?.status?.value()}</a>

        </div>
    </div>
    </g:if>
    <g:if test="${speciesFieldInstance?.audienceTypes}">
    <div class="prop span11">
        <div class="span2 name" style="margin-left:0px;">Audiences</div>
        <div class="value"><g:each
            in="${ speciesFieldInstance?.audienceTypes}"
            var="audienceType">

            <a href="#" class="audienceType ${isSpeciesContributor?'selector':''}" data-type="select" data-pk="${speciesFieldInstance.id}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="audienceType" data-original-title="Edit Audience Type"> ${audienceType.value}</a>
            </g:each>
        </div>
    </div>
    </g:if>
    <g:if test="${speciesFieldInstance?.licenses?.size() > 0}">
    <div class="prop span11">
        <div class="span2 name" style="margin-left:0px;">Licenses</div>
        <div class="value"><g:each status="i"
            in="${speciesFieldInstance?.licenses}" var="license">
            <a href="#" class="license ${isSpeciesContributor?'selector':''}" data-type="select" data-pk="${speciesFieldInstance.id}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="license" data-original-title="Edit license">${license.name}</a>
            </g:each>
        </div>
    </div>
    </g:if>


    <!-- references -->
    <g:if test="${speciesFieldInstance.references?.size() > 0}">
    <div class="prop span11">
        <div class="name" style="float:none;">References</div>
        <div>
            <ol>
                <g:each in="${speciesFieldInstance.references}" var="r">
                <li title="${r.title?:r.url}">
                
                <a href="#" class="${isSpeciesContributor?'editField':''}" data-type="textarea" data-rows="2"  data-pk="${speciesFieldInstance.id}" data-params="{cid:${r.id}}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="reference" data-original-title="Edit reference" title="Click to edit">
                <g:if
                test="${r.url}">
                 ${r.title?r.title:r.url}
                </g:if> <g:else>
                ${r.title }
                </g:else>
                </a>
                
                </li>
                </g:each>
                <g:if test="${isSpeciesContributor}">
                <li>
                    <a href="#" class="addField"  data-pk="${speciesFieldInstance.id}" data-type="textarea" data-rows="2"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="reference" data-original-title="Add reference" data-placeholder="Add reference"></a>
                </li>
                </g:if>

            </ol>
        </div>
    </div>
    </g:if>
    <g:elseif test="${isSpeciesContributor}">
    <div class="prop span11">
        <div class="name" style="float:none;">References</div>
            <ul>
                <li class="hidePoint">
                <a href="#" class="addField"  data-pk="${speciesFieldInstance.id}" data-type="textarea" data-rows="2" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="reference" data-original-title="Add reference" data-placeholder="Add reference"></a>
                </li>
            </ul>
    </div>
    </g:elseif>


</div>
</g:if>
</g:if>
