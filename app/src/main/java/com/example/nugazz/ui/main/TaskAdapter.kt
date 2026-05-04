package com.example.nugazz.ui.main

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nugazz.data.local.Task
import com.example.nugazz.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

// Adapter ini bertugas mengubah data List<Task> menjadi tampilan kartu-kartu di layar
// Menggunakan ListAdapter agar update data lebih efisien dan memiliki animasi otomatis
class TaskAdapter(
    private val onCheckedChange: (Task, Boolean) -> Unit,
    private val onItemClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback) {

    // Format tanggal dan jam untuk ditampilkan di kartu tugas
    private val dateTimeFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        // Menghubungkan layout item_task.xml dengan adapter melalui ViewBinding
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        // Mengisi data tugas ke dalam ViewHolder berdasarkan posisinya
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.apply {
                // Menampilkan teks judul dan deskripsi
                tvTitle.text = task.title
                tvDescription.text = task.description
                
                // MENCEGAH BUG CENTANG GANDA: Reset listener sebelum memasang status check
                cbCompleted.setOnCheckedChangeListener(null)
                cbCompleted.isChecked = task.isCompleted

                // Styling UI berdasarkan status penyelesaian
                if (task.isCompleted) {
                    // Memberikan efek coret pada teks jika tugas selesai
                    tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    root.alpha = 0.6f // Membuat kartu terlihat sedikit transparan
                } else {
                    // Menghapus efek coret jika tugas belum selesai
                    tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    root.alpha = 1.0f
                }

                // Memberikan warna indikator di sisi kiri kartu berdasarkan prioritas
                val color = when (task.priority) {
                    1 -> Color.RED    // Tinggi
                    2 -> Color.YELLOW // Sedang
                    3 -> Color.GREEN  // Rendah
                    else -> Color.GRAY
                }
                priorityIndicator.setBackgroundColor(color)

                // Menampilkan tenggat waktu (Deadline)
                task.dueDate?.let {
                    tvDueDate.text = dateTimeFormat.format(Date(it))
                    // Beri warna merah jika sudah melewati waktu tenggat (Overdue)
                    if (it < System.currentTimeMillis() && !task.isCompleted) {
                        tvDueDate.setTextColor(Color.RED)
                    } else {
                        tvDueDate.setTextColor(Color.GRAY)
                    }
                } ?: run {
                    tvDueDate.text = "No deadline"
                }

                // Menangani klik pada CheckBox untuk menandai selesai/belum
                cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                    // Hanya panggil update jika statusnya benar-benar berubah
                    if (task.isCompleted != isChecked) {
                        onCheckedChange(task, isChecked)
                    }
                }

                // Menangani klik pada kartu untuk melihat/edit detail
                root.setOnClickListener {
                    onItemClick(task)
                }
            }
        }
    }

    // DiffCallback membantu RecyclerView mendeteksi perubahan data secara cerdas (hanya update item yang berubah)
    companion object DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
