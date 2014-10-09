
<div class="control-group">
    <label class="control-label" for="title">Title</label> 
    <div class="controls">
        <input
        id="aq.title" data-provide="typeahead" type="text"
        class="input-block-level" name="aq.title"
        placeholder="Search by UserGroup title" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
    </div>
</div>


<div class="control-group">
    <label
        class="control-label" for="grantee">Pages</label> 
    <div class="controls">
        <input
        id="aq.pages" data-provide="typeahead" type="text"
        class="input-block-level" name="aq.pages"
        placeholder="Search by pages" value="${(queryParams?.get('aq.pages'))?.encodeAsHTML()}" />
    </div>
</div>


<div class="control-group">
    <label class="control-label" for="members">Members</label> 
    <div class="controls">
        <input id="aq.members"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.members" value="${queryParams?queryParams['aq.members']?.encodeAsHTML():''}"
        placeholder="Search all members" /> 
    </div>
</div>




