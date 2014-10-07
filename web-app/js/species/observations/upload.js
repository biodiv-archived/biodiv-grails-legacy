function viewGrid(){
    var input = $("#speciesfile_path").val();
    var res = "species";
    if($('#textAreaSection').is(':visible')){
        //console.log("LOAD SPECIES");
        parseData(  window.params.content.url + input , {callBack:loadSpeciesDataToGrid, res: res });
    }
    else{
        //console.log("INIT GRID");
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
                ]
        }
    }
}

function populateTagHeaders(columns) {    
    var tableRow = '';
    for(i=0; i<columns.length; i++){
        tableRow += '<tr><td class="columnName">'+columns[i].name+'</td><td class="dataColCell"></td><td class="headerFlagCell" align="center"><input type="checkbox" class="headerFlag" name = "header" value = "true"></td><td class="appendFlagCell" align="center"><input type="checkbox" class="appendFlag" name = "append" value = "true"></td><!--td class="groupRadioCell"><input type="radio" class="groupRadio" name="group'+i+'" value="1">1<input type="radio" class="groupRadio" name="group'+i+'" value="2">2<input type="radio" class="groupRadio" name="group'+i+'" value="3">3</td--><td class="delimiterCell"><input type="text" class="delimiter" style="width:49px;"></td><td class="imagesCell"></td><td class="contributorCell"></td><td class="attributionsCell"></td><td class="referencesCell"></td><td class="licenseCell"></td><td class="audienceCell"></td></tr>'
    } 
    
    $('#tableHeader').append(tableRow);
    $('#tagHeaders').show();

}

//var headerMarkers = {};

function getTagsForHeaders() {
    //tags extraction in new way
    //
    var index = 0;
    //console.log("tags getting");
    $("#tableHeader tr").each(function () {
        index = 0;
        var headerInfo = {};
        var headerName;
        $('td', this).each(function () {

            if($(this).attr("class") == "columnName"){
                headerName = $(this).text();
                //console.log(headerName);
            }
            else if($(this).attr("class") == "dataColCell") {
                var valArr = [];
                $(this).find('span.tagit-label').each(function(i){
                    valArr.push($(this).text()); // This is your rel value
                });
                var valData = valArr.join();
                //console.log(valData);
                headerInfo["dataColumns"] =  valData;
            }
            
            else if($(this).attr("class") == "imagesCell") {
                var valArr = [];
                $(this).find('span.tagit-label').each(function(i){
                    valArr.push($(this).text()); // This is your rel value
                });
                var valData = valArr.join();
                //console.log(valData);
                headerInfo["images"] =  valData;
            }
            else if($(this).attr("class") == "contributorCell") {
                var valArr = [];
                $(this).find('span.tagit-label').each(function(i){
                    valArr.push($(this).text()); // This is your rel value
                });
                var valData = valArr.join();
                //console.log(valData);
                headerInfo["contributor"] =  valData;
            }
            
            else if($(this).attr("class") == "attributionsCell") {
                var valArr = [];
                $(this).find('span.tagit-label').each(function(i){
                    valArr.push($(this).text()); // This is your rel value
                });
                var valData = valArr.join();
                //console.log(valData);
                headerInfo["attributions"] =  valData;
            }
            else if($(this).attr("class") == "referencesCell") {
                var valArr = [];
                $(this).find('span.tagit-label').each(function(i){
                    valArr.push($(this).text()); // This is your rel value
                });
                var valData = valArr.join();
                //console.log(valData);
                headerInfo["references"] =  valData;
            }
            else if($(this).attr("class") == "licenseCell") {
                var valArr = [];
                $(this).find('span.tagit-label').each(function(i){
                    valArr.push($(this).text()); // This is your rel value
                });
                var valData = valArr.join();
                //console.log(valData);
                headerInfo["license"] =  valData;
            }
            else if($(this).attr("class") == "audienceCell") {
                var valArr = [];
                $(this).find('span.tagit-label').each(function(i){
                    valArr.push($(this).text()); // This is your rel value
                });
                var valData = valArr.join();
                //console.log(valData);
                headerInfo["audience"] =  valData;
            }

            else if($(this).attr("class") == "headerFlagCell") {
                headerInfo["header"] = ""
            //if($(this).children(".headerFlag").prop("checked")){
            headerInfo["header"] =  $(this).children(".headerFlag:checked").map(function() {return this.value;}).get().join();
            if(headerInfo["header"] == "")
            {
                headerInfo["header"] = "false";
            }
        //}
            }
            else if($(this).attr("class") == "appendFlagCell") {
                //console.log("IN APPEND");
                headerInfo["append"] = ""
                    headerInfo["append"] =  $(this).children(".appendFlag:checked").map(function() {return this.value;}).get().join();
                if(headerInfo["append"] == "")
                {
                    headerInfo["append"] = "false";
                }

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
}


function updateMetadataValues() {
    var headerMetadata = getHeaderMetadata();
    //if(Object.keys(headerMetadata).length != 0){
    $("#tableHeader tr").each(function () {
        var taggedValues;
        $('td', this).each(function () {
            if($(this).attr("class") == "columnName"){
                var columnName = $(this).text();
                    taggedValues = headerMetadata[columnName];
                }
                else if($(this).attr("class") == "dataColCell") {
                    var preList='<ul class="headerInfoTags" >';
                    if(taggedValues != undefined){
                        var dataColumns = taggedValues["dataColumns"];
                        if(dataColumns!== ""){
                            var dataColArr = dataColumns.split(",");
                            $.each(dataColArr, function( index, value ) {
                                preList += '<li>'+value+'</li>'
                            });
                        }

                    }
                    preList += '</ul>';
                    $(this).append(preList);

                }
            else if($(this).attr("class") == "imagesCell" || $(this).attr("class") == "contributorCell" || $(this).attr("class") == "attributionsCell" || $(this).attr("class") == "referencesCell") {
                var preList='<ul class="extraInfoTags" >';
                if(taggedValues != undefined){
                    if($(this).attr("class") == "imagesCell"){
                        var images = taggedValues["images"];
                        if(images!== ""){
                            var imagesArr = images.split(",");
                            $.each(imagesArr, function( index, value ) {
                                preList += '<li>'+value+'</li>'
                            });
                        }
                    }
                    else if($(this).attr("class") == "contributorCell"){
                        var contributor = taggedValues["contributor"];
                        if(contributor!== ""){
                            var contributorArr = contributor.split(",");
                            $.each(contributorArr, function( index, value ) {
                                preList += '<li>'+value+'</li>'
                            });
                        }
                    }
                    else if($(this).attr("class") == "attributionsCell"){
                        var attributions = taggedValues["attributions"];
                        if(attributions!== ""){
                            var attributionsArr = attributions.split(",");
                            $.each(attributionsArr, function( index, value ) {
                                preList += '<li>'+value+'</li>'
                            });
                        }
                    }
                    else if($(this).attr("class") == "referencesCell"){
                        var references = taggedValues["references"];
                        if(references!== ""){
                            var referencesArr = references.split(",");
                            $.each(referencesArr, function( index, value ) {
                                preList += '<li>'+value+'</li>'
                            });
                        }
                    }

                }
                preList += '</ul>';
                $(this).append(preList);

            }
            
            else if($(this).attr("class") == "licenseCell") {
                    var preList='<ul class="licenseInfoTags" >';
                    if(taggedValues != undefined){
                        var license = taggedValues["license"];
                        if(license!== ""){
                            var licenseArr = license.split(",");
                            $.each(licenseArr, function( index, value ) {
                                preList += '<li>'+value+'</li>'
                            });
                        }

                    }
                    preList += '</ul>';
                    $(this).append(preList);

            }

            else if($(this).attr("class") == "audienceCell") {
                    var preList='<ul class="audienceInfoTags" >';
                    if(taggedValues != undefined){
                        var audience = taggedValues["audience"];
                        if(audience!== ""){
                            var audienceArr = audience.split(",");
                            $.each(audienceArr, function( index, value ) {
                                preList += '<li>'+value+'</li>'
                            });
                        }

                    }
                    preList += '</ul>';
                    $(this).append(preList);

            }

            else if($(this).attr("class") == "headerFlagCell") {
                var header = "";
                if(taggedValues != undefined){
                    header = taggedValues["header"];
                }
                if(header !== ""){
                    $(this).children("input[value='" + header + "']").prop('checked', true);
                }
            }
            else if($(this).attr("class") == "appendFlagCell") {
                    var append = "";
                    if(taggedValues != undefined){
                        append = taggedValues["append"];
                    }
                    if(append !== ""){
                        $(this).children("input[value='" + append + "']").prop('checked', true);
                    }
                }
                else if($(this).attr("class") == "groupRadioCell") {
                    var group = "";
                    if(taggedValues != undefined){
                        group = taggedValues["group"];
                    }
                    if(group !== ""){
                        $(this).children("input[value='" + group + "']").prop('checked', true);
                    }
                }
                else if($(this).attr("class") == "delimiterCell") {
                    var delimiter = "";
                    if(taggedValues != undefined){
                        delimiter = taggedValues["delimiter"];
                    }
                    if(delimiter !== ""){
                        $(this).children(".delimiter").val(delimiter);
                    }
                }
            });

        });
}

function getHeaderMetadata() {
    var headerMetadata = $('#headerMetadata').val();
    return headerMetadata;
}

function saveHeaderMetadata(headerMetadata) {
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

function tagMetadatas(data){
    var res = false;
    $("#tableHeader tr").each(function () {
        var columnName;
        $('td', this).each(function () {
            if($(this).attr("class") == "columnName"){
                columnName = $(this).text().toLowerCase();
            }
            else if($(this).attr("class") == "dataColCell") {
                var temp = $(this);
                $.each(data, function( index, value ) {
                    if(columnName == value.toLowerCase().trim()){
                        $(temp).find("ul").tagit( {showAutocompleteOnFocus: false});
                        $(temp).find("ul").tagit("createTag", value); 
                        $(temp).find("ul").tagit( {showAutocompleteOnFocus: true});
                        res = true;
                    }
                });
            }
        });
    });
    return res;
}

$('#downloadModifiedSpecies').click(function() {
    if($(this).hasClass('disabled')) {
        alert(window.i8ln.observation.upload.again);
        event.preventDefault();
        return false; 		 		
    }
    $(this).addClass('disabled');
    $("#uploadSpecies").addClass('disabled');
    getTagsForHeaders();
    var xlsxFileUrl = $('#xlsxFileUrl').val();
    var gData = JSON.stringify(grid.getData());
    //console.log(grid.getData().length);
    //console.log(gData);
    var orderedArray = $('#columnOrder').val();
    //console.log(orderedArray);
    orderedArray = JSON.stringify(orderedArray);
    //headerMarkers = JSON.stringify(headerMarkers);
    //Getting headerMetadata only
    //var headerMarkers = JSON.stringify($('#headerMetadata').val());
    var headerMarkers = JSON.stringify(getHeaderMetadata());
    //console.log("==CHECK THIS===" + headerMarkers);
    //var saveModifiedUrl = $('#saveModifiedUrl').val(); 
    $.ajax({
        url : window.params.saveModifiedSpecies,
        type : 'post', 
        dataType: 'json',
        data : {'headerMarkers': headerMarkers , 'xlsxFileUrl' : xlsxFileUrl, 'gridData' : gData, 'writeContributor' : 'false', 'orderedArray' : orderedArray },
        success : function(data) {
            //console.log(data.downloadFile);
            //var downloadUrl = window.params.downloadFile+"?downloadFile=" + encodeURIComponent(data.downloadFile);
            $("#downloadSpeciesFile input[name='downloadFile']").val(data.downloadFile);
            $("#downloadSpeciesFile").submit();
            $("#downloadModifiedSpecies").removeClass('disabled');
            $("#uploadSpecies").removeClass('disabled');

        },
        error: function(xhr, textStatus, errorThrown) {
            alert('Error downloading file!!');
            $("#downloadModifiedSpecies").removeClass('disabled');
            $("#uploadSpecies").removeClass('disabled');
            console.log(xhr);
        }
    });

});

function uploadSpecies(){
    $("#speciesLoader").show();
    getTagsForHeaders();
    var xlsxFileUrl = $('#xlsxFileUrl').val();
    var gData = JSON.stringify(grid.getData());
    var hm = getHeaderMetadata();
    delete hm["undefined"];
    var orderedArray = $('#columnOrder').val();
    orderedArray = JSON.stringify(orderedArray);
    var headerMarkers = JSON.stringify(hm);
    $.ajax({
        url : window.params.uploadSpecies,
        type : 'post', 
        dataType: 'json',
        data : {'headerMarkers': headerMarkers , 'xlsxFileUrl' : xlsxFileUrl, 'gridData' : gData, 'imagesDir': $("#imagesDir").val(), 'writeContributor': 'true','orderedArray' : orderedArray },
        success : function(data) {
           // $("#downloadSpeciesFile input[name='downloadFile']").val(data.downloadFile);
           // $("#downloadErrorFile input[name='downloadFile']").val(data.errorFile);
           // $("#filterLink").attr("href", data.filterLink);
            $("#speciesLoader").hide();
           // $("#uploadSpeciesDiv").hide();
            alert(data.msg);
           // document.getElementById("downloadSpeciesFile").style.visibility = "visible";
           // document.getElementById("downloadErrorFile").style.visibility = "visible";
            //$("#filterLinkSpan").show();
            $("#uploadSpecies").removeClass('disabled');
            $('#uploadSpecies').bind('click', uploadSpeciesWrapper);
            //$("#downloadModifiedSpecies").removeClass('disabled');
        },
        error: function(xhr, textStatus, errorThrown) {
            $("#speciesLoader").hide();
            $("#uploadSpecies").removeClass('disabled');
            $("#downloadModifiedSpecies").removeClass('disabled');
            $('#uploadSpecies').bind('click', uploadSpeciesWrapper);
            alert(window.i8ln.observation.upload.statu);
        }

    });
}

var uploadSpeciesWrapper = function() {
    $('#uploadSpecies').unbind('click');
    var check = validateContLic();
    if(check == false){
        alert(window.i8ln.observation.upload.lic);
        $('#uploadSpecies').bind('click', uploadSpeciesWrapper);
        return;
    }
    if($(this).hasClass('disabled')) {
        alert(window.i8ln.observation.upload.again);
        event.preventDefault();
        return false; 		 		
    }
    $(this).addClass('disabled');
    $("#downloadModifiedSpecies").addClass('disabled');
    uploadSpecies();
}

$('#uploadSpecies').click(uploadSpeciesWrapper);

$(".propagateButton").click(function(){
    var pEle = $(this).parents("th");
    var pClass = $(pEle).attr("class");
    var valArr = [];
    $(pEle).find('span.tagit-label').each(function(i){
        valArr.push($(this).text()); // This is your rel value
    });
     $("#tableHeader tr").each(function () {
            var dataCol = "";
            $('td', this).each(function () {
                if($(this).attr("class") == "dataColCell") {
                    var valArr1 = [];
                    $(this).find('span.tagit-label').each(function(i){
                        valArr1.push($(this).text()); // This is your rel value
                    });
                    dataCol = valArr1.join();
                }
                else if($(this).attr("class") == pClass){
                    if(dataCol.length != 0){
                        var temp = this
                        $(temp).find("ul").tagit( {showAutocompleteOnFocus: false});
                        $.each(valArr, function( index, value ) {
                            $(temp).find("ul").tagit("createTag", value);//select ul in this and create new tags//createTag
                        });
                        $(temp).find("ul").tagit( {showAutocompleteOnFocus: true});
                    }
                }
            });
     }); 
    $(pEle).find("div").hide();

});

function automaticPropagate(){
    if($("#isSimpleSheet").val() == true){
        $("#tableHeader tr").each(function () {
            var dataCol = "";
            $('td', this).each(function () {
                if($(this).attr("class") == "dataColCell") {
                    var valArr = [];
                    $(this).find('span.tagit-label').each(function(i){
                        valArr.push($(this).text()); // This is your rel value
                    });
                    dataCol = valArr.join();
                }
                else if($(this).attr("class") == "contributorCell"){
                    if(dataCol.length != 0){
                        $(this).find("ul").tagit( {showAutocompleteOnFocus: false});
                        $(this).find("ul").tagit("createTag", "contributor");
                        $(this).find("ul").tagit( {showAutocompleteOnFocus: true});
                    }
                }
                else if($(this).attr("class") == "attributionsCell"){
                    if(dataCol.length != 0){
                        $(this).find("ul").tagit( {showAutocompleteOnFocus: false});
                        $(this).find("ul").tagit("createTag", "attributions");
                        $(this).find("ul").tagit( {showAutocompleteOnFocus: true});
                    }
                }
                else if($(this).attr("class") == "licenseCell"){
                    if(dataCol.length != 0){
                        $(this).find("ul").tagit( {showAutocompleteOnFocus: false});
                        $(this).find("ul").tagit("createTag", "license");
                        $(this).find("ul").tagit( {showAutocompleteOnFocus: true});
                    }
                }
            });

        });

    }
}

$(".initPropagation").click(function(){
    var parentEle = $(this).parent("th");
    $(parentEle).find("div").toggle();
});

function validateContLic(){
    var check = true;
    var rowCount = 1;
    var map = new Object(); 
    $("#tableHeader tr").each(function () {
        var dataCol = "";
        $('td', this).each(function () {
            if($(this).attr("class") == "dataColCell") {
                var valArr = [];
                $(this).find('span.tagit-label').each(function(i){
                    valArr.push($(this).text()); // This is your rel value
                });
                dataCol = valArr.join();
            }
            else if($(this).attr("class") == "contributorCell"){
                if(dataCol.length != 0){
                    var valArr = [];
                    $(this).find('span.tagit-label').each(function(i){
                        valArr.push($(this).text()); // This is your rel value
                    });
                    var valData = valArr.join();
                    if(valData.length == 0 ){
                        check = false;
                        //map["check"] = check;
                        //map["rowCount"] = rowCount;
                        return check;
                    }
                }
            }
            else if($(this).attr("class") == "attributionsCell"){
                if(dataCol.length != 0){
                    var valArr = [];
                    $(this).find('span.tagit-label').each(function(i){
                        valArr.push($(this).text()); // This is your rel value
                    });
                    var valData = valArr.join();
                    if(valData.length == 0 ){
                        check = false;
                        //map["check"] = check;
                        //map["rowCount"] = rowCount;
                        return check;                    
                    }
                }
            }
            else if($(this).attr("class") == "licenseCell"){
                if(dataCol.length != 0){
                    var valArr = [];
                    $(this).find('span.tagit-label').each(function(i){
                        valArr.push($(this).text()); // This is your rel value
                    });
                    var valData = valArr.join();
                    if(valData.length == 0 ){
                        check = false;
                        //map["check"] = check;
                        //map["rowCount"] = rowCount;
                        return check;                    
                    }
                }
            }
        });
        rowCount = rowCount + 1;
    });
    return check;
}
