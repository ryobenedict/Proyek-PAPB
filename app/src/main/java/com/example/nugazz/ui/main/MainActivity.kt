package com.example.nugazz.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nugazz.data.local.Task
import com.example.nugazz.databinding.ActivityMainBinding
import com.example.nugazz.ui.task.AddTaskBottomSheetFragment
import com.example.nugazz.viewmodel.TaskViewModel
import com.google.android.material.snackbar.Snackbar

// Activity utama yang bertindak sebagai wadah (container) bagi seluruh antarmuka aplikasi
class MainActivity : AppCompatActivity() {

    // ViewBinding untuk mengakses komponen layout secara type-safe dan null-safe
    private lateinit var binding: ActivityMainBinding
    
    // Inisialisasi ViewModel menggunakan delegasi 'by viewModels()'
    private val viewModel: TaskViewModel by viewModels()
    
    // Adapter untuk mengelola tampilan item di RecyclerView
    private lateinit var adapter: TaskAdapter
    
    // List lokal untuk menyimpan data dari database guna keperluan filtering
    private var allTasks: List<Task> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Konfigurasi awal komponen UI
        setupRecyclerView()
        setupFilters()
        setupSwipeToDelete()

        // Tombol Floating Action (FAB) untuk menambah tugas baru
        binding.fabAdd.setOnClickListener {
            // Menampilkan BottomSheet sebagai form input
            AddTaskBottomSheetFragment.newInstance().show(supportFragmentManager, "AddTaskBottomSheet")
        }

        // Mengamati (Observe) perubahan data tugas dari ViewModel
        // Setiap kali data di database berubah, blok kode ini akan otomatis dijalankan
        viewModel.allTasks.observe(this) { tasks ->
            allTasks = tasks
            applyFilter() // Memperbarui daftar yang ditampilkan berdasarkan filter aktif
        }
    }

    // Mengatur RecyclerView dengan LayoutManager dan Adapter
    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onCheckedChange = { task, isChecked ->
                // Aksi saat CheckBox diklik: perbarui status di database
                viewModel.updateCompletionStatus(task.id, isChecked)
            },
            onItemClick = { task ->
                // Aksi saat item diklik: tampilkan form edit
                AddTaskBottomSheetFragment.newInstance(task).show(supportFragmentManager, "EditTaskBottomSheet")
            }
        )
        
        binding.rvTasks.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    // Mengatur listener untuk sistem filter (Chips)
    private fun setupFilters() {
        binding.cgFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            applyFilter()
        }
    }

    // Logika penyaringan data berdasarkan tab yang dipilih (Semua, Aktif, Selesai)
    private fun applyFilter() {
        val filteredList = when (binding.cgFilters.checkedChipId) {
            binding.chipActive.id -> allTasks.filter { !it.isCompleted }
            binding.chipCompleted.id -> allTasks.filter { it.isCompleted }
            else -> allTasks
        }
        
        // Mengirim list hasil filter ke adapter
        adapter.submitList(filteredList)
        
        // Tampilkan 'Empty State' jika tidak ada tugas yang sesuai kriteria
        if (filteredList.isEmpty()) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.rvTasks.visibility = View.GONE
        } else {
            binding.llEmptyState.visibility = View.GONE
            binding.rvTasks.visibility = View.VISIBLE
        }
        
        // Memperbarui statistik tugas aktif di bagian header
        updateTaskStats(allTasks.count { !it.isCompleted })
    }

    // Memperbarui teks jumlah tugas yang belum selesai
    private fun updateTaskStats(activeCount: Int) {
        binding.tvTaskStats.text = "$activeCount Tasks Remaining"
    }

    // Fitur modern: Swipe ke samping untuk menghapus tugas
    private fun setupSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = adapter.currentList[viewHolder.adapterPosition]
                viewModel.delete(task) // Hapus dari database
                
                // Menampilkan Snackbar untuk konfirmasi penghapusan dengan opsi 'Undo'
                Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") { viewModel.insert(task) }
                    .show()
            }
        }).attachToRecyclerView(binding.rvTasks)
    }
}
