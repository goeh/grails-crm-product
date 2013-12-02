package grails.plugins.crm.product

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Tests for CrmProduct domain.
 */
@TestFor(CrmProduct)
class CrmProductSpec extends Specification {

    def "test preferredName"() {
        when:
        def p = new CrmProduct()

        then:
        p.name == null
        p.displayName == null
        p.preferredName == null

        when:
        p.name = "Hello"

        then:
        p.displayName == null
        p.preferredName == "Hello"

        when:
        p.displayName = "World"

        then:
        p.preferredName == "World"
    }
}
