function viewGrid(){
    var input = $("#speciesfile_path").val();
    var res = "species";
    if($('#textAreaSection').is(':visible')){
        console.log("LOAD SPECIES");
        parseData(  window.params.content.url + input , {callBack:loadSpeciesDataToGrid, res: res });
    }
    else{
        console.log("INIT GRID");
        parseData(  window.params.content.url + input , {callBack:initGrid, res: res });
    }
}

function loadSpeciesDataToGrid(data, columns, res, sciNameColumn, commonNameColumn) {
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
    loadSpeciesTextToGrid(data, columns,res, sciNameColumn, commonNameColumn);
}

function loadSpeciesTextToGrid(data, columns, res, sciNameColumn, commonNameColumn) {
    $("#speciesGridSection").show();
    initGrid(data, columns, res, sciNameColumn, commonNameColumn)
    $("#textAreaSection").hide();
    $("#addNames").hide();
    $("#parseNames").show();
}

function getSpeciesHeaderMenuOptions() {
    return {
        menu: {
            items: [
            {
                title: "Scientific Name",
                command: "sciNameColumn"
            },
            {
                title: "Common Name",
                command: "commonNameColumn"
            }
            /*
            ,
            {
                title: "New Header",
                command: "newHeader"
            }

            ,
            {
                iconCssClass: "icon-help",
                title: "Help",
                command: "help"
            }*/
            ]
        }
    }
}

function populateTagHeaders(columns, tags) {    
    var tableRow = '';
    for(i=0; i<columns.length; i++){
        tableRow += '<tr><td class="columnName">'+columns[i].name+'</td><td class="dataColCell"><input class="dataColumns"></td><td class="headerFlagCell"><input type="checkbox" class="headerFlag" name = "header" value = "headerFlag"></td><td class="mergeFlagCell"><input type="checkbox" class="mergeFlag" name = "merge" value = "mergeFlag"></td><td class="groupRadioCell"><input type="radio" class="groupRadio" name="group'+i+'" value="G1">G1<input type="radio" class="groupRadio" name="group'+i+'" value="G2">G2<input type="radio" class="groupRadio" name="group'+i+'" value="G3">G3</td><td class="delimiterCell"><input type="text" class="delimiter"></td></tr>'
    } 
    /*
    for (i=0;i<columns.length;i++){
        $('<option/>').val(columns[i].name).html(columns[i].name).appendTo('#headerOptions');
    }

    var option = '';
    for (i=0;i<tags.length;i++){
        option += '<input type ="checkbox" name = "dataColumns" value="'+ tags[i] + '">' + tags[i] ;
    }
    */
    $('#tableHeader').append(tableRow);
    $('#tagHeaders').show();

}

//var headerMarkers = {};

function getTagsForHeaders() {
    //tags extraction in new way
    //
    var index = 0;
    console.log("tags getting");
    $("#tableHeader tr").each(function () {
        index = 0;
         var headerInfo = {};
         var headerName;
         $('td', this).each(function () {

             if($(this).attr("class") == "columnName"){
                 headerName = $(this).text();
                 console.log(headerName);
             }
             else if($(this).attr("class") == "dataColCell") {
                 headerInfo["dataColumns"] =  $(this).find(".dataColumns").val();
             }
             else if($(this).attr("class") == "headerFlagCell") {
                 headerInfo["header"] = ""
             //if($(this).children(".headerFlag").prop("checked")){
             headerInfo["header"] =  $(this).children(".headerFlag:checked").map(function() {return this.value;}).get().join();
         //}
             }
             else if($(this).attr("class") == "mergeFlagCell") {
                headerInfo["merge"] = ""
             //if($(this).children(".mergeFlag").prop("checked")){
                headerInfo["merge"] =  $(this).children(".mergeFlag:checked").map(function() {return this.value;}).get().join();
         //}
             }
             else if($(this).attr("class") == "groupRadioCell") {
                headerInfo["group"] = $(this).children(".groupRadio:checked").val();
                if(headerInfo["group"] == undefined){
                    headerInfo["group"] = "";
                }
             }
             else if($(this).attr("class") == "delimiterCell") {
                 headerInfo["delimiter"] = $(this).children(".delimiter").val();        
             }
             //index = index+1;
             /*
                var value = $(this).find(":input").val();
                var values = 100 - value + ', ' + value;

                if (value > 0) {
                $(this).append(htmlPre + values + htmlPost);
                }*/
         });
        populateHeaderMetadata(headerName, headerInfo);
    });
    /*
       var table = $("#tableHeader");
       for (var i = 0, row; row = table.rows[i]; i++) {
//iterate through rows
        //rows would be accessed using the "row" variable assigned in the for loop
        for (var j = 0, col; col = row.cells[j]; j++) {
            console.log(col);
            //iterate through columns
            //columns would be accessed using the "col" variable assigned in the for loop
        }  
    }

    /*
    var headerInfo = {};
    var headerName = $('#headerOptions :selected').text();
    headerInfo["dataColumns"] =  $( "#dataColumns :checked" ).map(function() {return this.value;}).get().join();
    headerInfo["header"] =  $( "#headerFlag :checked" ).map(function() {return this.value;}).get().join();
    headerInfo["group"] = $('[name="group"]:checked').val();
    headerInfo["merge"] =  $( "#mergeFlag :checked" ).map(function() {return this.value;}).get().join();
    populateHeaderMetadata(headerName, headerInfo);
    //showOnUI(headerMarkers);
    console.log(headerMarkers);
    */
}


function updateMetadataValues() {
    /*<li class="tagit-choice ui-widget-content ui-state-default ui-corner-all tagit-choice-editable"><span class="tagit-label">option1</span><a class="tagit-close"><span class="text-icon">×</span><span class="ui-icon ui-icon-close"></span></a></li>
    <ul class="tagit ui-widget ui-widget-content ui-corner-all">
    */
    var headerMetadata = getHeaderMetadata();
    console.log("UPDATE METADATA");
    console.log();
    
    if(Object.keys(headerMetadata).length != 0){
        $("#tableHeader tr").each(function () {
            var taggedValues;
            $('td', this).each(function () {
                if($(this).attr("class") == "columnName"){
                    var columnName = $(this).text();
                    taggedValues = headerMetadata[columnName];
                }
                else if($(this).attr("class") == "dataColCell") {
                    var dataColumns = taggedValues["dataColumns"];
                    if(dataColumns!== ""){
                        var dataColArr = dataColumns.split(",");
                        //APPEND EACH VALUE TO UL COMPONENT
                        var preList="";
                        $.each(dataColArr, function( index, value ) {
                            preList += '<li class="tagit-choice ui-widget-content ui-state-default ui-corner-all tagit-choice-editable"><span class="tagit-label">'+value+'</span><a class="tagit-close"><span class="text-icon">×</span><span class="ui-icon ui-icon-close"></span></a></li>'
                        });
                        console.log(preList);
                        $(this).find('ul').prepend(preList);
                    }
                }
                else if($(this).attr("class") == "headerFlagCell") {
                    var header = taggedValues["header"];
                    if(header !== ""){
                        $(this).children("input[value='" + header + "']").prop('checked', true);
                    }
                }
                else if($(this).attr("class") == "mergeFlagCell") {
                    var merge = taggedValues["merge"];
                    if(merge !== ""){
                        $(this).children("input[value='" + merge + "']").prop('checked', true);
                    }
                }
                else if($(this).attr("class") == "groupRadioCell") {
                    var group = taggedValues["group"];
                    if(group !== ""){
                        $(this).children("input[value='" + group + "']").prop('checked', true);
                    }
                }
                else if($(this).attr("class") == "delimiterCell") {
                    var delimiter = taggedValues["delimiter"];
                    if(delimiter !== ""){
                        $(this).children(".delimiter").val(delimiter);
                    }
                }
            });

        });
    }
    /*	
        console.log("UPDATION CALLED");
        var headerMetadata = getHeaderMetadata();
        console.log(headerMetadata);
        $("#dataColumns").children().prop('checked', false);
        $("#groupRadio").children().prop('checked', false);
        $("#mergeFlag").children().prop('checked', false);
        $("#headerFlag").children().prop('checked', false);
    if(headerMetadata && headerMetadata[value] != null) {
        console.log("VALUE HAI ISKI");
        var taggedValues = headerMetadata[value];
        var group = taggedValues["group"];
        var dataColumns = taggedValues["dataColumns"];
        var merge = taggedValues["merge"];
        var header = taggedValues["header"];
        console.log(taggedValues + "==== "+ group);
        if(group !== ""){
            $('#groupRadio').children("input[value='" + group + "']").prop('checked', true);
        }
        if(dataColumns!== ""){
            var dataColArr = dataColumns.split(",");
            $.each(dataColArr, function( index, value ) {
                $('#dataColumns').children("input[value='" + value + "']").prop('checked', true);
            });
        }
        if(merge !== ""){
            $('#mergeFlag').children("input[value='" + merge + "']").prop('checked', true);
        }
        if(header !== ""){
            $('#headerFlag').children("input[value='" + header + "']").prop('checked', true);
        }


    }
    //$('#groupRadio').children("input[value='G2']");
   //$("input[value='" + val + "']").prop('checked', true);
   */
}

function getHeaderMetadata() {
    var headerMetadata = $('#headerMetadata').val();
    console.log("getting data " + headerMetadata);
    //var headerMetadataParse = $.parseJSON(headerMetadata);
    //console.log("after parse" + headerMetadataParse);
    return headerMetadata;
}

function saveHeaderMetadata(headerMetadata) {
    //headerMetadata = JSON.stringify(headerMetadata);
    $('#headerMetadata').val(headerMetadata);
}

function populateHeaderMetadata(headerName, headerInfo) {
    var headerMetadata = getHeaderMetadata();
    headerMetadata[headerName] = headerInfo;
    saveHeaderMetadata(headerMetadata);
}

$('#tagHeadersButton').click(function() {
    //getTagsForHeaders();
    
});


$('#downloadModifiedSpecies').click(function() {
    getTagsForHeaders();
    var xlsxFileUrl = $('#xlsxFileUrl').val();
    var gData = JSON.stringify(grid.getData());
    //headerMarkers = JSON.stringify(headerMarkers);
    //Getting headerMetadata only
    //var headerMarkers = JSON.stringify($('#headerMetadata').val());
    var headerMarkers = JSON.stringify(getHeaderMetadata());
    //console.log(gData);
    //var saveModifiedUrl = $('#saveModifiedUrl').val(); 
    $.ajax({
        url : window.params.saveModifiedSpecies,
        type : 'post', 
        dataType: 'json',
        data : {'headerMarkers': headerMarkers , 'xlsxFileUrl' : xlsxFileUrl, 'gridData' : gData },
        success : function(data) {
            console.log(data.downloadFile);
            //var downloadUrl = window.params.downloadFile+"?downloadFile=" + encodeURIComponent(data.downloadFile);
            $("#downloadSpeciesFile input[name='downloadFile']").val(data.downloadFile);
            $("#downloadSpeciesFile").submit();

        },
        error: function(xhr, textStatus, errorThrown) {
            alert('Error downloading file!!');
            console.log(xhr);
        }
    });

});

$('#uploadSpecies').click(function() {
    getTagsForHeaders();
    var xlsxFileUrl = $('#xlsxFileUrl').val();
    var gData = JSON.stringify(grid.getData());
    var headerMarkers = JSON.stringify(getHeaderMetadata());
    $.ajax({
        url : window.params.uploadSpecies,
        type : 'post', 
        dataType: 'json',
        data : {'headerMarkers': headerMarkers , 'xlsxFileUrl' : xlsxFileUrl, 'gridData' : gData },
        success : function(data) {
            $("#downloadSpeciesFile input[name='downloadFile']").val(data.downloadFile);
            $("#uploadSpeciesDiv").hide();
            document.getElementById("downloadSpeciesFile").style.visibility = "visible";
        },
        error: function(xhr, textStatus, errorThrown) {
            alert('Error uploading species!!');
            console.log(xhr);
        }
    });

});


