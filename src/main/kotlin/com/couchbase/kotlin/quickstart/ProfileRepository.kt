package com.couchbase.kotlin.quickstart

import com.couchbase.client.kotlin.Scope
import com.couchbase.client.kotlin.query.execute
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import java.util.*

class ProfileRepository(scope: Scope) {
    private val collection = scope.collection("profile")
    private val databaseContext = newSingleThreadContext("CouchbaseThread")

    fun store(profile: Profile): Profile {
        runBlocking(databaseContext) {
            collection.insert(profile.pid.toString(), profile)
        }
        return profile
    }

    fun delete(id: UUID) {
        runBlocking(databaseContext) {
            collection.remove(id.toString());
        }
    }

    fun search(text: String, skip: Int = 0, limit: Int = 10): List<Profile> {
        return runBlocking(databaseContext) {
            val lowerText = text.lowercase()
            val qryString = "SELECT p.* FROM `profile` p " +
                    "WHERE lower(p.firstName) LIKE '%$lowerText%' " +
                    "OR lower(p.lastName) LIKE '%$lowerText%' " +
                    "LIMIT $limit OFFSET $skip"
            val result = collection.scope.query(qryString, readonly = true).execute()
            result.rows.map {
                it.contentAs<Profile>()
            }.toList()
        }
    }

    fun transfer(sourceId: UUID, targetId: UUID, amount: Int) {
        runBlocking(databaseContext) {
            collection.scope.query(
                "BEGIN TRANSACTION; " +
                        "UPDATE profile SET balance = balance - $amount WHERE META().id = $sourceId; " +
                        "UPDATE profile SET balance = balance + $amount WHERE META().id = $targetId; " +
                        "COMMIT TRANSACTION;"
            )
        }
    }

    fun list(skip: Int, limit: Int): List<Profile> {
        val result: LinkedList<Profile> = LinkedList()
        runBlocking(databaseContext) {
            collection.scope
                .query("SELECT raw profile from profile OFFSET $skip LIMIT $limit")
                .execute {
                    result.add(it.contentAs())
                }
        }
        return result
    }

    fun create(data: ProfileWithoutId): Profile {
        val profile = Profile(
            pid = UUID.randomUUID()
        ).apply {
            firstName = data.firstName
            lastName = data.lastName
            email = data.email
            password = data.password
            balance = data.balance
        }

        runBlocking(databaseContext) {
            collection.insert(profile.pid.toString(), profile)
        }
        return profile
    }

    fun getById(id: UUID): Profile {
        var result: Profile
        runBlocking(databaseContext) {
            result = collection.get(id.toString()).contentAs()
        }
        return result
    }
}