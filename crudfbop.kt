package com.zoya.crud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirestoreCrudScreen()
        }
    }
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = ""
)

@Composable
fun FirestoreCrudScreen() {

    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<User>()) }

    // READ
    LaunchedEffect(Unit) {
        db.collection("users")
            .addSnapshotListener { value, _ ->
                users = value?.documents?.map {
                    User(
                        id = it.id,
                        name = it.getString("name") ?: "",
                        email = it.getString("email") ?: ""
                    )
                } ?: emptyList()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // CREATE
        Button(
            onClick = {
                val user = hashMapOf(
                    "name" to name,
                    "email" to email
                )

                db.collection("users").add(user)
                name = ""
                email = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add User")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(users) { user ->
                UserItem(
                    user = user,
                    onUpdate = {
                        // UPDATE
                        db.collection("users")
                            .document(user.id)
                            .update("name", "Updated Name")
                    },
                    onDelete = {
                        // DELETE
                        db.collection("users")
                            .document(user.id)
                            .delete()
                    }
                )
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = user.name)
            Text(text = user.email)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onUpdate) {
                    Text("Update")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}
