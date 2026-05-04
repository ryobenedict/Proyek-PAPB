package com.example.nugazz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// @Entity menandai class ini sebagai tabel di database Room dengan nama 'tasks'
// @Parcelize memungkinkan objek Task dikirim antar Activity/Fragment melalui Intent/Bundle
@Parcelize
@Entity(tableName = "tasks")
data class Task(
    // PrimaryKey dengan autoGenerate=true berarti ID akan dibuat otomatis secara berurutan
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val title: String,          // Judul tugas
    val description: String,    // Deskripsi atau detail tugas
    val priority: Int,         // Prioritas (1: Tinggi, 2: Sedang, 3: Rendah)
    val isCompleted: Boolean = false, // Status penyelesaian (Default: Belum selesai)
    val dueDate: Long? = null,  // Waktu tenggat (Date & Time) disimpan dalam format timestamp (millisecond)
    val createdAt: Long = System.currentTimeMillis() // Waktu pembuatan tugas untuk pencatatan internal
) : Parcelable
