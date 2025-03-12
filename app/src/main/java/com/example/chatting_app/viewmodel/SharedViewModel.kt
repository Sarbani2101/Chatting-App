package com.example.chatting_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatting_app.dataclass.Contact

class SharedViewModel : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    fun setContacts(contactList: List<Contact>) {
        _contacts.value = contactList
    }

    fun removeContact(uid: String) {
        _contacts.value = _contacts.value?.filterNot { it.uid == uid }
    }
}


