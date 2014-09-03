<div class="tabbable speciesFieldImage-wrapper " style="">
    <ul class="nav nav-tabs" id="speciesFieldImage-tabs" style="margin:0px;background-color:transparent;">
        <g:if test="${isSpeciesContributor}">
            <li id="speciesFieldImage-li0" class="active"><a href="#speciesFieldImage-tab0" class="btn" data-toggle="tab"><g:message code="speciesimageupload.add.observation.images" /></a></li>
            <li id="speciesFieldImage-li1"><a href="#speciesFieldImage-tab1" class="btn" data-toggle="tab"><g:message code="speciesimageupload.upload.edit.images" /> </a></li>
        </g:if>
    </ul>
    <div class="sidebar_section" style="clear:both;overflow:hidden;">
        <div class="tab-content" id="speciesFieldImage-tab-content">
            <g:if test="${isSpeciesContributor}">
            <div class="tab-pane active" id="speciesFieldImage-tab0" style="max-height:410px;overflow-y:scroll;">
                
                <form id="pullObvImagesSpFieldForm" action="${uGroup.createLink(action:'pullObvMediaInSpField', controller:'species','userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">
                    <input type="hidden" name='speciesFieldId' value="" />
                    <input type="hidden" name='speciesId' value="${speciesInstance.id}" />
                    <obv:addPhotoWrapper model="['observationInstance':observationInstance, 'resourceListType':'fromRelatedObv' , 'checkFlag':true]"></obv:addPhotoWrapper>
                    <!--a id="pullObvImagesSpFieldBtn" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Pull Images </a-->
                </form>
                   
            </div>
            <div class="tab-pane" id="speciesFieldImage-tab1" style="max-height:410px;overflow-y:scroll;">
                <form id="uploadSpeciesFieldImagesForm" action="${uGroup.createLink(action:'uploadMediaInSpField', controller:'species','userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">
                    <input type="hidden" name='speciesFieldId' value="" />
                    <input type="hidden" name='speciesId' value="${speciesInstance.id}" />
                    <obv:addPhotoWrapper model="['observationInstance':observationInstance, 'resourceListType': 'fromSingleSpeciesField']"></obv:addPhotoWrapper>
                    <!--a id="uploadSpeciesFieldImagesBtn" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Save </a-->

                </form>
                <%

                def obvTmpFileName = (speciesInstance) ? (speciesInstance.fetchSpeciesImageDir().getAbsolutePath()) : false 
                def obvDir = obvTmpFileName ?  obvTmpFileName.substring(obvTmpFileName.lastIndexOf("/"), obvTmpFileName.size()) : ""
                %>
                <form class="upload_resource ${hasErrors(bean: speciesInstance, field: 'resources', 'errors')}" 
                    title="${g.message(code:'title.checklist.create')}"
                    method="post">
                    <span class="msg" style="float: right"></span>
                    <input class="videoUrl" type="hidden" name='videoUrl'value="" />
                    <input type="hidden" name='obvDir' value="${obvDir}" />
                    <input type="hidden" name='resType' value='${speciesInstance.class.name}'>
                </form>
            </div>
            </g:if>
        </div>
    </div>
</div>		
