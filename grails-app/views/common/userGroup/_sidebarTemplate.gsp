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

    <g:if test="${localeLanguages && !hideLanguages}">
    <li class="btn-toolbar" style="margin-top:0px;margin-bottom:0px;">
        <div class="btn-group">
            <g:each in="${localeLanguages}" var="localeLanguage">
                <a class="btn btn-link" href="#" onclick="setLanguage('${localeLanguage.code}')">${localeLanguage.code}</a>
            </g:each>

        </div>
    </li>
    </g:if>

    <li>
        <g:render template="/common/suser/userLoginBoxTemplate" model="['userGroup':userGroupInstance]" />
    </li>





</ul>
