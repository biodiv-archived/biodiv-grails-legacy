package content

import grails.converters.JSON
import org.gualdi.grails.plugins.ckeditor.utils.PathUtils
import org.gualdi.grails.plugins.ckeditor.utils.ImageUtils
import org.gualdi.grails.plugins.ckeditor.utils.FileUtils
import org.gualdi.grails.plugins.ckeditor.utils.MimeUtils
import org.gualdi.grails.plugins.ckeditor.OpenFileManagerConnectorController
import org.gualdi.grails.plugins.ckeditor.CkeditorConfig
 
class BiodivOpenFileManagerConnectorController extends OpenFileManagerConnectorController {

    def messageSource

    /**
     * Entry point
     *
     */
    def index = {
        //HACK to handle module level folders
        def spaceAndModule = params.space
        def arr = spaceAndModule.split(File.separator);
        def space,module;
        if(arr.length == 2) {
            space = arr[0];
            params.space = space;
            module = arr[1];
            params.module = module;
        } else if (arr.length == 1) {
            space = arr[0]
            params.space = space;
            params.module = '';
        }

        render (controller: "", view: "/biodivofm/ofm")
    }

    /**
     * Filemanager connector
     * 
     */
    def fileManager = {
        log.debug "begin fileManager()"
 
        def mode = params.mode
        def type = params.type 
        //HACK to handle module level folders
        def spaceAndModule = params.space
        def arr = spaceAndModule.split(File.separator);
        def space,module;
        if(arr.length == 2) {
            space = arr[0];
            params.space = space;
            module = arr[1];
            params.module = module;
        } else if (arr.length == 1) {
            space = arr[0]
            params.space = space;
            params.module = '';
        }

        def showThumbs = params.showThumbs == 'true'

        def baseUrl = PathUtils.getBaseUrl(params)
        def baseDir = getBaseDir(baseUrl)
        if(module) {
            baseUrl = baseUrl + PathUtils.checkSlashes(module, "L+ R-")
            baseDir = baseDir + PathUtils.checkSlashes(module, "L+ R-")
        }

         if (log.isDebugEnabled()) {
            log.debug "=============================================="
            log.debug params
            log.debug "baseDir = ${baseDir}"
            log.debug "baseUrl = ${baseUrl}"
            log.debug "type = ${type}"
            log.debug "space = ${space}"
            log.debug "showThumbs = ${showThumbs}"
            log.debug "=============================================="
        }
println mode        
        def resp
        switch (mode) {
            case 'getinfo':
                resp = getInfo(baseDir, baseUrl, params.path)
                break

            case 'getfolder':
                resp = getFolder(baseDir, baseUrl, module, params.path, showThumbs)
                break

            case 'rename':
                resp = rename(baseDir, params.old, params.'new', type)
                break

            case 'delete':
                resp = delete(baseDir, params.path)
                break

            case 'add':
                resp = add(baseDir, params.currentpath, type, request)
                break

            case 'addfolder':
                resp = addFolder(baseDir, params.path, params.name)
                break

            case 'download':
                resp = download(baseDir, params.path)
                break
        }
        log.debug "end fileManager()"

        if (resp) {
            render resp
        }
        else {
            return null
        }
    }

    private getBaseDir(baseUrl) {
        def config = grailsApplication.config.ckeditor

        def baseDir
        if (config?.upload?.baseurl) {
            baseDir = PathUtils.checkSlashes(config?.upload?.basedir, "L+ R-") + PathUtils.checkSlashes(baseUrl, "L+ R+")
        }
        else {
            baseDir = servletContext.getRealPath(baseUrl)
            baseDir = PathUtils.checkSlashes(baseDir, "R+")
        }
println "=========================="
println baseDir
        def f = new File(baseDir)
        if (!f.exists()) {
            f.mkdirs()
        }

        return baseDir
    }

    private getInfo(baseDir, baseUrl, path) {
        def resp = getFileInfo(baseDir, baseUrl, path)
        return (resp as JSON).toString()
    }

    private getFolder(baseDir, baseUrl, module, path, showThumbs) {
        println "***************************"
        println baseDir
        println baseUrl
        println path
        println "***************************"
        def resp = [:]
        def currentDir = new File(baseDir + PathUtils.checkSlashes(path, "L- R+"))
        println currentDir 
        if (currentDir.exists()) {
            currentDir.eachFile { file ->
                if (!file.name.startsWith('.')) {
                    def fname = path + file.name
                    println fname
                    resp["\"${fname}\""] = getFileInfo(baseDir, baseUrl, fname, showThumbs)
                }
            }
        }

        return (resp as JSON).toString() 
    }

    private getFileInfo(baseDir, baseUrl, path, showThumbs = true) {
        def currentObject = baseDir + PathUtils.checkSlashes(path, "L- R-")
        def file = new File(currentObject)

        def width = ''
        def height = ''
        def fileSize = 0
        def preview
        def fileType
        def properties
        if (file.isDirectory()){
            path = PathUtils.checkSlashes(path, "L+ R+", true)
            preview = g.resource(dir: "js/ofm/images/fileicons", file: "_Open.png", plugin: "ckeditor")
            fileType = 'dir'
            properties = [
                'Date Created': '',
                'Date Modified': '',
                'Width': '',
                'Height': '',
                'Size': ''
            ]
        }
        else {
            def fileParts = PathUtils.splitFilename(file.name)
            fileType = fileParts.ext?.toString()?.toLowerCase()
            fileSize = file.length()

            preview = g.resource(dir: "js/ofm/images/fileicons", file: "${fileParts.ext.toLowerCase()}.png", plugin: "ckeditor")
            if (fileType in CkeditorConfig.OFM_IMAGE_EXTS) {
                if (showThumbs) {
                    def config = grailsApplication.config.ckeditor
                    if (config?.upload?.baseurl && config?.upload?.baseurl != '/') {
                        preview = g.resource(file: PathUtils.checkSlashes(config?.upload?.baseurl, "L+ R-") + baseUrl + path)    
                    }
                    else {
                        preview = g.resource(file: baseUrl + path)
                    }
                }
                def imgDim = ImageUtils.calculateImageDimension(file, fileType)
                if (imgDim) {
                    width = imgDim.width
                    height = imgDim.height
                }
            }

            properties = [
                'Date Created': '',
                'Date Modified': new Date(file.lastModified()).format("dd-MM-yyyy HH:mm:ss"),
                'Width': width,
                'Height': height,
                'Size': fileSize
            ]
        }

        def resp = [
            'Path': path,
            'Filename': file.name,
            'File Type': fileType,
            'Preview': preview,
            'Properties': properties,
            'Error': '',
            'Code': 0
        ]

        return resp
    }

    private rename(baseDir, oldName, newName, type) {
        def oldFile = new File(baseDir + PathUtils.checkSlashes(oldName, "L-"))
        def newFile = new File(oldFile.parent, newName)

        def isDirectory = oldFile.isDirectory()

        def path
        if (isDirectory) {
            path = PathUtils.getFilePath(PathUtils.checkSlashes(oldName, "R-"))
        }
        else {
            path = PathUtils.getFilePath(oldName)
        }

        def resp
        if (PathUtils.isSafePath(baseDir, newFile)) {
            if (!newFile.exists()) {
                if (isDirectory || FileUtils.isFileAllowed(newName, type)) {
                    try {
                        if(oldFile.renameTo(newFile)) {
                            def tmpJSON = [
                                'Old Path' : oldName,
                                'Old Name' : oldFile.name,
                                'New Path' : path + newFile.name + (isDirectory ? '/' : ''),
                                'New Name' : newFile.name,
                                'Error' : '',
                                'Code' : 0
                            ]

                            resp = (tmpJSON as JSON).toString()
                        }
                        else {
                            resp = error('ofm.invalidFilename', 'Invalid file name')
                        }
                    }
                    catch (SecurityException se) {
                        resp = error('ofm.noPermissions', 'No permissions')
                    }
                }
                else {
                    resp = error('ofm.invalidFilename', 'Invalid file name');
                }
            }
            else {
                resp = error('ofm.fileAlreadyExists', 'File exists')
            }
        }
        else {
            resp = error('ofm.noPermissions', 'No permissions')
        }

        return resp
    }

    private delete(baseDir, path) {
        def file = new File(baseDir + PathUtils.checkSlashes(path, "L-"))
        
        def resp
        if (PathUtils.isSafePath(baseDir, file)) {
            if (file.exists()) {
                if (file.isDirectory()) {
                    try {
                        def deleteClosure
                        deleteClosure = {
                           it.eachDir(deleteClosure)
                           it.eachFile {
                               it.delete()
                           }
                        }
                        deleteClosure file
                        file.delete()

                        def tmpJSON = [
                            'Path': path,
                            'Error': '',
                            'Code': 0
                        ]

                        resp = (tmpJSON as JSON).toString()
                    }
                    catch(SecurityException se) {
                        resp = error('ofm.noPermissions', 'No permissions')
                    }
                }
                else {
                    try {
                        if(file.delete()) {
                            def tmpJSON = [
                                'Path': path,
                                'Error': '',
                                'Code': 0
                            ]

                            resp = (tmpJSON as JSON).toString()
                        }
                        else {
                            resp = error('ofm.invalidFilename','Invalid file name')
                        }
                    }
                    catch (SecurityException se) {
                        resp = error('ofm.noPermissions', 'No permissions')
                    }
                }
            }
            else {
                resp = error('ofm.fileDoesNotExists','File does not exists')
            }
        }
        else {
            resp = error('ofm.noPermissions', 'No permissions')    
        }

        return resp
    }

    private add(baseDir, currentPath, type, request) {
        def overwrite = grailsApplication.config.ckeditor.upload.overwrite ?: false

        def file
        try {
            file = request.getFile("newfile")
        }
        catch (Exception e) {
            file = null
        }

        def resp
        if (!file) {
            resp = error('ofm.invalidFilename','Invalid file', true)
        }
        else {
            def uploadPath = new File(baseDir + PathUtils.checkSlashes(currentPath, "L- R+"))
            def newName = file.originalFilename

            def f = PathUtils.splitFilename(newName)
            if (FileUtils.isAllowed(f.ext, type)) {
                def fileToSave = new File(uploadPath, newName)
                if ( !overwrite ) {
                    def idx = 1
                    while (fileToSave.exists()) {
                        newName = "${f.name}(${idx}).${f.ext}"
                        fileToSave = new File(uploadPath, newName)
                        idx++
                    }
                }
                file.transferTo(fileToSave)

                def tmpJSON = [
                    'Path': currentPath,
                    'Name': newName,
                    'Error': '',
                    'Code': 0
                ]
                resp = (tmpJSON as JSON).toString()
                resp = "<textarea>${resp}</textarea>"
            }
            else {
                resp = error('ofm.invalidFileType', 'Invalid file type', true)
            }
        }

        return resp
    }
    
    private addFolder(baseDir, path, name) {
        def newDir = new File(baseDir + PathUtils.checkSlashes(path, "L- R+") + name)

        def resp
        if (newDir.exists()) {
            resp = error('ofm.directoryAlreadyExists', 'Directory already exists!')
        }
        else {
            try {
                if (newDir.mkdir()) {
                    def tmpJSON = [
                        'Parent': path,
                        'Name': name,
                        'Error': '',
                        'Code': 0
                    ]
                    resp = (tmpJSON as JSON).toString()
                }
                else {
                    resp = error('ofm.invalidFolderName', 'invalid folder name')
                }
            }
            catch (SecurityException se) {
                resp = error('ofm.noPermissions', 'No permissions')
            }
        }

        return resp
    }

    private download(baseDir, path) {
        def file = new File(baseDir + PathUtils.checkSlashes(path, "L-"))

        response.setHeader("Content-Type", "application/force-download")
        response.setHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
        response.setHeader("Content-Length", "${file.size()}")
        response.setHeader("Content-Transfer-Encoding", "Binary");

        def os = response.outputStream

        byte[] buff = null
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))
        try
        {
            buff = new byte[2048]
            int bytesRead = 0
            while ((bytesRead = bis.read(buff, 0, buff.size())) != -1) {
                os.write(buff, 0, bytesRead)
            }
        }
        finally {
            bis.close()
            os.flush()
            os.close()
        }

        return null
    }

    def show = {
        def config = grailsApplication.config.ckeditor
        def filename = PathUtils.checkSlashes(config?.upload?.basedir, "L+ R+") + params.filepath
        def ext = PathUtils.splitFilename(params.filepath).ext

        def contentType = MimeUtils.getMimeTypeByExt(ext)
        def file = new File(filename)

        response.setHeader("Content-Type", contentType)
        response.setHeader("Content-Length", "${file.size()}")

        def os = response.outputStream

        byte[] buff = null
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))
        try
        {
            buff = new byte[2048]
            int bytesRead = 0
            while ((bytesRead = bis.read(buff, 0, buff.size())) != -1) {
                os.write(buff, 0, bytesRead)
            }
        }
        finally {
            bis.close()
            os.flush()
            os.close()
        }

        return null
    }

    private error(key, message, useTextarea = false) {
        def msg
        try {
            msg = messageSource.getMessage(key, null, request.getLocale())
        }
        catch (org.springframework.context.NoSuchMessageException nsme) {
            msg = message
        }
        def error = ['Error': msg, 'Code': -1]
        def jsonError = (error as JSON).toString()
        if (useTextarea) {
            jsonError = "<textarea>${jsonError}</textarea>"
        }

        return jsonError
    }
}
