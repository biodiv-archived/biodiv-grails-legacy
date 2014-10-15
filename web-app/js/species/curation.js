function getNamesFromTaxon(ele) {
    console.log(ele);
    $("#taxonHierarchy tr").css('background', 'white');
    console.log($(ele).parents("tr"));
    $(ele).parents("tr").css('background', 'burlywood');
    var taxonId = $(ele).parent("span").find(".taxDefIdVal").val();
    var classificationId = $('#taxaHierarchy option:selected').val();
    var url = window.params.curation.getNamesFromTaxonUrl;
    console.log("===URL=== " + url);
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {taxonId:taxonId, classificationId:classificationId},	
        success: function(data) {
            console.log("======SUCCESS====");
            if(data.dirtyList){
                var dlContent = "<ul>";
                $.each(data.dirtyList, function(index, value){
                    dlContent += "<li onclick='getNameDetails("+value.id +","+ value.classificationId+")'><a>" +value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                dlContent += "</ul>";
                $(".dl_content ul").remove();
                $(".dl_content").append(dlContent);
            }
            if(data.workingList){
                var wlContent = "<ul>";
                $.each(data.workingList, function(index, value){
                    wlContent +="<li onclick='getNameDetails("+value.id+","+ value.classificationId+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                wlContent += "</ul>";
                $(".wl_content ul").remove();
                $(".wl_content").append(dlContent);

            }
            if(data.cleanList){
                var clContent = "<ul><li>";
                $.each(data.cleanList, function(index, value){
                    clContent +="<li onclick='getNameDetails("+value.id+","+ value.classificationId+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                clContent += "</ul>";
                $(".cl_content ul").remove();
                $(".cl_content").append(dlContent);

            }
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });

}

function getNameDetails(taxonId, classificationId) {
    console.log("=======NAME DEATILS=======" + taxonId);
    var url = window.params.curation.getNameDetailsUrl;
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {taxonId:taxonId, classificationId:classificationId},	
        success: function(data) {
            populateNameDetails(data)
            console.log("======SUCCESS====");
            console.log(data);  
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
}

function setOption(selectElement, value) {
    console.log(selectElement);
    var options = selectElement.options;
    console.log(options);
    for (var i = 0, optionsLength = options.length; i < optionsLength; i++) {
        if (options[i].value == value) {
            selectElement.selectedIndex = i;
            return true;
        }
    }
    return false;
}

function populateNameDetails(data){
    for (var key in data) {
        console.log(key +"===== "+ data[key]);
        if(key != "rank" && key!= "status"){
            $("."+key).val(data[key]);
        }
    }  
    setOption(document.getElementById("rankDropDown"), data["rank"]);
    setOption(document.getElementById("statusDropDown"), data["nameStatus"]);
}

//takes name for search
function searchDatabase() {
    var name = $(".queryString").val();
    var dbName = $("#queryDatabase").val();
    if(dbName == "databaseName") {
        alert("Please select a database to query from!!");
        return;
    }
    var url = window.params.curation.searchExternalDbUrl;
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {name:name, dbName:dbName},	
        success: function(data) {
            //show the popup
            $("#externalDbResults").modal('show');
            fillPopupTable(data , $("#externalDbResults"));
            console.log("======SUCCESS====");
            console.log(data);  
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
}

function fillPopupTable(data, $ele) {
    var rows = "";
    $.each(data, function(index, value) {
        rows += "<tr><td>"+value['name'] +"</td><td>"+value['rank']+"</td><td>"+value['nameStatus']+"</td><td>"+value['group']+"</td><td>"+value['sourceDatabase']+"</td><td><button class='btn' onClick='getExternalDbDetails("+value['externalId']+")'>Select this</button></td></tr>"        
    });
    $ele.find("table").append(rows);
    return
}

//takes COL id
function getExternalDbDetails(externalId) {
    var url = window.params.curation.getExternalDbDetailsUrl;
    var dbName = $("#queryDatabase").val();
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {externalId:externalId, dbName:dbName},	
        success: function(data) {
            //show the popup
            $("#externalDbResults").modal('hide');
            populateNameDetails(data)
            console.log("======SUCCESS====");
            console.log(data);  
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
}

function saveHierarchy() {
    var taxonRegistryData = fetchTaxonRegistryData();
    taxonRegistryData['abortOnNewName'] = true;
    console.log("===============");
    console.log(taxonRegistryData);
    var url =  window.params.taxon.classification.updateUrl;
    console.log("====URL========= " + url);
    $.ajax({
        url: url,
        type: "POST",
        dataType: "json",
        data: taxonRegistryData,	
        success: function(data) {
            //show the popup
            $("#externalDbResults").modal('hide');
            populateNameDetails(data)
            console.log("======SUCCESS====");
            console.log(data);  
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
}

function fetchTaxonRegistryData() {
    var result = {}
    result['taxonRegistry.0'] = $('.kingdom').val();
    result['taxonRegistry.1'] = $('.phylum').val();
    result['taxonRegistry.2'] = $('.class').val();
    result['taxonRegistry.3'] = $('.order').val();
    result['taxonRegistry.4'] = $('.superfamily').val();
    result['taxonRegistry.5'] = $('.family').val();
    result['taxonRegistry.6'] = $('.subfamily').val();
    result['taxonRegistry.7'] = $('.genus').val();
    result['taxonRegistry.8'] = $('.subgenus').val();
    result['taxonRegistry.9'] = $('.species').val();
    
    result['reg'] = $(".taxonReg").val()          //$('#taxaHierarchy option:selected').val();
    result['classification'] = 817; //for author contributed
    
    var res = {};
    res['0'] = $('.kingdom').val();
    res['1'] = $('.phylum').val();
    res['2'] = $('.class').val();
    res['3'] = $('.order').val();
    res['4'] = $('.superfamily').val();
    res['5'] = $('.family').val();
    res['6'] = $('.subfamily').val();
    res['7'] = $('.genus').val();
    res['8'] = $('.subgenus').val();
    res['9'] = $('.species').val();
    result['taxonRegistry'] = res;

    var metadata1 = {};
    metadata1['name'] = $('.name').val();
    metadata1['rank'] = $('.rankDropDown').val();
    metadata1['authorString'] = $('.authorString').val();
    metadata1['nameStatus'] = $('.statusDropDown').val();
    metadata1['source'] = $('.source').val();
    metadata1['via'] = $('.via').val();
    metadata1['id'] = $('.id').val();
    result['metadata'] = metadata1;

    return result;
}
