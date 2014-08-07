<div id="location${i}" class="location-div block"
	<g:if test="${hidden}">style="display:none;"</g:if>
	
	<g:hiddenField name='locations[${i}].id' value='${location?.id}' />
	<g:hiddenField name='locations[${i}].deleted' value='false' />
	<g:hiddenField name='locations[${i}].new'
		value="${location?.id == null?'true':'false'}" />

	<div class="control-group span5" style="margin-left:0px;">
		<label class="control-label" for="siteName"><g:message
				code="project.location.siteName.label" default="Site Name" /> </label>

		<div class="controls">
			<g:textField name='locations[${i}].siteName' class="site-name input-xlarge" placeholder="Enter the siteName"
				value='${location?.siteName}' />
		</div>
	</div>
        
        <div class="control-group span5" style="margin-left:40px;">
		<label class="control-label" for="corridor"><g:message
				code="project.location.corridor.label" default="Corridor" /> </label>

		<div class="controls">
			<g:textField name='locations[${i}].corridor' class="corridor input-xlarge" placeholder="Enter the corridor"
				value='${location?.corridor}' />
		</div>
	</div>

        <span class="del-location close_button" style="clear:both;">
	</span>
</div>
