<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmProduct.label', default: 'Product')}"/>
    <title><g:message code="crmProduct.list.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="crmProduct.list.title" subtitle="Sökningen resulterade i ${crmProductTotal} st produkter"
            args="[entityName]">
</crm:header>

<div class="row-fluid">
    <div class="span9">
        <table class="table table-striped">
            <thead>
            <tr>
                <crm:sortableColumn property="number"
                                    title="${message(code: 'crmProduct.number.label', default: 'Number')}"/>

                <crm:sortableColumn property="name"
                                    title="${message(code: 'crmProduct.name.label', default: 'Name')}"/>

                <crm:sortableColumn property="group.orderIndex"
                                    title="${message(code: 'crmProduct.group.label', default: 'Group')}"/>

                <crm:sortableColumn property="supplier.name"
                                    title="${message(code: 'crmProduct.supplier.label', default: 'Supplier')}"/>
            </tr>
            </thead>
            <tbody>
            <g:each in="${crmProductList}" var="crmProduct">
                <tr>

                    <td>
                        <g:link controller="crmProduct" action="show" id="${crmProduct.id}">
                            ${fieldValue(bean: crmProduct, field: "number")}
                        </g:link>
                    </td>

                    <td>
                        <g:link controller="crmProduct" action="show" id="${crmProduct.id}">
                            ${fieldValue(bean: crmProduct, field: "name")}
                        </g:link>
                    </td>

                    <td>
                        ${fieldValue(bean: crmProduct, field: "group")}
                    </td>

                    <td>
                        <g:link controller="crmContact" action="show"
                                id="${crmProduct.supplier?.id}">${crmProduct.supplier?.encodeAsHTML()}</g:link>
                    </td>

                </tr>
            </g:each>
            </tbody>
        </table>

        <crm:paginate total="${crmProductTotal}"/>

        <div class="form-actions btn-toolbar">
            <crm:selectionMenu visual="primary"/>
            <div class="btn-group">
                <crm:button type="link" action="create" visual="success" icon="icon-file icon-white"
                            label="crmProduct.button.create.label" permission="crmProduct:create"/>
            </div>
        </div>
    </div>

    <div class="span3">

    </div>

</div>

</body>
</html>
