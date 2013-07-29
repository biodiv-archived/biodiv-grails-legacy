var grid;
var canBeColumnName = 'CanBe';
var dirtyRows;

//only returning modified data
function getDataFromGrid(){
	if(!dirtyRows){
		return grid.getData();
	}
	
	var selectedRows = grid.getSelectedRows()
	if(selectedRows.length > 0){
		dirtyRows.push(selectedRows.last());
	}
	
	dirtyRows = dirtyRows.unique();
	var data = new Array();
	$.each(dirtyRows, function(index, rowId) {
		data.push(grid.getDataItem(rowId));
	});
	return data;
}

function initGrid(data, columns){
    var options = {
        editable: true,
        enableAddRow: true,
        enableCellNavigation: true,
        asyncEditorLoading: false,
        autoEdit: true
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

        if(dirtyRows){
            grid.onCellChange.subscribe(function (e, args) {
                dirtyRows.push(args.row);
            });
        }

        grid.addNewColumn = function(newColumnName, options, position){
            if(newColumnName == undefined)
                newColumnName = prompt('New Column Name','');

            if(newColumnName == null||newColumnName==''){
                return;
            }
        
	        var newColumn = grid.getColumns()[grid.getColumnIndex(newColumnName)]
	
	        if(newColumn) return newColumn;
	        else {
	            options = $.extend({}, {
	                    id:newColumnName,
	                    name:newColumnName,
	                    field:newColumnName,
	                    editor: Slick.Editors.TextCellEditor
	                }, options);
	            
	            newColumn = options;
	            
	            if(typeof position === 'number' && position % 1 == 0 && position < columns.length)
	                columns.splice(position, 0 , newColumn);
	            else {
	                newColumn = [newColumn]
	                $.merge(columns,newColumn);
	            }
	
	            grid.setColumns(columns);
	            grid.render();
                    grid.autosizeColumns()
	            return newColumn;
	        }
        };

        $("#myGrid").show();
        $('#checklistStartFile_uploaded').hide();
    });
} 

function sciNameFormatter(row, cell, value, columnDef, dataContext) {
    if (value == null || value == undefined || !value.length)
        return '';
    else
        return '<i>'+value+'</i>';
}

function showGrid(){
    var input = $("#checklistStartFile_path").val(); 
    if($('#textAreaSection').is(':visible'))
        parseData(  window.params.content.url + input , {callBack:loadDataToGrid});
    else
        parseData(  window.params.content.url + input , {callBack:initGrid});
}

function loadDataToGrid(data, columns) {
    var cols = '', d = '';
    
    $.each(columns, function(i, n){
        cols = cols + ',' + n.name
    });
    
    $.each(data, function(i, n){
        var line = ''
        $.each(n, function(key, element) {
            line = line + ',' + element;
        });
        line = line.slice(1);
        d = d + '\n' + line
    });

    $("#checklistData").val(d);
    $("#checklistColumns").val(cols.slice(1));
   
    loadTextToGrid(data, columns)
}

function loadTextToGrid(data, columns) {
    initGrid(data, columns);
    $("#textAreaSection").hide();
    $("#gridSection").show();
    $("#addNames").hide();
    $("#parseNames").show();
}

function requiredFieldValidator(value) {
    if (value == null || value == undefined || !value.length) {
        return {valid: false, msg: "This is a required field"};
    } else {
        return {valid: true, msg: null};
    }
}

function loadGrid(url, id){
	dirtyRows = new Array();
	$.ajax({
		url: url,
		data:{id:id},
		success: function(data){
			var headers = data.columns;
			var columns = new Array();
			var finalCols =  new Array();
			var editor = Slick.Editors.Text
			$.each(headers, function(index, header) {
				if(index > 0){
					finalCols.push({id:header, name: header, field: header, editor:editor, sortable:true, minWidth: 200});
				}
				columns.push({id:header, name: header, field: header, editor:editor, sortable:true, minWidth: 200});
			});
                        initGrid(data.data, columns);
                        grid.setColumns(finalCols);
                        grid.render();
                        grid.autosizeColumns();
			return true;
		},
		error: function(xhr, status, error) {
			var msg = $.parseJSON(xhr.responseText);
			alert(msg);
			return false;
		}
	});
}



$('#addNewColumn').unbind('click').click(function(){
    grid.addNewColumn();
}); 

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

function getSelectedGroupArr() {
    var grp = []; 
    $('#speciesGroupFilter button').each (function() {
        if($(this).hasClass('active')) {
            grp.push($(this).attr('value'));
        }
    });
    return grp;	
} 

function getSelectedHabitatArr() {
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

    /**
     */
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

    /**
     */
    $("#help-identify input").click(function(){
        if ($(this).is(':checked')){
            $('.nameContainer input').val('');
            $('.nameContainer input').attr('disabled', 'disabled');
        }else{
            $('.nameContainer input').removeAttr('disabled');
        }
    });


    /**
     */
    $('#attachFiles').change(function(e){
        $('#upload_resource').submit().find("span.msg").html("Uploading... Please wait...");
        $("#iemsg").html("Uploading... Please wait...");
    });

    /**
     */
    var onUploadResourceSuccess = function(responseXML, statusText, xhr, form) {
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
        $( "#add_file" ).fadeIn(3000);
        $("#image-resources-msg").parent(".resources").removeClass("error");
        $("#image-resources-msg").html("");
        $("#upload_resource input[name='resources']").remove();
        $('#videoUrl').val('');
        $('#add_video').editable('setValue','', false);		
    }

    var onUploadResourceError = function (xhr, ajaxOptions, thrownError){
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

    $('#upload_resource').ajaxForm({ 
        url:window.params.observation.uploadUrl,
        dataType: 'xml',//could not parse json wih this form plugin 
        clearForm: true,
        resetForm: true,
        type: 'POST',

        beforeSubmit: function(formData, jqForm, options) {
            $("#addObservationSubmit").addClass('disabled');
            return true;
        }, 
        success:onUploadResourceSuccess,
        error:onUploadResourceError
    });  

    /**
     *
     */
    $("#addNames").click(function() {
        var cols = $('#checklistColumns').val();
        var data = $("#checklistData").val();

        if(!data || !cols) {
            alert("Please load the column names and data");
            event.preventDefault();
            return false; 		 		
        }

        data = cols + '\n' + data
        parseCSVData(data, {callBack:loadTextToGrid});
    })

    /**
     *
     */
    $("#createChecklist").click(function() {
        $('#restOfForm').show();
        $('html, body').animate({
            scrollTop: $("#restOfForm").offset().top
        }, 2000);
        loadMapInput();
        $('#wizardButtons').hide();
    });

    /**
    *
    */
    function selectColumn(){
        var markColumnSelect = this;
        var columns = grid.getColumns();
        $(markColumnSelect).empty();
        $.each(columns, function(index, column) {
            if(column.id != canBeColumnName)
                $(markColumnSelect).append($("<option />").val(column.id).text(column.name));
            //    $(markColumnSelect).append($("<option />").val(column).text(column));
        });
        var snVal =  $('select[name="sciNameColumn"]').val();
        var cnVal =  $('select[name="commonNameColumn"]').val();
        //$('select[name="sciNameColumn"]').find('option[value="' +  snVal '"]').attr("selected",true);
        //$('select[name="commonNameColumn"]').find('option[value="' + cnVal '"]').attr("selected",true);
    };

    $("#sciNameColumn").focus(selectColumn);
    $("#commonNameColumn").focus(selectColumn);
    
    $("#sciNameColumn").change(function() {
        var column = grid.getColumns()[grid.getColumnIndex($("#sciNameColumn").val())];
        column.editor = AutoCompleteEditor;
        column.formatter = sciNameFormatter;
    });
    
    /**
     *
     */
    function getNames() {
        var names = [];
        var sciNameColumn = $('#sciNameColumn').val();
        var commonNameColumn = $('#commonNameColumn').val();

        $.each(grid.getData(), function(i, item) {
            names[i] = {}
            if(item[sciNameColumn] != undefined && item[sciNameColumn] != '')
                names[i]['sciName'] = item[sciNameColumn]
            if(item[commonNameColumn] != undefined && item[commonNameColumn] != '')
                names[i]['commonName'] = item[commonNameColumn]
        });
        return names
    }

    /**
     *
     */
    $('#parseNames').click(function() {
        var sciNameColumn = $('#sciNameColumn').val();
        var commonNameColumn = $('#commonNameColumn').val();

        if(((typeof(sciNameColumn) == 'undefined') || (sciNameColumn == null)) && ((typeof(commonNameColumn) == 'undefined') || (commonNameColumn == null))) {
            confirm("Please mark scientific name column or common name column in the list");
            return
        }

        $.ajax({
            url : window.params.recommendation.getRecos,
            type : 'post', 
            dataType: 'json',
            data : {'names':JSON.stringify(getNames())},
            success : function(data) {
                var gridData = grid.getData();
                var sciNameColumnIndex = grid.getColumnIndex($('#sciNameColumn').val());
                var column = grid.getColumns()[sciNameColumnIndex];
                    //grid.addNewColumn(canBeColumnName, {formatter:sciNameFormatter}, sciNameColumnIndex+1);
                var rows = [];
                for(rowId in data) {
                    if(data.hasOwnProperty(rowId)) {
                        rowId = parseInt(rowId, 10);
                        rows.push(rowId);
                        grid.setCellCssStyles("validReco", {
                            rowId : {
                                sciNameColumnIndex : 'sciNameColumn'
                            }
                        })
                        if(data[rowId].speciesId)
                            gridData[rowId][column.id] = "<a href='"+window.params.species.url + '/' + data[rowId].speciesId+"' target='_blank' title='"+data[rowId].name+"'>"+gridData[rowId][column.id]+"</a> "
                        else
                            gridData[rowId][column.id] = "<span title='"+data[rowId].name+"'>"+gridData[rowId][column.id]+"</span>"
                    }
                }
                grid.invalidateRows(rows);
                grid.render();
                $('#createChecklist').show();
            },
            error: function(xhr, textStatus, errorThrown) {
                alert(xhr);
            }
        });
    });

    /**
     *
     */
    $("#addObservationSubmit").click(function(event){
        if($(this).hasClass('disabled')) {
            alert("Uploading is in progress. Please submit after it is over.");
            event.preventDefault();
            return false; 		 		
        }

        if (document.getElementById('agreeTerms').checked) {
            $(this).addClass("disabled");

            var speciesGroups = getSelectedGroupArr();
            var habitats = getSelectedHabitatArr();

            $.each(speciesGroups, function(index, element){
                var input = $("<input>").attr("type", "hidden").attr("name", "group_id").val(element);
                $('#addObservation').append($(input));	
            })

            $.each(habitats, function(index, element){
                var input = $("<input>").attr("type", "hidden").attr("name", "habitat_id").val(element);
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

            //checklist related data
            if(grid){
	            $("#checklistColumns").val(JSON.stringify(grid.getColumns()));
	            $("#checklistData").val(JSON.stringify(getDataFromGrid()));
	            $("#rawChecklist").val($("#checklistStartFile_path").val());
            }
            $("#addObservation").submit();        	
            return false;
        } else {
            alert("Please agree to the terms mentioned at the end of the form to submit the observation.")
        }
    });
	
});	

 function AutoCompleteEditor(args) {
    var $input;
    var defaultValue;
    var scope = this;

    this.init = function () {
      $input = $("<INPUT type=text class='editor-text' />")
          .appendTo(args.container)
          .bind("keydown.nav", function (e) {
            if (e.keyCode === $.ui.keyCode.LEFT || e.keyCode === $.ui.keyCode.RIGHT) {
              e.stopImmediatePropagation();
            }
          })
          .focus()
          .select();

        $input.autofillNames({
            'appendTo' : '#nameSuggestions',
            'nameFilter':args.column.id,
            focus: function( event, ui ) {
            }, select: function( event, ui ) {
            }, open: function(event, ui) {
                $("#nameSuggestions ul").css({'display': 'block','width':'300px'}); 
            }
        }); 
    };

    this.destroy = function () {
      $input.remove();
      //TODO:$input.autocomplete('destroy');
    };

    this.focus = function () {
      $input.focus();
    };

    this.getValue = function () {
      return $input.val();
    };

    this.setValue = function (val) {
      $input.val(val);
    };

    this.loadValue = function (item) {
      defaultValue = item[args.column.field] || "";
      $input.val(defaultValue);
      $input[0].defaultValue = defaultValue;
      $input.select();
    };

    this.serializeValue = function () {
      return $input.val();
    };

    this.applyValue = function (item, state) {
      item[args.column.field] = state;
    };

    this.isValueChanged = function () {
      return (!($input.val() == "" && defaultValue == null)) && ($input.val() != defaultValue);
    };

    this.validate = function () {
      if (args.column.validator) {
        var validationResults = args.column.validator($input.val());
        if (!validationResults.valid) {
          return validationResults;
        }
      }

      return {
        valid: true,
        msg: null
      };
    };

    this.init();
  }
