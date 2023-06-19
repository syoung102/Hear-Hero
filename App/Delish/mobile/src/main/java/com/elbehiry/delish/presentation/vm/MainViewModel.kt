/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elbehiry.delish.presentation.vm

import app.delish.base.vm.MviViewModel
import app.delish.domain.usecases.OnBoardingCompletedUseCase
import app.delish.result.data
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val onBoardingCompletedUseCase: OnBoardingCompletedUseCase
) : MviViewModel<ViewEvent, ViewResult, ViewState, ViewEffect>(ViewState()) {

    init {
        processEvent(ViewEvent.OnBoardingStatus)
    }

    override fun Flow<ViewEvent>.toResults(): Flow<ViewResult> {
        return merge(
            filterIsInstance<ViewEvent.OnBoardingStatus>().onBoardingToMainResult()
        )
    }

    private fun Flow<ViewEvent.OnBoardingStatus>.onBoardingToMainResult(): Flow<ViewResult> {
        return flatMapLatest {
            onBoardingCompletedUseCase(Unit).map { result ->
                delay(1000)
                ViewResult.OnBoardingShown(result.data ?: false)
            }
        }
    }

    override fun ViewResult.reduce(state: ViewState): ViewState {
        return when(this) {
            is ViewResult.OnBoardingShown -> state.copy(
                isOnBoardingShown = isShown
            )
        }
    }
}
