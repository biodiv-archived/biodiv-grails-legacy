
<div class="control-group">
    <label class="control-label" for="members">${g.message(code:'search.suser')}</label> 
    <div class="controls">
        <input
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.members" value="${queryParams?queryParams['aq.members']?.encodeAsHTML():''}"
        placeholder="${g.message(code:'placeholder.search.members')}" /> 
    </div>
</div>


