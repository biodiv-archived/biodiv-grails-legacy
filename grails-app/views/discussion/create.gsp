<%@page import="java.util.Arrays"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'showusergroupsig.title.discussions')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>

<r:require modules="add_file" />

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
    def form_action = uGroup.createLink(action:'save', controller:'discussion', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
    def form_title = "${g.message(code:'title.create.discussion')}"
    def form_button_name = "${g.message(code:'title.discussion.add')}"
    def form_button_val = "Add Discussion"
    if(params.action == 'edit' || params.action == 'update'){
    form_action = uGroup.createLink(action:'update', controller:'discussion', id:discussionInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
    form_button_name = "${g.message(code:'link.update.discussion')}"
    form_button_val = "Update Discussion"
    form_title = "Update Discussion"

    }
    %>
    <div class="span12 observation_create">
        <g:render template="/discussion/discussionSubMenuTemplate"
        model="['entityName': form_title]" />
        <uGroup:rightSidebar />


        <form id="documentForm" action="${form_action}" method="POST"
            onsubmit="document.getElementById('documentFormSubmit').disabled = 1;"
            class="form-horizontal">

            <div class="span12 super-section" style="margin-left: 0px;">
                <div class="section">
                    <div
                        class="control-group ${hasErrors(bean: discussionInstance, field: 'subject', 'error')}">
                        <label class="control-label" for="Subject"><g:message
                            code="discussion.title.label" default="${g.message(code:'default.subject.label')}" /><span class="req">*</span></label>
                        <div class="controls">

                            <input type="text" class="input-block-level" name="subject"
                            placeholder="${g.message(code:'placeholder.discussion.enter.title')}"
                            value="${discussionInstance?.subject}" required />

                            <div class="help-inline">
                                <g:hasErrors bean="${discussionInstance}" field="title">
                                <g:message code="default.blank.message" args="['Title']" />
                                </g:hasErrors>
                            </div>
                        </div>

                    </div>
                    <input type="hidden" name="plainText" id="plainText" value="${discussionInstance?.plainText}" />
                     <div
                        class="control-group ${hasErrors(bean: discussionInstance, field: 'body', 'error')}">
                        <label class="control-label" for="body"><g:message code="default.message.label" /><span class="req">*</span>
                        </label>
                        <div class="controls">

                            <textarea id="description" name="body"
                                placeholder="${g.message(code:'placeholder.discussion.write.description')}">
                                ${discussionInstance?.body}
                            </textarea>

                            <script type='text/javascript'>
                                CKEDITOR.plugins.addExternal( 'confighelper', '${request.contextPath}/js/ckeditor/plugins/confighelper/' );

                                var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
CKEDITOR.replace('description', config);
</script>
<div class="help-inline">
    <g:hasErrors bean="${userGroupInstance}" field="body">
    <g:eachError bean="${userGroupInstance}" field="body">
    <li><g:message error="${it}" /></li>
    </g:eachError>
    </g:hasErrors>
</div>

                                                </div>

                                            </div>
                                            <%
                                            def docTags = discussionInstance?.tags
                                            if(params.action == 'save' && params.tags){
                                            docTags = Arrays.asList(params.tags)
                                            }				
                                            %>
                                            <div
                                                class="control-group ${hasErrors(bean: discussionInstance, field: 'tags', 'error')}">
                                                <label class="control-label" for='tags'> <i
                                                        class="icon-tags"></i><g:message code="default.tags.label" />
                                                </label>
                                                <div class="controls">
                                                    <ul class='file-tags' id="tags" name="tags">
                                                        <g:if test='${discussionInstance}'>
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
                                                    model="['observationInstance':discussionInstance]" />
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    </uGroup:isUserGroupMember>



                                    <div class="span12 submitButtons">

                                        <g:if test="${discussionInstance?.id}">
                                        <a
                                            href="${uGroup.createLink(controller:'discussion', action:'show', id:discussionInstance.id)}"
                                            class="btn" style="float: right; margin-right: 30px;"><g:message code="button.cancel" /> 
                                        </a>
                                        </g:if>
                                        <g:else>
                                        <a
                                            href="${uGroup.createLink(controller:'discussion', action:'list')}"
                                            class="btn" style="float: right; margin-right: 30px;"><g:message code="button.cancel" /> 
                                        </a>
                                        </g:else>

                                        <a id="documentFormSubmit" class="btn btn-primary"
                                            style="float: right; margin-right: 5px;"> ${form_button_name}
                                        </a>
                                        <div class="control-group" style="clear: none;">
                                            <label class="checkbox" style="text-align: left;"> <g:checkBox
                                                style="margin-left:0px;" name="agreeTerms"
                                                value="${discussionInstance.agreeTerms}" /> <span
                                                    class="policy-text"> <g:message code="default.submiting.form.label" /> </span></label>
                                        </div>
                                    </div>

                                </form>
                            </div>
<%def alert_msg=g.message(code:'discussion.error.message')%>
	<r:script>
	
	$(document).ready(function() {
    
           
    			$("#documentFormSubmit").click(function(){
    			
    				//Disable click on the button. 
    				$("#documentFormSubmit").unbind("click");
    				
    				if (document.getElementById('agreeTerms').checked){
    			
		       			$(".userGroupsList").val(getSelectedUserGroups());
		       			var plainText = CKEDITOR.instances.description.document.getBody().getText();
    					$("#plainText").val(plainText);	
				        $("#documentForm").submit();
			    	    return false;
					} else {
                        var error_msg="${alert_msg}";
						alert(error_msg)
					}       	
		       	
	
			});
			
			
		$('input:radio[name=groupsWithSharingNotAllowed]').click(function() {
		    var previousValue = $(this).attr('previousValue');
    
    		if(previousValue == 'true'){
        		$(this).attr('checked', false)
    		}
    
    		$(this).attr('previousValue', $(this).attr('checked'));
		});
		
	
             $("#tags").tagit({
        	select:true, 
        	allowSpaces:true, 
        	placeholderText:'${g.message(code:"placeholder.add.tags")}',
        	fieldName: 'tags', 
        	autocomplete:{
        		source: '/discussion/tags'
        	}, 
        	triggerKeys:['enter', 'comma', 'tab'], 
        	maxLength:30
        });
		$(".tagit-hiddenSelect").css('display','none');
			
			
			
						});
						

		
    
        </r:script>
</body>
</html>
