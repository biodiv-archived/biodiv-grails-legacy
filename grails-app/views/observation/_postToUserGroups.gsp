<uGroup:isUserGroupMember>
<div class="userGroupsSuperDiv span12 super-section"  style="clear: both">
    <g:if test="${params.action == 'bulkCreate' || params.oldAction == 'bulkSave'}">
    <div class="close_button close_user_group"></div>
    </g:if>
    <div class="section" style="position: relative; overflow: visible;">
        <h3><g:message code="heading.post.user.groups" /></h3>
        <div>
            <%
            def obvActionMarkerClass = (params.action == 'create' || params.action == 'save')? 'create' : '' 
            %>
            <div class="userGroupsClass ${obvActionMarkerClass}" name="userGroups" style="list-style:none;clear:both;">
                <uGroup:getCurrentUserUserGroups model="['observationInstance':observationInstance]"/>
            </div>
        </div>
    </div>
</div>
</uGroup:isUserGroupMember>

