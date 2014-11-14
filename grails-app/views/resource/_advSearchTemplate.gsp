
<%@page import="species.Resource.ResourceType"%>
<div class="control-group">
    <label
        class="control-label" for="resourceType">${g.message(code:'default.type.label')}</label> 
    <div class="controls">
        <select name="aq.resourceType" multiple="multiple" class="multiselect typeFilter input-block-level">
            <g:each in="${ResourceType.toList()}" var="type">
            <option value="${type.value()}"> ${g.message(error:type)} </option>
            </g:each>
        </select>


    </div>
</div>

