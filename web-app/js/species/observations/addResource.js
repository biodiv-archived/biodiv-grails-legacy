function removeResource(event, imageId) {
    var targ;
    if (!event) var event = window.event;
    if (event.target) targ = event.target;
    else if (event.srcElement) targ = event.srcElement; //for IE
    else {}
    if(($(targ).closest(".imagesList").size() == 1) && ($( "input[name='resType']" ).val() == "species.auth.SUser")){
        var resId =  $(targ).parent('.addedResource').find(".resId").val();
        var fileName = $(targ).parent('.addedResource').find(".fileName").val();
        var resDeleteUrl = window.params.resDeleteUrl;
        $.ajax({
            url: resDeleteUrl,
            dataType: "json",
            data: {resId:resId , fileName:fileName},	
            success: function(data) {
                if(data.status){
                    alert(window.i8ln.observation.addResource.md)
                } else {
                    alert("Deletion failed - Uploaded media has no ID, refresh and try!!")
                } 
            }, error: function(xhr, status, error) {
                alert(xhr.responseText);
            }
        });
    }
    $(targ).parent('.addedResource').remove();
    $(targ).find(".image_"+imageId).remove();
    if($( "input[name='resType']" ).val() == "species.auth.SUser") {
        $(".image_"+imageId).first().closest(".addedResource").css('opacity', '1');
        $(".image_"+imageId).first().closest(".addedResource").draggable('enable');
    }

}

function attachThumbnailAndProcess(me , images) {
    var html = $( "#metadataTmpl" ).render( images );
    var metadataEle = $(html);
    if($( "input[name='resType']" ).val() == "species.participation.Observation") {
        metadataEle.each(function() {
            $('.geotagged_image', this).load(function(){
                var me = this;
                $.proxy(loadMapInput, $(".addObservation").find(".map_class"), $(me))();
                //$(".map_class").data('locationpicker').mapLocationPicker.update_geotagged_images_list($(this));		
            });
            var $ratingContainer = $(this).find('.star_obvcreate');
            rate($ratingContainer);
        });
    }
    me.$ele.find(".imagesList li:first" ).after (metadataEle);
    me.$ele.find(".add_file" ).fadeIn(3000);
    me.$ele.find(".image-resources-msg").parent(".resources").removeClass("error");
    me.$ele.find(".image-resources-msg").html("");
    me.$form.find("input[name='resources']").remove();
    me.$ele.find('.videoUrl').val('');
    me.$ele.find('.audioUrl').val('');
    me.$ele.find('.add_video').editable('setValue','', false);
    // me.$ele.find('.add_audio').editable('setValue','', false);		
    me.$ele.find('.add_video').editable('setValue','', false);	

    if($( "input[name='resType']" ).val() == "species.auth.SUser") {
        var count = $("input[name='lastUploaded']").val();
        var start = 0;
        var w = 1; 
        var end = start + w; 
        createResources(start, end, w, count);
        $("input[name='obvDir']").val('');
    }
}

function submitNextUpload(me) {
    var val = (me.start/me.uploadedFilesSize)*100;
        me.$ele.find(".mediaProgressBar").progressbar({
            value:val
        });
    if(me.start < me.uploadedFilesSize) {
        var count = 0;
        var FPF = me.uploadedFiles.slice(me.start, me.start + me.w);
        me.start = me.start + me.w;
        $.each(FPF, function(){
            $('<input>').attr({
                type: 'hidden',
                name: 'resources',
                value:JSON.stringify(this)
            }).appendTo(me.$form);
            count = count + 1;
        });
        if($( "input[name='resType']" ).val() == "species.auth.SUser") {
            $("input[name='obvDir']").val('');
            $("input[name='lastUploaded']").val(count);
        }
        me.submitRes();
    } else {
        me.$ele.find(".progress").css('z-index',90);
        me.$ele.find(".mediaProgressBar").progressbar("destroy");
        me.$ele.find('.progress_msg').html('');
        $(".sortMediaOnExif").removeClass("disabled"); 
    }
}

function getProcessedImageStatusInAjax(jobId, images, me) {
    if(!jobId) {
        console.log("NO JOB ID");
        return;
    }
    $.ajax({
        url:window.params.getProcessedImageUrl,
        dataType: "json",
        data:{jobId:jobId},
        success: function(data) {
            if(data.imageStatus == "Success") {
                //me.$ele.find(".progress").css('z-index',90);
                flag = false;
                attachThumbnailAndProcess(me, images);
                /*
                var html = $( "#metadataTmpl" ).render( images );
                var metadataEle = $(html);
                if($( "input[name='resType']" ).val() == "species.participation.Observation") {
                    metadataEle.each(function() {
                        $('.geotagged_image', this).load(function(){
                            var me = this;
                            $.proxy(loadMapInput, $(".addObservation").find(".map_class"), $(me))();
                            //$(".map_class").data('locationpicker').mapLocationPicker.update_geotagged_images_list($(this));		
                        });
                        var $ratingContainer = $(this).find('.star_obvcreate');
                        rate($ratingContainer);
                    });
                }
                me.$ele.find(".imagesList li:first" ).after (metadataEle);
                me.$ele.find(".add_file" ).fadeIn(3000);
                me.$ele.find(".image-resources-msg").parent(".resources").removeClass("error");
                me.$ele.find(".image-resources-msg").html("");
                me.$form.find("input[name='resources']").remove();
                me.$ele.find('.videoUrl').val('');
                me.$ele.find('.audioUrl').val('');
                me.$ele.find('.add_video').editable('setValue','', false);
                // me.$ele.find('.add_audio').editable('setValue','', false);		
                me.$ele.find('.add_video').editable('setValue','', false);	

                if($( "input[name='resType']" ).val() == "species.auth.SUser") {
                    var count = $("input[name='lastUploaded']").val();
                    var start = 0;
                    var w = 1; 
                    var end = start + w; 
                    createResources(start, end, w, count);
                    $("input[name='obvDir']").val('');
                }*/
                submitNextUpload(me); 
                return;

            } else if(data.imageStatus == "Failed") {
                me.$ele.find(".progress").css('z-index',90);
                me.$ele.find('.progress_msg').html('');
                flag = false;
                submitNextUpload(me);
                return;

            } else {
                setTimeout(function(){getProcessedImageStatusInAjax(jobId, images, me)}, 500);
            }
        }, error : function(xhr, status, error) {
            console.log("====ERROR=======");
            //alert(xhr.responseText);
        }
    });

}

function getProcessedImageStatus(jobId, images, me) {
    if(!jobId) {
        console.log("NO JOB ID");
        return;
    }
    getProcessedImageStatusInAjax(jobId, images, me);
}

function createResources(start, end, w, count) {
    if(count < end) {
        end = count;
    }
    var metadataForForm = $(".metadata.prop").slice(start, end).clone();
    $(metadataForForm).css("display","none");
    $("form.createResource").find(".metadata.prop").remove();
    $(metadataForForm).appendTo($("form.createResource"));

    $("form.createResource").ajaxSubmit({
        url : $(this).attr("action"),
        dataType : 'json', 
        type : 'POST',
        success : function(data, statusText, xhr, form) {
            if(end >= count) {
                $(".addedResource.thumbnail").draggable({helper:'clone'});  

                $(".imageHolder").droppable({
                    accept: ".addedResource.thumbnail",
                    drop: function(event,ui){
                        dropAction(event, ui, this);    
                    }
                });
                return;
            } else {
                createResources(end, end + w, w, count);
            }
        }, error : function (xhr, ajaxOptions, thrownError){
            console.log("THROWN ERROR");
            console.log(thrownError);
            createResources(end, end + w, w, count);
        }  
    });
}

/**
  @class uploadResource
 **/
(function ($) {
    "use strict";

    var UploadResource = function (ele, options) {
        this.$ele = $(ele);
        this.$form = $(ele).find('form.upload_resource');
        this.initForm(options);
    }

    UploadResource.prototype = {

        initForm : function(options) {
            var me = this;
            me.$ele.find('.add_image').bind('click', $.proxy(me.filePick, me));
            me.$ele.find('.add_audio').bind('click', $.proxy(me.filePickAudio, me));

            var videoOptions = {
                type : 'text',
                mode : 'popup',
                emptytext : '',
                placement : 'right', 
                url : function(params) {
                    var d = new $.Deferred;
                    if(!params.value) {
                        return d.reject(window.i8ln.observation.addResource.fr); //returning error via deferred object
                    } else {
                        me.$form.find('.videoUrl').val(params.value);
                        me.submitRes();
                        d.resolve();
                    }
                    return d.promise() 
                }, 
                    validate :  function(value) {
                        if($.trim(value) == '') {
                            return window.i8ln.observation.addResource.fr;
                        }
                    }, 
                title : window.i8ln.observation.addResource.youtube
            };



            var audioOptions = {
                type : 'text',
                mode : 'popup',
                emptytext : '',
                placement : 'bottom', 
                url : function(params) {
                    var d = new $.Deferred;
                    if(!params.value) {
                        return d.reject(window.i8ln.observation.addResource.fr); //returning error via deferred object
                    } else {
                        me.$form.find('.audioUrl').val(params.value);
                        me.submitRes();
                        d.resolve();
                    }
                    return d.promise() 
                }, 
                    validate :  function(value) {
                        if($.trim(value) == '') {
                            return window.i8ln.observation.addResource.fr;
                        }
                    }, 
                title : window.i8ln.observation.addResource.ayoutube
            };


            $.extend( videoOptions, options);
            me.$ele.find('.add_video').editable(videoOptions);
           // me.$ele.find('.add_audio').editable(audioOptions);




            me.$form.ajaxForm({ 
                url:window.params.observation.uploadUrl,
                dataType: 'xml',//could not parse json wih this form plugin 
                clearForm: true,
                resetForm: true,
                type: 'POST',
                beforeSubmit: function(formData, jqForm, opts) {
                    me.$ele.find("#addObservationSubmit").addClass('disabled');
                    return true;
                }, 
                context:me,
                success : me.onUploadResourceSuccess,
                error : me.onUploadResourceError
            });
        },

        submitRes : function() {
            this.$form.submit().find("span.msg").html(window.i8ln.observation.addResource.upload);
            this.$ele.find(".iemsg").html(window.i8ln.observation.addResource.upload);
            //this.$ele.find(".progress").css('z-index',110);
            //this.$ele.find('.progress_msg').html(window.i8ln.observation.addResource.uploading);
        },

        filePick : function(e) {
            var me = this;
            var onSuccess = function(FPFiles){
                $(".sortMediaOnExif").addClass("disabled");
                var count = 0;
                me.uploadedFiles = FPFiles;
                me.uploadedFilesSize = FPFiles.length;
                me.start = 0;
                me.w = 1;
                var FPF = me.uploadedFiles.slice(me.start, me.start + me.w);
                me.start = me.start + me.w;
                me.$form.find("input[name='resources']").remove();
                $.each(FPF, function(){
                    $('<input>').attr({
                        type: 'hidden',
                        name: 'resources',
                        value:JSON.stringify(this)
                    }).appendTo(me.$form);
                    count = count + 1;
                });
                if($( "input[name='resType']" ).val() == "species.auth.SUser") {
                    $("input[name='obvDir']").val('');
                    $('<input>').attr({
                        type: 'hidden',
                        name: 'lastUploaded',
                        value: count
                    }).appendTo(me.$form);
                }
                me.$ele.find(".progress").css('z-index',110);
                me.$ele.find('.progress_msg').html('Processing <br> Images...');
                me.$ele.find(".mediaProgressBar").progressbar({
                    value:0
                });
                me.$ele.find(".ui-progressbar-value").css('background','darkgoldenrod');
                me.submitRes();
            };

            var filepickerOptions = {
                maxSize: 104857600,
                services:['COMPUTER', 
                'FACEBOOK', 
                'FLICKR', 
                'PICASA', 
                'GOOGLE_DRIVE', 
                'DROPBOX'],
                mimetypes: ['image/*'],
                policy: me.POLICY, 
                signature: me.SIGNATURE
            };
            try {
            filepicker.pickMultiple(filepickerOptions, onSuccess, function(FPError){ 
                console.log(FPError.toString());
            });
            } catch(e) {
                console.log('filepicker error : '+e);
            }
                                    
        },






         filePickAudio : function(e) {
            var me = this;
            var onSuccess = function(FPFiles){
                var count = 0;
                me.uploadedFiles = FPFiles;
                me.uploadedFilesSize = FPFiles.length;
                me.start = 0;
                me.w = 1;
                var FPF = me.uploadedFiles.slice(me.start, me.start + me.w);
                me.start = me.start + me.w;
                me.$form.find("input[name='resources']").remove();
                $.each(FPF, function(){
                    $('<input>').attr({
                        type: 'hidden',
                        name: 'resources',
                        value:JSON.stringify(this)
                    }).appendTo(me.$form);
                    count = count + 1;
                });
                if($( "input[name='resType']" ).val() == "species.auth.SUser") {
                    $("input[name='obvDir']").val('');
                    $('<input>').attr({
                        type: 'hidden',
                        name: 'lastUploaded',
                        value: count
                    }).appendTo(me.$form);
                }
                me.submitRes();
            };

            var filepickerOptions1 = {
                maxSize: 104857600,
                services:['COMPUTER', 
                //'FACEBOOK',                 
                'GOOGLE_DRIVE', 
                'DROPBOX'],
                mimetypes: ['audio/*'],
                policy: me.POLICY, 
                signature: me.SIGNATURE
            };
            try {
            filepicker.pickMultiple(filepickerOptions1, onSuccess, function(FPError){ 
                console.log(FPError.toString());
            });
            } catch(e) {
                console.log('filepicker error : '+e);
            }
                                    
        },





        onUploadResourceSuccess : function(responseXML, statusText, xhr, form) {
            var me = this;
            me.$ele.find("#addObservationSubmit").removeClass('disabled');
            $(form).find("span.msg").html("");
            //me.$ele.find(".progress").css('z-index',90);
            //me.$ele.find('.progress_msg').html('Processing Image......');
            me.$ele.find(".iemsg").html("");
            //var rootDir = '${grailsApplication.config.speciesPortal.observations.serverURL}'
            //var rootDir = '${Utils.getDomainServerUrlWithContext(request)}' + '/observations'
            var obvDir = $(responseXML).find('dir').text();
            var obvDirInput = me.$form.find('input[name="obvDir"]');
            if(!obvDirInput.val()){
                $(obvDirInput).val(obvDir);
            }
            var images = [];
            var metadata = me.$ele.find(".metadata");
            var i = 0;
            if(metadata.length > 0) {
                var file_id = $(metadata.get(0)).children("input").first().attr("name");
                i = parseInt(file_id.substring(file_id.indexOf("_")+1));
            }
            var $s = $(responseXML).find('resources').find('res');
            var x = $s.length;
            var uploadedObjType
            $s.each(function() {
                me.jobId = $(this).attr('jobId');
                var fileName = $(this).attr('fileName');
                var type = $(this).attr('type');					
                uploadedObjType = type;
                images.push({i:x+i, file:fileName, url:$(this).attr('url'), thumbnail:$(this).attr('thumbnail'), type:type, title:fileName});
                x--;
            });
            console.log(uploadedObjType);
            if(uploadedObjType == "IMAGE"){
                getProcessedImageStatus(me.jobId, images, me); 
            } else {
                attachThumbnailAndProcess(me, images); 
            }
        },

        onUploadResourceError : function (xhr, ajaxOptions, thrownError) {
            var successHandler = this.success, errorHandler;
            var me = this;
            handleError(xhr, ajaxOptions, thrownError, successHandler, function(data) {
                if(data && data.status == 401) {
                    me.submitRes();
                    return; 
                }
                me.$ele.find("#addObservationSubmit").removeClass('disabled');                
                me.$form.find("input[name='resources']").remove();
                me.$form.find("span.msg").html("");
                me.$ele.find('.videoUrl').val('');
                me.$ele.find('.audioUrl').val('');
                me.$ele.find(".progress").css('z-index',90);
                me.$ele.find('.add_video').editable('setValue','', false);
              //  me.$ele.find('.add_audio').editable('setValue','', false);
                //xhr.upload.removeEventListener( 'progress', progressHandlingFunction, false); 

                var response = $.parseJSON(xhr.responseText);               
                if(response.error){
                    alert(response.error);
                    me.$ele.find(".image-resources-msg").parent(".resources").addClass("error");
                    //me.$ele.find(".image-resources-msg").html(response.error);
                }

                var messageNode = me.$ele.find(".message .resources");
               /* if(messageNode.length == 0 ) {
                    me.$form.prepend('<div class="message">'+(response?response.error:"Error")+'</div>');
                } else {
                    messageNode.append(response?response.error:"Error");
                }
                */
            });
        } 
    }

    //making object visible outside
    $.fn.components.UploadResource = UploadResource;

}(window.jQuery)); 



$(document).ready(function(){
    $('.dropdown-toggle').dropdown();

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
                uploadResource.submitRes();
            }
        }
    }



    /*
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
       */
    //$('#add_video').editable(addVideoOptions);
    $.each($('.star_obvcreate'), function(index, value){
        rate($(value));
    });


    /**
    */
    $('#attachFiles').change(function(e){
        uploadResource.submitRes();
    });


});


