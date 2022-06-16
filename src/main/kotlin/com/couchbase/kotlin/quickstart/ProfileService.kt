package com.couchbase.kotlin.quickstart

import org.koin.java.KoinJavaComponent.inject
import java.util.*

class ProfileService(repo: ProfileRepository){
    private val repository = repo

    fun createProfile(data: ProfileWithoutId): Profile {
        return repository.create(data)
    }

    fun updateProfile(profile: Profile) {
        repository.store(profile)
    }

    fun deleteProfile(id: UUID) {
        repository.delete(id)
    }

    fun transferCredits(source: UUID, target: UUID, amount: Int) {
        repository.transfer(source, target, amount)
    }

    fun listProfiles(skip: Int, limit: Int): List<Profile> {
        return repository.list(skip, limit)
    }

    fun getById(id: UUID): Profile = repository.getById(id)
    fun findProfiles(text: String, skip: Int, limit: Int): List<Profile> = repository.search(text, skip, limit)
}
