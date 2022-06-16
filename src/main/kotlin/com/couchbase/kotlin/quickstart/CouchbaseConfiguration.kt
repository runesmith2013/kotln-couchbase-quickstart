package com.couchbase.kotlin.quickstart

import com.couchbase.client.kotlin.Bucket
import com.couchbase.client.kotlin.Cluster
import com.couchbase.client.kotlin.Scope
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
val couchbaseModule = module {
  singleOf(::CouchbaseConfiguration)
  singleOf(::createCluster)
  singleOf(::createBucket)
  singleOf(::createScope)
}

class CouchbaseConfiguration(cfg: ApplicationConfig) {
  val connectionString: String = cfg.propertyOrNull("couchbase.connectionString")?.getString() ?: "localhost"
  val username: String = cfg.propertyOrNull("couchbase.username")?.getString() ?: "Administrator"
  val password: String = cfg.propertyOrNull("couchbase.password")?.getString() ?: "password"
  val bucket: String = cfg.propertyOrNull("couchbase.bucket")?.getString() ?: "user_profile"
  val scope: String = cfg.propertyOrNull("couchbase.scope")?.getString() ?: "_default"
}

fun createCluster(configuration: CouchbaseConfiguration): Cluster {
  return Cluster.connect(
    connectionString = configuration.connectionString,
    username = configuration.username,
    password = configuration.password,
  )
}


@ExperimentalTime
fun createBucket(cluster: Cluster, configuration: CouchbaseConfiguration): Bucket {
  val result : Bucket?
  runBlocking {
    result = cluster.bucket(configuration.bucket).waitUntilReady(10.seconds)
  }
  return result!!
}

fun createScope(bucket: Bucket, configuration: CouchbaseConfiguration): Scope {
  return bucket.scope(configuration.scope)
}
