package app.delish.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.delish.domain.usecases.InitHomeUseCase
import app.delish.domain.usecases.OnBoardingCompleteActionSuspendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OnBoardingViewModel @Inject constructor(
    val onBoardingCompleteActionUseCase: OnBoardingCompleteActionSuspendUseCase,
    val initHomeUseCase: InitHomeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(false)
    val viewState: StateFlow<Boolean>
        get() = _state

    init {
        viewModelScope.launch {
            initHomeUseCase(Unit)
        }
    }

    fun getStartedClick() {
        viewModelScope.launch {
            onBoardingCompleteActionUseCase(true)
            _state.value = true
        }
    }

//    fun getOnBoardingItemsList() = OnBoardingProvider.onBoardingItems
}
