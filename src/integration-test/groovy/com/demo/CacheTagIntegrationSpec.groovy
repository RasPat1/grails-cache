package com.demo

import grails.test.mixin.integration.Integration

import geb.spock.GebSpec


@Integration
class CacheTagIntegrationSpec extends GebSpec {

    void 'test clear cache'() {
        when:
        go '/demo/clearBlocksCache'

        then:
        $().text().contains 'cleared blocks cache'

        when:
        go '/demo/clearTemplatesCache'

        then:
        $().text().contains 'cleared templates cache'
    }

    void 'test block tag'() {
        when:
        go '/demo/blockCache?counter=5'

        then:
        $().text().contains 'First block counter 6'
        $().text().contains 'Second block counter 7'
        $().text().contains 'Third block counter 8'

        when:
        go '/demo/blockCache?counter=42'

        then:
        $().text().contains 'First block counter 6'
        $().text().contains 'Second block counter 7'
        $().text().contains 'Third block counter 8'
    }

    void 'test clear blocks cache'() {
        when:
        go '/demo/clearBlocksCache'

        then:
        $().text().contains 'cleared blocks cache'

        when:
        go '/demo/blockCache?counter=100'

        then:
        $().text().contains 'First block counter 101'
        $().text().contains 'Second block counter 102'
        $().text().contains 'Third block counter 103'

        when:
        go '/demo/blockCache?counter=42'

        then:
        $().text().contains 'First block counter 101'
        $().text().contains 'Second block counter 102'
        $().text().contains 'Third block counter 103'

        when:
        go '/demo/clearBlocksCache'

        then:
        $().text().contains 'cleared blocks cache'

        when:
        go '/demo/blockCache?counter=50'

        then:
        $().text().contains 'First block counter 51'
        $().text().contains 'Second block counter 52'
        $().text().contains 'Third block counter 53'

        when:
        go '/demo/blockCache?counter=150'

        then:
        $().text().contains 'First block counter 51'
        $().text().contains 'Second block counter 52'
        $().text().contains 'Third block counter 53'
    }

    void 'test render tag'() {
        when:
        go '/demo/renderTag?counter=1'

        then:
        $().text().contains 'First invocation: Counter value: 1'
        $().text().contains 'Second invocation: Counter value: 1'
        $().text().contains 'Third invocation: Counter value: 3'
        $().text().contains 'Fourth invocation: Counter value: 3'
        $().text().contains 'Fifth invocation: Counter value: 1'

        when:
        go '/demo/renderTag?counter=5'

        then:
        $().text().contains 'First invocation: Counter value: 1'
        $().text().contains 'Second invocation: Counter value: 1'
        $().text().contains 'Third invocation: Counter value: 3'
        $().text().contains 'Fourth invocation: Counter value: 3'
        $().text().contains 'Fifth invocation: Counter value: 1'

        when:
        go '/demo/clearTemplatesCache'

        then:
        $().text().contains 'cleared templates cache'

        when:
        go '/demo/renderTag?counter=5'

        then:
        $().text().contains 'First invocation: Counter value: 5'
        $().text().contains 'Second invocation: Counter value: 5'
        $().text().contains 'Third invocation: Counter value: 7'
        $().text().contains 'Fourth invocation: Counter value: 7'
        $().text().contains 'Fifth invocation: Counter value: 5'

        when:
        go '/demo/renderTag?counter=1'

        then:
        $().text().contains 'First invocation: Counter value: 5'
        $().text().contains 'Second invocation: Counter value: 5'
        $().text().contains 'Third invocation: Counter value: 7'
        $().text().contains 'Fourth invocation: Counter value: 7'
        $().text().contains 'Fifth invocation: Counter value: 5'
    }
}
