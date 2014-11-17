<%@page import="species.groups.SpeciesGroup"%>
<%@page import="species.groups.UserGroup"%>

<div id="filterPanel" class="span4 sidebar" style="margin-left:0px;margin-right:18px;">
    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5> ${g.message(code:'heading.modules')} </h5>
        <g:each in="${modules}" var="module">
        <label class="radio">
            <input class="searchFilter moduleFilter ${activeFilters?.object_type?.contains(module.name)?'active':''} " type="radio" name="module" value="${module.name}"  ${activeFilters?.object_type?.contains(module.name)?'checked':''} >${module.name} (${module.count})
        </label>
        </g:each>
    </div>

    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5> ${g.message(code:'button.user.groups')} </h5>
        <g:each in="${uGroups}" var="uGroup">
        <label class="checkbox">
            <g:set var="userGroupInstance" value="${UserGroup.read(Long.parseLong(uGroup.name))}"/>
            <input class="searchFilter uGroupFilter ${activeFilters?.uGroup?.contains(userGroupInstance.name)?'active':''} " type="checkbox" name="uGroup" value="${userGroupInstance.id}"  ${activeFilters?.uGroup?.contains(uGroup.name)?'checked':''} >${userGroupInstance.name} (${uGroup.count})
        </label>
        </g:each>
    </div>


</div>

