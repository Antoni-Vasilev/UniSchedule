package eu.nexabg.unischedule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import eu.nexabg.unischedule.model.SSubject;
import eu.nexabg.unischedule.model.SText;
import eu.nexabg.unischedule.model.Schedule;

public class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.ViewHolder> {

    private final Context context;
    private final List<Schedule> data;

    public ScheduleListAdapter(Context context, List<Schedule> data) {
        this.context = context;
        this.data = data;
    }

    @SuppressLint("NonConstantResourceId")
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(viewType, parent, false);
        if (viewType == R.layout.list_schedule_stext)
            return new ViewHolderSText(v);
        else if (viewType == R.layout.list_schedule_ssubject)
            return new ViewHolderSSubject(v);

        return new ViewHolderSText(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Schedule item = data.get(position);

        if (item.getClass() == SText.class && holder.getClass() == ViewHolderSText.class) {
            SText sText = (SText) item;
            holder.data.setText(sText.line.text());
        }

        if (item.getClass() == SSubject.class && holder.getClass() == ViewHolderSSubject.class) {
            SSubject sSubject = (SSubject) item;
            if (sSubject.cols != null) {
                holder.time.setText(sSubject.cols.get(1).text());
                holder.subject.setText(sSubject.cols.get(2).text());
                holder.location.setText(sSubject.cols.get(3).text());
                holder.teacher.setText(sSubject.cols.get(4).text());
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Schedule item = data.get(position);

        if (item.getClass() == SText.class) {
            return R.layout.list_schedule_stext;
        } else if (item.getClass() == SSubject.class) {
            return R.layout.list_schedule_ssubject;
        }

        return 0;
    }

    static abstract class ViewHolder extends RecyclerView.ViewHolder {

        /* SText */
        public TextView data;

        /* SSubject */
        public TextView time, location, subject, teacher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class ViewHolderSText extends ViewHolder {

        public ViewHolderSText(@NonNull View itemView) {
            super(itemView);

            this.data = itemView.findViewById(R.id.data);
        }
    }

    static class ViewHolderSSubject extends ViewHolder {

        public ViewHolderSSubject(@NonNull View itemView) {
            super(itemView);

            this.time = itemView.findViewById(R.id.time);
            this.location = itemView.findViewById(R.id.location);
            this.subject = itemView.findViewById(R.id.subject);
            this.teacher = itemView.findViewById(R.id.teacher);
        }
    }
}
