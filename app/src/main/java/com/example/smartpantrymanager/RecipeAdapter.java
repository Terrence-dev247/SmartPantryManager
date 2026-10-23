package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a list that mixes two kinds of rows: section headers
 * ("You can cook these now") and recipe cards.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_RECIPE = 1;

    /** One row of the list: either a header or a recipe. */
    public static final class Row {
        final boolean isHeader;
        final String title;
        final String subtitle;
        final int recipeId;

        private Row(boolean isHeader, String title, String subtitle, int recipeId) {
            this.isHeader = isHeader;
            this.title = title;
            this.subtitle = subtitle;
            this.recipeId = recipeId;
        }

        public static Row header(String title) {
            return new Row(true, title, null, -1);
        }

        public static Row recipe(int recipeId, String name, String subtitle) {
            return new Row(false, name, subtitle, recipeId);
        }
    }

    public interface Listener {
        void onRecipeClick(int recipeId);
    }

    private final List<Row> rows = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).isHeader ? TYPE_HEADER : TYPE_RECIPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new RecipeHolder(inflater.inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Row row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).title.setText(row.title);
        } else {
            RecipeHolder recipeHolder = (RecipeHolder) holder;
            recipeHolder.name.setText(row.title);
            recipeHolder.subtitle.setText(row.subtitle);
            recipeHolder.itemView.setOnClickListener(v -> listener.onRecipeClick(row.recipeId));
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView title;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitle);
        }
    }

    static class RecipeHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView subtitle;

        RecipeHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipeName);
            subtitle = itemView.findViewById(R.id.recipeSubtitle);
        }
    }
}
