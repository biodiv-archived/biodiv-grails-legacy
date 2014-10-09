<%@page import="species.utils.Utils"%>
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
    <label class="control-label" for="aq.taxon"><g:message code="default.taxon.hierarchy.label" /></label> 

    <div class="controls">
        <input data-provide="typeahead" id="aq.taxon"
        type="text" class="input-block-level" name="aq.taxon" value="${(queryParams?.get('aq.taxon'))?.encodeAsHTML()}"
        placeholder="${g.message(code:'placeholder.species.taxon.hierarchy')}" />
    </div>
</div>


<div class="control-group">
    <label class="control-label" for="sp_overview">Overview</label> 

    <div class="controls">
        <input id="aq.sp_overview"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.sp_overview" value="${(queryParams?.get('aq.sp_overview'))?.encodeAsHTML() }"
        placeholder="Search by species overview" />
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="aq.sp_nc">Nomenclature & Classification</label> 

    <div class="controls">
        <input data-provide="typeahead" id="aq.sp_nc"
        type="text" class="input-block-level" name="aq.sp_nc" value="${(queryParams?.get('aq.sp_nc'))?.encodeAsHTML()}"
        placeholder="Search using species nomenculature & classification" />
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="aq.sp_nh">Natural History</label> 

    <div class="controls">
        <input data-provide="typeahead" id="aq.sp_nh"
        type="text" class="input-block-level" name="aq.sp_nh" value="${(queryParams?.get('aq.sp_nh'))?.encodeAsHTML()}" 
        placeholder="Field to search species natural history" /> 
    </div>
</div>

<div class="control-group">
    <label
        class="control-label" for="aq.sp_hd">Habitat and Distribution</label> 
    <div class="controls">
        <input data-provide="typeahead" id="aq.sp_hd"
        type="text" class="input-block-level" name="aq.sp_hd" value="${(queryParams?.get('aq.sp_hd'))?.encodeAsHTML() }"
        placeholder="Field to search species habitat and distribution" />
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
        </div>
    </div>
</div>

<div class="control-group">
    <div style="${params.webaddress?:'display:none;'}">
        <label class="radio inline"> 
                <input type="radio" id="uGroup_ALL" name="uGroup" 
                value="ALL"> Search in all groups </label> <label
                class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
                value="THIS_GROUP"> Search within this group </label>
    </div>
</div>




