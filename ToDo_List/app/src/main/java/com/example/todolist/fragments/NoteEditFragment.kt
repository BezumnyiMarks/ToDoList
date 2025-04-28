package com.example.todolist.fragments

import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todolist.MainViewModel
import com.example.todolist.R
import com.example.todolist.data.Note
import com.example.todolist.databinding.DateFilterDialogLayoutBinding
import com.example.todolist.databinding.FragmentNoteEditBinding
import com.example.todolist.databinding.FragmentNotesListBinding
import com.example.todolist.databinding.TimePickerDialogLayoutBinding
import com.example.todolist.utils.getHourExt
import com.example.todolist.utils.getMinuteExt
import com.example.todolist.utils.getMonthExt
import com.example.todolist.utils.updateDateExt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class NoteEditFragment : Fragment() {

    private var _binding: FragmentNoteEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private val calendar = Calendar.getInstance()

    private var _datePickerDialog: Dialog? = null
    private val datePickerDialog get() = _datePickerDialog!!
    private var _datePickerBinding: DateFilterDialogLayoutBinding? = null
    private val datePickerBinding get() = _datePickerBinding!!

    private var _timePickerDialog: Dialog? = null
    private val timePickerDialog get() = _timePickerDialog!!
    private var _timePickerBinding: TimePickerDialogLayoutBinding? = null
    private val timePickerBinding get() = _timePickerBinding!!
    private var noteID: Long ?= null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteID = arguments?.getLong("noteID")
        bindUI()
        showNote()
        initDatePickerDialog()
        initTimePickerDialog()
        bindDatePickerDialog()
        bindTimePickerDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showNote(){
        viewModel.clearCurrentNote()
        noteID?.let { viewModel.getNoteByID(it) }
            lifecycleScope.launch {
                viewModel.noteStateFlow.collect { note ->
                    if (note.dateTime != 0L && noteID != null) {
                        calendar.timeInMillis = note.dateTime
                        showDate()
                        showTime()
                    }
                    with(binding) {
                        buttonSave.setOnClickListener{
                            if (binding.editTextTitle.text.isNotEmpty() && binding.editTextDesc.text.isNotEmpty()) {
                                viewModel.addNote(
                                    Note(
                                        noteID = noteID,
                                        header = binding.editTextTitle.text.toString(),
                                        description = binding.editTextDesc.text.toString(),
                                        done = if (noteID != null) note.done else false,
                                        dateTime = if (binding.tvDate.text.isNotEmpty()) calendar.timeInMillis else 0L
                                    )
                                )
                                findNavController().popBackStack()
                            }
                        }
                        editTextTitle.setText(note.header)
                        editTextDesc.setText(note.description)
                    }
                }
            }
    }

    private fun showDate(){
        val date = "${calendar.get(Calendar.DATE)}" +
                " ${calendar.getMonthExt()}" +
                " ${calendar.get(Calendar.YEAR)}"
        binding.tvDate.text = date
    }

    private fun showTime(){
        val time = "${calendar.getHourExt()}:${calendar.getMinuteExt()}"
        binding.tvTime.text = time
    }

    private fun bindUI(){
        with(binding){
            dateContainer.setOnClickListener {
                datePickerDialog.show()
            }

            timeContainer.setOnClickListener {
                timePickerDialog.show()
            }
        }
    }

    private fun bindDatePickerDialog(){
        with(datePickerBinding) {
            btnApply.text = resources.getString(R.string.save)
            btnClear.text = resources.getString(R.string.cancel)
            tvSince.visibility = View.GONE
            tvTo.visibility = View.GONE
            dpSince.visibility = View.GONE

            btnApply.setOnClickListener {
                with(datePickerBinding) {
                    calendar.set(dpTo.year, dpTo.month, dpTo.dayOfMonth)
                }
                showDate()
                datePickerDialog.dismiss()
            }

            btnClear.setOnClickListener {
                datePickerDialog.dismiss()
            }
        }
    }

    private fun bindTimePickerDialog(){
        with(timePickerBinding) {
            btnSave.setOnClickListener {
                with(timePickerBinding) {
                    calendar.set(
                        calendar.get(Calendar.DATE),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.YEAR),
                        timePicker.hour,
                        timePicker.minute
                    )
                }
                showTime()
                timePickerDialog.dismiss()
            }

            btnCancel.setOnClickListener {
                timePickerDialog.dismiss()
            }
        }
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

    private fun initTimePickerDialog(){
        _timePickerDialog = Dialog(requireActivity())
        timePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        timePickerDialog.setContentView(R.layout.time_picker_dialog_layout)
        _timePickerBinding = TimePickerDialogLayoutBinding.inflate(layoutInflater)
        timePickerDialog.setContentView(timePickerBinding.root)
        timePickerDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        timePickerDialog.window?.setGravity(Gravity.CENTER)
        timePickerDialog.window?.setBackgroundDrawable(requireActivity().getDrawable(R.drawable.bg_dialog))
    }
}