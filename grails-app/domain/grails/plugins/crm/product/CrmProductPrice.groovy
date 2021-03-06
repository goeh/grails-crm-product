/*
 * Copyright (c) 2014 Goran Ehrsson.
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
    Double fromAmount = 1
    Double inPrice = 0
    Double outPrice
    Double vat

    static belongsTo = [product:CrmProduct]

    static constraints = {
        unit(maxSize: 40, blank: false)
        inPrice(min:-999999d, max:999999d, scale:2, nullable: false)
        outPrice(min:-999999d, max:999999d, scale:2, nullable: false)
        vat(min: 0d, max: 1d, scale: 2)
    }

    static mapping = {
        sort 'fromAmount'
    }

    static transients = ['price', 'priceVAT']

    Double getPrice() {
        outPrice
    }

    Double getPriceVAT() {
        def p = outPrice ?: 0
        def v = vat ?: 0
        return p + (p * v)
    }

    String toString() {
        "$outPrice / $unit".toString()
    }
}
