/*
 * Copyright (C) 2026 The Chrona Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.febricahyaa.chrona.worldclock

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.febricahyaa.chrona.BaseActivity
import com.febricahyaa.chrona.R
import com.febricahyaa.chrona.data.City
import com.febricahyaa.chrona.data.DataModel

import java.util.Calendar
import java.util.Locale

/**
 * Searches the cities that can be added to the world clock. Nothing is listed until the user
 * types; each match shows its local time, and tapping it adds the city and closes the search.
 * Cities are removed by swiping them away on the world clock tab.
 */
class CitySelectionActivity : BaseActivity() {
    private lateinit var mSearchField: EditText
    private lateinit var mClearButton: View
    private lateinit var mEmptyView: View
    private lateinit var mEmptyText: TextView
    private val mAdapter = CityAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cities_activity)

        mSearchField = findViewById(R.id.city_search)
        mClearButton = findViewById(R.id.city_search_clear)
        mEmptyView = findViewById(R.id.city_search_empty)
        mEmptyText = findViewById(R.id.city_search_empty_text)

        val list: RecyclerView = findViewById(R.id.cities_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = mAdapter

        findViewById<View>(R.id.city_search_back).setOnClickListener { finish() }
        mClearButton.setOnClickListener { mSearchField.setText("") }
        mSearchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
        })
        mSearchField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        filter(mSearchField.text.toString())
        mSearchField.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        // The 12/24 hour setting or the selected cities may have changed meanwhile.
        filter(mSearchField.text.toString())
    }

    private fun filter(queryText: String) {
        val query = City.removeSpecialCharacters(queryText.trim().uppercase())
        mClearButton.visibility = if (queryText.isEmpty()) View.GONE else View.VISIBLE
        val matches = if (query.isEmpty()) {
            emptyList()
        } else {
            DataModel.dataModel.unselectedCities.filter { it.matches(query) }
        }
        mAdapter.setCities(matches, DateFormat.is24HourFormat(this))
        mEmptyView.visibility = if (matches.isEmpty()) View.VISIBLE else View.GONE
        mEmptyText.setText(if (query.isEmpty()) {
            R.string.search_city_hint
        } else {
            R.string.search_city_no_results
        })
    }

    private fun addCity(city: City) {
        DataModel.dataModel.selectedCities = DataModel.dataModel.selectedCities + city
        hideKeyboard()
        finish()
    }

    private fun hideKeyboard() {
        getSystemService<InputMethodManager>()
                ?.hideSoftInputFromWindow(mSearchField.windowToken, 0)
    }

    private inner class CityAdapter : RecyclerView.Adapter<CityViewHolder>() {
        private var mCities: List<City> = emptyList()
        private var mIs24Hour = false
        private val mCalendar = Calendar.getInstance()
        private val mPattern24 = DateFormat.getBestDateTimePattern(Locale.getDefault(), "Hm")
        private val mPattern12 = DateFormat.getBestDateTimePattern(Locale.getDefault(), "hma")

        fun setCities(cities: List<City>, is24Hour: Boolean) {
            val old = mCities
            mCities = cities
            mIs24Hour = is24Hour
            if (old.isNotEmpty()) {
                notifyItemRangeRemoved(0, old.size)
            }
            if (cities.isNotEmpty()) {
                notifyItemRangeInserted(0, cities.size)
            }
        }

        override fun getItemCount(): Int = mCities.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.city_list_item, parent, false)
            return CityViewHolder(view)
        }

        override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
            val city = mCities[position]
            holder.name.text = city.name
            mCalendar.timeZone = city.timeZone
            mCalendar.timeInMillis = System.currentTimeMillis()
            holder.time.text = DateFormat.format(if (mIs24Hour) mPattern24 else mPattern12, mCalendar)
            holder.itemView.setOnClickListener { addCity(city) }
        }
    }

    private class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.city_name)
        val time: TextView = itemView.findViewById(R.id.city_time)
    }
}
