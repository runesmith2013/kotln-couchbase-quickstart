package com.couchbase.kotlin.quickstart.services

import com.couchbase.kotlin.quickstart.models.*
import com.couchbase.kotlin.quickstart.repositories.RouteRepository

class RouteService(repo: RouteRepository) {
    private val repository = repo

    fun createRoute(data: RouteModel, id: String): Route {
        return repository.create(data, id)
    }

    fun updateRoute(route: Route, id: String) {
        repository.update(route, id)
    }

    fun deleteRoute(id: String) {
        repository.delete(id)
    }

    fun getRouteById(id: String): Route = repository.getById(id)
}