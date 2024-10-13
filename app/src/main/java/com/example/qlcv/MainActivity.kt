package com.example.qlcv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var taskDatabaseHelper: TaskDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tạo hoặc mở cơ sở dữ liệu
        taskDatabaseHelper = TaskDatabaseHelper(this)
        taskDatabaseHelper.createDatabase()

        setContent {
            var taskList by remember { mutableStateOf(taskDatabaseHelper.getAllTasks()) }
            var showAddTaskScreen by remember { mutableStateOf(false) }
            var editingTask by remember { mutableStateOf<Task?>(null) }

            if (showAddTaskScreen) {
                AddTaskScreen(onAddTask = { task ->
                    taskDatabaseHelper.addTask(task)
                    taskList = taskDatabaseHelper.getAllTasks() // Cập nhật lại danh sách
                    showAddTaskScreen = false
                })
            } else if (editingTask != null) {
                EditTaskScreen(
                    task = editingTask!!,
                    onUpdateTask = { updatedTask ->
                        taskDatabaseHelper.updateTask(updatedTask)
                        taskList = taskDatabaseHelper.getAllTasks() // Cập nhật lại danh sách
                        editingTask = null
                    },
                    onDeleteTask = { taskId ->
                        taskDatabaseHelper.deleteTask(taskId)
                        taskList = taskDatabaseHelper.getAllTasks() // Cập nhật lại danh sách
                        editingTask = null
                    }
                )
            } else {
                TaskListScreen(
                    tasks = taskList,
                    onAddClick = { showAddTaskScreen = true },
                    onTaskClick = { task -> editingTask = task }
                )
            }
        }
    }
}

@Composable
fun TaskListScreen(tasks: List<Task>, onAddClick: () -> Unit, onTaskClick: (Task) -> Unit) {
    Column {
        Button(onClick = onAddClick) {
            Text("Thêm Công Việc")
        }
        LazyColumn {
            items(tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTaskClick(task) }
                        .padding(16.dp)
                ) {
                    Text(text = task.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { /* Xóa công việc */ }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Xóa")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskScreen(onAddTask: (Task) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên công việc") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Mô tả công việc") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (name.isNotEmpty()) {
                onAddTask(Task(0, name, description)) // id có thể để 0
                name = ""
                description = ""
            }
        }) {
            Text("Thêm Công Việc")
        }
    }
}

@Composable
fun EditTaskScreen(task: Task, onUpdateTask: (Task) -> Unit, onDeleteTask: (Int) -> Unit) {
    var name by remember { mutableStateOf(task.name) }
    var description by remember { mutableStateOf(task.description) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên công việc") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Mô tả công việc") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = {
                onUpdateTask(task.copy(name = name, description = description))
            }) {
                Text("Cập Nhật")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                onDeleteTask(task.id)
            }) {
                Text("Xóa")
            }
        }
    }
}
