/**
 * 
 */

CKEDITOR.editorConfig = function( config ) {

config.toolbar_myEditorToolbar = [   { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] },
    { name: 'paragraph',   items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','BidiLtr','BidiRtl' ] },
    { name: 'links',       items : [ 'Link','Unlink','Anchor' ] },
    { name: 'insert',      items : [ 'Image','Flash','Table','HorizontalRule','SpecialChar','PageBreak' ] },
    { name: 'clipboard',   items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
    { name: 'editing',     items : [ 'Find','Replace','-','SelectAll','-','SpellChecker', 'Scayt' ] },
    { name: 'styles',      items : [ 'Styles','Format','Font','FontSize' ] },
    { name: 'tools',       items : [ 'Maximize', 'ShowBlocks','-','About' ] }
],
config.removePlugins = 'elementspath',
config.toolbar = 'myEditorToolbar'
};
