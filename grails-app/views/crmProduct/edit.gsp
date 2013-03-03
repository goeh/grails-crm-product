<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmProduct.label', default: 'Product')}"/>
    <title><g:message code="crmProduct.edit.title" args="[entityName, crmProduct]"/></title>
    <r:require modules="autocomplete"/>
    <r:script>
        $(document).ready(function () {
            // Supplier.
            $("input[name='supplier']").autocomplete("${createLink(action: 'autocompleteSupplier')}", {
                remoteDataType: 'json',
                preventDefaultReturn: true,
                selectFirst: true,
                onItemSelect: function(item) {
                    var id = item.data[0];
                    $("header h1 small").text(item.value);
                },
                onNoMatch: function() {
                    $("header h1 small").text($("input[name='supplier']").val());
                }
            });

            $("#btn-add-price").click(function(ev) {
                $.get("${createLink(action: 'addPrice', id: crmProduct.id)}", function(html) {
                    var table = $("#price-list");
                    $("tbody", table).append(html);
                    table.renumberInputNames();
                    $("tr:last :input:enabled:first", table).focus();
                });
            });
        });
    </r:script>
</head>

<body>

<crm:header title="crmProduct.edit.title" subtitle="${(crmProduct.supplier ?: '').encodeAsHTML()}"
            args="[entityName, crmProduct]"/>

<g:hasErrors bean="${crmProduct}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmProduct}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="edit">

    <g:hiddenField name="id" value="${crmProduct.id}"/>
    <g:hiddenField name="version" value="${crmProduct.version}"/>

    <div class="tabbable">
        <ul class="nav nav-tabs">
            <li class="active"><a href="#main" data-toggle="tab"><g:message code="crmProduct.tab.main.label"/></a>
            </li>
            <li><a href="#prices" data-toggle="tab"><g:message code="crmProduct.tab.prices.label"/><crm:countIndicator
                    count="${crmProduct.prices.size()}"/></a></li>
            <crm:pluginViews location="tabs" var="view">
                <crm:pluginTab id="${view.id}" label="${view.label}" count="${view.model?.totalCount}"/>
            </crm:pluginViews>
        </ul>

        <div class="tab-content">
            <div class="tab-pane active" id="main">

                <f:with bean="crmProduct">

                    <div class="row-fluid">

                        <div class="span4">
                            <div class="row-fluid">
                                <f:field property="number" input-autofocus="" input-class="span12"/>
                                <f:field property="name" input-class="span12"/>
                                <f:field property="displayNumber" input-class="span12"/>
                                <f:field property="displayName" input-class="span12"/>
                            </div>
                        </div>

                        <div class="span4">
                            <div class="row-fluid">
                                <f:field property="supplier">
                                    <g:textField name="supplier" value="${crmProduct.supplier?.name}" class="span12"
                                                 autocomplete="off"/>
                                </f:field>
                                <f:field property="suppliersNumber" input-class="span6"/>
                                <f:field property="group" input-class="span12"/>
                            </div>
                        </div>

                        <div class="span4">
                            <div class="row-fluid">
                                <f:field property="barcode" input-class="span6"/>
                                <f:field property="customsCode" input-class="span6"/>
                                <f:field property="weight">
                                    <g:textField name="weight" value="${formatNumber(number: crmProduct.weight)}"
                                                 class="span6"/>
                                </f:field>
                            </div>
                        </div>

                    </div>

                    <div class="row-fluid">
                        <div class="span6">
                            <f:field property="description">
                                <g:textArea name="description" value="${crmProduct.description}" rows="4" cols="50"
                                            class="span12"/>
                            </f:field>
                        </div>

                        <div class="span3">
                            <f:field property="enabled"/>
                        </div>
                    </div>

                </f:with>
            </div>

            <div class="tab-pane" id="prices">
                <table id="price-list" class="table table-striped">
                    <thead>
                    <tr>
                        <th><g:message code="crmProductPrice.priceList.label" default="Price List"/></th>
                        <th><g:message code="crmProductPrice.fromAmount.label" default="From Amount"/></th>
                        <th><g:message code="crmProductPrice.unit.label" default="Unit"/></th>
                        <th><g:message code="crmProductPrice.inPrice.label" default="Cost"/></th>
                        <th><g:message code="crmProductPrice.outPrice.label" default="Price"/></th>
                        <th><g:message code="crmProductPrice.vat.label" default="VAT"/></th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${crmProduct.prices}" var="price" status="row">
                        <g:render template="price" model="${[bean: price, row: row, vatList: vatList]}"/>
                    </g:each>
                    </tbody>
                    <tfoot>
                    <tr>
                        <td colspan="7">
                            <button type="button" class="btn btn-success" id="btn-add-price">
                                <i class="icon-plus icon-white"></i>
                                <g:message code="crmProductPrice.button.add.label" default="Add Price"/>
                            </button>
                        </td>
                    </tr>
                    </tfoot>
                </table>

            </div>

            <crm:pluginViews location="tabs" var="view">
                <div class="tab-pane tab-${view.id}" id="${view.id}">
                    <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
                </div>
            </crm:pluginViews>
        </div>
    </div>

    <div class="form-actions">
        <crm:button visual="primary" icon="icon-ok icon-white" label="crmProduct.button.update.label"/>
        <crm:button action="delete" visual="danger" icon="icon-trash icon-white"
                    label="crmProduct.button.delete.label"
                    confirm="crmProduct.button.delete.confirm.message" permission="crmProduct:delete"/>
        <crm:button type="link" action="show" id="${crmProduct.id}"
                    icon="icon-remove"
                    label="crmProduct.button.cancel.label"/>
    </div>

</g:form>

</body>
</html>
