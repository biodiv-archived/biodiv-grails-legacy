
<%@page import="content.eml.Document.DocumentType"%>
<div class="control-group">
    <label class="control-label" for="title">${g.message(code:'Dcoument.title.label')}</label> 
    <div class="controls">
        <input
        data-provide="typeahead" type="text"
        class="input-block-level" name="aq.title"
        placeholder="${g.message(code:'placeholder.search.document')}" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
    </div>
</div>


<div class="control-group">
    <label
        class="control-label" for="grantee">${g.message(code:'default.type.label')}</label> 
    <div class="controls">
        <select name="aq.type" multiple="multiple" class="multiselect typeFilter input-block-level">
            <g:each in="${DocumentType.toList()}" var="type">
            <option value="${type.value()}"> ${g.message(error:type)} </option>
            </g:each>
        </select>


    </div>
</div>

