<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.groups.UserGroup"%>
<%@ page import="species.participation.ActivityFeedService"%>

<ul class="nav left-sidebar pull-right">

	<li><search:searchBox />
	</li>

	
	<uGroup:showSuggestedUserGroups />

	<li><sUser:userLoginBox model="['userGroup':userGroupInstance]" />
	</li>





</ul>
