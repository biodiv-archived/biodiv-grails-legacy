var grid;
function f(data, columns){
    var options = {
        editable: true,
        enableAddRow: true,
        enableCellNavigation: true,
        asyncEditorLoading: false,
        autoEdit: false
    };

    $(function () {
        grid = new Slick.Grid("#myGrid", data, columns, options);

        grid.setSelectionModel(new Slick.CellSelectionModel());

        grid.onAddNewRow.subscribe(function (e, args) {
            var item = args.item;
            grid.invalidateRow(data.length);
            data.push(item);
            grid.updateRowCount();
            grid.render();
        });

        $("#myGrid").show();
        $('#checklistStartFile_uploaded').hide();
    });
} 

function showGrid(){
    var input = $("#checklistStartFile_path").val(); 
    parseData("/biodiv/content" + input , {callBack:f});
}

function requiredFieldValidator(value) {
    if (value == null || value == undefined || !value.length) {
        return {valid: false, msg: "This is a required field"};
    } else {
        return {valid: true, msg: null};
    }
}

function removeResource(event, imageId) {
    var targ;
    if (!event) var event = window.event;
    if (event.target) targ = event.target;
    else if (event.srcElement) targ = event.srcElement; //for IE

    $(targ).parent('.addedResource').remove();
    $(".image_"+imageId).remove();
}

$( ".date" ).datepicker({ 
    changeMonth: true,
changeYear: true,
format: 'dd/mm/yyyy' 
});


function progressHandlingFunction(e){
    if(e.lengthComputable){
        var position = e.position || e.loaded;
        var total = e.totalSize || e.total;

        var percentVal = ((position/total)*100).toFixed(0) + '%';
        $('#progress_bar').width(percentVal)
            $('#translucent_box').width('100%')
            $(".progress").css('z-index',110);
        $('#progress_msg').html('Uploaded '+percentVal);
    }
}

/**
 * upload_resource & FilePicker
 */
function filePick() {
    var onSuccess = function(FPFiles){
        $.each(FPFiles, function(){
            $('<input>').attr({
                type: 'hidden',
            name: 'resources',
            value:JSON.stringify(this)
            }).appendTo('#upload_resource');
        })
        $('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
        $("#iemsg").html("Uploading... Please wait...");
        $(".progress").css('z-index',110);
        $('#progress_msg').html('Uploading ...');
    }

    filepicker.pickMultiple({
        mimetypes: ['image/*'],
                            maxSize: 104857600,
    //debug:true,
    services:['COMPUTER', 'FACEBOOK', 'FLICKR', 'PICASA', 'GOOGLE_DRIVE', 'DROPBOX'],
    }, onSuccess 
    ,
    function(FPError){
    console.log(FPError.toString());
    }
    );	
    }

    /**
     * Google Picker API for the Google Docs import
     */

    function newPicker() {
        google.load('picker', '1', {"callback" : createPicker});
    }

// Create and render a Picker object for searching images.
function createPicker() {
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
            $('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
            $("#iemsg").html("Uploading... Please wait...");
            $(".progress").css('z-index',110);
            $('#progress_msg').html('Uploading ...');
        }
    }
}

function getSelectedGroup() {
    var grp = []; 
    $('#speciesGroupFilter button').each (function() {
        if($(this).hasClass('active')) {
            grp.push($(this).attr('value'));
        }
    });
    return grp;	
} 

function getSelectedHabitat() {
    var hbt = []; 
    $('#habitatFilter button').each (function() {
        if($(this).hasClass('active')) {
            hbt.push($(this).attr('value'));
        }
    });
    return hbt;	
}
	
/**
 * document ready
 */
$(document).ready(function(){
    $('.dropdown-toggle').dropdown();
    $('#add_image').bind('click', filePick);
    intializesSpeciesHabitatInterest(false);

    var onVideoAddSuccess = function(params) {
        var d = new $.Deferred;
        if(!params.value) {
            return d.reject('This field is required'); //returning error via deferred object
        } else {
            $('#videoUrl').val(params.value);
            $('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
            $("#iemsg").html("Uploading... Please wait...");
            $(".progress").css('z-index',110);
            $('#progress_msg').html('Uploading ...');
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
    //$('#add_video').click(function(){
    //    newPicker();                    
    //});

/*    $(".group_option").click(function(){
        $("#group_id").val($(this).val());
        var caret = "<span class='caret'></span>";
        $("#selected_group").html($(this).html() + caret);
        //$("#group_options").hide();
        $("#selected_group").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});

    });


    $(".habitat_option").click(function(){
        $("#habitat_id").val($(this).val());
        var caret = "<span class='caret'></span>";
        $("#selected_habitat").html($(this).html() + caret);
        //$("#habitat_options").hide();
        $("#selected_habitat").css({'background-color':'#e5e5e5', 'border-bottom-color':'#aeaeae'});
    });
*/
    $.each($('.star_obvcreate'), function(index, value){
        rate($(value));
    });

    //$(".tagit-input").watermark("Add some tags");
    $("#tags").tagit({
        select:true, 
        allowSpaces:true, 
        placeholderText:'Add some tags',
        fieldName: 'tags', 
        autocomplete:{
            source: '/observation/tags'
        }, 
        triggerKeys:['enter', 'comma', 'tab'], 
        maxLength:30
    });

    $(".tagit-hiddenSelect").css('display','none');

    function getSelectedUserGroups() {
        var userGroups = []; 
        $('.userGroups button[class~="btn-success"]').each (function() {
            userGroups.push($(this).attr('value'));
        });
        return userGroups;	
    }

    $('input:radio[name=groupsWithSharingNotAllowed]').click(function() {
        var previousValue = $(this).attr('previousValue');

        if(previousValue == 'true'){
            $(this).attr('checked', false)
        }

        $(this).attr('previousValue', $(this).attr('checked'));
    });



    $('#use_dms').click(function(){
        if ($('#use_dms').is(':checked')) {
            $('.dms_field').fadeIn();
            $('.degree_field').hide();
        } else {
            $('.dms_field').hide();
            $('.degree_field').fadeIn();
        }

    });
    $("#name").watermark("Suggest a species name");

    $("#help-identify input").click(function(){
        if ($(this).is(':checked')){
            $('.nameContainer input').val('');
            $('.nameContainer input').attr('disabled', 'disabled');
        }else{
            $('.nameContainer input').removeAttr('disabled');
        }
    });

    $("#addObservationSubmit").click(function(event){
        if($(this).hasClass('disabled')) {
            alert("Uploading is in progress. Please submit after it is over.");
            event.preventDefault();
            return false; 		 		
        }

        if (document.getElementById('agreeTerms').checked){
            $(this).addClass("disabled");

            var speciesGroups = getSelectedGroup();
            var habitats = getSelectedHabitat();

            $.each(speciesGroups, function(index){
                var input = $("<input>").attr("type", "hidden").attr("name", "group_id").val(this);
                $('#addObservation').append($(input));	
            })

            $.each(habitats, function(index){
                var input = $("<input>").attr("type", "hidden").attr("name", "habitat_id").val(this);
                $('#addObservation').append($(input));	
            })


            $("#userGroupsList").val(getSelectedUserGroups());
            if(drawnItems) {
                var areas = drawnItems.getLayers();
                if(areas.length > 0) {
                    var wkt = new Wkt.Wkt();
                    wkt.fromObject(areas[0]);
                    $("input#areas").val(wkt.write());
                }
            }
            $("#addObservation").submit();        	
            return false;
        } else {
            alert("Please agree to the terms mentioned at the end of the form to submit the observation.")
        }
    });

});	


