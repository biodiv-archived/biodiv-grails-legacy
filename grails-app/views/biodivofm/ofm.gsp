<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8"/>

        <title>File Manager</title>

        <link rel="stylesheet" type="text/css" href="${resource(dir: 'js/ofm/styles', file:'reset.css', plugin: 'ckeditor')}" />
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'js/ofm/scripts/jquery.filetree', file:'jqueryFileTree.css', plugin: 'ckeditor')}" />
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'js/ofm/scripts/jquery.contextmenu', file:'jquery.contextMenu-1.01.css', plugin: 'ckeditor')}" />
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'js/ofm/styles', file:'filemanager.css', plugin: 'ckeditor')}" />
        <!--[if IE]>
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'js/ofm/styles', file:'ie.css', plugin: 'ckeditor')}" />
        <![endif]-->

        <script type="text/javascript">
            var ofmBase ="${resource(dir: 'js/ofm', plugin: 'ckeditor')}";
            var culture = '${ofm.currentLocale()}';
            var autoload = true;
            var showFullPath = false;
            var browseOnly = false;
            var defaultViewMode = '${params.viewMode}';
            var fileRoot = '/';
            var fileConnector = '${params.fileConnector}';
            var tmp = '${params.showThumbs}';
            var showThumbs = (tmp == '' ? false : eval(tmp));
            var space = '${params.space+File.separator+params.module}';
            var type = '${params.type}';
            var webRoot = '${ofm.baseUrl(space: params.space, type: params.type)+File.separator+params.module}';
            webRoot = webRoot.replace('//','/');
        </script>
    </head>

    <body>
        <div>
            <form id="uploader" method="post">
                <button id="home" name="home" type="button" value="Home">&nbsp;</button>

                <h1></h1>

                <div id="uploadresponse"></div>
                <input id="mode" name="mode" type="hidden" value="add"/>
                <input id="currentpath" name="currentpath" type="hidden"/>
                <input id="newfile" name="newfile" type="file"/>
                <button id="upload" name="upload" type="submit" value="Upload"></button>
                <button id="newfolder" name="newfolder" type="button" value="New Folder"></button>
                <button id="grid" class="ON" type="button">&nbsp;</button>
                <button id="list" type="button">&nbsp;</button>
            </form>

            <div id="splitter">
                <div id="filetree"></div>

                <div id="fileinfo">
                    <h1></h1>
                </div>
            </div>

            <ul id="itemOptions" class="contextMenu">
                <li class="select"><a href="#select"></a></li>
                <li class="download"><a href="#download"></a></li>
                <li class="rename"><a href="#rename"></a></li>
                <li class="delete separator"><a href="#delete"></a></li>
            </ul>

            <script type="text/javascript" src="${resource(dir: 'js/ofm/scripts', file:'jquery-1.6.1.min.js', plugin: 'ckeditor')}"></script>
            <script type="text/javascript" src="${resource(dir: 'js/ofm/scripts', file:'jquery.form-2.63.js', plugin: 'ckeditor')}"></script>
            <script type="text/javascript" src="${resource(dir: 'js/ofm/scripts/jquery.splitter', file:'jquery.splitter-1.5.1.js', plugin: 'ckeditor')}"></script>
            <script type="text/javascript" src="${resource(dir: 'js/ofm/scripts/jquery.filetree', file:'jqueryFileTree.js', plugin: 'ckeditor')}"></script>
            <script type="text/javascript" src="${resource(dir: 'js/ofm/scripts/jquery.contextmenu', file:'jquery.contextMenu-1.01.js', plugin: 'ckeditor')}"></script>
            <script type="text/javascript" src="${resource(dir: 'js/ofm/scripts', file:'jquery.impromptu-3.1.min.js', plugin: 'ckeditor')}"></script>
            <script type="text/javascript" src="${resource(dir: 'js/ofm/scripts', file:'jquery.tablesorter-2.0.5b.min.js', plugin: 'ckeditor')}"></script>
            <script type="text/javascript" src="${resource(dir: 'js/ofm/scripts', file:'filemanager.js', plugin: 'ckeditor')}"></script>
        </div>
    </body>
</html>
