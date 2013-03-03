<%@ page import="grails.plugins.crm.product.CrmProduct" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmProduct.label', default: 'Product')}"/>
    <title><g:message code="crmProduct.create.title" args="[entityName]"/></title>
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
        });
    </r:script>
</head>

<body>

<crm:header title="crmProduct.create.title" args="[entityName]"/>

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

<g:form action="create">

    <f:with bean="crmProduct">

        <div class="row-fluid">

            <div class="span4">
                <div class="row-fluid">
                    <f:field property="number" input-autofocus="" input-class="span12"/>
                    <f:field property="name" input-class="span12"/>
                    <f:field property="displayNumber" input-class="span12"/>
                    <f:field property="displayName" input-class="span12"/>
                    <f:field property="description">
                        <g:textArea name="description" value="${crmProduct.description}" rows="4" cols="50"
                                    class="span12"/>
                    </f:field>
                </div>
            </div>

            <div class="span4">
                <div class="row-fluid">
                    <f:field property="supplier">
                        <g:textField name="supplier" value="${crmProduct.supplier?.name}" class="span12"
                                     autocomplete="off"/>
                    </f:field>
                    <f:field property="suppliersNumber" input-class="span6"/>
                    <f:field property="group">
                        <g:select name="group.id" from="${productGroups}" optionKey="id" value="${crmProduct.group?.id}"/>
                    </f:field>
                </div>
            </div>

            <div class="span4">
                <div class="row-fluid">
                    <f:field property="barcode" input-class="span6"/>
                    <f:field property="customsCode" input-class="span6"/>
                    <f:field property="weight" input-class="span6"/>
                    <f:field property="enabled"/>
                </div>
            </div>

        </div>

        <div class="form-actions">
            <crm:button visual="primary" icon="icon-ok icon-white" label="crmProduct.button.save.label"/>
        </div>

    </f:with>

</g:form>

</body>
</html>
