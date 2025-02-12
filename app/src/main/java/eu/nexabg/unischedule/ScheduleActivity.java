package eu.nexabg.unischedule;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nexabg.unischedule.model.SSubject;
import eu.nexabg.unischedule.model.SText;
import eu.nexabg.unischedule.model.Schedule;

public class ScheduleActivity extends AppCompatActivity {

    private RecyclerView scheduleList;

    private List<Schedule> schedule = new ArrayList<>();
    private ScheduleListAdapter scheduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setupList();

        Document body = readPage();

        if (body != null) {
            Elements rows = body.select("#content-full").select("table").select("tbody").select("tr");
            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 2) {
                    Pattern pattern = Pattern.compile("([а-яА-Я]*)([, ]*)([0-9]*)-([0-9]*)-([0-9]*)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(cols.get(0).text());
                    if (matcher.find()) {
                        schedule.add(new SText(row));
                    }
                } else {
                    if (cols.stream().filter(it -> !it.text().isBlank()).toArray().length > 1)
                        schedule.add(new SSubject(row, cols));
                }
            }
            schedule.removeIf(it -> {
                try {
                    new Scanner(it.line.text()).nextInt();
                    return false;
                } catch (Exception e) {
                    Pattern pattern = Pattern.compile("([а-яА-Я]*)([, ]*)([0-9]*)-([0-9]*)-([0-9]*)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(it.line.text());
                    return !matcher.find();
                }
            });
            scheduleAdapter.update();
        }
    }

    private void init() {
        scheduleList = findViewById(R.id.scheduleList);
    }

    private void setupList() {
        scheduleAdapter = new ScheduleListAdapter(this, schedule);
        scheduleList.setHasFixedSize(true);
        scheduleList.setLayoutManager(new LinearLayoutManager(this));
        scheduleList.setAdapter(scheduleAdapter);
    }

    private Document readPage() {
        AtomicReference<Document> document = new AtomicReference<>();
        AtomicReference<Boolean> isDone = new AtomicReference<>(false);

        new Thread(() -> {
            try {
                document.set(Jsoup.connect("https://nvna.eu/wp/?group=140241&queryType=group&Week=7").get());
            } catch (Exception e) {
                System.err.println("Document error");
            }
            isDone.set(true);
        }).start();

        while (!isDone.get()) {
        }
        return document.get();
    }
}