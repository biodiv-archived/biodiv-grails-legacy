<%@page import="species.groups.SpeciesGroup"%>
<%@page import="species.groups.UserGroup"%>

<div id="filterPanel" class="span4 sidebar" style="margin-left:0px;margin-right:18px;">
    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <h5 style="position:relative"> ${g.message(code:'heading.modules')}
            <span class="pull-right" style="position:absolute;top:0px;right:0px;"><button class="btn btn-link resetFilter">${g.message(code:'objectposttogroups.title.select')}</button></span>
        </h5>
        <g:each in="${objectTypes}" var="objectType">
        <g:if test="${modules[objectType.name]}">
        <label class="checkbox">
            <% boolean checked = false;
            if(activeFilters?.object_type == null) {
                checked = true
            } else if (activeFilters.object_type.contains(objectType.name)) {
                checked = true
                }%>
            <input class="searchFilter moduleFilter ${checked?'active':''} " type="checkbox" name="module" value="${objectType.name}"  ${checked?'checked':''}/>${modules[objectType.name].displayName} (${objectType.count})
        </label>
        </g:if>
        </g:each>
    </div>
</div>
