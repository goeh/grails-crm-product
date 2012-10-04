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

import grails.events.Listener
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.core.SearchUtils
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod

class CrmProductService {

    static transactional = true

    def crmSecurityService
    def crmTagService

    @Listener(namespace = "crmProduct", topic = "enableFeature")
    def enableFeature(event) {
        // event = [feature: feature, tenant: tenant, role:role, expires:expires]
        def tenant = crmSecurityService.getTenantInfo(event.tenant)
        TenantUtils.withTenant(tenant.id) {
            crmTagService.createTag(name: CrmProduct.name, multiple: true)
        }
    }

    /**
     * Empty query = search all records.
     *
     * @param params pagination parameters
     * @return List of CrmProduct domain instances
     */
    def list(Map params = [:]) {
        listProducts([:], params)
    }

    /**
     * Find CrmProduct instances filtered by query.
     *
     * @param query filter parameters
     * @param params pagination parameters
     * @return List of CrmProduct domain instances
     */
    def list(Map query, Map params) {
        listProducts(query, params)
    }

    /**
     * Find CrmProduct instances filtered by query.
     *
     * @param query filter parameters
     * @param params pagination parameters
     * @return List of CrmProduct domain instances
     */
    def listProducts(Map query, Map params) {
        CrmProduct.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (query.number) {
                ilike('number', SearchUtils.wildcard(query.number))
            }
            if (query.name) {
                ilike('name', SearchUtils.wildcard(query.name))
            }
        }
    }

    CrmProduct createProduct(Map params, boolean save = false) {
        def tenant = TenantUtils.tenant
        def m = CrmProduct.findByNumberAndTenantId(params.number, tenant)
        if (!m) {
            m = new CrmProduct()
            def args = [m, params, [include: CrmProduct.BIND_WHITELIST]]
            new BindDynamicMethod().invoke(m, 'bind', args.toArray())
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    boolean updateProduct(CrmProduct crmProduct, Map params) {
        def args = [crmProduct, params, [include: CrmProduct.BIND_WHITELIST]]
        new BindDynamicMethod().invoke(crmProduct, 'bind', args.toArray())
        return crmProduct.validate()
    }

    def deleteProduct(CrmProduct crmProduct) {
        crmProduct.delete()
    }

    def listProductGroups(Map params) {
        CrmProductGroup.findAllByTenantId(TenantUtils.tenant, params)
    }

    CrmProductGroup createProductGroup(Map params, boolean save = false) {
        def tenant = TenantUtils.tenant
        def m = CrmProductGroup.findByNameAndTenantId(params.name, tenant)
        if (!m) {
            m = new CrmProductGroup(params)
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    boolean updateProductGroup(CrmProductGroup productGroup, Map params) {
        def args = [productGroup, params]
        new BindDynamicMethod().invoke(productGroup, 'bind', args.toArray())
        return productGroup.validate()
    }

    def deleteProductGroup(CrmProductGroup group) {
        group.delete()
    }

    def listPriceLists(Map params) {
        CrmPriceList.findAllByTenantId(TenantUtils.tenant, params)
    }

    CrmPriceList createPriceList(Map params, boolean save = false) {
        def tenant = TenantUtils.tenant
        def m = CrmPriceList.findByNameAndTenantId(params.name, tenant)
        if (!m) {
            m = new CrmPriceList()
            m.properties = params
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    boolean updatePriceList(CrmPriceList priceList, Map params) {
        def args = [priceList, params]
        new BindDynamicMethod().invoke(priceList, 'bind', args.toArray())
        return priceList.validate()
    }

    def deletePriceList(CrmPriceList priceList) {
        priceList.delete()
    }

    /**
     * Return price for a product.
     *
     * @param productNumber CrmProduct.number
     * @param amount amount to base price on
     * @param priceList CrmPriceList instance or CrmPriceList.param string
     * @return outPrice for the product, or null if no price are found
     */
    Float getPrice(String productNumber, Integer amount = null, Object priceList = null) {
        def crmProduct = CrmProduct.findByNumber(productNumber, [cache: true])
        if (!crmProduct) {
            return null
            //throw new IllegalArgumentException("CrmProduct not found: " + productNumber)
        }
        if (!(priceList instanceof CrmPriceList)) {
            priceList = CrmPriceList.findByParam(priceList.toString())
        }
        if (!amount) {
            amount = 1
        }
        if (crmProduct.prices) {
            // Sort by descending fromAmount to find the best matching price
            for (p in crmProduct.prices.findAll {priceList ? it.priceList == priceList : true}.sort {it.fromAmount}.reverse()) {
                if (p.fromAmount == null || p.fromAmount <= amount) {
                    return p.outPrice
                }
            }
        }
        return null
    }
}
