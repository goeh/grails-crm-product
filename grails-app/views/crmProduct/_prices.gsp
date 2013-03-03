<table class="table table-striped">
    <thead>
    <tr>
        <crm:sortableColumn property="priceList.orderIndex"
                            title="${message(code: 'crmProductPrice.priceList.label', default: 'Price List')}"/>
        <crm:sortableColumn property="fromAmount"
                            title="${message(code: 'crmProductPrice.fromAmount.label', default: 'From Amount')}"/>
        <crm:sortableColumn property="unit"
                                    title="${message(code: 'crmProductPrice.unit.label', default: 'Unit')}"/>
        <crm:sortableColumn property="inPrice"
                            title="${message(code: 'crmProductPrice.inPrice.label', default: 'Cost')}"/>
        <crm:sortableColumn property="outPrice"
                                    title="${message(code: 'crmProductPrice.outPrice.label', default: 'Price')}"/>
        <th><g:message code="crmProductPrice.vat.label" default="VAT"/></th>
        <th><g:message code="crmProductPrice.priceVAT.label" default="Price inc. VAT"/></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${result}" var="price">
        <tr>

            <td>
                <g:link controller="crmPriceList" action="show" id="${price.priceList.id}">
                    ${fieldValue(bean: price, field: "priceList")}
                </g:link>
            </td>

            <td>${fieldValue(bean: price, field: "fromAmount")}</td>
            <td>${fieldValue(bean: price, field: "unit")}</td>
            <td><g:formatNumber number="${price.inPrice}" type="currency" currencyCode="${currency}" minFractionDigits="2" maxFractionDigits="2"/></td>
            <td><g:formatNumber number="${price.outPrice}" type="currency" currencyCode="${currency}" minFractionDigits="2" maxFractionDigits="2"/></td>
            <td><g:formatNumber number="${price.vat ?: 0}" type="percent"/></td>
            <td><g:formatNumber number="${price.priceVAT}" type="currency" currencyCode="${currency}" minFractionDigits="2" maxFractionDigits="2"/></td>
        </tr>
    </g:each>
    </tbody>
</table>
