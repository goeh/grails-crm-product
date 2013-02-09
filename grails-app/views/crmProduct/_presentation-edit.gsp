<r:require module="content-editor"/>
<r:script>
    $(document).ready(function() {
        var editor = CKEDITOR.replace('template',
        {
            width : "98%",
            height : "400px",
            resize_enabled : true,
            startupFocus : false,
            skin : "moono",
            filebrowserBrowseUrl: "${createLink(controller: 'crmContentEdit', action: 'index', params: [ref: reference, type:'image'])}",
            filebrowserUploadUrl: "${createLink(controller: 'crmContentEdit', action: 'upload', params: [ref: reference])}",
            filebrowserWindowWidth: "1024",
            filebrowserWindowHeight: "768",
            toolbarGroups: [
                { name: 'clipboard',   groups: [ 'clipboard', 'undo' ] },
                { name: 'editing',     groups: [ 'find', 'selection' ] },
                { name: 'links' },
                { name: 'insert',      groups: ['image'] },
                { name: 'forms' },
                { name: 'document',    groups: [ 'mode' ] },
                { name: 'others' },
                '/',
                { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
                { name: 'paragraph',   groups: [ 'list', 'indent', 'blocks', 'align' ] },
                { name: 'styles' },
                { name: 'colors' },
                { name: 'about' }
            ]
        });
    });
</r:script>

<textarea id="template" name="template" cols="80" rows="24">
    <g:include action="presentation" id="${crmProduct.id}"/>
</textarea>