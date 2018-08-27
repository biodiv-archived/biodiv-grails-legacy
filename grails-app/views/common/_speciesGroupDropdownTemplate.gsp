<%@page import="species.Habitat.HabitatType"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.groups.SpeciesGroup"%>
<%@page import="species.Habitat"%>
<div class ="control-group ${hasErrors(bean: observationInstance, field: 'group', 'error')}" style="clear:both;">
    <label class="label_group" for="group"><g:message
        code="observation.group.label" default="${g.message(code:'default.group.label')}" />
    </label> 
    <g:hasErrors bean="${observationInstance}" field="group">
    <div class="help-inline control-label">
        <g:hasErrors bean="${observationInstance}" field="group">
        <g:message code="observation.group.not_selected" />
        </g:hasErrors>
    </div>
    </g:hasErrors>
    <div class="groups_super_div" style="clear:both;">
        <%

        def selected_group = (observationInstance?.group?.name) ? observationInstance?.group?.name : 'All';
        def message_selected_group = (observationInstance?.group?.name) ? observationInstance?.group?.name : g.message(code:'default.select.group.label');
        %>

        <div class="groups_div dropdown" style="z-index:14;">
            <div class="dropdown-toggle btn selected_group selected_value " data-toggle="dropdown">
                <span style="float:left;"
                    class="group_icon species_groups_sprites active ${SpeciesGroup.findByName(selected_group).iconClass()}"
                    title="${SpeciesGroup.findByName(selected_group).name}"></span>
                <span class="display_value">${message_selected_group}</span>
                <b class="caret"></b>
            </div>
            <ul class="group_options dropdown-menu">
                <g:each in="${species.groups.SpeciesGroup.list()}" var="g">
                <li class="group_option" value="${g.id}">
                <div>
                    <span style="float:left;"
                        class="group_icon species_groups_sprites active ${g.iconClass()}"
                        title="${g.name}"></span>
                    <span class="display_value">${g.name}</span>
                </div>
                </li>
                </g:each>
            </ul>
        </div>
        <input class="group" type="hidden" name="group_id"></input>
    </div>
</div>
