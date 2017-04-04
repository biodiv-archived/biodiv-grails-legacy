

window.filesutra = {
  importFiles: function(callback, options) {
    //console.log(options)
    //var filesutraServer = "http://localhost:5000/picker";
    var filesutraServer = window.params.filesutraURL

    if (options && options.dialogType == 'iframe') {
        /*var ifrm = document.createElement("iframe");
        ifrm.src = filesutraServer;
        ifrm.style.width = "640px";
        ifrm.style.height = "480px";
        document.getElementsByClassName(options.parentSelector)[0].prepend(ifrm);*/
    } else {
      window.open(filesutraServer, "Filesutra", "width=1000, height=600, top=100, left=300");
    }
    window.filesutraCallback = callback;
  }
};

window.onmessage = function (e) {
  if (e.data.type === 'filesutra') {
    window.filesutraCallback(e.data);
  }
};
