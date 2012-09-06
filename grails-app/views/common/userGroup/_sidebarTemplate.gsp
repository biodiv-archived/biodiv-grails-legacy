<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>

<div class="sidebar left-sidebar"
	style="margin: 0px; width: 250px; float: left;">

	<div class="super-section">
		<h3>Home</h3>
		<div class="section">
			<i class="icon-home"></i> Activity

		</div>
		<div class="section">
			<i class="icon-envelope"></i> Notifications
		</div>
		<div class="section">
			<i class="icon-user"></i> <a href="<sUser:renderProfileHyperLink/>">Profile</a>

		</div>
		<div class="section">
			<i class="icon-cog"></i> Settings
		</div>

	</div>

	<div class="super-section">
		<h3>Groups</h3>
		<div class="section">
			<i class="icon-snapshot"></i>
			<g:link controller="userGroup" action="list" params="['user':2]">My Groups</g:link>

		</div>

		<div class="section">
			<i class="icon-snapshot"></i>
			<g:link controller="userGroup" action="list">Browse Groups</g:link>
		</div>
		
		<div class="section">
			<i class="icon-snapshot"></i> India Biodiversity Group
		</div>
		
		<div class="section">
			<i class="icon-plus-sign"></i>
			<g:link controller="userGroup" action="create">Create a Group</g:link>
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
</div>

<<<<<<< HEAD =======
<r:script>
function reloadMembers() {
	$.ajax({
       	url: "${createLink(action:'members',id:userGroupInstance.id) }",
           method: "GET",
           dataType: "json",
           data:{'isAjaxLoad':true,'onlyMembers':true},
           success: function(data) {
           	var html = "";
           	$.each(data.result, function(i, item) {
           		html += "<a
		href='"+"${createLink(controller:'SUser', action:'show')}/"+item.id+"'>"+
		"<img src='"+item.icon+"' class='pull-left small_profile_pic'
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
>>>>>>> branch 'biodiv_usergroups' of git@github.com:strandls/biodiv.git
