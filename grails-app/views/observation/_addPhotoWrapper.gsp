<%@page import="species.Resource"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.Species"%>
<div>
    <g:if test="${resourceListType != 'fromRelatedObv'}">
    <i class="icon-picture"></i><span>Upload photos of a
        single observation and species and rate images inorder to order them.</span>

    </g:if>
    <div
        class="resources control-group ${hasErrors(bean: observationInstance, field: 'resource', 'error')}">
        <ul class="imagesList" class="thumbwrap thumbnails"
            style='list-style: none; margin-left: 0px;'>
            <g:render template="/observation/addPhoto" model="['observationInstance':observationInstance, 'resList': resList, 'obvLinkList': obvLinkList , 'resourceListType': resourceListType, 'offset':offset]"/>
            <g:if test="${resourceListType == 'fromRelatedObv'}">
                <input type="hidden" id='relatedImagesOffset' name='relatedImagesOffset' value = ''/>
            </g:if>
            <input type="hidden" name='resourceListType' value="${resourceListType}" /> 
            <g:if test="${resourceListType == 'fromRelatedObv'}" >
            <a class="btn" style="float: right; margin-right: 5px;" id="relatedObvLoadMore" onclick='getNextRelatedObvImages("${observationInstance.id}", "${createLink(controller:'species',  action:'getRelatedObvForSpecies')}", "${resourceListType}" )'>Load More</a>
            </g:if>
        </ul>
        <div id="image-resources-msg" class="help-inline">
            <g:renderErrors bean="${observationInstance}" as="list"
            field="resource" />
        </div>
    </div>
</div>

<r:script>
    $(document).ready(function(){
        filepicker.setKey("${grailsApplication.config.speciesPortal.observations.filePicker.key}");
        $("#relatedImagesOffset").val(1); 
    });
</r:script>
