<%@page import="java.util.Arrays"%>
<%@ page import="content.eml.Document"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'showusergroupsig.title.documents')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>

<r:require modules="add_file" />
<uploader:head />

<style>
.control-group.error  .help-inline {
	padding-top: 15px
}

input.dms_field {
	width: 19%;
	display: none;
}

.sidebar-section {
	width: 450px;
	margin: 0px 0px 20px -10px;
	float: right;
}

[class*="cke"] {
	max-width: 100%;
}
</style>
</head>
<body>


    <% 
    def form_action = uGroup.createLink(action:'save', controller:'document', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
    def form_title = "Create Document"
    def form_button_name = "Add Document"
    def form_button_val = "Add Document"
    if(params.action == 'edit' || params.action == 'update'){
    form_action = uGroup.createLink(action:'update', controller:'document', id:documentInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
    form_button_name = "Update Document"
    form_button_val = "Update Document"
    form_title = "Update Document"

    }

    String uploadDir = "documents/"+ "document-"+UUID.randomUUID().toString()	
    %>
    <div class="span12 observation_create">
        <g:render template="/document/documentSubMenuTemplate"
        model="['entityName': form_title]" />
        <uGroup:rightSidebar />



        <form id="documentForm" action="${form_action}" method="POST"
            onsubmit="document.getElementById('documentFormSubmit').disabled = 1;"
            class="form-horizontal">

            <div class="span12 super-section" style="margin-left: 0px;">
                <div class="section">

                    <div
                        class="control-group ${hasErrors(bean: documentInstance, field: 'type', 'error')}">
                        <label class="control-label" for="type"><g:message
                            code="document.type.label" default="Type" /><span class="req">*</span></label>
                        <div class="controls">
                            <g:select name="type" class="input-block-level"
                            placeholder="${g.message(code:'placeholder.document.select')}"
                            from="${content.eml.Document$DocumentType?.values()}"
                            keys="${content.eml.Document$DocumentType?.values()*.value()}"
                            value="${documentInstance?.type?.value()}" />

                        </div>

                    </div>
                    <div
                        class="control-group ${hasErrors(bean: documentInstance, field: 'title', 'error')}">
                        <label class="control-label" for="title"><g:message
                            code="document.title.label" default="Title" /><span class="req">*</span></label>
                        <div class="controls">

                            <input type="text" class="input-block-level" name="title"
                            placeholder="${g.message(code:'placeholder.document.enter.title')}"
                            value="${documentInstance?.title}" required />

                            <div class="help-inline">
                                <g:hasErrors bean="${documentInstance}" field="title">
                                <g:message code="default.blank.message" args="['Title']" />
                                </g:hasErrors>
                            </div>
                        </div>

                    </div>


                    <div
                        class="control-group ${hasErrors(bean: documentInstance, field: 'uFile', 'error')}">
                        <label class="control-label" for="file"><g:message code="default.resource.label" /> <span
                                class="req">*</span></label>
                        <div class="controls" style="">
                            <div class="span2" style="margin-left: 0px;">
                                <g:render template='/UFile/docUpload'
                                model="['name': 'ufilepath', 'path': documentInstance?.uFile?.path, 'size':documentInstance?.uFile?.size,'fileParams':['uploadDir':uploadDir]]" />

                            </div>
                            <div class="span1"><g:message code="loginformtemplate.or" /></div>
                            <div
                                class="span6 control-group ${hasErrors(bean: documentInstance, field: 'uri', 'error')}"
                                style="width: 480px;">
                                <label class="control-label" for="uri" style="width: 40px;"><g:message code="default.url.label" /></label>
                                <div class="controls" style="margin-left: 55px;">
                                    <input type="text" class="input-block-level" name="uri"
                                    placeholder="${g.message(code:'placeholder.document.enter.url')}"
                                    value="${documentInstance?.uri}" />
                                </div>
                            </div>
                            <div class="help-inline">
                                <g:hasErrors bean="${documentInstance}" field="uFile">
                                <g:message code="fileOrUrl.validator.invalid" />
                                </g:hasErrors>
                            </div>
                        </div>
                    </div>


                    <div
                        class="control-group ${hasErrors(bean: documentInstance, field: 'contributors', 'error')}">
                        <label class="control-label" for="contributors"><g:message code="default.contributors.label" /></label>
                        <div class="controls">
                            <g:textField name="contributors" class="input-block-level"
                            value="${documentInstance?.contributors }"
                            placeholder="${g.message(code:'placeholder.document.enter.contributors')}" />
                        </div>
                    </div>



                    <div
                        class="control-group ${hasErrors(bean: documentInstance, field: 'attribution', 'error')}">
                        <label class="control-label" for="attribution"><g:message code="default.attribution.label" /></label>
                        <div class="controls">
                            <g:textField name="attribution" class="input-block-level"
                            placeholder="${g.message(code:'placeholder.document.enter.attribution')}"
                            value="${documentInstance?.attribution}" />
                        </div>
                    </div>

                    <div
                        class="control-group ${hasErrors(bean: documentInstance, field: 'license', 'error')}">
                        <label class="control-label" for="License"><g:message code="default.licenses.label" /> </label>

                        <div class="controls">

                            <div id="licenseDiv" class="licence_div dropdown">

                                <a id="selected_license" class="btn dropdown-toggle btn-mini"
                                    data-toggle="dropdown"> <img
                                    src="${documentInstance.license?resource(dir:'images/license',file:documentInstance.license.name.getIconFilename()+'.png'):resource(dir:'images/license',file:'cc_by.png', absolute:true)}"
                                    title="${g.message(code:'title.set.license')}" /> <b class="caret"></b>
                                </a>

                                <ul id="license_options" class="dropdown-menu license_options">
                                    <span><g:message code="default.choose.license.label" /></span>
                                    <g:each in="${species.License.list()}" var="l">
                                    <li class="license_option"
                                    onclick="$('#license').val($.trim($(this).text()));$('#selected_license').find('img:first').replaceWith($(this).html());">
                                    <img
                                    src="${resource(dir:'images/license',file:l?.name?.getIconFilename()+'.png', absolute:true)}" /><span
                                        style="display: none;"> ${l?.name?.value}
                                    </span>
                                    </li>
                                    </g:each>
                                </ul>

                                <input id="license" type="hidden" name="licenseName"
                                value="${documentInstance.license?.name?.value()}"></input>

                            </div>
                        </div>
                    </div>

                    <hr>

                    <div
                        class="control-group ${hasErrors(bean: documentInstance, field: 'notes', 'error')}">
                        <label class="control-label" for="description"><g:message code="default.description.label" />
                        </label>
                        <div class="controls">

                            <textarea id="description" name="description"
                                placeholder="${g.message(code:'placeholder.document.write.description')}">
                                ${documentInstance?.notes}
                            </textarea>

                            <script type='text/javascript'>
                                CKEDITOR.plugins.addExternal( 'confighelper', '${request.contextPath}/js/ckeditor/plugins/confighelper/' );

                                var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
CKEDITOR.replace('description', config);
</script>
<div class="help-inline">
    <g:hasErrors bean="${userGroupInstance}" field="notes">
    <g:eachError bean="${userGroupInstance}" field="notes">
    <li><g:message error="${it}" /></li>
    </g:eachError>
    </g:hasErrors>
</div>

                                                </div>

                                            </div>
                                            <%
                                            def docTags = documentInstance?.tags
                                            if(params.action == 'save' && params.tags){
                                            docTags = Arrays.asList(params.tags)
                                            }				
                                            %>
                                            <div
                                                class="control-group ${hasErrors(bean: documentInstance, field: 'tags', 'error')}">
                                                <label class="control-label" for='tags'> <i
                                                        class="icon-tags"></i><g:message code="default.tags.label" />
                                                </label>
                                                <div class="controls">
                                                    <ul class='file-tags' id="tags" name="tags">
                                                        <g:if test='${documentInstance}'>
                                                        <g:each in="${docTags}" var="tag">
                                                        <li>
                                                        ${tag}
                                                        </li>
                                                        </g:each>
                                                        </g:if>
                                                    </ul>
                                                </div>
                                            </div>



                                        </div>
                                    </div>

                                    <g:render template="coverage"
                                    model="['coverageInstance':documentInstance, 'sourceInstance':documentInstance]"></g:render>



                                    <uGroup:isUserGroupMember>
                                    <div class="span12 super-section"
                                        style="clear: both; margin-left: 0px;">
                                        <div class="section" style="position: relative; overflow: visible;">
                                            <h3><g:message code="heading.post.user.groups" /></h3>
                                            <div>
                                                <%
                                                def docActionMarkerClass = (params.action == 'create' || params.action == 'save')? 'create' : '' 
                                                %>
                                                <div id="userGroups" class="${docActionMarkerClass}"
                                                    name="userGroups" style="list-style: none; clear: both;">
                                                    <uGroup:getCurrentUserUserGroups
                                                    model="['observationInstance':documentInstance]" />
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    </uGroup:isUserGroupMember>



                                    <div class="span12 submitButtons">

                                        <g:if test="${documentInstance?.id}">
                                        <a
                                            href="${uGroup.createLink(controller:'document', action:'show', id:documentInstance.id)}"
                                            class="btn" style="float: right; margin-right: 30px;"><g:message code="button.cancel" /> 
                                        </a>
                                        </g:if>
                                        <g:else>
                                        <a
                                            href="${uGroup.createLink(controller:'document', action:'browser')}"
                                            class="btn" style="float: right; margin-right: 30px;"><g:message code="button.cancel" /> 
                                        </a>
                                        </g:else>

                                        <a id="documentFormSubmit" class="btn btn-primary"
                                            style="float: right; margin-right: 5px;"> ${form_button_name}
                                        </a>
                                        <div class="control-group" style="clear: none;">
                                            <label class="checkbox" style="text-align: left;"> <g:checkBox
                                                style="margin-left:0px;" name="agreeTerms"
                                                value="${documentInstance.agreeTerms}" /> <span
                                                    class="policy-text"> <g:message code="default.submiting.form.label" /> </span></label>
                                        </div>
                                    </div>

                                </form>
                            </div>

	<r:script>
	
	$(document).ready(function() {
    
        $('.use_dms').click(function(){
            var map_class = $(this).closest(".map_class");
            if ($(this).is(':checked')) {
                $(map_class).find('.dms_field').fadeIn();
                $(map_class).find('.degree_field').hide();
            } else {
                $(map_class).find('.dms_field').hide();
                $(map_class).find('.degree_field').fadeIn();
            }

        });
    
    			$("#documentFormSubmit").click(function(){
    			
    				//Disable click on the button. 
    				$("#documentFormSubmit").unbind("click");
    			
    				if (document.getElementById('agreeTerms').checked){
    			
						var speciesGroups = getSelectedGroup();
		        		var habitats = getSelectedHabitat();
		        
		        		
		       			$.each(speciesGroups, function(index){
		       				var input = $("<input>").attr("type", "hidden").attr("name", "speciesGroup."+index).val(this);
							$('#documentForm').append($(input));	
		       			})
		        
		       			$.each(habitats, function(index){
		       				var input = $("<input>").attr("type", "hidden").attr("name", "habitat."+index).val(this);
							$('#documentForm').append($(input));	
		       			})
		       			$(".userGroupsList").val(getSelectedUserGroups());	
                                        
                                        var locationpicker = $(".map_class").data('locationpicker'); 
                                        if(locationpicker && locationpicker.mapLocationPicker.drawnItems) { 
                                        //if(drawnItems) {
                                          /*var areaBounds = new Array();
                                            locationpicker.mapLocationPicker.drawnItems.eachLayer(function(layer){
                                                if(layer.constructor === L.MultiPolygon) {
                                                    for(var l in layer._layers) {
                                                        areaBounds.push(layer._layers[l].getLatLngs());    
                                                    }
                                                } else
                                                    areaBounds.push(layer.getLatLngs());    
                                            })
                                            var areas = new L.MultiPolygon(areaBounds);
                                            */
                                            var areas = locationpicker.mapLocationPicker.drawnItems.getLayers();
                                            if(areas.length > 0) {
                                                var wkt = new Wkt.Wkt();
                                                wkt.fromObject(areas[0]);
                                                $("input.areas").val(wkt.write());
                                            }
                                        }

                                        
				        $("#documentForm").submit();
			    	    return false;
					} else {
						alert("Please agree to the terms mentioned at the end of the form to submit the document.")
					}       	
		       	
	
			});
			
			
		$('input:radio[name=groupsWithSharingNotAllowed]').click(function() {
		    var previousValue = $(this).attr('previousValue');
    
    		if(previousValue == 'true'){
        		$(this).attr('checked', false)
    		}
    
    		$(this).attr('previousValue', $(this).attr('checked'));
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
			<%
				documentInstance?.speciesGroups.each {
					out << "jQuery('#group_${it.id}').addClass('active');";
				}
				documentInstance?.habitats.each {
					out << "jQuery('#habitat_${it.id}').addClass('active');";
				}
			%>
			
			
	
             $("#tags").tagit({
        	select:true, 
        	allowSpaces:true, 
        	placeholderText:'${g.message(code:"placeholder.add.tags")}',
        	fieldName: 'tags', 
        	autocomplete:{
        		source: '/document/tags'
        	}, 
        	triggerKeys:['enter', 'comma', 'tab'], 
        	maxLength:30
        });
		$(".tagit-hiddenSelect").css('display','none');
			
			
			
						});
						

		
    
        </r:script>
</body>
</html>
