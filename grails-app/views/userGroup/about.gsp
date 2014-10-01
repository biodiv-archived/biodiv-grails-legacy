<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'ugroup.value.aboutus')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>


<r:require modules="userGroups_show" />
</head>
<body>
	<div class="observation span12 bodymarker">
		<div class="page-header clearfix">
			<div style="width: 100%;">
				<div class="span8 main_heading" style="margin-left: 0px;">
					<h1><g:message code="button.about.us" /></h1>
				</div>
				<sec:permitted className='species.groups.UserGroup'
					id='${userGroupInstance.id}'
					permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

					<a class="btn btn-info pull-right"
						href="${uGroup.createLink(mapping:'userGroup', action:'edit', userGroup:userGroupInstance)}">
						<i class="icon-edit"></i><g:message code="button.edit.group" /> </a>
					<!-- a class="btn btn-primary" href="${uGroup.createLink(mapping:'userGroup', action:'settings', userGroup:userGroupInstance)}"><i class="icon-cog"></i>Settings</a-->
				</sec:permitted>

			</div>
		</div>
		<div style="clear: both;"></div>

		
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]" />

		<div class="userGroup-section">


			<div class="tabbable">
				<ul class="nav nav-tabs">
					<li
						class="${(!params.action || params.action == 'about')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'about', 'userGroup':userGroupInstance)}">
							<g:message code="button.about.us" /></a></li>
					<li
						class="${(!params.action || params.action == 'user')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'user', 'userGroup':userGroupInstance)}">
							<g:message code="button.all.members" />  (${membersTotalCount})</a></li>

					<li class="${(params.action == 'founders')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'founders', 'userGroup':userGroupInstance)}">
							<g:message code="tabs.founder" /> (${foundersTotalCount})</a></li>
					<li class="${(params.action == 'experts')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'moderators', 'userGroup':userGroupInstance)}">
							<g:message code="tabs.moderators" /> (${expertsTotalCount})</a></li>
				</ul>


				<g:if test="${params.action == 'about' }">
					<div class="tab-pane">
						<div class="super-section userGroup-section">
							<div class="description notes_view">
								<g:render template="/common/languagewrap" model="['domainInstance': userGroupInstance , 'userLanguage' : userLanguage, 'contentValue' : userGroupInstance.description]"/>
							</div>
							<div class="section">
								<div class="prop">
									<span class="name"><i class="icon-time"></i><g:message code="default.founded.label" /></span>
									<obv:showDate
										model="['userGroupInstance':userGroupInstance, 'propertyName':'foundedOn']" />
								</div>
							</div>
						</div>


						<!-- div class="super-section">
			<h3>Driven by</h3>
			<div class="section">
				<h5>
					Founders
				</h5>
				<div id="founders_sidebar"></div>
				<g:link mapping="userGroup" action="founders"
					params="['webaddress':userGroupInstance.webaddress]">...</g:link>
			</div>

			<div class="section">
				<h5>
					Members
				</h5>
				<div id="members_sidebar"></div>
				<g:link mapping="userGroup" action="user"
					params="['webaddress':userGroupInstance.webaddress]">...</g:link>
			</div>
		</div-->

						<div class="super-section">
							<h3><g:message code="suser.edit.intrests" /></h3>
							<div class="section">
								<h5><g:message code="default.species.groups.label" /></h5>
								<uGroup:interestedSpeciesGroups
									model="['userGroupInstance':userGroupInstance]" />
							</div>

							<div class="section">
								<h5><g:message code="default.habitats.label" /></h5>
								<uGroup:interestedHabitats
									model="['userGroupInstance':userGroupInstance]" />

							</div>
							<div class="section">
								<h5><g:message code="default.location.label" /></h5>
								<uGroup:showLocation
									model="['userGroupInstance':userGroupInstance]" />
							</div>
							<div class="section">
								<uGroup:showAllTags
									model="['tagFilterByProperty':'UserGroup' , 'tagFilterByPropertyValue':userGroupInstance, 'isAjaxLoad':true]" />
							</div>
						</div>

						<!-- div class="super-section userGroup-section">
			<div class="description notes_view" name="contactEmail">
				Contact us by filling in the following feedback form.</div>
		</div-->
					</div>
				</g:if>
			</div>
		</div>

	</div>


	<r:script>
			function reloadMembers(url) {
				var membersUrl = (url)?url:"${createLink(mapping:'userGroup', action:'members', params:['webaddress':userGroupInstance.webaddress]) }"  
				$.ajax({
			       	url: membersUrl,
			           method: "GET",
			           dataType: "json",
			           data:{'isAjaxLoad':true,'onlyMembers':true},
			           success:
					function(data) {
			           	var html='';
			           	$.each(data.result, function(i, item) {
			           		html +="<a href='"+"${createLink(controller:'SUser', action:'show')}/"+item.id+"'>"
                                                + "<img src='"+item.icon+"' class='pull-left small_profile_pic' title='"+item.name+"'>"+ "</a>";
			           	});
			           	$("#members_sidebar").html(html);
			           }, error: function(xhr, status, error) {
						handleError(xhr, status, error, undefined, function() {
			               	var msg = $.parseJSON(xhr.responseText);
			                   $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
						});
			           }
				});
			}
			function reloadFounders(url) {
				var foundersUrl = (url)?url:"${createLink(mapping:'userGroup', action:'founders',params:['webaddress':userGroupInstance.webaddress]) }"  
				$.ajax({
			       		url: foundersUrl,
			           method: "GET",
			           dataType: "json",
			           data:{'isAjaxLoad':true},
			           success: function(data) {
			           	var html = "";
			           	$.each(data.result, function(i, item) {
			           		html += "<a href='"+"${createLink(controller:'SUser', action:'show')}/"+item.id+"'>"+
                        			"<img src='"+item.icon+"' class='pull-left small_profile_pic' title='"+item.name+"'>"+ "</a>";
			           	});
			           	$("#founders_sidebar").html(html);
			           }, 
			           error: function(xhr, status, error) {
						handleError(xhr, status, error, undefined, function() {
			               	var msg = $.parseJSON(xhr.responseText);
			                   $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
						});
			           }
				});
			}
			$(document).ready(function(){
				//reloadFounders();
				//reloadMembers();
			});
		</r:script>
</body>
</html>
