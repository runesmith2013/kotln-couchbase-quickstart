package com.couchbase.kotlin.quickstart;

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.*

// This class is used to represent
// client requests to create a new 
// profile record
open class ProfileWithoutId(
  var firstName: String = "",
  var lastName: String = "",
  var email: String = "",
  var password: String = "",
  var balance: Int = 0
)

// This class is used to represent
// profile records
class Profile(
  val pid: UUID = UUID.randomUUID()
) : ProfileWithoutId()

val profileModule = module {
  singleOf(::ProfileRepository)
  singleOf(::ProfileService)
}
