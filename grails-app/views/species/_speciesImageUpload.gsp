<div class="tabbable speciesImage-wrapper " style="display:none;">
    <ul class="nav nav-tabs" id="speciesImage-tabs" style="margin:0px;background-color:transparent;">
        <g:if test="${isSpeciesContributor}">
        <li class="active"><a href="#speciesImage-tab0" class="btn" data-toggle="tab">Observation Images</a></li>
        </g:if>
        <g:if test="${isSpeciesContributor}">
            <li id=""><a href="#speciesImage-tab1" class="btn" data-toggle="tab">Upload Images</a></li>
        </g:if>
    </ul>
    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <div class="tab-content" id="speciesImage-tab-content">
            <g:if test="${isSpeciesContributor}">
            <div class="tab-pane active" id="speciesImage-tab0" style="max-height:345px;overflow-y:scroll;">
                <%--
                <form id="pullObvImagesForm" action="${uGroup.createLink(action:'uploadImage', controller:'species','userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">
                    <input type="hidden" name='speciesId' value="${speciesInstance.id}" />
                    <g:render template="/observation/addPhoto" model="['observationInstance':speciesInstance]"/>
                    <a id="pullObvImagesBtn" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Pull Images </a>
                </form>
                --%>
            </div>
            </g:if>
            <g:if test="${isSpeciesContributor}">
            <div class="tab-pane" id="speciesImage-tab1" style="max-height:345px;overflow-y:scroll;">
                <form id="uploadSpeciesImagesForm" action="${uGroup.createLink(action:'uploadImage', controller:'species','userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">
                    <input type="hidden" name='speciesId' value="${speciesInstance.id}" />
                    <g:render template="/observation/addPhoto" model="['observationInstance':speciesInstance]"/>
                    <a id="uploadSpeciesImagesBtn" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Upload Images </a>

                </form>
            </div>
            </g:if>
        </div>
    </div>
</div>		
