/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.product

import grails.plugins.crm.contact.CrmContact
import grails.plugins.crm.core.TenantEntity

@TenantEntity
class CrmProduct {

    public static final List BIND_WHITELIST = ['number', 'name', 'displayName', 'description', 'supplier', 'suppliersNumber', 'group', 'barcode', 'customsCode', 'weight', 'enabled', 'prices']

    String number
    String name
    String displayName
    String description
    CrmContact supplier
    String suppliersNumber
    CrmProductGroup group
    String barcode
    String customsCode
    Float weight // TODO should weight be in CrmProductPrice instead?
    boolean enabled

    static hasMany = [prices: CrmProductPrice]

    static constraints = {
        number(maxSize: 40, blank: false, unique: 'tenantId')
        name(maxSize: 255, blank: false)
        displayName(maxSize: 255, nullable: true)
        description(maxSize: 2000, nullable: true, widget: 'textarea')
        supplier(nullable: true)
        suppliersNumber(maxSize: 40, nullable: true)
        group()
        barcode(maxSize: 255, nullable: true)
        customsCode(maxSize: 10, nullable: true)
        weight(nullable: true)
    }

    static mapping = {
        sort "number"
    }

    static transients = ['price', 'vat']

    static taggable = true
    static attachmentable = true
    static dynamicProperties = true
    static relatable = true

    transient Float getPrice(CrmPriceList priceList = null) {
        prices?.find {priceList ? it.priceList == priceList : true}?.outPrice
    }

    transient Float getVat(CrmPriceList priceList = null) {
        prices?.find {priceList ? it.priceList == priceList : true}?.vat
    }

    String toString() {
        name
    }
}
