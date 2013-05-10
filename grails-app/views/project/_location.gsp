<div id="location${i}" class="location-div block"
	<g:if test="${hidden}">style="display:none;"</g:if>>
	
	<g:hiddenField name='locationsList[${i}].id' value='${location?.id}' />
	<g:hiddenField name='locationsList[${i}].deleted' value='false' />
	<g:hiddenField name='locationsList[${i}].new'
		value="${location?.id == null?'true':'false'}" />

	<div class="control-group span5" style="margin-left:0px;">
		<label class="control-label" for="siteName"><g:message
				code="project.location.siteName.label" default="Site Name" /> </label>

		<div class="controls">
			<g:textField name='locationsList[${i}].siteName' class="site-name input-xlarge"
				value='${location?.siteName}' />
		</div>
	</div>
        
        <div class="control-group span5" style="margin-left:40px;">
		<label class="control-label" for="corridor"><g:message
				code="project.location.corridor.label" default="Corridor" /> </label>

		<div class="controls">
			<g:textField name='locationsList[${i}].corridor' class="corridor input-xlarge"
				value='${location?.corridor}' />
		</div>
	</div>

        <span class="del-location close_button" style="clear:both;">
	</span>
</div>
