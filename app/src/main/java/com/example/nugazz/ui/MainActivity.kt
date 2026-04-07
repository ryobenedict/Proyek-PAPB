package com.example.nugazz.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nugazz.data.Task
import com.example.nugazz.databinding.ActivityMainBinding
import com.example.nugazz.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private var allTasks: List<Task> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeToDelete()
        setupFilters()

        viewModel.allTasks.observe(this) { tasks ->
            allTasks = tasks
            applyFilter()
        }

        binding.fabAdd.setOnClickListener {
            AddTaskBottomSheetFragment.newInstance().show(supportFragmentManager, "AddTaskBottomSheet")
        }
    }

    private fun setupFilters() {
        binding.cgFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filteredList = when (binding.cgFilters.checkedChipId) {
            binding.chipActive.id -> allTasks.filter { !it.isCompleted }
            binding.chipCompleted.id -> allTasks.filter { it.isCompleted }
            else -> allTasks
        }

        adapter.submitList(filteredList)
        updateUI(filteredList.isEmpty())
        updateTaskStats(allTasks.count { !it.isCompleted })
    }

    private fun updateUI(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvTasks.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvTasks.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE
        }
    }

    private fun updateTaskStats(count: Int) {
        val statsText = if (count > 0) {
            "Kamu punya $count tugas yang harus diselesaikan"
        } else {
            "Semua tugas sudah beres! Mantap!"
        }
        binding.tvTaskStats.text = statsText
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onCheckedChange = { task, isChecked ->
                viewModel.updateCompletionStatus(task.id, isChecked)
            },
            onItemClick = { task ->
                AddTaskBottomSheetFragment.newInstance(task).show(supportFragmentManager, "EditTaskBottomSheet")
            }
        )

        binding.rvTasks.apply {
            this.adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = adapter.currentList[position]
                viewModel.delete(task)
                Toast.makeText(this@MainActivity, "Tugas '${task.title}' dihapus", Toast.LENGTH_SHORT).show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvTasks)
    }
}
