
<div class="control-group">
    <label class="control-label" for="title">${g.message(code:'Dcoument.title.label')}</label> 
    <div class="controls">
        <input
        data-provide="typeahead" type="text"
        class="input-block-level" name="aq.title"
        placeholder="${g.message(code:'placeholder.search.usergroup')}" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
    </div>
</div>


<div class="control-group">
    <label
        class="control-label" for="grantee">${g.message(code:'default.pages.label')}</label> 
    <div class="controls">
        <input
        data-provide="typeahead" type="text"
        class="input-block-level" name="aq.pages"
        placeholder="${g.message(code:'placeholder.search.pages')}" value="${(queryParams?.get('aq.pages'))?.encodeAsHTML()}" />
    </div>
</div>


