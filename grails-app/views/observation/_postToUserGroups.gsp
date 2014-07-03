<uGroup:isUserGroupMember>
<div class="span12 super-section"  style="clear: both">
    <div class="section" style="position: relative; overflow: visible;">
        <h3>Post to User Groups</h3>
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


