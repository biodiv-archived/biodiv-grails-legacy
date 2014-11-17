<%@page import="species.groups.SpeciesGroup"%>
<%@page import="species.groups.UserGroup"%>

<div id="filterPanel" class="span4 sidebar" style="margin-left:0px;margin-right:18px;">
    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5> ${g.message(code:'heading.modules')} </h5>
        <g:each in="${modules}" var="module">
        <label class="checkbox">
            <% boolean checked = false;
            if(activeFilters?.object_type == null) {
                checked = true
            } else if (activeFilters.object_type.contains(module.name)) {
                checked = true
                }%>
            <input class="searchFilter moduleFilter ${checked?'active':''} " type="checkbox" name="module" value="${module.name}"  ${checked?'checked':''} >${module.name} (${module.count})
        </label>
        </g:each>
    </div>

    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5> ${g.message(code:'button.user.groups')} </h5>
        <g:each in="${uGroups}" var="uGroup">
        <label class="checkbox">
            <g:set var="userGroupInstance" value="${UserGroup.read(Long.parseLong(uGroup.name))}"/>
            <% checked = false;
            //HACK need to fix
            if((activeFilters?.uGroup == null) || (activeFilters.uGroup.contains(userGroupInstance.id.toString()))) {
                checked = true
            } %>
            <input class="searchFilter uGroupFilter ${checked?'active':''} " type="checkbox" name="uGroup" value="${userGroupInstance.id}"  ${checked?'checked':''} >${userGroupInstance.name} (${uGroup.count})
        </label>
        </g:each>
    </div>


</div>

