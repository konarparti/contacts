package com.example.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    val result = loadContacts()

                    setContent {
                        ContactListScreen(contacts = result)
                    }

                    Toast.makeText(this, "Найден(о) ${result.size} контакт(ов)", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    setContent {
                        DefaultPreview()
                    }
                    Toast.makeText(this, "Нет доступа к контактам", Toast.LENGTH_SHORT).show()
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                val result = loadContacts()

                setContent {
                    ContactListScreen(contacts = result)
                }

                Toast.makeText(this, "Найден(о) ${result.size} контакт(ов)", Toast.LENGTH_SHORT)
                    .show()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun loadContacts(): List<Contact> {
        val contacts = mutableStateListOf<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor == null) return emptyList()
        val builder = ArrayList<Contact>()
        while (cursor.moveToNext()) {
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    ?: "N/A"
            val phoneNumber =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    ?: "N/A"

            builder.add(Contact(name, phoneNumber))
        }
        return builder
    }
}

data class Contact(val name: String, val number: String)

@Composable
fun ContactListScreen(contacts: List<Contact>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (contacts.isEmpty()) {
            Text(text = "Контакты не найдены")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(contacts) { contact ->
                    ContactItem(contact)
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${contact.number}")
                }
                context.startActivity(intent)
            }
    ) {
        Column(modifier = Modifier.weight(9F)) {
            Text(
                text = contact.name,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = contact.number,
                fontSize = 18.sp
            )
        }
        Image(
            painter = painterResource(id = android.R.drawable.ic_menu_call),
            contentDescription = "call img",
            modifier = Modifier
                .size(36.dp)
                .padding(end = 8.dp)
                .weight(1F)
        )


    }


}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ContactListScreen(contacts = emptyList())
}