<%@page import="species.groups.SpeciesGroup"%>

<div id="filterPanel" class="span4 sidebar" style="margin-left:0px;margin-right:18px;">
    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5> ${g.message(code:'heading.modules')} </h5>
        <g:each in="${modules}" var="module">
        <label class="radio">
            <input class="searchFilter moduleFilter ${activeFilters?.object_type?.contains(module.name)?'active':''} " type="radio" name="module" value="${module.name}"  ${activeFilters?.object_type?.contains(module.name)?'checked':''} >${module.name} (${module.count})
        </label>
        </g:each>
    </div>

</div>

