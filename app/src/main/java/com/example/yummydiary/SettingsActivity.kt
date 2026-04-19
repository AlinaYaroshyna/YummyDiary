package com.example.yummydiary

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import android.widget.Toast

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setToolbarTitle("Ustawienia")

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            findPreference<Preference>("pref_edit_categories")?.setOnPreferenceClickListener {
                startActivity(android.content.Intent(context, EditCategoriesActivity::class.java))
                true
            }

            findPreference<Preference>("pref_manage_photos")?.setOnPreferenceClickListener {
                Toast.makeText(context, "Zarządzanie zdjęciami wkrótce!", Toast.LENGTH_SHORT).show()
                true
            }

            findPreference<Preference>("pref_export_data")?.setOnPreferenceClickListener {
                Toast.makeText(context, "Eksport danych wkrótce!", Toast.LENGTH_SHORT).show()
                true
            }

            findPreference<Preference>("pref_import_data")?.setOnPreferenceClickListener {
                Toast.makeText(context, "Import danych wkrótce!", Toast.LENGTH_SHORT).show()
                true
            }
        }
    }
}