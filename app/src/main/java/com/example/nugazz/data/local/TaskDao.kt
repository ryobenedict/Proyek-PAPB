package com.example.nugazz.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

// DAO (Data Access Object) untuk mendefinisikan cara aplikasi mengakses data di database Room
@Dao
interface TaskDao {
    // Mengambil semua data dan mengurutkannya secara otomatis: 
    // Status (Aktif dulu), Prioritas (Tinggi dulu), Tanggal terdekat, lalu waktu pembuatan
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority ASC, dueDate ASC, createdAt DESC")
    fun getAllTasks(): LiveData<List<Task>>

    // Menyimpan tugas baru. OnConflictStrategy.REPLACE berarti jika ada ID yang sama, data lama akan diperbarui
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    // Memperbarui data tugas yang sudah ada secara keseluruhan
    @Update
    suspend fun updateTask(task: Task)

    // Menghapus data tugas tertentu dari database
    @Delete
    suspend fun deleteTask(task: Task)

    // Query khusus untuk memperbarui status 'selesai' saja tanpa mengganggu data lain (lebih efisien)
    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateCompletionStatus(id: Int, completed: Boolean)
}
