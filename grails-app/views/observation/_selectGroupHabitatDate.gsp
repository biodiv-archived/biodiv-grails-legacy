<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.utils.Utils"%>

<div class="control-group ${hasErrors(bean: observationInstance, field: 'group', 'error')} ${hasErrors(bean: observationInstance, field: 'habitat', 'error')}">
    <label for="group" class="control-label"><g:message
        code="observation.groupHabitat.label" default="${g.message(code:'default.group.habitat.label')}" /> <span class="req">*</span></label>
    <div class="filters controls textbox" style="position: relative;">
        <obv:showGroupFilter
        model="['observationInstance':observationInstance, 'hideAdvSearchBar':true]" />
    <div class="help-inline">
        <g:hasErrors bean="${observationInstance}" field="group">
        <g:message code="observation.group.not_selected" />
        </g:hasErrors>

        <g:hasErrors bean="${observationInstance}" field="habitat">
        <g:message code="observation.habitat.not_selected" />
        </g:hasErrors>
    </div>
    </div>
</div>

