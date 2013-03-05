<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.groups.UserGroup"%>
<%@ page import="species.participation.ActivityFeedService"%>

<ul class="nav left-sidebar pull-right">

	<li><search:searchBox />
	</li>
	<%--	<li><div class="header_group span2" style="height: 50px;">--%>
	<%--			<obv:showRelatedStory--%>
	<%--				model="['controller':'userGroup', 'observationId': 1, 'action':'getFeaturedUserGroups', 'id':'uG', hideShowAll:true]" />--%>
	<%--		</div></li>--%>
	<uGroup:showSuggestedUserGroups />

	<li><sUser:userLoginBox model="['userGroup':userGroupInstance]" />
	</li>





</ul>
