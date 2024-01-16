package com.couchbase.kotlin.quickstart

import com.couchbase.client.kotlin.Bucket
import com.couchbase.client.kotlin.Cluster
import com.couchbase.client.kotlin.Scope
import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class CouchbaseConfigurationKtTest {

    @Test
    fun testConfiguration() {
        val cfg = mockk<ApplicationConfig>()
        val values = mapOf(
            Pair("couchbase.connectionString", mockk<ApplicationConfigValue>()),
            Pair("couchbase.username", mockk<ApplicationConfigValue>()),
            Pair("couchbase.password", mockk<ApplicationConfigValue>()),
            Pair("couchbase.bucket", mockk<ApplicationConfigValue>()),
            Pair("couchbase.scope", mockk<ApplicationConfigValue>())
        )
        every { values["couchbase.connectionString"]!!.getString() } returns "connectionStringValue"
        every { values["couchbase.username"]!!.getString() } returns "usernameValue"
        every { values["couchbase.password"]!!.getString() } returns "passwordValue"
        every { values["couchbase.bucket"]!!.getString() } returns "bucketValue"
        every { values["couchbase.scope"]!!.getString() } returns "scopeValue"

        every { cfg.propertyOrNull(any()) } answers {
            values[arg(0)]
        }

        val result = CouchbaseConfiguration(cfg)

        assert(result.connectionString == "connectionStringValue")
        assert(result.username == "usernameValue")
        assert(result.password == "passwordValue")
        assert(result.bucket == "bucketValue")
        assert(result.scope == "scopeValue")
    }

    @Test
    fun testCreateCluster() {
        val cfg : CouchbaseConfiguration = mockk()
        every { cfg.connectionString } returns "localhost"
        every { cfg.username } returns "Administrator"
        every { cfg.password } returns "password"

        val cluster : Cluster = createCluster(cfg)

        verify {
            cfg.connectionString
            cfg.username
            cfg.password
        }
    }

    @Test
    fun testCreateBucket() {
        val cfg = mockk<CouchbaseConfiguration>()
        every { cfg.bucket } returns "bucketValue"

        val bucket = mockk<Bucket>()
        every { runBlocking {
            bucket.waitUntilReady(10.seconds)
        } } returns bucket

        val cluster = mockk<Cluster>()
        every { cluster.bucket("bucketValue") } returns bucket

        assert(bucket == createBucket(cluster, cfg))

        verify {
            cfg.bucket
            cluster.bucket("bucketValue")
        }
    }

    @Test
    fun testCreateScope() {
        val cfg = mockk<CouchbaseConfiguration>()
        every { cfg.scope } returns "scopeValue"

        val scope = mockk<Scope>()

        val bucket = mockk<Bucket>()
        every { bucket.scope("scopeValue") } returns scope

        assert(scope == createScope(bucket, cfg))

        verify {
            cfg.scope
            bucket.scope("scopeValue")
        }
    }
}