package com.example.nugazz.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.nugazz.R
import com.example.nugazz.data.Task
import com.example.nugazz.databinding.BottomSheetAddTaskBinding
import com.example.nugazz.viewmodel.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class AddTaskBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()
    
    private var selectedDateTime: Calendar = Calendar.getInstance()
    private var isDateTimeSelected = false
    
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var taskToEdit: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskToEdit = arguments?.getParcelable("TASK_TO_EDIT")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskToEdit?.let { task ->
            binding.tvAddTaskTitle.text = "Edit Tugas"
            binding.etTitle.setText(task.title)
            binding.etDescription.setText(task.description)
            when (task.priority) {
                1 -> binding.rbHigh.isChecked = true
                2 -> binding.rbMedium.isChecked = true
                3 -> binding.rbLow.isChecked = true
            }
            task.dueDate?.let {
                selectedDateTime.timeInMillis = it
                isDateTimeSelected = true
                binding.etDueDate.setText(dateFormat.format(selectedDateTime.time))
                binding.etDueTime.setText(timeFormat.format(selectedDateTime.time))
            }
            binding.btnSave.text = "Update Tugas"
        }

        binding.etDueDate.setOnClickListener { showDatePicker() }
        binding.etDueTime.setOnClickListener { showTimePicker() }
        binding.btnSave.setOnClickListener { saveTask() }
        
        setupPrioritySelectionUI()
    }

    private fun setupPrioritySelectionUI() {
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val gray = Color.parseColor("#757575")

        binding.rgPriority.setOnCheckedChangeListener { _, checkedId ->
            binding.rbHigh.setTextColor(if (checkedId == R.id.rb_high) white else gray)
            binding.rbMedium.setTextColor(if (checkedId == R.id.rb_medium) white else gray)
            binding.rbLow.setTextColor(if (checkedId == R.id.rb_low) white else gray)
        }
        
        // Initial state
        binding.rbHigh.setTextColor(if (binding.rbHigh.isChecked) white else gray)
        binding.rbMedium.setTextColor(if (binding.rbMedium.isChecked) white else gray)
        binding.rbLow.setTextColor(if (binding.rbLow.isChecked) white else gray)
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDateTime.set(Calendar.YEAR, year)
                selectedDateTime.set(Calendar.MONTH, month)
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                isDateTimeSelected = true
                binding.etDueDate.setText(dateFormat.format(selectedDateTime.time))
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDateTime.set(Calendar.MINUTE, minute)
                isDateTimeSelected = true
                binding.etDueTime.setText(timeFormat.format(selectedDateTime.time))
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val priority = when (binding.rgPriority.checkedRadioButtonId) {
            binding.rbHigh.id -> 1
            binding.rbLow.id -> 3
            else -> 2
        }

        if (title.isNotEmpty()) {
            val dueDateValue = if (isDateTimeSelected) selectedDateTime.timeInMillis else null
            
            val task = if (taskToEdit == null) {
                Task(title = title, description = description, priority = priority, dueDate = dueDateValue)
            } else {
                taskToEdit!!.copy(title = title, description = description, priority = priority, dueDate = dueDateValue)
            }

            if (taskToEdit == null) viewModel.insert(task) else viewModel.update(task)
            dismiss()
        } else {
            Toast.makeText(context, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: Task? = null): AddTaskBottomSheetFragment {
            val fragment = AddTaskBottomSheetFragment()
            val args = Bundle()
            args.putParcelable("TASK_TO_EDIT", task)
            fragment.arguments = args
            return fragment
        }
    }
}
