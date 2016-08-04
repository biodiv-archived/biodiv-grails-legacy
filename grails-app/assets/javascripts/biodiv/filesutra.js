

var filesutraServer = "/url/path/to/the/fileOps/app";//fileOps server link
window.filesutra = {
  importFiles: function(callback, options) {
    //console.log(options)
    //var filesutraServer = "http://localhost:5000/picker?resType="+parent.resType;

    if (options && options.dialogType == 'iframe') {
      var iframe = document.getElementById(options.parentId)
      iframe.src = filesutraServer;
    } else {
      window.open(filesutraServer, "Filesutra", "width=1000, height=600, top=100, left=300");
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