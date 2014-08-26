<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.groups.UserGroup"%>
<div class="story-actions clearfix" style="width: 100%;">
    <div class="span8" style="margin-left:0px;position:relative">
        <div class="footer-item pull-left">
            <obv:like model="['resource':instance]"/>
        </div>

        <g:if test="${!hideShare}">
		<div class="footer-item">
			<obv:identificationByEmail
                        model="['source':params.controller+params.action.capitalize(), 'requestObject':request, 'cssClass':'btn btn-mini', title:'Share']" />
		</div>
        </g:if>

        <g:if test="${!hideFlag}">
		<div class="footer-item">
		    <obv:addFlag model="['observationInstance':instance]" />
                </div>
        </g:if>

        <g:if test="${!hideFollow}">
		<div class="footer-item">
		    <feed:follow model="['sourceObject':instance]" />
                </div>
        </g:if>
        
        <g:if test="${!hideDownload}">
            <div class="footer-item">
                <obv:download  model="['source':'Checklist', 'requestObject':request, 'downloadTypes':[DownloadType.CSV, DownloadType.PDF], downloadObjectId:checklistInstance.id ]" />
            </div>		
        </g:if>

        <g:if test="${!hideSocial}">
		<div class="footer-item" style="position:absolute;right:0px;">
                    <fb:like layout="button_count" href="${href}" width="450" show_faces="true" data-send="true" style="top:-6px"></fb:like>
                    <div id="googleplus" class="pull-left"></div>
                    <% String twitterDesc = title?:'';
                    if(twitterDesc && description) twitterDesc += " - "
                    twitterDesc += description?:'';
                    %>
                    <a href="https://twitter.com/share" class="twitter-share-button" data-via="inbiodiversity" data-hashtags="IBP" data-count="none" data-url="${href}" data-text="${twitterDesc}" data-size="450"></a>
                </div>
        </g:if>
    </div>
    <div class="span4 nav clearfix" style="margin-left:20px;margin-bottom:3px;">
        <%
                def navParams = [:];
                navParams['controller'] = params.controller
                navParams['action'] ='show' 
                navParams['userGroupWebaddress'] = userGroup?userGroup.webaddress:userGroupWebaddress
                int pos = params.pos?params.int('pos'):0
                def curr_id = instance.id
                def prevId, nextId;
                def clazz = instance.class
                def obj = instance
                def userGroupInstance = UserGroup.findByWebaddress(params.webaddress);
                if(pos>=0 && (prevObservationId || nextObservationId)) {
                    prevId = prevObservationId;
                    nextId = nextObservationId
				} else {
					def prevIdList = clazz.withCriteria(){
								projections {
									property('id')
								}
								and{
									 lt('id', curr_id)
									 if(obj.hasProperty('isDeleted')){
										 eq('isDeleted', false)
									 }
									 if(obj.hasProperty('isShowable')){
										 eq('isShowable', true)
                                    }
                                    if(userGroupInstance){
                                        userGroups{
                                            eq('id', userGroupInstance.id)
                                        }
                                    }
			 					}
								maxResults 1
								order 'id', 'desc'
		                    } 
					def nextIdList = clazz.withCriteria(){
							projections {
								property('id')
							}
							and{
								 gt('id', curr_id)
								 if(obj.hasProperty('isDeleted')){
									 eq('isDeleted', false)
								 }
								 if(obj.hasProperty('isShowable')){
									 eq('isShowable', true)
                                     }
                                     if(userGroupInstance){
                                        userGroups{
                                            eq('id', userGroupInstance.id)
                                        }
                                    }
							 }
							maxResults 1
							order 'id', 'asc'
						}
					prevId = prevIdList.isEmpty() ? '' : prevIdList[0]
					nextId = nextIdList.isEmpty() ? '' : nextIdList[0]
                }
                if(!lastListParams) {
                    lastListParams = [:]
                    lastListParams['controller'] = params.controller
                    lastListParams['action'] = 'list'
                }
        %>
        
        <% navParams['id'] = prevId; 
        if(pos>=0 && (prevObservationId || nextObservationId)) 
            navParams['pos'] = pos-1; 
        %>
        <a class="pull-left btn  btn-mini ${prevId?:'disabled'}" href="${uGroup.createLink(navParams.clone())}"><i class="icon-backward"></i><g:message code="button.prev" /></a>

        <% navParams['id'] = nextId; 
        if(pos>=0 && (prevObservationId || nextObservationId))
            navParams['pos'] = pos+1; 
        %>
        <a class="pull-right  btn btn-mini ${nextId?:'disabled'}"  href="${uGroup.createLink(navParams.clone())}"><g:message code="button.next" /><i style="margin-right: 0px; margin-left: 3px;" class="icon-forward"></i></a>

        <%lastListParams.put('userGroupWebaddress', userGroup?userGroup.webaddress:userGroupWebaddress);
        if(pos)
                lastListParams.put('fragment', pos);	 
        %>
        <a class="btn btn-mini" href="${uGroup.createLink(lastListParams)}" style="text-align: center;display: block;margin: 0 auto;">List</a>
    </div>


</div>
<r:script>
function renderGooglePlus() {
    gapi.plusone.render("googleplus", {size:'medium',annotation:'none', href:"${href}"});
}
</r:script>
