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
    Float fromAmount = 1
    Float inPrice = 0f
    Float outPrice
    Float vat

    static belongsTo = [product:CrmProduct]

    static constraints = {
        unit(maxSize: 40, blank: false)
        inPrice(min:-999999f, max:999999f, scale:2, nullable: false)
        outPrice(min:-999999f, max:999999f, scale:2, nullable: false)
        vat(min: 0f, max: 1f, scale: 2)
    }

    static mapping = {
        sort 'fromAmount'
    }

    String toString() {
        "$outPrice / $unit".toString()
    }
}
