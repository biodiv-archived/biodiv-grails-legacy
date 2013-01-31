<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<%@ page import="species.Habitat"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'userGroup.label', default: 'Create New Group')}" />
<title>
	${entityName}
</title>
<script src="https://maps.googleapis.com/maps/api/js?sensor=false"></script>
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

.cke_skin_kama .cke_editor {
	display: table !important;
}

input.dms_field {
	width: 19%;
	display: none;
}

.userOrEmail-list {
	clear:none;
}
#cke_description {
width: 100%;
min-width: 100%;
max-width: 100%;
}
</style>


</head>
<body>
<%
				def form_id = "createGroup"
				def form_action = uGroup.createLink(mapping:'userGroupGeneric', controller:'userGroup', action:'save')
				def form_button_name = "Create Group"
				def form_button_val = "Create Group"
				
				if(params.action == 'edit' || params.action == 'update'){
					//form_id = "updateGroup"
					form_action = uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'update', 'userGroup':userGroupInstance)
				 	form_button_name = "Update Group"
					form_button_val = "Update Group"
					entityName = "Edit Group"
				}
				%>
		<div class="observation_create">
			<div class="span12">
				<uGroup:showSubmenuTemplate  model="['entityName':entityName]"/>
						

				<g:hasErrors bean="${userGroupInstance}">
					<i class="icon-warning-sign"></i>
					<span class="label label-important"> <g:message
							code="fix.errors.before.proceeding" default="Fix errors" /> </span>
					<%--<g:renderErrors bean="${userGroupInstance}" as="list" />--%>
				</g:hasErrors>
			
			
			<g:set var="founders_autofillUsersId" value="id1" />
			<g:set var="members_autofillUsersId" value="id2" />
			<form id="${form_id}" action="${form_action}" method="POST"
				class="form-horizontal">
				<input type="hidden" name="id" value="${userGroupInstance?.id}"/>
				<div class="super-section">
					<div class="section"
						style="position: relative; overflow: visible;">
						<h3>Groups</h3>
						<div
							class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'name', 'error')}">

							<label for="name" class="control-label"><g:message
									code="userGroup.name.label" default="Group Name" /> </label>
							<div class="controls textbox">
								<div id="groups_div" class="btn-group" style="z-index: 3;">
									<g:if test="${userGroupInstance?.domainName }">
										<g:textField name="name" value="${userGroupInstance?.name}" placeholder="Enter a group name..." readonly="true"/>
									</g:if>
									<g:else>
										<g:textField name="name" value="${userGroupInstance?.name}" placeholder="Enter a group name..." />
									</g:else>
									<div class="help-inline">
										<g:hasErrors bean="${userGroupInstance}" field="name">
											<g:eachError bean="${userGroupInstance}" field="name">
    											<li><g:message error="${it}" /></li>
											</g:eachError>
										</g:hasErrors>
									</div>
								</div>
							</div>
						</div>
						
												
						<div
							class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'description', 'error')}"
							>
							<!--label for="notes"><g:message code="observation.notes.label" default="Notes" /></label-->
							
								<label for="description" class="control-label">Description</label>
							<div class="controls  textbox">
								
								<textarea id="description" name="description" placeholder="Write a small description about your activities in this Group. This will appear on the Group's home page...">${userGroupInstance?.description}</textarea>
								
								<script type='text/javascript'>
									CKEDITOR.plugins.addExternal( 'confighelper', '${request.contextPath}/js/ckeditor/plugins/confighelper/' );
									
									var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
									CKEDITOR.replace('description', config);
								</script>
								<div class="help-inline">
									<g:hasErrors bean="${userGroupInstance}" field="description">
										<g:eachError bean="${userGroupInstance}" field="description">
    											<li><g:message error="${it}" /></li>
										</g:eachError>
									</g:hasErrors>
								</div>
							</div>

						</div>
						
						<!-- div
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
											<g:eachError bean="${userGroupInstance}" field="webaddress">
    											<li><g:message error="${it}" /></li>
											</g:eachError>
										</g:hasErrors>
									</div>
								</div>
							</div>
						</div-->

						<div
							class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'icon', 'error')}">
							<label for="icon" class="control-label"><g:message
									code="userGroup.icon.label" default="Icon" /> </label>
							<div class="controls">
								<div id="groups_div" class="btn-group" style="z-index: 3;">
										<div
											class="resources control-group ${hasErrors(bean: userGroupInstance, field: 'icon', 'error')}">

											<%def thumbnail = userGroupInstance.icon%>
											<div style="max-height:100px; width:auto;margin-left: 0px;">
												<a id="change_picture" onclick="$('#attachFile').select()[0].click();return false;">
													<img id="thumbnail"
													src='${createLink(url: userGroupInstance.mainImage().fileName)}' class='logo '/>
													<div><i class="icon-picture"></i>Upload group icon preferably of dimensions 150px X 50px and size < 50KB</div>
												</a>
												
											</div>
											<input id="icon" name="icon" type="hidden" value='${thumbnail}' />
											
											<div id="image-resources-msg" class="help-inline">
												<g:hasErrors bean="${userGroupInstance}" field="icon">
													<g:eachError bean="${userGroupInstance}" field="icon">
    													<li><g:message error="${it}" /></li>
													</g:eachError>
												</g:hasErrors>
											</div>
											
										</div>
									
								</div>
							</div>
						</div>
						
						<uGroup:showGeneralSettings model="['userGroupInstance':userGroupInstance]" />
					
					</div>
				</div>

				<div class="super-section" style="clear: both;">
					<div class="section"
						style="position: relative; overflow: visible;">
						<h3>Founders</h3>
						<div
							class="row control-group left-indent ${hasErrors(bean: userGroupInstance, field: 'founders', 'error')}">
							<label for="founders" class="control-label"><g:message
									code="userGroup.founders.label" default="Invite Founders" /> </label>
							<div class="controls  textbox">
								<sUser:selectUsers model="['id':founders_autofillUsersId]" />
								<input type="hidden" name="founderUserIds" id="founderUserIds" />
								<textarea name="founderMsg" rows="3" style="max-width:100%;min-width:100%;" placeholder="Place your message here" >You are invited to be a founder for the group. Please click on the link to accept being a founder for this group</textarea>
								
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

				<!-- div class="super-section" style="clear: both;">
					<div class="section" style="position: relative; overflow: visible;">
						<h3>Additional Information</h3>
						
						
					</div>
				</div-->

                                <div class="super-section" style="clear: both;">
					<div class="section"
						style="position: relative; overflow: visible;">
						<h3>Location</h3>
                                                <uGroup:locationSelector model="['userGroupInstance':userGroupInstance]"/>
                                        </div>
                                </div>

				<div class="super-section" style="clear: both;">
					<div class="section"
						style="position: relative; overflow: visible;">
						<h3>Interests</h3>
						
						<div class="row control-group left-indent">
							
								<label class="control-label">Species Groups & Habitats
								</label>
							
							<div class="filters controls textbox" style="position: relative;">
								<obv:showGroupFilter
									model="['observationInstance':observationInstance, 'hideAdvSearchBar':true]" />
							</div>
						</div>
						
						<div class="row control-group left-indent">
							
								<label class="control-label">Tags <small><g:message
											code="observation.tags.message" default="" />
								</small>
								</label>
							
							<div class="create_tags controls  textbox" >
								<ul id="tags" style="margin-left:0px;">
									<g:each in="${userGroupInstance.tags}" var="tag">
										<li>${tag}</li>
									</g:each>
								</ul>
							</div>
						</div>
						
					</div>
				</div>

				<div class="super-section" style="clear: both;">
					<div class="section"
						style="position: relative; overflow: visible;">
						<h3>Permissions</h3>
						<div class="row control-group left-indent">
							
								<label class="checkbox" style="text-align: left;"> 
								 <g:checkBox style="margin-left:0px;"
												name="allowUsersToJoin" checked="${userGroupInstance.allowUsersToJoin}"/>
								 <g:message code="userGroup.permissions.members.joining" default="Can users join the Group without invitation?" /> </label>
						</div>
						<div class="row control-group left-indent">
							
								<label class="checkbox" style="text-align: left;"> 
								 <g:checkBox style="margin-left:0px;"
												name="allowObvCrossPosting" checked="${userGroupInstance.allowObvCrossPosting}"/>
								 <g:message code="userGroup.permissions.observations.crossposting" default="Can members cross post Observations to other Groups as well?" /> </label>
						</div>
						<div class="row control-group left-indent">
							
								<label class="checkbox" style="text-align: left;"> 
								 <g:checkBox style="margin-left:0px;"
												name="allowMembersToMakeSpeciesCall" checked="${userGroupInstance.allowMembersToMakeSpeciesCall}"/>
								 <g:message code="userGroup.permissions.observations.allowMembersToMakeSpeciesCall" default="Can members make species call on Observations?" /> </label>
						</div>
						<div class="row control-group left-indent">
							
								<label class="checkbox" style="text-align: left;"> 
								 <g:checkBox style="margin-left:0px;"
												name="allowNonMembersToComment" checked="${userGroupInstance.allowNonMembersToComment}"/>
								 <g:message code="userGroup.permissions.comments.bynonmembers" default="Can non-members comment on Observations of the Group? " /> </label>
						</div>
						
					</div>
				</div>

				<div class="" style="margin-top: 20px; margin-bottom: 40px;">
				
					<g:if test="${userGroupInstance?.id}">
						<a href="${createLink(mapping:'userGroup', action:'show', params:['webaddress':userGroupInstance.webaddress])}" class="btn"
							style="float: right; margin-right: 5px;"> Cancel </a>
					</g:if>
					<g:else>
					<a href="${createLink(mapping:'userGroupgeneric', action:'list')}" class="btn"
							style="float: right; margin-right: 5px;"> Cancel </a>
					</g:else>
					
					<g:if test="${userGroupInstance?.id}">
						<div class="btn btn-danger"
							style="float: right; margin-right: 5px;">
							<a
								href="${createLink(mapping:'userGroup', action:'delete', params:['webaddress':userGroupInstance.webaddress])}"
								onclick="return confirm('${message(code: 'default.userGroup.delete.confirm.message', default: 'This group and its content will be deleted. Are you sure ?')}');">Delete
								Group </a>
						</div>
					</g:if>
					 <a id="createGroupSubmit"
						class="btn btn-primary" style="float: right; margin-right: 5px;">
						${form_button_val} </a>
					<span class="policy-text"> By submitting this form for
						creating a new group you agree to our <a href="/terms">Terms
							and Conditions</a> on the use of our site </span>
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
			url:'${g.createLink(controller:'userGroup', action:'upload_resource')}',
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
		
	function getSelectedGroup() {
	    var grp = []; 
	    $('#speciesGroupFilter button').each (function() {
	            if($(this).hasClass('active')) {
	                    grp.push($(this).attr('value'));
	            }
	    });
	    return grp;	
	} 
	    
	function getSelectedHabitat() {
	    var hbt = []; 
	    $('#habitatFilter button').each (function() {
	            if($(this).hasClass('active')) {
	                    hbt.push($(this).attr('value'));
	            }
	    });
	    return hbt;	
	}
	
	$("#createGroupSubmit").click(function(){
		$('#founderUserIds').val(founders_autofillUsersComp[0].getEmailAndIdsList().join(","));
		//$('#memberUserIds').val(members_autofillUsersComp[0].getEmailAndIdsList().join(","));
		/*var tags = $("#tags").tagit("tags");
       	$.each(tags, function(index){
       		var input = $("<input>").attr("type", "hidden").attr("name", "tags."+index).val(this.label);
			$('#${form_id}').append($(input));	
       	})*/
        
        var speciesGroups = getSelectedGroup();
        var habitats = getSelectedHabitat();
        
       	$.each(speciesGroups, function(index){
       		var input = $("<input>").attr("type", "hidden").attr("name", "speciesGroup."+index).val(this);
			$('#${form_id}').append($(input));	
       	})
        
       	$.each(habitats, function(index){
       		var input = $("<input>").attr("type", "hidden").attr("name", "habitat."+index).val(this);
			$('#${form_id}').append($(input));	
       	})
       	
       	$('#homePage').val(getSelectedVal('home_page_label'));
		$('#theme').val(getSelectedVal('theme_label')); 
       	
        $("#${form_id}").submit();
        return false;
        
	});
	
	//$("#tags .tagit-input").watermark("Add some tags");	
	$("#tags").tagit({
		select:true, 
		allowSpaces:true, 
		placeholderText:'Add some tags',
		fieldName:'tags', 
		autocomplete:{
			source: "${uGroup.createLink(controller:'userGroup', action: 'tags')}"
		}, 
		triggerKeys:['enter', 'comma', 'tab'], 
		maxLength:30
	});
	$(".tagit-hiddenSelect").css('display','none');
	
	intializesSeciesHabitatInterest()
	
 	<%
 		userGroupInstance.speciesGroups.each {
			out << "jQuery('#group_${it.id}').addClass('active');";
		}
		userGroupInstance.habitats.each {
			out << "jQuery('#habitat_${it.id}').addClass('active');";
		}
	%>
 	
        $('.dropdown-toggle').dropdown()
});
</r:script>

</body>

</html>
