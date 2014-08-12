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
import grails.plugins.selection.Selectable

class CrmProductService {

    static transactional = true

    def crmTagService

    @Listener(namespace = "crmProduct", topic = "enableFeature")
    def enableFeature(event) {
        // event = [feature: feature, tenant: tenant, role:role, expires:expires]
        def tenant = event.tenant
        TenantUtils.withTenant(tenant) {
            crmTagService.createTag(name: CrmProduct.name, multiple: true)
        }
    }

    @Listener(namespace = "crmTenant", topic = "requestDelete")
    def requestDeleteTenant(event) {
        def tenant = event.id
        def count = 0
        count += CrmProduct.countByTenantId(tenant)
        count += CrmProductGroup.countByTenantId(tenant)
        count += CrmPriceList.countByTenantId(tenant)
        count ? [namespace: 'crmProduct', topic: 'deleteTenant'] : null
    }

    @Listener(namespace = "crmProduct", topic = "deleteTenant")
    def deleteTenant(event) {
        def tenant = event.id
        CrmProductComposition.createCriteria().list() {
            mainProduct {
                eq('tenantId', tenant)
            }
        }*.delete()
        def result = CrmProduct.findAllByTenantId(tenant)
        result*.delete()
        CrmProductGroup.findAllByTenantId(tenant)*.delete()
        CrmPriceList.findAllByTenantId(tenant)*.delete()
        log.warn("Deleted ${result.size()} products in tenant $tenant")
    }

    /**
     * Empty query = search all records.
     *
     * @param params pagination parameters
     * @return List of CrmProduct domain instances
     */
    @Selectable
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
    @Selectable
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
    private List<CrmProduct> listProducts(Map query, Map params) {
        def tagged
        if (query.tags) {
            tagged = crmTagService.findAllIdByTag(CrmProduct, query.tags) ?: [0L]
        }

        CrmProduct.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (tagged) {
                inList('id', tagged)
            }
            if (query.number) {
                or {
                    ilike('number', SearchUtils.wildcard(query.number))
                    ilike('displayNumber', SearchUtils.wildcard(query.number))
                }
            }
            if (query.name) {
                or {
                    ilike('name', SearchUtils.wildcard(query.name))
                    ilike('displayName', SearchUtils.wildcard(query.name))
                }
            }
            if (query.supplier) {
                supplier {
                    ilike('name', SearchUtils.wildcard(query.supplier))
                }
            }
            if (query.suppliersNumber) {
                ilike('suppliersNumber', SearchUtils.wildcard(query.suppliersNumber))
            }
            if (query.barcode) {
                ilike('barcode', SearchUtils.wildcard(query.barcode))
            }
            if (query.customsCode) {
                ilike('customsCode', SearchUtils.wildcard(query.customsCode))
            }
            if (query.group || query.productGroup) {
                group {
                    or {
                        ilike('name', SearchUtils.wildcard(query.group ?: query.productGroup))
                        eq('param', query.group ?: query.productGroup)
                    }
                }
            }
            if (query.enabled != null) {
                eq('enabled', query.enabled)
            }
            if (query.weight != null) {
                eq('weight', query.weight)
            }
            if (query.price != null) {
                prices {
                    eq('outPrice', query.price)
                }
            }
        }
    }

    CrmProduct getProduct(String number) {
        CrmProduct.findByNumberAndTenantId(number, TenantUtils.tenant)
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

    CrmProductGroup getProductGroup(String param) {
        CrmProductGroup.findByParamAndTenantId(param, TenantUtils.tenant)
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
     * @param unit unit to get price for
     * @return outPrice for the product, or null if no price are found
     */
    Double getPrice(String productNumber, Integer amount = null, Object priceList = null, String unit = null) {
        CrmProduct.findByNumberAndTenantId(productNumber, TenantUtils.tenant, [cache: true])?.getPrice(amount, priceList, unit)
    }
}
