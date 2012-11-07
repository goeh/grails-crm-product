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

import grails.plugins.crm.core.TenantEntity

@TenantEntity
class CrmProduct {

    public static final List BIND_WHITELIST = ['number', 'name', 'displayName', 'description', 'supplier', 'suppliersNumber', 'group', 'barcode', 'customsCode', 'weight', 'enabled', 'prices']

    def crmCoreService

    String number
    String name
    String displayName
    String description
    String supplierRef
    String suppliersNumber
    CrmProductGroup group
    String barcode
    String customsCode
    Float weight // TODO should weight be in CrmProductPrice instead?
    boolean enabled

    static hasMany = [prices: CrmProductPrice, compositions: CrmProductComposition]
    static mappedBy = [compositions: 'mainProduct']

    static constraints = {
        number(maxSize: 40, blank: false, unique: 'tenantId')
        name(maxSize: 255, blank: false)
        displayName(maxSize: 255, nullable: true)
        description(maxSize: 2000, nullable: true, widget: 'textarea')
        supplierRef(maxSize: 80, nullable: true)
        suppliersNumber(maxSize: 40, nullable: true)
        group()
        barcode(maxSize: 255, nullable: true)
        customsCode(maxSize: 10, nullable: true)
        weight(nullable: true)
    }

    static mapping = {
        sort "number"
        prices sort: 'fromAmount', 'asc'
    }

    static transients = ['price', 'vat', 'supplier', 'includes', 'excludes', 'depends']

    static taggable = true
    static attachmentable = true
    static dynamicProperties = true
    static relatable = true

    transient Float getPrice(CrmPriceList priceList = null) {
        prices?.find { priceList ? it.priceList == priceList : true }?.outPrice
    }

    transient Float getVat(CrmPriceList priceList = null) {
        prices?.find { priceList ? it.priceList == priceList : true }?.vat
    }

    transient Object getSupplier() {
        crmCoreService.getReference(supplierRef)
    }

    transient void setSupplier(Object arg) {
        supplierRef = crmCoreService.getReferenceIdentifier(arg)
    }

    transient List<CrmProduct> getIncludes() {
        def result = []
        for (c in compositions) {
            if (c.type == CrmProductComposition.INCLUDES) {
                result << c.product
            }
        }
        return result
    }

    def addIncludes(CrmProduct included, Float quantity = null) {
        if (included == this) {
            throw new IllegalArgumentException("self reference")
        }
        if (!compositions?.find { it.product == included }) {
            addToCompositions(product: included, quantity: quantity, type: CrmProductComposition.INCLUDES)
        }
        return this
    }

    transient List<CrmProduct> getExcludes() {
        def result = []
        for (c in compositions) {
            if (c.type == CrmProductComposition.EXCLUDES) {
                result << c.product
            }
        }
        return result
    }

    def addExcludes(CrmProduct excluded, Float quantity = null) {
        if (excluded == this) {
            throw new IllegalArgumentException("self reference")
        }
        if (!compositions?.find { it.product == excluded }) {
            addToCompositions(product: excluded, quantity: quantity, type: CrmProductComposition.EXCLUDES)
        }
        return this
    }

    transient List<CrmProduct> getDepends() {
        def result = []
        for (c in compositions) {
            if (c.type == CrmProductComposition.DEPENDS) {
                result << c.product
            }
        }
        return result
    }

    def addDepends(CrmProduct dependedOn, Float quantity = null) {
        if (dependedOn == this) {
            throw new IllegalArgumentException("self reference")
        }
        if (!compositions?.find { it.product == dependedOn }) {
            addToCompositions(product: dependedOn, quantity: quantity, type: CrmProductComposition.DEPENDS)
        }
        return this
    }

    String toString() {
        name
    }
}
