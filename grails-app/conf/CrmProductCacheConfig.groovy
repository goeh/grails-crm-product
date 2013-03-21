import grails.plugins.crm.product.CrmPriceList
import grails.plugins.crm.product.CrmProduct
import grails.plugins.crm.product.CrmProductGroup

config = {
// Hibernate domain class second-level caches.
    domain {
        name CrmProduct
        maxElementsInMemory 1000
        timeToLiveSeconds 300
        timeToIdleSeconds 300

        collection {
            name 'prices'
            eternal false
            overflowToDisk false
            maxElementsInMemory 1000
            maxElementsOnDisk 0
            timeToLiveSeconds 300
            timeToIdleSeconds 300
        }
    }

    domain {
        name CrmProductGroup
        maxElementsInMemory 100
        timeToLiveSeconds 300
        timeToIdleSeconds 300
    }


    domain {
        name CrmPriceList
        maxElementsInMemory 100
        timeToLiveSeconds 300
        timeToIdleSeconds 300
    }
}
