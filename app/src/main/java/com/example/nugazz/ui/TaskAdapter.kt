package com.example.nugazz.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nugazz.R
import com.example.nugazz.data.Task
import com.example.nugazz.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onCheckedChange: (Task, Boolean) -> Unit,
    private val onItemClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback) {

    private val dateTimeFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.apply {
                tvTitle.text = task.title
                tvDescription.text = task.description
                cbCompleted.isChecked = task.isCompleted

                // Task Completion Effects
                if (task.isCompleted) {
                    tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    root.alpha = 0.6f
                } else {
                    tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    root.alpha = 1.0f
                }

                // Priority Indicator
                val color = when (task.priority) {
                    1 -> Color.RED
                    2 -> Color.YELLOW
                    3 -> Color.GREEN
                    else -> Color.GRAY
                }
                priorityIndicator.setBackgroundColor(color)

                // Due Date Display (Date + Time)
                task.dueDate?.let {
                    tvDueDate.text = dateTimeFormat.format(Date(it))
                    
                    // Highlight overdue tasks in red
                    if (it < System.currentTimeMillis() && !task.isCompleted) {
                        tvDueDate.setTextColor(Color.RED)
                    } else {
                        tvDueDate.setTextColor(Color.GRAY)
                    }
                } ?: run {
                    tvDueDate.text = "No deadline"
                }

                cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                    onCheckedChange(task, isChecked)
                }

                root.setOnClickListener {
                    onItemClick(task)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
