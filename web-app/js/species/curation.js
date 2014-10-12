function getNamesFromTaxon(taxonId) {
    console.log("called--------------");
    taxonId = $(".taxDefIdCheck:checked").parent("span").find(".taxDefIdVal").val();
    var url = window.params.curation.getNamesFromTaxonUrl;
    $.ajax({
        url: url,
        dataType: "json",
        data: {taxonId:taxonId},	
        success: function(data) {
            console.log("======SUCCESS====");
            if(data.dirtyList){
                var dlContent = "<ul>";
                $.each(data.dirtyList, function(index, value){
                    dlContent += "<li onclick='getNameDetails("+value.id+")'><a>" +value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                dlContent += "</ul>";
                $(".dl_content ul").remove();
                $(".dl_content").append(dlContent);
            }
            if(data.workingList){
                var wlContent = "<ul>";
                $.each(data.workingList, function(index, value){
                    wlContent +="<li onclick='getNameDetails("+value.id+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                wlContent += "</ul>";
                $(".wl_content ul").remove();
                $(".wl_content").append(dlContent);

            }
            if(data.cleanList){
                var clContent = "<ul><li>";
                $.each(data.cleanList, function(index, value){
                    clContent +="<li onclick='getNameDetails("+value.id+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                clContent += "</ul>";
                $(".cl_content ul").remove();
                $(".cl_content").append(dlContent);

            }

            console.log(dlContent);
            console.log(wlContent);
            console.log(clContent);
            console.log(data);
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });

}

function getNameDetails(taxonId) {
    console.log("=======NAME DEATILS=======" + taxonId);
    var url = window.params.curation.getNameDetailsUrl;
    $.ajax({
        url: url,
        dataType: "json",
        data: {taxonId:taxonId},	
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
    var dbName = $( "#queryDatabase").val();
    var url = window.params.curation.searchExternalDbUrl;
    $.ajax({
        url: url,
        dataType: "json",
        data: {name:name, dbName:dbName},	
        success: function(data) {
            //show the popup
            $("#externalDbResults").modal('show');
            console.log("======SUCCESS====");
            console.log(data);  
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
}

//takes COL id
function getCOLDetails(colId) {
    var url = window.params.curation.getCOLDetailsUrl;
    $.ajax({
        url: url,
        dataType: "json",
        data: {colId:colId},	
        success: function(data) {
            //show the popup
            populateNameDetails(data)
            console.log("======SUCCESS====");
            console.log(data);  
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
}
