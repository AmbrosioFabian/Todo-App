package com.example.todoapp.addtasks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.todoapp.R
import com.example.todoapp.addtasks.ui.model.TaskModel
import com.example.todoapp.addtasks.ui.utils.formatDate
import com.example.todoapp.addtasks.ui.utils.formatTime
import com.example.todoapp.ui.components.DatePickerDialogComponent
import com.example.todoapp.ui.components.TextFieldComponent
import com.example.todoapp.ui.components.TextFieldWithButtonComponent
import com.example.todoapp.ui.components.TimePickerDialogComponent
import com.example.todoapp.ui.theme.Typography
import org.threeten.bp.LocalDate

@Composable
fun EditTaskScreen(taskViewModel: TaskViewModel, id: Int) {
    // Observe the StateFlow changes for the task UI state and other states
    val taskUiState by taskViewModel.taskFlowUiState.collectAsState()
    val showDatePicker by taskViewModel.showDatePicker.collectAsState()
    val showTimePicker by taskViewModel.showTimePicker.collectAsState()

    // Launch effect to load the task by id when the composable is first launched or id changes
    LaunchedEffect(id) {
        taskViewModel.getTaskById(id)
    }

    when (taskUiState) {
        is TaskUiState.Loading -> {
            CircularProgressIndicator()
        }

        is TaskUiState.Error -> {
            Text(text = "Error al cargar la tarea.")
        }

        is TaskUiState.Success -> {
            Container(
                showDatePicker,
                showTimePicker,
                taskViewModel,
                (taskUiState as TaskUiState.Success).task
            )
        }

        is TaskUiState.Empty -> {
            Text(text = "Tarea no encontrada.")
        }
    }
}

@Composable
fun Container(
    showDatePicker: Boolean,
    showTimePicker: Boolean,
    taskViewModel: TaskViewModel,
    task: TaskModel,
) {
    var taskText by remember { mutableStateOf(task.task) }
    var taskDetail by remember { mutableStateOf(task.details ?: "") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.Top) {
            // TextField for task title
            TextField(
                value = taskText,
                textStyle = Typography.bodyLarge,
                onValueChange = {
                    taskText = it
                    taskViewModel.updateTask(task.copy(task = it))
                },
                placeholder = { Text(text = "Editar tarea", style = Typography.bodyLarge) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedContainerColor = colorScheme.secondaryContainer,
                    unfocusedContainerColor = colorScheme.secondaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done // Para que el teclado se cierre
                )
            )

            // TextField for task details
            TextFieldComponent(
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notes),
                        contentDescription = "Icono para agregar detalles",
                        modifier = Modifier.padding(end = 6.dp)
                    )
                },
                value = taskDetail,
                textStyle = Typography.bodyMedium,
                onValueChange = {
                    taskDetail = it
                    taskViewModel.updateTask(task.copy(details = it))
                },
                placeholder = "Agregar detalles",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.secondaryContainer)
                    .padding(start = 8.dp, end = 12.5.dp, top = 7.dp, bottom = 7.dp)
            )

            // Button to show date picker
            TextButton(
                onClick = { taskViewModel.onShowDateDialogClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.secondaryContainer)
                    .padding(start = 3.5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_access_time),
                        contentDescription = "Icono para agregar fecha/hora",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    if (task.startDate != null) {
                        val formattedDate = formatDate(task.startDate)
                        val formattedTime = task.time?.let { formatTime(it) }
                        val displayText = "$formattedDate${formattedTime?.let { ", $it" } ?: ""}"

                        TextFieldWithButtonComponent(
                            text = displayText,
                            onIconClick = {
                                taskViewModel.resetTaskDateTime(task.id)
                                taskViewModel.resetTemporaryDateTime()
                            }
                        )
                    } else {
                        Text(
                            text = "Agregar Fecha/Hora",
                            color = colorScheme.onBackground,
                            style = Typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Mostrar DateTimePickerDialog si `showDatePicker` es verdadero
        if (showDatePicker) {
            DatePickerDialogComponent(
                taskViewModel = taskViewModel,
                onDismiss = { taskViewModel.onHideDatePicker() },
                onConfirm = {
                    val updatedTask = task.copy(
                        startDate = taskViewModel.temporaryDate.value ?: LocalDate.now(),
                        time = taskViewModel.temporaryTime.value
                    )
                    taskViewModel.updateTask(updatedTask)
                    taskViewModel.resetTemporaryDateTime()
                }
            )
        }

        // Mostrar TimePickerDialog si `showTimePicker` es verdadero
        if (showTimePicker) {
            TimePickerDialogComponent(
                taskViewModel = taskViewModel,
                onDismiss = { taskViewModel.onHideTimePicker() }
            )
        }
    }
}