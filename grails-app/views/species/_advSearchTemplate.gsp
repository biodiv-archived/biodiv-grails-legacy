<%@page import="species.utils.Utils"%>
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
    <label class="control-label" for="aq.taxon"><g:message code="default.taxon.hierarchy.label" /></label> 

    <div class="controls">
        <input data-provide="typeahead" id="aq.taxon"
        type="text" class="input-block-level" name="aq.taxon" value="${(queryParams?.get('aq.taxon'))?.encodeAsHTML()}"
        placeholder="${g.message(code:'placeholder.species.taxon.hierarchy')}" />
    </div>
</div>


<div class="control-group">
    <label class="control-label" for="sp_overview">${g.message(code:'label.overview')}</label> 

    <div class="controls">
        <input id="aq.sp_overview"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.sp_overview" value="${(queryParams?.get('aq.sp_overview'))?.encodeAsHTML() }"
        placeholder="${g.message(code:'placeholder.search.overview')}" />
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="aq.sp_nc">${g.message(code:'label.nomen.classification')}</label> 

    <div class="controls">
        <input data-provide="typeahead" id="aq.sp_nc"
        type="text" class="input-block-level" name="aq.sp_nc" value="${(queryParams?.get('aq.sp_nc'))?.encodeAsHTML()}"
        placeholder="${g.message(code:'placeholder.search.nomenculature')}" />
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="aq.sp_nh">${g.message(code:'label.natural.history')}</label> 

    <div class="controls">
        <input data-provide="typeahead" id="aq.sp_nh"
        type="text" class="input-block-level" name="aq.sp_nh" value="${(queryParams?.get('aq.sp_nh'))?.encodeAsHTML()}" 
        placeholder="${g.message(code:'placeholder.field.search.species')}" /> 
    </div>
</div>

<div class="control-group">
    <label
        class="control-label" for="aq.sp_hd">${g.message(code:'label.habitat.dist')}</label> 
    <div class="controls">
        <input data-provide="typeahead" id="aq.sp_hd"
        type="text" class="input-block-level" name="aq.sp_hd" value="${(queryParams?.get('aq.sp_hd'))?.encodeAsHTML() }"
        placeholder="${g.message(code:'placeholder.search.habitat')}" />
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
        </div>
    </div>
</div>

<div class="control-group">
    <div style="${params.webaddress?:'display:none;'}">
        <label class="radio inline"> 
                <input type="radio" id="uGroup_ALL" name="uGroup" 
                value="ALL"> ${g.message(code:'default.search.in.all.groups')} </label> <label
                class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
                value="THIS_GROUP"> ${g.message(code:'default.search.within.this.group')} </label>
    </div>
</div>




