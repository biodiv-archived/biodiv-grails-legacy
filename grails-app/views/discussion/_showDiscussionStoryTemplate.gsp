<div class="observation_story" style="border: 1px solid rgb(106, 201, 162);">
    <div style="height: 175px;">
        <g:if test="${showFeatured}">

        <div class="featured_body">
        <div class="featured_title ellipsis"> 
            <div class="heading">
                <g:link url="${uGroup.createLink(controller:'discussion', action:'show', id:discussionInstance.id, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}">
                	<span class="ellipsis">${discussionInstance.subject}</span>
                </g:link>
            </div>
        </div>
        <g:render template="/common/featureNotesTemplate" model="['instance':discussionInstance, 'featuredNotes':featuredNotes]"/>
    </div>
        </g:if>
        <g:else>



        <%  def styleVar = 'block';
            def clickcontentVar = '' 
        %> 
        <g:if test="${discussionInstance?.language?.id != userLanguage?.id}">
            <%  
              styleVar = "none"
              clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+discussionInstance?.language?.threeLetterCode.toUpperCase()+'</a>';
            %>
        </g:if>
                


        <div class="prop">
            <span class="name"><g:message code="default.subject.label" /></span>
            <div class="notes_view linktext value">
                ${raw(clickcontentVar)}
                <div style="display:${styleVar}">
                <g:link url="${uGroup.createLink(controller:'discussion', action:'show', id:discussionInstance.id, 'pos':pos, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}">
               		${raw(discussionInstance?.subject)}
               	</g:link>	
                </div>    
            </div>
        </div>
       
        <div class="prop">
            <span class="name"><g:message code="default.message.label" /></span>
            <div class="notes_view linktext value">
                ${raw(clickcontentVar)}
                <div class="ellipsis multiline" style="display:${styleVar};">
                    ${raw(discussionInstance?.body)}
                </div>    
            </div>
        </div>
       
        <div class="prop">
        	<g:if test="${showDetails}">
             	<span class="name"><i class="icon-time"></i><g:message code="default.submitted.label" /></span>
             </g:if>
             <g:else>
                  <i class="pull-left icon-time"></i>
             </g:else>
             <div class="value">
             	<time class="timeago"
                      datetime="${discussionInstance.createdOn.getTime()}"></time>
             </div>
        </div>

        <div class="prop">
        	<g:if test="${showDetails}">
                <span class="name"><i class="icon-time"></i><g:message code="default.updated.label" /></span>
            </g:if>
            <g:else>
                <i class="pull-left icon-time"></i>
            </g:else>
                <div class="value">
                    <time class="timeago"
                    datetime="${discussionInstance.lastRevised?.getTime()}"></time>
                </div>
            </div>
<%--        <g:if test="${showDetails && discussionInstance?.tags}">--%>
        <div class="prop">
            <span class="name"><g:message code="default.tags.label" /></span>

            <div class="value">
                <g:render template="/project/showTagsList"
                model="['instance': discussionInstance, 'controller': 'discussion', 'action':'list']" />
            </div>
        </div>
<%--        </g:if>--%>
        
        <div class="row observation_footer" style="margin-left:0px;">
             <g:render template="/discussion/showDiscussionStoryFooterTemplate"
                model="['discussionInstance':discussionInstance, 'showDetails':showDetails, 'showLike':true]" />

            <div class="story-footer" style="right:3px;">
                <sUser:showUserTemplate
                model="['userInstance':discussionInstance.author, 'userGroup':userGroup]" />
            </div>
        </div>
              
     </g:else>       
    </div>
</div>

