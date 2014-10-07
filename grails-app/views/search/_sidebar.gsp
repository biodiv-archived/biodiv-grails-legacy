<%@page import="species.groups.SpeciesGroup"%>

<div id="filterPanel" class="span4 sidebar" style="margin-left:0px;margin-right:18px;">
    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5> Modules </h5>
        <g:each in="${modules}" var="module">
        <label class="radio">
            <input class="searchFilter moduleFilter ${activeFilters.object_type?.contains(module.name)?'active':''} " type="radio" name="module" value="${module.name}"  ${activeFilters.object_type?.contains(module.name)?'checked':''} >${module.name} (${module.count})
        </label>
        </g:each>
    </div>

    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5> Species Groups </h5>
        <g:each in="${sGroups}" var="module">
        <label class="radio">
            <input class="searchFilter sGroupFilter ${(activeFilters.sGroup.toString() == module.name)?'active':''}" type="radio" name="sGroup" value="${module.name}"  ${(activeFilters.sGroup.toString() == module.name)?'checked':''} >${SpeciesGroup.read(Long.parseLong(module.name)).name} (${module.count})
        </label>
        </g:each>
    </div>

    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5> Contributors </h5>
        <g:each in="${contributors}" var="module">
        <label class="checkbox">
            <input class="searchFilter contributorFilter  ${activeFilters.contributor?.contains(module.name)?'active':''}" type="checkbox" name="${module.name}"  ${activeFilters.contributor?.contains(module.name)?'checked':''} >${module.name} (${module.count})
        </label>
        </g:each>
    </div>

    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5> Tags </h5>
        <g:each in="${tags}" var="module">
        <label class="checkbox">
            <input class="searchFilter tagFilter ${activeFilters.tag?.contains(module.name)?'active':''} " type="checkbox" name="${module.name}"  ${activeFilters.tag?.contains(module.name)?'checked':''}>${module.name} (${module.count})
        </label>
        </g:each>
    </div>
</div>

