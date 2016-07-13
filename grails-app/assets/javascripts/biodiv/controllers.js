var filesutraControllers = angular.module("filesutraControllers", ["filesutraServices"]);

filesutraControllers.controller("AppCtrl", ['$scope', '$http', '$location', "fileService", "authService",
  function($scope, $http, $location, fileService, authService) {
     $scope.toggleObject = true;
    $scope.selectApp = function(app) {
      $scope.runningApp = app;
      $location.path(app);
    }
    if($location.path()==""){
      //console.log($location.absUrl())
      $scope.resType = $location.absUrl().split("=")[1];
      //console.log($scope.resType)
    }
        $scope.login = function(app) {
      var redirectUrl = '/auth/' + (app == 'AmazonCloudDrive'? 'amazon' : app.toLowerCase());
      if (window.opener) {
        window.location = redirectUrl;
      } else {
        var oAuthWndow = window.open(redirectUrl, "Filesutra", "width=800, height=600, top=100, left=300");
        var interval = window.setInterval(function() {
          if (oAuthWndow.location.href.indexOf('picker') != -1) {
            oAuthWndow.close();
            location.reload();
          }
        }, 1000);
      }
    }

    $scope.logout = function(app) {
      var connectedAppPos = $scope.appSettings.connectedApps.indexOf(app)
      if (connectedAppPos != -1) {
        authService.logout(app, function(data) {
          if (data.success) {
            $scope.appSettings.connectedApps.splice(connectedAppPos, 1);
            $location.path(app);
          }
        });
      }
    }

    $scope.isConnected = function(app) {
     // console.log($scope.appSettings.connectedApps);
      if ($scope.appSettings.connectedApps.indexOf(app) != -1) {
        return true;
      } else {
        return false;
      }
    }


      $scope.uploadFile = function(event){
       //$('#submitIt').submit();
        var me1 = $('#submitIt')
        me1.ajaxSubmit({
              url : "/observation/upload_resource",//window.params.observation.uploadUrl, // or whatever
              success : function (responseXML, statusText, xhr, form) {
                  //alert("The server says: " + response);
                  var me = this;
                  console.log(responseXML)
            
            var images = [];
            
            var i=0
            var $s = $(responseXML).find('resources').find('res');
            var x = $s.length;
            var uploadedObjType
            $s.each(function() {
                me.jobId = $(this).attr('jobId');
                var fileName = $(this).attr('fileName');
                var type = $(this).attr('type');          
                uploadedObjType = type;
                images.push({jobId:$(this).attr('jobId'), file:fileName, url:$(this).attr('url'), thumbnail:$(this).attr('thumbnail'), type:type, title:fileName});
                x--;
            });
            console.log(images);
            /*var importedFiles = [];
            for(var j=0; j<images.length; j++){
              importedFiles.push({mimetype:"image/jpeg",filename:"hello.jpg",size:2000,url:"http://indiabiodiversity.localhost.org/biodiv/observations/"+images[i].file})
            }
            console.log(importedFiles);*/
            var message = {
              type  : 'filesutra',
              data   :  images
            }
            if (window.opener) {
              window.opener.postMessage(message, '*');
              window.close();
            } else {
              // iframe
              parent.postMessage(message, '*');
            }
           

              },
              error : function(xhr, ajaxOptions, thrownError){
                console.log(thrownError);
              }
          });
         

        /*$.ajax({
        url: window.params.observation.uploadUrl,    //give your url here
        //type: 'POST',
        dataType: "json",
        //data: logindata,
        success: function ( data ){
        //  alert(data);    do your stuff
        },
        error: function ( data ){
        //  alert(data);    do your stuff
        }
    });*/
        
       // alert('came');
       // var files = event.target.files;
    };

    $scope.selectItem = function (item) {
      if (item.type == "folder") {
        
        $location.path($location.path()+'/'+item.id);
      }else{
            var addToArray = true;
      for(var i=0;i<$scope.userGroupId.length;i++){
        if($scope.userGroupId[i]['id'] == item.id){
          var index = i;//$scope.userGroupId.indexOf(item.id);
          if (index > -1) {
              $scope.userGroupId.splice(index, 1);
              $scope.itemId.splice(index, 1);
          }
          addToArray = false;
          
        }
      }
      if(addToArray){
        $scope.itemId.push(item.id)
        $scope.userGroupId.push(item);
      }
    }

      $scope.selectedItem = item;
      //console.log($scope.userGroupId);
      //console.log($scope.itemId);

    }

    $scope.import = function() {
      var uploadCount = 0;
      var importedFiles = [];
     for(var i=0;i<$scope.userGroupId.length;i++){
      fileService.import($scope.app, $scope.userGroupId[i], function(data) {
        //console.log(data);
        uploadCount++;
        importedFiles.push(data);
        if(uploadCount == $scope.userGroupId.length ){
        var message = {
          type  : 'filesutra',
          data   :  importedFiles
        }
        if (window.opener) {
          window.opener.postMessage(message, '*');
          window.close();
        } else {
          // iframe
          parent.postMessage(message, '*');
        }
      }
      });
    }
    }
    

    $scope.init = function(appSettings){
      $scope.appSettings = appSettings;
    }
    $scope.backButton = function(){
      window.history.back();
    }

    $scope.$on("$locationChangeSuccess", function (event, newUrl) {
      $scope.gettingList(0);
      //console.log($location.search('resType'));
    });

    $scope.gettingList = function(code){
      $scope.showBackButton = false;

            var path = $location.path();
      var chunks = path.split("/");
      var app, folderId;
      if (chunks.length < 2) {
        $scope.selectApp("Local");
        return;
      } else {
        app = chunks[1];
        $scope.app = app;
        $scope.runningApp = app;

      }
      if (chunks.length > 2) {
        $scope.showBackButton = true;
        folderId = chunks[chunks.length - 1];
      }
      
      if($scope.app == "Facebook" || $scope.app == "Flickr"){
        
      if(code==0){
        $scope.showButton = false;

        delete $scope.items;
      $scope.afterTokenVal = '';

        if ($scope.isConnected(app)) {
          $scope.userGroupId = [];
        $scope.itemId = [];
           fileService.getItems(app, folderId, $scope.afterTokenVal, function (items) {
            //delete $scope.items;
             $scope.items = [];
             $scope.afterTokenVal = items.afterval;
             if(items.listresponse.length < 25){
                    $scope.showButton = false;

             }else{
              $scope.showButton = true;
             }
             for(var i=0; i< items.listresponse.length;i++){
               $scope.items.push(items.listresponse[i]);
             }
            //$scope.items.push(items.listresponse);
          });
        }
       }else{
        $scope.isDisabled = true;
        fileService.getItems(app, folderId, $scope.afterTokenVal, function (items) {
            $scope.afterTokenVal = items.afterval;
            console.log(items);
            if(items!="error"){
            if(items.listresponse.length < 25){
                    $scope.showButton = false;

             }else{
              $scope.showButton = true;
              $scope.isDisabled = false;
             }
                       for(var i=0; i< items.listresponse.length;i++){
               $scope.items.push(items.listresponse[i]);
             }
           }else{
              $scope.showButton = false;
           }
          });

       }
     } else {
      if ($scope.isConnected(app)) {
        $scope.userGroupId = [];
        $scope.itemId = [];
        delete $scope.items;
        $scope.items = null
           fileService.getListItems(app, folderId, function (items) {
            $scope.items = items;
            });
         }
     }
      
    }
}]);