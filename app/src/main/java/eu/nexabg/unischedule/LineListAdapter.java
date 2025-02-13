package eu.nexabg.unischedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LineListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> items;

    public LineListAdapter(@NotNull Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_line, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textViewItem);
        textView.setText(items.get(position));

        LinearLayout background = convertView.findViewById(R.id.background);
        if (position == ScheduleActivity.selectedItem) {
            background.setBackgroundColor(context.getColor(R.color.now));
        } else {
            background.setBackgroundColor(context.getColor(R.color.white));
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
