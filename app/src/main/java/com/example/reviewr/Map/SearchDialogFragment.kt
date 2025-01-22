package com.example.reviewr.Map

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.reviewr.R


class SearchDialogFragment(private val onSearchApplied: (Map<String, String>) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.search_dialog_fragment, null)

        val statusSpinner = view.findViewById<Spinner>(R.id.statusSpinner)
        val categorySpinner = view.findViewById<Spinner>(R.id.categorySpinner)
        val applyButton = view.findViewById<Button>(R.id.applyFiltersButton)
        val showAllButton = view.findViewById<Button>(R.id.showAllButton)
        val goBackButton = view.findViewById<Button>(R.id.goBackButton)

        applyButton.setOnClickListener {
            val filters = mapOf(
                "status" to statusSpinner.selectedItem.toString(),
                "category" to categorySpinner.selectedItem.toString(),
                "action" to "applyFilters"
            )
            onSearchApplied(filters)
            dismiss()
        }

        showAllButton.setOnClickListener {
            val filters = mapOf("action" to "showAll")
            onSearchApplied(filters) // Trigger the callback for showing all reviews
            dismiss()
        }

        goBackButton.setOnClickListener {
            dismiss() // Simply close the dialog
        }

        builder.setView(view)
        return builder.create()
    }
}





