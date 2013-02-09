<table class="table table-striped">
    <thead>
    <th><g:message code="crmContent.filename.label" default="Name"/></th>
    <th><g:message code="crmContent.modified.label" default="Modified"/></th>
    <th><g:message code="crmContent.length.label" default="Size"/></th>
    <th></th>
    </thead>
    <tbody>
    <g:each in="${list}" var="res" status="i">
        <g:set var="metadata" value="${res.metadata}"/>
        <tr class="status-${res.statusText} ${(i + 1) == params.int('selected') ? 'active' : ''}">
            <td>
                <img src="${crm.fileIcon(contentType: metadata.contentType)}" alt="${metadata.contentType}"
                     title="${metadata.contentType}"/>
                <g:link controller="crmContent" action="open"
                        params="${[id: res.id, disposition: 'attachment']}">${res.encodeAsHTML()}</g:link>
            </td>
            <td><g:formatDate date="${metadata.modified ?: metadata.created}" type="datetime"/></td>
            <td>${metadata.size}</td>
            <td>
                <crm:hasPermission permission="${controllerName + ':edit'}">
                    <g:link controller="crmContent" action="deleteAttachment" params="${[id:res.id, referer: request.forwardURI + '#' + view.id]}"
                            onclick="return confirm('${message(code: 'crmContent.button.delete.confirm.message', args: [res.name], default: 'Are you sure you want to delete the document?')}')"><i
                            class="icon-trash"></i></g:link>
                </crm:hasPermission>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>

<crm:hasPermission permission="${controllerName + ':edit'}">
    <g:uploadForm controller="crmContent" action="attachDocument">
        <g:hiddenField name="ref" value="${reference}"/>
        <g:hiddenField name="referer" value="${request.forwardURI + '#' + view.id}"/>
        <div class="form-actions">
            <crm:button action="attachDocument" visual="primary" icon="icon-upload icon-white"
                        label="crmContent.button.upload.label"/>
            <input type="file" name="file"/>
        </div>
    </g:uploadForm>
</crm:hasPermission>