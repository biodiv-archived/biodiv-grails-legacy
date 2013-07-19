<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.utils.Utils"%>

<div class="row control-group ${hasErrors(bean: observationInstance, field: 'group', 'error')} ${hasErrors(bean: observationInstance, field: 'habitat', 'error')}">
    <label for="group" class="control-label"><g:message
        code="observation.groupHabitat.label" default="Group & Habitat" /> </label>
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

<div
    class="row control-group ${hasErrors(bean: observationInstance, field: 'fromDate', 'error')}">

    <label for="fromOn" class="control-label"><i
            class="icon-calendar"></i>
        <g:message code="observation.observedOn.label"
        default="Observed on" /></label>


    <div class="controls textbox">
        <g:if test="${params.controller == 'checklist'}">

        <input name="fromDate" type="text" class="date" class="input-block-level"
        value="${observationInstance?.fromDate?.format('dd/MM/yyyy')}"
        placeholder="Select from date (dd/MM/yyyy)" />

        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="fromOn">
            <g:if test="${observationInstance.fromDate == null}">
            <g:message code="observation.observedOn.validator.invalid_date" />
            </g:if>
            <g:else>
            <g:message code="observation.observedOn.validator.future_date" />
            </g:else>

            </g:hasErrors>
        </div>


        <input name="toDate" type="text" class="date" class="input-block-level"
        value="${observationInstance?.toDate?.format('dd/MM/yyyy')}"
        placeholder="Select to date (dd/MM/yyyy)" />

        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="toOn">
            <g:if test="${observationInstance.toDate == null}">
            <g:message code="observation.observedOn.validator.invalid_date" />
            </g:if>
            <g:else>
            <g:message code="observation.observedOn.validator.future_date" />
            </g:else>

            </g:hasErrors>
        </div>

        </g:if>
        <g:else>

        <input name="fromDate" type="text" id="fromDate" class="input-block-level"
        value="${observationInstance?.fromDate?.format('dd/MM/yyyy')}"
        placeholder="Select date (dd/MM/yyyy)" />

        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="fromOn">
            <g:if test="${observationInstance.fromDate == null}">
            <g:message code="observation.observedOn.validator.invalid_date" />
            </g:if>
            <g:else>
            <g:message code="observation.observedOn.validator.future_date" />
            </g:else>

            </g:hasErrors>
        </div>


        </g:else>
    </div>
</div>
