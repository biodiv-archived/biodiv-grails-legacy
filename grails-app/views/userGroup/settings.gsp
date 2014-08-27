<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<g:set var="title" value="Settings"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>

<r:require modules="userGroups_show" />
</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate />


		<div class="userGroup-section">
		
			<div class="super-section"><a 
				href="${uGroup.createLink(mapping:'userGroup', action:'edit', userGroup:userGroupInstance)}"> <i
				class="icon-edit"></i><g:message code="button.edit.group" /> </a></div>
			<div class="super-section" style="clear: both;">
			
				<div class="section" style="position: relative; overflow: visible;">
					<h3><g:message code="usergroup.display.settings" /></h3>
					<form class="form-horizontal"
						action="${uGroup.createLink(mapping:'userGroup', action:'settings', params:['webaddress':userGroupInstance.webaddress])}"
						id='groupSettingForm' name='groupSettingForm' method="POST">
						<g:hiddenField name="id" value="${userGroupInstance.id}" />

						<uGroup:showGeneralSettings model="['userGroupInstance':userGroupInstance]" />
						
						<div class="" style="margin-top: 20px; margin-bottom: 40px;">
							<input type="submit" value="Update"
								class="btn btn-primary" 
								style="clear: both; float:right; border-radius: 5px" />
							</div>
					</form>
				</div>
			</div>
		</div>
	</div>

<r:script>
$(document).ready(function(){
	$('#groupSettingForm').bind('submit', function(event) {
		$('#homePage').val(getSelectedVal('home_page_label'));
		$('#theme').val(getSelectedVal('theme_label')); 
	});
});
</r:script>

</body>
</html>
