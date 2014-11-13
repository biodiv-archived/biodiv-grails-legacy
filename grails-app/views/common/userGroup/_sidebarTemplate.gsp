<%@page import="species.utils.ImageType"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.groups.UserGroup"%>
<%@ page import="species.participation.ActivityFeedService"%>
<%@ page import ="org.springframework.web.servlet.support.RequestContextUtils" %>


<ul class="nav left-sidebar pull-right">

	<li><search:searchBox />
	</li>

    <li>
        <div id="advSearchDropdown" class="dropdown" style="display:inline-block;left:-60px; top:8px;z-index:200">
            <a href="#"  id="advSearchDropdownA" data-target="#"> <b class="caret"></b> </a>
            <div id="advSearchBox" class="dropdown-menu" aria-labelledby="advSearchDropdownA" style="text-align: left;left:-412px;width:430px;">
                <search:advSearch />

            </div>
        </div>
    </li>


	<uGroup:showSuggestedUserGroups />
    <g:if test="${localeLanguages && !hideLanguages}">
    <li class="btn-toolbar" style="margin-top:0px;margin-bottom:0px;">
        <div class="btn-group" style="padding: 6px 12px 6px 0px;">
            <g:each in="${localeLanguages}" var="localeLanguage">
                <%  
                    def lang_code = RequestContextUtils.getLocale(request); 
                    def isDisabled = (lang_code.toString() == localeLanguage.code) ? 'disabled' : '' ;
                %>
                <button class="btn btn-mini lang_c ${isDisabled}" rel="${localeLanguage.code}">${localeLanguage.code?.toUpperCase()}</button>
            </g:each>
        </div>
    </li>
    <script type="text/javascript">
    $(document).ready(function(){
        $('.lang_c').click(function(){
            if(!$(this).hasClass('disabled')){
                setLanguage($(this).attr('rel'));
            }                
        });
    });
    </script>
    </g:if>

    <li>
        <g:render template="/common/suser/userLoginBoxTemplate" model="['userGroup':userGroupInstance]" />
    </li>





</ul>
