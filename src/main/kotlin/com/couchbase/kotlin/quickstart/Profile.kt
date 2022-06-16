package com.couchbase.kotlin.quickstart;

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.*

open class ProfileWithoutId(
  var firstName: String = "",
  var lastName: String = "",
  var email: String = "",
  var password: String = "",
  var balance: Int = 0
)

class Profile(
  val pid: UUID = UUID.randomUUID()
) : ProfileWithoutId()

val profileModule = module {
  singleOf(::ProfileRepository)
  singleOf(::ProfileService)
}