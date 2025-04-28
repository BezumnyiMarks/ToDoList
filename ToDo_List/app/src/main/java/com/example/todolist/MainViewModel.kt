package com.example.todolist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Delete
import com.example.todolist.MainViewModel.FiltersApplied.SelectedStatus
import com.example.todolist.data.Note
import com.example.todolist.database.ToDoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
): ViewModel() {

    private val filtersApplied = MutableStateFlow<List<FiltersApplied>>(listOf(
        FiltersApplied.Status(SelectedStatus.None)
    ))
    val filters = filtersApplied.asStateFlow()

    private val sortApplied = MutableStateFlow<SortApplied>(SortApplied.Default)
    val sort = sortApplied.asStateFlow()

    private val _noteStateFlow = MutableStateFlow<Note>(Note())
    val noteStateFlow = _noteStateFlow.asStateFlow()

    private var _notesStateFlow = MutableStateFlow<List<Note>>(listOf())

    var allNotes: StateFlow<List<Note>> = mainRepository.getAllNotesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList<Note>()
        )

    init {
        applyFilters()
    }

    fun applyFilters(){
        viewModelScope.launch {
            allNotes.collect { notes ->
                _notesStateFlow.emit(notes)

                allNotes = combine (_notesStateFlow, filtersApplied) { notes, filters ->
                    var filteredNotes = notes
                    filters.forEach { filter ->
                        when(filter){
                            is FiltersApplied.Status -> {
                                    when(filter.status){
                                        is SelectedStatus.Done -> {
                                            filteredNotes = filteredNotes.filter {
                                                it.done == true
                                            }
                                        }

                                        is SelectedStatus.Undone -> {
                                            filteredNotes = filteredNotes.filter {
                                                it.done == false
                                            }
                                        }

                                        is SelectedStatus.None -> {

                                        }
                                    }
                            }
                            is FiltersApplied.Keyword -> {
                                filteredNotes = filteredNotes.filter {
                                    it.header.lowercase(Locale.ROOT)
                                        .contains(
                                            filter.keyword.lowercase(Locale.ROOT)
                                        ) ||
                                            it.description.lowercase(Locale.ROOT)
                                                .contains(
                                                    filter.keyword.lowercase(Locale.ROOT)
                                                )

                                }
                            }
                            is FiltersApplied.Date -> {
                                filteredNotes = filteredNotes.filter {
                                    it.dateTime >= filter.dateSince && it.dateTime <= filter.dateTo
                                }
                            }
                        }
                    }
                    filteredNotes
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = _notesStateFlow.value
                )
            }
        }
    }

    fun setKeywordFilter(currentKeyword: String){
        viewModelScope.launch {
            val filtersList = filtersApplied.value.toMutableList()
            filtersList.remove(filtersList.find {
                it is FiltersApplied.Keyword
            })
            if (currentKeyword.isNotEmpty())
                filtersList.add(FiltersApplied.Keyword(currentKeyword))
            filtersApplied.emit(filtersList.toList())
        }
    }

    fun setDateFilter(selectedDates: FiltersApplied.Date, removeFilter: Boolean){
        viewModelScope.launch {
            val filtersList = filtersApplied.value.toMutableList()
            filtersList.remove(filtersList.find {
                it is FiltersApplied.Date
            })
            if (!removeFilter)
                filtersList.add(selectedDates)
            filtersApplied.emit(filtersList.toList())
        }
    }

    fun setStatusFilter(status: FiltersApplied.Status){
        viewModelScope.launch {
            val filtersList = filtersApplied.value.toMutableList()
            filtersList.remove(filtersList.find {
                it is FiltersApplied.Status
            })

            filtersList.add(status)
            filtersApplied.emit(filtersList.toList())
        }
    }

    fun applySort(sort: SortApplied){
        val notes = _notesStateFlow.value.sortedBy {
            it.dateTime
        }
        when(sort){
            SortApplied.Default -> {
                viewModelScope.launch {
                    _notesStateFlow.emit(mainRepository.getAllNotes())
                    sortApplied.emit(SortApplied.Default)
                }
            }
            SortApplied.Earlier -> {
                viewModelScope.launch {
                    _notesStateFlow.emit(notes)
                    sortApplied.emit(SortApplied.Earlier)
                }
            }
            SortApplied.Later -> {
                viewModelScope.launch {
                    _notesStateFlow.emit(notes.asReversed())
                    sortApplied.emit(SortApplied.Later)
                }
            }
        }
    }

    fun getNoteByID(noteID: Long){
        viewModelScope.launch {
            _noteStateFlow.emit(mainRepository.getNoteByID(noteID))
        }
    }

    fun addNote(note: Note){
        viewModelScope.launch {
            mainRepository.addNote(note)
        }
    }

    fun deleteNote(note: Note){
        viewModelScope.launch {
            mainRepository.deleteNote(note)
        }
    }

    fun changeNoteStatus(note: Note){
        addNote(
            note.copy(
                done = !note.done
            )
        )
    }

    fun clearCurrentNote(){
        _noteStateFlow.value = Note()
    }

    sealed class FiltersApplied {
        sealed class SelectedStatus {
            data object Done: SelectedStatus()
            data object Undone: SelectedStatus()
            data object None: SelectedStatus()
        }
        data class Status(val status: SelectedStatus): FiltersApplied()
        data class Keyword(val keyword: String): FiltersApplied()
        data class Date(val dateSince: Long, val dateTo: Long): FiltersApplied()
    }

    sealed class SortApplied {
        data object Default: SortApplied()
        data object Earlier: SortApplied()
        data object Later: SortApplied()
    }
}