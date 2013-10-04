<%@page import="species.participation.Featured"%>
<%@page import="species.participation.Observation"%>

<div class="is-featured">
    <g:if test="${Featured.isFeaturedIn(observationInstance)}">
    <a href="#" onclick="$(this).next('.featured-groups-name').toggle(300);return false;">
	    		<h5>Featured in Groups : <span class="caret" style="margin-top: 3px;margin-left: 4px"></span></h5>
        </a>
        <div class="featured-groups-name">
            <g:each in="${Featured.isFeaturedIn(observationInstance)}" var="groupInfo">
            <li title ="Why Featured : ${groupInfo.notes}" class=""><uGroup:showUserGroupSignature
                    model="[ 'userGroup':groupInfo.userGroup]" /></li>
            </g:each>        
        </div>
    </g:if>
</div>

