<div class="tabbable speciesImage-wrapper " style="display:none;">
    <ul class="nav nav-tabs" id="speciesImage-tabs" style="margin:0px;background-color:transparent;">
        <g:if test="${isSpeciesContributor}">
            <li id="speciesImage-li0" class="active"><a href="#speciesImage-tab0" class="btn" data-toggle="tab">Add Observation Images</a></li>
            <li id="speciesImage-li1"><a href="#speciesImage-tab1" class="btn" data-toggle="tab">Upload/Edit Images</a></li>
            <li id="speciesImage-li2"><a href="#speciesImage-tab2" class="btn" data-toggle="tab">Add Species Field Images</a></li>   
        </g:if>
    </ul>
    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <div class="tab-content" id="speciesImage-tab-content">
            <g:if test="${isSpeciesContributor}">
            <div class="tab-pane active" id="speciesImage-tab0" style="max-height:392px;overflow-y:scroll;">
                
                <form id="pullObvImagesForm" action="${uGroup.createLink(action:'pullObvImage', controller:'species','userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">
                    <input type="hidden" name='speciesId' value="${speciesInstance.id}" />
                    <obv:addPhotoWrapper model="['observationInstance':speciesInstance, 'resourceListType':'fromRelatedObv']"></obv:addPhotoWrapper>
                    <a id="pullObvImagesBtn" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Pull Images </a>
                </form>
                   
            </div>
            <div class="tab-pane" id="speciesImage-tab1" style="max-height:392px;overflow-y:scroll;">
                <form id="uploadSpeciesImagesForm" action="${uGroup.createLink(action:'uploadImage', controller:'species','userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">
                    <input type="hidden" name='speciesId' value="${speciesInstance.id}" />
                    <obv:addPhotoWrapper model="['observationInstance':speciesInstance, 'resourceListType': 'ofSpecies']"></obv:addPhotoWrapper>
                    <a id="uploadSpeciesImagesBtn" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Save </a>

                </form>
                <%

                def obvTmpFileName = (speciesInstance) ? (speciesInstance.fetchSpeciesImageDir().getAbsolutePath()) : false 
                def obvDir = obvTmpFileName ?  obvTmpFileName.substring(obvTmpFileName.lastIndexOf("/"), obvTmpFileName.size()) : ""
                %>
                <form id="upload_resource" 
                    title="Add a photo for this observation"
                    method="post"
                    class="${hasErrors(bean: speciesInstance, field: 'resources', 'errors')}">

                    <span class="msg" style="float: right"></span>
                    <input class="videoUrl" type="hidden" name='videoUrl'value="" />
                    <input type="hidden" name='obvDir' value="${obvDir}" />
                    <input type="hidden" name='resType' value='${speciesInstance.class.name}'>
                </form>

            </div>
            <div class="tab-pane" id="speciesImage-tab2" style="max-height:392px;overflow-y:scroll;">
                <form id="pullSpeciesFieldImagesForm" action="${uGroup.createLink(action:'pullSpeciesFieldImage', controller:'species','userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">
                    <input type="hidden" name='speciesId' value="${speciesInstance.id}" />
                    <obv:addPhotoWrapper model="['observationInstance':speciesInstance, 'resourceListType':'fromSpeciesField']"></obv:addPhotoWrapper>
                    <a id="pullSpeciesFieldImagesBtn" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Pull Images </a>
                </form>
                   
            </div>
            </g:if>
        </div>
    </div>
</div>		
