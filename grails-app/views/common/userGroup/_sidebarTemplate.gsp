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
    <li class="dropdown open">
        <a class="dropdown-toggle" data-toggle="dropdown" style="color:#bbb;">
                 ${g.message(code:'button.language')} <b class="caret"></b>
        </a>
        <ul class="dropdown-menu" style="max-height:300px;overflow-x:hidden;overflow-y:auto;">
            <g:each in="${localeLanguages}" var="localeLanguage">
                <li>
                <a class="btn btn-link ${(params?.lang == localeLanguage.code)?'disabled':''}" href="#" onclick="setLanguage('${localeLanguage.code}')">${localeLanguage.name}</a>
                </li>
            </g:each>

        </ul>
    </li>
    </g:if>

    <li><sUser:userLoginBox model="['userGroup':userGroupInstance]" /></li>





</ul>
