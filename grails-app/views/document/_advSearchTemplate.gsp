
<div class="control-group">
    <label class="control-label" for="members">Members</label> 
    <div class="controls">
        <input id="aq.members"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.members" value="${queryParams?queryParams['aq.members']?.encodeAsHTML():''}"
        placeholder="Search all members" /> 
    </div>
</div>


<div class="control-group">
    <label class="control-label" for="text">Tags</label> 
    <div class="controls">
        <input id="aq.tag"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.tag" value="${queryParams?queryParams['aq.tag']?.encodeAsHTML():''}"
        placeholder="Search all tags" /> 
    </div>
</div>


<div class="control-group">
    <label class="control-label" for="title">Title</label> 
    <div class="controls">
        <input
        id="aq.title" data-provide="typeahead" type="text"
        class="input-block-level" name="aq.title"
        placeholder="Search by Document title" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
    </div>
</div>


<div class="control-group">
    <label
        class="control-label" for="grantee">Type</label> 
    <div class="controls">
        <input
        id="aq.type" data-provide="typeahead" type="text"
        class="input-block-level" name="aq.type"
        placeholder="Search by Description" value="${(queryParams?.get('aq.type'))?.encodeAsHTML()}" />
    </div>
</div>


<div class="control-group">
    <label
        class="control-label" for="observedOn">Last updated during</label>
    <div class="controls">

        <div id="uploadedOnDatePicker" style="position: relative;overflow:visible">
            <div id="uploadedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
                <i class="icon-calendar icon-large"></i> <span class="date"></span>
            </div>
    </div>            </div>
</div>



<div style="${params.webaddress?:'display:none;'}">

    <div class="control-group">
        <label class="radio inline"> 
            <input type="radio" id="uGroup_ALL" name="uGroup" 
            value="ALL"> Search in all groups </label> <label
            class="radio inline"> 
            <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
            value="THIS_GROUP"> Search within this group </label>
    </div>
</div>




