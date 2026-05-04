package com.example.nugazz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.nugazz.data.local.AppDatabase
import com.example.nugazz.data.local.Task
import com.example.nugazz.data.local.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ViewModel bertugas menyediakan data untuk UI dan bertahan dari perubahan konfigurasi (seperti rotasi layar)
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    
    // LiveData yang berisi daftar semua tugas, akan diamati (observe) oleh MainActivity
    val allTasks: LiveData<List<Task>>

    init {
        // Inisialisasi Database, DAO, dan Repository saat ViewModel dibuat
        val taskDao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        allTasks = repository.allTasks
    }

    // Menggunakan Coroutines (viewModelScope.launch) untuk menjalankan operasi database di background thread
    // Dispatchers.IO dikhususkan untuk operasi Input/Output seperti Database atau Jaringan
    
    fun insert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(task)
    }

    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(task)
    }

    // Fungsi khusus untuk memperbarui status selesai/belum tanpa memproses seluruh objek Task
    fun updateCompletionStatus(id: Int, completed: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCompletionStatus(id, completed)
    }
}
