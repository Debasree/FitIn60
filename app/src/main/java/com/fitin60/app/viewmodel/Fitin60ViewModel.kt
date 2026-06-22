package com.fitin60.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitin60.app.data.local.DayPlanEntity
import com.fitin60.app.data.local.ProgramEntity
import com.fitin60.app.data.local.WeeklyCheckinEntity
import com.fitin60.app.data.parser.PlanParseResult
import com.fitin60.app.data.repository.Fitin60Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ImportState(
    val rawInput: String = "",
    val previewDays: List<DayPlanEntity> = emptyList(),
    val previewProgramName: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)

data class HomeState(
    val program: ProgramEntity? = null,
    val days: List<DayPlanEntity> = emptyList(),
    val checkins: List<WeeklyCheckinEntity> = emptyList(),
    val currentDay: Int? = null,
    val currentWeek: Int? = null,
)

class Fitin60ViewModel(
    private val repository: Fitin60Repository,
) : ViewModel() {

    private val _importState = MutableStateFlow(ImportState())
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    val hasProgram: StateFlow<Boolean?> = repository.observeProgram()
        .map { it != null }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val homeState: StateFlow<HomeState> = combine(
        repository.observeProgram(),
        repository.observeAllDays(),
        repository.observeCheckins(),
    ) { program, days, checkins ->
        HomeState(
            program = program,
            days = days,
            checkins = checkins,
            currentDay = repository.currentDay(program),
            currentWeek = repository.currentWeek(program),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeState(),
    )

    fun observeDay(day: Int) = repository.observeDay(day)

    fun updateInput(text: String) {
        _importState.value = _importState.value.copy(rawInput = text, errorMessage = null)
    }

    fun previewParse() {
        val input = _importState.value.rawInput
        _importState.value = _importState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.previewParse(input)) {
                is PlanParseResult.Success -> _importState.value = _importState.value.copy(
                    previewDays = result.days,
                    previewProgramName = result.programName,
                    errorMessage = null,
                    isLoading = false,
                )
                is PlanParseResult.Failure -> _importState.value = _importState.value.copy(
                    previewDays = emptyList(),
                    previewProgramName = "",
                    errorMessage = result.message,
                    isLoading = false,
                )
            }
        }
    }

    fun confirmImport(onDone: () -> Unit) {
        viewModelScope.launch {
            val state = _importState.value
            if (state.previewDays.isEmpty()) return@launch
            when (val r = repository.importPlan(state.rawInput)) {
                is PlanParseResult.Success -> {
                    _importState.value = ImportState()
                    onDone()
                }
                is PlanParseResult.Failure -> {
                    _importState.value = state.copy(errorMessage = r.message)
                }
            }
        }
    }

    fun useSamplePlan(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.startWithSeed()
            onDone()
        }
    }

    fun resetProgram(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.resetProgram()
            onDone()
        }
    }

    fun toggleSleep(day: DayPlanEntity) = viewModelScope.launch {
        repository.updateDay(day.copy(sleepDone = !day.sleepDone))
    }

    fun toggleMeals(day: DayPlanEntity) = viewModelScope.launch {
        repository.updateDay(day.copy(mealsDone = !day.mealsDone))
    }

    fun toggleWorkout(day: DayPlanEntity) = viewModelScope.launch {
        repository.updateDay(day.copy(workoutDone = !day.workoutDone))
    }

    fun saveUserNotes(day: DayPlanEntity, notes: String) = viewModelScope.launch {
        repository.updateDay(day.copy(userNotes = notes))
    }

    fun saveCheckin(week: Int, weightKg: Double?, photoUri: Uri?, notes: String) =
        viewModelScope.launch {
            val existing = repository.getCheckin(week)
            val storedPath = if (photoUri != null) {
                repository.copyPhotoToInternalStorage(photoUri) ?: existing?.photoPath
            } else existing?.photoPath
            repository.upsertCheckin(
                WeeklyCheckinEntity(
                    weekNumber = week,
                    weightKg = weightKg ?: existing?.weightKg,
                    photoPath = storedPath,
                    notes = notes,
                    recordedAtMillis = System.currentTimeMillis(),
                )
            )
        }
}

class Fitin60ViewModelFactory(
    private val repository: Fitin60Repository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(Fitin60ViewModel::class.java))
        return Fitin60ViewModel(repository) as T
    }
}
