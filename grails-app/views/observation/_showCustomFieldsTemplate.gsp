<g:if test="${customFields?.size() > 0}">
<div style="margin-top:8px;" class="sidebar_section">
	<h5><g:message code="heading.customfields" /></h5>
	<div>
		<table class="table"
			style="margin-left: 0px;">
			<tbody>
				<g:each in="${customFields.entrySet()}" status="i"
					var="e">
					<tr>
						<td style="width:100px;">
							<b>${e.value.key}<g:if test="${e.key.isMandatory}"><span class="req">*</span></g:if></b>
						</td>
						<td>
							<div class="cfStaticVal">
								${e.value.value}
							</div>
							<div class="cfInlineEdit" style="display:none;">
								<g:if test='${e.key.allowedParticipation}'>
									<g:render template="customFieldTemplate" model="['observationInstance':observationInstance, 'customFieldInstance':e.key, hideLabel:true]"/>
								</g:if>	
							</div>	
						</td>
						<td style="width:50px;">	
							<g:if test='${e.key.allowedParticipation}'>
								<div class="editCustomField btn btn-small btn-primary" onclick="customFieldInlineEdit($(this),  '${createLink(controller:'observation', action:'updateCustomField')}', '${e.key.id}', ${observationInstance.id}); return false;">Edit</div>
							</g:if>	
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</div>  
</g:if>

