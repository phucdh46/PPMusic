package com.dhp.musicplayer.core.billing.di

import com.dhp.musicplayer.core.billing.BillingClientProvider
import com.dhp.musicplayer.core.billing.BillingClientProviderImpl
import com.dhp.musicplayer.core.billing.repository.SubscriptionDataRepository
import com.dhp.musicplayer.core.billing.repository.SubscriptionDataRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds
    internal abstract fun bindsBillingClient(
        billingClient: BillingClientProviderImpl,
    ): BillingClientProvider

    @Binds
    internal abstract fun bindsSubscriptionDataRepository(
        subscriptionDataRepository: SubscriptionDataRepositoryImpl,
    ): SubscriptionDataRepository
}