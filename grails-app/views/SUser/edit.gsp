<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<html>
<%@ page import="org.codehaus.groovy.grails.plugins.PluginManagerHolder"%>

<sec:ifNotSwitched>
	<sec:ifAllGranted roles='ROLE_SWITCH_USER'>
		<g:if test='${user.username}'>
			<g:set var='canRunAs' value='${true}' />
		</g:if>
	</sec:ifAllGranted>
</sec:ifNotSwitched>

<head>
<g:set var="title" value="User"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="observations_show" />
<style>
.form-horizontal .control-label {
	width: 90px;
}

.form-horizontal .controls {
	margin-left: 110px;
}
.super-section  {
    background-color:white;
}


</style>
</head>

<body>
	
			<div class="span12">
				<div class="page-header clearfix">
					<div style="width: 100%;">
						<div class="span8 main_heading" style="margin-left: 0px;">
							<h1>
								${fieldValue(bean: user, field: "name")}
							</h1>
						</div>
		
						<div style="float: right; margin: 10px 0;">
							<a class="btn btn-info pull-right"
								href="${uGroup.createLink(action:'show', controller:"SUser", id:user.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">View
								my profile </a>
						</div>
					</div>
				</div>
				<div style="clear: both;"></div>


				<g:hasErrors bean="${user}">
					<i class="icon-warning-sign"></i>
					<span class="label label-important"> <g:message
							code="fix.errors.before.proceeding" default="Fix errors" /> </span>
					<%--<g:renderErrors bean="${user}" as="list" />--%>
				</g:hasErrors>

				<form class="form-horizontal" action="${uGroup.createLink(action:'update', controller:'SUser', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" id='userEditForm' method="POST">
					<g:hiddenField name="id" value="${user?.id}" />
					<g:hiddenField name="version" value="${user?.version}" />

					<div style="clear: both;">
						<div class="row section">
							<div class="figure span3 ${hasErrors(bean: user, field: 'icon', 'error')}" style="float: left; max-height: 220px; max-width: 200px">
								
								<%def thumbnail = user.icon%>
								<div class='pull-left' style="height:100px; width:auto;margin-left: 0px;">
									<a onclick="$('#attachFile').select()[0].click();return false;" style="postiion:relative;">
										<img id="thumbnail"
										src='${createLink(url: user.mainImage().fileName)}' class='logo '/>
										<div style="clear:both;">
											<i class="icon-picture"></i>
											<span>Upload picture of size < 2MB</span>
										</div>
									</a>
								</div>
								
								<input id="icon" name="icon" type="hidden" value='${thumbnail}' />
									
											<div id="image-resources-msg" class="help-inline">
												<g:hasErrors bean="${user}" field="icon">
													<g:eachError bean="${user}" field="icon">
    													<li><g:message error="${it}" /></li>
													</g:eachError>
												</g:hasErrors>
											</div>
								
							</div>
                                                        <div class="observation_story" style="width:660px;float:left;">

								<div
									class="control-group ${hasErrors(bean: user, field: 'username', 'error')}">
									<label class="control-label" for="username"><i
										class="icon-user"></i> <g:message code="suser.username.label"
											default="Username" /> </label>
									<div class="controls">
										<input type="text" name="username" class="input-xlarge"
											id="username" value="${user.username}">
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="username">
												<g:renderErrors bean="${user}" as="list" field="username" />
											</g:hasErrors>
										</div>
									</div>
								</div>

								<div
									class="control-group ${hasErrors(bean: user, field: 'name', 'error')}">
									<label class="control-label" for="name"><i
										class="icon-user"></i> <g:message code="suser.name.label"
											default="Full Name" /> </label>
									<div class="controls">
										<input type="text" name="name" class="input-xlarge" id="name"
											value="${user.name}">
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="name">
												<g:renderErrors bean="${user}" as="list" field="name" />
											</g:hasErrors>
										</div>
									</div>
								</div>

								<div
									class="control-group ${hasErrors(bean: user, field: 'email', 'error')}">
									<label class="control-label" for="email"><i
										class="icon-envelope"></i> <g:message code="suser.email.label"
											default="Email *" /> </label>
									<div class="controls">
										<input type="text" name="email" class="input-xlarge disabled"
											id="email" value="${user.email}" disabled readonly>
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="email">
												<g:renderErrors bean="${user}" as="list" field="email" />
											</g:hasErrors>
										</div>
									</div>
								</div>

								<div
									class="control-group ${hasErrors(bean: user, field: 'website', 'error')}">
									<label class="control-label" for="website"><i
										class="icon-road"></i> <g:message code="suser.website.label"
											default="Website" /> </label>
									<div class="controls">
										<input type="text" name="website" class="input-xlarge"
											id="website" value="${user.website ?: null}"  placeholder="Provide a comma separated list of urls">
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="website">
												<g:renderErrors bean="${user}" as="list" field="website" />
											</g:hasErrors>
										</div>
									</div>
								</div>

								<div
									class="control-group ${hasErrors(bean: user, field: 'location', 'error')}">
									<label class="control-label" for="location"><i
										class="icon-map-marker"></i> <g:message
											code="suser.location.label" default="Location" /> </label>
									<div class="controls">
										<input type="text" name="location" class="input-xlarge"
											id="location" value="${user.location}" />
										<div class="help-inline">
											<g:hasErrors bean="${user}" field="location">
												<g:renderErrors bean="${user}" as="list" field="location" />
											</g:hasErrors>
										</div>
									</div>
								</div>

							</div>
						</div>


						<div
							class="super-section control-group  ${hasErrors(bean: user, field: 'aboutMe', 'error')}"
							style="clear: both;">
							<h5>
								<i class="icon-user"></i>About Me
							</h5>
							<textarea cols='70' rows='3' style="width: 99%" name="aboutMe"
								id="aboutMe">
								${user.aboutMe }
							</textarea>
							<div class="help-inline">
								<g:hasErrors bean="${user}" field="aboutMe">
									<g:renderErrors bean="${user}" as="list" field="aboutMe" />
								</g:hasErrors>
							</div>
						</div>
						
						<div class="super-section"
								style="position: relative; overflow: visible;">
								<h5>
									<i class="icon-screenshot"></i>Interests
								</h5>
								
								<div class="row control-group left-indent">
									
										<label class="control-label">Species Groups & Habitats
										</label>
									
									<div class="filters controls textbox" style="position: relative;">
										<obv:showGroupFilter
											model="['observationInstance':observationInstance, 'hideAdvSearchBar':true]" />
									</div>
								</div>						
						</div>
						
						<div class="super-section" style="clear: both;">
							<h5>
								<i class="icon-cog"></i>Settings
							</h5>
							<div
								class="control-group ${hasErrors(bean: user, field: 'sendNotification', 'error')}">
								<div class="controls" style="margin-left: 0px;">
									<label class="checkbox" style="clear: both;"> <g:checkBox
											name="sendNotification" value="${user.sendNotification}" />
										<g:message code='user.sendNotification.label'
											default='Send me email notifications' /> </label>
									<div class="help-inline">
										<g:hasErrors bean="${user}" field="sendNotification">
											<g:renderErrors bean="${user}" as="list"
												field="sendNotification" />
										</g:hasErrors>
									</div>
								</div>
							</div>
							<div
								class="control-group ${hasErrors(bean: user, field: 'allowIdentifactionMail', 'error')}">
								<div class="controls" style="margin-left: 0px;">
									<label class="checkbox" style="clear: both;"> <g:checkBox
											name="allowIdentifactionMail"
											value="${user.allowIdentifactionMail}" /> <g:message
											code='user.allowIdentifactionMail.label'
											default='Allow identification email' /> </label>
									<div class="help-inline">
										<g:hasErrors bean="${user}" field="sendNotification">
											<g:renderErrors bean="${user}" as="list"
												field="sendNotification" />
										</g:hasErrors>
									</div>
								</div>
							</div>
							<div
								class="control-group ${hasErrors(bean: user, field: 'hideEmailId', 'error')}">
								<div class="controls" style="margin-left: 0px;">
									<label class="checkbox" style="clear: both;"> <g:checkBox
											name="hideEmailId" value="${user.hideEmailId}" /> <g:message
											code='user.hideEmailId.label'
											default='Hide my email from others' /> </label>
									<div class="help-inline">
										<g:hasErrors bean="${user}" field="hideEmailId">
											<g:renderErrors bean="${user}" as="list" field="hideEmailId" />
										</g:hasErrors>
									</div>
								</div>
                                                        </div>
                                                        <div
								class="control-group ${hasErrors(bean: user, field: 'sendDigest', 'error')}">
								<div class="controls" style="margin-left: 0px;">
									<label class="checkbox" style="clear: both;"> <g:checkBox
											name="sendDigest" value="${user.sendDigest}" />
										<g:message code='user.sendDigest.label'
											default='Send me digest email' /> </label>
									<div class="help-inline">
										<g:hasErrors bean="${user}" field="sendDigest">
											<g:renderErrors bean="${user}" as="list"
												field="sendDigest" />
										</g:hasErrors>
									</div>
								</div>
							</div>
						</div>
						<div class="super-section" style="clear: both;">
							<h5>
								<i class="icon-cog"></i>Actions
							</h5>
                                                        <ul>
                                                            <li><a href="${uGroup.createLink(controller:'SUser', action:'resetPassword', id:user.id) }">Change Password</a></li>
                                                        </ul>

                                                </div>

						<sUser:isAdmin model="['user':user]">
							<div class="super-section" style="clear: both;">
								<h5>
									<i class="icon-cog"></i>
									<g:message code="default.edit.label" args="[title]" />
								</h5>
								<%
	def tabData = []
	tabData << [name: 'userinfo', icon: 'icon_user', messageCode: 'spring.security.ui.user.info']
	tabData << [name: 'roles',    icon: 'icon_role', messageCode: 'spring.security.ui.user.roles']
	tabData << [name: 'speciesPermissions',    icon: 'icon_leaf', messageCode: 'species.permission.label']
	%>

								<s2ui:tabs elementId='tabs' height='375' data="${tabData}">

									<s2ui:tab name='userinfo' height='275'>
										<table>
											<tbody>

												<s2ui:checkboxRow name='enabled'
													labelCode='user.enabled.label' bean="${user}"
													labelCodeDefault='Enabled' value="${user?.enabled}" />

												<s2ui:checkboxRow name='accountExpired'
													labelCode='user.accountExpired.label' bean="${user}"
													labelCodeDefault='Account Expired'
													value="${user?.accountExpired}" />

												<s2ui:checkboxRow name='accountLocked'
													labelCode='user.accountLocked.label' bean="${user}"
													labelCodeDefault='Account Locked'
													value="${user?.accountLocked}" />

												<s2ui:checkboxRow name='passwordExpired'
													labelCode='user.passwordExpired.label' bean="${user}"
													labelCodeDefault='Password Expired'
													value="${user?.passwordExpired}" />
											</tbody>
										</table>
									</s2ui:tab>

									<s2ui:tab name='roles' height='275'>
										<g:each var="entry" in="${roleMap}">
											<div>
												<g:checkBox name="${entry.key.authority}"
													value="${entry.value}" />
												<g:link controller='role' action='edit' id='${entry.key.id}'>
													${entry.key.authority.encodeAsHTML()}
												</g:link>
											</div>
										</g:each>
									</s2ui:tab>



									<s2ui:tab name='speciesPermissions' height='275'>
										<table>
                                            <tbody>
                                                <g:each in="${contributorForTaxonConcepts}" var="${taxonConcept}">
                                                <tr>
                                                    <td>${TaxonomyRank.list()[taxonConcept.rank].value()} : ${taxonConcept.italicisedForm}</td>
                                                </tr>
                                                </g:each>
											</tbody>
										</table>
									</s2ui:tab>


								</s2ui:tabs>
							</div>
						</sUser:isAdmin>
						</div>
						
						<div class="section form-action"
							style='clear: both; margin-top: 20px; margin-bottom: 40px;'>
							 <a id="userEditFormSubmit"
								class="btn btn-primary" style="float: right; margin-right: 5px;">
								Update
							 </a>
<%--							<s2ui:submitButton elementId='update' form='userEditForm'--%>
<%--								messageCode='default.button.update.label'--%>
<%--								class="btn btn-primary" style="float: right; margin-right: 5px;" />--%>

							<sUser:isAdmin model="['user':user]">
								<g:if test='${user}'>
									<a class="btn btn-danger" id="deleteButton"> ${message(code:'default.button.delete.label')}
									</a>
								</g:if>
							</sUser:isAdmin>

							<g:if test='${canRunAs}'>
								<a id="runAsButton"> ${message(code:'spring.security.ui.runas.submit')}
								</a>
							</g:if>

						</div>
				</form>
				
				<sUser:isAdmin model="['user':user]">
					<g:if test='${user}'>
						<form action="${uGroup.createLink(controller:'SUser', action:'delete')}" method='POST' name='deleteForm'>
							<input type="hidden" name="id" value="${user.id}" />
						</form>
						<div id="deleteConfirmDialog" title="Are you sure?"></div>

						<r:script>
							$(document).ready(function() {
								$("#deleteButton").button().bind('click', function() {
									$('#deleteConfirmDialog').dialog('open');
								});
				
								$("#deleteConfirmDialog").dialog({
									autoOpen: false,
									resizable: false,
									height: 100,
									modal: true,
									buttons: {
										'Delete': function() {
											document.forms.deleteForm.submit();
										},
										Cancel: function() {
											$(this).dialog('close');
										}
									}
								});
							});
						</r:script>
					</g:if>
				</sUser:isAdmin>

				<g:if test='${canRunAs}'>
					<form name='runAsForm'
						action='${request.contextPath}/j_spring_security_switch_user'
						method='POST'>
						<g:hiddenField name='j_username' value="${user.username}" />
						<input type='submit' class='s2ui_hidden_button' />
					</form>
				</g:if>
				
				
				<form id="upload_resource" enctype="multipart/form-data"
					title="Upload profile picture" method="post"
					class="${hasErrors(bean: user, field: 'profilePic', 'errors')}">
					<input type="file" id="attachFile" name="resources" accept="image/*"/> 
					<span class="msg" style="float: right"></span> 
					<input type="hidden" name='dir' value="${userGroupDir}" />
				</form>
	
			</div>
	



	<r:script>
		$(document).ready(function() {
			$('#username').focus();

			$("#runAsButton").button();
			$('#runAsButton').bind('click', function() {
				document.forms.runAsForm.submit();
			});
			
			
			$("#userEditFormSubmit").click(function(){
				var speciesGroups = getSelectedGroup();
		        var habitats = getSelectedHabitat();
		        
		       	$.each(speciesGroups, function(index){
		       		var input = $("<input>").attr("type", "hidden").attr("name", "speciesGroup."+index).val(this);
					$('#userEditForm').append($(input));	
		       	})
		        
		       	$.each(habitats, function(index){
		       		var input = $("<input>").attr("type", "hidden").attr("name", "habitat."+index).val(this);
					$('#userEditForm').append($(input));	
		       	})
		       	
		        $("#userEditForm").submit();
		        return false;
			});
			
			
			
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
			intializesSpeciesHabitatInterest()
			<%
				user.speciesGroups.each {
					out << "jQuery('#group_${it.id}').addClass('active');";
				}
				user.habitats.each {
					out << "jQuery('#habitat_${it.id}').addClass('active');";
				}
			%>
			
		//hack: for fixing ie image upload
        if (navigator.appName.indexOf('Microsoft') != -1) {
            $('#upload_resource').css({'visibility':'visible'});
        } else {
            $('#upload_resource').css({'visibility':'hidden'});
        }
		
		$('#attachFile').change(function(e){
  			$('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
		});

     	$('#upload_resource').ajaxForm({ 
			url:'${g.createLink(controller:'SUser', action:'upload_resource')}',
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
				var rootDir = '${grailsApplication.config.speciesPortal.users.serverURL}'
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
		});
	</r:script>

</body>
</html>
