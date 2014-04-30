var grid;
var dirtyRows;
var prevNameColumn = {};

function isEmptyRow(rowEntry){
    var emptyRow = true;
    $.each( rowEntry, function( key, value ) {
        if(value != "" && value != undefined && value != null) {
            emptyRow = false;
        }
    });
    return emptyRow;
}

/*function getData(gridData) {
    var data = [];
    var gridData = grid.getData();

    for(var rowId=0; rowId<gridData.length; rowId++) {
        var rowEntry = gridData.getDataItem(rowId);
         if(isEmptyRow(rowEntry)){
            continue;
        }
        data.push(rowEntry);
    }
    return data; 
}*/

//only returning modified data
function getDataFromGrid(){
    if(!dirtyRows){
        return grid.getData();
    }
    var selectedRows = grid.getSelectedRows();
    if(selectedRows.length > 0){
    	dirtyRows.push(selectedRows.last());
    }
    dirtyRows = dirtyRows.unique();
    var data = new Array();
    $.each(dirtyRows, function(index, rowId) {
        var rowEntry = grid.getDataItem(rowId);
        data.push(rowEntry);
    });
    return data;
}

function addDirtyRows(e, args) {
	if(dirtyRows)
        dirtyRows.push(args.row);
};

function initGrid(data, columns, res, sciNameColumn, commonNameColumn) {
    $("#speciesGridSection").show();
    var options = {
        editable: true,
        enableAddRow: true,
        enableCellNavigation: true,
        asyncEditorLoading: false,
        autoEdit: true,
        fullWidthRows:true,
        rowHeight:32
    };
    
    var headerFunction;
    if(res === "species") {
        headerFunction = getSpeciesHeaderMenuOptions;
    }
    else{
        headerFunction = getHeaderMenuOptions;
    }

    function setUnEditableColumn(columns){
        var unEditableColumn = "Id";
        $.each(columns, function(index, column) {
            if(column.name === unEditableColumn){
                column.editor = null;
            }	
        });
    }

    $(function () {
    	setUnEditableColumn(columns);
        grid = new Slick.Grid("#myGrid", data, columns, options);
        grid.autosizeColumns();
        grid.setSelectionModel(new Slick.CellSelectionModel());

        grid.onAddNewRow.subscribe(function (e, args) {
            var item = args.item;
            grid.invalidateRow(data.length);
            data.push(item);
            grid.updateRowCount();
            grid.render();
        });

        if(dirtyRows){
            grid.onCellChange.subscribe(addDirtyRows)
        }

        grid.addNewColumn = function(newColumnName, options, position){
            columns = grid.getColumns();
            if(newColumnName == undefined)
                newColumnName = prompt('New Column Name','');

            if(newColumnName == null||$.trim(newColumnName)==''){
                return;
            }
            newColumnName = $.trim(newColumnName);
            var newColumn = grid.getColumns()[grid.getColumnIndex(newColumnName)]
            if(newColumn) return;
            else {
                if(res === "species"){
                    options = $.extend({}, {
                        id:newColumnName,
                        name:newColumnName,
                        field:newColumnName,
                        editor: Slick.Editors.Text,
                    }, options);
                }
                else{
                    options = $.extend({}, {
                        id:newColumnName,
                        name:newColumnName,
                        field:newColumnName,
                        editor: Slick.Editors.Text,
                        header:headerFunction()
                    }, options);
                }
                newColumn = options;

                if(typeof position === 'number' && position % 1 == 0 && position < columns.length)
                    columns.splice(position, 0 , newColumn);
                else {
                    newColumn = [newColumn]
                        $.merge(columns,newColumn);
                }

                grid.setColumns(columns);
                grid.render();
                return newColumn;
            }
        };


        var headerMenuPlugin = new Slick.Plugins.HeaderMenu({buttonImage:window.params.dropDownIconUrl});
        headerMenuPlugin.onBeforeMenuShow.subscribe(function(e, args) {
            var menu = args.menu;
            var i = menu.items.length;
            var iconClass = undefined
            menu.items[0].iconCssClass = (args.column.name === $("#sciNameColumn").val())?'icon-check':undefined
            menu.items[1].iconCssClass = (args.column.name === $("#commonNameColumn").val())?'icon-check':undefined
        });
        
        headerMenuPlugin.onCommand.subscribe(function(e, args) {
            var name = args.column.name;

            if(args.command === 'sciNameColumn') {
                if(args.column.name == $('#sciNameColumn').val())
                    name = ''
                if(args.column.name == $('#commonNameColumn').val())
                    $('#commonNameColumn').val('');
                $('#sciNameColumn').val(name);
            } else if(args.command === 'commonNameColumn') {
                if(args.column.name == $('#commonNameColumn').val())
                    name = ''
                if(args.column.name == $('#sciNameColumn').val())
                    $('#sciNameColumn').val('');
                $('#commonNameColumn').val(name);
            }
            selectNameColumn($('#commonNameColumn'), commonNameFormatter);
            selectNameColumn($('#sciNameColumn'), sciNameFormatter);
        });
        grid.registerPlugin(headerMenuPlugin);

        
        
        $("#myGrid").show();
        $('#checklistStartFile_uploaded').hide();

        if(res === "species") {
            var col = new Array();
            var k = 0;
            for(var i= 0; i < columns.length; i++) {
                if(columns[i].name != "Media"){
                    col[k] = columns[i];
                    k=k+1;
                }
            }
            var infoCol = new Array();
            for(var z= 0; z < col.length; z++) {
                infoCol[z] = col[z].name;
            }
            $('#columnOrder').val(infoCol);

            populateTagHeaders(col);
            $.ajax({
                url:window.params.getDataColumnsDB,
                dataType:'JSON',
                success:function(data){
                    updateMetadataValues();  
                    $(".propagateDown").tagit({
                        availableTags:infoCol,
                        fieldName: 'tags',
                        showAutocompleteOnFocus: true,
                        allowSpaces: true,
                        beforeTagAdded: function(event, ui) {
                            if(infoCol.indexOf(ui.tagLabel) == -1)
                            {
                                return false;
                            }
                            if(ui.tagLabel == "not found")
                            {
                                return false;
                            }
                        }
                    });
                    $(".headerInfoTags").tagit({
                        availableTags:data,
                        fieldName: 'tags', 
                        showAutocompleteOnFocus: true,
                        allowSpaces: true,
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
                    $(".extraInfoTags").tagit({
                        availableTags:infoCol,
                        fieldName: 'tags', 
                        showAutocompleteOnFocus: true,
                        allowSpaces: true,
                        beforeTagAdded: function(event, ui) {
                            if(infoCol.indexOf(ui.tagLabel) == -1)
                            {
                                return false;
                            }
                            if(ui.tagLabel == "not found")
                            {
                                return false;
                            }
                        }
                    });
                    $(".licenseInfoTags").tagit({
                        availableTags:infoCol,
                        fieldName: 'tags',
                        showAutocompleteOnFocus: true,
                        allowSpaces: true,
                        beforeTagAdded: function(event, ui) {
                            if(infoCol.indexOf(ui.tagLabel) == -1)
                            {
                                return false;
                            }
                            if(ui.tagLabel == "not found")
                            {
                                return false;
                            }
                        }
                    });
                    /*
                    $(".licenseInfoTags").tagit( {showAutocompleteOnFocus: false});
                    $(".licenseInfoTags").tagit("createTag", "brand-new-tag");
                    $(".licenseInfoTags").tagit( {showAutocompleteOnFocus: true});
                    */
                    $(".audienceInfoTags").tagit({
                        availableTags:infoCol,
                        fieldName: 'tags', 
                        showAutocompleteOnFocus: true,
                        allowSpaces: true,
                        beforeTagAdded: function(event, ui) {
                            if(infoCol.indexOf(ui.tagLabel) == -1)
                            {
                                return false;
                            }
                            if(ui.tagLabel == "not found")
                            {
                                return false;
                            }
                        }
                    });
                    var headerMetadata = getHeaderMetadata();
                    if(Object.keys(headerMetadata).length == 0){
                        var res = tagMetadatas(data);
                        if(res == true){
                            automaticPropagate();
                            alert("Your columns have been automatically marked.Please Verify");
                        }
                    }
                }
            });

        }

        if(sciNameColumn) {
            $('#sciNameColumn').val(sciNameColumn);
            selectNameColumn($('#sciNameColumn'), sciNameFormatter);
        }
        else {
            $('#sciNameColumn').val(sciNameColumn);
            selectNameColumn($('#sciNameColumn'), sciNameFormatter);
        }
        if(commonNameColumn) {
            $('#commonNameColumn').val(commonNameColumn);
            selectNameColumn($('#commonNameColumn'), commonNameFormatter);
        }
        else{
            $('#commonNameColumn').val(commonNameColumn);
            selectNameColumn($('#commonNameColumn'), commonNameFormatter);
        }
    });
} 

function sciNameFormatter(row, cell, value, columnDef, dataContext) {
    if (value == null || value == undefined || !value.length)
        return '';
    else if(dataContext.speciesId)
        return "<a href='"+window.params.species.url + '/' + dataContext.speciesId+"' target='_blank' title='"+dataContext.speciesTitle+"'><i>"+value+"</i></a> "
    else if(dataContext.speciesTitle)
        return "<span title='"+dataContext.speciesTitle+"'><i>"+value+"</i></span>"
    else
        return '<i>'+value+'</i>';
}
function commonNameFormatter(row, cell, value, columnDef, dataContext) {
    if (value == null || value == undefined || !value.length)
        return '';
    else if(dataContext.speciesId)
        return "<a href='"+window.params.species.url + '/' + dataContext.speciesId+"' target='_blank' title='"+dataContext.speciesTitle+"'>"+value+"</a> "
    else if(dataContext.speciesTitle)
        return "<span title='"+dataContext.speciesTitle+"'>"+value+"</span>"
    else
        return value;
}


/**
 */
function addMediaFormatter(row, cell, value, columnDef, dataContext) {
    var html = '';
    if(value) {
        //HACK
        var len = value.length;
        for (var i=0; i<len; i++) {
            html += "<img class='small_profile_pic' src='"+value[i]['thumbnail']+"'/>";
        }
    }
    html += "<button class='btn btn-link'  title='Add Media' data-toggle='modal' data-target='#addResources'  onclick='openDetails("+row+","+cell+");return false;'><i class='icon-plus'></i></button>";
    return html;
}

/**
 */
function showGrid(){
    var input = $("#checklistStartFile_path").val();
    var res = "checklist";
    if($('#textAreaSection').is(':visible')){
        parseData(  window.params.content.url + input , {callBack:loadDataToGrid, res: res});
    }
    else{
        parseData(  window.params.content.url + input , {callBack:initGrid, res: res });
    }
}

function loadDataToGrid(data, columns, res, sciNameColumn, commonNameColumn) {
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

    loadTextToGrid(data, columns, res, sciNameColumn, commonNameColumn);
}

function loadTextToGrid(data, columns, res, sciNameColumn, commonNameColumn) {
    $("#gridSection").show();
    initGrid(data, columns, res, sciNameColumn, commonNameColumn)
    $("#textAreaSection").hide();
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

/*
 * This function is called in edit checklist page
 */
function loadGrid(url, id){
	dirtyRows = new Array();
	$.ajax({
		url: url,
        dataType: 'json',
		data:{id:id},
		success: function(data){
			var headers = data.columns;
			var columns = new Array();
			var editor = Slick.Editors.Text
			$.each(headers, function(index, header) {
                            columns.push({id:header, name: header, field: header, editor:editor, sortable:false, minWidth: 100, 'header':getHeaderMenuOptions()});
			});
                        columns.push(getMediaColumnOptions());
                        loadTextToGrid(data.data, columns, data.res, data.sciNameColumn, data.commonNameColumn);
                        //grid.setColumns(finalCols);
                        //grid.render();
                        //grid.autosizeColumns();
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

$( ".date" ).datepicker({ 
    changeMonth: true,
    changeYear: true,
    dateFormat: 'dd/mm/yy' 
});

/**
 *
 */
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
 *
 */
function selectColumn(selector, selectedColumn){
    var markColumnSelect = selector ? selector : this;
    /*var columns = grid.getColumns();
    $(markColumnSelect).empty();
    $.each(columns, function(index, column) {
        $(markColumnSelect).append($("<option />").val(column.id).text(column.name));
    });*/
    if(selectedColumn) {
        $(markColumnSelect).val(selectedColumn);
    }
};

function selectNameColumn(selectedColumn, formatter) {
    var columns = grid.getColumns();
    var nameColumn = $(selectedColumn).val();
    var command = $(selectedColumn).attr('id');
    if(prevNameColumn[command]) {
        for (var i = 0, len = columns.length; i < len; i++) {
            var column = columns[i];
            if(prevNameColumn[command].id == column.id) {
                columns[i] = prevNameColumn[command]
            }
        }
    }
    for (var i = 0, len = columns.length; i < len; i++) {
        var column = columns[i];
        if(nameColumn == column.id) {
            prevNameColumn[command] = $.extend({},column);
            column.editor = AutoCompleteEditor;
            column.cssClass = 'nameColumn';
            column.formatter = formatter;
        }
    };

    grid.invalidate();
}


/**
 * document ready
 */
$(document).ready(function(){
    $('.dropdown-toggle').dropdown();
    intializesSpeciesHabitatInterest(false);

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
        /*$('html, body').animate({
            scrollTop: $("#restOfForm").offset().top
        }, 1000);*/
        //loadMapInput();
        $('#wizardButtons').hide();
    });

    /*function selectColumn2(event) {
        selectColumn(this);
    }
    $("#sciNameColumn").focus(selectColumn2);
    $("#commonNameColumn").focus(selectColumn2);
    
    $("#sciNameColumn").change(selectSciNameColumn);
    */ 
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

        if(((typeof(sciNameColumn) == 'undefined') || (sciNameColumn == '')) && ((typeof(commonNameColumn) == 'undefined') || (commonNameColumn == ''))) {
            confirm("Please mark scientific name column or common name column in the list");
            return;
        }
        if(sciNameColumn === commonNameColumn) {
            alert('Same column is mentioned as scientific name and common name.');
            return;
        }

        $('#legend').hide();
        var me = this
        
       
        
        $.ajax({
            url : window.params.recommendation.getRecos,
            type : 'post', 
            dataType: 'json',
            data : {'names':JSON.stringify(getNames())},
            success : function(data) {
                var gridData = grid.getData();
                var sciNameColumnIndex = grid.getColumnIndex($('#sciNameColumn').val());
                var sciNameColumn = grid.getColumns()[sciNameColumnIndex];
                var commonNameColumnIndex = grid.getColumnIndex($('#commonNameColumn').val());
                var commonNameColumn = grid.getColumns()[commonNameColumnIndex];
                var changes = {}; var incorrectNames = false;
                for(var rowId=0; rowId<gridData.length; rowId++) {
                	var rowEntry = grid.getDataItem(rowId);
                	if(isEmptyRow(rowEntry)){
                        continue;
                    }
                    if(data.hasOwnProperty(rowId+'')) {
                        if(!changes[rowId])
                            changes[rowId] = {}
                        
                        if(data[rowId].id) {
                            //if(sciNameColumn) changes[rowId][sciNameColumn.id] = 'validReco'
                            //if(commonNameColumn) changes[rowId][commonNameColumn.id] = 'validReco'
       
                            var dataItem = grid.getDataItem(rowId);
                            if(data[rowId].speciesId)
                                dataItem.speciesId = data[rowId].speciesId;
                            dataItem.speciesTitle = data[rowId].name;
                        } 
                        else {
                            if(sciNameColumn && !data[rowId].parsed ) {
                                changes[rowId][sciNameColumn.id] = 'incorrectName';
                                incorrectNames = true;
                            }
                            if(commonNameColumn && !data[rowId].parsed) {
                                changes[rowId][commonNameColumn.id] = 'incorrectName';
                                incorrectNames = true;
                            }
                        }
                    } else {
                        if(!data[rowId].parsed) {
                            changes[rowId][sciNameColumn.id] = 'incorrectName'
                            incorrectNames = true;
                        }
                    }
                    grid.invalidateRow(rowId);
                }

                grid.setCellCssStyles("highlight", changes);
                grid.render();
                if(incorrectNames === true)
                    $('#legend').show();
                $('#createChecklist').trigger('click');
            },
            error: function(xhr, textStatus, errorThrown) {
                alert('Error while validating names');
                console.log(xhr);
            }
        });
    });

    //removning empty rows and properties
    function getGridDataJSON(gridData) {
        var ck = new Array();
        for(var rowId=0; rowId<gridData.length; rowId++) {
            var rowEntry = grid.getDataItem(rowId);
            if(isEmptyRow(rowEntry)){
                continue;
            }
            var row = new Object();
            $.each( rowEntry, function( key, value ) {
                if(value != "" && value != undefined && value != null) {
                    row[key] = value;
                }
            });
            ck.push(row);
        }
        return JSON.stringify(ck);
    }

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
	            $("#checklistData").val(getGridDataJSON(getDataFromGrid()));
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
          .focus()
          .select();

        $input.autofillNames({
            'appendTo' : '#nameSuggestions',
            'nameFilter':args.column.id,
            focus: function( event, ui ) {
                return false;
            }, select: function( event, ui ) {
                if(ui.item.speciesId) {
                    args.item.speciesId = ui.item.speciesId
                    args.item.speciesTitle = ui.item.value
                }
                //CHANGING item value itself
                ui.item.value = ui.item.label.replace(/<.*?>/g,"");
            }, open: function(event, ui) {
                $("#nameSuggestions ul").css({'display': 'block','width':'300px'}); 
            }
        });
        $input.bind("keydown.nav", function (e) {
            if (e.keyCode === $.ui.keyCode.LEFT || e.keyCode === $.ui.keyCode.RIGHT || e.keyCode === $.ui.keyCode.DOWN || e.keyCode === $.ui.keyCode.UP) {
              e.stopImmediatePropagation();
            }
          })

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


function openDetails(row, cell) {
    var columns = grid.getColumns()
    if (grid.getEditorLock().isActive() && !grid.getEditorLock().commitCurrentEdit()) {
        return;
    }
    if(row === undefined || cell === undefined) {
        alert('Either row or cell is missing');
        $('#addResourcesModal').modal('toggle');
        return false;
    }

    $('#addResourcesModal ul#imagesList>li.addedResource.thumbnail').remove();

    var data = grid.getData()[row];

    var media = data.Media;
    if(media) {
        var obvDir = data.obvDir;
        var obvDirInput = $('#upload_resource input[name="obvDir"]');
        if(!obvDirInput.val()){
            $(obvDirInput).val(obvDir);
        }

        var images = [];
        var metadata = $(".metadata");
        var i = 0;
        /*if(metadata.length > 0) {
          var file_id = $(metadata.get(-1)).children("input").first().attr("name");
          i = parseInt(file_id.substring(file_id.indexOf("_")+1));
          }*/
        for(i=0; i< media.length; i++) {
            var m = media[i];
            //TODO:push rating also
            images.push({i:i+1, file:obvDir + "/" + m['file'], url:m['url'], thumbnail:m['thumbnail'], type:m['type'], title:m['fileName']});
        };

        var html = $( "#metadataTmpl" ).render( images );
        var metadataEle = $(html);
        metadataEle.each(function() {
            //$('.geotagged_image', this).load(function(){
            //    update_geotagged_images_list($(this));		
            //});
            var $ratingContainer = $(this).find('.star_obvcreate');
            rate($ratingContainer)
        })

        $("#imagesList li:last" ).before (metadataEle);
    }

    $('#addResourcesModal').data({'row':row, 'cell':cell}).modal('show');

    return false;
}

$(document).ready(function() {
    /**
     *
     */
    $('#addResourcesModalSubmit').click(function(){
       var row = $('#addResourcesModal').data().row;    
       var cell = $('#addResourcesModal').data().cell;
       if(row === undefined || cell === undefined) {
           alert('Either row or cell is missing');
           $('#addResourcesModal').modal('toggle');
           return false;
       }
        var data = grid.getData()[row]
        var addedResources = $('#addResourcesModal ul.imagesList>li');
        data.Media = new Array($(addedResources).length-1);
        for(var i=0; i<$(addedResources).length-1; i++) {
            data.Media[i] = {};
        }
        $.each($(addedResources).find('input'), function(index, input){
            var name = $(input).attr('name');
            var n = name.substring(0, name.lastIndexOf("_"));
            var j = parseInt(name.substring(name.indexOf("_")+1));
            data.Media[j-1][n] = $(input).val();
            data.Media[j-1]['thumbnail'] = $('#image_'+j).attr('src');
        });

        grid.getEditController().commitCurrentEdit();
        addDirtyRows(undefined, {row:row});
        grid.invalidateRow(row);
        grid.render();
        $('#addResourcesModal').modal('toggle');
    });
});

function selectLicense($this, i) {
    $('#license_'+i).val($.trim($this.text()));
    $('#selected_license_'+i).find('img:first').replaceWith($this.html());
    return false;
}
