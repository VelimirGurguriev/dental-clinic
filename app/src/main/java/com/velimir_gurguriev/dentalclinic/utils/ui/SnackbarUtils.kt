package com.velimir_gurguriev.dentalclinic.utils.ui

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import com.velimir_gurguriev.dentalclinic.R

object SnackbarUtils {

    fun show(
        rootView: View,
        message: String
    ) {
        val snackbar = Snackbar.make(
            rootView,
            message,
            Snackbar.LENGTH_SHORT
        )

        val snackbarView =
            snackbar.view

        ViewCompat.setBackgroundTintList(
            snackbarView,
            null
        )

        snackbarView.background =
            AppCompatResources.getDrawable(
                rootView.context,
                R.drawable.snackbar_background
            )

        val layoutParams =
            snackbarView.layoutParams
                    as FrameLayout.LayoutParams

        layoutParams.gravity =
            Gravity.TOP or
                    Gravity.CENTER_HORIZONTAL

        val resources =
            rootView.resources

        val horizontalMargin =
            resources.getDimensionPixelSize(
                R.dimen.snackbar_horizontal_margin
            )

        layoutParams.setMargins(
            horizontalMargin,
            resources.getDimensionPixelSize(
                R.dimen.snackbar_top_margin
            ),
            horizontalMargin,
            0
        )

        snackbarView.layoutParams =
            layoutParams

        snackbarView.elevation =
            resources.getDimension(
                R.dimen.snackbar_elevation
            )

        snackbar.show()
    }
}