<%@page import="species.Resource"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.Species"%>
<div>
    <g:if test="${resourceListType != 'fromRelatedObv' && resourceListType != 'fromSpeciesField' && resourceListType != 'usersResource'}"
    <i class="icon-picture"></i><span>Upload photos of a
        single observation and species and rate images inorder to order them.</span>

    </g:if>
    <g:if test="${resourceListType == 'usersResource'}"
    <i class="icon-picture"></i><span style="font-weight:bold;margin-left:2px;">My Uploads</span> <span> (Uploaded media will be stored for a period of 20 days. Media with red border will be deleted in 1 day.)</span>
    <a style="margin-bottom:3px;" class="pull-right sortMediaOnExif btn" onclick='sortMediaOnExif()'>Sort by EXIF date</a>
    </g:if>

    <div class="resources control-group imagesListWrapper ${hasErrors(bean: observationInstance, field: 'resource', 'error')}">
        <g:if test="${resourceListType == 'usersResource'}">
        <%
            /*
            def firstRes = resList[0]
            def fName = firstRes.fileName
            def resDir = fName ?  fName.substring(0, fName.lastIndexOf("/")) : ""
            */
        %>
        <!--input type="hidden" name='resDir' value="${resDir}" /-->
        </g:if>
        <ul class="imagesList thumbwrap thumbnails"
            style='list-style: none; margin-left: 0px;'>
            <g:render template="/observation/addPhoto" model="['observationInstance':observationInstance, 'resList': resList, 'obvLinkList': obvLinkList , 'resourceListType': resourceListType, 'offset':offset, 'currentUser':currentUser]"/>
            <g:if test="${resourceListType == 'fromRelatedObv'}">
                <input type="hidden" class='relatedImagesOffset' name='relatedImagesOffset' value = ''/>
            </g:if>
            <input class="resourceListTypeFilled" type="hidden" name='resourceListType' value="${resourceListType}" /> 
            <g:if test="${resourceListType == 'fromRelatedObv'}" >
            <a style="margin-right: 5px;" class="relatedObvLoadMore btn" onclick='getNextRelatedObvImages("${observationInstance.id}", "${createLink(controller:'species',  action:'getRelatedObvForSpecies')}", "${resourceListType}" , this )'>Load More</a>
            </g:if>
        </ul>
        <div class="image-resources-msg help-inline">
            <g:renderErrors bean="${observationInstance}" as="list"
            field="resource" />
        </div>
    </div>
</div>

<r:script>
    $(document).ready(function(){
        filepicker.setKey("${grailsApplication.config.speciesPortal.observations.filePicker.key}");
        if("${resourceListType}" == "fromRelatedObv"){
            if(${resCount} == 0){
                $('#speciesImage-li0').removeClass('active');
                $('#speciesImage-li0').hide();
                $('#speciesImage-tab0').hide();
                $('#speciesImage-li1 a').tab('show');
                $('#speciesFieldImage-li0').removeClass('active');
                $('#speciesFieldImage-li0').hide();
                $('#speciesFieldImage-tab0').hide();
                $('#speciesFieldImage-li1 a').tab('show');

            }
            $(".relatedImagesOffset").val(${resCount});
        }
        if("${resourceListType}" == "fromSpeciesField"){
            if(${resCount} == 0){
                $('#speciesImage-li2').hide();
                $('#speciesImage-tab2').hide();
            }
        }
        $(".resourceListType").val($(".resourceListTypeFilled").val());
    });
</r:script>
