
window.filesutra = {
  importFiles: function(callback, options) {
    //console.log(options)
    //alert(parent.resType)
    var filesutraServer = "http://localhost.indiabiodiversity.org/picker?resType="+parent.resType;

    if (options && options.dialogType == 'iframe') {
      var iframe = document.getElementById(options.parentId)
      iframe.src = filesutraServer;
    } else {
      window.open(filesutraServer, "Filesutra", "width=1000, height=600, top=100, left=300");
     /*$(function ()    {
      $.ajax({
    url: filesutraServer,
    "dataType" :"html",
    success: function(data){
      //console.log(data)
        
        $('#filesutra ').modal('show');
        $("#filesutra .modal-body .pickerbox").html(data);
        }   
    }); 
    });*/
        /*var app = angular.module("app", []);
console.log(app)
      app.directive('myDirective', function(){
        alert('reached');
        return {
        restrict: 'AE',
        compile: function(element, attrs){
        //here your all jQuery code will lie to ensure binding
        element.load(filesutraServer, function (data) {
        console.log(data);
        $('#filesutra ').modal('show');
        //$("#filesutra .modal-body .pickerbox").html(data);
        });
        }
        }
        });*/
      /*$('<div>').dialog({
            modal: true,
            open: function ()
            {
                $(this).load(filesutraServer);
            },         
            height: 513,
            width: 860,
            bgiframe: true,
            title: 'Dynamically Loaded Page'
        });*/
        /*var dialog1 = $("<div>").dialog(
        {
           autoOpen: false,
           modal: true,
           width:860,
           height:513,
           //top:20,
           position:"fixed",
           title: "filesutra",
           close: function (e, ui) { $(this).remove(); },
           //buttons: { "Ok": function () { $(this).dialog("close"); } }
        });
        
        dialog1.load(filesutraServer, function () {
            $(this).dialog('open');
        });*/
      /*$("#dialog").dialog(
       {
        bgiframe: true,
        autoOpen: true,
        height: 100,
        modal: true
       }
    );*/

        
    }
    window.filesutraCallback = callback;
  }
};

window.onmessage = function (e) {
  var data = e.data;
  if (data.type === 'filesutra') {
    window.filesutraCallback(data.data);
  }
};