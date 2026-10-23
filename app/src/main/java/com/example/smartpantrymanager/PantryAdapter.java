package com.example.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.logic.Format;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Shows the pantry items from the database as a scrolling list of cards. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** The screen that owns the list reacts to taps. */
    public interface Listener {
        void onItemClick(PantryItem item);

        void onDeleteClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final List<PantryItem> allItems = new ArrayList<>();
    private final Listener listener;
    private String searchQuery = "";

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems) {
        allItems.clear();
        allItems.addAll(newItems);
        applyFilter();
    }

    /** Filters the database-backed pantry list by ingredient name. */
    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT);
        applyFilter();
    }

    private void applyFilter() {
        items.clear();
        if (searchQuery.isEmpty()) {
            items.addAll(allItems);
        } else {
            for (PantryItem item : allItems) {
                String name = item.name == null ? "" : item.name.toLowerCase(java.util.Locale.ROOT);
                if (name.contains(searchQuery)) {
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PantryItem item = items.get(position);

        holder.name.setText(Format.capitalise(item.name));
        holder.quantity.setText(Format.qty(item.quantity) + " " + item.unit);
        bindExpiry(holder, item);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.delete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Shows the expiry date, in red or orange when it is close. */
    private void bindExpiry(ViewHolder holder, PantryItem item) {
        Context context = holder.itemView.getContext();

        if (item.expiryDate == null) {
            holder.expiry.setText(R.string.no_expiry);
            holder.expiry.setTextColor(holder.defaultExpiryColor);
            return;
        }

        int days = Format.daysUntil(item.expiryDate, System.currentTimeMillis());
        String text;
        if (days < -1) {
            text = context.getString(R.string.expired_ago, -days);
        } else if (days == -1) {
            text = context.getString(R.string.expired_yesterday);
        } else if (days == 0) {
            text = context.getString(R.string.expires_today);
        } else if (days == 1) {
            text = context.getString(R.string.expires_tomorrow);
        } else if (days <= 3) {
            text = context.getString(R.string.expires_in, days);
        } else {
            String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(item.expiryDate));
            text = context.getString(R.string.expires_on, date);
        }
        holder.expiry.setText(text);

        if (days <= 0) {
            holder.expiry.setTextColor(ContextCompat.getColor(context, R.color.red_700));
        } else if (days <= 3) {
            holder.expiry.setTextColor(ContextCompat.getColor(context, R.color.orange_700));
        } else {
            holder.expiry.setTextColor(holder.defaultExpiryColor);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final ImageButton delete;
        final int defaultExpiryColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            quantity = itemView.findViewById(R.id.itemQuantity);
            expiry = itemView.findViewById(R.id.itemExpiry);
            delete = itemView.findViewById(R.id.deleteButton);
            defaultExpiryColor = expiry.getCurrentTextColor();
        }
    }
}
