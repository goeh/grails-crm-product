<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmPriceList.label', default: 'Product Group')}"/>
    <title><g:message code="crmPriceList.edit.title" args="[entityName, crmPriceList]"/></title>
</head>

<body>

<crm:header title="crmPriceList.edit.title" args="[entityName, crmPriceList]"/>

<div class="row-fluid">
    <div class="span9">

        <g:hasErrors bean="${crmPriceList}">
            <crm:alert class="alert-error">
                <ul>
                    <g:eachError bean="${crmPriceList}" var="error">
                        <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                                error="${error}"/></li>
                    </g:eachError>
                </ul>
            </crm:alert>
        </g:hasErrors>

        <g:form class="form-horizontal" action="edit"
                id="${crmPriceList?.id}">
            <g:hiddenField name="version" value="${crmPriceList?.version}"/>

            <f:with bean="crmPriceList">
                <f:field property="name" input-autofocus=""/>
                <f:field property="description"/>
                <f:field property="param"/>
                <f:field property="orderIndex"/>
                <f:field property="enabled"/>
            </f:with>

            <div class="form-actions">
                <crm:button visual="primary" icon="icon-ok icon-white" label="crmPriceList.button.update.label"/>
                <crm:button action="delete" visual="danger" icon="icon-trash icon-white"
                            label="crmPriceList.button.delete.label"
                            confirm="crmPriceList.button.delete.confirm.message"
                            permission="crmPriceList:delete"/>
                <crm:button type="link" action="list"
                            icon="icon-remove"
                            label="crmPriceList.button.cancel.label"/>
            </div>
        </g:form>
    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>
