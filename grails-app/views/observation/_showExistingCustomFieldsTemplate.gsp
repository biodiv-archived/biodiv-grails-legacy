<%@ page import="species.groups.CustomField"%>
<g:if test="${userGroupInstance.id && !CustomField.fetchCustomFields(userGroupInstance).isEmpty()}">
<div>
	<div>
		<table class="table table-hover" style="margin-left: 0px;">
		<thead>
				<tr>
					<th><g:message code="customField.name.label"  default="Name" /></th>
					<th><g:message code="customField.descriptoin.label" default="Description"/> </th>
					<th><g:message code="customField.type.label" default="Type"/> </th>
					<th><g:message code="customField.options.label" default="Options"/></th>
					<th><g:message code="customField.allowedMultiple.label" default="MultiSelect"/></th>
					<th><g:message  code="customField.defalutValue.label" default="Default Value" /></th>
					<th><g:message code="customField.isMandatory.label" default="Mandatory"/></th>
					<th><g:message code="customField.allowedParticipation.label" default="Participation"/></th>
					
				</tr>
			</thead>
		
			<tbody class="mainContentList">
				<g:each in="${CustomField.fetchCustomFields(userGroupInstance)}" status="i"
					var="cf">
					<tr class="mainContent">
						<td>
							${cf.name}
						</td>
						<td class="ellipsis multiline">
							${cf.notes}
						</td>
						<td>
							${cf.dataType}
						</td>
						<td class="ellipsis multiline">
							${cf.options}
						</td>
						<td>
							${cf.allowedMultiple}
						</td>
						<td>
							${cf.defaultValue}
						</td>
						<td>
							${cf.isMandatory}
						</td>
						<td>
							${cf.allowedParticipation}
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</div>  
</g:if>
