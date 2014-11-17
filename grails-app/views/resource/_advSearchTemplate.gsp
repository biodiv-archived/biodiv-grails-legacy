
<%@page import="species.Resource.ResourceType"%>
<div class="control-group">
    <label
        class="control-label" for="resourcetype">${g.message(code:'default.type.label')}</label> 
    <div class="controls">
        <select name="aq.resourcetype" multiple="multiple" class="multiselect typeFilter input-block-level">
            <g:each in="${ResourceType.toList()}" var="type">
            <option value="${type.value()}"> ${g.message(error:type)} </option>
            </g:each>
        </select>


    </div>
</div>

