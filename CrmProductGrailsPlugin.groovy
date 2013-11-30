class CrmProductGrailsPlugin {
    def groupId = "grails.crm"
    def version = "1.2.1"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
    def loadAfter = ['crmContact']
    def pluginExcludes = [
            "grails-app/conf/ApplicationResources.groovy",
            "src/groovy/grails/plugins/crm/product/TestSecurityDelegate.groovy",
            "grails-app/views/error.gsp"
    ]
    def title = "GR8 CRM Product Plugin"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
A GR8 CRM plugin that provides product/item management for orders and sales projects, etc.
'''
    def documentation = "https://github.com/goeh/grails-crm-product"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-product/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-product"]

    def features = {
        crmProduct {
            description "Product Catalogue"
            link controller: "crmProduct", action: "index"
            permissions {
                guest "crmProduct:index,list,show"
                partner "crmProduct:index,list,show"
                user "crmProduct:*"
                admin "crmProduct,crmProductGroup,crmPriceList:*"
            }
            hidden true
        }
    }
}
