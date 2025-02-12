package eu.nexabg.unischedule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import eu.nexabg.unischedule.model.SDay;
import eu.nexabg.unischedule.model.SSubject;
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
        if (viewType == R.layout.list_schedule_sday)
            return new ViewHolderSDay(v);
        else if (viewType == R.layout.list_schedule_ssubject)
            return new ViewHolderSSubject(v);

        return new ViewHolderSDay(v);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SimpleDateFormat"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Schedule item = data.get(position);

        if (item.getClass() == SDay.class && holder.getClass() == ViewHolderSDay.class) {
            SDay sDay = (SDay) item;
            String formatedDate = new SimpleDateFormat("EEEE, yyyy-MM-dd").format(sDay.date);
            formatedDate = formatedDate.substring(0, 1).toUpperCase() + formatedDate.substring(1);
            holder.data.setText(formatedDate);

            Date date = new Date();
            Date now = new Date(date.getYear(), date.getMonth(), date.getDate());
            if (sDay.date.compareTo(now) == 0) {
                holder.data.setBackground(context.getDrawable(R.drawable.list_schedule_sday_bg));
            } else {
                holder.data.setBackground(context.getDrawable(android.R.color.transparent));
            }
        }

        if (item.getClass() == SSubject.class && holder.getClass() == ViewHolderSSubject.class) {
            SSubject sSubject = (SSubject) item;
            holder.time.setText(sSubject.time);
            holder.subject.setText(sSubject.subject);
            holder.location.setText(sSubject.location);
            holder.teacher.setText(sSubject.teacher);

            SDay sDay = null;
            for (int i = position; i >= 0; i--) {
                if (data.get(i).getClass() == SDay.class) {
                    sDay = (SDay) data.get(i);
                    break;
                }
            }

            if (sDay != null) {
                Date now = new Date();
                String[] hours = sSubject.time.split("[-:]");
                Date startTime = new Date(sDay.date.getYear(), sDay.date.getMonth(), sDay.date.getDate(), Integer.parseInt(hours[0]), Integer.parseInt(hours[1]));
                Date endTime = new Date(sDay.date.getYear(), sDay.date.getMonth(), sDay.date.getDate(), Integer.parseInt(hours[2]), Integer.parseInt(hours[3]));
                if (endTime.before(now)) {
                    holder.background.setBackground(context.getDrawable(R.drawable.list_schedule_ssubject_old_bg));
                } else if (startTime.before(now) && endTime.after(now)) {
                    holder.background.setBackground(context.getDrawable(R.drawable.list_schedule_ssubject_now_bg));
                } else {
                    holder.background.setBackground(context.getDrawable(R.drawable.list_schedule_ssubject_upcoming_bg));
                }
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

        if (item.getClass() == SDay.class) {
            return R.layout.list_schedule_sday;
        } else if (item.getClass() == SSubject.class) {
            return R.layout.list_schedule_ssubject;
        }

        return 0;
    }

    static abstract class ViewHolder extends RecyclerView.ViewHolder {

        /* SDay */
        public TextView data;

        /* SSubject */
        public TextView time, location, subject, teacher;
        public LinearLayout background;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class ViewHolderSDay extends ViewHolder {

        public ViewHolderSDay(@NonNull View itemView) {
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
            this.background = itemView.findViewById(R.id.background);
        }
    }
}
