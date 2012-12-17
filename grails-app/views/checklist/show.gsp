<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'checklist', action:'show', id:checklistInstance.id)}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'checklistShow.label', default: 'Checklist Show')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<%--<script src="http://maps.google.com/maps/api/js?sensor=true"></script>--%>
<r:require modules="checklist"/>
<style>
.comment-post-btn{
	position:relative;
}
</style>
</head>
<body>
	
			<div class="span12">
				<div class="page-header clearfix" style="padding-bottom: 0px;">
						<h1>
							${checklistInstance.title}
						</h1>
						<h6>
						${checklistInstance.attribution}
						</h6>
				</div>

				<g:if test="${flash.message}">
					<div class="message alert alert-info">
						${flash.message}
					</div>
				</g:if>
				
				<div style="clear:both;"></div>
					<g:if test="${params.pos && lastListParams}">
						<div class="nav" style="width:100%;">
							<g:if test="${test}">
								<a class="pull-left btn ${prevObservationId?:'disabled'}" href="${uGroup.createLink([action:"show", controller:"checklist", id:prevObservationId, 'pos':params.int('pos')-1, 'userGroupWebaddress':(userGroup?userGroup.webaddress:userGroupWebaddress)])}">Prev Checklist</a>
								<a class="pull-right  btn ${nextObservationId?:'disabled'}"  href="${uGroup.createLink([action:"show", controller:"checklist",
									id:nextObservationId, 'pos':params.int('pos')+1, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Next Checklist</a>
								<%lastListParams.put('userGroupWebaddress', userGroup?userGroup.webaddress:userGroupWebaddress);
									lastListParams.put('fragment', params.pos);
								 %>
								<a class="btn" href="${uGroup.createLink(lastListParams)}" style="text-align: center;display: block;width: 125px;margin: 0 auto;">List Checklist</a>
							</g:if>
							<g:else>
								<a class="pull-left btn ${prevObservationId?:'disabled'}" href="${uGroup.createLink([action:"show", controller:"checklist",
									id:prevObservationId, 'pos':params.int('pos')-1, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Prev Checklist</a>
								<a class="pull-right  btn ${nextObservationId?:'disabled'}"  href="${uGroup.createLink([action:"show", controller:"checklist",
									id:nextObservationId, 'pos':params.int('pos')+1, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress])}">Next Checklist</a>
								<%lastListParams.put('userGroupWebaddress', userGroup?userGroup.webaddress:userGroupWebaddress);
								lastListParams.put('fragment', params.pos);	 
								%>
								<a class="btn" href="${uGroup.createLink(lastListParams)}" style="text-align: center;display: block;width: 125px;margin: 0 auto;">List Checklist</a>
							</g:else>
						</div>
					</g:if>
				
				
			<div>	
				<table class="table table-hover" style="margin-left: 0px;">
					<thead>
						<tr>
							<th>Species Group</th>
							<th>No. of Species</th>
							<th>Place Name</th>
							<th>State(s)</th>
							<th>District(s)</th>
							<th>Taluk(s)</th>
						</tr>
					</thead>
					<tbody>
							<tr>
								<td>${checklistInstance.speciesGroup?.name}</td>
								<td>${checklistInstance.speciesCount}</td>
								<td>${checklistInstance.placeName}</td>
								<td>${checklistInstance.state.join(",")}</td>
								<td>${checklistInstance.district.join(",")}</td>
								<td>${checklistInstance.taluka.join(",")}</td>
							</tr>
					</tbody>
				</table>
			</div>
			
			<div>
				<div class="ui-widget">
					<div class="speciesFieldHeader ui-dialog-titlebar ui-helper-clearfix ui-widget-header">
						<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
						<a href="#license_information">License information</a> 
					</div>
					<div class="ui-widget-content speciesField">
						<dl class="dl">
			     			<dt>Attribution</dt>
    						<dd>${checklistInstance.attribution}</dd>
    				
    						<dt>License</dt>
			    			<dd><img src="${resource(dir:'images/license',file:checklistInstance?.license?.name?.getIconFilename()+'.png', absolute:true)}"
								title="${checklistInstance.license.name}"/>
							</dd>
						</dl>
					</div>
				</div>	
				
				<g:if test="${checklistInstance.sourceText}" >
					<div class="ui-widget">
						<div class="speciesFieldHeader ui-dialog-titlebar ui-helper-clearfix ui-widget-header">
							<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
							<a href="#source">Source</a> 
						</div>
						<div class="ui-widget-content speciesField">
							<dl class="dl linktext">
								<dd>${checklistInstance.sourceText}</dd>
							</dl>
						</div>
					</div>		
				</g:if>
				
				<div class="ui-widget">
						<div class="speciesFieldHeader ui-dialog-titlebar ui-helper-clearfix ui-widget-header">
							<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
							<a href="#checklist_details">Checklist details</a> 
						</div>
						<div class="ui-widget-content speciesField">
							<dl class="dl linktext">
								<dd>${checklistInstance.description}</dd>
    						</dl>
    					</div>
    			</div>
				
				
			<div class="ui-widget">
				<div class="speciesFieldHeader ui-dialog-titlebar ui-helper-clearfix ui-widget-header">
						<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
						<a href="#checklist"> Checklist</a> 
				</div>
				<div class="ui-widget-content speciesField">
					<clist:showData
						model="['checklistInstance':checklistInstance]">
					</clist:showData>
				</div>
			</div>
			
			<g:if test="${checklistInstance.refText}" >
					<div class="ui-widget">
						<div class="speciesFieldHeader ui-dialog-titlebar ui-helper-clearfix ui-widget-header">
							<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
							<a href="#references">References</a> 
						</div>
						<div class="ui-widget-content speciesField">
						<dl class="dl linktext">
							<dd>${checklistInstance.refText}</dd>
						</dl>
						</div>
					</div>		
			</g:if>
				
				
			<div class="union-comment" style="clear: both;">
				<feed:showAllActivityFeeds model="['rootHolder':checklistInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
				<%
					def canPostComment = customsecurity.hasPermissionAsPerGroups([object:checklistInstance, permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
				%>
				<comment:showAllComments model="['commentHolder':checklistInstance, commentType:'super', 'canPostComment':canPostComment, 'showCommentList':false]" />
			</div>
			
			</div>
	</div>	
	<g:javascript>
	$(document).ready(function(){
	window.params = {};
	
	$('div.speciesFieldHeader').collapser({
		target: 'next',
		effect: 'slide',
		changeText: false
		},function(){
			var ele = $(this);
			var x = ele.find(".ui-icon")
			if(ele.next('.speciesField').is(":visible")) {				 
				x.removeClass('ui-icon-circle-triangle-s').addClass('ui-icon-circle-triangle-e');
			} else {
				x.removeClass('ui-icon-circle-triangle-e').addClass('ui-icon-circle-triangle-s')
			}
		} , function(){
					
		}
	);

    $(".speciesField").each(function() {
	    if(jQuery.trim($(this).text()).length == 0) {
		    $(this).prev("div.speciesFieldHeader").children("span").removeClass("ui-icon ui-icon-circle-triangle-s")
		}
	})
	$('.linktext').linkify();
	});
	</g:javascript>
</body>
</html>
