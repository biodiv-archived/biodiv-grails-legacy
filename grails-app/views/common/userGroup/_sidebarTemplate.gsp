<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.participation.ActivityFeedService"%>

<div class="span3 sidebar">
	
		<div class="super-section">

			<ul class="nav">
				<li class="nav-header">Home</li>
				<sec:ifLoggedIn>
				<li><a
					href="${createLink(controller:'activityFeed', params:['user':sUser.renderCurrentUserId(), 'feedType':ActivityFeedService.MY_FEEDS])}"><i
						class="icon-home"></i>My Feed</a>
				</li>

				<li><a
					href="${createLink(controller:'user', action:'show', params:['user':sUser.renderCurrentUserId()])}"><i
						class="icon-user"></i>My Profile</a>
				</li>
				<li><a
					href="${createLink(controller:'userGroup', action:'list', params:['user':sUser.renderCurrentUserId()])}"><i
						class="icon-user"></i>My Groups</a>
				</li>
				<li><a
					href="${createLink(controller:'observation', action:'list', params:['user':sUser.renderCurrentUserId()])}"><i
						class="icon-screenshot"></i>My Observations</a>
				</li>
				</sec:ifLoggedIn>
				<sec:ifNotLoggedIn>
				<li><a
					href="${createLink(controller:'activityFeed', params:['user':sUser.renderCurrentUserId(), 'feedType':ActivityFeedService.ALL])}"><i
						class="icon-home"></i>Activity</a>
				</li>
				</sec:ifNotLoggedIn>
				<li class="${(params.action=='create')?'active':'' }">
					<g:link controller='login' class="btn btn-primary span1">Login</g:link>
				</li>
				<li class="${(params.action=='create')?'active':'' }" style="clear:both;"><a
					href="${createLink(action:'create')}"><i class="icon-plus"></i>Add
					Observation</a>
				</li>
			</ul>
		</div>
	
	<div class="super-section">
		<uGroup:showSuggestedUserGroups />
	</div>

</div>

<g:if test="${false }">
	<div class="super-section">
		<div class="section">
			<h5>
				<i class="icon-user"></i>Founders
			</h5>
			<div id="founders_sidebar"></div>
			<g:link controller="userGroup" action="founders"
				id="${userGroupInstance.id}">...</g:link>
		</div>

		<div class="section">
			<h5>
				<i class="icon-user"></i>Members
			</h5>
			<div id="members_sidebar"></div>
			<g:link controller="userGroup" action="members"
				id="${userGroupInstance.id}">...</g:link>
		</div>
	</div>

	<div class="super-section">
		<h3>Interested In</h3>
		<div class="section">
			<h5>
				<i class="icon-snapshot"></i>Species Groups
			</h5>
			<uGroup:interestedSpeciesGroups
				model="['userGroupInstance':userGroupInstance]" />
		</div>

		<div class="section">
			<h5>
				<i class="icon-snapshot"></i>Habitat
			</h5>
			<uGroup:interestedHabitats
				model="['userGroupInstance':userGroupInstance]" />

		</div>
	</div>

	<div class="super-section">
		<div class="section">
			<uGroup:showLocation model="['userGroupInstance':userGroupInstance]" />
		</div>
	</div>

	<div class="super-section">
		<div class="section">
			<uGroup:showAllTags
				model="['tagFilterByProperty':'UserGroup' , 'tagFilterByPropertyValue':userGroupInstance, 'isAjaxLoad':true]" />
		</div>
	</div>

	<div class="super-section">
		<div class="section">
			<div class="prop">
				<span class="name"><i class="icon-time"></i>Founded</span>
				<obv:showDate
					model="['userGroupInstance':userGroupInstance, 'propertyName':'foundedOn']" />
			</div>
		</div>
	</div>

	<div class="super-section">
		<div class="section">
			<g:link action="aboutUs" id="${userGroupInstance.id}">More about us here</g:link>
			or<br />
			<g:link action="aboutUs" id="${userGroupInstance.id}"
				fragment="contactEmail">Contact us here</g:link>
		</div>
	</div>

	<r:script>
			function reloadMembers() {
				$.ajax({
			       	url: "
					${createLink(action:'members',id:userGroupInstance.id) }",
			           method: "GET",
			           dataType: "json",
			           data:{'isAjaxLoad':true,'onlyMembers':true},
			           success:
					function(data) {
			           	var html=""
					;
			           	$.each(data.result, function(i, item) {
			           		html +="<a
			href='"+"${createLink(controller:'SUser', action:'show')}/"+item.id+"'>"
			+ "<img src='"+item.icon+"' class='pull-left small_profile_pic'
			title='"+item.name+"'>"+ "</a>";
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
			function reloadFounders() {
				$.ajax({
			       	url: "${createLink(action:'founders',id:userGroupInstance.id) }",
			           method: "GET",
			           dataType: "json",
			           data:{'isAjaxLoad':true},
			           success: function(data) {
			           	var html = "";
			           	$.each(data.result, function(i, item) {
			           		html += "<a
			href='"+"${createLink(controller:'SUser', action:'show')}/"+item.id+"'>"+
			"<img src='"+item.icon+"' class='pull-left small_profile_pic'
			title='"+item.name+"'>"+ "</a>";
			           	});
			           	$("#founders_sidebar").html(html);
			           }, error: function(xhr, status, error) {
						handleError(xhr, status, error, undefined, function() {
			               	var msg = $.parseJSON(xhr.responseText);
			                   $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
						});
			           }
				});
			}
			$(document).ready(function(){
				reloadFounders();
				reloadMembers();	
			});
		</r:script>
</g:if>



