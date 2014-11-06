
<div
    class=" control-group ${hasErrors(bean: observationInstance, field: 'fromDate', 'error')} ${hasErrors(bean: observationInstance, field: 'toDate', 'error')}">

    <label for="fromOn" class="control-label"><i
            class="icon-calendar"></i>
        <g:message code="observation.observedOn.label"
        default="${g.message(code:'default.observed.on.label')}" /><span class="req">*</span></label>


    <div class="controls textbox">
        <g:if test="${params.controller == 'checklist'}">

        <input name="fromDate" type="text" class="date" class="input-block-level"
        value="${observationInstance?.fromDate?.format('dd/MM/yyyy')}"
        placeholder="${g.message(code:'placeholder.dateinput.select.fromdate')}" />

        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="fromDate">
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
        placeholder="${g.message(code:'placeholder.dateinput.select.todate')}" />

        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="toDate">
            <g:if test="${observationInstance.toDate == null}">
            	<g:message code="observation.observedOn.validator.invalid_date" />
            </g:if>
            <g:elseif test="${observationInstance.toDate < observationInstance.fromDate}">
            	<g:message code="observation.toDate.validator.invalid_date_range" />
            </g:elseif>
            <g:else>
            	<g:message code="observation.observedOn.validator.future_date" />
            </g:else>

            </g:hasErrors>
        </div>

        </g:if>
        <g:else>

        <input name="fromDate" type="text" class = "fromDate date input-block-level"
        value="${observationInstance?.fromDate?.format('dd/MM/yyyy')}"
        placeholder="${g.message(code:'placeholder.dateinput.select.date')}" />

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
