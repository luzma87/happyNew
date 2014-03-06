CKEDITOR.plugins.add('createPdf', {
    icons : 'createPdf',
    init  : function (editor) {
        editor.addCommand('createPdf', {
            exec : function (editor) {
                var id = editor.name;
                var cont = $("#" + id).val();
                var url = editor.config.createPdf.saveUrl;
                var data = editor.config.createPdf.saveData;

                data[id] = cont;

                $.ajax({
                    type    : "POST",
                    url     : url,
                    data    : data,
                    success : function (msg) {
                        editor.config.createPdf.createDone(msg);
                    }
                });
            }
        });
        editor.ui.addButton('CreatePdf', {
            label   : 'Crear Pdf',
            command : 'createPdf',
            toolbar : 'insert'
        });
    }
});