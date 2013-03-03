<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmProduct.label', default: 'Task')}"/>
    <title><g:message code="crmProduct.show.title" args="[entityName, crmProduct]"/></title>
</head>

<body>

<div class="row-fluid">
    <div class="span9">

        <crm:header title="crmProduct.show.title" subtitle="${(crmProduct.supplier ?: '').encodeAsHTML()}"
                    args="[entityName, crmProduct]"/>

        <div class="tabbable">
            <ul class="nav nav-tabs">
                <li class="active"><a href="#main" data-toggle="tab"><g:message code="crmProduct.tab.main.label"/></a>
                </li>
                <li><a href="#prices" data-toggle="tab"><g:message
                        code="crmProduct.tab.prices.label"/><crm:countIndicator
                        count="${crmProduct.prices.size()}"/></a></li>
                <crm:pluginViews location="tabs" var="view">
                    <crm:pluginTab id="${view.id}" label="${view.label}" count="${view.model?.totalCount}"/>
                </crm:pluginViews>
            </ul>

            <div class="tab-content">
                <div class="tab-pane active" id="main">
                    <div class="row-fluid">
                        <div class="span4">
                            <dl>

                                <g:if test="${crmProduct?.number}">
                                    <dt><g:message code="crmProduct.number.label" default="Number"/></dt>
                                    <dd><g:fieldValue bean="${crmProduct}" field="number"/></dd>
                                </g:if>

                                <g:if test="${crmProduct?.name}">
                                    <dt><g:message code="crmProduct.name.label" default="Name"/></dt>

                                    <dd><g:fieldValue bean="${crmProduct}" field="name"/></dd>
                                </g:if>

                                <g:if test="${crmProduct?.displayNumber}">
                                    <dt><g:message code="crmProduct.displayNumber.label" default="Display Number"/></dt>

                                    <dd><g:fieldValue bean="${crmProduct}" field="displayNumber"/></dd>
                                </g:if>

                                <g:if test="${crmProduct?.displayName}">
                                    <dt><g:message code="crmProduct.displayName.label" default="Display Name"/></dt>

                                    <dd><g:fieldValue bean="${crmProduct}" field="displayName"/></dd>
                                </g:if>

                                <g:if test="${crmProduct?.group}">
                                    <dt><g:message code="crmProduct.group.label" default="Group"/></dt>

                                    <dd><g:fieldValue bean="${crmProduct}" field="group"/></dd>
                                </g:if>

                            </dl>
                        </div>

                        <div class="span4">
                            <dl>

                                <g:if test="${crmProduct?.supplier}">
                                    <dt><g:message code="crmProduct.supplier.label" default="Supplier"/></dt>

                                    <dd><g:fieldValue bean="${crmProduct}" field="supplier"/></dd>
                                </g:if>

                                <g:if test="${crmProduct?.suppliersNumber}">
                                    <dt><g:message code="crmProduct.suppliersNumber.label"
                                                   default="Supplier Number"/></dt>

                                    <dd><g:fieldValue bean="${crmProduct}" field="suppliersNumber"/></dd>
                                </g:if>

                            </dl>
                        </div>

                        <div class="span4">
                            <dl>

                                <g:if test="${crmProduct?.barcode}">
                                    <dt><g:message code="crmProduct.barcode.label" default="Barcode"/></dt>
                                    <dd><g:fieldValue bean="${crmProduct}" field="barcode"/></dd>
                                </g:if>

                                <g:if test="${crmProduct?.weight}">
                                    <dt><g:message code="crmProduct.weight.label" default="Weight"/></dt>
                                    <dd><g:fieldValue bean="${crmProduct}" field="weight"/></dd>
                                </g:if>

                            </dl>
                        </div>

                    </div>

                    <div class="row-fluid">
                        <div class="span8">

                            <g:if test="${crmProduct.description}">
                                <dt><g:message code="crmProduct.description.label" default="Description"/></dt>
                                <dd><g:decorate encode="HTML">${crmProduct.description}</g:decorate></dd>
                            </g:if>

                        </div>

                        <div class="span4">
                        </div>
                    </div>

                    <div class="form-actions">
                        <g:form>
                            <g:hiddenField name="id" value="${crmProduct?.id}"/>

                            <crm:button type="link" action="edit" id="${crmProduct?.id}" visual="primary"
                                        icon="icon-pencil icon-white"
                                        label="crmProduct.button.edit.label" permission="crmProduct:edit">
                            </crm:button>

                            <crm:button type="link" action="create"
                                        params="${['group.id': crmProduct.group?.id]}"
                                        visual="success"
                                        icon="icon-file icon-white"
                                        label="crmProduct.button.create.label"
                                        title="crmProduct.button.create.help"
                                        permission="crmProduct:create"/>
                        </g:form>
                    </div>

                </div>

                <div class="tab-pane" id="prices">
                    <g:render template="prices" model="${[result: prices]}"/>
                </div>

                <crm:pluginViews location="tabs" var="view">
                    <div class="tab-pane tab-${view.id}" id="${view.id}">
                        <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
                    </div>
                </crm:pluginViews>
            </div>

        </div>

    </div>

    <div class="span3">

        <g:render template="/tags" plugin="crm-tags" model="${[bean: crmProduct]}"/>

        <div id="resource-preview"></div>

    </div>
</div>

</body>
</html>
