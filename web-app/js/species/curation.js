function getNamesFromTaxon(ele , parentId) {
    console.log(ele);
    if($("#taxonHierarchy tr").hasClass("clickedEle")) {
        $("#taxonHierarchy tr").removeClass("clickedEle");
    }
    $(ele).parents("tr").addClass("clickedEle");
    $("#taxonHierarchy tr").css('background', 'white');
    console.log($(ele).parents("tr"));
    $(ele).parents("tr").css('background', '#3399FF');
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
                    dlContent += "<li onclick='getNameDetails("+value.taxonid +","+ value.classificationid+",this)'><a>" +value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                dlContent += "</ul>";
                $(".dl_content ul").remove();
                $(".dl_content").append(dlContent);
            }
            if(data.workingList){
                var wlContent = "<ul>";
                $.each(data.workingList, function(index, value){
                    wlContent +="<li onclick='getNameDetails("+value.taxonid+","+ value.classificationid+",this)'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                wlContent += "</ul>";
                $(".wl_content ul").remove();
                $(".wl_content").append(wlContent);

            }
            if(data.cleanList){
                var clContent = "<ul><li>";
                $.each(data.cleanList, function(index, value){
                    clContent +="<li onclick='getNameDetails("+value.taxonid+","+ value.classificationid+",this)'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
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

function getNameDetails(taxonId, classificationId, ele) {
    $("#externalDbResults").modal('hide');
    $("body").css("cursor", "progress");
    $("#searching").show();
    $("HTML").mousemove(function(e) {
        $("#searching").css({
            "top" : e.pageY,
            "left" : e.pageX + 15
        });
    });
    console.log("=======NAME DEATILS=======" + taxonId);
    $(ele).parent("ul").find("a").css('background-color','inherit');
    console.log(ele);
    $(ele).find("a").css('background-color','#3399FF');
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
            populateTabDetails(data);
            $(".countSp").text(data["countSp"]);
            $(".countObv").text(data["countObv"]);
            $(".countCKL").text(data["countCKL"]);
            $(".taxonRegId").val(data['taxonRegId']);
            $("#searching").hide();
            $("body").css("cursor", "default");
            if(ele == undefined) {
                return;
            }
            if($(ele).parents(".dl_content").length) {
                alert("Existing name attributes from IBP displayed below. Catalogue of Life (CoL) is the preferred taxonomic reference for IBP, please proceed to auto-query CoL for up-to-date name attributes.");
                $('.queryDatabase option[value="col"]').attr("selected", "selected");
                $('.queryString').trigger("click");
            }
            console.log("======SUCCESS====");
            console.log(data);  
        }, error: function(xhr, status, error) {
            $("#searching").hide();
            $("body").css("cursor", "default");
            alert(xhr.responseText);
        } 
    });
}

function populateTabDetails(data) {
    console.log("====TAB DETAILS====");
    console.log(data);
    $("#names-tab1 .singleRow input").val('');
    //clearing synonyms
    var synonymsList = data['synonymsList']
    if(synonymsList && synonymsList.length > 0) {
        console.log(synonymsList);
        var e = $("#names-tab1 .singleRow").first().clone();
        $("#names-tab1 .singleRow").remove();
        $.each(synonymsList, function(index, value){
            var f = $(e).clone();
            $(f).insertBefore("#names-tab1 .add_new_row");
            var ele = $("#names-tab1 .singleRow").last();
            console.log(value["id"]);
            $(ele).find("input[name='sid']").val(value["id"]);
            console.log($(ele).find("input[name='value']"));
            $(ele).find("input[name='value']").val(value["name"]);
            $(ele).find("input[name='source']").val(value["source"]);
            $(ele).find("input[name='contributor']").val(value["contributors"]);
        })
    }
    //clearing common names
    $("#names-tab2 .singleRow input").val('');
    var commonNamesList = data['commonNamesList']
    if(commonNamesList && commonNamesList.length > 0) {
        console.log(commonNamesList);
        var e = $("#names-tab2 .singleRow").first().clone();
        $("#names-tab2 .singleRow").remove();
        $.each(commonNamesList, function(index, value){
            var f = $(e).clone();
            $(f).insertBefore("#names-tab2 .add_new_row");
            var ele = $("#names-tab2 .singleRow").last();
            console.log(value["id"]);
            $(ele).find("input[name='cid']").val(value["id"]);
            console.log($(ele).find("input[name='value']"));
            $(ele).find("input[name='value']").val(value["name"]);
            $(ele).find("input[name='source']").val(value["source"]);
            $(ele).find("input[name='contributor']").val(value["contributors"]);
        })
    }
    
    //clearing accepted names
    $("#names-tab0 .singleRow input").val('');
    var acceptedNamesList = data['acceptedNamesList']
    if(acceptedNamesList && acceptedNamesList.length > 0) {
        console.log(acceptedNamesList);
        var e = $("#names-tab0 .singleRow").first().clone();
        $("#names-tab0 .singleRow").remove();
        $.each(acceptedNamesList, function(index, value){
            var f = $(e).clone();
            $(f).insertBefore("#names-tab0 .add_new_row");
            var ele = $("#names-tab0 .singleRow").last();
            console.log(value["id"]);
            $(ele).find("input[name='aid']").val(value["id"]);
            console.log($(ele).find("input[name='value']"));
            $(ele).find("input[name='value']").val(value["name"]);
            $(ele).find("input[name='source']").val(value["source"]);
            $(ele).find("input[name='contributor']").val(value["contributors"]);
        })
    }
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
    console.log(data);
    $(".canBeDisabled input[type='text']").val('');
    $('.rankDropDown option:first-child').attr("selected", "selected");
    $('.statusDropDown option:first-child').attr("selected", "selected");
    for (var key in data) {
        console.log(key +"===== "+ data[key]);
        if(key != "rank" && key!= "status"){
            $("."+key).val(data[key]);
        }
    }  
    $(".via").val(data["sourceDatabase"]);
    if(data["externalId"]) {
        console.log(data["externalId"]);
        $(".source").val($("#queryDatabase option:selected ").text());
        $(".id").val(data["externalId"]);
    }
    setOption(document.getElementById("rankDropDown"), data["rank"]);
    setOption(document.getElementById("statusDropDown"), data["nameStatus"]);
}

//takes name for search
function searchDatabase(addNewName) {
    $("body").css("cursor", "progress");
    $("#searching").show();
    $("HTML").mousemove(function(e) {
        $("#searching").css({
            "top" : e.pageY,
            "left" : e.pageX + 15
        });
    });
    var name = "";
    if(addNewName) {
        name = $(".newName").val();
        $("#newNamePopup").modal("hide");
        $('.queryDatabase option[value="col"]').attr("selected", "selected");
    } else {
        name = $(".name").val();
    }
    if(name == "") {
        alert("Please provide a name to search!!");
        return;
    }
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
            $("#searching").hide();
            $("body").css("cursor", "default");
            //show the popup
            if(data.length != 0) {
                $("#externalDbResults").modal('show');
                fillPopupTable(data , $("#externalDbResults"), "externalData");
                console.log("======SUCCESS====");
                console.log(data); 
            } else {
                alert("Sorry no results found from "+ $("#queryDatabase option:selected").text() + ". Please query an alternative database or input name-attributes manually.");
                if(addNewName) {
                    alert("Searching name in IBP Database");
                    searchIBP(name);
                }
            }
        }, error: function(xhr, status, error) {
            $("#searching").hide();
            $("body").css("cursor", "default");
            alert(xhr.responseText);
        } 
    });
}

function searchIBP(name) {
    $("body").css("cursor", "progress");
    $("#searching").show();
    $("HTML").mousemove(function(e) {
        $("#searching").css({
            "top" : e.pageY,
            "left" : e.pageX + 15
        });
    });
    var url = window.params.curation.searchIBPURL;
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {name:name},	
        success: function(data) {
            $("#searching").hide();
            $("body").css("cursor", "default");
            //show the popup
            if(data.length != 0) {
                $("#externalDbResults").modal('show');
                fillPopupTable(data , $("#externalDbResults"), "IBPData");
                console.log("======SUCCESS====");
                console.log(data); 
            } else {
                alert("Sorry no results found from IBP Database. Fill in details manually");
            }
        }, error: function(xhr, status, error) {
            $("#searching").hide();
            $("body").css("cursor", "default");
            alert(xhr.responseText);
        } 
    });

}

function fillPopupTable(data, $ele, dataFrom) {
    console.log("=====fill popup table====");
    console.log(data.length);
    if(data.length == 0) {
        alert("Sorry No results found!!");
    }
    var classificationId = $('#taxaHierarchy option:selected').val();
    //clear table
    $ele.find("table tr td").remove();
    var rows = "";
    $.each(data, function(index, value) {
        if(dataFrom == "externalData") {
            rows += "<tr><td>"+value['name'] +"</td><td>"+value['rank']+"</td><td>"+value['nameStatus']+"</td><td>"+value['group']+"</td><td>"+value['sourceDatabase']+"</td><td><button class='btn' onclick='getExternalDbDetails("+value['externalId']+")'>Select this</button></td></tr>"        
        } else {
            rows += "<tr><td>"+value['name'] +"</td><td>"+value['rank']+"</td><td>"+value['nameStatus']+"</td><td>"+value['group']+"</td><td>"+value['sourceDatabase']+"</td><td><button class='btn' onclick='getNameDetails("+value['taxonId'] +","+ classificationId+",undefined)'>Select this</button></td></tr>"
        }
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
            populateTabDetails(data);
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

function saveHierarchy(moveToWKG) {
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
    if(moveToWKG == true) {
        taxonRegistryData['moveToWKG'] = true;
    }
    $.ajax({
        url: url,
        type: "POST",
        dataType: "json",
        //contentType: "application/json",
        data: {taxonData: JSON.stringify(taxonRegistryData)},	
        success: function(data) {
            //show the popup                                   //what in response
            //$("#externalDbResults").modal('hide');
            console.log("======SUCCESS SAVED HIERARCHY====");
            console.log(data);
            if(data['success']) {
                /*
                var index = $(".rankDropDown")[0].selectedIndex -1;
                var arr = data['activityType'].split('>');
                var index1 = arr.length -1;
                if(index >= 4) {
                    index1 = index1 + 1; 
                }
                var lastName = arr[arr.length - 2];
                */
                
                if(data["newlyCreated"]) {
                    alert(data["newlyCreatedName"] +" is a new uncurated name on the portal. Hierarchy saved is -- " + data['activityType'] +" .Please explicitly curate "+ data["newlyCreatedName"] +" from dirty list to continue.");
                } else {
                    alert( "Successfully " + data['activityType']);
                }
                if(moveToWKG == true) {
                    console.log("========TRIGGERING CLICK======");
                    $(".clickedEle .taxDefIdSelect").trigger("click");
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
    $(".canBeDisabled input").prop("disabled", mode); 
    $(".canBeDisabled select").prop("disabled", mode); 
}


//====================== SYNONYM RELATED ===============================
function modifySynonym(ele) {
    event.preventDefault();
    console.log("========UPDATE SY=========");
    var that = $(ele);
    var url = window.params.species.updateUrl;
    var p = {};
    var  modifyType = that.attr('rel');
    var form_var = that.closest('form');   

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
            that.html("<i class='icon-trash'></i>");
            return false;
        }else{
            if(!confirm("Are you sure to delete?")) {
                return false;
            } else {
                form_var.find('input').attr("disabled", false);
            }
        }    
    }

    form_value = form_var.serializeArray();
    console.log(form_value);
    for (var i = 0; i < form_value.length; i++) {
        p[form_value[i].name] = form_value[i].value;        
    }
    p['name']  = "synonym";
    p['act'] = modifyType;    
    p['relationship'] = 'synonym';
    var otherParams = {};
    otherParams['atAnyLevel'] = true;
    otherParams['taxonId'] = $(".taxonId").val();  //272991;
    p['otherParams'] = otherParams    
    form_var.find('input').attr("disabled", true);
    console.log(p);
    if(modifyType != 'delete') {
        that.html("<i class='icon-edit icon-white'></i>").attr('rel','edit');
    }
    $.ajax({
        url: url,
        type: "POST",
        dataType: "json",
        data: {synonymData: JSON.stringify(p)},	
        success: function(data) {
            //show the popup
            //$("#externalDbResults").modal('hide');
            if(data['success']) {
                form_var.find(".sid").val(data['synonymId']);
                that.next().html("<i class='icon-trash'></i>");
                console.log("======SUCCESS====");
                console.log(data);  
                if(modifyType == 'delete') {
                    form_var.parent().hide();
                }
            } else {
                alert("Error in saving - "+data['msg']);
            }
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
 
}

$('.add_new_row').click(function(){
    console.log("======ADD NEW CALLED=======");
    var me = this;
    var typeClass = $(me).prev().find("input[type='hidden']").attr('name')
    var p = new Object();
    p['typeClass']= typeClass;
    console.log(p);
    var html = $('#newRowTmpl').render(p);
    $(me).before(html);
});

function showNewNamePopup() {
    $("#newNamePopup").modal("show");
}
