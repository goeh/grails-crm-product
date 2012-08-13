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

/**
 * Defines a product price.
 */
class CrmProductPrice {

    CrmPriceList priceList

    String unit
    Float fromAmount
    Float inPrice
    Float outPrice
    Float vat

    static belongsTo = [product:CrmProduct]

    static constraints = {
        unit(maxSize: 20, blank: false)
        vat(scale: 2)
    }
    String toString() {
        outPrice.toString()
    }
}
