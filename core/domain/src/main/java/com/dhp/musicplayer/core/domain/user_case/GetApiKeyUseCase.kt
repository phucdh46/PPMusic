package com.dhp.musicplayer.core.domain.user_case

import com.dhp.musicplayer.core.common.model.ApiResponse
import com.dhp.musicplayer.core.domain.repository.AppRepository
import com.dhp.musicplayer.core.model.settings.ApiKey
import javax.inject.Inject

class GetApiKeyUseCase @Inject constructor(
    private val appRepository: AppRepository, 
) {
    suspend operator fun invoke(): ApiResponse<ApiKey>? =
        appRepository.getKey()
}
