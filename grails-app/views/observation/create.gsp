<%@page import="species.Resource.ResourceType"%>
<%@page import="species.Resource"%>
<%@page import="species.License"%>
<%@page import="species.License.LicenseType"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.utils.Utils"%>

<html>
<head>
<g:set var="title" value="Observations"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="observations_create"/>
</head>
<body>
    <div class="observation_create">
        <div class="span12">
            <obv:showSubmenuTemplate model="['entityName':(params.action == 'edit' || params.action == 'update')?'Edit Observation':'Add Observation']"/>


            <%
            def form_id = "addObservation"
            def form_action = uGroup.createLink(action:'save', controller:'observation', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            def form_button_name = "Add Observation"
            def form_button_val = "Add Observation"
            if(params.action == 'edit' || params.action == 'update'){
            //form_id = "updateObservation"
            form_action = uGroup.createLink(action:'update', controller:'observation', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            form_button_name = "Update Observation"
            form_button_val = "Update Observation"
            }

            %>

            <form id="${form_id}" action="${form_action}" method="POST"
                class="form-horizontal">

                <div class="span12 super-section">
                    <div class="section">
                        <h3>What did you observe?</h3>

                        <g:render template="addPhoto" model="['observationInstance':observationInstance]"/>

                        <div class="span6" style="margin:0px";>
                            <g:render template="selectGroupHabitatDate" model="['observationInstance':observationInstance]"/>
                        </div>
                        <div class="span6 sidebar-section" style="margin-top:-5px;">
                            <g:if
                            test="${observationInstance?.fetchSpeciesCall() == 'Unknown'}">
                            <div id="help-identify" class="control-label">
                                <label class="checkbox" style="text-align: left;"> <input
                                    type="checkbox" name="help_identify" /> Help identify </label>
                            </div>
                            </g:if>
                            <reco:create />
                        </div>

                    </div>
                </div>


                <div class="span12 super-section" style="clear: both;">
                    <%
                    def obvInfoFeeder = lastCreatedObv ? lastCreatedObv : observationInstance
                    %>
                    <obv:showMapInput model="[observationInstance:observationInstance, userObservationInstanceList: totalObservationInstanceList, obvInfoFeeder:obvInfoFeeder, locationHeading:'Where did you find this observation?']"></obv:showMapInput>
                </div>
                <div class="span12 super-section"  style="clear: both">
                    <g:render template="addNotes" model="['observationInstance':observationInstance]"/>
                </div>

                <g:render template="postToUserGroups" model="['observationInstance':obervationInstance]"/>
                <div class="span12" style="margin-top: 20px; margin-bottom: 40px;">

                    <g:if test="${observationInstance?.id}">
                    <a href="${uGroup.createLink(controller:params.controller, action:'show', id:observationInstance.id)}" class="btn"
                        style="float: right; margin-right: 30px;"> Cancel </a>
                    </g:if>
                    <g:else>
                    <a href="${uGroup.createLink(controller:params.controller, action:'list')}" class="btn"
                        style="float: right; margin-right: 30px;"> Cancel </a>
                    </g:else>

                    <g:if test="${observationInstance?.id}">
                    <div class="btn btn-danger"
                        style="float: right; margin-right: 5px;">
                        <a
                            href="${uGroup.createLink(controller:'observation', action:'flagDeleted', id:observationInstance.id)}"
                            onclick="return confirm('${message(code: 'default.observatoin.delete.confirm.message', default: 'This observation will be deleted. Are you sure ?')}');">Delete
                            Observation </a>
                    </div>
                    </g:if>
                    <a id="addObservationSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> ${form_button_val} </a>

                    <div class="row control-group">
                        <label class="checkbox" style="text-align: left;"> 
                            <g:checkBox style="margin-left:0px;"
                            name="agreeTerms" value="${observationInstance?.agreeTerms}"/>
                            <span class="policy-text"> By submitting this form, you agree that the photos or videos you are submitting are taken by you, or you have permission of the copyright holder to upload them on creative commons licenses. </span></label>
                    </div>

                </div>
            </form>
            <%

            def obvTmpFileName = (observationInstance?.resource?.iterator()?.hasNext() ) ? (observationInstance.resource.iterator().next()?.fileName) : false 
            def obvDir = obvTmpFileName ?  obvTmpFileName.substring(0, obvTmpFileName.lastIndexOf("/")) : ""
            %>


            <form id="upload_resource" 
                title="Add a photo for this observation"
                method="post"
                class="${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">

                <span class="msg" style="float: right"></span>
                <input id="videoUrl" type="hidden" name='videoUrl'value="" />
                <input type="hidden" name='obvDir' value="${obvDir}" />
            </form>

        </div>
    </div>
</div>
<!--====== Template ======-->
<script id="metadataTmpl" type="text/x-jquery-tmpl">
    <li class="addedResource thumbnail">
    <div class='figure' style='height: 200px; overflow:hidden;'>
        <span> 
            <img id='image_{{>i}}' style="width:auto; height: auto;" src='{{>thumbnail}}' class='geotagged_image' exif='true'/> 
        </span>
    </div>

    <div class='metadata prop' style="position:relative; top:-30px;">
        <input name="file_{{>i}}" type="hidden" value='{{>file}}'/>
        <input name="url_{{>i}}" type="hidden" value='{{>url}}'/>
        <input name="type_{{>i}}" type="hidden" value='{{>type}}'/>
        <%def r = new Resource();%>
        <obv:rating model="['resource':r, class:'obvcreate', 'hideForm':true, index:1]"/>

        {{if type == '${ResourceType.IMAGE}'}}
        <div id="license_div_{{>i}}" class="licence_div pull-left dropdown">
            <a id="selected_license_{{>i}}" class="btn dropdown-toggle btn-mini" data-toggle="dropdown">
                <img src="${resource(dir:'images/license',file:'cc_by.png', absolute:true)}" title="Set a license for this image"/>
                <b class="caret"></b>
            </a>
            <ul id="license_options_{{>i}}" class="dropdown-menu license_options">
                <span>Choose a license</span>
                <g:each in="${species.License.list()}" var="l">
                <li class="license_option" onclick="$('#license_{{>i}}').val($.trim($(this).text()));$('#selected_license_{{>i}}').find('img:first').replaceWith($(this).html());">
                <img src="${resource(dir:'images/license',file:l?.name.getIconFilename()+'.png', absolute:true)}"/><span style="display:none;">${l?.name?.value}</span>
                </li>
                </g:each>
            </ul>
            <input id="license_{{>i}}" type="hidden" name="license_{{>i}}" value="CC BY"></input>
        </div>	
        {{/if}}
    </div>
    <div class="close_button" onclick="removeResource(event, {{>i}});$('#geotagged_images').trigger('update_map');"></div>
    </li>

</script>

<script type="text/javascript" src="//api.filepicker.io/v1/filepicker.js"></script>
	
<r:script>
	
    var add_file_button = '<li id="add_file" class="addedResource" style="display:none;z-index:10;"><div id="add_file_container"><div id="add_image"></div><div id="add_video" class="editable"></div></div><div class="progress"><div id="translucent_box"></div><div id="progress_bar"></div ><div id="progress_msg"></div ></div></li>';



$(document).ready(function(){
     <%
           if(observationInstance?.group) {
           out << "jQuery('#group_${observationInstance.group.id}').addClass('active');";
           }
           if(observationInstance?.habitat) {
           out << "jQuery('#habitat_${observationInstance.habitat.id}').addClass('active');";
           }
    %>


    $('#attachFiles').change(function(e){
        $('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
        $("#iemsg").html("Uploading... Please wait...");
    });

    $('#upload_resource').ajaxForm({ 
        url:'${g.createLink(controller:'observation', action:'upload_resource')}',
        dataType: 'xml',//could not parse json wih this form plugin 
        clearForm: true,
        resetForm: true,
        type: 'POST',

        beforeSubmit: function(formData, jqForm, options) {
            $("#addObservationSubmit").addClass('disabled');
            return true;
        }, 
        /*xhr: function() {  // custom xhr
          myXhr = $.ajaxSettings.xhr();
          if(myXhr.upload){ // check if upload property exists
          myXhr.upload.addEventListener('progress', progressHandlingFunction, false); // for handling the progress of the upload
          }
          return myXhr;
          },*/
        success: function(responseXML, statusText, xhr, form) {
            $("#addObservationSubmit").removeClass('disabled');
            $(form).find("span.msg").html("");
            $(".progress").css('z-index',90);
            $('#progress_msg').html('');
            $("#iemsg").html("");
            //var rootDir = '${grailsApplication.config.speciesPortal.observations.serverURL}'
            //var rootDir = '${Utils.getDomainServerUrlWithContext(request)}' + '/observations'
            var obvDir = $(responseXML).find('dir').text();
            var obvDirInput = $('#upload_resource input[name="obvDir"]');
            if(!obvDirInput.val()){
                $(obvDirInput).val(obvDir);
            }
            var images = []
                var metadata = $(".metadata");
            var i = 0;
            if(metadata.length > 0) {
                var file_id = $(metadata.get(-1)).children("input").first().attr("name");
                i = parseInt(file_id.substring(file_id.indexOf("_")+1));
            }
            $(responseXML).find('resources').find('res').each(function() {
                var fileName = $(this).attr('fileName');
                var type = $(this).attr('type');					
                //var thumbnail = rootDir + obvDir + "/" + fileName.replace(/\.[a-zA-Z]{3,4}$/, "${grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix}");
                images.push({i:++i, file:obvDir + "/" + fileName, url:$(this).attr('url'), thumbnail:$(this).attr('thumbnail'), type:type, title:fileName});
            });

            var html = $( "#metadataTmpl" ).render( images );
            var metadataEle = $(html)
                metadataEle.each(function() {
                    $('.geotagged_image', this).load(function(){
                        update_geotagged_images_list($(this));		
                    });
                    var $ratingContainer = $(this).find('.star_obvcreate');
                    rate($ratingContainer)
                })
            $( "#imagesList li:last" ).before (metadataEle);

            /*if (navigator.appName.indexOf('Microsoft') == -1) {
              $( "#imagesList" ).append (add_file_button);
              }*/
            $( "#add_file" ).fadeIn(3000);
            $("#image-resources-msg").parent(".resources").removeClass("error");
            $("#image-resources-msg").html("");
            $("#upload_resource input[name='resources']").remove();
            $('#videoUrl').val('');
            $('#add_video').editable('setValue','', false);		
        }, error:function (xhr, ajaxOptions, thrownError){
            var successHandler = this.success, errorHandler;
            handleError(xhr, ajaxOptions, thrownError, successHandler, function(data) {
                if(data && data.status == 401) {
                    $('#upload_resource').submit();
                    return; 
                }
                $("#addObservationSubmit").removeClass('disabled');
                $("#upload_resource input[name='resources']").remove();
                $('#videoUrl').val('');
                $(".progress").css('z-index',90);
                $('#add_video').editable('setValue','', false);
                //xhr.upload.removeEventListener( 'progress', progressHandlingFunction, false); 

                var response = $.parseJSON(xhr.responseText);
                if(response.error){
                    $("#image-resources-msg").parent(".resources").addClass("error");
                    $("#image-resources-msg").html(response.error);
                }

                var messageNode = $(".message .resources");
                if(messageNode.length == 0 ) {
                    $("#upload_resource").prepend('<div class="message">'+(response?response.error:"Error")+'</div>');
                } else {
                    messageNode.append(response?response.error:"Error");
                }


            });
        } 



    });  


    filepicker.setKey("${grailsApplication.config.speciesPortal.observations.filePicker.key}");
});


</r:script>

</body>
</html>
