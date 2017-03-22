<%@page import="java.util.Arrays"%>
<%@page import="species.trait.Trait"%>
<%@page import="species.trait.TraitValue"%>
<%@page import="species.trait.TraitTranslation"%>
<%@page import="species.trait.TraitValueTranslation"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.Classification"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>

<html>
<head>
<g:set var="title" value="${g.message(code:'create.title.trait')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>


<style>
.ui-autocomplete{
max-width: 707px;
margin-top: 30px;
}
ul.tagit{
    margin-left:0px;
}
.save, .cancel {
display:none;
}
</style>
</head>
<body>
    <% 
    def form_action = uGroup.createLink(action:'update', controller:'trait')
    def form_title = "${g.message(code:'title.create.trait')}"
    def form_button_name = "${g.message(code:'title.trait.add')}"
    def form_button_val = "Add Trait"

    if(params.action == 'edit' || params.action == 'update'){
        
            def traitTrans = TraitTranslation.findByTraitAndLanguage(traitInstance,userLanguage);
            println traitTrans?.dump()
            println
            traitInstance.name = (traitTrans?.name)?:'';
            traitInstance.description = (traitTrans?.description)?:'';
            traitInstance.source = (traitTrans?.source)?:'';
            
            form_action = uGroup.createLink(action:'update', controller:'trait', id:traitInstance.id)
            form_button_name = "${g.message(code:'button.update.trait')}"
            form_button_val = "Update Trait"
            form_title = "${g.message(code:'link.update.trait')}"
    }
    %>
    <div class="span12 observation_create">
        <form id="traitForm" action="${form_action}" method="POST"
            onsubmit="document.getElementById('traitFormSubmit').disabled = 1;"
            class="form-horizontal">

            <div class="span12 super-section" style="margin-left: 150px;">
                <div class="section">
                    <div
                        class="control-group ${hasErrors(bean: traitInstance, field: 'name', 'error')}">
                        <label class="control-label" for="type"><g:message
                            code="trait.name.label" default="${g.message(code:'trait.name.label')}" /><span class="req">*</span></label>
                        <div class="controls">
                            <input type="text" class="input-block-level" name="name"
                            placeholder="${g.message(code:'placeholder.trait.enter.name')}"
                            value="${traitInstance?.name}" required />
                        </div>

                    </div>
                    <g:if test="${params.action=='create'}">
                    <div
                        class="control-group ${hasErrors(bean: traitInstance, field: 'DataType', 'error')}">
                        <label class="control-label" for="title"><g:message
                            code="trait.datatype.label" default="${g.message(code:'trait.datatype.label')}" /><span class="req">*</span></label>
                        <div class="controls">
                            <g:select name="datatype" class="input-block-level"
                            placeholder="${g.message(code:'placeholder.document.select')}"
                            from="${species.trait.Trait$DataTypes?.values()}"
                            keys="${species.trait.Trait$DataTypes?.values()*.value()}"
                            value="${traitInstance?.dataTypes?.value()}" />
                        </div>
                        </div> 
                        </g:if>

                    <g:if test="${params.action=='create'}">
                    <div
                        class="control-group ${hasErrors(bean: traitInstance, field: 'traittypes', 'error')}">
                        <label class="control-label" for="traittypes"><g:message code="trait.traittypes.label" /></label>
                        <div class="controls">
                             <g:select name="traittype" class="input-block-level"
                            placeholder="${g.message(code:'placeholder.document.select')}"
                            from="${species.trait.Trait$TraitTypes?.values()}"
                            keys="${species.trait.Trait$TraitTypes?.values()*.value()}"
                            value="${traitInstance?.traitTypes?.value()}" />

                        </div>
                    </div> 
                    <input type="hidden" id="valueCount" name="valueCount" />
                    </g:if>

                <div class="control-group sciNameDiv" style="margin-top:5px;">
                <label for="recommendationVote" class="control-label"> <g:message
                    code="observation.recommendationVote.label" default="${g.message(code:'trait.taxon.name')}" />
                </label>
                <div class="controls">
                    <div class="textbox nameContainer">
                        <g:set var="species_sn_lang" value="${species_sn_lang}" />
                     <!--   <g:each in="${traitInstance.taxon}" var="taxon" status="i">
                        <input type="text" name="recoName[]" class="recoName input-block-level" value="${taxon?.name}" rel="${g.message(code:'placeholder.suggest.species.name')}"
                            placeholder='${g.message(code:"editrecomendation.placeholder.scientific")}'
                            class="input-block-level ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}"/>    
                        <div class='nameSuggestions' style='display: block;'></div>
                        <input type="hidden" name="taxonId[]" class="taxonId" value="${taxon?.id}"/>
                        </g:each> -->
                        <ul id="taxonName" class="taxonName" rel="${g.message(code:'placeholder.add.tags')}">
                        <g:each in="${traitInstance.taxon}" var="taxon" status="i">
                            <li><span class="tagit-label">${taxon?.name+' ('+taxon?.id+'-'+(taxon?.status)+'-'+(taxon?.position)+')'}</span></li>
                        </g:each>
                        </ul>
                    </div>
                </div>
            </div>

                    <div
                        class="control-group ${hasErrors(bean: traitInstance, field: 'value', 'error')}">
                        <label class="control-label" for="value"><g:message code="trait.value.label" /></label>
                        <div class="controls">
                        <%
                        def value=[];
                        value=TraitValue.findAllByTrait(Trait.findById(traitInstance.id)) ;
                        %>

                        <table class="table" id="valueTable">
                        <tr>
                        <th>Value</th>
                        <th>Description</th>
                        <th>Source</th>
                        <th>Icon</th>
                      
                        </tr>
                        <g:each in="${value}" var="val" status="i">
                        <g:if test="${val.traitValueTranslations}">
                        <% def traitValueTrans = TraitValueTranslation.findByTraitValueAndLanguage(val,userLanguage); %>
                        <g:if test="${traitValueTrans}">

                        <% 
                        val.value = (traitValueTrans?.value)?:'';
                        val.description = (traitValueTrans?.description)?:'';
                        val.source = (traitValueTrans?.source)?:'';
                        %>
                        <tr>
                        
                        <td>
                            <input type="hidden" name="traitValueId" id="traitValueId_${i}" value="${val.id}" />
                           <g:textField name="value_${i}" id="value_${i}" class="input-block-level"
                            value="${val.value}"
                            placeholder="${g.message(code:'placeholder.trait.enter.values')}" style="display:none;" /> 
                            <div  id="valuelable_${i}">${val.value}</div>
                        </td>
                        <td>
                            <g:textField name="traitDesc_${i}" id="traitDesc_${i}" class="input-block-level"
                            value="${val.description}"
                            placeholder="${g.message(code:'placeholder.trait.enter.description')}" style="display:none;"/>
                            <div id="traitDescLable_${i}">${val.description}</div>
                        </td>
                        <td>
                            <g:textField name="traitSource_${i}" class="input-block-level" id="traitSource_${i}"
                            value="${val.source}"
                            placeholder="${g.message(code:'placeholder.trait.enter.source')}" style="display:none;" />
                            <div id="traitSourceLable_${i}">${val.source}</div>
                        </td>

                        <td>
                            <%def thumbnail = val.icon%>
                            <a onclick="$('#attachFile').select()[0].click();return false;" style="postiion:relative;">
                            <img id="thumbnail" class="user-icon small_profile_pic" src="${val?.mainImage()?.fileName}" title="${val.value}" alt="${val.value}" />
                            <input class="icon" name="icon" id="icon_${i}" type="hidden" value='${thumbnail}' />
                            </a>
                        </td>
                        
                        <td>
                        <div> 
                         <a class="btn btn-primary edit" id="editValue" data-id="${i}"><i class="icon-edit icon-white"></i></a>
                         <a class="btn btn-success save addValue" id="addValue" data-id="${i}"><i class="icon-ok icon-white"></i></a> 
                         <a class="btn btn-danger cancel" id="removeValue" data-id="${i}"><i class="icon-remove icon-white"></i></a>
                         <a class="btn btn-danger delete" id="deleteValue" data-val="${val.id}"><i class="icon-remove icon-white"></i></a>
                         </div>
                        </td>
                        </tr>
                        </g:if>
                        </g:if>
                            </g:each>
                            </table>
                        <g:if test="${params.action=='edit'}">
                        <a class="btn btn-primary" id="addNewValue"><i class="icon-plus icon-white"></i></a>
                        </g:if>
                        <g:else>
                        <a class="btn btn-primary" id="createNewValue"><i class="icon-plus icon-white"></i></a>
                        </g:else>
                        </div>
                    </div>
                    <div
                        class="control-group ${hasErrors(bean: traitInstance, field: 'notes', 'error')}">
                            <label class="control-label" for="description"><g:message code="default.description.label" />
                            </label>
                        <div class="controls">
                            <textarea id="description" name="description" style="width:705px; height:75px;">${traitInstance?.description}</textarea>
            </div>
        </div>
                        <div
                        class="control-group ${hasErrors(bean: traitInstance, field: 'source', 'error')}">
                        <label class="control-label" for="value"><g:message code="trait.source.label" /></label>
                        <div class="controls">
                            <g:textField name="source" class="input-block-level"
                            value="${traitInstance.source }"
                            placeholder="${g.message(code:'placeholder.trait.enter.source')}" />
                        </div>
                    </div>
                        <div
                        class="control-group ${hasErrors(bean: traitInstance, field: 'fieldid', 'error')}">
                        <label class="control-label" for="value"><g:message code="trait.fieldid.label" /></label>
                        <div class="controls">
                            
                <ul id="fieldid" class="fieldid" rel="${g.message(code:'placeholder.add.tags')}">
                    <li><span class="tagit-label">${field}</span></li>
                </ul>

                        </div>
                    </div>

                    <div
            </ul>

                        </div>
                    </div>
                     <div
                        class="control-group ${hasErrors(bean: traitInstance, isNotObservationTrait: 'source', 'error')}">
                        <label class="control-label" for="value"><g:message code="trait.isNotObservationTrait.label" /></label>
                        <div class="controls">
                            <g:checkBox name="isNotObservationTrait" class="input-block-level" value="${traitInstance.isNotObservationTrait}"/>
                        </div>
                    </div>
                   <div
                        class="control-group ${hasErrors(bean: traitInstance, isParticipatory: 'source', 'error')}">
                        <label class="control-label" for="value"><g:message code="trait.isParticipatory.label" /></label>
                        <div class="controls">
                            <g:checkBox name="isParticipatory" class="input-block-level" value="${traitInstance.isParticipatory}" />
                        </div>
                    </div>
                   <div
                        class="control-group ${hasErrors(bean: traitInstance, showInObservation: 'source', 'error')}">
                        <label class="control-label" for="value"><g:message code="trait.showInObservation.label" /> </label>
                        <div class="controls">                        
                            <g:checkBox name="showInObservation" class="input-block-level" value="${traitInstance.showInObservation}" />
                        </div>
                    </div>
                             </div>
                                    </div>
                                    <div class="span12 submitButtons">

                                        <g:if test="${traitInstance?.id}">
                                        <a
                                            href="${uGroup.createLink(controller:'trait', action:'show', id:traitInstance.id)}"
                                            class="btn" style="float: right; margin-right: 30px;"><g:message code="button.cancel" /> 
                                        </a>
                                        </g:if>

                                        <a id="traitFormSubmit" class="btn btn-primary"
                                            style="float: right; margin-right: 5px;"> ${form_button_name}
                                        </a>
                                    </div>
                                </form>

                <form class="upload_resource" enctype="multipart/form-data"
                    title="Upload profile picture" method="post" style="visibility:hidden;">
                    <input type="file" id="attachFile" name="resources" accept="image/*"/> 
                    <span class="msg" style="float: right"></span> 
                    <input type="hidden" name='dir' value="${userGroupDir}" />
                </form>
                            </div>
                            </div>
<%def alert_msg=g.message(code:'document.error.message')%>

<asset:script>   
    $(document).ready (function() {
        $('.edit').live ('click', function () {
        $(this).hide();
        $(this).siblings('.save, .cancel').show();
        $(this).siblings('.delete').hide();
        var id=$(this).data("id");
        $('#valuelable_'+id).hide();
        $('#value_'+id).show();
        $('#traitDescLable_'+id).hide();
        $('#traitDesc_'+id).show();
        $('#traitSourceLable_'+id).hide();
        $('#traitSource_'+id).show();
    });

        $('.cancel').live ('click', function () {
            $(this).siblings('.edit').show();
            $(this).siblings('.delete').show();
            $(this).siblings('.save').hide();
            $(this).hide();
            var id=$(this).data("id");
            $('#valuelable_'+id).show();
            $('#value_'+id).hide();
            $('#traitDescLable_'+id).show();
            $('#traitDesc_'+id).hide();
            $('#traitSourceLable_'+id).show();
            $('#traitSource_'+id).hide();
        });
        
        $('.save').live ('click', function () {
            $(this).siblings('.edit').show();
            $(this).siblings('.cancel').hide();
            $(this).hide();
            var id=$(this).data("id");
            $('#valuelable_'+id).show();
            $('#value_'+id).hide();
            $('#traitDescLable_'+id).show();
            $('#traitDesc_'+id).hide();
            $('#traitSourceLable_'+id).show();
            $('#thumbnail').show();
            $('#traitSource_'+id).hide();
            $('#valuelable_'+id).text($('#value_'+id).val());
            $('#traitDescLable_'+id).text($('#traitDesc_'+id).val());
            $('#traitSourceLable_'+id).text($('#traitSource_'+id).val());

        });

    $('#addNewValue').click(function(){
        var rowCount = $('#valueTable tr').length;
            $('#valueTable tr:last').after("<tr><td><input type='textbox' id='value_"+ rowCount +"'class ='input-block-level' /></td>"+
                                      "<td><input type='textbox' id='traitDesc_"+ rowCount +"'class ='input-block-level' /></td>"+
                                      "<td><input type='textbox' id='traitSource_"+ rowCount +"'class ='input-block-level' /></td>"+
                                      "<td><a onclick=$('#attachFile').select()[0].click();return false;><img id='thumbnail_"+rowCount +"' class='user-icon small_profile_pic' src='' title='thumbnai' alt='Browse' /></a>"+
                                      "<input class='icon' name='icon' id='icon_${i}' type='hidden' value='${thumbnail}' /></td>"+
                                      "<td><a class='btn btn-success addValue' id='addValue' data-id='"+ rowCount +"'><i class='icon-ok icon-white'></i></a> </td>"+
                                      "<td><a class='btn btn-danger removeRow' id='removeValue' data-id='"+rowCount+"'><i class='icon-remove icon-white'></i></a></td></tr>");
    });

    $('#createNewValue').click(function(){
        var rowCount = $('#valueTable tr').length;
        $('#valueCount').val(rowCount);
             $('#valueTable tr:last').after("<tr><td><input type='textbox' name='value_"+ rowCount +"' id='value_"+ rowCount +"'class ='input-block-level' /></td>"+
                                      "<td><input type='textbox' name='traitDesc_"+ rowCount +"' id='traitDesc_"+ rowCount +"'class ='input-block-level' /></td>"+
                                      "<td><input type='textbox' name='traitSource_"+ rowCount +"' id='traitSource_"+ rowCount +"'class ='input-block-level' /></td>"+
                                      "<td><a onclick=$('#attachFile').select()[0].click();return false;><img id='thumbnail_"+rowCount +"' class='user-icon small_profile_pic' src='' title='thumbnai' alt='Browse' /></a>"+
                                      "<input class='icon' name='icon_"+ rowCount +"' id='icon_${i}' type='hidden' value='${thumbnail}' /></td>"+
                                      "<td><a class='btn btn-danger removeRow' id='removeValue' data-id='"+rowCount+"'><i class='icon-remove icon-white'></i></a></td></tr>");
    });

    $('.removeRow').live ('click', function (){
        $(this).closest ('tr').remove ();
        $('#valueCount').val($('#valueCount').val()-1);
    });

    $('.delete').live ('click', function (){
        var test="${message(code: 'default.trait.delete.confirm.message', default: 'This Trait value will be deleted. Are you sure ?')}";
        if(confirm(test)){
            var traitValueId=$(this).data("val");
            alert(traitValueId);
                        $.ajax({ 
                        url:'${uGroup.createLink(controller:'trait', action:'deleteValue')}',
                        data:{id:traitValueId},
                        success: function(data, statusText, xhr, form) {
                        
                        },
                        error:function (xhr, ajaxOptions, thrownError){
                            console.log('error');
                            return false;
                        }
                        });
           $(this).closest ('tr').remove ();
        }           
    });


    window.params.trait.updateTraitValueUrl="${uGroup.createLink(controller:'trait', action:'updateTraitValue', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, params:[actionType:params.action])}";
    $("#traitFormSubmit").click(function(){
        $("#traitForm").submit();
    });

            $('.addValue').live ('click', function (){
                var loopId=$(this).data("id");
                var value=$('#value_'+loopId).val();
                var description=$('#traitDesc_'+loopId).val();
                var source=$('#traitSource_'+loopId).val();
                var traitValueId=$('#traitValueId_'+loopId).val();
                var icon=$('#icon_'+loopId).val();
               
                            $.ajax({ 
                        url:'${uGroup.createLink(controller:'trait', action:'updateTraitValue', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress,traitInstance:traitInstance.id)}',
                        data:{value:value,description:description,source:source,traitValueId:traitValueId,icon:icon},
                        success: function(data, statusText, xhr, form) {
                        
                        },
                        error:function (xhr, ajaxOptions, thrownError){
                            console.log('error');
                            return false;
                        }
                        });
                    $(this).closest ('tr').remove ();
                      $('#valueTable tr:last').after("<tr><td><div>"+ value +" </div></td>"+
                                      "<td><div>"+ description+"</div></td>"+
                                      "<td><div>"+ source +"</td><td><a class='btn btn-primary edit' id='editValue' data-id='${i}'><i class='icon-edit icon-white'></i></a></td></tr>");

                });

    $.ajax({
        url:window.params.getDataColumnsDB,
                dataType:'JSON',
                success:function(data){
                        $("#fieldid").tagit({
                        availableTags:data,
                        fieldName: 'fieldid', 
                        showAutocompleteOnFocus: false,
                        allowSpaces: true,
                        triggerKeys:['comma'], 
                        beforeTagAdded: function(event, ui) {
                            if(data.indexOf(ui.tagLabel) == -1)
                            {
                                return false;
                            }
                            if(ui.tagLabel == "not found")
                            {
                                return false;
                            }
                        }
                    });
                  }
    });

            $(".taxonName").tagit({
                select:true, 
                allowSpaces:true,
                placeholderText:$(".obvCreateTags").attr('rel'),//'Add some tags',
                fieldName: 'taxonName', 
                maxTags  : 1,
                autocomplete:{
                    source: '/trait/taxonTags'
                }, 
                triggerKeys:['enter', 'comma', 'tab'], 
                maxLength:30

        });

//image icon upload
        if (navigator.appName.indexOf('Microsoft') != -1) {
            $('.upload_resource').css({'visibility':'visible'});
        } else {
            $('.upload_resource').css({'visibility':'hidden'});
        }
        
    $('#attachFile').change(function(e){
            $('.upload_resource').find("span.msg").html("Uploading... Please wait...");
            var onUploadResourceSuccess =  function(responseXML, statusText, xhr, form) {
                if($(responseXML).find('success').text() == 'true') {
                    $(form).find("span.msg").html("");
                    var rootDir = '${grailsApplication.config.speciesPortal.traits.serverURL}'
                    var dir = $(responseXML).find('dir').text();
                    var dirInput = $('.upload_resource input[name="dir"]');
                    if(!dirInput.val()){
                        $(dirInput).val(dir);
                    }
                    
                    $(responseXML).find('resources').find('image').each(function() {
                        var rowCount = $('#valueTable tr').length;
                        rowCount=rowCount-1;
                        var file = dir + "/" + $(this).attr('fileName');
                        var thumbnail = rootDir + file.replace(/\.[a-zA-Z]{3,4}$/, "${grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix}");
                        $(".icon").val(file);
                    
                        $('#thumbnail_'+rowCount).attr("src", thumbnail);
                    });
                    $("#image-resources-msg").parent(".resources").removeClass("error");
                    $("#image-resources-msg").html("");
                } else {
                    onUploadResourceError(xhr);
                }
            }

            var onUploadResourceError = function (xhr, ajaxOptions, thrownError){
                    //successHandler is used when ajax login succedes
                    var successHandler = onUploadResourceSuccess, errorHandler;
                    handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
                        var response = $(xhr.responseXML).find('msg').text();
                        if(response){
                            $("#image-resources-msg").parent(".resources").addClass("error");
                            $("#image-resources-msg").html(response);
                        }
                        
                        var messageNode = $(".message .resources");
                        if(messageNode.length == 0 ) {
                            $(".upload_resource").prepend('<div class="message">'+(response?response:"Error")+'</div>');
                        } else {
                            messageNode.append(response?response:"Error");
                        }
                    });
                } 
            $('.upload_resource').ajaxSubmit({ 
                url:'/trait/upload_resource',
                dataType : 'xml',
                clearForm: true,
                resetForm: true,
                type: 'POST',
                success:onUploadResourceSuccess, 
                error:onUploadResourceError
            });
        });

    });
</asset:script>
</body>
</html>
