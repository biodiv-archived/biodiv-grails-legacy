<div id="location${i}" class="location-div"
	<g:if test="${hidden}">style="display:none;"</g:if>>
	
	<table style="background-color: whitesmoke;">
	<thead></thead>
	<tbody>
	<tr>
	<td>
	<g:hiddenField name='locationsList[${i}].id' value='${location?.id}' />
	<g:hiddenField name='locationsList[${i}].deleted' value='false' />
	<g:hiddenField name='locationsList[${i}].new'
		value="${location?.id == null?'true':'false'}" />

	<div class="control-group">
		<label class="control-label" for="siteName"><g:message
				code="project.location.siteName.label" default="Site Name" /> </label>

		<div class="controls">
			<g:textField name='locationsList[${i}].siteName' class="site-name"
				value='${location?.siteName}' />
		</div>
	</div>
</td>
<td>
	<div class="control-group">
		<label class="control-label" for="corridor"><g:message
				code="project.location.corridor.label" default="Corridor" /> </label>

		<div class="controls">
			<g:textField name='locationsList[${i}].corridor' class="corridor"
				value='${location?.corridor}' />
		</div>
	</div>

</td>
<td>
	<span class="del-location" style="margin-left:20px; padding:5px;"> <img
		src="${resource(dir:'images/skin', file:'remove.jpg')}"
		style="vertical-align: middle;" />
	</span>
	</td>
	</tr>
	</tbody>
	</table>
</div>
