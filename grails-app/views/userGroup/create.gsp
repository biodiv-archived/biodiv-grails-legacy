<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'userGroup.label', default: 'Create New Group')}" />
<title>
	${entityName}
</title>
<g:javascript src="tagit.js"></g:javascript>
<g:javascript src="jquery/jquery.watermark.min.js"></g:javascript>
<link rel="stylesheet"
	href="${resource(dir:'css',file:'tagit/tagit-custom.css')}"
	type="text/css" media="all" />
<style>
.btn-group.open .dropdown-menu {
	top: 43px;
}

.btn-large .caret {
	margin-top: 13px;
	position: absolute;
	right: 10px;
}

.btn-group .btn-large.dropdown-toggle {
	width: 300px;
	height:44px;
	text-align: left;
	padding:5px;
}

.textbox input{
	text-align: left;
	width: 290px;
	height:34px;
	padding:5px;
}

.form-horizontal .control-label {
	padding-top: 15px;
}

.btn-large {
	font-size: 13px;
}

.block {
	border-radius: 5px;
	background-color: #a6dfc8; 
	margin: 3px;
}

.block label {
	float: left; 
	text-align: left; 
	padding: 10px; 
	width: auto;
}

.block small{
    color: #444444;
}

#help-identify {
	height: 0;
    left: 300px;
    padding: 0;
    position: relative;
    top: -35px;
}

.left-indent {
	margin-left:100px;
}
.control-group.error  .help-inline {
	padding-top : 15px
}

.cke_skin_kama .cke_editor {
display: table !important;
}

input.dms_field {
width: 19%;
display: none;       
}

</style>

<g:javascript src="species/users.js"></g:javascript>
</head>
<body>
	<div class="container outer-wrapper">

		<div class="observation_create row">
			<div class="span12">
				<div class="page-header clearfix">
					<h1>
						${entityName}
					</h1>
				</div>
				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>

				<g:hasErrors bean="${userGroupInstance}">
					<i class="icon-warning-sign"></i>
					<span class="label label-important"> <g:message
							code="fix.errors.before.proceeding" default="Fix errors" /> </span>
					<%--<g:renderErrors bean="${userGroupInstance}" as="list" />--%>
				</g:hasErrors>
				</div>
				<%
				def form_id = "createGroup"
				def form_action = createLink(action:'save')
				def form_button_name = "Create Group"
				def form_button_val = "Create Group"
				if(params.action == 'edit' || params.action == 'update'){
					//form_id = "updateGroup"
					form_action = createLink(action:'update', id:userGroupInstance.id)
				 	form_button_name = "Update Group"
					form_button_val = "Update Group"
				}
			
				%>
				<g:set var="founders_autofillUsersId" value="id1"/>
				<g:set var="members_autofillUsersId" value="id2"/>
				<form id="${form_id}" action="${form_action}" method="POST"
					class="form-horizontal">
					<div class="span12 super-section" style="clear: both;">
						<div class="span11 section"
							style="position: relative; overflow: visible;">
							<h3>What will this group be called?</h3>
							<div
								class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'name', 'error')}">

								<label for="name"  class="control-label"><g:message code="userGroup.name.label"
										default="Group Name" /> </label>
								<div class="controls textbox">
									<div id="groups_div" class="btn-group" style="z-index: 3;">
										<g:textField name="name" value="${userGroupInstance?.name}" />

										<div class="help-inline">
											<g:hasErrors bean="${userGroupInstance}" field="name">
												<g:message code="userGroup.name.invalid" />
											</g:hasErrors>
										</div>
									</div>
								</div>
							</div>
							<div
								class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'webaddress', 'error')}">
								<label for="webaddress"  class="control-label"><g:message
										code="userGroup.webaddress.label" default="Web Address" /> </label>
								<div class="controls  textbox">
									<div id="groups_div" class="btn-group" style="z-index: 3;">
									<span class="whiteboard">
										<g:createLink action='show' base="${Utils.getDomainServerUrl(request)}"></g:createLink>/</span>
										<g:textField name="webaddress" value="${userGroupInstance?.webaddress}" />

										<div class="help-inline">
											<g:hasErrors bean="${userGroupInstance}" field="webaddress">
												<g:message code="userGroup.webaddress.invalid" />
											</g:hasErrors>
										</div>
									</div>
								</div>
							</div>
						</div>

					</div>
					
					<div class="span12 super-section" style="clear: both;">
						<div class="span11 section"
							style="position: relative; overflow: visible;">
							<h3>Driven By</h3>
							<div
								class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'founders', 'error')}">
								<label for="founders"  class="control-label"><g:message
										code="userGroup.founders.label" default="Group Founders" /> </label>
								<div class="controls  textbox">
									<div class="create_tags section-item">
										<sUser:selectUsers  model="['id':founders_autofillUsersId]"/>
										<input type="hidden" name="founderUserIds" id="founderUserIds" />
									</div>
								</div>
							</div>
							
							<div
								class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'members', 'error')}">
								<label for="members" class="control-label"><g:message
										code="userGroup.members.label" default="Invite Members" /> </label>
								<div class="controls  textbox">
									<div class="create_tags section-item">
										<sUser:selectUsers model="['id':members_autofillUsersId]"/>
										<input type="hidden" name="memberUserIds" id="memberUserIds" />
									</div>
								</div>
							</div>
						</div>
					</div>
					
					<div class="span12 super-section" style="clear: both;">
						<div class="span11 section"
							style="position: relative; overflow: visible;">
							<h3>Interested In</h3>

						</div>
					</div>

					
					<div class="span12 super-section" style="clear: both;">
						<div class="section"
							style="position: relative; overflow: visible;">
							<h3>Additional Information</h3>
							<div class="span6 block">
								<!--label for="notes"><g:message code="observation.notes.label" default="Notes" /></label-->
								<h5><label><i
									class="icon-pencil"></i>Description </label><br />
								</h5>
								<div class="section-item" style="margin-right: 10px;">
										<!-- g:textArea name="description" rows="10" value=""
										class="text ui-corner-all" /-->
										<ckeditor:config var="toolbar_editorToolbar">
										[
	    									[ 'Bold', 'Italic' ]
										]
										</ckeditor:config>
										<ckeditor:editor name="description" height="200px"
											toolbar="editorToolbar">
											${userGroupInstance?.description}
										</ckeditor:editor>
										<div class="help-inline">
											<g:hasErrors bean="${userGroupInstance}" field="description">
												<g:message code="userGroup.description.invalid" />
											</g:hasErrors>
										</div>
								</div>
								
							</div>
							<div class="sidebar-section block">
								<h5><label><i
									class="icon-tags"></i>Tags <small><g:message code="observation.tags.message" default="" /></small></label>
								</h5>
								<div class="create_tags section-item" style="clear: both;">
									<ul id="tags" name="tags">
										<g:each in="${userGroupInstance.tags}" var="tag">
											<li>${tag}</li>
										</g:each>
									</ul>
								</div>
							</div>
						</div>
					</div>
					<div class="span12 super-section" style="clear: both;">
						<div class="span11 section"
							style="position: relative; overflow: visible;">
							<h3>Pick a Theme</h3>

						</div>
					</div>		
								
					<div class="span12" style="margin-top: 20px; margin-bottom: 40px;">
						<g:if test="${userGroupInstance?.id}">
							<div class="btn btn-danger"
								style="float: right; margin-right: 5px;">
								<a
									href="${createLink(action:'deleted', id:userGroupInstance.id)}"
									onclick="return confirm('${message(code: 'default.userGroup.delete.confirm.message', default: 'This group and its content will be deleted. Are you sure ?')}');">Delete
									Group </a>
							</div>
						</g:if>
						<span class="policy-text"> By submitting this form for creating a new group you agree to
										our <a href="/terms">Terms and Conditions</a> on the use of
										our site </span>
						<a id="createGroupSubmit" class="btn btn-primary"
							style="float: right; margin-right: 5px;"> ${form_button_val} </a>
					</div>


				</form>
			</div>
		</div>
	
<g:javascript>
$(document).ready(function() {

	var founders_autofillUsersComp = $("#userAndEmailList_${founders_autofillUsersId}").autofillUsers({
		usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
	});
	
	var members_autofillUsersComp = $("#userAndEmailList_${members_autofillUsersId}").autofillUsers({
		usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
	});
	
	 $("#createGroupSubmit").click(function(){
		$('#founderUserIds').val(founders_autofillUsersComp[0].getEmailAndIdsList().join(","));
		$('#memberUserIds').val(members_autofillUsersComp[0].getEmailAndIdsList().join(","));
		var tags = $("ul[name='tags']").tagit("tags");
        	$.each(tags, function(index){
        		var input = $("<input>").attr("type", "hidden").attr("name", "tags."+index).val(this);
				$('#${form_id}').append($(input));	
        	})
        $("#${form_id}").submit();        	
        return false;
        
	});
	
	$(".tagit-input").watermark("Add some tags");
	$("#tags").tagit({select:true,  tagSource: "${g.createLink(action: 'tags')}", triggerKeys:['enter', 'comma', 'tab'], maxLength:30});
	$(".tagit-hiddenSelect").css('display','none');
});
</g:javascript>

</body>

</html>
