<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmProduct.label', default: 'Product')}"/>
    <title><g:message code="crmProduct.find.title" args="[entityName]"/></title>
    <r:require module="select2"/>
    <r:script>
        $(document).ready(function () {
        });
    </r:script>
</head>

<body>

<div class="row-fluid">
    <div class="span9">

        <crm:header title="crmProduct.find.title" args="[entityName]"/>

        <g:form action="list">

            <div class="row-fluid">

                <f:with bean="cmd">
                    <div class="span4">
                            <f:field property="number" label="crmProduct.number.label"
                                     input-class="input-large" input-autofocus=""
                                     input-placeholder="${message(code: 'crmProductQueryCommand.number.placeholder', default: '')}"/>
                            <f:field property="name" label="crmProduct.name.label"
                                     input-class="input-large"
                                     input-placeholder="${message(code: 'crmProductQueryCommand.name.placeholder', default: '')}"/>
                            <f:field property="productGroup" label="crmProduct.group.label"
                                     input-class="input-large"
                                     input-placeholder="${message(code: 'crmProductQueryCommand.productGroup.placeholder', default: '')}"/>
                    </div>

                    <div class="span4">
                            <f:field property="suppliersNumber" label="crmProduct.suppliersNumber.label"
                                     input-class="input-large"
                                     input-placeholder="${message(code: 'crmProductQueryCommand.suppliersNumber.placeholder', default: '')}"/>
                            <f:field property="supplier" label="crmProduct.supplier.label"
                                     input-class="input-large"
                                     input-placeholder="${message(code: 'crmProductQueryCommand.supplier.placeholder', default: '')}"/>
                            <f:field property="barcode" label="crmProduct.barcode.label"
                                     input-class="input-large"
                                     input-placeholder="${message(code: 'crmProductQueryCommand.barcode.placeholder', default: '')}"/>
                            <f:field property="customsCode" label="crmProduct.customsCode.label"
                                     input-class="input-large"
                                     input-placeholder="${message(code: 'crmProductQueryCommand.customsCode.placeholder', default: '')}"/>
                    </div>

                    <div class="span4">
                            <f:field property="price" label="crmProduct.price.label"
                                     input-class="input-small"
                                     input-placeholder="${message(code: 'crmProductQueryCommand.price.placeholder', default: '')}"/>
                            <f:field property="weight" label="crmProduct.weight.label"
                                     input-class="input-small"
                                     input-placeholder="${message(code: 'crmProductQueryCommand.weight.placeholder', default: '')}"/>
                    </div>

                </f:with>

            </div>

            <div class="form-actions btn-toolbar">
                <crm:selectionMenu visual="primary">
                    <crm:button action="list" icon="icon-search icon-white" visual="primary"
                                label="crmProduct.button.find.label"/>
                </crm:selectionMenu>
                <crm:button type="link" group="true" action="create" visual="success" icon="icon-file icon-white"
                            label="crmProduct.button.create.label" permission="crmProduct:create"/>
            </div>

        </g:form>
    </div>

    <div class="span3">
    </div>
</div>

</body>
</html>
