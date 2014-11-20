
<div class="control-group">
    <label class="control-label" for="user">${g.message(code:'search.suser')}</label> 
    <div class="controls">
        <input
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.user" value="${queryParams?queryParams['aq.user']?.encodeAsHTML():''}"
        placeholder="${g.message(code:'placeholder.search.user')}" /> 
    </div>
</div>


