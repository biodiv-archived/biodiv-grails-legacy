
<div class="control-group">
    <label class="control-label" for="title">${g.message(code:'Dcoument.title.label')}</label> 
    <div class="controls">
        <input
        id="aq.title" data-provide="typeahead" type="text"
        class="input-block-level" name="aq.title"
        placeholder="${g.message(code:'placeholder.search.usergroup')}" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
    </div>
</div>


<div class="control-group">
    <label
        class="control-label" for="grantee">${g.message(code:'default.pages.label')}</label> 
    <div class="controls">
        <input
        id="aq.pages" data-provide="typeahead" type="text"
        class="input-block-level" name="aq.pages"
        placeholder="${g.message(code:'placeholder.search.pages')}" value="${(queryParams?.get('aq.pages'))?.encodeAsHTML()}" />
    </div>
</div>


<div class="control-group">
    <label class="control-label" for="members">${g.message(code:'default.members.label')}</label> 
    <div class="controls">
        <input id="aq.members"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.members" value="${queryParams?queryParams['aq.members']?.encodeAsHTML():''}"
        placeholder="${g.message(code:'placeholder.search.members')}" /> 
    </div>
</div>




