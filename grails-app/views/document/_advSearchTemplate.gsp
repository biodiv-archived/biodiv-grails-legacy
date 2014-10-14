
<div class="control-group">
    <label class="control-label" for="members">${g.message(code:'default.members.label')}</label> 
    <div class="controls">
        <input id="aq.members"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.members" value="${queryParams?queryParams['aq.members']?.encodeAsHTML():''}"
        placeholder="${g.message(code:'placeholder.search.members')}" /> 
    </div>
</div>


<div class="control-group">
    <label class="control-label" for="text">${g.message(code:'default.tags.label')}</label> 
    <div class="controls">
        <input id="aq.tag"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.tag" value="${queryParams?queryParams['aq.tag']?.encodeAsHTML():''}"
        placeholder="${g.message(code:'placeholder.search.all.tags')}" /> 
    </div>
</div>


<div class="control-group">
    <label class="control-label" for="title">${g.message(code:'Dcoument.title.label')}</label> 
    <div class="controls">
        <input
        id="aq.title" data-provide="typeahead" type="text"
        class="input-block-level" name="aq.title"
        placeholder="${g.message(code:'placeholder.search.document')}" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
    </div>
</div>


<div class="control-group">
    <label
        class="control-label" for="grantee">${g.message(code:'default.type.label')}</label> 
    <div class="controls">
        <input
        id="aq.type" data-provide="typeahead" type="text"
        class="input-block-level" name="aq.type"
        placeholder="${g.message(code:'placeholder.search.description')}" value="${(queryParams?.get('aq.type'))?.encodeAsHTML()}" />
    </div>
</div>


<div class="control-group">
    <label
        class="control-label" for="observedOn">${g.message(code:'label.last.update')}</label>
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
            value="ALL"> ${g.message(code:'default.search.in.all.groups')} </label> <label
            class="radio inline"> 
            <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
            value="THIS_GROUP"> ${g.message(code:'default.search.within.this.group')} </label>
    </div>
</div>




