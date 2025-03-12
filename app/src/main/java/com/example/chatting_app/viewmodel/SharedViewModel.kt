package com.example.chatting_app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatting_app.dataclass.Contact

class SharedViewModel : ViewModel() {

    // ✅ Backing property for contacts to prevent direct modification
    private val _contacts = MutableLiveData<List<Contact>>()

    // ✅ Function to update the contact list
    fun setContacts(contactList: List<Contact>) {
        _contacts.value = contactList
    }
}
