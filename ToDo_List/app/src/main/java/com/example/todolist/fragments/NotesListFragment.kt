package com.example.todolist.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.MainViewModel
import com.example.todolist.R
import com.example.todolist.adapters.NotesAdapter
import com.example.todolist.data.Note
import com.example.todolist.databinding.FragmentNotesListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import com.example.todolist.databinding.DateFilterDialogLayoutBinding
import com.example.todolist.databinding.SortBsDialogLayoutBinding
import com.example.todolist.databinding.StatusFilterBsDialogLayoutBinding
import com.example.todolist.utils.updateDateExt
import com.google.android.material.bottomsheet.BottomSheetDialog

@AndroidEntryPoint
class NotesListFragment : Fragment() {

    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private val adapter = NotesAdapter(
        {note -> onNoteClick(note)},
        {note -> onCheckboxClick(note)}
    )
    private var _datePickerDialog: Dialog? = null
    private val datePickerDialog get() = _datePickerDialog!!
    private var _datePickerBinding: DateFilterDialogLayoutBinding? = null
    private val datePickerBinding get() = _datePickerBinding!!

    private var _statusPickerDialog: BottomSheetDialog? = null
    private val statusPickerDialog get() = _statusPickerDialog!!
    private var _statusPickerDialogBinding: StatusFilterBsDialogLayoutBinding? = null
    private val statusPickerDialogBinding get() = _statusPickerDialogBinding!!

    private var _sortDialog: BottomSheetDialog? = null
    private val sortDialog get() = _sortDialog!!
    private var _sortDialogBinding: SortBsDialogLayoutBinding? = null
    private val sortDialogBinding get() = _sortDialogBinding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindAdapter()
        bindAddButton()
        bindTopBar()
        initDatePickerDialog()
        initStatusPickerDialog()
        initSortDialog()
        bindDatePickerDialog()
        bindStatusPickerDialog()
        bindSortDialog()
        observeFiltersState()
        observeSortState()
        showNotes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindAdapter(){
        getNoteSwapper().attachToRecyclerView(binding.listNotes)
        binding.listNotes.adapter = adapter
    }

    private fun bindAddButton(){
        binding.buttonAdd.setOnClickListener{
            findNavController().navigate(R.id.action_notesListFragment_to_noteEditFragment)
        }
    }

    private fun bindTopBar(){
        with (binding) {
            etSearch.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(keyword: Editable?) {
                        viewModel.setKeywordFilter(keyword.toString())
                    }
                }
            )

            imgDate.setOnClickListener {
                datePickerDialog.show()
            }

            imgStatus.setOnClickListener {
                statusPickerDialog.show()
            }

            sort.setOnClickListener {
                sortDialog.show()
            }
        }
    }

    private fun bindDatePickerDialog(){
        with(datePickerBinding) {
            btnApply.setOnClickListener {
                val calendarSince = Calendar.getInstance()
                val calendarTo = Calendar.getInstance()
                with(datePickerBinding) {
                    calendarSince.set(dpSince.year, dpSince.month, dpSince.dayOfMonth)
                    calendarTo.set(dpTo.year, dpTo.month, dpTo.dayOfMonth)
                }
                viewModel.setDateFilter(
                    MainViewModel.FiltersApplied.Date(
                        calendarSince.timeInMillis,
                        calendarTo.timeInMillis
                    ),
                    false
                )
                datePickerDialog.dismiss()
            }

            btnClear.setOnClickListener {
                val calendar = Calendar.getInstance()
                with(datePickerBinding) {
                    dpSince.updateDateExt(calendar)
                    dpTo.updateDateExt(calendar)
                }
                viewModel.setDateFilter(
                    MainViewModel.FiltersApplied.Date(
                        0L,
                        0L
                    ),
                    true
                )
                datePickerDialog.dismiss()
            }
        }
    }

    private fun bindStatusPickerDialog(){
        with(statusPickerDialogBinding){
            radioGroup.setOnCheckedChangeListener { group, id ->
                when(id){
                    R.id.all -> {
                        viewModel.setStatusFilter(MainViewModel.FiltersApplied.Status
                            (MainViewModel.FiltersApplied.SelectedStatus.None)
                        )
                    }
                    R.id.done -> {
                        viewModel.setStatusFilter(MainViewModel.FiltersApplied.Status
                            (MainViewModel.FiltersApplied.SelectedStatus.Done)
                        )
                    }
                    R.id.undone -> {
                        viewModel.setStatusFilter(MainViewModel.FiltersApplied.Status
                            (MainViewModel.FiltersApplied.SelectedStatus.Undone)
                        )
                    }
                }
                statusPickerDialog.dismiss()
            }
        }
    }

    private fun bindSortDialog(){
        with(sortDialogBinding){
            radioGroup.setOnCheckedChangeListener { group, id ->
                when(id){
                    R.id.defaultSort -> {
                        viewModel.applySort(MainViewModel.SortApplied.Default)
                    }
                    R.id.earlier -> {
                        viewModel.applySort(MainViewModel.SortApplied.Earlier)
                    }
                    R.id.later -> {
                        viewModel.applySort(MainViewModel.SortApplied.Later)
                    }
                }
                sortDialog.dismiss()
            }
        }
    }

    private fun observeFiltersState(){
        lifecycleScope.launch {
            viewModel.filters.collect { filters ->
                filters.forEach { filter ->
                    when(filter){
                        is MainViewModel.FiltersApplied.Date -> {
                            val calendarSince = Calendar.getInstance()
                            val calendarTo = Calendar.getInstance()
                            calendarSince.timeInMillis = filter.dateSince
                            calendarTo.timeInMillis = filter.dateTo
                            with(datePickerBinding) {
                                dpSince.updateDateExt(calendarSince)
                                dpTo.updateDateExt(calendarTo)
                            }
                        }
                        is MainViewModel.FiltersApplied.Keyword -> {
                        }
                        is MainViewModel.FiltersApplied.Status -> {
                            with (statusPickerDialogBinding){
                                when(filter.status){
                                    is MainViewModel.FiltersApplied.SelectedStatus.None -> {
                                        radioGroup.check(R.id.all)
                                    }
                                    is MainViewModel.FiltersApplied.SelectedStatus.Done -> {
                                        radioGroup.check(R.id.done)
                                    }
                                    is MainViewModel.FiltersApplied.SelectedStatus.Undone -> {
                                        radioGroup.check(R.id.undone)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeSortState(){
        lifecycleScope.launch {
            viewModel.sort.collect { sort ->
                when(sort){
                    MainViewModel.SortApplied.Default -> {
                        binding.tvSort.text = resources.getString(R.string.default_sort)
                        sortDialogBinding.radioGroup.check(R.id.defaultSort)
                    }
                    MainViewModel.SortApplied.Earlier -> {
                        binding.tvSort.text = resources.getString(R.string.earlier_sort)
                        sortDialogBinding.radioGroup.check(R.id.earlier)
                    }
                    MainViewModel.SortApplied.Later -> {
                        binding.tvSort.text = resources.getString(R.string.later_sort)
                        sortDialogBinding.radioGroup.check(R.id.later)
                    }
                }
            }
        }
    }

    private fun onNoteClick(note: Note) {
        val bundle = bundleOf("noteID" to note.noteID)
        findNavController().navigate(R.id.action_notesListFragment_to_noteEditFragment, bundle)
    }

    private fun onCheckboxClick(note: Note) {
        viewModel.changeNoteStatus(note)
    }

    private fun showNotes(){
        lifecycleScope.launch {
            viewModel.allNotes.collect { notesList ->
                adapter.submitList(notesList)
            }
        }
    }

    private fun getNoteSwapper(): ItemTouchHelper{
        return ItemTouchHelper(object : ItemTouchHelper.
        SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.deleteNote(adapter.currentList[viewHolder.adapterPosition])
            }
        })
    }

    private fun initDatePickerDialog(){
        _datePickerDialog = Dialog(requireActivity())
        datePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        datePickerDialog.setContentView(R.layout.date_filter_dialog_layout)
        _datePickerBinding = DateFilterDialogLayoutBinding.inflate(layoutInflater)
        datePickerDialog.setContentView(datePickerBinding.root)
        datePickerDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        datePickerDialog.window?.setGravity(Gravity.CENTER)
        datePickerDialog.window?.setBackgroundDrawable(requireActivity().getDrawable(R.drawable.bg_dialog))
    }

    private fun initStatusPickerDialog(){
        _statusPickerDialog = BottomSheetDialog(requireActivity())
        statusPickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        statusPickerDialog.setContentView(R.layout.status_filter_bs_dialog_layout)
        _statusPickerDialogBinding = StatusFilterBsDialogLayoutBinding.inflate(layoutInflater)
        statusPickerDialog.setContentView(statusPickerDialogBinding.root)
        statusPickerDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        statusPickerDialog.window?.setGravity(Gravity.BOTTOM)
        statusPickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun initSortDialog(){
        _sortDialog = BottomSheetDialog(requireActivity())
        sortDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        sortDialog.setContentView(R.layout.sort_bs_dialog_layout)
        _sortDialogBinding = SortBsDialogLayoutBinding.inflate(layoutInflater)
        sortDialog.setContentView(sortDialogBinding.root)
        sortDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        sortDialog.window?.setGravity(Gravity.BOTTOM)
        sortDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}