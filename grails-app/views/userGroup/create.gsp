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
<r:require modules="userGroups_create" />
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
	height: 44px;
	text-align: left;
	padding: 5px;
}

.textbox input {
	text-align: left;
	width: 290px;
	height: 34px;
	padding: 5px;
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

.block small {
	color: #444444;
}

.left-indent {
	margin-left: 100px;
}

.control-group.error  .help-inline {
	padding-top: 15px
}

.cke_skin_kama .cke_editor {
	display: table !important;
}

input.dms_field {
	width: 19%;
	display: none;
}

.userOrEmail-list {
	clear:none;
	width: 300px;
}
</style>


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
			<g:set var="founders_autofillUsersId" value="id1" />
			<g:set var="members_autofillUsersId" value="id2" />
			<form id="${form_id}" action="${form_action}" method="POST"
				class="form-horizontal">
				<div class="span12 super-section" style="clear: both;">
					<div class="span11 section"
						style="position: relative; overflow: visible;">
						<h3>What will this group be called?</h3>
						<div
							class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'name', 'error')}">

							<label for="name" class="control-label"><g:message
									code="userGroup.name.label" default="Group Name" /> </label>
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
							<label for="webaddress" class="control-label"><g:message
									code="userGroup.webaddress.label" default="Web Address" /> </label>
							<div class="controls  textbox">
								<div id="groups_div" class="btn-group" style="z-index: 3;">
									<span class="whiteboard"> <g:createLink action='show'
											base="${Utils.getDomainServerUrl(request)}"></g:createLink>/</span>
									<g:textField name="webaddress"
										value="${userGroupInstance?.webaddress}" />

									<div class="help-inline">
										<g:hasErrors bean="${userGroupInstance}" field="webaddress">
											<g:message code="userGroup.webaddress.invalid" />
										</g:hasErrors>
									</div>
								</div>
							</div>
						</div>

						<div
							class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'webaddress', 'error')}">
							<label for="icon" class="control-label"><g:message
									code="userGroup.icon.label" default="Icon" /> </label>
							<div class="controls">
								<div id="groups_div" class="btn-group" style="z-index: 3;">


									<div>
										<i class="icon-picture"></i><span>Upload group icon preferably of dimensions 150px X 50px and size < 50KB</span>
										<div
											class="resources control-group ${hasErrors(bean: userGroupInstance, field: 'icon', 'error')}">

											<%def thumbnail = userGroupInstance.icon%>
											<div class='span3' style="height:80px; width:auto;margin-left: 0px;">
												<img id="thumbnail"
													src='${createLink(url: userGroupInstance.mainImage().fileName)}' class='logo'/>
												<a id="change_picture" onclick="$('#attachFile').select()[0].click();return false;"> Change Picture</a>
											</div>
											<input id="icon" name="icon" type="hidden" value='${thumbnail}' />
											<div id="image-resources-msg" class="help-inline">
												<g:renderErrors bean="${userGroupInstance}" as="list"
													field="icon" />
											</div>
										</div>
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
							<label for="founders" class="control-label"><g:message
									code="userGroup.founders.label" default="Group Founders" /> </label>
							<div class="controls  textbox">
								
									<sUser:selectUsers model="['id':founders_autofillUsersId]" />
									<input type="hidden" name="founderUserIds" id="founderUserIds" />
								
							</div>
						</div>

						<!-- div
								class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'members', 'error')}">
								<label for="members" class="control-label"><g:message
										code="userGroup.members.label" default="Invite Members" /> </label>
								<div class="controls  textbox">
									<div class="create_tags section-item">
										<sUser:selectUsers model="['id':members_autofillUsersId]"/>
										<input type="hidden" name="memberUserIds" id="memberUserIds" />
									</div>
								</div>
							</div-->
					</div>
				</div>

				<div class="span12 super-section" style="clear: both;">
					<div class="section" style="position: relative; overflow: visible;">
						<h3>Additional Information</h3>
						<div
							class="span6 block ${hasErrors(bean: userGroupInstance, field: 'description', 'error')}"
							style="width: 442px;">
							<!--label for="notes"><g:message code="observation.notes.label" default="Notes" /></label-->
							<h5>
								<label><i class="icon-pencil"></i>Description <small><g:message
											code="userGroup.description.message" default="" />
								</small>
								</label><br />
							</h5>
							<div class="section-item">
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
						<div
							class="sidebar-section block ${hasErrors(bean: userGroupInstance, field: 'aboutUs', 'error')}"
							style="width: 442px;">
							<!--label for="notes"><g:message code="observation.notes.label" default="Notes" /></label-->
							<h5>
								<label><i class="icon-pencil"></i>AboutUs <small><g:message
											code="userGroup.aboutUs.message" default="" />
								</small>
								</label><br />
							</h5>
							<div class="section-item">
								<ckeditor:editor name="aboutUs" height="200px"
									toolbar="editorToolbar">
									${userGroupInstance?.aboutUs}
								</ckeditor:editor>
								<div class="help-inline">
									<g:hasErrors bean="${userGroupInstance}" field="aboutUs">
										<g:message code="userGroup.aboutUs.invalid" />
									</g:hasErrors>
								</div>
							</div>

						</div>
						<div class="span6 block" style="width: 442px;clear:both;">
							<h5>
								<label><i class="icon-tags"></i>Tags <small><g:message
											code="observation.tags.message" default="" />
								</small>
								</label>
							</h5>
							<div class="create_tags section-item" style="clear: both;">
								<ul id="tags">
									<g:each in="${userGroupInstance.tags}" var="tag">
										<li>${tag}</li>
									</g:each>
								</ul>
							</div>
						</div>
						<div class="sidebar-section block" style="width: 442px;">
							<h5>
								<label><i class="icon-envelope"></i>Contact us at <small><g:message
											code="userGroup.contactUs.message" default="" />
								</small>
								</label>
							</h5>
							<div
								class="control-group ${hasErrors(bean: userGroupInstance, field: 'contactEmail', 'error')}">
								<!-- label class="control-label" for="contactEmail"><g:message
										code='user.password.label' default='Contact at Email' /> </label-->
								<div class="create_tags section-item" style="clear: both;">
									<input class="input-large" id="contactEmail" style="width: 95%"
										value="${userGroupInstance?.contactEmail?:currentUser.email}"
										name="contactEmail"
										placeholder="Enter a single email address where you can be contacted...">

									<g:hasErrors bean="${userGroupInstance}" field="contactEmail">
										<div class="help-inline">
											<g:renderErrors bean="${userGroupInstance}"
												field="contactEmail" />
										</div>
									</g:hasErrors>
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
					<span class="policy-text"> By submitting this form for
						creating a new group you agree to our <a href="/terms">Terms
							and Conditions</a> on the use of our site </span> <a id="createGroupSubmit"
						class="btn btn-primary" style="float: right; margin-right: 5px;">
						${form_button_val} </a>
				</div>


			</form>
			<form id="upload_resource" enctype="multipart/form-data"
				title="Add a logo for this group" method="post"
				class="${hasErrors(bean: userGroupInstance, field: 'icon', 'errors')}">

				<input type="file" id="attachFile" name="resources" accept="image/*"/> 
				<span class="msg" style="float: right"></span> 
				<input type="hidden" name='dir' value="${userGroupDir}" />
			</form>
			
		</div>
	</div>

	<r:script>
$(document).ready(function() {

    	//hack: for fixing ie image upload
        if (navigator.appName.indexOf('Microsoft') != -1) {
            $('#upload_resource').css({'visibility':'visible'});
            //$('#change_picture').hide();
        } else {
            $('#upload_resource').css({'visibility':'hidden'});
            //$('#change_picture').show();
        }
		
		$('#attachFile').change(function(e){
  			$('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
		});

     	$('#upload_resource').ajaxForm({ 
			url:'${createLink(action:'upload_resource')}',
			dataType: 'xml',//could not parse json wih this form plugin 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			 
			beforeSubmit: function(formData, jqForm, options) {
				return true;
			}, 
            xhr: function() {  // custom xhr
                myXhr = $.ajaxSettings.xhr();
                return myXhr;
            },
			success: function(responseXML, statusText, xhr, form) {
				$(form).find("span.msg").html("");
				var rootDir = '${grailsApplication.config.speciesPortal.userGroups.serverURL}'
				var dir = $(responseXML).find('dir').text();
				var dirInput = $('#upload_resource input[name="dir"]');
				if(!dirInput.val()){
					$(dirInput).val(dir);
				}
				
				$(responseXML).find('resources').find('image').each(function() {
					var file = dir + "/" + $(this).attr('fileName');
					var thumbnail = rootDir + file.replace(/\.[a-zA-Z]{3,4}$/, "${grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix}");
					$("#icon").val(file);
					$("#thumbnail").attr("src", thumbnail);
				});
				$("#image-resources-msg").parent(".resources").removeClass("error");
                $("#image-resources-msg").html("");
			}, error:function (xhr, ajaxOptions, thrownError){
					//successHandler is used when ajax login succedes
	            	var successHandler = this.success, errorHandler;
	            	handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
						var response = $.parseJSON(xhr.responseText);
						if(response.error){
							$("#image-resources-msg").parent(".resources").addClass("error");
							$("#image-resources-msg").html(response.error);
						}
						
						var messageNode = $(".message .resources");
						if(messageNode.length == 0 ) {
							$("#upload_resource").prepend('<div class="message">'+(response?response.error:"Error")+'</div>');
						} else {
							messageNode.append(response?response.error:"Error");
						}
					});
           } 
     	});  
     	
     	
	var founders_autofillUsersComp = $("#userAndEmailList_${founders_autofillUsersId}").autofillUsers({
		usersUrl : '${createLink(controller:'SUser', action: 'terms')}'
	});
	
	<g:if test="${userGroupInstance.isAttached() }">
			<g:each in="${userGroupInstance.getFounders(userGroupInstance.getFoundersCount()+1, 0)}" var="user">
		founders_autofillUsersComp[0].addUserId({'item':{'userId':'${user.id}', 'value':'${user.name}'}});
	</g:each>
		</g:if>

		<%--	var members_autofillUsersComp = $("#userAndEmailList_${members_autofillUsersId}").autofillUsers({--%>
<%--		usersUrl : '${createLink(controller:'SUser', action: 'terms')}'--%>
<%--	});--%>
		<%--	--%>
		<%--	<g:each in="${userGroupInstance?.getMembers()}" var="user">--%>
		<%--		members_autofillUsersComp[0].addUserId('item':{{'userId':'${user.id}', 'value':'${user.name}'}});--%>
		<%--	</g:each>--%>
		<%--	--%>
	 $("#createGroupSubmit").click(function(){
		$('#founderUserIds').val(founders_autofillUsersComp[0].getEmailAndIdsList().join(","));
		//$('#memberUserIds').val(members_autofillUsersComp[0].getEmailAndIdsList().join(","));
		var tags = $("#tags").tagit("tags");
        	$.each(tags, function(index){
        		var input = $("<input>").attr("type", "hidden").attr("name", "tags."+index).val(this.label);
				$('#${form_id}').append($(input));	
        	})
        $("#${form_id}").submit();        	
        return false;
        
	});
	
	$(".tagit-input").watermark("Add some tags");
	$("#tags").tagit({select:true,  tagSource: "${g.createLink(action: 'tags')}", triggerKeys:['enter', 'comma', 'tab'], maxLength:30});
	$(".tagit-hiddenSelect").css('display','none');
});
</r:script>

</body>

</html>
