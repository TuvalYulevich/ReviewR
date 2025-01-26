package com.example.reviewr.Map

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.reviewr.R

class FilterDialogFragment(private val onFilterAction: (String, Map<String, Any>?) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.filter_dialog, null)

        val statusSpinner = view.findViewById<Spinner>(R.id.statusSpinner)
        val categorySpinner = view.findViewById<Spinner>(R.id.categorySpinner)
        val within500MetersCheckbox = view.findViewById<CheckBox>(R.id.within500MetersCheckbox)
        val applyFiltersButton = view.findViewById<Button>(R.id.applyButton)
        val showAllButton = view.findViewById<Button>(R.id.showAllButton)
        val hideAllButton = view.findViewById<Button>(R.id.hideAllButton)
        val goBackButton = view.findViewById<Button>(R.id.goBackButton)

        // Apply Filters button logic
        applyFiltersButton.setOnClickListener {
            val filters = mutableMapOf<String, Any>(
                "status" to statusSpinner.selectedItem.toString(),
                "category" to categorySpinner.selectedItem.toString(),
                "within500Meters" to within500MetersCheckbox.isChecked
            )
            onFilterAction("applyFilters", filters)
            dismiss()
        }

        // Show All button logic
        showAllButton.setOnClickListener {
            val filters = mapOf("within500Meters" to within500MetersCheckbox.isChecked)
            onFilterAction("showAll", filters)
            dismiss()
        }

        // Hide All button logic
        hideAllButton.setOnClickListener {
            val filters = mapOf("within500Meters" to within500MetersCheckbox.isChecked)
            onFilterAction("hideAll", filters)
            dismiss()
        }

        // Go Back button logic
        goBackButton.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }
}

