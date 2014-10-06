<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.groups.UserGroup"%>
<%@ page import="species.participation.ActivityFeedService"%>

<ul class="nav left-sidebar pull-right">

	<li><search:searchBox />
	</li>

    <li>
        <div class="dropdown" style="display:inline-block;left:-60px; top:8px;z-index:200">
            <a href="#" class="dropdown-toggle"
                data-toggle="dropdown"> <b class="caret"></b> </a>
            <div id="advSearchBox" class="dropdown-menu" style="text-align: left;left:-412px;width:430px;">
                <search:advSearch />

            </div>
        </div>
    </li>


	<uGroup:showSuggestedUserGroups />

	<li><sUser:userLoginBox model="['userGroup':userGroupInstance]" />
	</li>





</ul>
