<%@page import="species.utils.Utils"%>
<%@ page import="species.dataset.Datasource"%>
<%@ page import="species.Habitat"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'datasource.name.label')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
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
				def form_id = "createDatasource"
				def form_action = uGroup.createLink(controller:'datasource', action:'save')
				def form_button_name = "Create Datasource"
				def form_button_val = "${g.message(code:'button.create.datasource')}"
			    entityName="Create Datasource"	
				if(params.action == 'edit' || params.action == 'update'){
					//form_id = "updateGroup"
					form_action = uGroup.createLink(controller:'datasource', action:'update')
				 	form_button_name = "Update Datasource"
					form_button_val = "${g.message(code:'button.update.datasource')}"
					entityName = "Edit Datasource"
				}
				%>
		<div class="observation_create">
			<div class="span12">
				<uGroup:showSubmenuTemplate  model="['entityName':entityName]"/>
						

				<g:hasErrors bean="${datasourceInstance}">
					<i class="icon-warning-sign"></i>
					<span class="label label-important"> <g:message
							code="fix.errors.before.proceeding" default="Fix errors" /> </span>
				</g:hasErrors>
			
			
			<form id="${form_id}" action="${form_action}" method="POST"
				class="form-horizontal">
				<input type="hidden" name="id" value="${datasourceInstance?.id}"/>
				<div class="super-section">
					<div class="section"
						style="position: relative; overflow: visible;">
						<div
							class="row control-group left-indent ${hasErrors(bean: datasourceInstance, field: 'title', 'error')}">

							<label for="name" class="control-label"><g:message
									code="datasource.name.label" default="${g.message(code:'datasource.name.label')}" /> </label>
							<div class="controls textbox">
								<div id="groups_div" class="btn-group" style="z-index: 3;">
									<g:textField name="title" value="${datasourceInstance?.title}" placeholder="${g.message(code:'button.create.datasource')}" />
									<div class="help-inline">
										<g:hasErrors bean="${datasourceInstance}" field="title">
											<g:eachError bean="${datasourceInstance}" field="title">
    											<li><g:message error="${it}" /></li>
											</g:eachError>
										</g:hasErrors>
									</div>
								</div>
							</div>
						</div>

                        <div
							class="row control-group left-indent ${hasErrors(bean: datasourceInstance, field: 'website', 'error')}">

							<label for="website" class="control-label"><g:message
									code="datasource.website.label" default="${g.message(code:'datasource.website.label')}" /> </label>
							<div class="controls textbox">
								<div id="groups_div" class="btn-group" style="z-index: 3;">
									<g:textField name="website" value="${datasourceInstance?.website}" placeholder="${g.message(code:'button.create.website')}" />
									<div class="help-inline">
										<g:hasErrors bean="${datasourceInstance}" field="website">
											<g:eachError bean="${datasourceInstance}" field="website">
    											<li><g:message error="${it}" /></li>
											</g:eachError>
										</g:hasErrors>
									</div>
								</div>
							</div>
						</div>
						
												
						<div
                            class="row control-group left-indent ${hasErrors(bean: datasourceInstance, field: 'description', 'error')}">
								<label for="description" class="control-label"><g:message code="default.description.label" /></label>
							<div class="controls  textbox">
								
								<textarea id="description" name="description" placeholder="${g.message(code:'datasource.small.description')}">${datasourceInstance?.description}</textarea>
								
								<script type='text/javascript'>
                                    CKEDITOR.plugins.addExternal( 'confighelper', "${assetPath(src:'ckeditor/confighelper/plugin.js')}" );
									
									var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
									CKEDITOR.replace('description', config);
								</script>
								<div class="help-inline">
									<g:hasErrors bean="${datasourceInstance}" field="description">
										<g:eachError bean="${datasourceInstance}" field="description">
    											<li><g:message error="${it}" /></li>
										</g:eachError>
									</g:hasErrors>
								</div>
							</div>

						</div>

                        <div
							class="row control-group left-indent ${hasErrors(bean: datasourceInstance, field: 'icon', 'error')}">
							<label for="icon" class="control-label"><g:message
									code="datasource.icon.label" default="${g.message(code:'usergroup.icon.label')}" /> </label>
							<div class="controls">
								<div style="z-index: 3;">
										<div
											class="resources control-group ${hasErrors(bean: datasourceInstance, field: 'icon', 'error')}">

											<%def thumbnail = datasourceInstance.icon%>
											<div style="max-height:100px; width:auto;margin-left: 0px;">
												<a id="change_picture">
													<img id="thumbnail"
                                                    src='${createLink(url: datasourceInstance.mainImage().fileName)}' class='logo '/>
                                                    <div>
                                                        <i class="icon-picture"></i>
                                                        <g:message code="datasource.upload.icon.size" /> ${grailsApplication.config.speciesPortal.datasource.logo.MAX_IMAGE_SIZE/1024}KB
                                                    </div>
												</a>
												
											</div>
											<input id="icon" name="icon" type="hidden" value='${thumbnail}' />
											
											<div id="image-resources-msg" class="help-inline">
												<g:hasErrors bean="${datasourceInstance}" field="icon">
													<g:eachError bean="${datasourceInstance}" field="icon">
    													<li><g:message error="${it}" /></li>
													</g:eachError>
												</g:hasErrors>
											</div>
											
										</div>
									
								</div>
							</div>
						</div>
						
					
					</div>
				</div>

				<div class="" style="margin-top: 20px; margin-bottom: 40px;">
				
					<g:if test="${datasourceInstance?.id}">
						<a href="${createLink(mapping:'datasource', action:'show', id:datasourceInstance.id)}" class="btn"
							style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>
					</g:if>
					<g:else>
					<a href="${createLink(mapping:'userGroupgeneric', action:'list')}" class="btn"
							style="float: right; margin-right: 5px;"> <g:message code="button.cancel" /> </a>
					</g:else>
					
					<g:if test="${datasourceInstance?.id}">
						<div class="btn btn-danger"
							style="float: right; margin-right: 5px;">
							<a
								href="${createLink(mapping:'datasource', action:'delete', id:datasourceInstance?.id)}"
				        onclick="return confirm('${message(code: 'default.datasource.delete.confirm.message')}');"><g:message code="button.delete.datasource" /></a>
						</div>
					</g:if>
					 <a id="createDatasourceSubmit"
						class="btn btn-primary" style="float: right; margin-right: 5px;">
						${form_button_val} </a>
					<span class="policy-text"> <g:message code="datasource.create.submitting.for.new" /> <a href="/terms"><g:message code="link.terms.conditions" /></a> <g:message code="register.index.use.of.site" /> </span>
				</div>


			</form>
			<form class="upload_resource ${hasErrors(bean: datasourceInstance, field: 'icon', 'errors')}" enctype="multipart/form-data"
				title="${g.message(code:'ugroup.place.message')}" method="post">
				<input type="file" id="attachFile" name="resources" accept="image/*"/> 
				<span class="msg" style="float: right"></span> 
				<input type="hidden" name='dir' value="${datasourceDir}" />
			</form>
			
		</div>
	
</div>
	<asset:script>
$(document).ready(function() {


    	//hack: for fixing ie image upload
        if (navigator.appName.indexOf('Microsoft') != -1) {
            $('.upload_resource').css({'visibility':'visible'});
            //$('#change_picture').hide();
        } else {
            $('.upload_resource').css({'visibility':'hidden'});
            //$('#change_picture').show();
        }

        $('#change_picture').bind('click', function(){
            $('#attachFile').click();
            return false;
        });
        
        $('#attachFile').change(function(e){
                $('.upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
        });

     	$('.upload_resource').ajaxForm({ 
			url:'${g.createLink(controller:'datasource', action:'upload_resource')}',
			dataType: 'xml',//could not parse json wih this form plugin 
			clearForm: true,
			resetForm: true,
			type: 'POST',
			 
			beforeSubmit: function(formData, jqForm, options) {
                        	$("#createGroupSubmit").addClass('disabled');
				return true;
			}, 
                        xhr: function() {  // custom xhr
                            myXhr = $.ajaxSettings.xhr();
                            return myXhr;
                        },
			success: function(responseXML, statusText, xhr, form) {
                        	$("#createDatasourceSubmit").removeClass('disabled');
				$(form).find("span.msg").html("");
				var rootDir = '${grailsApplication.config.speciesPortal.datasource.serverURL}'
				var dir = $(responseXML).find('dir').text();
				var dirInput = $('.upload_resource input[name="dir"]');
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
			}, error:function (xhr, ajaxOptions, thrownError) {
                            //successHandler is used when ajax login succedes
                            var successHandler = this.success, errorHandler;
                            handleError(xhr, ajaxOptions, thrownError, successHandler, function(data) {
                                var response = $.parseJSON(xhr.responseText);
                                $("#addObservationSubmit").removeClass('disabled');

                                if(response.error){
                                    $("#image-resources-msg").parent(".resources").addClass("error");
                                    $("#image-resources-msg").html(response.error);
                                }

                                var messageNode = $(".message .resources");
                                if(messageNode.length == 0 ) {
                                    $(".upload_resource").prepend('<div class="message">'+(response?response.error:"Error")+'</div>');
                                } else {
                                    messageNode.append(response?response.error:"Error");
                                }
                            });
                    } 
     	});  
     	
     	$("#createDatasourceSubmit").click(function(){
        
       
        $("#${form_id}").submit();
        return false;
	});
});
</asset:script>

</body>

</html>
