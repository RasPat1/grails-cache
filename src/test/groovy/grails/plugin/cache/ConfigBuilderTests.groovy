package grails.plugin.cache

import grails.util.Environment
import grails.test.mixin.*
import grails.test.mixin.support.*

/**
 * Most of the tests are from the Ehcache plugin, here to ensure that everything works when
 * specifying unsupported attributes, child nodes, etc.
 *
 * @author Burt Beckwith
 */
@TestMixin(GrailsUnitTestMixin)
class ConfigBuilderTests  {

	private ConfigBuilder builder
	private List<String> cacheNames

	void testEmpty() {
		parse {}

		assert cacheNames.empty
	}

	void testDefaultCache() {

		parse {
			defaultCache {
				maxElementsInMemory 10000
				eternal false
				timeToIdleSeconds 120
				timeToLiveSeconds 120
				overflowToDisk true
				maxElementsOnDisk 10000000
				diskPersistent false
				diskExpiryThreadIntervalSeconds 120
				memoryStoreEvictionPolicy 'LRU'
			}
		}

		assert cacheNames.empty
	}

	void testCache() {

		parse {
			cache {
				name 'basic'
				eternal false
				overflowToDisk true
				maxElementsInMemory 10000
				maxElementsOnDisk 10000000
			}
		}

		assert ['basic'] == cacheNames
	}

	void testDomain() {

		parse {
			domain {
				name 'com.foo.Thing'
				eternal false
				overflowToDisk true
				maxElementsInMemory 10000
				maxElementsOnDisk 10000000
			}
		}

		assert ['com.foo.Thing'] == cacheNames
	}

	void testCacheWithDefaults() {

		parse {
			defaults {
				maxElementsInMemory 1000
				eternal false
				overflowToDisk false
				maxElementsOnDisk 0
			}

			domain {
				name 'com.foo.Thing'
				maxElementsOnDisk 10000000
			}
		}

		assert ['com.foo.Thing'] == cacheNames
	}

	void testCollection() {

		parse {
			domain {
				name 'com.foo.Author'
				eternal false
				overflowToDisk true
				maxElementsInMemory 10000
				maxElementsOnDisk 10000000
			}
			domainCollection {
				name 'books'
				domain 'com.foo.Author'
				eternal true
				overflowToDisk true
				maxElementsInMemory 100
				maxElementsOnDisk 10000
			}
		}

		assert ['com.foo.Author'] == cacheNames
	}

	void testInnerCollection() {

		parse {
			defaults {
				maxElementsInMemory 1000
				eternal false
				overflowToDisk false
				maxElementsOnDisk 0
			}

			domain {
				name 'com.foo.Author'
				maxElementsInMemory 10000

				collection {
					name 'books'
					eternal true
					maxElementsInMemory 100
				}
			}
		}

		assert ['com.foo.Author'] == cacheNames
	}

	void testHibernate() {

		parse {
			hibernateQuery()
			hibernateTimestamps()
		}

		assert cacheNames.empty
	}

	void testEnvironment() {

		def config = {
			defaults {
				maxElementsInMemory 1000
				eternal false
				overflowToDisk false
				maxElementsOnDisk 0
			}

			domain {
				name 'com.foo.Thing'
			}

			domain {
				name 'com.foo.Other'
				env 'staging'
			}

			domain {
				name 'com.foo.Book'
				env(['staging', 'production'])
			}
		}

		parse config
		assert ['com.foo.Thing'] == cacheNames

		Environment.metaClass.getName = { -> 'staging' }
		parse config
		assert ['com.foo.Thing', 'com.foo.Other', 'com.foo.Book'] == cacheNames

		Environment.metaClass.getName = { -> 'production' }
		parse config
		assert ['com.foo.Thing', 'com.foo.Book'] == cacheNames
	}

	void testDistributed() {

		Environment.metaClass.getName = { -> 'production' }

		parse {

			defaults {
				maxElementsInMemory 1000
				eternal false
				overflowToDisk false
				maxElementsOnDisk 0
				cacheEventListenerFactoryName 'cacheEventListenerFactory'
			}

			domain {
				name 'com.foo.Book'
			}

			cacheManagerPeerProviderFactory {
				env 'production'
				factoryType 'rmi'
				multicastGroupAddress '${ehcacheMulticastGroupAddress}'// '237.0.0.2'
				multicastGroupPort '${ehcacheMulticastGroupPort}'// 5557
				timeToLive 'subnet'
			}

			cacheManagerPeerListenerFactory {
				env 'production'
			}

			cacheEventListenerFactory {
				env 'production'
				name 'cacheEventListenerFactory'
				factoryType 'rmi'
				replicateAsynchronously false
			}
		}

		assert ['com.foo.Book'] == cacheNames
	}

	// void testFromConfigGroovy() {

	// 	ConfigObject config = new ConfigSlurper(Environment.current.name).parse(getClass().getResource('application.groovy'))
	// 	def cacheConfig = config.grails.cache.config
	// 	assert cacheConfig instanceof Closure

	// 	parse cacheConfig
	// 	assert ['fromConfigGroovy1', 'fromConfigGroovy2'] == cacheNames.sort()
	// }

	void testLenient() {

		parse {
			domain {
				name 'com.foo.Thing'
				eternal false
				overflowToDisk true
				maxElementsInMemory 10000
				maxElementsOnDisk 10000000
				type 'com.foo.BarCache'
				someOtherProperty 42
				nested {
					bar 123
				}
			}
		}

		assert ['com.foo.Thing'] == cacheNames
	}

	private void parse(Closure config) {
		builder = new ConfigBuilder()
		builder.parse config
		cacheNames = builder.cacheNames
	}
}
