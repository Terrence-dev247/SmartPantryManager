package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.DbExecutor;
import com.example.smartpantrymanager.data.PantryDao;
import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.logic.Format;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Add a new ingredient, or edit / delete an existing one.
 * The Intent extra EXTRA_ITEM_ID decides the mode: missing = add, present = edit.
 */
public class EditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    private static final int NEW_ITEM = -1;
    private static final String STATE_EXPIRY = "state_expiry";
    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp", "cup"};

    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private Spinner unitSpinner;
    private TextView expiryText;
    private MaterialButton saveButton;

    private int itemId = NEW_ITEM;
    private Long expiryMillis = null; // null = no expiry date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ingredient);

        itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, NEW_ITEM);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(itemId == NEW_ITEM ? R.string.title_add : R.string.title_edit);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationContentDescription(R.string.back);
        toolbar.setNavigationOnClickListener(v -> finish());

        nameLayout = findViewById(R.id.nameLayout);
        quantityLayout = findViewById(R.id.quantityLayout);
        nameInput = findViewById(R.id.nameInput);
        quantityInput = findViewById(R.id.quantityInput);
        unitSpinner = findViewById(R.id.unitSpinner);
        expiryText = findViewById(R.id.expiryText);
        saveButton = findViewById(R.id.saveButton);
        MaterialButton deleteButton = findViewById(R.id.deleteButton);

        ArrayAdapter<String> unitAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_EXPIRY)) {
            expiryMillis = savedInstanceState.getLong(STATE_EXPIRY);
        }
        showExpiry();

        findViewById(R.id.pickDateButton).setOnClickListener(v -> pickDate());
        findViewById(R.id.clearDateButton).setOnClickListener(v -> {
            expiryMillis = null;
            showExpiry();
        });
        saveButton.setOnClickListener(v -> save());
        quantityInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                save();
                return true;
            }
            return false;
        });

        if (itemId != NEW_ITEM) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> confirmDelete());
            if (savedInstanceState == null) {
                loadItem();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (expiryMillis != null) {
            outState.putLong(STATE_EXPIRY, expiryMillis);
        }
    }

    // ---------------------------------------------------------------- loading

    /** Edit mode: read the item from the database and fill in the form. */
    private void loadItem() {
        final Context appContext = getApplicationContext();
        DbExecutor.run(() -> {
            final PantryItem item = AppDatabase.get(appContext).pantryDao().getById(itemId);
            runOnUiThread(() -> {
                if (item == null) {
                    finish(); // it was deleted in the meantime
                    return;
                }
                nameInput.setText(Format.capitalise(item.name));
                quantityInput.setText(Format.qty(item.quantity));
                unitSpinner.setSelection(indexOfUnit(item.unit));
                expiryMillis = item.expiryDate;
                showExpiry();
            });
        });
    }

    private int indexOfUnit(String unit) {
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equalsIgnoreCase(unit)) {
                return i;
            }
        }
        return 0;
    }

    // ----------------------------------------------------------- expiry date

    private void pickDate() {
        Calendar calendar = Calendar.getInstance();
        if (expiryMillis != null) {
            calendar.setTimeInMillis(expiryMillis);
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.clear();
            chosen.set(year, month, day, 0, 0, 0);
            expiryMillis = chosen.getTimeInMillis();
            showExpiry();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showExpiry() {
        if (expiryMillis == null) {
            expiryText.setText(R.string.no_expiry);
        } else {
            expiryText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(expiryMillis)));
        }
    }

    // ---------------------------------------------------------- validation

    /** Returns a string resource id describing the problem, or 0 if the name is fine. */
    private int validateName(String name) {
        if (name.isEmpty()) {
            return R.string.error_name_empty;
        }
        if (name.length() < 2) {
            return R.string.error_name_short;
        }
        if (name.length() > 40) {
            return R.string.error_name_long;
        }
        // Must start with a letter; then letters, digits, spaces and a few symbols.
        if (!name.matches("^\\p{L}[\\p{L}\\p{N} '.&-]*$")) {
            return R.string.error_name_invalid;
        }
        return 0;
    }

    /** Returns a string resource id describing the problem, or 0 if the quantity is fine. */
    private int validateQuantity(String raw) {
        if (raw.isEmpty()) {
            return R.string.error_qty_empty;
        }
        double value;
        try {
            value = Double.parseDouble(raw.replace(',', '.')); // allow "0,5" as well as "0.5"
        } catch (NumberFormatException e) {
            return R.string.error_qty_invalid;
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return R.string.error_qty_invalid;
        }
        if (value <= 0) {
            return R.string.error_qty_positive;
        }
        if (value > 100000) {
            return R.string.error_qty_large;
        }
        return 0;
    }

    // -------------------------------------------------------------- saving

    private void save() {
        String name = String.valueOf(nameInput.getText()).trim().replaceAll("\\s+", " ");
        String quantityText = String.valueOf(quantityInput.getText()).trim();

        nameLayout.setError(null);
        quantityLayout.setError(null);

        int nameError = validateName(name);
        int quantityError = validateQuantity(quantityText);
        if (nameError != 0) {
            nameLayout.setError(getString(nameError));
        }
        if (quantityError != 0) {
            quantityLayout.setError(getString(quantityError));
        }
        if (nameError != 0 || quantityError != 0) {
            return; // stay on the form until everything is valid
        }

        final PantryItem item = new PantryItem();
        item.name = name.toLowerCase(Locale.ROOT);
        item.quantity = Double.parseDouble(quantityText.replace(',', '.'));
        item.unit = String.valueOf(unitSpinner.getSelectedItem());
        item.expiryDate = expiryMillis;

        saveButton.setEnabled(false); // avoid saving twice if the button is tapped again
        final Context appContext = getApplicationContext();
        DbExecutor.run(() -> {
            PantryDao dao = AppDatabase.get(appContext).pantryDao();
            if (itemId == NEW_ITEM) {
                dao.insert(item);      // CREATE
            } else {
                item.id = itemId;
                dao.update(item);      // UPDATE
            }
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    // ------------------------------------------------------------ deleting

    private void confirmDelete() {
        String name = Format.capitalise(String.valueOf(nameInput.getText()).trim());
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, name))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteItem())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteItem() {
        final Context appContext = getApplicationContext();
        DbExecutor.run(() -> {
            PantryItem item = new PantryItem();
            item.id = itemId; // Room deletes by primary key
            AppDatabase.get(appContext).pantryDao().delete(item); // DELETE
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
