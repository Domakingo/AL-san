package com.doma.alsan.data.network.apollo

import com.apollographql.apollo3.ApolloClient

interface ApolloHandler {
    val apolloClient: ApolloClient
}