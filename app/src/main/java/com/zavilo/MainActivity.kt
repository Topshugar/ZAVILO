package com.zavilo

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this)
        textView.text = "Zavilo AI - Licensed Device\n\nStatus: Active"
        textView.textSize = 20f
        textView.setPadding(50, 200, 50, 50)
        
        setContentView(textView)
        
        // Check license on start
        val licenseManager = LicenseManager(this)
        if (!licenseManager.isLicensed()) {
            textView.text = "Zavilo - LICENSE REQUIRED\n\nPlease activate."
        }
    }
}
