package com.example.chatting_app.viewmodel

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatting_app.dataclass.Contact

class SharedViewModel() : ViewModel(), Parcelable {

    private val _contacts = MutableLiveData<List<Contact>>()

    constructor(parcel: Parcel) : this() {
    }

    fun setContacts(contactList: List<Contact>) {
        _contacts.value = contactList
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SharedViewModel> {
        override fun createFromParcel(parcel: Parcel): SharedViewModel {
            return SharedViewModel(parcel)
        }

        override fun newArray(size: Int): Array<SharedViewModel?> {
            return arrayOfNulls(size)
        }
    }

}


