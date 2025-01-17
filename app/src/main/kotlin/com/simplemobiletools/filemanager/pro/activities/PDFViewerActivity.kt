package com.simplemobiletools.filemanager.pro.activities

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsetsController
import android.view.WindowManager
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.REAL_FILE_PATH
import com.simplemobiletools.commons.helpers.isPiePlus
import com.simplemobiletools.commons.helpers.isRPlus
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.extensions.hideSystemUI
import com.simplemobiletools.filemanager.pro.extensions.showSystemUI
import com.simplemobiletools.filemanager.pro.helpers.PdfDocumentAdapter
import kotlinx.android.synthetic.main.activity_pdf_viewer.*

class PDFViewerActivity : SimpleActivity() {
    private var realFilePath = ""
    private var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        if (checkAppSideloading()) {
            return
        }

        window.decorView.setBackgroundColor(getProperBackgroundColor())
        top_shadow.layoutParams.height = statusBarHeight + actionBarHeight
        checkNotchSupport()

        if (intent.extras?.containsKey(REAL_FILE_PATH) == true) {
            realFilePath = intent.extras?.get(REAL_FILE_PATH)?.toString() ?: ""
            supportActionBar?.title = realFilePath.getFilenameFromPath()
        }

        checkIntent()
        if (isRPlus()) {
            window.insetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.statusBarColor = Color.TRANSPARENT
        setTranslucentNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_pdf_viewer, menu)
        menu.apply {
            findItem(R.id.menu_print).isVisible = realFilePath.isNotEmpty()
        }

        updateMenuItemColors(menu, forceWhiteIcons = true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_print -> printText()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun checkIntent() {
        val uri = intent.data
        if (uri == null) {
            finish()
            return
        }

        val primaryColor = getProperPrimaryColor()
        pdf_viewer.setBackgroundColor(getProperBackgroundColor())
        pdf_viewer.fromUri(uri)
            .scrollHandle(DefaultScrollHandle(this, primaryColor.getContrastColor(), primaryColor))
            .spacing(15)
            .onTap { toggleFullScreen() }
            .onError {
                showErrorToast(it.localizedMessage.toString())
                finish()
            }
            .load()

        showSystemUI(true)

        val filename = getFilenameFromUri(uri)
        if (filename.isNotEmpty()) {
            supportActionBar?.title = filename
        }
    }

    private fun printText() {
        val adapter = PdfDocumentAdapter(this, realFilePath)

        (getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.apply {
            print(realFilePath.getFilenameFromPath(), adapter, PrintAttributes.Builder().build())
        }
    }

    private fun toggleFullScreen(): Boolean {
        isFullScreen = !isFullScreen
        val newAlpha: Float
        if (isFullScreen) {
            newAlpha = 0f
            hideSystemUI(true)
        } else {
            newAlpha = 1f
            showSystemUI(true)
        }

        top_shadow.animate().alpha(newAlpha).start()

        // return false to also toggle scroll handle
        return true
    }

    private fun checkNotchSupport() {
        if (isPiePlus()) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
    }
}
