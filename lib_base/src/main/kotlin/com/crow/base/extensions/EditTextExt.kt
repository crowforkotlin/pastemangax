package com.crow.base.view

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/view
 * @Time: 2022/6/19 19:01
 * @Author: BarryAllen
 * @Description: EditText Ext
 * @formatter:on
 **************************/

/* Extension function to simplify setting an afterTextChanged action to EditText components. */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}