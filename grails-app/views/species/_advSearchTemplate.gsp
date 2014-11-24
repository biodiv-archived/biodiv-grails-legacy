<%@page import="species.utils.Utils"%>
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
        <input
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.sp_overview" value="${(queryParams?.get('aq.sp_overview'))?.encodeAsHTML() }"
        placeholder="${g.message(code:'placeholder.search.overview')}" />
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="aq.sp_nc">${g.message(code:'label.nomen.classification')}</label> 

    <div class="controls">
        <input data-provide="typeahead"
        type="text" class="input-block-level" name="aq.sp_nc" value="${(queryParams?.get('aq.sp_nc'))?.encodeAsHTML()}"
        placeholder="${g.message(code:'placeholder.search.nomenculature')}" />
    </div>
</div>

<div class="control-group">
    <label class="control-label" for="aq.sp_nh">${g.message(code:'label.natural.history')}</label> 

    <div class="controls">
        <input data-provide="typeahead"
        type="text" class="input-block-level" name="aq.sp_nh" value="${(queryParams?.get('aq.sp_nh'))?.encodeAsHTML()}" 
        placeholder="${g.message(code:'placeholder.field.search.species')}" /> 
    </div>
</div>

<div class="control-group">
    <label
        class="control-label" for="aq.sp_hd">${g.message(code:'label.habitat.dist')}</label> 
    <div class="controls">
        <input data-provide="typeahead"
        type="text" class="input-block-level" name="aq.sp_hd" value="${(queryParams?.get('aq.sp_hd'))?.encodeAsHTML() }"
        placeholder="${g.message(code:'placeholder.search.habitat')}" />
    </div>
</div>


