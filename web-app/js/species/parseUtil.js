Array.prototype.contains = function(v) {
    for(var i = 0; i < this.length; i++) {
        if(this[i] === v) return true;
    }
    return false;
};

Array.prototype.unique = function() {
    var arr = [];
    for(var i = 0; i < this.length; i++) {
        if(!arr.contains(this[i])) {
            arr.push(this[i]);
        }
    }
    return arr; 
}

String.prototype.splitCSV = function(sep) {
    for (var thisCSV = this.split(sep = sep || ","), x = thisCSV.length - 1, tl; x >= 0; x--) {
        if (thisCSV[x].replace(/"\s+$/, '"').charAt(thisCSV[x].length - 1) == '"') {
            if ((tl = thisCSV[x].replace(/^\s+"/, '"')).length > 1 && tl.charAt(0) == '"') {
                thisCSV[x] = thisCSV[x].replace(/^\s*"|"\s*$/g, '').replace(/""/g, '"');
            } else if (x) {
                thisCSV.splice(x - 1, 2, [thisCSV[x - 1], thisCSV[x]].join(sep));
            } else thisCSV = thisCSV.shift().split(sep).concat(thisCSV);
        } else thisCSV[x].replace(/""/g, '"');
    } return thisCSV;
}


function parseData(csvFile, options) {
    $.get(csvFile, function(data) {
        parseCSVData(data, options);	
    });
}

function parseCSVData(data, options) {
    var defaults = {
        tableClass: "CSVTable",
        theadClass: "",
        thClass: "",
        tbodyClass: "",
        trClass: "",
        tdClass: "",
        loadingImage: "",
        loadingText: "Loading CSV data...",
        separator: ",",
        startLine: 0
    };	
    
    options = $.extend({}, defaults, options);
 
    var rowData = new Array();
    var columns = new Array();

    //console.log(rowData.length)
    var lines = data.replace('\r','').split('\n');
    var printedLines = 0;
    var headerCount = 0;
    var error = '';
    var foundHeader = false;
    $.each(lines, function(lineCount, line) {
    	line = $.trim(line);
        try{
            if ((!foundHeader) || (lineCount == options.startLine) && (typeof(options.headers) == 'undefined')) {
                var headers = $.csv.toArray(line);
                foundHeader = isValidRow(headers);
                headers = getSafeHeaders(headers);
                if(foundHeader){
	                headerCount = headers.length;
	                $.each(headers, function(headerCount, header) {
	                	var columnName = header;
	                    columns.push({id:columnName, name: columnName, field: columnName, editor: Slick.Editors.Text, sortable:false, minWidth: 100, header:getHeaderMenuOptions()});
	                    console.log(columns);
	                });
                }
            } else if (lineCount >= options.startLine) {
                var items = $.csv.toArray(line);
                if(isValidRow(items)) {
                    printedLines++;
                    if (items.length != headerCount) {
                        error += 'Error on line ' + lineCount + ': Item count (' + items.length + ') does not match header count (' + headerCount + ') \n';
                    }
                    var d = (rowData[printedLines-1] = {});
                    $.each(items, function(itemCount, item) {
                        var dataKey = columns[itemCount]['field']
                        d[dataKey] = item;
                    });
                }
            }
        }catch(e){
            error += e + '\n';
        }
    });
    if (error) {
        alert(error);
    }else{
        columns.push(getMediaColumnOptions());


	    if(options.callBack){
	        options.callBack(rowData, columns);
	    }
    }
}

function getSafeHeaders(array){
	var newArray = new Array();
	$.each(array, function(index, value) {
		if(value === undefined || value.trim() === ''){
			value = getDefaultColumnName(newArray, array.length)
		}
		newArray.push($.trim(value))
	});
	return newArray
}

function getDefaultColumnName(array, maxLength){
	for (var i=1;i<=maxLength;i++){
		var columnName = 'column' + i
		if(!array.contains(columnName)){
			return columnName
		}
	}
}

function isValidRow(array){
	var isValid = false;
	$.each(array, function(index, value) {
		if(value !== undefined && value !== ''){
			isValid = true
			return false;
		}
	});
	return isValid
}

/*
function getValidData(data){
	var validData = new Array();
	 $.each(data, function(index, value) {
		 if(isValidCollection(value)){
			 validData.push(value);
		 }
	 });
	 return validData;
}

function isValidCollection(obj){
	var isValid = false;
	$.each( obj, function( key, value ) {
		if(value !== undefined && value !== ''){
			isValid = true;
			return false;
		}
	});
	return isValid;
}
*/

function getMediaColumnOptions() {
    return {
    id: "Media",
    name: "Media",
    field:'Media',
    width: 100,
    selectable: false,
    resizable: true,
    formatter:addMediaFormatter
  }
}

function getHeaderMenuOptions() {
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
            /*,
            {
                iconCssClass: "icon-help",
                title: "Help",
                command: "help"
            }*/
            ]
        }
    }
}
