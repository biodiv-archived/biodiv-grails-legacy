<g:if test="${observationInstance || canPullResource}">
    <div class="tabbable groups-wrapper">
        <ul class="nav nav-tabs" id="action-tabs" style="margin:0px;background-color:transparent;">
            <g:if test="${observationInstance}">
            <li class="active"><a href="#tab0" class="btn" data-toggle="tab">Groups</a></li>
            </g:if>
            <g:if test="${canPullResource}">
            <li id="post-on-list"><a href="#tab1" class="btn" data-toggle="tab">Post to Groups</a></li>
           	</g:if>	
            <g:if test="${observationInstance && canPullResource}">
            <li><a href="#tab2" class="btn" data-toggle="tab">Feature in a Group</a></li>
            </g:if>
        </ul>
        <div class="sidebar_section" style="clear:both;overflow:hidden;">

            <div class="tab-content" id="action-tab-content">
                <g:if test="${observationInstance}">
                <div class="tab-pane active" id="tab0">
                    <uGroup:resourceInGroups
                    model="['observationInstance':observationInstance]"  />
                </div>
                </g:if>
                <g:if test="${canPullResource}">
                <div class="tab-pane" id="tab1">
                    <uGroup:objectPostToGroups
                    model="['objectType':objectType, userGroup:userGroup, canPullResource:canPullResource, isBulkPull:isBulkPull, 'observationInstance':observationInstance]" />
                </div>
                </g:if>     
                <g:if test="${observationInstance && canPullResource}">
                <div class="tab-pane" id="tab2">
                    <uGroup:featureUserGroups model="['observationInstance':observationInstance]"/>
                </div>
                </g:if>
            </div>
        </div>
    </div>		
</g:if>
