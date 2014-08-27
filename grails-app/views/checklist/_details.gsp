<div
    class="row control-group ${hasErrors(bean: observationInstance, field: 'title', 'error')}">

    <label for="title" class="control-label"><g:message
        code="checklist.title.label" default="Title" /> <span class="req">*</span></label>
    <div class="controls textbox">
        <g:textField name="title" value="${observationInstance?.title}" class="input-block-level" placeholder="${g.message(code:'checklist.details.enter.name')}" />
        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="title">
            <g:eachError bean="${observationInstance}" field="title">
            <li><g:message error="${it}" /></li>
            </g:eachError>
            </g:hasErrors>
        </div>
    </div>
</div>
<div
    class="row control-group ${hasErrors(bean: observationInstance, field: 'license', 'error')}">

    <label for="license" class="control-label"><g:message
        code="checklist.license.label" default="License" /> <span class="req">*</span></label>
    <div class="controls">
	<g:render template="/observation/selectLicense" model="['i':0, 'selectedLicense':observationInstance?.license]"/>
        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="license">
            <g:eachError bean="${observationInstance}" field="license">
            <li><g:message error="${it}" /></li>
            </g:eachError>
            </g:hasErrors>
        </div>
    </div>
</div>
<div
    class="row control-group ${hasErrors(bean: observationInstance, field: 'publicationDate', 'error')}">

    <label for="publicationDate" class="control-label"><i
            class="icon-calendar"></i>
        <g:message code="observation.publicationDate.label"
        default="Publication Date" /></label>


    <div class="controls textbox">

        <input name="publicationDate" type="text" class="date input-block-level"
        value="${observationInstance?.publicationDate?.format('dd/MM/yyyy')}"
        placeholder="${g.message(code:'checklist.details.select.date')}" />

        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="publicationDate">
            <g:if test="${observationInstance.publicationDate == null}">
            <g:message code="observation.observedOn.validator.invalid_date" />
            </g:if>
            <g:else>
            <g:message code="observation.observedOn.validator.future_date" />
            </g:else>

            </g:hasErrors>
        </div>
    </div>
</div>
<div
    class="row control-group ${hasErrors(bean: observationInstance, field: 'attribution', 'error')}">

    <label for="title" class="control-label"><g:message
        code="checklist.attribution.label" default="Attribution" /> </label>
    <div class="controls textbox">
        <g:textField name="attributions" value="${params.attributions ?: (observationInstance?.attributions?.collect{ it.name}?.join(', ')) }" class="input-block-level" placeholder="${g.message(code:'checklist.details.enter.attribution')}" />
        <div class="help-inline">
            <g:hasErrors bean="${observationInstance}" field="attributions">
            <g:eachError bean="${observationInstance}" field="attributions">
            <li><g:message error="${it}" /></li>
            </g:eachError>
            </g:hasErrors>
        </div>
    </div>
</div>
<div
    class="row control-group ${hasErrors(bean: observationInstance, field: 'refText', 'error')}">

    <label for="refText" class="control-label"><g:message
        code="checklist.refText.label" default="References" /> </label>
    <div class="controls textbox">
        <ckeditor:config var="toolbar_editorToolbar">
        [
        [ 'Bold', 'Italic' ]
        ]
        </ckeditor:config>
        <ckeditor:editor name="refText" height="53px" toolbar="editorToolbar">
        ${observationInstance?.refText}
        </ckeditor:editor>
    </div>
</div>
