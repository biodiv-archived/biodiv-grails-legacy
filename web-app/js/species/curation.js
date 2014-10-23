function getNamesFromTaxon(ele , parentId) {
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
        data: {parentId:parentId, classificationId:classificationId},	
        success: function(data) {
            console.log("======SUCCESS====");
            if(data.dirtyList){
                var dlContent = "<ul>";
                $.each(data.dirtyList, function(index, value){
                    dlContent += "<li onclick='getNameDetails("+value.taxonid +","+ value.classificationid+")'><a>" +value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                dlContent += "</ul>";
                $(".dl_content ul").remove();
                $(".dl_content").append(dlContent);
            }
            if(data.workingList){
                var wlContent = "<ul>";
                $.each(data.workingList, function(index, value){
                    wlContent +="<li onclick='getNameDetails("+value.taxonid+","+ value.classificationid+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                wlContent += "</ul>";
                $(".wl_content ul").remove();
                $(".wl_content").append(wlContent);

            }
            if(data.cleanList){
                var clContent = "<ul><li>";
                $.each(data.cleanList, function(index, value){
                    clContent +="<li onclick='getNameDetails("+value.taxonid+","+ value.classificationid+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                clContent += "</ul>";
                $(".cl_content ul").remove();
                $(".cl_content").append(clContent);

            }
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });

}

function getNameDetails(taxonId, classificationId) {
    console.log("=======NAME DEATILS=======" + taxonId);
    $('.taxonId').val(taxonId);
    var url = window.params.curation.getNameDetailsUrl;
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {taxonId:taxonId, classificationId:classificationId},	
        success: function(data) {
            changeEditingMode(false);
            populateNameDetails(data);
            $(".taxonRegId").val(data['taxonRegId']);
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
    console.log("=======REACHED POPULATE====");
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
    var name = $(".name").val();
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
    //clear table
    $ele.find("table tr td").remove();
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
            console.log("======SUCCESS ID DETAILS====");
            $("#externalDbResults").modal('hide');
            populateNameDetails(data);
            if(dbName == 'col') {
                changeEditingMode(true);
                console.log("======ID DETAILS====");
                console.log(data['id_details']);
                $(".id_details").val(JSON.stringify(data['id_details']));
            }
            console.log(data);  
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
}

function saveHierarchy() {
    var taxonRegistryData = fetchTaxonRegistryData();
    taxonRegistryData['abortOnNewName'] = true;
    taxonRegistryData['fromCOL'] = $('.fromCOL').val();
    if($('.fromCOL').val() == "true") {
        taxonRegistryData['abortOnNewName'] = false;
        taxonRegistryData['id_details'] = JSON.parse($(".id_details").val());
    }
    console.log("===============");
    console.log(taxonRegistryData);
    var url =  window.params.taxon.classification.updateUrl;
    console.log("====URL========= " + url);
    $.ajax({
        url: url,
        type: "POST",
        dataType: "json",
        //contentType: "application/json",
        data: {taxonData: JSON.stringify(taxonRegistryData)},	
        success: function(data) {
            //show the popup                                   //what in response
            //$("#externalDbResults").modal('hide');
            //populateNameDetails(data)
            console.log("======SUCCESS SAVED HIERARCHY====");
            console.log(data);
            if(data['success']) {
                var index = $(".rankDropDown")[0].selectedIndex -1;
                var arr = data['activityType'].split('>');
                var index1 = arr.length -1;
                if(index1 < index) {
                    alert("Hierarchy saved only till - " + arr[arr.length - 2]);
                } else {
                    alert(data['msg']);
                }
            } else {
                alert(data['msg']);
            }
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
    
    result['reg'] = $(".taxonRegId").val()          //$('#taxaHierarchy option:selected').val();
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

function changeEditingMode(mode) {
    if(mode == false) {
        $(".fromCOL").val(mode);
    } else {
        $(".fromCOL").val(mode);
    }
    $(".canBeDiasbled input").prop("disabled", mode); 
    $(".canBeDiasbled select").prop("disabled", mode); 
}


//====================== SYNONYM RELATED ===============================
function modifySynonym(ele) {
    event.preventDefault();
    console.log("========UPDATE SY=========");
    var that = $(ele);
    var url = window.params.species.updateUrl;
    var p = {};
    var  modifyType = that.attr('rel');
    var form_var = that.parent().parent().parent().find('form');   

    if(modifyType == "edit"){
        form_var.find('input').attr("disabled", false);
        that.html("<i class='icon-ok icon-white'></i>").attr('rel','update');
        return false;
    }   

    if(modifyType == "delete"){
        var modify = that.prev().attr('rel');
        if(modify == "update"){
            form_var.find('input').attr("disabled", true);
            that.prev().html("<i class='icon-edit icon-white'></i>").attr('rel','edit');
            return false;
        }else{
            confirm("Are you sure to delete?");
            return false;
        }    
    }

    form_value = form_var.serializeArray();
    for (var i = 0; i < form_value.length; i++) {
        p[form_value[i].name] = form_value[i].value;        
    }
    p['name']  = "synonym";
    p['act'] = modifyType;    
   // p['sid'] =221555;
    p['relationship'] = 'synonym';
    var otherParams = {};
    otherParams['atAnyLevel'] = true;
    otherParams['taxonId'] = $(".taxonId").val();  //272991;
    p['otherParams'] = otherParams    
    form_var.find('input').attr("disabled", true);
    console.log(p);
    that.html("<i class='icon-edit icon-white'></i>").attr('rel','edit');
  $.ajax({
        url: url,
        type: "POST",
        dataType: "json",
        data: {synonymData: JSON.stringify(p)},	
        success: function(data) {
            //show the popup
            //$("#externalDbResults").modal('hide');
            //populateNameDetails(data)
            form_var.find(".sid").val(data['synonymId']);
            console.log("======SUCCESS====");
            console.log(data);  

        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
 
}
