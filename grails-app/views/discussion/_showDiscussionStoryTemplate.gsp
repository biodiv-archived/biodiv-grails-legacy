<div class="observation_story observation" style="border: 1px solid rgb(106, 201, 162);">
    <div style="height:${params.action == 'show'?'inherit':'160px'};">
        <g:if test="${showFeatured}">

        <div class="featured_body">
        <div class="featured_title ellipsis"> 
            <div class="heading">
                <g:link url="${uGroup.createLink(controller:'discussion', action:'show', id:discussionInstance.id, 'pos':pos, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}">
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
            <span class="name"><i class="icon-align-justify"></i><g:message code="default.subject.label" /></span>
            <div class="notes_view linktext value">
                ${raw(clickcontentVar)}
                <div style="display:${styleVar}">
                <g:link url="${uGroup.createLink(controller:'discussion', action:'show', id:discussionInstance.id, 'pos':pos, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress) }" name="l${pos}">
               		<b>${raw(discussionInstance?.subject)}</b>
               	</g:link>	
                </div>    
            </div>
        </div>
       
       <%
			def message = raw(discussionInstance.body)
			if(params.action != 'show'){
				message = discussionInstance.plainText.substring(0, Math.min(discussionInstance.plainText.length(), 140)) + "..."
			}
		%>
       
        <div class="prop">
            <span class="name"><i class="icon-align-justify"></i><g:message code="default.message.label" /></span>
            <div class="notes_view linktext value">
                ${raw(clickcontentVar)}
                <div class="${params.action == 'show'?'':''}" style="display:${styleVar};">
                    ${message}
                </div>    
            </div>
        </div>

	  	<g:if test="${showDetails}">
        <div class="prop">
             <span class="name"><i class="icon-time"></i><g:message code="default.started.label" /></span>
             <div class="value">
             	<time class="timeago"
                      datetime="${discussionInstance.createdOn.getTime()}"></time>
             </div>
        </div>
        </g:if>
    
        <div class="prop">
                <span class="name"><i class="icon-time"></i><g:message code="default.updated.label" /></span>
                <div class="value">
                    <time class="timeago"
                    datetime="${discussionInstance.lastRevised?.getTime()}"></time>
                </div>
            </div>
        <g:if test="${discussionInstance.tags}">
        <div class="prop">
            <span class="name"><i class="icon-tags"></i><g:message code="default.tags.label" /></span>
            <div class="value">
                <g:render template="/project/showTagsList"
                model="['instance': discussionInstance, 'controller': 'discussion', 'action':'list']" />
            </div>
        </div>
        </g:if>
        
        <div class="row observation_footer" style="margin-left:0px;">
             <g:render template="/discussion/showDiscussionStoryFooterTemplate"
                model="['discussionInstance':discussionInstance, 'showDetails':true, 'showLike':true]" />

            <div class="story-footer" style="right:3px;">
                <sUser:showUserTemplate
                model="['userInstance':discussionInstance.author, 'userGroup':userGroup]" />
            </div>
        </div>
              
     </g:else>       
    </div>
</div>

