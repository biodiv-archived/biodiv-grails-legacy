<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.utils.Utils"%>


<div class="row control-group ${hasErrors(bean: observationInstance, field: 'group', 'error')}">

    <label for="group" class="control-label"><g:message
        code="observation.group.label" default="Group" /> </label>

    <div class="controls">
        <div id="groups_div" class="btn-group" style="z-index: 30;">
            <%
            def defaultGroup = observationInstance?.group
            //def defaultGroupIconFileName = (defaultGroupId)? SpeciesGroup.read(defaultGroupId).icon(ImageType.VERY_SMALL)?.fileName?.trim() : SpeciesGroup.findByName('All').icon(ImageType.VERY_SMALL)?.fileName?.trim()
            def defaultGroupValue = (defaultGroup) ? defaultGroup.name : "Select group"
            def defaultIcon = (defaultGroup) ? defaultGroup.iconClass() : "all_gall_th"
            %>

            <button id="selected_group"
                class="btn btn-large dropdown-toggle" data-toggle="dropdown"
                data-target="#groups_div">
                <i class="display_value group_icon pull-left species_groups_sprites active ${defaultIcon}"></i> ${defaultGroupValue}
                <span class="caret"></span>
            </button>

            <ul id="group_options" class="dropdown-menu">

                <g:each in="${species.groups.SpeciesGroup.list()}" var="g">
                <g:if
                test="${!g.name.equals(grailsApplication.config.speciesPortal.group.ALL)}">
                <li class="group_option" value="${g.id}" title="${g.name}">
                <a>
                    <i class="group_icon pull-left species_groups_sprites active ${g.iconClass()}"></i>
                    ${g.name}
                </a></li>
                </g:if>
                </g:each>
            </ul>


            <div class="help-inline">
                <g:hasErrors bean="${observationInstance}" field="group">
                <g:message code="observation.group.not_selected" />
                </g:hasErrors>
            </div>

        </div>
        <input id="group_id" type="hidden" name="group_id"
        value="${observationInstance?.group?.id}"></input>
    </div>
</div>

<div
    class="row control-group ${hasErrors(bean: observationInstance, field: 'habitat', 'error')}">

    <label class="control-label" for="habitat"><g:message
        code="observation.habitat.label" default="Habitat" /> </label>

    <div class="controls">
        <div id="habitat_div" class="btn-group" style="z-index: 20;">
            <%
            def defaultHabitat = observationInstance?.habitat;
            //def defaultHabitatIconFileName = (defaultHabitatId)? defaultHabitat.icon(ImageType.VERY_SMALL)?.fileName?.trim() : Habitat.findByName('All').icon(ImageType.VERY_SMALL)?.fileName?.trim()
            def defaultHabitatValue = (defaultHabitat) ? defaultHabitat.name : "Select habitat"
            def defaultHabitatIcon = (defaultHabitat) ? defaultHabitat.iconClass() : "all_gall_th"
            %>
            <button id="selected_habitat"
                class="btn btn-large dropdown-toggle" data-toggle="dropdown"
                data-target="#habitat_div">
                <i class="display_value group_icon pull-left habitats_sprites active ${defaultHabitatIcon}"></i> ${defaultHabitatValue}
                <span class="caret"></span>
            </button>

            <ul id="habitat_options" class="dropdown-menu">

                <g:each in="${species.Habitat.list()}" var="h">
                <g:if
                test="${!h.name.equals(grailsApplication.config.speciesPortal.group.ALL)}">
                <li class="habitat_option" value="${h.id}" title="${h.name}"><a>

                    <i class="group_icon pull-left habitats_sprites active ${h.iconClass()}"></i>
                    ${h.name}</a>
                </li>
                </g:if>
                </g:each>
            </ul>


            <div class="help-inline">
                <g:hasErrors bean="${observationInstance}" field="habitat">
                <g:message code="observation.habitat.not_selected" />
                </g:hasErrors>
            </div>
        </div>
    </div>
    <input id="habitat_id" type="hidden" name="habitat_id"
    value="${observationInstance?.habitat?.id}"></input>
</div>

<div
    class="row control-group ${hasErrors(bean: observationInstance, field: 'fromDate', 'error')}">

    <label for="observedOn" class="control-label"><i
            class="icon-calendar"></i>
        <g:message code="observation.observedOn.label"
        default="Observed on" /></label>

    <div class="controls textbox">
        <input name="observedOn" type="text" id="observedOn" class="input-block-level"
        value="${observationInstance?.fromDate?.format('dd/MM/yyyy')}"
        placeholder="Select date of observation (dd/MM/yyyy)" />

        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="observedOn">
            <g:if test="${observationInstance.fromDate == null}">
            <g:message code="observation.observedOn.validator.invalid_date" />
            </g:if>
            <g:else>
            <g:message code="observation.observedOn.validator.future_date" />
            </g:else>

            </g:hasErrors>
        </div>

    </div>
</div>
