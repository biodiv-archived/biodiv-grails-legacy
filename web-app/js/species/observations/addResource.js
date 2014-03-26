function removeResource(event, imageId) {
    var targ;
    if (!event) var event = window.event;
    if (event.target) targ = event.target;
    else if (event.srcElement) targ = event.srcElement; //for IE

    $(targ).parent('.addedResource').remove();
    $(".image_"+imageId).remove();
}


$(document).ready(function(){
    $('.dropdown-toggle').dropdown();

    var uploadResource = new UploadResource($('.upload_resource').parent());
    /**
     * upload_resource & FilePicker
     */
    /*
    function filePick() {
        var onSuccess = function(FPFiles){
            $.each(FPFiles, function(){
                $('<input>').attr({
                    type: 'hidden',
                    name: 'resources',
                    value:JSON.stringify(this)
                }).appendTo('.upload_resource');
            });
            uploadResource.submit();
        };
        filepicker.pickMultiple({
            maxSize: 104857600,
            services:['COMPUTER', 'FACEBOOK', 'FLICKR', 'PICASA', 'GOOGLE_DRIVE', 'DROPBOX'],
            mimetypes: ['image/*'] }, onSuccess, function(FPError){ console.log(FPError.toString()); });

    }
*/

    function newPicker() {
        google.load('picker', '1', {"callback" : createPicker});
    }

        // Create and render a Picker object for searching images.
        var createPicker = function () {
        var picker = new google.picker.PickerBuilder().
        addView(google.picker.ViewId.YOUTUBE).
        setCallback(pickerCallback).
        build();
        picker.setVisible(true);
        //$(".picker-dialog-content").prepend("<div id='anyVideoUrl' class='editable'></div>");
        //$('#anyVideoUrl').editable(addVideoOptions);
        }

        // A simple callback implementation.
        function pickerCallback(data) {
            var url = 'nothing';
            if (data[google.picker.Response.ACTION] == google.picker.Action.PICKED) {
            var doc = data[google.picker.Response.DOCUMENTS][0];
            url = doc[google.picker.Document.URL];
            if(url) {
                $('#videoUrl').val(url);
                uploadResource.submit();
            }
        }
        }


        $('#add_image').bind('click', filePick);

        var onVideoAddSuccess = function(params) {
        var d = new $.Deferred;
        if(!params.value) {
        return d.reject('This field is required'); //returning error via deferred object
        } else {
        $('#videoUrl').val(params.value);
        uploadResource.submit();
        d.resolve();
        }
        return d.promise()  
        }

        var onVideoAddValidate = function(value) {
        if($.trim(value) == '') {
        return 'This field is required';
        }
        }

        var addVideoOptions = {
        type: 'text',
        mode:'popup',
        emptytext:'',
        placement:'bottom',
        url: onVideoAddSuccess,
        validate : onVideoAddValidate,
        title: 'Enter YouTube watch url like http://www.youtube.com/watch?v=v8HVWDrGr6o'
        }

        $('#add_video').editable(addVideoOptions);
        $.each($('.star_obvcreate'), function(index, value){
        rate($(value));
    });


    /**
    */
    $('#attachFiles').change(function(e){
        uploadResource.submit();
    });


});

/**
  @class uploadResource
 **/
(function ($) {
    "use strict";

    var UploadResource = function (ele) {
        this.$ele = $(ele);
        this.initForm();
    }

    UploadResource.prototype = {
        initForm : function() {
            var me = this;
            me.$ele.find(".upload_resource").ajaxForm({ 
                url:window.params.observation.uploadUrl,
                dataType: 'xml',//could not parse json wih this form plugin 
                clearForm: true,
                resetForm: true,
                type: 'POST',
                beforeSubmit: function(formData, jqForm, options) {
                    me.$ele.find("#addObservationSubmit").addClass('disabled');
                    return true;
                }, 
                success : me.onSuccess,
                error : me.onError
            });
        },
        filePick : function() {
            var me = this;
            var onSuccess = function(FPFiles){
            $.each(FPFiles, function(){
                $('<input>').attr({
                    type: 'hidden',
                    name: 'resources',
                    value:JSON.stringify(this)
                }).appendTo(me.$ele.find(".upload_resource"));
            });
                uploadResource.submit();
            };
            filepicker.pickMultiple({
            maxSize: 104857600,
            services:['COMPUTER', 'FACEBOOK', 'FLICKR', 'PICASA', 'GOOGLE_DRIVE', 'DROPBOX'],
            mimetypes: ['image/*'] }, onSuccess, function(FPError){ console.log(FPError.toString()); });
        },
        submit : function() {
            this.$ele.find(".upload_resource").submit().find("span.msg").html("Uploading... Please wait...");
            this.$ele.find(".iemsg").html("Uploading... Please wait...");
            this.$ele.find(".progress").css('z-index',110);
            this.$ele.find('.progress_msg').html('Uploading ...');
        },
        onSuccess : function(responseXML, statusText, xhr, form) {
            me.$ele.find("#addObservationSubmit").removeClass('disabled');
            $(form).find("span.msg").html("");
            me.$ele.find(".progress").css('z-index',90);
            me.$ele.find('.progress_msg').html('');
            me.$ele.find(".iemsg").html("");
            //var rootDir = '${grailsApplication.config.speciesPortal.observations.serverURL}'
            //var rootDir = '${Utils.getDomainServerUrlWithContext(request)}' + '/observations'
            var obvDir = $(responseXML).find('dir').text();
            var obvDirInput = $('#upload_resource input[name="obvDir"]');
            if(!obvDirInput.val()){
                $(obvDirInput).val(obvDir);
            }
            var images = [];
            var metadata = me.$ele.find(".metadata");
            var i = 0;
            if(metadata.length > 0) {
                var file_id = $(metadata.get(-1)).children("input").first().attr("name");
                i = parseInt(file_id.substring(file_id.indexOf("_")+1));
            }
            $(responseXML).find('resources').find('res').each(function() {
                var fileName = $(this).attr('fileName');
                var type = $(this).attr('type');					
                images.push({i:++i, file:obvDir + "/" + fileName, url:$(this).attr('url'), thumbnail:$(this).attr('thumbnail'), type:type, title:fileName});
            });

            var html = $( "#metadataTmpl" ).render( images );
            var metadataEle = $(html);
            metadataEle.each(function() {
                me.$ele.find('.geotagged_image', this).load(function(){
                    update_geotagged_images_list($(this));		
                });
                var $ratingContainer = $(this).find('.star_obvcreate');
                rate($ratingContainer)
            })

            me.$ele.find(".imagesList li:first" ).after (metadataEle);
            me.$ele.find(".add_file" ).fadeIn(3000);
            me.$ele.find(".image-resources-msg").parent(".resources").removeClass("error");
            me.$ele.find(".image-resources-msg").html("");
            me.$ele.find(".upload_resource input[name='resources']").remove();
            me.$ele.find('.videoUrl').val('');
            me.$ele.find('.add_video').editable('setValue','', false);		
        },

        onError : function (xhr, ajaxOptions, thrownError){
            var successHandler = this.success, errorHandler;
            var me = this;
            handleError(xhr, ajaxOptions, thrownError, successHandler, function(data) {
                if(data && data.status == 401) {
                    me.$ele.find('.upload_resource').submit();
                    return; 
                }
                me.$ele.find("#addObservationSubmit").removeClass('disabled');
                 me.$ele.find(".upload_resource input[name='resources']").remove();
                me.$ele.find('.videoUrl').val('');
                me.$ele.find(".progress").css('z-index',90);
                me.$ele.find('.add_video').editable('setValue','', false);
                //xhr.upload.removeEventListener( 'progress', progressHandlingFunction, false); 

                var response = $.parseJSON(xhr.responseText);
                if(response.error){
                    me.$ele.find(".image-resources-msg").parent(".resources").addClass("error");
                    me.$ele.find(".image-resources-msg").html(response.error);
                }

                var messageNode = me.$ele.find(".message .resources");
                if(messageNode.length == 0 ) {
                    me.$ele.find(".upload_resource").prepend('<div class="message">'+(response?response.error:"Error")+'</div>');
                } else {
                    messageNode.append(response?response.error:"Error");
                }

            });
        } 
    }

}(window.jQuery)); 



