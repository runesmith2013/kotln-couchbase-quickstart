package com.couchbase.kotlin.quickstart

import com.couchbase.client.kotlin.Bucket
import com.couchbase.client.kotlin.Cluster
import com.couchbase.client.kotlin.Collection
import com.couchbase.client.kotlin.Scope
import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class CouchbaseConfigurationKtTest {
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
        val collection = mockk<Collection>()

        val bucket = mockk<Bucket>()
        every { bucket.scope("scopeValue") } returns scope
        every { scope.collection("scopeValue") } returns collection

        assert(scope == createScope(bucket, cfg))

        verify {
            cfg.scope
            bucket.scope("scopeValue")
            scope.collection("scopeValue")
        }
    }

}