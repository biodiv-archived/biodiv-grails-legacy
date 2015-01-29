<%@page import="species.utils.Utils"%>
<%@page import="species.participation.ActivityFeedService"%>
<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'discussion', action:'show', id:discussionInstance.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${discussionInstance.subject}"/>
<g:set var="description" value="${Utils.stripHTML(discussionInstance.body?:'') }" />
<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':null]"/>
<r:require modules="content_view, activityfeed, comment" />
</head>
<body>
    <div class="span12">
        <g:if test="${discussionInstance}">
        <g:set var="featureCount" value="${discussionInstance.featureCount}"/>
        </g:if>
        <div class="page-header clearfix" style="position:relative">
            <span class="badge ${(featureCount>0) ? 'featured':''}" style="left:-50px;"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}">
            </span>

			<div style="width: 100%;">
				<div class="main_heading" style="margin-left: 0px;">
					<sUser:ifOwns model="['user':discussionInstance.author]">
                                            <div class="pull-right">
                                                <a class="btn btn-success pull-right"
                                                        href="${uGroup.createLink(
                                                                controller:'discussion', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                                        class="btn btn-info" title="${g.message(code:'title.discussion.add')}">
                                                        <i class="icon-plus"></i><g:message code="link.add.discussion" />  
                                                </a>

						<a class="btn btn-primary pull-right" title="${g.message(code:'title.discussion.edit')}" style="margin-right: 5px;"
							href="${uGroup.createLink(controller:'discussion', action:'edit', id:discussionInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
							<i class="icon-edit"></i><g:message code="button.edit" /> 
						</a>
						
						<a class="btn btn-danger pull-right"  href="#" title="${g.message(code:'title.discussion.delete')}" style="margin-right: 5px;"

							onclick="deleteDocument(); return false;">
							<i class="icon-trash"></i> <g:message code="button.delete" />
						</a>
							
						<form action="${uGroup.createLink(controller:'discussion', action:'delete')}" method='POST' name='deleteForm'>
							<input type="hidden" name="id" value="${discussionInstance.id}" />
						</form>
                        <%
                        def y="${g.message(code:'info.discussion.delete')}"
                        %>
						<r:script>
						function deleteDocument(){
                            var test="${y}";
                            
							if(confirm(test)){
								document.forms.deleteForm.submit();
							}
						}
						</r:script>
	                                        </div>					
				</sUser:ifOwns>
				<s:showHeadingAndSubHeading
						model="['subHeading':discussionInstance.subject, 'subHeadingClass':subHeadingClass]" />


                            </div>

			</div>
		</div>

                <div class="span12" style="margin-left:0px">
                    <g:render template="/common/observation/showObservationStoryActionsTemplate"
                    model="['instance':discussionInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':false, 'hideDownload':true, 'hideFollow':false]" />
                </div>



                <div class="span8 right-shadow-box" style="margin:0;">
                    <g:render template="/discussion/showDiscussionStoryTemplate" model="['discussionInstance':discussionInstance, showDetails:true,'userLanguage':userLanguage]"/>

			<div class="union-comment">
				<feed:showAllActivityFeeds model="['rootHolder':discussionInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable', 'feedClass':ActivityFeedService.COMMENT_ADDED]" />
				<comment:showAllComments model="['commentHolder':discussionInstance, commentType:'super','showCommentList':false]" />
			</div>
			
			<uGroup:objectPostToGroupsWrapper 
            	model="['objectType':discussionInstance.class.canonicalName, 'observationInstance':discussionInstance]" />
			
		</div>
                <g:render template="/discussion/discussionSidebar" model="['discussionInstance':discussionInstance]"/>

	</div>
        </body>
</html>
